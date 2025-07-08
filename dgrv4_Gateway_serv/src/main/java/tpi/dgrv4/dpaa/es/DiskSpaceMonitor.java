package tpi.dgrv4.dpaa.es;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;

public class DiskSpaceMonitor {
	private static final Path LOG_DIR = Paths.get("apilogs");
	private static final long DEFAULT_MAX_DIR_SIZE_BYTES = 10_000_000_000L; // 10GB 默認值
	private static final int DEFAULT_MAX_FILES = 10000; // 默認最大文件數
	private static final long CHECK_INTERVAL_MS = 60000; // 每分鐘檢查一次

	private long maxDirSizeBytes;
	private int maxFiles;
	private volatile boolean pauseWriting = false;
	private ScheduledExecutorService scheduler;
	private boolean isRunning = false;

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

		scheduler = Executors.newScheduledThreadPool(1, runnable -> {
		    Thread thread = new Thread(runnable, "DiskSpaceMonitor-Thread");
		    thread.setDaemon(true);  // 可選，設置為守護線程
		    return thread;
		});
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
		if (!isRunning) {
			// 確保日誌目錄存在
			try {
				if (!Files.exists(LOG_DIR)) {
					Files.createDirectories(LOG_DIR);
				}

				// 創建具有自定義線程名稱的排程器
				scheduler = Executors.newScheduledThreadPool(1, runnable -> {
				    Thread thread = new Thread(runnable, "DiskSpaceMonitor-Thread");
				    thread.setDaemon(true);  // 可選，設置為守護線程
				    return thread;
				});

				// 排程執行檢查任務 (2分後才會啟動排程)
				scheduler.scheduleWithFixedDelay(this::checkDiskSpace, 120000, CHECK_INTERVAL_MS, TimeUnit.MILLISECONDS);
				isRunning = true;
				TPILogger.tl.info("DiskSpaceMonitor started, monitoring: " + LOG_DIR.toAbsolutePath() + ", maxSize="
						+ formatSize(maxDirSizeBytes) + ", maxFiles=" + maxFiles);
				
			} catch (IOException e) {
				TPILogger.tl.error("Failed to create log directory: " + StackTraceUtil.logTpiShortStackTrace(e));
			}
		}
	}

	// 停止監控
	public synchronized void stop() {
		if (isRunning) {
			scheduler.shutdown();
			isRunning = false;
			TPILogger.tl.info("DiskSpaceMonitor stopped");
		}
	}

	private void checkDiskSpace() {
		TPILogger.tl.info(StackTraceUtil.getStackTraceAsString());
		try {
			// 使用 Java NIO 計算文件數量
			long fileCount;
			try (Stream<Path> files = Files.walk(LOG_DIR)) {
				fileCount = files.filter(Files::isRegularFile).count();
			}

			// 使用跨平台方法計算目錄大小（替換原本使用系統命令的部分）
			long currentSize = calculateDirectorySize(LOG_DIR);

			boolean oldStatus = pauseWriting;

			//  0.8 為狀態切換的緩衝區, 目錄大小在邊界值附近波動：10.01GB → 9.99GB → 10.02GB → 9.98GB
			// 若沒有緩衝區，狀態會頻繁切換：暫停 → 恢復 → 暫停 → 恢復
			// 「抖動」會造成系統不穩定且產生大量通知
			// 根據結果決定是否暫停寫入
			if (currentSize > maxDirSizeBytes || fileCount > maxFiles) {
				pauseWriting = true;
			} else if (currentSize < maxDirSizeBytes * 0.8 && fileCount < maxFiles * 0.8) {
				// 新增緩衝區，避免頻繁切換狀態
				pauseWriting = false;
			}
			
			TPILogger.tl.info(
				    String.format("...oldPauseWriting=%b, pauseWriting=%b, currentSize=%d, fileCount=%d", oldStatus,
				    pauseWriting, currentSize, fileCount));

			// 只有狀態變化時才通知
			if (oldStatus != pauseWriting) {
				notifyApplications();
				TPILogger.tl.warn("API-Log to BulkFile Writing status changed to: " + (pauseWriting ? "paused" : "active") + " (Size: "
						+ formatSize(currentSize) + "/" + formatSize(maxDirSizeBytes) + ", Files: " + fileCount + "/"
						+ maxFiles + ")");
			}
		} catch (NoSuchFileException e) {
			TPILogger.tl.trace("找不到檔案, 可能被 clear 排程刪掉了, 所以不用處理它");
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
	private String formatSize(long size) {
		String[] units = { "B", "KB", "MB", "GB", "TB" };
		int unitIndex = 0;
		double sizeDbl = size;

		while (sizeDbl > 1024 && unitIndex < units.length - 1) {
			sizeDbl /= 1024;
			unitIndex++;
		}

		return String.format("%.2f %s", sizeDbl, units[unitIndex]);
	}

	private void notifyApplications() {
		// 實現通知機制，將狀態文件寫入日誌目錄
		try {
			Path statusFile = LOG_DIR.resolve(".write_status");
			if (pauseWriting) {
			    Files.write(statusFile, "PAUSE".getBytes(StandardCharsets.UTF_8));
			} else {
			    Files.deleteIfExists(statusFile);
			}
		} catch (IOException e) {
			TPILogger.tl.error("Failed to update status file[.write_status]: " + StackTraceUtil.logTpiShortStackTrace(e));
		}
	}

	// 供應用程序查詢當前狀態的方法
	public boolean shouldPauseWriting() {
		return pauseWriting;
	}

	// 如果為 true，表示應該暫停寫入；如果為 false，表示可以正常寫入
	public static boolean isPauseRequired() {
		return getInstance().shouldPauseWriting();
	}

	// 使用 Path API 直接檢查狀態，適用於獨立的應用程序
	public static boolean checkStatusFromFile() {
		try {
			Path statusFile = LOG_DIR.resolve(".write_status");
			if (Files.exists(statusFile)) {
				String status = new String(Files.readAllBytes(statusFile), StandardCharsets.UTF_8).trim();
				return "PAUSE".equals(status); // 暫停寫入 log
			}
			return false; // 允許寫入
		} catch (NoSuchFileException e) {
			return false;
		} catch (Exception e) {
			TPILogger.tl.error("Error reading status file: " + StackTraceUtil.logTpiShortStackTrace(e));
			return false; // 發生錯誤時默認允許寫入
		}
	}
}