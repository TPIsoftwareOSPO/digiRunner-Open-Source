package tpi.dgrv4.dpaa.es;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.DpaaSystemInfoHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;

public class ESLogBuffer {
    private static volatile ESLogBuffer instance;
    private final CloseableHttpClient httpClient;
    
    private static final String LOG_DIR = "apilogs";
    private static final String APPEND_SUFFIX = ".logappd";
    private static final String COMPLETE_SUFFIX = ".complete";
    private static final String HTTP_SUFFIX = ".loghttp";
    private static final String SUCCESS_SUFFIX = ".logok";
    private static final String FAILED_SUFFIX = ".logfailed";
    private static final String CONFIG_SUFFIX = ".config";
    private static final String YYYY_MM_DD_HH_MM_SS_SSS = "yyyyMMddHHmmssSSS";
    
    
    private static final List<String> LOG_SUFFIXES = Arrays.asList(
        APPEND_SUFFIX,
        HTTP_SUFFIX,
        SUCCESS_SUFFIX,
        FAILED_SUFFIX,
        CONFIG_SUFFIX,
        COMPLETE_SUFFIX
    );
    
//    private static final ExecutorService writeExecutor = Executors.newSingleThreadExecutor(r -> {
//        Thread thread = new Thread(r);
//        thread.setName("ES-filelog-writer");
//        thread.setPriority(Thread.NORM_PRIORITY);
//        return thread;
//    });
    // 修改調度執行器，增加延遲和間隔時間
    private static final ScheduledExecutorService processExecutor = 
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r);
            thread.setName("ES-log-schedule");
            thread.setPriority(Thread.MIN_PRIORITY);  // 設置最低優先級
            return thread;
        }
    );
    
    // Add this as a new field in the ESLogBuffer class, alongside the other executors
    private static final ScheduledExecutorService cleanupExecutor = 
    Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setName("ES-cleanup-schedule");
        thread.setPriority(Thread.MIN_PRIORITY);
        return thread;
    });
    
    private void startFileProcessor() {
        processExecutor.scheduleWithFixedDelay(() -> {
            if (!isProcessing) {
                processLogFiles();
            }
        }, 60*2, 5, TimeUnit.SECONDS);
    }
    
    // Add this to the constructor (ESLogBuffer method), after startFileProcessor()
    private void startCleanupProcessor() {
        cleanupExecutor.scheduleWithFixedDelay(() -> {
            cleanupExpiredFiles();
        }, 60*2, 30, TimeUnit.SECONDS);
    }
    
    // Add this as a constant in the class
    private static final long EXPIRED_FILE_MINUTES = 1; // 1 minute expiration time
    
    // [新增 1] 每批次處理的最大檔案數
    private static final int MAX_BATCH_SIZE = 1000;      // 正常負載時的批次大小
    private static final int MIN_BATCH_SIZE = 1;      // 高負載時的批次大小
    
    // [新增 2] 批次間隔時間（毫秒）
    private static final int BATCH_INTERVAL = 100;
    // [新增 3] CPU 負載閾值，超過此值則暫停處理
    private static final double CPU_THRESHOLD = 0.95; // 95%
    
    // [新增 4] 檢查系統 CPU 負載的方法
    private boolean isSystemBusy() {
    	double normalizedLoad = DpaaSystemInfoHelper.getInstance().getCachedSystemInfo().getCpu();
    	sysout(String.format("系統負載 (%.2f)", normalizedLoad));
        if (normalizedLoad > CPU_THRESHOLD) {
        	sysout(String.format("系統負載較高 (%.2f)，暫停處理", normalizedLoad));
            return true;
        }
        return false;
    }

    private static final AtomicInteger retryCount = new AtomicInteger(0);
    private static final int MAX_RETRIES = 2;
    private volatile boolean isProcessing = false;

    // Add sysout proxy methods
    private static void systrace(String message) {
//        System.out.println("[ESLogBuffer] " + message);
    }
    
    private static void sysoutdebug(String message) {
//    	System.out.println("[ESLogBuffer] " + message);
    }
    
    // 設定日誌輸出間隔（3 分鐘，單位毫秒）
    private static final long LOG_INTERVAL = 1 * 60 * 1000;

    // 儲存上次日誌輸出的時間
    private static final AtomicLong lastLogTime = new AtomicLong(0);
    
    private static void sysout(String message) {
    	System.out.println("[ESLogBuffer] " + message);
    }
    
    private static void sysoutRateLimited(String message) {
    	System.out.println("[ESLogBuffer] " + message);
    	
    	// 取得當前時間
    	long currentTime = System.currentTimeMillis();
    	// 取得上次日誌輸出的時間
    	long previousLogTime = lastLogTime.get();
    	// 如果距離上次日誌已經過了指定時間，則輸出日誌
    	if (currentTime - previousLogTime > LOG_INTERVAL && lastLogTime.compareAndSet(previousLogTime, currentTime)) {
    		TPILogger.tl.info("[ESLogBuffer] " + message); //超過 3 分鐘才會執行
    	}
    }

    private static void syserr(String message) {
        System.err.println("[ESLogBuffer] " + message);
    }

    private static void syserr(String message, Throwable e) {
        System.err.println("[ESLogBuffer] " + message);
        TPILogger.tl.error("[ESLogBuffer] " + message + "\n" + StackTraceUtil.logTpiShortStackTrace(e));
    }

    private Float diskFreeThreshHold = 0.2f;
    private int deletePercent = 80;
    private boolean allowWriteElastic = false;
	public static boolean enableRetry = false; //預設不做 retry 3 次
    private ESLogBuffer(CloseableHttpClient httpClient, Float diskFreeThreshHold, int deletePercent, boolean allowWriteElastic) {
        this.httpClient = httpClient;
        this.diskFreeThreshHold = diskFreeThreshHold;
        this.deletePercent = deletePercent;
        this.allowWriteElastic = allowWriteElastic;
        createLogDirectoryIfNeeded();
        startFileProcessor();
        startCleanupProcessor();
    }
    
    
    private void cleanupExpiredFiles() {
    	if (! allowWriteElastic) {
            systrace("不允許寫入 Elastic，跳過本次處理");
            return;
        }
    	
        try {
            TPILogger.tl.info("開始清理每天過期、佔用高磁碟容量、已完成的檔案...");
            
            retainRecentLogs(1); //保留 n 天
            cleanFilesOnLowDiskSpace(diskFreeThreshHold, deletePercent); // disk free 剩 0.05 就刪 30%
            cleanCompleteFile();
            
        } catch (NoSuchFileException e) {
        	systrace("找不到檔案, 可能被 clear 排程刪掉了, 所以不用處理它");
        } catch (IOException e) {
            syserr("清理過期檔案時發生錯誤", e);
        } catch (Exception e) {
            syserr("清理過程發生未預期的錯誤", e);
        }
    }


    /**
     * 過期 1min 的成功(logok)或失敗檔案(logfailed)
     * */
    private void cleanCompleteFile() throws IOException {
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(EXPIRED_FILE_MINUTES);
        int removedFiles = 0;
        Map<String, List<Path>> fileGroups = new HashMap<>();
        
        Path logDir = Paths.get(LOG_DIR);
        if (!Files.exists(logDir)) {
            return;
        }
        
        try (Stream<Path> files = Files.list(logDir)) {
            // 先識別符合條件的 SUCCESS_SUFFIX 和 FAILED_SUFFIX 檔案
            List<Path> targetFiles = files
                    .filter(path -> {
                        String fileName = path.getFileName().toString();
                        return fileName.endsWith(SUCCESS_SUFFIX) 
                              || fileName.endsWith(FAILED_SUFFIX);
                    })
                    .filter(path -> {
                        LocalDateTime fileDate = extractDateFromFileName(path.getFileName().toString());
                        return fileDate != null && fileDate.isBefore(expirationTime);
                    })
                    .collect(Collectors.toList());
            
            if (targetFiles.isEmpty()) {
                systrace("沒有找到過期的成功或失敗檔案");
                return;
            }
            
            sysout("找到 " + targetFiles.size() + " 個過期檔案需要刪除");
            
            // 對於每個找到的檔案，識別它們的基本檔名
            for (Path targetFile : targetFiles) {
                String fileName = targetFile.getFileName().toString();
                String baseName = fileName;
                
                // 取得沒有後綴的基本檔名
                if (fileName.endsWith(SUCCESS_SUFFIX)) {
                    baseName = fileName.substring(0, fileName.length() - SUCCESS_SUFFIX.length());
                } else if (fileName.endsWith(FAILED_SUFFIX)) {
                    baseName = fileName.substring(0, fileName.length() - FAILED_SUFFIX.length());
                }
                
                // 將檔案按基本檔名分組
                fileGroups.computeIfAbsent(baseName, k -> new ArrayList<>()).add(targetFile);
                
                // 檢查並加入相關的 COMPLETE_SUFFIX 和 CONFIG_SUFFIX 檔案
                Path completePath = logDir.resolve(baseName + COMPLETE_SUFFIX);
                Path configPath = logDir.resolve(baseName + CONFIG_SUFFIX);
                
                if (Files.exists(completePath)) {
                    fileGroups.get(baseName).add(completePath);
                }
                
                if (Files.exists(configPath)) {
                    fileGroups.get(baseName).add(configPath);
                }
            }
            
            // 刪除所有識別出的檔案
            Path lastFile = null;
            for (List<Path> group : fileGroups.values()) {
                for (Path file : group) {
                    try {
                        Files.delete(file);
                        removedFiles++;
                        lastFile = file;
                        systrace("已刪除過期檔案: " + file);
                    } catch (NoSuchFileException e) {
                        systrace("找不到檔案, 可能被 clear 排程刪掉了, 所以不用處理它");
                    } catch (IOException e) {
                        syserr("刪除檔案時發生錯誤: " + file, e);
                    }
                }
            }
            
            TPILogger.tl.info("[過期的檔案清理] 完成，共刪除 " + removedFiles + " 個檔案, 本次最後一個刪掉的檔名:" + lastFile);
        }
    }

	private void createLogDirectoryIfNeeded() {
        try {
            Path directory = Paths.get(LOG_DIR);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create log directory", e);
        }
    }
    
    public static ESLogBuffer getInstance(CloseableHttpClient httpClient, Float diskFreeThreshHold, int deletePercent, boolean allowWriteElastic) {
        if (instance == null) {
            synchronized (ESLogBuffer.class) {
                if (instance == null) {
                    if (httpClient == null) {
                        throw new IllegalArgumentException("First call to getInstance must provide httpClient");
                    }
                    instance = new ESLogBuffer(httpClient, diskFreeThreshHold, deletePercent, allowWriteElastic);
                }
            }
        }
        return instance;
    }

    public static ESLogBuffer getInstance() {
        if (instance == null) {
            throw new IllegalStateException("LogBuffer not initialized. Call getInstance(httpClient) first");
        }
        return instance;
    }
    
    private String generateFileName() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS_SSS));
        // 添加 UUID 確保唯一性，即使在同一毫秒有多個請求
        String uniqueId = UUID.randomUUID().toString().substring(0, 5);
        return LOG_DIR + File.separator + timestamp + "_" + uniqueId;
    }
    
    public static int abortNum = 0;

    public int bulkWrite(String esUrl, String bulkBody, Map<String, String> headers) throws IOException {
        String baseFileName = generateFileName();
        String logFileName = baseFileName + APPEND_SUFFIX;
        String configFileName = baseFileName + CONFIG_SUFFIX;
        String markerFileName = baseFileName + COMPLETE_SUFFIX; // 新增完成標記檔案

        systrace("\n開始寫入日誌檔案: " + logFileName );
        
        // 使用虛擬線程執行 I/O 操作
        Thread.startVirtualThread(() -> {
            try {
                // 寫入日誌內容
                Path logPath = Paths.get(logFileName);
                Files.write(logPath, bulkBody.getBytes(), 
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                
                systrace("日誌內容寫入完成: " + logFileName);
                // 寫入配置檔案
                Properties config = new Properties();
                config.setProperty("esUrl", esUrl);
                headers.forEach((key, value) -> config.setProperty("header." + key, value));
                
                try (OutputStream out = Files.newOutputStream(Paths.get(configFileName))) {
                    config.store(out, "Log configuration");
                }
                systrace("配置檔案寫入完成: " + configFileName);
                sysoutRateLimited("Bulk size:" + bulkBody.getBytes().length + ", 寫入任務完成\n");
                
                //最後寫入標記檔案，表示所有檔案都已完成寫入
                try {
                    Files.createFile(Paths.get(markerFileName));
                } catch (FileAlreadyExistsException e) {
                    // 標記檔案已存在，這意味著另一個線程已經完成了相同的操作
                    // 這種情況不常見但不必失敗
                    sysout("標記檔案已存在 (不常見但可接受): " + markerFileName);
                }
            } catch (IOException e) {
            	syserr("寫入檔案失敗: " + e.getMessage(), e);
            }
        });

        // 立即返回，不等待虛擬線程完成
        return 202; // Accepted
    }
    

    /***
     * 確保：
     * 1.系統負載高時降低處理速度
     * 2.系統負載正常時提高效率
     * 3.在各個層次都有適當的暫停，避免資源過度使用
     */
    private void processLogFiles() {
        if (! allowWriteElastic) {
            systrace("不允許寫入 Elastic，跳過本次處理");
            return;
        }
        if (isProcessing) {
        	systrace("已有處理程序在執行中，跳過本次處理");
        	return;
        }
        
        try {
            isProcessing = true;
            Path logDirPath = Paths.get(LOG_DIR);
            if (!Files.exists(logDirPath)) {
                sysout("日誌目錄不存在: " + LOG_DIR);
                return;
            }
            
            // 只有當 filename.append_suffix 和 filename.complete 都存在時，
            // filename.append_suffix 才會被添加到 pendingFiles 中
            systrace("開始掃描日誌檔案...");
            List<Path> pendingFiles = new ArrayList<>();
            try (Stream<Path> stream = Files.list(logDirPath)) {
                stream.filter(path -> {
                    String basePath = path.toString().substring(0, path.toString().lastIndexOf('.'));
                    return path.toString().endsWith(APPEND_SUFFIX) && 
                           Files.exists(Paths.get(basePath + COMPLETE_SUFFIX));
                })
                .limit(isSystemBusy() ? MIN_BATCH_SIZE : MAX_BATCH_SIZE) // 限制/調控檔案
                .forEach(pendingFiles::add);
            }

            if (pendingFiles.isEmpty()) {
                systrace("沒有待處理的檔案...");
                return;
            }
            
            if (!pendingFiles.isEmpty()) {
            	sysoutdebug("找到 " + pendingFiles.size() + " 個待處理檔案");
                
                                
                Path lastFile = null;
				// [外層迴圈：控制批次進度]
                for (int i = 0; i < pendingFiles.size();) {  // 注意這裡沒有 i++，因為在內部控制進度
                	// 根據系統負載動態調整批次大小
                	int currentBatchSize = isSystemBusy() ? MIN_BATCH_SIZE : MAX_BATCH_SIZE;
                	sysoutdebug("動態調整批次大小：" + currentBatchSize);
                	int end = Math.min(i + currentBatchSize, pendingFiles.size());
                
                	// [內層迴圈：處理單個批次內的檔案]
                    for (int j = i; j < end; j++) {
                        Path file = pendingFiles.get(j);
                        try {
                        	sysoutdebug("處理檔案: " + file);
                            processLogFile(file);
                            lastFile  = file;
                        } catch (Exception e) {
                            syserr("處理檔案失敗: " + file, e);
                            continue;
                        }
                        
                        // [高負載時的等待間隔]
                        if (isSystemBusy()) {
                            try {
                                Thread.sleep(BATCH_INTERVAL * 15L);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return;
                            }
                        }
                    }
                    
                    // [更新批次進度]
                    i = end;
                    
                    // [批次間的等待間隔] && CPU 0.8
                    if (i < pendingFiles.size() && isSystemBusy()) {
                        try {
                            Thread.sleep(BATCH_INTERVAL);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }
                TPILogger.tl.info("處理程序完成，共處理 " + pendingFiles.size() + " 個檔案, 本次最後一個檔案: " + lastFile);
            }
        } catch (NoSuchFileException e) {
        	syserr("找不到檔案, 可能被 clear 排程刪掉了, 所以不用處理它");
        } catch (IOException e) {
            syserr("處理日誌檔案時發生錯誤: " + e.getMessage(), e);
        } catch (Exception e) {
            syserr("處理過程發生未預期的錯誤", e);
            Thread.currentThread().interrupt();
        } finally {
            isProcessing = false;
        }
    }

    private void processLogFile(Path file) {
        String filePath = file.toString();
        String baseName = filePath.substring(0, filePath.lastIndexOf('.'));
        Path httpPath = Paths.get(baseName + HTTP_SUFFIX);
        Path configPath = Paths.get(baseName + CONFIG_SUFFIX);

        try {
            // 讀取配置檔案
        	sysoutdebug("[處理檔案] 開始處理: " + file);
            Properties config = new Properties();
            try (InputStream in = Files.newInputStream(configPath)) {
                config.load(in);
            }

            // 獲取 URL 和 headers
            String esUrl = config.getProperty("esUrl");
            Map<String, String> headers = new HashMap<>();
            config.stringPropertyNames().stream()
                .filter(key -> key.startsWith("header."))
                .forEach(key -> headers.put(
                    key.substring("header.".length()), 
                    config.getProperty(key)));

            // 將日誌檔案移動到 HTTP 狀態
            sysoutdebug("[處理檔案] 移動到 HTTP 狀態: " + httpPath);
            Files.move(file, httpPath, StandardCopyOption.REPLACE_EXISTING);
            
            // 讀取並驗證日誌內容
            String content = new String(Files.readAllBytes(httpPath));
            validateBulkContent(content);  // 新增的驗證方法
            
            // 發送到 Elastic
            //sysout("[處理檔案] 開始發送到 Elastic: " + esUrl);
            HttpPost post = new HttpPost(esUrl);
            headers.forEach(post::addHeader);
            post.setEntity(new StringEntity(content, "UTF-8"));  // 明確指定編碼
            
            // 增加請求超時設定
            post.setConfig(RequestConfig.custom()
                .setConnectTimeout(5000)
                .setSocketTimeout(30000)
                .build());
            
            // 執行請求並獲取完整回應
            String responseBody = httpClient.execute(post, response -> {
                int statusCode = response.getStatusLine().getStatusCode();
                sysoutRateLimited("[處理檔案] 開始發送到 Elastic: " + esUrl + ", \nHTTP code: " + statusCode);
                String responseContent = EntityUtils.toString(response.getEntity());
                sysoutdebug("[處理檔案] Elastic 回應內容: " + responseContent.substring(0,50) + "..." + responseContent.substring(responseContent.length()-20, responseContent.length()));
                return responseContent;
            });

            // 解析回應
            boolean hasErrors = responseBody.contains("\"errors\":true");
            if (hasErrors) {
                syserr("[處理檔案] Bulk 請求含有錯誤: " + responseBody);
                throw new IOException("Bulk request contains errors");
            }

            // 成功後移動到 success 狀態
            Path successPath = Paths.get(baseName + SUCCESS_SUFFIX);
            sysoutdebug("[處理檔案] 發送成功，移動到成功狀態: " + successPath + "\n\n");
            Files.move(httpPath, successPath, StandardCopyOption.REPLACE_EXISTING);
            
        } catch (NoSuchFileException e) {
        	systrace("找不到檔案, 可能被 clear 排程刪掉了, 所以不用處理它");
        } catch (IOException e) {
            syserr("[處理檔案] 錯誤: " + e.getMessage() + ", File:" + filePath.toString(), e);
            handleProcessingError(filePath, e);
        }
    }

    private void validateBulkContent(String content) throws IOException {
        String[] lines = content.split("\n");
        sysout("[驗證] 總行數: " + lines.length);

        for (int i = 0; i < lines.length; i += 2) {
            // 檢查 metadata 行
            if (!lines[i].contains("\"index\":{") || !lines[i].contains("\"_id\":")) {
                syserr("[驗證] 無效的 metadata 行: " + lines[i]);
                throw new IOException("Invalid metadata line at " + i);
            }
            
            // 檢查資料行
            if (!lines[i+1].startsWith("{") || !lines[i+1].endsWith("}")) {
                syserr("[驗證] 無效的資料行: " + lines[i+1]);
                throw new IOException("Invalid data line at " + (i+1));
            }
            
            if (i<6 || i>lines.length-6) {
            	systrace("[驗證] 第 " + (i/2 + 1) + " 組資料驗證通過");
            } 
        }
    }

    private void handleProcessingError(String fileName, Exception e) {
        sysout("開始錯誤處理流程，目前重試次數: " + retryCount.get());
        
        if (enableRetry && retryCount.incrementAndGet() <= MAX_RETRIES) {
            sysout("將進行重試，嘗試次數: " + retryCount.get() + "/" + MAX_RETRIES);
            
            // === 新增的重試邏輯開始 ===
            try {
                // 短暫延遲後再重試 (指數退避策略)
                int delayMs = 1000 * (int)Math.pow(2, retryCount.get() - 1); // 1秒, 2秒, 4秒
                sysout("等待 " + delayMs + " 毫秒後重試...");
                Thread.sleep(delayMs);
                
                // 直接重試處理文件
                sysout("開始第 " + retryCount.get() + " 次重試處理: " + fileName);
                 
                // '.loghttp' 還原 '.logappd'
                String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
                Path failedPath = Paths.get(baseName + APPEND_SUFFIX);
                Path httpPath = Paths.get(baseName + HTTP_SUFFIX);
                Files.move(httpPath, failedPath, StandardCopyOption.REPLACE_EXISTING);
        
                
                // 檢查文件是否仍然存在
                Path filePath = Paths.get(fileName);
                if (Files.exists(filePath)) {
                    processLogFile(filePath);
                    //TPILogger.tl.info("[ESLogBuffer] 重試已執行! ::" + fileName);
                    retryCount.set(0); // 重試成功後重置計數器
                } else {
                    throw new NoSuchFileException("重試時文件不存在: " + fileName);
                }
                return; // 重試成功，退出方法
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                syserr("重試等待被中斷", ie);
            } catch (NoSuchFileException nsfe) {
                syserr("重試失敗: " + nsfe.getMessage(), nsfe);
            } catch (Exception retryError) {
                syserr("進入失敗重試流程: " + fileName + ", enableRetry::" + enableRetry, retryError);
                // 重試失敗，會繼續嘗試下一次重試 (如果還有剩餘次數)
                handleProcessingError(fileName, retryError); // 遞迴調用以嘗試下一次重試
                return; // 防止執行到移動文件的代碼
            }
            // === 新增的重試邏輯結束 ===
            
            // 刪除這行，因為我們現在直接重試而不是加入隊列
            // processingQueue.offer(fileName);
            return;
        }
        
        if (!enableRetry) {
        	TPILogger.tl.info("重試功能已停用，直接移動到失敗狀態, filename::" + fileName);
        } else {
        	TPILogger.tl.info("超過重試次數，移動到失敗狀態, filename::" + fileName);
        }
        
        try {
            Path source = Paths.get(fileName);
            String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
            Path httpPath = Paths.get(baseName + HTTP_SUFFIX);
            Path failedPath = Paths.get(baseName + FAILED_SUFFIX);
            
            sysout("超過重試次數，移動到失敗狀態: " + failedPath);
            Files.move(httpPath, failedPath, StandardCopyOption.REPLACE_EXISTING);
            
            // 如果存在對應的配置檔案，也移動到失敗狀態
            Path configPath = Paths.get(baseName + CONFIG_SUFFIX);
            if (Files.exists(configPath)) {
                Path failedConfigPath = Paths.get(baseName + CONFIG_SUFFIX + FAILED_SUFFIX);
                sysout("移動配置檔案到失敗狀態: " + failedConfigPath);
                Files.move(configPath, failedConfigPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException moveError) {
            syserr("移動檔案到失敗狀態時發生錯誤: " + moveError.getMessage(), moveError);
        }
        
        retryCount.set(0);
        sysout("錯誤處理完成，重置重試計數器");
    }

    public int retainRecentLogs(int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("Days must be greater than 0");
        }

        LocalDateTime retentionDate = LocalDateTime.now().minusDays(days);
        int removedFiles = 0;

        try {
            Path logDir = Paths.get(LOG_DIR);
            if (!Files.exists(logDir)) {
                return 0;
            }

            try (Stream<Path> files = Files.list(logDir)) {
            	//第一層 filter：取得檔案以 LOG_SUFFIXES 列表中的任何一個後綴結尾
            	//第二層 filter：根據檔案日期進行篩選
            	List<Path> filesToDelete = files
                        .filter(path -> {
                            String fileName = path.getFileName().toString();
                            return LOG_SUFFIXES.stream().anyMatch(suffix -> fileName.endsWith(suffix));
                        })
                        .filter(path -> {
                            LocalDateTime fileDate = extractDateFromFileName(path.getFileName().toString());
                            return fileDate != null && fileDate.isBefore(retentionDate);
                        })
                        .collect(Collectors.toList());

                for (Path file : filesToDelete) {
                    try {
                        Files.delete(file);
                        removedFiles++;
                        sysoutRateLimited("已刪除過期(" + days + "天)檔案: " + file);
                    } catch (NoSuchFileException e) {
                    	systrace("找不到檔案, 可能被 clear 排程刪掉了, 所以不用處理它");
                    } catch (IOException e) {
                        syserr("刪除檔案時發生錯誤: " + file, e);
                    }
                }
            }
        } catch (NoSuchFileException e) {
        	systrace("找不到檔案, 可能被 clear 排程刪掉了, 所以不用處理它");
        } catch (IOException e) {
            throw new RuntimeException("清理日誌檔案失敗", e);
        }
        
        if (removedFiles > 0) {
        	TPILogger.tl.info("保留天數排程，清理完成，共刪除 " + removedFiles + " 個檔案");
        }
        return removedFiles;
    }

    public void shutdown() {
        sysout("開始關閉 ESLogBuffer...");
//        writeExecutor.shutdown();
        processExecutor.shutdown();
        cleanupExecutor.shutdown(); 
        try {
            if (!processExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                sysout("處理執行緒池未能在60秒內關閉，強制關閉");
                processExecutor.shutdownNow();
            }
            if (!cleanupExecutor.awaitTermination(60, TimeUnit.SECONDS)) { 
                sysout("清理執行緒池未能在60秒內關閉，強制關閉");
                cleanupExecutor.shutdownNow();
            }
            sysout("ESLogBuffer 已成功關閉");
        } catch (InterruptedException e) {
            syserr("關閉過程被中斷", e);
            processExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 提取日期時間戳的邏輯修改為更加穩健的方式，使用下劃線(_)作為分隔符
     * @param fileName
     * @return
     */
    private LocalDateTime extractDateFromFileName(String fileName) {
        try {
            // 找到第一個下劃線的位置
            int underscorePos = fileName.indexOf('_');
            
            // 如果找不到下劃線（舊格式），使用固定長度提取
            String dateStr;
            if (underscorePos == -1) {
                dateStr = fileName.substring(0, 17);
            } else {
                dateStr = fileName.substring(0, underscorePos);
            }
            
            // 解析日期
            return LocalDateTime.parse(
                dateStr,
                DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS_SSS)
            );
        } catch (Exception e) {
            syserr("解析檔案日期時發生錯誤: " + fileName, e);
            return null;
        }
    }
    
    public Float diskFree = 0.99f; //由 MonitorHostService.java 定期傳入, 0.99(empty) ~ 0.01(full)
    
    /**
     * 當磁盤空間不足時清理舊文件
     * @param freeThreshHold 磁盤可用空間閾值，低於此值觸發清理（例如：0.05表示5%）
     * @param deletePercent 要刪除的文件百分比（例如：30表示刪除30%最舊的文件）
     */
    private void cleanFilesOnLowDiskSpace(Float freeThreshHold, int deletePercent) {
    	
        if (! allowWriteElastic) {
            systrace("不允許寫入 Elastic，跳過本次處理");
            return;
        }
    	
    	// 檢查磁盤可用空間是否低於閾值（diskFree < freeThreshHold）, Yes才要執行
        if (diskFree >= freeThreshHold) {
            return; // 磁盤空間充足，不需要執行清理
        }
        
        sysout(String.format("磁盤空間不足 (可用率: %.2f%%, 閾值: %.2f%%)，開始清理舊文件...", 
            diskFree * 100, freeThreshHold * 100));
        
        try {
            Path logDir = Paths.get(LOG_DIR);
            if (!Files.exists(logDir)) {
                return;
            }
            
            // 獲取所有日誌文件並並使用已有的 extractDateFromFileName 方法從文件名中提取時間
            List<Path> allLogFiles;
            try (Stream<Path> files = Files.list(logDir)) {
                allLogFiles = files
                    .filter(path -> {
                        String fileName = path.getFileName().toString();
                        return LOG_SUFFIXES.stream().anyMatch(suffix -> fileName.endsWith(suffix));
                    })
                    .filter(path -> {
                        // 確保可以從文件名中提取有效日期
                        return extractDateFromFileName(path.getFileName().toString()) != null;
                    })
                    .sorted((path1, path2) -> {
                        // 按創建時間升序排序（從舊到新）
                        LocalDateTime date1 = extractDateFromFileName(path1.getFileName().toString());
                        LocalDateTime date2 = extractDateFromFileName(path2.getFileName().toString());
                        return date1.compareTo(date2);
                    })
                    .collect(Collectors.toList());
            }
            
            if (allLogFiles.isEmpty()) {
                sysout("沒有找到可以清理的日誌文件");
                return;
            }
            
            // 計算需要刪除的文件數量（總文件數 * 刪除百分比）
            int totalFiles = allLogFiles.size();
            int filesToDeleteCount = Math.max(1, (int)(totalFiles * deletePercent / 100.0));
            
            sysout(String.format("找到 %d 個日誌文件，將刪除最舊的 %d 個文件（%d%%）", 
                totalFiles, filesToDeleteCount, deletePercent));
            
            // 取最舊的一部分文件進行刪除
            List<Path> filesToDelete = allLogFiles.subList(0, Math.min(filesToDeleteCount, totalFiles));
            
            int successCount = 0;
            for (Path file : filesToDelete) {
                try {
                    Files.delete(file);
                    successCount++;
                    sysoutRateLimited("disk空間不足, 已刪除舊文件: " + file.getFileName());
                } catch (NoSuchFileException e) {
                	systrace("找不到檔案, 可能被 clear 排程刪掉了, 所以不用處理它");
                } catch (IOException e) {
                    syserr("刪除文件時發生錯誤: " + file, e);
                }
            }
            
            TPILogger.tl.info(String.format("disk空間不足 (可用率: %.2f%%, 閾值: %.2f%%)，現已清理完成，成功刪除 %d/%d 個文件", 
                    diskFree * 100, freeThreshHold * 100, successCount, filesToDelete.size()));      
            
        } catch (NoSuchFileException e) {
        	systrace("找不到檔案, 可能被 clear 排程刪掉了, 所以不用處理它");
        } catch (IOException e) {
            syserr("低磁盤空間清理過程中發生錯誤", e);
        } catch (Exception e) {
            syserr("低磁盤空間清理過程中發生未預期的錯誤", e);
        }
    }
}