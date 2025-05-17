package tpi.dgrv4.tcp.utils.communication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import tpi.dgrv4.tcp.utils.packets.UrlStatusPacket;

public class UrlStatusInfoMap {

	private ConcurrentHashMap<String, UrlStatusPacket> urlStatusInfos = new ConcurrentHashMap<>();
	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

	public UrlStatusInfoMap() {
		// 每秒執行一次清理工作
		scheduler.scheduleAtFixedRate(this::cleanExpiredStatus, 1, 1, TimeUnit.SECONDS);
	}

	public void putStatus(String key, UrlStatusPacket packet) {
		urlStatusInfos.put(key, packet);
	}

	private void cleanExpiredStatus() {
		long currentTime = System.currentTimeMillis();
		urlStatusInfos.entrySet().removeIf(entry -> (currentTime - entry.getValue().getUpdateTime()) > 5000); // 5秒
	}

	public Collection<UrlStatusPacket> getAllStatus() {
		return new ArrayList<>(urlStatusInfos.values());
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
