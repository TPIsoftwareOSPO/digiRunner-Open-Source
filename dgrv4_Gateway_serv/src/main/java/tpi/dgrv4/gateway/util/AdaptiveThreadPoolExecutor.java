package tpi.dgrv4.gateway.util;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;

public class AdaptiveThreadPoolExecutor extends ThreadPoolExecutor {

	// 核心執行緒數：CPU 核心數 + 1
	private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors() + 1;
	// 最大執行緒數：核心執行緒數的 2 倍
	private static final int MAXIMUM_POOL_SIZE = CORE_POOL_SIZE * 2;
	// 空閒執行緒的存活時間：60 秒
	private static final int KEEP_ALIVE_TIME = 60;
	private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
	// 任務隊列容量：1000
	private static final int QUEUE_CAPACITY = 1000;

	// 使用 LongAdder 來計算總執行時間和總等待時間
	private final LongAdder totalExecutionTime = new LongAdder();
	private final LongAdder totalWaitingTime = new LongAdder();
	// 任務計數器
	private final AtomicLong taskCount = new AtomicLong(0);
	// 使用 ThreadLocal 存儲每個執行緒的任務提交時間
	private static final ThreadLocal<Long> taskSubmitTime = new ThreadLocal<>();
	// 定時調度器
	private final ScheduledExecutorService scheduler;
	// 執行緒池大小的 AtomicInteger
	private final AtomicInteger poolSize = new AtomicInteger(CORE_POOL_SIZE);

	// 滑動窗口大小
	private final int WINDOW_SIZE = 100;
	// 執行時間的滑動窗口
	private final long[] executionTimes = new long[WINDOW_SIZE];
	// 等待時間的滑動窗口
	private final long[] waitingTimes = new long[WINDOW_SIZE];
	// 滑動窗口的當前索引
	private int windowIndex = 0;

	// 初始調整延遲時間（毫秒）
	private final long INITIAL_ADJUSTMENT_DELAY = 1000;
	// 最大調整延遲時間（毫秒）
	private final long MAX_ADJUSTMENT_DELAY = 60000;
	// 當前調整延遲時間
	private long currentAdjustmentDelay = INITIAL_ADJUSTMENT_DELAY;

	public AdaptiveThreadPoolExecutor() {
		super(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT,
				new LinkedBlockingQueue<>(QUEUE_CAPACITY), new ThreadPoolExecutor.CallerRunsPolicy());

		TPILogger.tl.debug("AdaptiveThreadPoolExecutor initialized with core pool size: " + CORE_POOL_SIZE
				+ " and scheduled adjustment task with initial delay: " + currentAdjustmentDelay + "ms");

		// 設置定時任務，根據當前調整延遲時間動態調整執行緒池大小
		scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
		    Thread thread = new Thread(r);
		    thread.setName("Adaptive-util-Thread");
		    return thread;
		});
		scheduler.schedule(this::adjustThreadPoolSize, currentAdjustmentDelay, TimeUnit.MILLISECONDS);
	}

	@Override
	public void execute(Runnable command) {

		TimestampedTask timestampedTask = new TimestampedTask(command);
		TPILogger.tl.debug("Task submitted at: " + timestampedTask.getSubmitTime() + ". Starting execution.");

		super.execute(() -> {
			long startTime = System.nanoTime();
			try {
				timestampedTask.run();
			} finally {
				long endTime = System.nanoTime();
				long waitingTime = startTime - timestampedTask.getSubmitTime();
				long executionTime = endTime - startTime;

				totalExecutionTime.add(executionTime);
				totalWaitingTime.add(waitingTime);
				taskCount.incrementAndGet();

				executionTimes[windowIndex] = executionTime;
				waitingTimes[windowIndex] = waitingTime;
				windowIndex = (windowIndex + 1) % WINDOW_SIZE;
			}
		});
	}

	private void adjustThreadPoolSize() {
		TPILogger.tl.trace("Adjusting thread pool size. Current pool size: " + getPoolSize() + ", Queue size: "
				+ getQueue().size());

		try {
			if (taskCount.get() == 0) {
				return;
			}

			double avgExecutionTime = calculateAverage(executionTimes);
			double avgWaitingTime = calculateAverage(waitingTimes);
			int queueSize = this.getQueue().size();
			double utilization = calculateUtilization();
			double cpuUsage = getAdjustedCpuUsage();
			double memoryUsage = getJvmMemoryUsage();

			adjustPoolSize(avgExecutionTime, avgWaitingTime, queueSize, utilization, cpuUsage, memoryUsage);
			adjustDelayBasedOnSystemLoad(utilization, cpuUsage, memoryUsage);

			scheduleNextAdjustment();
		} catch (Exception e) {
			TPILogger.tl.error("Error adjusting thread pool size: " + StackTraceUtil.logStackTrace(e));
		}
	}

	private double calculateAverage(long[] times) {
		return Arrays.stream(times).average().orElse(0.0);
	}

	private double calculateUtilization() {
		return (double) this.getActiveCount() / this.getPoolSize();
	}

	private double getAdjustedCpuUsage() {
		double cpuUsage = getSystemCpuUsage();
		return cpuUsage < 0 ? Double.MAX_VALUE : cpuUsage;
	}

	private void adjustPoolSize(double avgExecutionTime, double avgWaitingTime, int queueSize, double utilization,
			double cpuUsage, double memoryUsage) {
		if (shouldIncreasePoolSize(avgExecutionTime, avgWaitingTime, queueSize, utilization, cpuUsage, memoryUsage)) {
			increasePoolSize();
		} else if (shouldDecreasePoolSize(avgExecutionTime, avgWaitingTime, queueSize, utilization, cpuUsage,
				memoryUsage)) {
			decreasePoolSize();
		}
	}

	private boolean shouldIncreasePoolSize(double avgExecutionTime, double avgWaitingTime, int queueSize,
			double utilization, double cpuUsage, double memoryUsage) {
		return (avgExecutionTime > 100_000_000 || avgWaitingTime > 1_000_000_000 || queueSize > 500)
				&& utilization > 0.8 && cpuUsage < 0.9 && memoryUsage < 0.8;
	}

	private boolean shouldDecreasePoolSize(double avgExecutionTime, double avgWaitingTime, int queueSize,
			double utilization, double cpuUsage, double memoryUsage) {
		return avgExecutionTime < 50_000_000 && avgWaitingTime < 500_000_000 && queueSize < 100 && utilization < 0.2
				&& cpuUsage < 0.6 && memoryUsage < 0.6;
	}

	private void increasePoolSize() {
		int newPoolSize = Math.min(poolSize.get() + 1, MAXIMUM_POOL_SIZE);
		if (poolSize.compareAndSet(poolSize.get(), newPoolSize)) {
			this.setMaximumPoolSize(newPoolSize);
		}
	}

	private void decreasePoolSize() {
		int newPoolSize = Math.max(poolSize.get() - 1, CORE_POOL_SIZE);
		if (poolSize.compareAndSet(poolSize.get(), newPoolSize)) {
			this.setMaximumPoolSize(newPoolSize);
		}
	}

	private void adjustDelayBasedOnSystemLoad(double utilization, double cpuUsage, double memoryUsage) {
		if (utilization > 0.8 || cpuUsage > 0.8 || memoryUsage > 0.8) {
			currentAdjustmentDelay = Math.max(currentAdjustmentDelay / 2, INITIAL_ADJUSTMENT_DELAY);
		} else if (utilization < 0.2 && cpuUsage < 0.2 && memoryUsage < 0.2) {
			currentAdjustmentDelay = Math.min(currentAdjustmentDelay * 2, MAX_ADJUSTMENT_DELAY);
		}
	}

	private void scheduleNextAdjustment() {
		if (!isShutdown()) {
			scheduler.schedule(this::adjustThreadPoolSize, currentAdjustmentDelay, TimeUnit.MILLISECONDS);
		}
	}

	@Override
	public void shutdown() {
		TPILogger.tl.info("Shutting down AdaptiveThreadPoolExecutor.");
		// 停止接受新任務
		super.shutdown();
		try {
			// 等待所有任務完成，最長等待 1 小時
			if (!super.awaitTermination(1, TimeUnit.HOURS)) {
				// 如果超時，強制關閉執行緒池
				super.shutdownNow();
			}
		} catch (InterruptedException e) {
			TPILogger.tl.error("AdaptiveThreadPoolExecutor shutdown interrupted: " + StackTraceUtil.logStackTrace(e));
			// 如果等待過程中被中斷，強制關閉執行緒池
			super.shutdownNow();
			// 保留中斷狀態
			Thread.currentThread().interrupt();
		} finally {
			// 清理 ThreadLocal
			taskSubmitTime.remove();
			// 關閉定時調度器
			scheduler.shutdown();
		}
	}

	// 獲取系統 CPU 使用率
	private double getSystemCpuUsage() {
		OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
		double systemLoadAverage = osBean.getSystemLoadAverage();
		int availableProcessors = osBean.getAvailableProcessors();

		// 如果 getSystemLoadAverage 方法不支持，則返回 -1
		if (systemLoadAverage < 0) {
			return -1;
		}

		return systemLoadAverage / availableProcessors;
	}

	// 獲取 JVM 記憶體使用率
	private double getJvmMemoryUsage() {
		Runtime runtime = Runtime.getRuntime();
		long usedMemory = runtime.totalMemory() - runtime.freeMemory();
		long maxMemory = runtime.maxMemory();
		return (double) usedMemory / maxMemory;
	}
}

class TimestampedTask implements Runnable {
	private final Runnable task;
	private final long submitTime;

	public TimestampedTask(Runnable task) {
		this.task = task;
		this.submitTime = System.nanoTime();
	}

	public long getSubmitTime() {
		return submitTime;
	}

	@Override
	public void run() {
		task.run();
	}
}
