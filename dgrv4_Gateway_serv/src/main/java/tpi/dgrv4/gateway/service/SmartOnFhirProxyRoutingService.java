package tpi.dgrv4.gateway.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.constant.FhirInteraction;
import tpi.dgrv4.entity.constant.FhirInteraction.IdentifyResult;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.SmartOnFhirProxyHttpClient.SmartOnFhirProxyResponse;
import tpi.dgrv4.gateway.service.SmartOnFhirProxyRouteContext.DiversionConfig;
import tpi.dgrv4.gateway.service.SmartOnFhirProxyRouteContext.RouteContext;

/**
 * SMART on FHIR Proxy 路由服務
 * 負責 Resource Type 智能路由邏輯，包含：
 * - Sticky 紀錄查詢
 * - 手動配置查詢
 * - POST 智能分配
 * - GET/PUT/PATCH/DELETE 自動發現
 */
@Service
public class SmartOnFhirProxyRoutingService {

	private final SmartOnFhirProxyStickyService stickyService;
	private final SmartOnFhirProxyHttpClient httpClient;

	@Autowired
	public SmartOnFhirProxyRoutingService(SmartOnFhirProxyStickyService stickyService,
			SmartOnFhirProxyHttpClient httpClient) {
		this.stickyService = stickyService;
		this.httpClient = httpClient;
	}

	/**
	 * 自動發現時已讀取的回應（取代 HttpRespData）
	 */
	public record PreDiscoveredResponse(
			int statusCode,
			Map<String, List<String>> headers,
			byte[] body) {

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof PreDiscoveredResponse that)) return false;
			return statusCode == that.statusCode
				&& java.util.Objects.equals(headers, that.headers)
				&& Arrays.equals(body, that.body);
		}

		@Override
		public int hashCode() {
			int result = java.util.Objects.hash(statusCode, headers);
			result = 31 * result + Arrays.hashCode(body);
			return result;
		}

		@Override
		public String toString() {
			return "PreDiscoveredResponse[statusCode=" + statusCode
				+ ", headers=" + headers
				+ ", body=" + Arrays.toString(body) + "]";
		}
	}

	/**
	 * 路由結果
	 */
	public record RoutingResult(
			DiversionConfig diversionConfig,
			PreDiscoveredResponse preDiscoveredResponse,
			boolean needSaveBinding,
			String resourceType,
			String resourceId,
			String compartment,
			String compartmentId) {
	}

	/**
	 * 自動發現結果
	 */
	public record AutoDiscoveryResult(
			boolean found,
			Long diversionId,
			DiversionConfig diversionConfig,
			PreDiscoveredResponse response) {
	}

	/**
	 * 選擇後端 Diversion（主入口）
	 *
	 * @param routeContext    路由上下文
	 * @param resourceURL     資源 URL（如 /Patient/123）
	 * @param method          HTTP 方法
	 * @param headersSupplier Header 供應器（延遲取得，僅在需要時調用）
	 * @param queryStr        查詢字串
	 * @param payload         請求 Body
	 * @return 路由結果
	 */
	public RoutingResult selectDiversion(
			RouteContext routeContext,
			String resourceURL,
			String method,
			Supplier<Map<String, List<String>>> headersSupplier,
			String queryStr,
			String payload) {

		// --- Per-Diversion FHIR Base Path 分流 ---
		// 找出 fhirBasePath 與 resourceURL 匹配的最長 basePath
		String matchedBasePath = null;
		for (DiversionConfig d : routeContext.diversions()) {
			String bp = d.fhirBasePath();
			if ("/".equals(bp) || resourceURL.equals(bp) || resourceURL.startsWith(bp + "/")) {
				if (matchedBasePath == null || bp.length() > matchedBasePath.length()) {
					matchedBasePath = bp;
				}
			}
		}

		if (matchedBasePath == null) {
			// 沒有任何 diversion 的 fhirBasePath 匹配 → 非 FHIR，機率選擇
			DiversionConfig dc = selectByProbability(routeContext.diversions());
			TPILogger.tl.debug("Non-FHIR path, probability selection: " + resourceURL);
			return new RoutingResult(dc, null, false, null, null, null, null);
		}

		// 剝離最長匹配的 basePath，讓 FhirInteraction 正確辨識
		String fhirResourcePath = "/".equals(matchedBasePath) ? resourceURL
				: resourceURL.substring(matchedBasePath.length());
		if (fhirResourcePath.isEmpty()) {
			fhirResourcePath = "/";
		}

		// 使用 FhirInteraction 識別路徑類型並提取參數
		String urlWithQuery = queryStr != null ? fhirResourcePath + "?" + queryStr : fhirResourcePath;
		IdentifyResult fhirResult = FhirInteraction.identifyWithParams(urlWithQuery, method);

		String resourceType = fhirResult.getType();  // 從 FHIR 解析結果取得 Resource Type
		FhirInteraction interaction = fhirResult.getInteraction();

		// 從 FHIR 解析結果取得 compartment 與 resource ID
		// compartment search 時：compartment=getCompartment(), compartmentId=getId()
		// 普通 read 時：resourceId=getId(), compartment/compartmentId=null
		String fhirCompartment = fhirResult.getCompartment();
		String fhirResourceId = (fhirCompartment == null) ? fhirResult.getId() : null;
		String fhirCompartmentId = (fhirCompartment != null) ? fhirResult.getId() : null;

		TPILogger.tl.debug("FHIR Interaction identified: " + interaction +
			", resourceType=" + resourceType + ", url=" + resourceURL);
		
		// Fallback：若 FhirInteraction 無法識別，嘗試從路徑萃取 ResourceType
		if (resourceType == null && fhirResourcePath.contains("/$")) {
		    String[] segments = fhirResourcePath.split("/");
		    for (String seg : segments) {
		        if (!seg.isEmpty() && !seg.startsWith("$") && Character.isUpperCase(seg.charAt(0))) {
		            resourceType = seg;
		            break;
		        }
		    }
		    TPILogger.tl.debug("resourceType: " + resourceType + 
		        ", from path: " + fhirResourcePath);
		}

		DiversionConfig diversionConfig = null;
		PreDiscoveredResponse preDiscoveredResp = null;
		boolean needSaveBinding = false;

		// 特殊端點不需要 Sticky（如 /metadata, /_history 系統級）
		if (!requiresSticky(interaction, resourceType)) {
			diversionConfig = selectByProbability(routeContext.diversions());
			TPILogger.tl.debug("Non-sticky interaction, using probability: " + interaction);
			return new RoutingResult(diversionConfig, null, false, resourceType,
				fhirResourceId, fhirCompartment, fhirCompartmentId);
		}

		// Sticky 開關未啟用，直接用概率選擇
		if (!routeContext.trafficConfig().enableSmartOnFhirProxySticky()) {
			diversionConfig = selectByProbability(routeContext.diversions());
			TPILogger.tl.debug("Sticky disabled, using probability: resourceType=" + resourceType);
			return new RoutingResult(diversionConfig, null, false, resourceType,
				fhirResourceId, fhirCompartment, fhirCompartmentId);
		}

		// 1. 查詢 Sticky 紀錄（按 Resource Type）
		SmartOnFhirProxyStickyService.TypeLookupResult typeLookup =
			stickyService.lookupByType(routeContext.routeId(), resourceType);

		if (typeLookup.found()) {
			// 2. 有紀錄，檢查 Diversion 是否有效
			diversionConfig = findDiversionById(
				typeLookup.diversionId().orElseThrow(() ->
					new IllegalStateException("TypeLookupResult found=true but diversionId is empty")),
				routeContext.diversions());

			if (diversionConfig == null) {
				// Diversion 已被刪除，清除紀錄
				stickyService.evictCache(typeLookup.hashcode());
				TPILogger.tl.debug("Diversion not found, evicting cache: type=" + resourceType);
			}
		}

		if (diversionConfig == null) {
			// 3. 無紀錄：所有 Interaction 都先用智能分配選擇最佳後端
			Long balancedId = stickyService.selectDiversionByBalance(
				routeContext.routeId(),
				resourceType,
				toDiversionInfoList(routeContext.diversions()));

			if (interaction == FhirInteraction.CREATE) {
				// 3a. CREATE (POST /[type])：直接使用平衡選擇結果
				diversionConfig = findDiversionById(balancedId, routeContext.diversions());
				needSaveBinding = true;
				TPILogger.tl.debug("Balance selection for CREATE: type=" + resourceType +
					", diversionId=" + balancedId);
			} else {
				// 3b. READ/UPDATE/PATCH/DELETE/SEARCH 等：
				//     以平衡選擇的 Backend 為優先，再自動發現驗證
				Map<String, List<String>> headers = headersSupplier.get();
				AutoDiscoveryResult discovery = autodiscoverWithPreference(
					routeContext.diversions(), balancedId, resourceURL, method, payload, headers, queryStr);

				if (discovery.found()) {
					diversionConfig = discovery.diversionConfig();
					preDiscoveredResp = discovery.response();
					needSaveBinding = true;
					TPILogger.tl.debug("Balanced autodiscovery success: type=" + resourceType +
						", balancedId=" + balancedId +
						", actualId=" + diversionConfig.diversionId());
				} else {
					// 全部 404，使用平衡選擇結果（fallback）
					diversionConfig = findDiversionById(balancedId, routeContext.diversions());
					TPILogger.tl.debug("Autodiscovery failed, using balance selection: type=" + resourceType);
				}
			}
		}

		return new RoutingResult(diversionConfig, preDiscoveredResp, needSaveBinding, resourceType,
			fhirResourceId, fhirCompartment, fhirCompartmentId);
	}

	/**
	 * 判斷此 Interaction 是否需要 Sticky 機制
	 *
	 * @param interaction  FHIR Interaction 類型
	 * @param resourceType Resource Type（可能為 null）
	 * @return true 如果需要 Sticky
	 */
	private boolean requiresSticky(FhirInteraction interaction, String resourceType) {
		if (interaction == null || resourceType == null) {
			return false;
		}

		// 不需要 Sticky 的 Interaction：
		// - CAPABILITIES (/metadata) - 系統級端點
		// - HISTORY_SYSTEM (/_history) - 系統級端點
		// - SEARCH_SYSTEM (/_search) - 系統級端點
		return interaction != FhirInteraction.CAPABILITIES
			&& interaction != FhirInteraction.HISTORY_SYSTEM
			&& interaction != FhirInteraction.SEARCH_SYSTEM;
	}

	/**
	 * 保存 Resource Type 綁定
	 *
	 * @param routeId      路由 ID
	 * @param resourceType Resource Type
	 * @param diversionId  Diversion ID
	 */
	public void saveBinding(Long routeId, String resourceType, Long diversionId) {
		stickyService.saveTypeBinding(routeId, resourceType, diversionId);
	}

	// ============ 輔助方法 ============

	/**
	 * 從 URL 和 Method 提取 Resource Type
	 * 使用 FhirInteraction 正確識別 FHIR 路徑
	 *
	 * @param resourceURL 資源 URL（如 /Patient/123）
	 * @param method      HTTP 方法
	 * @return Resource Type，特殊端點（如 /metadata）返回 null
	 */
	public String extractResourceType(String resourceURL, String method) {
		if (resourceURL == null || resourceURL.isEmpty()) {
			return null;
		}
		IdentifyResult result = FhirInteraction.identifyWithParams(resourceURL, method);
		return result.getType();
	}

	/**
	 * 根據 ID 查找 Diversion
	 */
	private DiversionConfig findDiversionById(Long diversionId,
			List<DiversionConfig> diversions) {
		if (diversionId == null || diversions == null) {
			return null;
		}
		return diversions.stream()
				.filter(d -> diversionId.equals(d.diversionId()))
				.findFirst()
				.orElse(null);
	}

	/**
	 * 將 DiversionConfig 列表轉換為 DiversionInfo 列表
	 */
	private List<SmartOnFhirProxyStickyService.DiversionInfo> toDiversionInfoList(
			List<DiversionConfig> diversions) {
		return diversions.stream()
				.map(d -> new SmartOnFhirProxyStickyService.DiversionInfo(
						d.diversionId(),
						d.probability()))
				.toList();
	}

	/**
	 * 概率選擇 Diversion
	 */
	private DiversionConfig selectByProbability(List<DiversionConfig> diversions) {
		if (diversions == null || diversions.isEmpty()) {
			return null;
		}

		// 計算總權重
		int totalWeight = diversions.stream()
				.mapToInt(DiversionConfig::probability)
				.sum();

		if (totalWeight <= 0) {
			return diversions.get(0);
		}

		// 隨機選擇
		int random = (int) (Math.random() * totalWeight);
		int cumulative = 0;

		for (DiversionConfig d : diversions) {
			cumulative += d.probability();
			if (random < cumulative) {
				return d;
			}
		}

		return diversions.get(diversions.size() - 1);
	}

	/**
	 * 帶優先選擇的自動發現：先嘗試平衡選擇的 Backend，再依 probability 降序嘗試其餘
	 * 解決 5:5 等權重場景下 autodiscover 總是命中同一台的問題
	 *
	 * @param diversions    所有可用的 Diversion
	 * @param preferredId   由 selectDiversionByBalance 選出的優先 Diversion ID
	 * @param resourceURL   資源 URL
	 * @param method        HTTP 方法
	 * @param payload       請求 Body
	 * @param headers       請求 Headers
	 * @param queryStr      查詢字串
	 * @return 自動發現結果
	 */
	private AutoDiscoveryResult autodiscoverWithPreference(
			List<DiversionConfig> diversions,
			Long preferredId,
			String resourceURL,
			String method,
			String payload,
			Map<String, List<String>> headers,
			String queryStr) {

		// 重新排序：平衡選擇的 Backend 優先，其餘依 probability 降序
		List<DiversionConfig> ordered = new ArrayList<>(diversions);
		ordered.sort((a, b) -> {
			boolean aPreferred = a.diversionId().equals(preferredId);
			boolean bPreferred = b.diversionId().equals(preferredId);
			if (aPreferred != bPreferred) {
				return aPreferred ? -1 : 1;
			}
			return Integer.compare(b.probability(), a.probability());
		});

		byte[] bodyBytes = (payload != null) ? payload.getBytes(java.nio.charset.StandardCharsets.UTF_8) : null;

		for (DiversionConfig d : ordered) {
			String url = buildUrl(d.targetUrl(), resourceURL);
			if (queryStr != null) {
				url += "?" + queryStr;
			}

			try (SmartOnFhirProxyResponse resp = httpClient.forward(url, method, headers, bodyBytes)) {
				if (resp.statusCode() != HttpStatus.NOT_FOUND.value()) {
					byte[] respBody = resp.body().readAllBytes();
					TPILogger.tl.debug("Balanced autodiscovery found: diversionId=" + d.diversionId() +
							", preferred=" + d.diversionId().equals(preferredId) +
							", url=" + url + ", statusCode=" + resp.statusCode());
					PreDiscoveredResponse preResp = new PreDiscoveredResponse(
							resp.statusCode(), resp.headers(), respBody);
					return new AutoDiscoveryResult(true, d.diversionId(), d, preResp);
				}
			} catch (Exception e) {
				TPILogger.tl.error("Balanced autodiscovery error for diversionId=" + d.diversionId() +
						": " + StackTraceUtil.logStackTrace(e));
			}
		}

		TPILogger.tl.debug("Balanced autodiscovery: resource not found in any backend");
		return new AutoDiscoveryResult(false, null, null, null);
	}

	/**
	 * 自動發現：嘗試所有 Backend
	 * 依 probability 降序嘗試，第一個非 404 的即為正確後端
	 */
	private AutoDiscoveryResult autodiscover(
			List<DiversionConfig> diversions,
			String resourceURL,
			String method,
			String payload,
			Map<String, List<String>> headers,
			String queryStr) {

		// 依 probability 降序排列（高權重優先嘗試）
		List<DiversionConfig> sorted = diversions.stream()
				.sorted((a, b) -> Integer.compare(b.probability(), a.probability()))
				.toList();

		byte[] bodyBytes = (payload != null) ? payload.getBytes(java.nio.charset.StandardCharsets.UTF_8) : null;

		for (DiversionConfig d : sorted) {
			String url = buildUrl(d.targetUrl(), resourceURL);
			if (queryStr != null) {
				url += "?" + queryStr;
			}

			try (SmartOnFhirProxyResponse resp = httpClient.forward(url, method, headers, bodyBytes)) {
				if (resp.statusCode() != HttpStatus.NOT_FOUND.value()) {
					byte[] respBody = resp.body().readAllBytes();
					TPILogger.tl.debug("Auto discovery found: diversionId=" + d.diversionId() +
							", url=" + url + ", statusCode=" + resp.statusCode());
					PreDiscoveredResponse preResp = new PreDiscoveredResponse(
							resp.statusCode(), resp.headers(), respBody);
					return new AutoDiscoveryResult(true, d.diversionId(), d, preResp);
				}
			} catch (Exception e) {
				TPILogger.tl.error("Auto discovery error for diversionId=" + d.diversionId() +
						": " + StackTraceUtil.logStackTrace(e));
			}
		}

		// 全部 404 或失敗
		TPILogger.tl.debug("Auto discovery: resource not found in any backend");
		return new AutoDiscoveryResult(false, null, null, null);
	}

	/**
	 * 建構完整 URL
	 */
	private String buildUrl(String baseUrl, String resourceUrl) {
		return baseUrl + resourceUrl;
	}
}
