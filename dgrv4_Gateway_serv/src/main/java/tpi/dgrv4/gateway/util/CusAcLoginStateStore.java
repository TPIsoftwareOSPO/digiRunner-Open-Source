package tpi.dgrv4.gateway.util;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.data.util.Pair;

import tpi.dgrv4.common.keeper.ITPILogger;
import tpi.dgrv4.entity.entity.DgrAcIdpInfoCus;

public enum CusAcLoginStateStore {

	INSTANCE; // 單例實例

	// 存儲狀態和 DgrAcIdpInfoCus 對象的 ConcurrentHashMap
	private final ConcurrentHashMap<String, Pair<Long, DgrAcIdpInfoCus>> store = new ConcurrentHashMap<>();

	// 用於執行定期清理任務的執行器
	private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(r -> {
		Thread thread = new Thread(r);
		thread.setName("CusAcLoginStateStoreThread");
		return thread;
	});

	// 過期時間設置，默認為 1 分鐘
	private long expirationTime = 1;
	private TimeUnit timeUnit = TimeUnit.MINUTES;

	// 定期執行的任務
	private ScheduledFuture<?> scheduledFuture;

	// 構造函數：初始化定期清理任務
	CusAcLoginStateStore() {
		// 固定每秒執行一次清理，與過期時間無關
		scheduledFuture = executorService.scheduleAtFixedRate(this::evictExpiredEntries, 1, 1, TimeUnit.SECONDS);
	}

	/**
	 * 從配置字串設置過期時間
	 * 
	 * @param expirationConfig 格式為"數字+單位"，例如"5m"或"30s"，單位s為秒，m為分鐘，不區分大小寫
	 *                         秒的有效範圍為1-86400，分鐘的有效範圍為1-1440 如果配置無效，則使用預設值1分鐘
	 */
	public void setExpirationFromConfig(String expirationConfig) {
		// 預設值：1分鐘
		long newExpirationTime = 1;
		TimeUnit newTimeUnit = TimeUnit.MINUTES;

		// 如果配置為空，使用預設值
		if (expirationConfig == null || expirationConfig.trim().isEmpty()) {
			setExpiration(newExpirationTime, newTimeUnit);
			return;
		}

		// 使用正則表達式解析配置
		Pattern pattern = Pattern.compile("^(\\d+)([sSmM])$");
		Matcher matcher = pattern.matcher(expirationConfig.trim());

		if (matcher.matches()) {
			// 提取數字部分
			int value;
			try {
				value = Integer.parseInt(matcher.group(1));
			} catch (NumberFormatException e) {
				// 如果數字解析失敗，使用預設值
				setExpiration(newExpirationTime, newTimeUnit);
				return;
			}

			// 提取單位
			String unit = matcher.group(2).toLowerCase();

			if ("s".equals(unit)) {
				// 秒數限制: 1 ~ 86400
				value = Math.max(1, Math.min(value, 86400));
				newExpirationTime = value;
				newTimeUnit = TimeUnit.SECONDS;
			} else if ("m".equals(unit)) {
				// 分鐘限制: 1 ~ 1440
				value = Math.max(1, Math.min(value, 1440));
				newExpirationTime = value;
				newTimeUnit = TimeUnit.MINUTES;
			}
		}

		// 設置新的過期時間
		setExpiration(newExpirationTime, newTimeUnit);
	}

	// 生成唯一的狀態標識符
	public String getState() {
		return UUID.randomUUID().toString();
	}

	// 存儲 DgrAcIdpInfoCus 對象並返回對應的狀態標識符
	public String putDgrAcIdpInfoCus(DgrAcIdpInfoCus info) {

		if (info == null) {
			return "";
		}

		String state = getState();
		long time = System.currentTimeMillis();
		store.put(state, Pair.of(time, info));
		return state;
	}

	// 根據狀態標識符獲取並移除 DgrAcIdpInfoCus 對象
	public Optional<DgrAcIdpInfoCus> getDgrAcIdpInfoCus(String state) {

		if (state == null) {
			return Optional.empty();
		}

		Pair<Long, DgrAcIdpInfoCus> pair = store.remove(state);
		if (pair == null) {
			return Optional.empty();
		}

		return Optional.of(pair.getSecond());
	}

	// 設置新的過期時間和單位，並重新調度清理任務
	public void setExpiration(long expirationTime, TimeUnit timeUnit) {

		// 記錄舊值，用於日誌
		long oldExpirationTime = this.expirationTime;
		TimeUnit oldTimeUnit = this.timeUnit;

		this.expirationTime = expirationTime;
		this.timeUnit = timeUnit;

		if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
			scheduledFuture.cancel(true);
		}

		// 固定每秒執行一次清理，與過期時間無關
		scheduledFuture = executorService.scheduleAtFixedRate(this::evictExpiredEntries, 1, 1, TimeUnit.SECONDS);

		ITPILogger.tl.debug("CusAcLoginStateStore setExpiration: " + "old [" + oldExpirationTime + " " + oldTimeUnit
				+ "], " + "new [" + expirationTime + " " + timeUnit + "], " + "current store size: " + store.size()
				+ ", " + "ms value: " + TimeUnit.MILLISECONDS.convert(expirationTime, timeUnit) + "ms, "
				+ "execution time: " + new java.util.Date());
	}

	// 清理過期條目的方法
	private void evictExpiredEntries() {
		long currentTime = System.currentTimeMillis();
		long expirationMs = TimeUnit.MILLISECONDS.convert(expirationTime, timeUnit);
		store.entrySet().removeIf(entry -> currentTime - entry.getValue().getFirst() > expirationMs);
	}

	/**
	 * Returns the detailed status as a Map that can be easily converted to JSON.
	 * This method is the recommended way to get JSON-formatted status information.
	 * The returned Map can be converted to JSON using libraries like Jackson or
	 * Gson.
	 * 
	 * Example with Jackson:
	 * 
	 * <pre>
	 * ObjectMapper mapper = new ObjectMapper();
	 * String json = mapper.writeValueAsString(store.getDetailedStatusAsMap(true));
	 * </pre>
	 * 
	 * @param includeEntryDetails If true, includes details of each entry in the
	 *                            store
	 * @return A Map representing the detailed status of the store
	 */
	public java.util.Map<String, Object> getDetailedStatusAsMap(boolean includeEntryDetails) {
		java.util.Map<String, Object> statusMap = new java.util.LinkedHashMap<>();

		// Basic information
		java.util.Map<String, Object> basicInfo = new java.util.LinkedHashMap<>();
		basicInfo.put("reportTime", System.currentTimeMillis());
		basicInfo.put("storeSize", store.size());
		basicInfo.put("expirationSettings", expirationTime + " " + timeUnit);
		basicInfo.put("expirationMilliseconds", TimeUnit.MILLISECONDS.convert(expirationTime, timeUnit));
		basicInfo.put("cleanupInterval", "1 SECONDS");
		basicInfo.put("schedulerStatus",
				scheduledFuture != null && !scheduledFuture.isCancelled() ? "Active" : "Inactive");
		statusMap.put("basicInfo", basicInfo);

		// Store entries
		java.util.Map<String, Object> entriesInfo = new java.util.LinkedHashMap<>();
		entriesInfo.put("count", store.size());

		if (includeEntryDetails && !store.isEmpty()) {
			long currentTime = System.currentTimeMillis();
			java.util.List<java.util.Map<String, Object>> entries = new java.util.ArrayList<>();

			store.forEach((key, pair) -> {
				java.util.Map<String, Object> entryDetails = new java.util.LinkedHashMap<>();
				DgrAcIdpInfoCus info = pair.getSecond();
				long storageTime = pair.getFirst();
				long ageMs = currentTime - storageTime;
				long expirationMs = TimeUnit.MILLISECONDS.convert(expirationTime, timeUnit);
				long remainingMs = expirationMs - ageMs;

				entryDetails.put("key", key);
				entryDetails.put("ageSeconds", ageMs / 1000.0);
				entryDetails.put("expiresInSeconds", remainingMs / 1000.0);
				entryDetails.put("infoType", info != null ? info.getClass().getSimpleName() : "null");

				if (info != null) {
					entryDetails.put("creationTime", storageTime);
				}

				entries.add(entryDetails);
			});

			entriesInfo.put("entries", entries);
		} else {
			entriesInfo.put("entries", new java.util.ArrayList<>());
		}

		statusMap.put("storeEntries", entriesInfo);

		// Service status
		java.util.Map<String, Object> serviceStatus = new java.util.LinkedHashMap<>();
		serviceStatus.put("executorServiceShutdown", executorService.isShutdown());
		serviceStatus.put("executorServiceTerminated", executorService.isTerminated());

		// 新增剩餘清空時間資訊
		if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
			long remainingTimeMs = scheduledFuture.getDelay(TimeUnit.MILLISECONDS);
			serviceStatus.put("nextCleanupRemainingMs", remainingTimeMs);
			serviceStatus.put("nextCleanupRemainingSeconds", remainingTimeMs / 1000.0);
			serviceStatus.put("nextCleanupEstimatedTime", System.currentTimeMillis() + remainingTimeMs);
		} else {
			serviceStatus.put("nextCleanupRemainingMs", -1);
			serviceStatus.put("nextCleanupRemainingSeconds", -1);
			serviceStatus.put("nextCleanupEstimatedTime", "Scheduler not active");
		}

		statusMap.put("serviceStatus", serviceStatus);

		return statusMap;
	}
}
