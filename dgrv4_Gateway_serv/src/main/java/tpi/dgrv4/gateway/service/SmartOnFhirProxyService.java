package tpi.dgrv4.gateway.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.common.utils.CheckmarxCommUtils;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.DgrSmartOnFhirProxy;
import tpi.dgrv4.entity.entity.DgrSmartOnFhirProxyDiversion;
import tpi.dgrv4.entity.repository.DgrSmartOnFhirProxyDao;
import tpi.dgrv4.entity.repository.DgrSmartOnFhirProxyDiversionDao;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.component.check.SqlInjectionCheck;
import tpi.dgrv4.gateway.component.check.XssCheck;
import tpi.dgrv4.gateway.component.check.XxeCheck;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.SmartOnFhirProxyHttpClient.SmartOnFhirProxyResponse;
import tpi.dgrv4.gateway.service.SmartOnFhirProxyRouteContext.DiversionConfig;
import tpi.dgrv4.gateway.service.SmartOnFhirProxyRouteContext.RouteContext;
import tpi.dgrv4.gateway.service.SmartOnFhirProxyRoutingService.RoutingResult;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp;
import tpi.dgrv4.gateway.vo.SmartOnFhirAuthorizationContext;

/**
 * Smart on FHIR Proxy 主服務。
 *
 * <p>
 * 從 {@link WebsiteService} 獨立出來，專責處理 Smart on FHIR Proxy 的請求轉發。 使用
 * {@link SmartOnFhirProxyHttpClient}（java.net.http.HttpClient）取代 HttpUtil，
 * 支援二進制回應串流、文字內容緩衝改寫。
 * </p>
 */
@Service
public class SmartOnFhirProxyService {
	private static final ObjectMapper objectMapper = new ObjectMapper();

	// ===== URL / Path =====
	private static final String PROXY_PREFIX_TEMPLATE = "/smart-on-fhir/";

	// ===== Headers =====
	private static final String HEADER_X_FORWARDED_PROTO = "X-Forwarded-Proto";
	private static final String HEADER_X_FORWARDED_HOST = "X-Forwarded-Host";
	private static final String HEADER_X_FORWARDED_PREFIX = "X-Forwarded-Prefix";
	private static final String HEADER_HOST = "Host";
	private static final String HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";
	private static final String HEADER_IF_NONE_MATCH = "If-None-Match";
	private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
	private static final String HEADER_TRANSFER_ENCODING = "Transfer-Encoding";
	private static final String HSTS_HEADER_NAME = "Strict-Transport-Security";
	private static final String HSTS_HEADER_VALUE = "max-age=31536000; includeSubDomains; preload";

	// ===== SMART App Launch =====
	private static final String WELL_KNOWN_SMART_CONFIGURATION = "/.well-known/smart-configuration";

	// ===== Content Types =====
	private static final String CONTENT_TYPE_JSON_UTF8 = "application/json;charset=UTF-8";
	private static final String CONTENT_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";

	// ===== Error Codes / Messages =====
	private static final String ERROR_INVALID_TOKEN = "invalid_token";
	private static final String ERROR_MSG_ACCESS_DENIED = "Access denied: invalid or missing access token";
	private static final String ERROR_SQL_INJECTION = "Sql Injection";
	private static final String ERROR_XSS = "xss";
	private static final String ERROR_XXE = "xxe";
	private static final String ERROR_INVALID_PARAM = "Invalid parameter";
	private static final String ERROR_TPS = "tps";
	private static final String ERROR_TPS_EXCEEDED = "exceeds TPS limit";
	private static final String ERROR_NOT_FOUND = "websiteName not found";

	// ===== Log Templates =====
	private static final String LOG_PREFIX = "\n--【LOGUUID】【";
	private static final String LOG_START_SUFFIX = "】【Start smart-on-fhir】--";
	private static final String LOG_END_SUFFIX = "】【End smart-on-fhir】--";
	private static final String LOG_END_PRE_DISCOVERED = "】【End smart-on-fhir (pre-discovered)】--";
	private static final String LOG_END_ERROR = "】【End smart-on-fhir error】--";

	// ===== HTTP Status Range =====
	private static final int HTTP_OK_MIN = 200;
	private static final int HTTP_OK_MAX = 300;

	// ===== Dependencies =====
	private final SmartOnFhirProxyHttpClient httpClient;
	private final SmartOnFhirProxyRoutingService routingService;
	private final DgrSmartOnFhirProxyDao proxyDao;
	private final DgrSmartOnFhirProxyDiversionDao diversionDao;
	private final SqlInjectionCheck sqlInjectionCheck;
	private final XssCheck xssCheck;
	private final XxeCheck xxeCheck;
	private final TokenHelper tokenHelper;

	@Autowired
	private SmartDiscoveryService smartDiscoveryService;

	/** 流量控制：routeName → (targetUrl → TrafficCounter) */
	public final ConcurrentHashMap<String, ConcurrentHashMap<String, TrafficCounter>> trafficMap = new ConcurrentHashMap<>();

	@Autowired
	public SmartOnFhirProxyService(SmartOnFhirProxyHttpClient httpClient, SmartOnFhirProxyRoutingService routingService,
			DgrSmartOnFhirProxyDao proxyDao, DgrSmartOnFhirProxyDiversionDao diversionDao,
			SqlInjectionCheck sqlInjectionCheck, XssCheck xssCheck, XxeCheck xxeCheck, TokenHelper tokenHelper) {
		this.httpClient = httpClient;
		this.routingService = routingService;
		this.proxyDao = proxyDao;
		this.diversionDao = diversionDao;
		this.sqlInjectionCheck = sqlInjectionCheck;
		this.xssCheck = xssCheck;
		this.xxeCheck = xxeCheck;
		this.tokenHelper = tokenHelper;
	}

	// ==================== 主入口 ====================

	/**
	 * Smart on FHIR Proxy 的請求處理入口。
	 *
	 * @param httpHeaders Spring 解析的 HTTP headers
	 * @param request     原始 Servlet request
	 * @param response    Servlet response（直接寫入）
	 * @param websiteName proxy 名稱（URL path 中的 {websiteName}）
	 */
	public void resource(HttpHeaders httpHeaders, HttpServletRequest request, HttpServletResponse response,
			String websiteName) throws Exception {

		try {
			// 1. URL 解析
			String requestUri = request.getRequestURI();
			SmartOnFhirProxyRouteContext.ProxyPathParts urlParts = SmartOnFhirProxyRouteContext
					.extractProxyPathParts(requestUri);
			String proxyName = urlParts != null ? urlParts.proxyName() : "";
			String resourceURL = urlParts != null ? urlParts.remainingPath() : "";

			// 2. 載入 RouteContext（DB 路由，可能為 null）
			String uuid = UUID.randomUUID().toString();
			RouteContext routeContext = loadRouteContext(websiteName);

			// 2.5 SMART App Launch：攔截 .well-known/smart-configuration
			// 放在 loadRouteContext 之後、null check 之前，
			// 讓有 proxy 設定時可取得 routeContext 資訊（未來擴充用），
			// 沒有 proxy 設定時也能正常回應（方便測試）。
			if (resourceURL.endsWith(WELL_KNOWN_SMART_CONFIGURATION)) {
				smartDiscoveryService.handleDiscovery(request, response);
				return;
			}

			if (routeContext == null) {
				writeError(response, HttpStatus.NOT_FOUND, ERROR_NOT_FOUND,
						"Smart on FHIR Proxy: [" + websiteName + "] could not find the proxy route", requestUri, false,
						uuid);
				return;
			}

			String method = request.getMethod();
			boolean isShowLog = routeContext.showLog();

			// 3. 讀取 request body
			byte[] requestBody = readRequestBody(request);
			String payload = new String(requestBody, StandardCharsets.UTF_8);

			logEnd(isShowLog, uuid, LOG_START_SUFFIX, "");

			// 判斷是否為 Bundle transaction 請求
			boolean isBundleTransaction = isBundleTransaction(method, resourceURL, payload);
			TPILogger.tl.debug("Bundle Transaction: " + isBundleTransaction);

			if (isBundleTransaction && routeContext.trafficConfig().enableSmartOnFhirProxySticky()) {
				// 請求 Bundle transaction 時, 不能開啟 sticky
				writeError(response, HttpStatus.BAD_REQUEST, "Bundle transaction error",
						"When requesting a Bundle transaction, sticky must not be enabled", requestUri, isShowLog,
						uuid);
				return;
			}

			// 4. 安全檢查（ignoreApiPaths 中的路徑跳過）
			ResponseEntity<?> securityError = runAllSecurityChecks(resourceURL, payload, request.getQueryString(),
					routeContext, requestUri);
			if (securityError != null) {
				writeErrorResponse(response, securityError, isShowLog, uuid);
				return;
			}

			// 5. 智能路由（selectDiversion）
			RoutingResult routingResult = routingService.selectDiversion(routeContext, resourceURL, method,
					() -> convertHeaders(request, httpHeaders), request.getQueryString(), payload);

			DiversionConfig diversionConfig = routingResult.diversionConfig();
			if (diversionConfig == null) {
				writeError(response, HttpStatus.NOT_FOUND, ERROR_NOT_FOUND,
						"Smart on FHIR Proxy: [" + websiteName + "] no diversion available", requestUri, isShowLog,
						uuid);
				return;
			}

			// 6.Access Token 檢查
			if (routeContext.securityConfig().enableAccessToken()) {
				if (!isLauncherAdminBypass(request)) {
					ResponseEntity<?> respEntity = getTokenHelper().verifyAccessTokenForSmartOnFhir(httpHeaders,
							request, requestUri, routingResult.resourceType(), routeContext.clientIds(),
							resourceURL, isBundleTransaction);
					if (respEntity != null) {
						writeErrorResponse(response, respEntity, isShowLog, uuid);
						return;
					}
				}
			}

			// 6.8 錯誤模擬：request_* 系列（從 JWT sim_error claim 讀取）
			{
				SmartOnFhirAuthorizationContext simCtx = (SmartOnFhirAuthorizationContext) request
						.getAttribute("authContext");
				if (simCtx != null && simCtx.getSimError() != null) {
					String simErr = simCtx.getSimError();
					if ("request_invalid_token".equals(simErr)) {
						writeError(response, HttpStatus.UNAUTHORIZED, "invalid_token",
								"Simulated error: invalid access token", requestUri, isShowLog, uuid);
						return;
					}
					if ("request_expired_token".equals(simErr)) {
						writeError(response, HttpStatus.UNAUTHORIZED, "invalid_token",
								"Simulated error: access token has expired", requestUri, isShowLog, uuid);
						return;
					}
				}
			}

			// 7. 流量控制
			String redirectedUrl = diversionConfig.targetUrl();
			ResponseEntity<?> trafficError = checkTraffic(routeContext, redirectedUrl);
			if (trafficError != null) {
				writeErrorResponse(response, trafficError, isShowLog, uuid);
				return;
			}

			// 8. 組裝完整 URL
			String completeURL = redirectedUrl + resourceURL;
			if (request.getQueryString() != null) {
				completeURL += "?" + request.getQueryString();
			}

			// 9. URL 改寫準備（僅在 enableUrlRewrite 時計算）
			boolean rewrite = routeContext.enableUrlRewrite();
			String proxyPrefix = PROXY_PREFIX_TEMPLATE + proxyName;
			String proxyBaseUrl = rewrite ? resolveProxyBaseUrl(request, proxyPrefix) : null;

			// 10. 轉發 headers + 改寫 request body
			Map<String, List<String>> headers = convertHeaders(request, httpHeaders);
			injectForwardedHeaders(headers, request, proxyPrefix, redirectedUrl);

			// 10.5 注入 SMART on FHIR launch context headers
			// 先清除來源請求可能夾帶的 X-Smart-* headers（防止 header 注入）
			headers.entrySet().removeIf(e -> e.getKey().toLowerCase().startsWith("x-smart-"));

			SmartOnFhirAuthorizationContext authCtx = (SmartOnFhirAuthorizationContext) request
					.getAttribute("authContext");
			if (authCtx != null) {
				if (authCtx.getPatient() != null && !authCtx.getPatient().isEmpty()) {
					headers.put("X-Smart-Patient", List.of(authCtx.getPatient()));
				}
				if (authCtx.getEncounter() != null && !authCtx.getEncounter().isEmpty()) {
					headers.put("X-Smart-Encounter", List.of(authCtx.getEncounter()));
				}
			}

			byte[] forwardBody = rewrite
					? SmartOnFhirProxyResponseRewriter.rewriteRequestBody(requestBody, proxyBaseUrl, redirectedUrl)
					: requestBody;

			// 11. HTTP 轉發 + 回應
			if (routingResult.preDiscoveredResponse() != null) {
				writePreDiscoveredResponse(routingResult, proxyName, proxyBaseUrl, resourceURL, response, rewrite,
						isShowLog, uuid);
			} else {
				forwardAndWriteResponse(new ForwardContext(completeURL, method, headers, forwardBody, proxyPrefix,
						proxyBaseUrl, redirectedUrl, resourceURL, rewrite, isShowLog, uuid), response);
			}

			// 12. 儲存 Binding
			saveBindingIfNeeded(routingResult, diversionConfig, routeContext, response.getStatus());

		} catch (AsyncRequestNotUsableException e) {
			// 客戶端已斷線（HTTP/2 RST_STREAM、瀏覽器換頁/取消等），非伺服器錯誤
			TPILogger.tl.debug("SmartOnFhirProxyService: client disconnected: " + e.getMessage());
		} catch (Exception e) {
			TPILogger.tl.error("SmartOnFhirProxyService error: " + StackTraceUtil.logStackTrace(e));
			if (!response.isCommitted()) {
				try {
					response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
					response.setContentType(CONTENT_TYPE_JSON_UTF8);
					OAuthTokenErrorResp errResp = new OAuthTokenErrorResp();
					errResp.setError("server_error");
					errResp.setMessage("Internal server error");
					errResp.setPath(request.getRequestURI());
					errResp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
					errResp.setTimestamp(System.currentTimeMillis() + "");
					byte[] errBody = objectMapper.writeValueAsBytes(errResp);
					// checkmarx, Missing HSTS Header
					response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
					response.getOutputStream().write(errBody);
				} catch (Exception writeEx) {
					TPILogger.tl
							.debug("SmartOnFhirProxyService: failed to write error response: " + writeEx.getMessage());
				}
			}
		}
	}

	// ==================== 是否為 Bundle transaction 請求 ====================
	/**
	 * 判斷是否為 Bundle transaction 請求, <br>
	 * Bundle transaction 的特性是一次請求包含多個不同資源的操作，例如： <br>
	 * { <br>
	 * "resourceType": "Bundle", <br>
	 * "type": "transaction", <br>
	 * "entry": [ <br>
	 * { "request": { "method": "POST", "url": "Patient" } }, <br>
	 * { "request": { "method": "PUT", "url": "Observation/123" } }, <br>
	 * { "request": { "method": "DELETE","url": "Condition/456" } } <br>
	 * ] <br>
	 * } <br>
	 */
	public static boolean isBundleTransaction(String method, String resourceURL, String payload) {
		// 1. 必須是 POST
		if (!"POST".equalsIgnoreCase(method)) {
			return false;
		}

		// 2. URL 路徑判斷
		boolean isBundleTransactionPath = isBundleTransactionPath(resourceURL);
		if (!isBundleTransactionPath) {
			return false;
		}

		// 3. 解析 Body 確認是 Bundle Transaction
		return isBundleTransactionBody(payload);
	}

	/**
	 * 判斷 URL 路徑 是否為 Bundle transaction, <br>
	 * Bundle transaction 打的是 POST FHIR Base URL（不帶 resource type）, e.g. 僅接受 "/fhir"
	 * 或 /baseR4 (長度為 1),
	 */
	private static boolean isBundleTransactionPath(String path) {
		// 移除開頭 /，切割
		String[] segments = path.replaceFirst("^/", "").split("/");

		// 後面不能有其他 segment（或後面沒有東西）
		return segments.length == 1;
	}

	/**
	 * 是否為 Bundle transaction 內容 <br>
	 * 解析 body JSON, 是否為 resourceType=Bundle AND type=transaction
	 */
	private static boolean isBundleTransactionBody(String payload) {
		try {
			if (payload == null || payload.isEmpty())
				return false;

			JsonNode root = objectMapper.readTree(payload);

			// 確認 resourceType = "Bundle"
			JsonNode resourceType = root.get("resourceType");
			if (resourceType == null || !"Bundle".equals(resourceType.asText())) {
				return false;
			}

			// 確認 type = "transaction"
			JsonNode type = root.get("type");
			return type != null && "transaction".equals(type.asText());

		} catch (Exception e) {
			TPILogger.tl.debug(StackTraceUtil.logStackTrace(e));
			String errMsg = "[SMART on FHIR] Failed to parse Bundle Transaction body: " + e.getMessage();
			TPILogger.tl.debug(errMsg);
			return false;
		}
	}

	// ==================== Request Processing ====================

	/**
	 * 從 request 讀取 body bytes。 Tomcat 會自動消耗 form-urlencoded 的 InputStream，需從
	 * parameterMap 重建。
	 */
	private byte[] readRequestBody(HttpServletRequest request) throws IOException {
		byte[] requestBody = request.getInputStream().readAllBytes();
		if (requestBody.length == 0 && request.getContentType() != null
				&& request.getContentType().contains(CONTENT_TYPE_FORM_URLENCODED)) {
			StringBuilder sb = new StringBuilder();
			request.getParameterMap().forEach((key, values) -> {
				for (String value : values) {
					if (sb.length() > 0)
						sb.append("&");
					sb.append(java.net.URLEncoder.encode(key, StandardCharsets.UTF_8));
					sb.append("=");
					sb.append(java.net.URLEncoder.encode(value, StandardCharsets.UTF_8));
				}
			});
			requestBody = sb.toString().getBytes(StandardCharsets.UTF_8);
		}
		return requestBody;
	}

	// ==================== 路由上下文載入 ====================

	/**
	 * 從 DB 載入 FHIR Proxy 的 RouteContext。
	 */
	private RouteContext loadRouteContext(String websiteName) {
		if (!StringUtils.hasText(websiteName)) {
			return null;
		}
		List<DgrSmartOnFhirProxy> proxyList = getProxyDao().findBySofProxyName(websiteName);
		if (proxyList == null || proxyList.isEmpty()) {
			return null;
		}
		DgrSmartOnFhirProxy proxy = proxyList.get(0);
		List<DgrSmartOnFhirProxyDiversion> diversionList = getDiversionDao().findBySofProxyId(proxy.getSofProxyId());
		return RouteContext.fromFhirProxy(proxy, diversionList);
	}

	// ==================== Proxy Base URL 解析 ====================

	/**
	 * 解析 proxy 的絕對 Base URL（支援 X-Forwarded-* header）。
	 *
	 * <p>
	 * 優先順序：
	 * <ol>
	 * <li>X-Forwarded-Proto / X-Forwarded-Host（反向代理注入）</li>
	 * <li>Host header + request.getScheme()（直連）</li>
	 * </ol>
	 *
	 * <p>
	 * 前提：DGR 前方一定有 Nginx，且 Nginx 使用 {@code proxy_set_header} 覆蓋模式注入
	 * X-Forwarded-*，client 無法偽造。
	 * </p>
	 */
	private String resolveProxyBaseUrl(HttpServletRequest request, String proxyPrefix) {
		// 1. Scheme：優先讀 X-Forwarded-Proto
		String scheme = request.getHeader(HEADER_X_FORWARDED_PROTO);
		if (scheme == null || scheme.isEmpty()) {
			scheme = request.getScheme();
		}
		if (scheme.contains(",")) {
			scheme = scheme.split(",")[0].trim();
		}

		// 2. Host：優先讀 X-Forwarded-Host，fallback 到 Host header
		String host = request.getHeader(HEADER_X_FORWARDED_HOST);
		if (host == null || host.isEmpty()) {
			host = request.getHeader(HEADER_HOST);
		}
		if (host == null || host.isEmpty()) {
			host = request.getServerName();
			int port = request.getServerPort();
			if (port != 80 && port != 443) {
				host += ":" + port;
			}
		}
		if (host.contains(",")) {
			host = host.split(",")[0].trim();
		}

		// 3. Prefix：如果反向代理剝了路徑前綴
		String prefix = proxyPrefix;
		String forwardedPrefix = request.getHeader(HEADER_X_FORWARDED_PREFIX);
		if (forwardedPrefix != null && !forwardedPrefix.isEmpty()) {
			if (forwardedPrefix.endsWith("/")) {
				forwardedPrefix = forwardedPrefix.substring(0, forwardedPrefix.length() - 1);
			}
			prefix = forwardedPrefix + prefix;
		}

		return scheme + "://" + host + prefix;
	}

	// ==================== Header 轉換 ====================

	/**
	 * 從 HttpServletRequest 提取 headers（排除快取相關 headers）。
	 */
	private Map<String, List<String>> convertHeaders(HttpServletRequest request, HttpHeaders httpHeaders) {
		Enumeration<String> headerKeys = request.getHeaderNames();
		Map<String, List<String>> headers = new HashMap<>();
		// 不轉發的 request headers：
		// - Accept-Encoding：避免上游回壓縮格式（brotli 等）proxy 無法處理
		// - If-Modified-Since / If-None-Match：避免 304 快取干擾

		while (headerKeys.hasMoreElements()) {
			String key = headerKeys.nextElement();
			List<String> headerValueList = httpHeaders.get(key);
			if (!HEADER_IF_MODIFIED_SINCE.equalsIgnoreCase(key) && !HEADER_IF_NONE_MATCH.equalsIgnoreCase(key)
					&& !HEADER_ACCEPT_ENCODING.equalsIgnoreCase(key)) {
				headers.put(key, headerValueList);
			}
		}
		return headers;
	}

	/**
	 * 注入 DGR proxy 的 X-Forwarded-* headers，讓上游 FHIR server 感知 DGR domain。
	 *
	 * <ul>
	 * <li>{@code X-Forwarded-Proto}：外部 scheme（https/http）</li>
	 * <li>{@code X-Forwarded-Host}：外部 domain（從 Nginx 注入或直連 Host）</li>
	 * <li>{@code X-Forwarded-Prefix}：DGR proxy 的路徑前綴（/smart-on-fhir/{name}）</li>
	 * <li>{@code Host}：上游 FHIR server 的 host（確保 virtual host routing 正確）</li>
	 * </ul>
	 */
	private void injectForwardedHeaders(Map<String, List<String>> headers, HttpServletRequest request,
			String proxyPrefix, String targetUrl) {
		// 1. Scheme：優先讀 Nginx 注入的 X-Forwarded-Proto，fallback 到 request.getScheme()
		String scheme = request.getHeader(HEADER_X_FORWARDED_PROTO);
		if (scheme == null || scheme.isEmpty()) {
			scheme = request.getScheme();
		}
		if (scheme.contains(",")) {
			scheme = scheme.split(",")[0].trim();
		}
		headers.put(HEADER_X_FORWARDED_PROTO, List.of(scheme));

		// 2. Host：優先讀 Nginx 注入的 X-Forwarded-Host，fallback 到 Host header
		String host = request.getHeader(HEADER_X_FORWARDED_HOST);
		if (host == null || host.isEmpty()) {
			host = request.getHeader(HEADER_HOST);
		}
		if (host == null || host.isEmpty()) {
			host = request.getServerName();
			int port = request.getServerPort();
			if (port != 80 && port != 443) {
				host += ":" + port;
			}
		}
		if (host.contains(",")) {
			host = host.split(",")[0].trim();
		}
		headers.put(HEADER_X_FORWARDED_HOST, List.of(host));

		// 3. Prefix：DGR 的 proxy 路徑前綴
		String prefix = proxyPrefix;
		String forwardedPrefix = request.getHeader(HEADER_X_FORWARDED_PREFIX);
		if (forwardedPrefix != null && !forwardedPrefix.isEmpty()) {
			if (forwardedPrefix.endsWith("/")) {
				forwardedPrefix = forwardedPrefix.substring(0, forwardedPrefix.length() - 1);
			}
			prefix = forwardedPrefix + prefix;
		}
		headers.put(HEADER_X_FORWARDED_PREFIX, List.of(prefix));

		// 4. Host header：改為上游的 host，確保 virtual host routing 正確
		try {
			java.net.URI targetUri = java.net.URI.create(targetUrl);
			String targetHost = targetUri.getHost();
			if (targetUri.getPort() > 0) {
				targetHost += ":" + targetUri.getPort();
			}
			headers.put(HEADER_HOST, List.of(targetHost));
		} catch (Exception e) {
			TPILogger.tl.debug("injectForwardedHeaders: failed to parse targetUrl: " + targetUrl);
		}
	}

	/**
	 * 複製 response headers 到 HttpServletResponse，並改寫 URL 類 headers。
	 */
	private void copyResponseHeaders(Map<String, List<String>> respHeaders, int statusCode,
			HttpServletResponse response, String proxyPrefix, String proxyBaseUrl, String targetUrl, boolean rewrite) {
		if (rewrite) {
			rewriteLocationHeaders(respHeaders, proxyPrefix, proxyBaseUrl, targetUrl);
		}
		response.setStatus(statusCode);
		response.setHeader(HSTS_HEADER_NAME, HSTS_HEADER_VALUE);
		respHeaders.forEach((k, vs) -> {
			if (k != null && !k.startsWith(":") && !k.equalsIgnoreCase(HEADER_TRANSFER_ENCODING)
					&& !k.toLowerCase().startsWith("access-control-")) {
				vs.forEach(v -> response.addHeader(k, v));
			}
		});
	}

	/**
	 * 改寫 Location / Content-Location / Link 等 URL 類 response headers。
	 *
	 * <p>
	 * 將後端絕對 URL 替換為 proxy 絕對 URL， root-relative 路徑則加上 proxy 前綴。
	 * </p>
	 */
	private void rewriteLocationHeaders(Map<String, List<String>> respHeaders, String proxyPrefix, String proxyBaseUrl,
			String targetUrl) {
		if (targetUrl == null || proxyBaseUrl == null) {
			return;
		}
		String target = targetUrl.endsWith("/") ? targetUrl.substring(0, targetUrl.length() - 1) : targetUrl;
		Set<String> urlHeaders = Set.of("location", "content-location", "link");

		for (Map.Entry<String, List<String>> entry : respHeaders.entrySet()) {
			String key = entry.getKey();
			if (key == null || !urlHeaders.contains(key.toLowerCase())) {
				continue;
			}
			List<String> newValues = new ArrayList<>();
			for (String v : entry.getValue()) {
				if (v == null) {
					newValues.add(v);
					continue;
				}
				String result = v;
				// 絕對 URL：https://hapi.fhir.tw/... →
				// https://localhost:18080/smart-on-fhir/fhir/...
				result = result.replace(target, proxyBaseUrl);
				if (target.startsWith("https://")) {
					result = result.replace("http://" + target.substring(8), proxyBaseUrl);
				} else if (target.startsWith("http://")) {
					result = result.replace("https://" + target.substring(7), proxyBaseUrl);
				}
				// root-relative：/fhir/... → /smart-on-fhir/fhir/fhir/...
				if (result.equals(v) && result.startsWith("/") && !result.startsWith("//")
						&& !result.startsWith(proxyPrefix)) {
					result = proxyPrefix + result;
				}
				newValues.add(result);
			}
			entry.setValue(newValues);
		}
	}

	// ==================== 安全檢查 ====================

	/**
	 * 執行所有安全檢查（含 ignorePath 判斷、SQLi/XSS/XXE）。
	 *
	 * @return 若檢查失敗，回傳 error ResponseEntity；通過則回傳 null
	 */
	private ResponseEntity<?> runAllSecurityChecks(String resourceURL, String payload, String queryString,
			RouteContext routeContext, String requestUri) {

		if (isIgnorePath(resourceURL, routeContext.ignoreApiPaths())) {
			return null;
		}

		// SQL Injection / XSS / XXE
		String checkValue = (queryString != null ? queryString : "") + payload;
		return runSecurityChecks(checkValue, routeContext, requestUri);
	}

	private ResponseEntity<?> runSecurityChecks(String checkValue, RouteContext routeContext, String requestUri) {

		if (routeContext.securityConfig().enableSqlInjectionCheck()) {
			if (getSqlInjectionCheck().check(checkValue, false)) {
				return errorResponse(ERROR_SQL_INJECTION, ERROR_INVALID_PARAM, requestUri, HttpStatus.BAD_REQUEST);
			}
		}

		if (routeContext.securityConfig().enableXssCheck()) {
			if (getXssCheck().check(checkValue, false)) {
				return errorResponse(ERROR_XSS, ERROR_INVALID_PARAM, requestUri, HttpStatus.BAD_REQUEST);
			}
		}

		if (routeContext.securityConfig().enableXxeCheck()) {
			if (getXxeCheck().check(checkValue, false)) {
				return errorResponse(ERROR_XXE, ERROR_INVALID_PARAM, requestUri, HttpStatus.BAD_REQUEST);
			}
		}

		return null;
	}

	// ==================== 流量控制 ====================

	/**
	 * 執行緒安全的 TPS 計數器。 每個 (routeName, targetUrl) 對應一個實例。
	 */
	private static class TrafficCounter {
		private long timestampSec;
		private int frequency;

		TrafficCounter(long timestampSec) {
			this.timestampSec = timestampSec;
			this.frequency = 1;
		}

		/**
		 * 記錄一次存取並回傳當前秒的累積次數。 synchronized 保證 timestamp 檢查 + frequency 更新的原子性。
		 */
		synchronized int recordAccess(long currentTimestampSec) {
			if (this.timestampSec == currentTimestampSec) {
				this.frequency++;
			} else {
				this.timestampSec = currentTimestampSec;
				this.frequency = 1;
			}
			return this.frequency;
		}
	}

	private ResponseEntity<?> checkTraffic(RouteContext routeContext, String redirectedUrl) {
		if (!routeContext.trafficConfig().enableTrafficControl() || routeContext.trafficConfig().tps() <= 0) {
			return null;
		}

		long timestampSec = System.currentTimeMillis() / 1000;

		// computeIfAbsent 保證 outer/inner map 的原子建立
		ConcurrentHashMap<String, TrafficCounter> counters = trafficMap.computeIfAbsent(routeContext.routeName(),
				k -> new ConcurrentHashMap<>());
		TrafficCounter counter = counters.computeIfAbsent(redirectedUrl, k -> new TrafficCounter(timestampSec));

		// recordAccess 內部 synchronized，保證 read-modify-write 原子性
		int freq = counter.recordAccess(timestampSec);

		if (freq > routeContext.trafficConfig().tps()) {
			return errorResponse(ERROR_TPS, ERROR_TPS_EXCEEDED, redirectedUrl, HttpStatus.TOO_MANY_REQUESTS);
		}
		return null;
	}

	// ==================== HTTP 轉發 ====================

	/**
	 * HTTP 轉發所需的上下文資訊。
	 */
	private record ForwardContext(String completeURL, String method, Map<String, List<String>> headers,
			byte[] forwardBody, String proxyPrefix, String proxyBaseUrl, String redirectedUrl, String resourceURL,
			boolean rewrite, boolean isShowLog, String uuid) {

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof ForwardContext that))
				return false;
			return rewrite == that.rewrite && isShowLog == that.isShowLog
					&& java.util.Objects.equals(completeURL, that.completeURL)
					&& java.util.Objects.equals(method, that.method) && java.util.Objects.equals(headers, that.headers)
					&& Arrays.equals(forwardBody, that.forwardBody)
					&& java.util.Objects.equals(proxyPrefix, that.proxyPrefix)
					&& java.util.Objects.equals(proxyBaseUrl, that.proxyBaseUrl)
					&& java.util.Objects.equals(redirectedUrl, that.redirectedUrl)
					&& java.util.Objects.equals(resourceURL, that.resourceURL)
					&& java.util.Objects.equals(uuid, that.uuid);
		}

		@Override
		public int hashCode() {
			int result = java.util.Objects.hash(completeURL, method, headers, proxyPrefix, proxyBaseUrl, redirectedUrl,
					resourceURL, rewrite, isShowLog, uuid);
			result = 31 * result + Arrays.hashCode(forwardBody);
			return result;
		}

		@Override
		public String toString() {
			return "ForwardContext[completeURL=" + completeURL + ", method=" + method + ", headers=" + headers
					+ ", forwardBody=" + Arrays.toString(forwardBody) + ", proxyPrefix=" + proxyPrefix
					+ ", proxyBaseUrl=" + proxyBaseUrl + ", redirectedUrl=" + redirectedUrl + ", resourceURL="
					+ resourceURL + ", rewrite=" + rewrite + ", isShowLog=" + isShowLog + ", uuid=" + uuid + "]";
		}
	}

	/**
	 * 執行 HTTP 轉發並將 response 寫入 HttpServletResponse。
	 */
	private void forwardAndWriteResponse(ForwardContext ctx, HttpServletResponse response) throws Exception {

		try (SmartOnFhirProxyResponse proxyResp = httpClient.forward(ctx.completeURL(), ctx.method(), ctx.headers(),
				ctx.forwardBody())) {

			if (ctx.rewrite() && (SmartOnFhirProxyResponseRewriter.shouldRewrite(proxyResp.headers())
					|| SmartOnFhirProxyResponseRewriter.isTextContentType(proxyResp.headers()))) {
				// 文字回應（HTML/CSS/JSON/XML）：緩衝 + 改寫
				byte[] rawBody = proxyResp.body().readAllBytes();
				byte[] rewritten = SmartOnFhirProxyResponseRewriter.rewriteIfNeeded(rawBody, ctx.proxyPrefix(),
						ctx.redirectedUrl(), ctx.proxyBaseUrl(), proxyResp.headers(), ctx.resourceURL());

				copyResponseHeaders(proxyResp.headers(), proxyResp.statusCode(), response, ctx.proxyPrefix(),
						ctx.proxyBaseUrl(), ctx.redirectedUrl(), ctx.rewrite());
				writeBody(response, rewritten);
			} else {
				// 二進位或 rewrite=false：直接串流
				copyResponseHeaders(proxyResp.headers(), proxyResp.statusCode(), response, ctx.proxyPrefix(),
						ctx.proxyBaseUrl(), ctx.redirectedUrl(), ctx.rewrite());
				streamBody(response, proxyResp.body());
			}

			logEnd(ctx.isShowLog(), ctx.uuid(), LOG_END_SUFFIX,
					"\n\tstatus=" + proxyResp.statusCode() + ", url=" + ctx.completeURL());
		}
	}

	// ==================== 預先發現回應處理 ====================

	/**
	 * 處理自動發現已獲取的回應（PreDiscoveredResponse）。
	 */
	private void writePreDiscoveredResponse(RoutingResult routingResult, String proxyName, String proxyBaseUrl,
			String resourceURL, HttpServletResponse response, boolean rewrite, boolean isShowLog, String uuid)
			throws Exception {

		var preResp = routingResult.preDiscoveredResponse();
		String proxyPrefix = PROXY_PREFIX_TEMPLATE + proxyName;
		String targetUrl = routingResult.diversionConfig() != null ? routingResult.diversionConfig().targetUrl() : null;
		copyResponseHeaders(preResp.headers(), preResp.statusCode(), response, proxyPrefix, proxyBaseUrl, targetUrl,
				rewrite);

		if (preResp.body() != null && preResp.body().length > 0) {
			if (rewrite) {
				byte[] rewritten = SmartOnFhirProxyResponseRewriter.rewriteIfNeeded(preResp.body(), proxyPrefix,
						targetUrl, proxyBaseUrl, preResp.headers(), resourceURL);
				writeBody(response, rewritten);
			} else {
				writeBody(response, preResp.body());
			}
		}

		logEnd(isShowLog, uuid, LOG_END_PRE_DISCOVERED, "\n\tstatus=" + preResp.statusCode());
	}

	// ==================== Binding 儲存 ====================

	/**
	 * 若路由結果需要儲存 binding 且 response 成功，執行儲存。
	 */
	private void saveBindingIfNeeded(RoutingResult routingResult, DiversionConfig diversionConfig,
			RouteContext routeContext, int responseStatus) {
		if (routingResult.needSaveBinding() && routingResult.resourceType() != null && responseStatus >= HTTP_OK_MIN
				&& responseStatus < HTTP_OK_MAX) {
			routingService.saveBinding(routeContext.routeId(), routingResult.resourceType(),
					diversionConfig.diversionId());
		}
	}

	// ==================== Response 寫入 ====================

	/**
	 * 將 byte[] body 寫入 response（已緩衝的 body）。
	 */
	private void writeBody(HttpServletResponse response, byte[] body) throws IOException {
		response.setContentLength(body.length);
		// checkmarx, Missing HSTS Header
		response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
		response.getOutputStream().write(body);
	}

	/**
	 * 將 InputStream 串流寫入 response（二進位直通）。
	 */
	private void streamBody(HttpServletResponse response, InputStream body) throws IOException {
		body.transferTo(response.getOutputStream());
	}

	// ==================== 路徑忽略判斷 ====================

	private boolean isIgnorePath(String resourceUrl, String ignoreApiPath) {
		if (StringUtils.hasText(ignoreApiPath)) {
			String[] paths = ignoreApiPath.split(",");
			for (String path : paths) {
				if (path.contains("**")) {
					String pattern = path.replaceAll("\\*\\*", ".*");
					if (CheckmarxCommUtils.sanitizeForCheckmarxMatches(resourceUrl, pattern)) {
						return true;
					}
				} else if (path.equals(resourceUrl)) {
					return true;
				}
			}
		}
		return false;
	}

	// ==================== Base URL 解析 ====================

	// ==================== 錯誤回應 ====================

	private ResponseEntity<OAuthTokenErrorResp> errorResponse(String error, String message, String path,
			HttpStatus status) {
		OAuthTokenErrorResp resp = new OAuthTokenErrorResp();
		resp.setError(error);
		resp.setMessage(message);
		resp.setPath(path);
		resp.setStatus(status.value());
		resp.setTimestamp(System.currentTimeMillis() + "");
		return new ResponseEntity<>(resp, null, status);
	}

	private void writeError(HttpServletResponse response, HttpStatus status, String error, String message, String path,
			boolean isShowLog, String uuid) throws Exception {
		writeErrorResponse(response, errorResponse(error, message, path, status), isShowLog, uuid);
	}

	private void writeErrorResponse(HttpServletResponse response, ResponseEntity<?> respEntity, boolean isShowLog,
			String uuid) throws Exception {
		int status = respEntity.getStatusCode().value();
		String errMsg = objectMapper.writeValueAsString(respEntity.getBody());

		logEnd(isShowLog, uuid, LOG_END_ERROR, "\n\tstatus=" + status + ", body=" + errMsg);

		response.setStatus(status);
		response.setContentType(CONTENT_TYPE_JSON_UTF8);
		writeBody(response, errMsg.getBytes(StandardCharsets.UTF_8));
	}

	// ==================== Log ====================

	/**
	 * 統一的日誌結尾輸出。
	 */
	private void logEnd(boolean isShowLog, String uuid, String suffix, String detail) {
		if (isShowLog) {
			TPILogger.tl.debug(LOG_PREFIX + uuid + suffix + detail);
		}
	}

	// ==================== Protected getters (for test override)
	// ====================

	protected DgrSmartOnFhirProxyDao getProxyDao() {
		return proxyDao;
	}

	protected DgrSmartOnFhirProxyDiversionDao getDiversionDao() {
		return diversionDao;
	}

	protected SqlInjectionCheck getSqlInjectionCheck() {
		return sqlInjectionCheck;
	}

	protected XssCheck getXssCheck() {
		return xssCheck;
	}

	protected XxeCheck getXxeCheck() {
		return xxeCheck;
	}

	private boolean isLauncherAdminBypass(HttpServletRequest request) {
		String launcherHeader = request.getHeader("X-Smart-Launcher");
		if (!"true".equals(launcherHeader)) {
			return false;
		}

		String authHeader = request.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return false;
		}

		String jwt = authHeader.substring(7);
		try {
			TokenHelper.JwtPayloadData data = tokenHelper.getJwtPayloadData(jwt);
			if (data.errRespEntity != null || data.payloadJsonNode == null) {
				return false;
			}
			if (!data.payloadJsonNode.has("user_name")) {
				return false;
			}
			long exp = data.payloadJsonNode.path("exp").asLong(0);
			if (exp > 0 && exp < System.currentTimeMillis() / 1000) {
				return false;
			}
			return true;
		} catch (Exception e) {
			TPILogger.tl.debug("Launcher admin bypass failed: " + e.getMessage());
			return false;
		}
	}

	protected TokenHelper getTokenHelper() {
		return tokenHelper;
	}
}
