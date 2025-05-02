package tpi.dgrv4.gateway.component.job;

import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.component.cache.core.CacheValueAdapter;
import tpi.dgrv4.gateway.component.cache.core.GenericCache;
import tpi.dgrv4.gateway.keeper.TPILogger;

@SuppressWarnings("serial")
public class RefreshCacheJob extends DeferrableJob {

	public final static String GROUP_ID = "refreshCacheJob";
	
	public static ConcurrentHashMap<String, Long> alreadyRun = new ConcurrentHashMap<>();

	public final static long BUFFER_INTERVAL = 6000;
	
    // 時間閥值（毫秒）
    private final long timeThreshold = 6000;
	
	public long putIn2ndTime = -1L;

	private TPILogger logger;

	private GenericCache cache;

	private String key;

	private Supplier<?> supplier;

	private CacheValueAdapter adapter;

	@Autowired
	public RefreshCacheJob(String key, Supplier<?> supplier, CacheValueAdapter adapter, TPILogger logger,
			GenericCache cache) {
		// 啟用 API 自適用 cache , RefreshCacheJob 搭配 DummyJob
		super(GROUP_ID.concat("-").concat(key));  // "refreshCacheJob-RDB_PK"
		this.key = key;
		this.supplier = supplier;
		this.adapter = adapter;
		this.logger = logger;
		this.cache = cache;
	}

	// 追蹤排程器是否運行中
    private static final AtomicBoolean isRunning = new AtomicBoolean(false);

    // 呼叫執行緒調度器，並指定執行緒的轉發, corePoolSize = 1
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread thread = Executors.defaultThreadFactory().newThread(r);
			thread.setName("RefreshCacheJob-Scheduler");
			return thread;
		}
	});
	
    // 儲存排程任務的引用
    private ScheduledFuture<?> scheduledTask;

    // 啟動定時任務，確保只執行一次
    public synchronized boolean startCleanupTask() {
        // 如果任務已在運行中，則不重複啟動
        if (!isRunning.compareAndSet(false, true)) {
            return false;
        }
        
        scheduler.scheduleAtFixedRate(this::cleanupExpiredEntries, 0, 3, TimeUnit.SECONDS);
        TPILogger.tl.info("...RefreshCacheJob cleanupExpired task startup...");
        return true;
    }
    
    // 停止定時任務
    public void stopCleanupTask() {
        scheduler.shutdown();
        System.out.println("Cache cleanup task stopped");
    }
    
    // 清理過期條目的方法
    private void cleanupExpiredEntries() {
        long currentTime = System.currentTimeMillis();
        
        // 使用迭代器安全地遍歷和移除元素
        Iterator<Map.Entry<String, Long>> iterator = alreadyRun.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            String key = entry.getKey();
            Long timestamp = entry.getValue();
            
            // 檢查時間戳是否超過閾值
            if (currentTime - timestamp > timeThreshold) {
                // 移除過期條目
                iterator.remove();
                
                // 使用虛擬執行緒執行自定義操作
                Thread.startVirtualThread(() -> {
        			// 更新 Cache 值 (真正存取 DB)
        			updateMemValue();
                });
            }
        }
        
        cache.clean(); //只清除到期資料
    }
	
	public void runJobProc() {
		// "refreshCacheJob-RDB_PK", 直接更新它的值, 代表對 RDB 發出請求
		RefreshCacheJob.alreadyRun.put(this.getGroupId(), Long.valueOf(System.currentTimeMillis()));
		
		startCleanupTask(); //定時任務，只會執行一次
		
	}

	@Override
	public void runJob(JobHelperImpl jobHelper, JobManager jobManager) {
		//   19
		int qsize = getDeferrableJM(jobManager).getExecutor2ndQueueSize();
		if (qsize == -1) {
			markIdAndDoJob(jobManager); // 若是 UT 時直接做, 不用等待, 更新 Cache 值 (真正存取 DB)
			return;
		}
		
		runJobProc(); // 不走 UT 流程, 時間寫入 HashMap, 然後等待排程來更新 DB
		
//		// 讓工作不要太快被執行, 它會動態 sleep(n) , pause() 棄用
//		boolean isTooEarly = pause2(jobManager);
//		if (isTooEarly) {
//			//System.out.println("TooEealy Abort g_id:" + this.getGroupId());
//			return ;
//		}
//		
//		// 要檢查 2nd 中是否有相同的 key
//		if (jobManager.buff2nd.containsKey(this.getGroupId())) {
//			//System.out.println("Abort g_id:" + this.getGroupId());
//			return ; // 因為有相的 groupdId , 所以放工作, 留給下次的 job 來執行
//		}
//		
//		int cnt = jobManager.count(this.getGroupId());
//		if (cnt > 0) {
//			this.logger.trace("Forsake job (" + this.getId() + ")");
//			return;
//		}
//		
//		if (getCache() == null) {
//			this.logger.error("Cannot acquire cache instance!");
//			return;
//		}
//		
//		// 針對 group id 標記, 更新 Cache 值 (真正存取 DB)
//		markIdAndDoJob(jobManager);
	}
	
	private void markIdAndDoJob(JobManager jobManager) {
		synchronized (alreadyRun) {
			if ( ! RefreshCacheJob.alreadyRun.containsKey(this.getGroupId())) {
				// 找不到表示還沒有執行 Query DB, 那就標記起來
				RefreshCacheJob.alreadyRun.put(this.getGroupId(), Long.valueOf(System.currentTimeMillis()));
			} else {
				return;
			}
			// 更新 Cache 值 (真正存取 DB)
			updateMemValue();
	
			// for UT
			releaseMark(jobManager);
		}
	}

	private void releaseMark(JobManager jobManager) {
		int qsize = getDeferrableJM(jobManager).getExecutor2ndQueueSize();
		if (qsize == -1) {
			// 釋放標記 (UT)
			RefreshCacheJob.alreadyRun.remove(this.getGroupId()); 
			return ;
		} else {
			// 釋放標記 (PROD)
//			executorRemove.execute(()->{
//				mySleep(BUFFER_INTERVAL); // 延後 釋放 因為剛剛才做過而
//				RefreshCacheJob.alreadyRun.remove(this.getGroupId());
//			});
			try {
				synchronized (alreadyRun) {
					alreadyRun.wait(BUFFER_INTERVAL);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			RefreshCacheJob.alreadyRun.remove(this.getGroupId());
		}
	
	}

//	private ExecutorService executorRemove = Executors.newFixedThreadPool(20);

	private Object updateMemValue() {
		// 真正存取 DB
		Object value = getSupplier().get();
		this.logger.trace("Refresh cache by key: " + getKey());
		getCache().put(getKey(), value, this.adapter);
		
		// TODO, John 測試用, 印出來表示有更新
//		if (value != null && value.toString().contains("API開關是否啟用")) {
//			String t1 = RandomSeqLongUtil.getRandomLongHexString(RandomSeqLongUtil.getRandomLongByYYYYMMDDHHMMSS(), RandomLongTypeEnum.YYYYMMDDHHMMSS).substring(0, 15);
//			System.err.println("...Refresh cache by key:"+getKey()+"..."+value); //輸出 tpi.dgrv4.gateway.service.ProxyMethodService:queryByIdCallApi:193596706.....tpi.dgrv4.gateway.vo.AutoCacheRespVo@195ba0b0
//		}
		
		return value;
	}

	@Override
	public void replace(DeferrableJob source) {
		RefreshCacheJob src = (RefreshCacheJob) source;
		this.key = src.getKey();
		this.supplier = src.getSupplier();
	}

	/**
	 * 計算現在距離 JOB 被排入延遲佇列的時間, 是否已超過 BUFFER_INTERVAL, 若已超過則不等待, 直接執行
	 */
	protected void pause() {
		Predicate<ZonedDateTime> tester = (t) -> {
			try {
				//System.out.println("排入延遲佇列的時間: " + Math.abs(t.toInstant().toEpochMilli() - getDeferTimestamp()));
				return Math.abs(t.toInstant().toEpochMilli() - getDeferTimestamp()) >= BUFFER_INTERVAL;
			} catch (Exception e) {
				return false;
			}
		};

		while (!tester.test(ZonedDateTime.now())) {
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				this.logger.error("Wait error! (" + this.getGroupId() + ")\n" + StackTraceUtil.logStackTrace(e));
				Thread.currentThread().interrupt();
			}
		}
		this.logger.trace("Ready to refresh...(" + this.getGroupId() + ")");
	}
	
	protected boolean pause2(JobManager jobManager) {
		long expectedDelayTime = putIn2ndTime + BUFFER_INTERVAL; //預計要在未來的這個時間點才能更新 DB
		long diffTime = expectedDelayTime - System.currentTimeMillis(); //若尚未到未來的時間, 則為正數
		if (diffTime > 0) {
			//如果已有人執行過了, 把最新的時間再更新回去
			Long lastNewExecuteTime = alreadyRun.get(getGroupId());
			if (lastNewExecuteTime != null && lastNewExecuteTime > this.putIn2ndTime) {
				this.putIn2ndTime = lastNewExecuteTime + BUFFER_INTERVAL;
			}

			// 2025.3.10 改放回 main queue
			mySleep(diffTime); //補停時間
			jobManager.jobHelper.add(this);
			// put 2nd Queue(還沒有到 6 秒就再放回去 2nd)
//			synchronized (jobManager.buff2nd) {
//				jobManager.buff2nd.put(this.getGroupId(), this);
//				jobManager.buff2nd.notifyAll();
//			}
			
			// 為了不讓 2nd 又利刻取出來, 所以在此 Thread 停 n 秒
//			sleepByQueueSize(jobManager);
			// 再觸發一次 take2nd Job
//			jobManager.doAgaint2nd();
			return true; // 太早了
		}
		this.logger.trace("Ready to refresh...(" + this.getGroupId() + ")");
		return false; // 已是 6 秒之後了
	}
	
	private void sleepByQueueSize(JobManager jobManager) {
		int qsize = getDeferrableJM(jobManager).getExecutor2ndQueueSize();
		if (qsize == -1) {
			// UT時不 sleep
		} else if (qsize < 20) {
			mySleep(1000L);
		} else {
			mySleep(50L);
		}
		
//		if(qsize > 100 && (qsize % 100 == 0)) {
//			System.out.println("Waiting tasks: " + qsize);
//		}
	}

	private DeferrableJobManager getDeferrableJM(JobManager jobManager) {
		return ((DeferrableJobManager)jobManager);
	}

	private void mySleep(long t) {
		try {
			Thread.sleep(t);
		} catch (Exception e) {
			Thread.currentThread().interrupt();
		}		
	}

	protected GenericCache getCache() {
		return this.cache;
	}

	protected String getKey() {
		return this.key;
	}

	protected Supplier<?> getSupplier() {
		return this.supplier;
	}

}
