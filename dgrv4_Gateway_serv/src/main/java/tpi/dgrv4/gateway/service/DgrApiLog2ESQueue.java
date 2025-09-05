package tpi.dgrv4.gateway.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.es.EsHttpClient;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.httpu.utils.HttpsConnectionChecker;

public class DgrApiLog2ESQueue {
    // 靜態配置參數
    private static final int QUEUE_CAPACITY = 20000;
    private static final int MAX_BATCH_SIZE = 2000;
    private static final int MAX_BULK_BYTES = 20 * 1024 * 1024; // 20MB
    private static final int CONCURRENT_TASKS = 1; // 可同時處理的任務數 (activeTasks 不可大於 20)
    private static final long FLUSH_INTERVAL_MS = 1000; // 1秒刷新間隔
    
    // 計數器和標誌
    public static final AtomicInteger abortNum = new AtomicInteger(0);
    private static final AtomicBoolean startFlag = new AtomicBoolean(false); 	// 確保只能做一次
    public static AtomicInteger activeTasks = new AtomicInteger(0);     //活躍任務計數

    
    // 使用 Virtual Thread Executor
    private static final ExecutorService virtualThreadExecutor = 
        Executors.newVirtualThreadPerTaskExecutor();
    
    // 定時任務執行器，使用平台線程
    private static final ScheduledExecutorService scheduledExecutor = 
        Executors.newSingleThreadScheduledExecutor();
    
    // 隊列
    public static final BlockingQueue<DgrApiLog2ESQueue> ES_LoggerQueue = 
        new LinkedBlockingQueue<>(QUEUE_CAPACITY);
	
    
    // 實例變數
    private final String strJson;
    private final String esReqUrl;
    private final String _id;
    private final int timeout;
    
    // 構造函數
    public DgrApiLog2ESQueue(int timeout, String strJson, String esReqUrl, String _id) {
        this.timeout = timeout;
        this.strJson = strJson;
        this.esReqUrl = esReqUrl;
        this._id = _id;
    }
    
	public DgrApiLog2ESQueue(String[] arrEsUrl, String[] arrIdPwd, int timeout, String indexName, String strJson, String esReqUrl, String _id) {
		this.arrEsUrl = arrEsUrl;
		this.arrIdPwd = arrIdPwd;
		this.timeout = timeout;
		this.indexName = indexName;
		this.strJson = strJson;
		this.esReqUrl = esReqUrl;
		this._id = _id;
	}
	
	private static Float diskFreeThreshHold;
	private static int deletePercent;
	private static boolean allowWriteElastic;
	private static boolean checkStatusFromFile;
    /**
     * 將日誌添加到隊列
     */
    public static void put(DgrApiLog2ESQueue logObj, Float diskFreeThreshHold, int deletePercent, boolean allowWriteElastic, boolean checkStatusFromFile) {
    	DgrApiLog2ESQueue.diskFreeThreshHold = diskFreeThreshHold; 
    	DgrApiLog2ESQueue.deletePercent = deletePercent;
    	DgrApiLog2ESQueue.allowWriteElastic = allowWriteElastic;
    	DgrApiLog2ESQueue.checkStatusFromFile = checkStatusFromFile;
        putByPoll(logObj);
        startProcessing();
    }
    
    /**
     * 添加到隊列，隊列滿時丟棄最舊的
     */
    private static void putByPoll(DgrApiLog2ESQueue logObj) {
        if (!ES_LoggerQueue.offer(logObj)) {
            ES_LoggerQueue.poll(); // 丟棄最舊的
            
			if (!ES_LoggerQueue.offer(logObj)) {// 添加新元素到隊尾
				TPILogger.tl.warn("Failed to insert job into queue");
			}
            
            int count = abortNum.incrementAndGet();
            if (count % 100 == 0) {
                TPILogger.tl.warn("ES log queue is full, discarded " + count + " requests");
            }
        }
    }
    
    
    /**
     * 啟動處理線程和定時刷新任務
     */
    private static void startProcessing() {
    	//只被啟動一次(檢查 startFlag 的當前值是否等於 false, 如果是 false，則將其設置為 true 並返回 true)
        if (startFlag.compareAndSet(false, true)) {
            // 啟動處理協調器
            virtualThreadExecutor.submit(DgrApiLog2ESQueue::processQueueCoordinator);
            
            // 啟動定時刷新任務
            scheduledExecutor.scheduleAtFixedRate(
                DgrApiLog2ESQueue::flushPendingItems, 
                FLUSH_INTERVAL_MS, FLUSH_INTERVAL_MS, TimeUnit.MILLISECONDS
            );
            
            TPILogger.tl.debug("[#queueTrace#] \nES log Queue has been STARTED with Virtual Threads\n");
        }
    }
    
    
    /**
     * 隊列處理協調器 - 使用單一虛擬線程來協調批次處理
     */
    private static void processQueueCoordinator() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // 如果當前活躍任務少於上限，則嘗試處理一個新批次
                if (activeTasks.get() < CONCURRENT_TASKS && !ES_LoggerQueue.isEmpty()) {
                    List<DgrApiLog2ESQueue> batch = new ArrayList<>(MAX_BATCH_SIZE);
                    StringBuilder bulkDatas = new StringBuilder(MAX_BULK_BYTES);
                    
                    // 收集一批數據到 bulkDatas
                    collectBatch(batch, bulkDatas);
                    
                    if (!batch.isEmpty()) {
                        // 增加活躍任務計數
                        activeTasks.incrementAndGet();
                        
                        // 使用虛擬線程提交寫入任務
                        virtualThreadExecutor.submit(() -> {
                            try {
                                writeBulkFile(batch, bulkDatas);
                            } finally {
                                // 任務完成，減少活躍任務計數
                                activeTasks.decrementAndGet();
                            }
                        });
                    }
                } else {
                    // 等待一小段時間，避免CPU空轉
                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                TPILogger.tl.error("Queue coordinator interrupted: " + StackTraceUtil.logTpiShortStackTrace(e));
                break;
            } catch (Exception e) {
                TPILogger.tl.error("Error in queue coordinator: " + StackTraceUtil.logTpiShortStackTrace(e));
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
    
    

    /**
     * 收集一批數據
     */
    private static void collectBatch(List<DgrApiLog2ESQueue> batch, StringBuilder bulkDatas) {
        // 首先嘗試獲取一個項目
        DgrApiLog2ESQueue item = ES_LoggerQueue.poll();
        if (item == null) return;
        
        // 添加到批次
        batch.add(item);
        appendToBulk(item, bulkDatas);
        
        // 嘗試收集更多項目，但有大小限制
        int collectedCount = 1;
        while (collectedCount < MAX_BATCH_SIZE && bulkDatas.length() < MAX_BULK_BYTES) {
            item = ES_LoggerQueue.poll();
            if (item == null) break;
            
            batch.add(item);
            appendToBulk(item, bulkDatas);
            collectedCount++;
        }
    }
    
    /**
     * 定時刷新任務
     */
    private static void flushPendingItems() {
        if (ES_LoggerQueue.isEmpty()) return;
        
        // 強制啟動一次處理
        if (startFlag.get()) {
            virtualThreadExecutor.submit(() -> {
                List<DgrApiLog2ESQueue> batch = new ArrayList<>();
                StringBuilder bulkDatas = new StringBuilder();
                
                // 從隊列中收集一批數據
                collectBatch(batch, bulkDatas);
                
                if (!batch.isEmpty()) {
                    writeBulkFile(batch, bulkDatas);
                }
            });
        }
    }
    
    /**
     * 將一個項目添加到bulk數據中
     */
    private static void appendToBulk(DgrApiLog2ESQueue item, StringBuilder bulkDatas) {
        String firstLine = "{\"index\":{\"_id\":\"" + item._id + "\"}}";
        bulkDatas.append(firstLine).append("\n")
                 .append(item.strJson).append("\n");
    }
    
    /**
     * 寫入一批數據到ES File
     */
    private static void writeBulkFile(List<DgrApiLog2ESQueue> batch, StringBuilder bulkDatas) {
        if (batch.isEmpty() || bulkDatas.length() == 0) return;
        
        try {
            // 使用批次中的第一個元素進行寫入(因為 writeES 是物件方法, 雖然批次中有多個元素，但批量寫入只需要執行一次)
            batch.get(0).writeES(bulkDatas.toString());
            
            // 寫入成功日誌
            if (batch.size() > 1) {
                TPILogger.tl.debug("Successfully wrote " + batch.size() + 
                                  " records (" + bulkDatas.length() + " bytes) to ES");
            }
        } catch (OutOfMemoryError e) {
            handleOutOfMemoryError(e);
        } catch (Exception e) {
            TPILogger.tl.error("Failed to write bulk data to ES: " + StackTraceUtil.logTpiShortStackTrace(e));
            // 可以考慮實現重試機制或將失敗的批次保存到磁盤
        }
    }
    
    /**
     * 處理OutOfMemory錯誤
     */
    private static void handleOutOfMemoryError(Error e) {
        // 清空隊列
        ES_LoggerQueue.clear();
        
        StringBuilder sb = new StringBuilder();
        sb.append(StackTraceUtil.logStackTrace(e));
        sb.append("\n\t.....ES_LoggerQueue cleared.....");
        sb.append("\n\t.....JVM.freeMemory() " + 
                  Runtime.getRuntime().freeMemory() / 1024 / 1024 + "MB.....");
        
        TPILogger.tl.error(sb.toString());
        
        // 建議GC回收
        System.gc();
        
        // 不直接退出，讓系統有機會恢復
        TPILogger.tl.info("System recovering from OutOfMemoryError, reduced queue load");
    }
    
    
    
    
	
//	private final static int BUFFER_SIZE = 500; // 3分鐘壓測, DgrApiLog2ESQueue REV ES_LoggerQueue size:31,993
//	public final static BlockingQueue<DgrApiLog2ESQueue> ES_LoggerQueue = new ArrayBlockingQueue<DgrApiLog2ESQueue>(BUFFER_SIZE);

	private static String[] arrEsUrl;
	private static String[] arrIdPwd;
	private static String indexName;

	
    /**
     * 優雅關閉方法
     */
    public static void shutdown() {
        // 設置標誌，不再接受新任務
        startFlag.set(false);
        
        // 關閉調度器
        scheduledExecutor.shutdown();
        
        // 關閉虛擬線程執行器
        virtualThreadExecutor.shutdown();
        try {
            if (!virtualThreadExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                virtualThreadExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            virtualThreadExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        TPILogger.tl.info("ES log queue shutdown completed. Remaining logs: " + 
                         ES_LoggerQueue.size());
    }
    
		
	private void writeES(String bulkBody) {
		//提供了連接池和 keep-alive 機制
		EsHttpClient httpClient = EsHttpClient.getInstance();
	
		String esReqUrl = getEsReqUrl(arrEsUrl, arrIdPwd, indexName, timeout);
		// 如是第一次連 ES or 連失敗則 workIndex = -1, 需要重新 find connection
//		if (workIndex == -1) {
//			esReqUrl = getEsReqUrl(arrEsUrl, arrIdPwd, indexName, timeout);
//		}
		
		if (esReqUrl == null) {
			TPILogger.tl.error("esReqUrl == null, Find ES URL has bean FAIL !");
			return;
		}
		
		// 到了這一步表示 ES 活著, 於是可以代入 Authorization
		Map<String, String> header = new HashMap<>();
		header.put("Accept", "application/json");
		header.put("Content-Type", "application/json");
		header.put("Authorization", "Basic " + arrIdPwd[workIndex]);
		
		try {
			int httpCode = httpClient.bulkWrite(esReqUrl, bulkBody, header, diskFreeThreshHold, deletePercent, allowWriteElastic, checkStatusFromFile);
			
//			System.out.println("...POST 2...");
			// 告知寫入成功或失敗
			if (httpCode >= 200 && httpCode < 400) {

				TPILogger.tl.debug("...ES API Log to Disk ...OK ...http code = " + httpCode );
			} else {
				TPILogger.tl.error("...ES API log to Disk FAIL , status code = " + httpCode);
			}
		} catch (Exception e) {
			// JVM memory
			String freeMemory = Runtime.getRuntime().freeMemory() / 1024 / 1024 + "MB";
			String totalMemory = Runtime.getRuntime().totalMemory() / 1024 / 1024 + "MB";
			String maxMemory = Runtime.getRuntime().maxMemory() / 1024 / 1024 + "MB";
			TPILogger.tl.info(freeMemory + " / " + totalMemory + " / " + maxMemory + "...Memory(free/total/Max)" + "\n");
			TPILogger.tl.error("\nesReqUrl = " + esReqUrl + "\n" + StackTraceUtil.logStackTrace(e));
		}

		
	}
	
	public static int workIndex = -1; // -1:不可用, 0~n:為可用URL
	public static boolean esUrlCheckConnection = false; // 預設不偵測
	
	/**
	 * 回傳選定的一條 Elastic URL 
	 * @param arrEsUrl EX: https://10.20.30.88:19200/,https://10.20.30.88:29200/
	 * @param arrIdPwd EX: id1:pwd1,id2:pwd2
	 * @param indexName
	 * @param timeout
	 * @return
	 */
	public static String getEsReqUrl(String[] arrEsUrl, String[] arrIdPwd, String indexName, int timeout) {
	    // 檢查陣列是否有內容
	    if (arrEsUrl == null || arrEsUrl.length == 0) {
	        return null;
	    }
	    
	    // 檢查是否需要進行連接檢測
	    if (esUrlCheckConnection) {
	        // 需要檢測連線，找出可用的連線
	        workIndex = -1;
	        int index = 0;
	        boolean isConnection = false;
	        for (; index < arrEsUrl.length; index++) {
	            try {
	                isConnection = HttpsConnectionChecker.checkConnection(arrEsUrl[index]);
	            } catch (Exception e) {
	                TPILogger.tl.error("Connection failed! Url = " + arrEsUrl[index] + "\n" + StackTraceUtil.logStackTrace(e));
	            }

	            if (isConnection) {
	                workIndex = index;
	                break;
	            }
	        }
	    } else {
	        // 不需要檢測連線，直接選擇一個URL
	        // 輪流選擇下一個URL（Round Robin）
	        workIndex = (workIndex + 1) % arrEsUrl.length;
	    }
		
		// 如果算完還是 -1 表示, 表示沒有可用的連線
		if(workIndex > -1) {
//			String esReqUrl = arrEsUrl[workIndex] + indexName + "/_doc";  // 這是單筆處理
			String esReqUrl = arrEsUrl[workIndex] + indexName + "/_bulk"; // 改為批次處理
			return esReqUrl;
		}
		
		// 沒有可用的 URL
		return null;
	}
	


}
