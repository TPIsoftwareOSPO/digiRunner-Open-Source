package tpi.dgrv4.gateway.util;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.data.util.Pair;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.keeper.ITPILogger;

public enum CusGatewayLoginStateStore {

	INSTANCE;

	private final ConcurrentHashMap<String, Pair<Long, Map<String, String>>> store = new ConcurrentHashMap<>();

	private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(r -> {
		Thread thread = new Thread(r);
		thread.setName("CusGatewayLoginStateStoreThread");
		return thread;
	});

	private long expirationTime = 1;
	private TimeUnit timeUnit = TimeUnit.MINUTES;

	private ScheduledFuture<?> scheduledFuture;

	CusGatewayLoginStateStore() {
		// 固定每秒執行一次清理，與過期時間無關
		scheduledFuture = executorService.scheduleAtFixedRate(this::evictExpiredEntries, 1, 1, TimeUnit.SECONDS);
	}

	public String getState() {
		return UUID.randomUUID().toString();
	}

	public String putQueryParams(String state, Map<String, String> queryParams) {

		if (!StringUtils.hasText(state)) {
			state = getState();
		}

		long time = System.currentTimeMillis();
		store.put(state, Pair.of(time, queryParams));
		return state;
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

	public Map<String, String> getQueryParams(String state) {

		if (!StringUtils.hasText(state)) {
			return Collections.emptyMap();
		}

		Pair<Long, Map<String, String>> pair = store.remove(state);
		if (pair == null) {
			return Collections.emptyMap();
		}

		return pair.getSecond();
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

		ITPILogger.tl.debug("CusGatewayLoginStateStoreThread setExpiration: " + "old [" + oldExpirationTime + " "
				+ oldTimeUnit + "], " + "new [" + expirationTime + " " + timeUnit + "], " + "current store size: "
				+ store.size() + ", " + "ms value: " + TimeUnit.MILLISECONDS.convert(expirationTime, timeUnit) + "ms, "
				+ "execution time: " + new java.util.Date());
	}

	private void evictExpiredEntries() {
		long currentTime = System.currentTimeMillis();
		store.entrySet().removeIf(entry -> currentTime - entry.getValue().getFirst() > TimeUnit.MILLISECONDS
				.convert(expirationTime, timeUnit));
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
				Map<String, String> info = pair.getSecond();
				long storageTime = pair.getFirst();
				long ageMs = currentTime - storageTime;
				long expirationMs = TimeUnit.MILLISECONDS.convert(expirationTime, timeUnit);
				long remainingMs = expirationMs - ageMs;

				entryDetails.put("key", key);
				entryDetails.put("ageSeconds", ageMs / 1000.0);
				entryDetails.put("expiresInSeconds", remainingMs / 1000.0);

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
