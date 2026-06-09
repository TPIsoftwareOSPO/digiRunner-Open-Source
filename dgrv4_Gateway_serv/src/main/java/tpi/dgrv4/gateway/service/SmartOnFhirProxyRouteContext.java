package tpi.dgrv4.gateway.service;

import java.net.URI;
import java.util.List;

import lombok.Builder;

import tpi.dgrv4.entity.entity.DgrSmartOnFhirProxy;
import tpi.dgrv4.entity.entity.DgrSmartOnFhirProxyDiversion;

/**
 * Smart on FHIR Proxy 專屬的路由上下文。
 *
 * <p>從 {@code WebsiteService.RouteContext} 分離而來，
 * 移除 RouteType 概念（因為一定是 FHIR）。</p>
 */
public class SmartOnFhirProxyRouteContext {

	private SmartOnFhirProxyRouteContext() {
		// utility class
	}

	/**
	 * URL 拆分結果
	 *
	 * @param baseUrl      scheme + authority（如 {@code "https://hapi.fhir.tw:8080"}）
	 * @param fhirBasePath 正規化後的 path（如 {@code "/fhir"}），無 path 時為 {@code "/"}
	 */
	public record ParsedUrl(String baseUrl, String fhirBasePath) {

		/**
		 * 將 baseUrl 與 fhirBasePath 合併為完整 URL。
		 * 當 fhirBasePath 為 "/" 時只回傳 baseUrl，避免產生多餘的尾斜線。
		 */
		public String toFullUrl() {
			if ("/".equals(fhirBasePath)) {
				return baseUrl;
			}
			return baseUrl + fhirBasePath;
		}
	}

	/**
	 * 將完整 URL 拆分為 baseUrl（scheme + authority）與 fhirBasePath（path 部分）。
	 *
	 * <ul>
	 *   <li>{@code "https://hapi.fhir.tw/fhir"} → {@code baseUrl="https://hapi.fhir.tw", fhirBasePath="/fhir"}</li>
	 *   <li>{@code "https://hapi.fhir.tw"} → {@code baseUrl="https://hapi.fhir.tw", fhirBasePath="/"}</li>
	 *   <li>{@code "https://hapi.fhir.tw/"} → {@code baseUrl="https://hapi.fhir.tw", fhirBasePath="/"}</li>
	 *   <li>{@code "https://example.com:8080/fhir/r4"} → {@code baseUrl="https://example.com:8080", fhirBasePath="/fhir/r4"}</li>
	 * </ul>
	 *
	 * @param fullUrl 使用者填入的完整 URL
	 * @return 拆分結果；輸入為 null/blank 時回傳 null
	 */
	public static ParsedUrl parseDiversionUrl(String fullUrl) {
		if (fullUrl == null || fullUrl.isBlank()) {
			return null;
		}
		try {
			URI uri = new URI(fullUrl.trim());
			String baseUrl = uri.getScheme() + "://" + uri.getAuthority();
			String fhirBasePath = normalizeFhirBasePath(uri.getPath());
			return new ParsedUrl(baseUrl, fhirBasePath);
		} catch (Exception e) {
			// 解析失敗，將整個 URL 當作 baseUrl，basePath 預設 "/"
			return new ParsedUrl(fullUrl.trim(), "/");
		}
	}

	// ==================== Proxy URL 路徑解析 ====================

	/**
	 * 從 smart-on-fhir proxy URL 的路徑中拆出 proxyName 和剩餘路徑。
	 *
	 * 背景：smart-on-fhir proxy 的 URL 結構為
	 * {anything}/smart-on-fhir/{proxyName}[/{remainingPath}]，
	 * 此方法從路徑字串中找到 "smart-on-fhir" 段落後拆分。
	 * SmartOnFhirProxyService（處理 proxy 轉發）和 SmartAuthorizeService（驗證 aud）
	 * 都需要這個解析邏輯，集中在此避免重複。
	 *
	 * 範例：
	 *   "/smart-on-fhir/hapi-tw/Patient/123" → ("hapi-tw", "/Patient/123")
	 *   "/smart-on-fhir/hapi-tw"             → ("hapi-tw", "")
	 *   "/fhir/r4"                           → null（不含 smart-on-fhir 段落）
	 *
	 * @param path URL 路徑（可以是 requestURI 或從完整 URL 取出的 path）
	 * @return 解析結果，格式不合法時回傳 null
	 */
	public record ProxyPathParts(String proxyName, String remainingPath) {
	}

	public static ProxyPathParts extractProxyPathParts(String path) {
		if (path == null || path.isEmpty()) {
			return null;
		}

		String[] segments = path.split("/", -1);
		int sofIndex = -1;
		for (int i = 0; i < segments.length; i++) {
			if (SMART_ON_FHIR_PATH_SEGMENT.equals(segments[i])) {
				sofIndex = i;
				break;
			}
		}

		if (sofIndex == -1 || sofIndex + 1 >= segments.length || segments[sofIndex + 1].isEmpty()) {
			return null;
		}

		String proxyName = segments[sofIndex + 1];
		String remaining = String.join("/",
				java.util.Arrays.copyOfRange(segments, sofIndex + 2, segments.length));
		if (!remaining.isEmpty()) {
			remaining = "/" + remaining;
		}

		return new ProxyPathParts(proxyName, remaining);
	}

	private static final String SMART_ON_FHIR_PATH_SEGMENT = "smart-on-fhir";

	// ==================== FHIR Base Path 正規化 ====================

	/**
	 * 正規化 FHIR Base Path。
	 * 支援完整 URL、絕對路徑、相對路徑等輸入格式，統一輸出為 "/path" 形式。
	 *
	 * <ul>
	 *   <li>{@code "https://hapi.fhir.tw/fhir"} → {@code "/fhir"}</li>
	 *   <li>{@code "/fhir"} → {@code "/fhir"}</li>
	 *   <li>{@code "fhir"} → {@code "/fhir"}</li>
	 *   <li>{@code "/fhir/"} → {@code "/fhir"}</li>
	 *   <li>{@code null} / {@code ""} → {@code "/"}</li>
	 * </ul>
	 */
	public static String normalizeFhirBasePath(String input) {
		if (input == null || input.isBlank()) {
			return "/";
		}
		String path = input.trim();
		// 完整 URL → 取 path 部分
		if (path.startsWith("http://") || path.startsWith("https://")) {
			try {
				path = new URI(path).getPath();
			} catch (Exception e) {
				// 解析失敗就當作 path 處理
			}
		}
		// 確保以 / 開頭
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		// 移除尾斜線（但保留 "/"）
		if (path.length() > 1 && path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		// 空路徑 fallback
		if (path.isEmpty()) {
			path = "/";
		}
		return path;
	}

	/**
	 * Smart on FHIR Proxy 路由上下文
	 *
	 * @param routeId        Proxy ID（對應 DGR_SMART_ON_FHIR_PROXY.SOF_PROXY_ID）
	 * @param routeName      Proxy 名稱
	 * @param isEnabled      是否啟用
	 * @param remark         備註
	 * @param securityConfig 安全檢查配置
	 * @param trafficConfig  流量控制配置
	 * @param diversions     分流設定列表
	 * @param ignoreApiPaths 忽略檢查的 API 路徑
	 * @param showLog        是否顯示日誌
	 * @param clientIds      允許的 Client ID 列表
	 */
	@Builder
	public record RouteContext(
			Long routeId,
			String routeName,
			boolean isEnabled,
			String remark,
			SecurityConfig securityConfig,
			TrafficConfig trafficConfig,
			List<DiversionConfig> diversions,
			String ignoreApiPaths,
			boolean showLog,
			List<String> clientIds,
			boolean enableUrlRewrite) {

		/**
		 * 從 DgrSmartOnFhirProxy 和 DgrSmartOnFhirProxyDiversion 列表建立 RouteContext
		 */
		public static RouteContext fromFhirProxy(DgrSmartOnFhirProxy proxy,
				List<DgrSmartOnFhirProxyDiversion> diversions) {
			return RouteContext.builder()
					.routeId(proxy.getSofProxyId())
					.routeName(proxy.getSofProxyName())
					.isEnabled(proxy.getSofProxyStatus() == DgrSmartOnFhirProxy.Status.ENABLED)
					.remark(proxy.getSofProxyRemark())
					.securityConfig(SecurityConfig.fromFhirProxy(proxy))
					.trafficConfig(TrafficConfig.fromFhirProxy(proxy))
					.diversions(diversions.stream()
							.map(DiversionConfig::fromFhirDiversion)
							.toList())
					.ignoreApiPaths(proxy.getSofProxyIgnoreApi())
					.showLog(proxy.getSofProxyShowLog() == DgrSmartOnFhirProxy.Status.ENABLED)
					.clientIds(proxy.getSofProxyClientId())
					.enableUrlRewrite(proxy.getSofProxyUrlRewrite() == DgrSmartOnFhirProxy.Status.ENABLED)
					.build();
		}
	}

	/**
	 * 安全檢查配置
	 *
	 * @param enableSqlInjectionCheck 是否啟用 SQL Injection 檢查
	 * @param enableXssCheck          是否啟用 XSS 檢查
	 * @param enableXxeCheck          是否啟用 XXE 檢查
	 * @param enableAccessToken       是否啟用 ACCESS TOKEN 檢查
	 */
	@Builder
	public record SecurityConfig(
			boolean enableSqlInjectionCheck,
			boolean enableXssCheck,
			boolean enableXxeCheck,
			boolean enableAccessToken) {

		public static SecurityConfig fromFhirProxy(DgrSmartOnFhirProxy proxy) {
			return SecurityConfig.builder()
					.enableSqlInjectionCheck(proxy.getSofProxySqlInjection() == DgrSmartOnFhirProxy.Status.ENABLED)
					.enableXssCheck(proxy.getSofProxyXss() == DgrSmartOnFhirProxy.Status.ENABLED)
					.enableXxeCheck(proxy.getSofProxyXxe() == DgrSmartOnFhirProxy.Status.ENABLED)
					.enableAccessToken(proxy.getSofProxyAccessToken() == DgrSmartOnFhirProxy.Status.ENABLED)
					.build();
		}
	}

	/**
	 * 流量控制配置
	 *
	 * @param enableTrafficControl          是否啟用流量控制
	 * @param enableSmartOnFhirProxySticky  是否啟用 Sticky Session
	 * @param tps                           每秒交易數限制
	 */
	@Builder
	public record TrafficConfig(
			boolean enableTrafficControl,
			boolean enableSmartOnFhirProxySticky,
			int tps) {

		public static TrafficConfig fromFhirProxy(DgrSmartOnFhirProxy proxy) {
			return TrafficConfig.builder()
					.enableTrafficControl(proxy.getSofProxyTraffic() == DgrSmartOnFhirProxy.Status.ENABLED)
					.enableSmartOnFhirProxySticky(proxy.getSofProxySticky() == DgrSmartOnFhirProxy.Status.ENABLED)
					.tps(proxy.getSofProxyTps() != null ? proxy.getSofProxyTps() : 0)
					.build();
		}
	}

	/**
	 * 分流配置
	 *
	 * @param diversionId   分流 ID
	 * @param probability   分流機率（權重）
	 * @param targetUrl     目標 URL
	 * @param fhirBasePath  此 Diversion 的 FHIR API 根路徑（正規化後，如 "/fhir"）
	 */
	@Builder
	public record DiversionConfig(
			Long diversionId,
			int probability,
			String targetUrl,
			String fhirBasePath) {

		public static DiversionConfig fromFhirDiversion(DgrSmartOnFhirProxyDiversion diversion) {
			return DiversionConfig.builder()
					.diversionId(diversion.getSofProxyDiversionId())
					.probability(diversion.getSofProxyDiversionProbability())
					.targetUrl(diversion.getSofProxyDiversionUrl())
					.fhirBasePath(normalizeFhirBasePath(diversion.getSofProxyDiversionFhirBasePath()))
					.build();
		}
	}
}
