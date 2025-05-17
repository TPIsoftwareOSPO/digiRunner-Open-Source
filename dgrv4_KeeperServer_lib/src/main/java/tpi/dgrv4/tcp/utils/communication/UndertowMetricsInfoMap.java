package tpi.dgrv4.tcp.utils.communication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import tpi.dgrv4.tcp.utils.packets.UndertowMetricsPacket;

public class UndertowMetricsInfoMap {

	private ConcurrentHashMap<String, UndertowMetricsPacket> undertowMetricsInfos = new ConcurrentHashMap<>();
	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

	public UndertowMetricsInfoMap() {
		// 每秒執行一次清理工作
		scheduler.scheduleAtFixedRate(this::cleanExpiredMetrics, 1, 1, TimeUnit.SECONDS);
	}

	public void putMetric(String key, UndertowMetricsPacket packet) {
		undertowMetricsInfos.put(key, packet);
	}

	private void cleanExpiredMetrics() {
		long currentTime = System.currentTimeMillis();
		undertowMetricsInfos.entrySet().removeIf(entry -> (currentTime - entry.getValue().getUpdateTime()) > 5000); // 5秒
	}

	public Collection<UndertowMetricsPacket> getAllMetrics() {
		return new ArrayList<>(undertowMetricsInfos.values());
	}

	// 關閉 scheduler
	public void shutdown() {
		scheduler.shutdown();
		try {
			if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
				scheduler.shutdownNow();
			}
		} catch (InterruptedException e) {
			scheduler.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}
}
