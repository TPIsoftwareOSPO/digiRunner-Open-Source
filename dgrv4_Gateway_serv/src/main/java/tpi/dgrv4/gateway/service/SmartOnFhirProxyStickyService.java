package tpi.dgrv4.gateway.service;

import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import lombok.Builder;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.constant.FhirInteraction;
import tpi.dgrv4.entity.entity.DgrSmartOnFhirProxySticky;
import tpi.dgrv4.entity.repository.DgrSmartOnFhirProxyStickyDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

/**
 * Smart on FHIR Proxy Sticky Session 服務
 * 負責處理 FHIR 資源與後端伺服器的固定對應關係
 *
 * 快取策略：
 * - 使用 hashcode 作為 key
 * - TTL: 1 小時（expireAfterAccess）
 * - 每次查詢時自動刷新 TTL
 * - 1 小時內未查詢則自動移除
 */
@Service
public class SmartOnFhirProxyStickyService {

	/**
	 * 可追蹤的 FHIR Interaction 類型
	 * 這些 interaction 會建立資源與後端的 sticky 對應關係
	 */
	private static final List<FhirInteraction> TRACKABLE_INTERACTIONS = Arrays.asList(
		FhirInteraction.READ,
		FhirInteraction.VREAD,
		FhirInteraction.UPDATE,
		FhirInteraction.PATCH,
		FhirInteraction.DELETE,
		FhirInteraction.CREATE,
		FhirInteraction.SEARCH_COMPARTMENT,
		FhirInteraction.HISTORY_INSTANCE
	);

	/**
	 * Sticky Session 快取
	 * Key: hashcode (String)
	 * Value: DgrSmartOnFhirProxySticky
	 *
	 * 設定：
	 * - expireAfterAccess: 1 小時，每次 get 都會刷新
	 * - maximumSize: 10000，避免記憶體無限增長
	 */
	private final Cache<String, DgrSmartOnFhirProxySticky> stickyCache;

	private DgrSmartOnFhirProxyStickyDao dgrSmartOnFhirProxyStickyDao;

	@Autowired
	public SmartOnFhirProxyStickyService(DgrSmartOnFhirProxyStickyDao dgrSmartOnFhirProxyStickyDao) {
		this.dgrSmartOnFhirProxyStickyDao = dgrSmartOnFhirProxyStickyDao;

		// 初始化快取
		this.stickyCache = Caffeine.newBuilder()
			.expireAfterAccess(Duration.ofHours(1))  // 1 小時未存取則過期
			.maximumSize(10000)  // 最多存 10000 筆
			.recordStats()  // 記錄統計資訊（可選，用於監控）
			.build();

		TPILogger.tl.debug("SmartOnFhirProxyStickyService cache initialized: " +
			"expireAfterAccess=1h, maximumSize=10000");
	}

	/**
	 * 解析 FHIR URL 並查詢 Sticky 紀錄
	 *
	 * @param sofProxyId Proxy ID
	 * @param resourceURL Resource URL 路徑（可能沒有前導 "/"）
	 * @param method HTTP Method
	 * @return StickyLookupResult 包含解析結果和查詢到的紀錄
	 */
	public StickyLookupResult lookupStickyRecord(Long sofProxyId, String resourceURL, String method) {
		// 標準化 resourceURL：確保有前導 "/"
		String normalizedUrl = normalizeResourceUrl(resourceURL);

		// 解析 FHIR URL
		FhirInteraction.IdentifyResult fhirResult = FhirInteraction.identifyWithParams(normalizedUrl, method);

		if (!fhirResult.isIdentified()) {
			return StickyLookupResult.builder()
				.fhirResult(fhirResult)
				.isTrackable(false)
				.stickyRecord(Optional.empty())
				.hashcode(null)
				.build();
		}

		FhirInteraction interaction = fhirResult.getInteraction();

		// 檢查是否為可追蹤的 Interaction
		boolean isTrackable = TRACKABLE_INTERACTIONS.contains(interaction);

		if (!isTrackable) {
			return StickyLookupResult.builder()
				.fhirResult(fhirResult)
				.isTrackable(false)
				.stickyRecord(Optional.empty())
				.hashcode(null)
				.build();
		}

		// 計算 hashcode（Resource Type 級別路由）
		String hashcode;
		if (interaction == FhirInteraction.SEARCH_COMPARTMENT) {
			// SEARCH_COMPARTMENT: 使用目標資源類型（如 /Patient/123/Observation 中的 Observation）
			// 若無目標類型，則使用 compartment 類型（如 Patient）
			String targetType = fhirResult.getType();
			String compartment = fhirResult.getCompartment();
			String routeType = (targetType != null && !targetType.isEmpty()) ? targetType : compartment;
			hashcode = calculateHashcode(sofProxyId, routeType);
		} else {
			String type = fhirResult.getType();
			hashcode = calculateHashcode(sofProxyId, type);
		}

		// 查詢 Sticky 紀錄：先查快取，再查資料庫
		Optional<DgrSmartOnFhirProxySticky> stickyRecord = Optional.empty();

		// 1. 先查快取
		DgrSmartOnFhirProxySticky cachedRecord = stickyCache.getIfPresent(hashcode);
		if (cachedRecord != null) {
			stickyRecord = Optional.of(cachedRecord);
			TPILogger.tl.debug("Sticky session found in cache: hashcode=" + hashcode +
				", diversionId=" + cachedRecord.getSofProxyDiversionId());
		} else {
			// 2. 快取未命中，查詢資料庫
			stickyRecord = dgrSmartOnFhirProxyStickyDao
				.findBySofProxyIdAndSofProxyStickyHashcode(sofProxyId, hashcode);

			if (stickyRecord.isPresent()) {
				// 3. 查到後放入快取
				stickyCache.put(hashcode, stickyRecord.get());
				TPILogger.tl.debug("Sticky session found in database and cached: hashcode=" + hashcode +
					", diversionId=" + stickyRecord.get().getSofProxyDiversionId());
			}
		}

		return StickyLookupResult.builder()
			.fhirResult(fhirResult)
			.isTrackable(true)
			.stickyRecord(stickyRecord)
			.hashcode(hashcode)
			.build();
	}

	/**
	 * 儲存 Sticky 紀錄
	 * 根據不同的 FHIR Interaction 類型，儲存對應的資源類型與後端關係
	 *
	 * @param context 儲存上下文，包含所有必要資訊
	 * @deprecated 建議使用 {@link #saveTypeBinding(Long, String, Long)} 直接儲存 Resource Type 綁定
	 */
	@Deprecated
	public void saveStickyRecord(StickyRecordContext context) {
		FhirInteraction interaction = context.lookupResult().fhirResult().getInteraction();

		// CREATE: 從 Location header 解析新資源資訊
		if (interaction == FhirInteraction.CREATE && context.statusCode() == 201) {
			Optional<ResourceInfo> createdResource = parseCreatedResourceLocation(context.respObj());
			if (createdResource.isPresent()) {
				String createdType = createdResource.get().type();
				String createdHashcode = calculateHashcode(context.sofProxyId(), createdType);

				doSaveStickyRecord(
					context.sofProxyId(),
					context.diversionId(),
					interaction,
					createdType,
					null,  // Resource Type 路由模式不儲存 resourceId
					context.method(),
					context.resourceURL(),
					createdHashcode
				);
			}
		}
		// READ, VREAD, UPDATE, PATCH, DELETE, HISTORY_INSTANCE
		else if (Arrays.asList(
			FhirInteraction.READ,
			FhirInteraction.VREAD,
			FhirInteraction.UPDATE,
			FhirInteraction.PATCH,
			FhirInteraction.DELETE,
			FhirInteraction.HISTORY_INSTANCE
		).contains(interaction)) {
			doSaveStickyRecord(
				context.sofProxyId(),
				context.diversionId(),
				interaction,
				context.lookupResult().fhirResult().getType(),
				null,  // Resource Type 路由模式不儲存 resourceId
				context.method(),
				context.resourceURL(),
				context.lookupResult().hashcode()
			);
		}
		// SEARCH_COMPARTMENT
		else if (interaction == FhirInteraction.SEARCH_COMPARTMENT) {
			Map<String, String> params = context.lookupResult().fhirResult().getParams();
			String targetType = params.get("type");
			String compartment = params.get("compartment");
			String routeType = (targetType != null && !targetType.isEmpty()) ? targetType : compartment;

			doSaveStickyRecord(
				context.sofProxyId(),
				context.diversionId(),
				interaction,
				routeType,
				null,  // Resource Type 路由模式不儲存 resourceId
				context.method(),
				context.resourceURL(),
				context.lookupResult().hashcode()
			);
		}
	}

	/**
	 * 儲存 Resource Type 與 Diversion 的綁定關係
	 * 這是 Resource Type 路由模式的主要儲存方法
	 *
	 * @param sofProxyId Proxy ID
	 * @param resourceType Resource Type (例如: "Patient")
	 * @param diversionId 選定的 Diversion ID
	 */
	public void saveTypeBinding(Long sofProxyId, String resourceType, Long diversionId) {
		String hashcode = calculateHashcode(sofProxyId, resourceType);

		// 檢查是否已存在（避免重複儲存）
		if (stickyCache.getIfPresent(hashcode) != null) {
			TPILogger.tl.debug("Type binding already exists in cache: type=" + resourceType);
			return;
		}

		Optional<DgrSmartOnFhirProxySticky> existing = dgrSmartOnFhirProxyStickyDao
			.findBySofProxyIdAndSofProxyStickyHashcode(sofProxyId, hashcode);
		if (existing.isPresent()) {
			stickyCache.put(hashcode, existing.get());
			TPILogger.tl.debug("Type binding already exists in database: type=" + resourceType);
			return;
		}

		doSaveStickyRecord(
			sofProxyId,
			diversionId,
			null,  // interaction 不重要
			resourceType,
			null,  // Resource Type 路由模式不儲存 resourceId
			null,  // method 不重要
			null,  // resourceURL 不重要
			hashcode
		);
	}

	/**
	 * 根據 Resource Type 查詢 Sticky 紀錄
	 * 這是 Resource Type 路由模式的主要查詢方法
	 *
	 * @param sofProxyId Proxy ID
	 * @param resourceType Resource Type (例如: "Patient")
	 * @return 查詢結果
	 */
	public TypeLookupResult lookupByType(Long sofProxyId, String resourceType) {
		String hashcode = calculateHashcode(sofProxyId, resourceType);

		// 1. 先查快取
		DgrSmartOnFhirProxySticky cachedRecord = stickyCache.getIfPresent(hashcode);
		if (cachedRecord != null) {
			TPILogger.tl.debug("Type binding found in cache: type=" + resourceType +
				", diversionId=" + cachedRecord.getSofProxyDiversionId());
			return new TypeLookupResult(true, Optional.of(cachedRecord.getSofProxyDiversionId()), hashcode);
		}

		// 2. 查詢資料庫
		Optional<DgrSmartOnFhirProxySticky> dbRecord = dgrSmartOnFhirProxyStickyDao
			.findBySofProxyIdAndSofProxyStickyHashcode(sofProxyId, hashcode);

		if (dbRecord.isPresent()) {
			stickyCache.put(hashcode, dbRecord.get());
			TPILogger.tl.debug("Type binding found in database and cached: type=" + resourceType +
				", diversionId=" + dbRecord.get().getSofProxyDiversionId());
			return new TypeLookupResult(true, Optional.of(dbRecord.get().getSofProxyDiversionId()), hashcode);
		}

		return new TypeLookupResult(false, Optional.empty(), hashcode);
	}

	/**
	 * Resource Type 查詢結果
	 */
	public record TypeLookupResult(
		boolean found,
		Optional<Long> diversionId,
		String hashcode
	) {}

	/**
	 * 統計各 Diversion 已綁定的 Resource Type 數量
	 *
	 * @param sofProxyId Proxy ID
	 * @return Map of diversionId -> count
	 */
	public Map<Long, Long> countBindingsByDiversion(Long sofProxyId) {
		List<Object[]> results = dgrSmartOnFhirProxyStickyDao.countByDiversion(sofProxyId);
		Map<Long, Long> counts = new HashMap<>();
		for (Object[] row : results) {
			Long diversionId = (Long) row[0];
			Long count = (Long) row[1];
			counts.put(diversionId, count);
		}
		return counts;
	}

	/**
	 * 智能分配：選擇後端（用於 POST 新增資料）
	 * 優先分配到「低於目標比例」的 Backend，達標後用概率隨機
	 *
	 * @param sofProxyId Proxy ID
	 * @param resourceType Resource Type
	 * @param diversions 可用的 Diversion 列表（包含 diversionId, probability）
	 * @return 選定的 Diversion ID
	 */
	public Long selectDiversionByBalance(Long sofProxyId, String resourceType,
			List<DiversionInfo> diversions) {

		if (diversions == null || diversions.isEmpty()) {
			throw new IllegalArgumentException("No diversions available");
		}

		// 1. 統計各 Diversion 已綁定的 Type 數量
		Map<Long, Long> bindingCounts = countBindingsByDiversion(sofProxyId);
		long totalBindings = bindingCounts.values().stream().mapToLong(Long::longValue).sum();

		// 2. 計算各 Diversion 的目標比例與差距
		int totalProbability = diversions.stream().mapToInt(DiversionInfo::probability).sum();

		DiversionInfo selected = null;
		double maxDeficit = Double.NEGATIVE_INFINITY;

		for (DiversionInfo d : diversions) {
			long currentCount = bindingCounts.getOrDefault(d.diversionId(), 0L);
			double currentRatio = totalBindings > 0 ? (double) currentCount / totalBindings : 0;
			double targetRatio = (double) d.probability() / totalProbability;
			double deficit = targetRatio - currentRatio;

			TPILogger.tl.debug("Diversion balance check: id=" + d.diversionId() +
				", current=" + currentCount + "/" + totalBindings +
				", target=" + d.probability() + "/" + totalProbability +
				", deficit=" + deficit);

			if (deficit > maxDeficit) {
				maxDeficit = deficit;
				selected = d;
			}
		}

		// 3. 都達標，用概率隨機
		if (selected == null || maxDeficit <= 0) {
			selected = selectByProbability(diversions);
			TPILogger.tl.debug("Using probability selection: diversionId=" + selected.diversionId());
		} else {
			TPILogger.tl.debug("Using balance selection: diversionId=" + selected.diversionId() +
				", deficit=" + maxDeficit);
		}

		return selected.diversionId();
	}

	/**
	 * 根據概率權重隨機選擇 Diversion
	 */
	private DiversionInfo selectByProbability(List<DiversionInfo> diversions) {
		int totalProbability = diversions.stream().mapToInt(DiversionInfo::probability).sum();
		int random = (int) (Math.random() * totalProbability);

		int cumulative = 0;
		for (DiversionInfo d : diversions) {
			cumulative += d.probability();
			if (random < cumulative) {
				return d;
			}
		}
		return diversions.get(diversions.size() - 1);
	}

	/**
	 * Diversion 資訊（用於智能分配）
	 */
	public record DiversionInfo(
		Long diversionId,
		int probability
	) {}

	/**
	 * 清除指定 Diversion 的所有快取和紀錄
	 * 用於刪除 Backend 時清理
	 *
	 * @param diversionId Diversion ID
	 */
	@Transactional
	public void cleanupDiversion(Long diversionId) {
		// 1. 查詢所有相關紀錄
		List<DgrSmartOnFhirProxySticky> records = dgrSmartOnFhirProxyStickyDao
			.findBySofProxyDiversionId(diversionId);

		// 2. 清除快取
		for (DgrSmartOnFhirProxySticky record : records) {
			stickyCache.invalidate(record.getSofProxyStickyHashcode());
		}

		// 3. 刪除資料庫紀錄
		dgrSmartOnFhirProxyStickyDao.deleteByDiversionId(diversionId);

		TPILogger.tl.info("Cleaned up " + records.size() + " sticky records for diversionId=" + diversionId);
	}

	/**
	 * 標準化 Resource URL
	 * 確保 URL 以 "/" 開頭，以便 FhirInteraction.identifyWithParams() 正確解析
	 *
	 * @param resourceURL 原始的 resource URL（可能沒有前導 "/"）
	 * @return 標準化後的 URL（保證有前導 "/"）
	 *
	 * 範例：
	 * - "Patient/123" -> "/Patient/123"
	 * - "/Patient/123" -> "/Patient/123"（已有前導 "/" 則不變）
	 * - "" -> "/"
	 * - null -> "/"
	 */
	private String normalizeResourceUrl(String resourceURL) {
		if (resourceURL == null || resourceURL.isEmpty()) {
			return "/";
		}
		return resourceURL.startsWith("/") ? resourceURL : "/" + resourceURL;
	}

	/**
	 * 計算 Sticky Session 的 hashcode（公開靜態版本）
	 * 供 CRUD Service 等外部服務使用
	 *
	 * @param sofProxyId Proxy ID
	 * @param type Resource Type (例如: "Patient")
	 * @return SHA256 hashcode
	 */
	public static String calculateStickyHashcode(Long sofProxyId, String type) {
		String key = (sofProxyId != null ? sofProxyId.toString() : "") +
			(type != null ? type : "");
		return DigestUtils.sha256Hex(key);
	}

	/**
	 * 計算 Sticky Session 的 hashcode
	 * 用於識別資源類型的唯一性，作為資料庫查詢的 key
	 *
	 * Resource Type 路由模式：同一資源類型的所有請求會路由到同一後端
	 *
	 * @param sofProxyId Proxy ID
	 * @param type Resource Type (例如: "Patient")
	 * @return SHA256 hashcode
	 */
	private String calculateHashcode(Long sofProxyId, String type) {
		return calculateStickyHashcode(sofProxyId, type);
	}

	/**
	 * 實際儲存 Sticky 紀錄到資料庫並更新快取
	 */
	private void doSaveStickyRecord(
		Long sofProxyId,
		Long diversionId,
		FhirInteraction interaction,
		String type,
		String id,
		String method,
		String resourceURL,
		String hashcode) {

		try {
			DgrSmartOnFhirProxySticky sticky = new DgrSmartOnFhirProxySticky();
			sticky.setSofProxyId(sofProxyId);
			sticky.setSofProxyDiversionId(diversionId);
			sticky.setSofProxyStickyInteraction(interaction);
			sticky.setSofProxyStickyType(type);
			sticky.setSofProxyStickyTypeId(id);
			sticky.setSofProxyStickyVerb(method);
			sticky.setSofProxyStickyPath(resourceURL);
			sticky.setSofProxyStickyHashcode(hashcode);

			// 儲存到資料庫
			DgrSmartOnFhirProxySticky savedSticky = dgrSmartOnFhirProxyStickyDao.save(sticky);

			// 同時更新快取
			stickyCache.put(hashcode, savedSticky);

			TPILogger.tl.debug("Sticky record saved to database and cache: hashcode=" + hashcode +
				", type=" + type + ", diversionId=" + diversionId);
		} catch (Exception e) {
			TPILogger.tl.error("Failed to save sticky record: " + StackTraceUtil.logStackTrace(e));
		}
	}

	/**
	 * 從 CREATE 回應的 Location header 解析新資源資訊
	 * Location header 格式範例: "http://backend/fhir/Patient/999"
	 *
	 * @param respObj HTTP 回應物件
	 * @return 包含 type 和 id 的 ResourceInfo，解析失敗則返回 empty
	 */
	private Optional<ResourceInfo> parseCreatedResourceLocation(HttpRespData respObj) {
		try {
			List<String> locationHeaders = respObj.respHeader.get("Location");
			if (locationHeaders == null || locationHeaders.isEmpty()) {
				TPILogger.tl.debug("No Location header in CREATE response");
				return Optional.empty();
			}

			String location = locationHeaders.get(0);
			TPILogger.tl.debug("Parsing Location header: " + location);

			URI uri = new URI(location);
			String path = uri.getPath();

			// 解析路徑，提取 type 和 id
			// 使用 GET method 來匹配 READ pattern
			FhirInteraction.IdentifyResult result =
				FhirInteraction.identifyWithParams(path, "GET");

			if (result.isIdentified() && result.getType() != null && result.getId() != null) {
				return Optional.of(new ResourceInfo(
					result.getType(),
					result.getId()
				));
			}

			TPILogger.tl.debug("Failed to parse Location header: " + location);
			return Optional.empty();
		} catch (Exception e) {
			TPILogger.tl.error("Error parsing Location header: " + StackTraceUtil.logStackTrace(e));
			return Optional.empty();
		}
	}

	/**
	 * Sticky 查詢結果
	 *
	 * @param fhirResult FHIR URL 解析結果
	 * @param isTrackable 是否為可追蹤的 interaction
	 * @param stickyRecord 查詢到的 sticky 紀錄
	 * @param hashcode 計算出的 hashcode
	 */
	@Builder
	public record StickyLookupResult(
		FhirInteraction.IdentifyResult fhirResult,
		boolean isTrackable,
		Optional<DgrSmartOnFhirProxySticky> stickyRecord,
		String hashcode
	) {
		/**
		 * 是否找到 sticky 紀錄
		 */
		public boolean hasStickyRecord() {
			return stickyRecord.isPresent();
		}

		/**
		 * 取得 sticky 紀錄的 diversion ID
		 */
		public Optional<Long> getStickyDiversionId() {
			return stickyRecord.map(DgrSmartOnFhirProxySticky::getSofProxyDiversionId);
		}
	}

	/**
	 * 儲存 Sticky 紀錄的上下文
	 *
	 * @param sofProxyId Proxy ID
	 * @param diversionId 選定的後端 Diversion ID
	 * @param lookupResult 查詢結果
	 * @param method HTTP Method
	 * @param resourceURL Resource URL
	 * @param respObj HTTP 回應物件
	 * @param statusCode HTTP 狀態碼
	 */
	@Builder
	public record StickyRecordContext(
		Long sofProxyId,
		Long diversionId,
		StickyLookupResult lookupResult,
		String method,
		String resourceURL,
		HttpRespData respObj,
		int statusCode
	) {}

	/**
	 * 將 Sticky 紀錄放入快取
	 * 供 CRUD Service 建立或更新 Sticky 後預熱快取使用
	 *
	 * @param hashcode 快取 key
	 * @param entity   Sticky 紀錄
	 */
	public void putCache(String hashcode, DgrSmartOnFhirProxySticky entity) {
		stickyCache.put(hashcode, entity);
		TPILogger.tl.debug("Cache put for hashcode: " + hashcode);
	}

	/**
	 * 清除快取中的特定紀錄
	 *
	 * @param hashcode 要清除的 hashcode
	 */
	public void evictCache(String hashcode) {
		stickyCache.invalidate(hashcode);
		TPILogger.tl.debug("Cache evicted for hashcode: " + hashcode);
	}

	/**
	 * 清除所有快取
	 */
	public void evictAllCache() {
		stickyCache.invalidateAll();
		TPILogger.tl.debug("All cache evicted");
	}

	/**
	 * 取得快取統計資訊
	 * 用於監控快取效能
	 *
	 * @return 快取統計資訊字串
	 */
	public String getCacheStats() {
		com.github.benmanes.caffeine.cache.stats.CacheStats stats = stickyCache.stats();
		return String.format(
			"Cache Stats: hitCount=%d, missCount=%d, hitRate=%.2f%%, " +
			"evictionCount=%d, size=%d",
			stats.hitCount(),
			stats.missCount(),
			stats.hitRate() * 100,
			stats.evictionCount(),
			stickyCache.estimatedSize()
		);
	}

	/**
	 * 資源資訊記錄
	 * 用於封裝從 Location header 解析出的資源類型和 ID
	 *
	 * @param type Resource Type (例如: "Patient")
	 * @param id Resource ID (例如: "999")
	 */
	private record ResourceInfo(String type, String id) {}
}
