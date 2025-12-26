package tpi.dgrv4.dpaa.es;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;

public class DiskSpaceMonitor {
	private static final Path LOG_DIR = Paths.get("apilogs");
	private static final long DEFAULT_MAX_DIR_SIZE_BYTES = 10_000_000_000L; // 10GB 默認值
	private static final int DEFAULT_MAX_FILES = 10000; // 默認最大文件數
	private static final long CHECK_INTERVAL_MS = 60000; // 每分鐘檢查一次

	private static final AtomicBoolean isTouchEsWriteThreshold = new AtomicBoolean(false);

	private long maxDirSizeBytes;
	private int maxFiles;

	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().name("DiskSpaceMonitor-Thread-").factory());
	private final AtomicBoolean isRunning = new AtomicBoolean(false);

	// Singleton 實例
	private static DiskSpaceMonitor instance;

	// 私有構造函數防止外部實例化，使用默認值
	private DiskSpaceMonitor() {
		this(DEFAULT_MAX_DIR_SIZE_BYTES, DEFAULT_MAX_FILES);
	}

	// 帶參數的私有構造函數
	private DiskSpaceMonitor(long maxDirSizeBytes, int maxFiles) {
		this.maxDirSizeBytes = maxDirSizeBytes;
		this.maxFiles = maxFiles;

//		scheduler = Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().name("DiskSpaceMonitor-Thread-").factory());

//				Executors.newScheduledThreadPool(1, runnable -> {
//		    Thread thread = new Thread(runnable, "DiskSpaceMonitor-Thread");
//		    thread.setDaemon(true);  // 可選，設置為守護線程
//		    return thread;
//		});
	}

	// 獲取單例實例的方法（使用默認值）
	public static synchronized DiskSpaceMonitor getInstance() {
		if (instance == null) {
			instance = new DiskSpaceMonitor();
		}
		return instance;
	}

	// 獲取或創建單例實例的方法（使用自定義值）
	public static synchronized DiskSpaceMonitor getInstance(long maxDirSizeBytes, int maxFiles) {
		if (instance == null) {
			instance = new DiskSpaceMonitor(maxDirSizeBytes, maxFiles);
		} else {
			// 如果實例已存在，更新配置
			instance.updateConfiguration(maxDirSizeBytes, maxFiles);
		}
		return instance;
	}

	// 更新配置方法
	public synchronized void updateConfiguration(long maxDirSizeBytes, int maxFiles) {
		this.maxDirSizeBytes = maxDirSizeBytes;
		this.maxFiles = maxFiles;
		TPILogger.tl.info("DiskSpaceMonitor configuration updated: maxSize=" + formatSize(maxDirSizeBytes)
				+ ", maxFiles=" + maxFiles);
	}

	// 啟動監控
	public synchronized void start() {
		if (isRunning.compareAndSet(false, true)) {
			// 確保日誌目錄存在
			try {
				if (!Files.exists(LOG_DIR)) {
					Files.createDirectories(LOG_DIR);
				}

				// 創建具有自定義線程名稱的排程器
//				scheduler = Executors.newScheduledThreadPool(1, runnable -> {
//				    Thread thread = new Thread(runnable, "DiskSpaceMonitor-Thread");
//				    thread.setDaemon(true);  // 可選，設置為守護線程
//				    return thread;
//				});

				// 排程執行檢查任務 (2分後才會啟動排程)
				scheduler.scheduleWithFixedDelay(this::checkDiskSpace, 120000, CHECK_INTERVAL_MS, TimeUnit.MILLISECONDS);
				TPILogger.tl.info("DiskSpaceMonitor started, monitoring: " + LOG_DIR.toAbsolutePath() + ", maxSize="
						+ formatSize(maxDirSizeBytes) + ", maxFiles=" + maxFiles);
				
			} catch (IOException e) {
				TPILogger.tl.error("Failed to create log directory: " + StackTraceUtil.logTpiShortStackTrace(e));
			}
		}
	}

	// 停止監控
	public synchronized void stop() {
		if (isRunning.get()) {
			scheduler.shutdown();
			TPILogger.tl.info("DiskSpaceMonitor stopped");
		}
	}
	
	public static long currentFileCount = -1L;
	public static long currentFilesSize = -1L;

	private void checkDiskSpace() {
		TPILogger.tl.info(StackTraceUtil.getStackTraceAsString());
		try {
			// 使用 Java NIO 計算文件數量
			try (Stream<Path> files = Files.walk(LOG_DIR)) {
				currentFileCount = files.filter(Files::isRegularFile).count();
			}

			// 使用跨平台方法計算目錄大小（替換原本使用系統命令的部分）
			currentFilesSize = calculateDirectorySize(LOG_DIR);

			boolean oldStatus = isTouchEsWriteThreshold.getAndSet(currentFilesSize >= maxDirSizeBytes || currentFileCount >= maxFiles);
			boolean newStatus = isTouchEsWriteThreshold.get();
			
			TPILogger.tl.info(String.format(
					"...oldPauseWriting=%b, if(FileSize=(%s > %s) || currentFilesCount=(%d > %d)) = pauseWriting=%b",
					oldStatus, formatSize(currentFilesSize), formatSize(maxDirSizeBytes),
					currentFileCount, maxFiles, newStatus));

			// 只有狀態變化時才通知
			if (oldStatus != newStatus) {
				TPILogger.tl.warn("API-Log to BulkFile Writing status changed to: " + (newStatus ? "paused" : "active") + " (Size: "
						+ currentFilesSize + "/" + formatSize(maxDirSizeBytes) + ", Files: " + currentFileCount + "/"
						+ maxFiles + ")");
			}
		} catch (NoSuchFileException | UncheckedIOException e) {
			TPILogger.tl.trace("找不到檔案, 可能被 clear 排程刪掉了, 所以不用處理它");
			checkDiskSpace();
		} catch (Exception e) {
			TPILogger.tl.error("Error checking disk space: " + StackTraceUtil.logTpiShortStackTrace(e));
		}
	}

	private static long calculateDirectorySize(Path path) throws IOException {
		try (Stream<Path> walk = Files.walk(path)) {
			return walk.filter(Files::isRegularFile).mapToLong(p -> {
				try {
					return Files.size(p);
				} catch (IOException e) {
					System.err.println("Could not get size for: " + p);
					return 0L;
				}
			}).sum();
		}
	}

	// 格式化文件大小顯示
	public static String formatSize(long size) {
		String[] units = { "B", "KB", "MB", "GB", "TB" };
		int unitIndex = 0;
		double sizeDbl = size;

		while (sizeDbl > 1024 && unitIndex < units.length - 1) {
			sizeDbl /= 1024;
			unitIndex++;
		}

		return String.format("%.2f %s", sizeDbl, units[unitIndex]);
	}



	// 使用 Path API 直接檢查狀態，適用於獨立的應用程序
	public static boolean checkStatusFromFile() {
		return isTouchEsWriteThreshold.get();
	}
}