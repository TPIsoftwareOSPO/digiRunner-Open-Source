package tpi.dgrv4.gateway.filter;

import java.io.IOException;
import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.RealtimeDashboardService;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.entity.TsmpApiReg;
import tpi.dgrv4.entity.entity.TsmpApiRegId;
import tpi.dgrv4.entity.exceptions.DgrException;
import tpi.dgrv4.entity.exceptions.ICheck;
import tpi.dgrv4.gateway.component.DgrcRoutingHelper;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpApiCacheProxy;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpApiRegCacheProxy;
import tpi.dgrv4.gateway.component.check.ApiNotFoundCheck;
import tpi.dgrv4.gateway.component.check.ApiStatusCheck;
import tpi.dgrv4.gateway.component.check.BotDetectionCheck;
import tpi.dgrv4.gateway.component.check.CusTokenCheck;
import tpi.dgrv4.gateway.component.check.DgrJtiCheck;
import tpi.dgrv4.gateway.component.check.HostHeaderCheck;
import tpi.dgrv4.gateway.component.check.IgnoreApiPathCheck;
import tpi.dgrv4.gateway.component.check.ModeCheck;
import tpi.dgrv4.gateway.component.check.SqlInjectionCheck;
import tpi.dgrv4.gateway.component.check.TokenCheck;
import tpi.dgrv4.gateway.component.check.TrafficCheck;
import tpi.dgrv4.gateway.component.check.XssCheck;
import tpi.dgrv4.gateway.component.check.XxeCheck;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.CommForwardProcService;
import tpi.dgrv4.gateway.service.TsmpSettingService;
import tpi.dgrv4.gateway.service.WebsiteService;
import tpi.dgrv4.gateway.util.ControllerUtil;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp2;
import tpi.dgrv4.gateway.vo.ResHeader;
import tpi.dgrv4.gateway.vo.TsmpApiLogReq;

@Component
public class GatewayFilter extends OncePerRequestFilter {

	// [ZH]高併發網路流量快速通關分派裝置(記憶單元)
	// [EN]High-concurrency network traffic fast-pass dispatch device (memory unit)
	private static final Map<String, Map<String, Long>> apiRespTimeMap = new ConcurrentHashMap<>();

	private static final Object apiReqThroughputObjLock = new Object();
	private static final Object apiRespThroughputObjLock = new Object();
	private static final LinkedHashMap<Long, Long> apiReqThroughput = new LinkedHashMap<>() {
		@Serial
		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Map.Entry<Long, Long> eldest) {
			// [ZH]限制HashMap大小為10個
			// [EN]Limit HashMap size to 10 entries
			return size() > 10;
		}
	};
	private static final LinkedHashMap<Long, Long> apiRespThroughput = new LinkedHashMap<>() {
		@Serial
		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Map.Entry<Long, Long> eldest) {
			// [ZH]限制HashMap大小為10個
			// [EN]Limit HashMap size to 10 entries
			return size() > 10;
		}
	};

	public static Long getReqTps(long key) {
		return GatewayFilter.apiReqThroughput.getOrDefault(key, 0L);
	}

	public static Long getRespTps(long key) {
		return GatewayFilter.apiRespThroughput.getOrDefault(key, 0L);
	}

	private static final long TSMPC_ROUTING = 0;
	private static final long DGRC_ROUTING = 1;
	private static final long TSMP_DGRCROUTING = 2;
	private static final String GCP_ROUTING = "/kibana";

	public static final String API_ID = "apiId";
	public static final String MODULE_NAME = "moduleName";
	
	// [ZH]高併發網路流量快速通關分派裝置(控制旗標)	
	// [EN]High concurrent network traffic fast clearance dispatch device (control flag)
	public static final String START_TIME = "startTime";

	public static final String ELAPSED_TIME = "elapsedTime";
	public static final String HTTP_CODE = "httpCode";
	public static final String ELAPSED_TIME23 = "elapsedTime23";
	public static final String HTTP_CODE23 = "httpCode23";
	public static final String SET_CIRCUIT_BREAKER = "setCircuitBreaker";
	public static final long ELAPSED_TIME_DEF_VAL = 999999L; // [ZH]耗時的預設值 // [EN]Preset time consumption
	public static final int HTTP_CODE_DEF_VAL = -1; // [ZH]HTTP code 的預設值 // [EN]Default value of HTTP code

	@Setter(onMethod_ = @Autowired)
	private TPILogger logger;
	@Setter(onMethod_ = @Autowired)
	private ApiStatusCheck apiStatusCheck;
	@Setter(onMethod_ = @Autowired)
	private IgnoreApiPathCheck ignoreApiPathCheck;
	@Setter(onMethod_ = @Autowired)
	private SqlInjectionCheck sqlInjectionCheck;
	@Setter(onMethod_ = @Autowired)
	private TrafficCheck trafficCheck;
	@Setter(onMethod_ = @Autowired)
	private XssCheck xssCheck;
	@Setter(onMethod_ = @Autowired)
	private XxeCheck xxeCheck;
	@Setter(onMethod_ = @Autowired)
	private TokenCheck tokenCheck;
	@Setter(onMethod_ = @Autowired)
	private ApiNotFoundCheck apiNotFoundCheck;
	@Setter(onMethod_ = @Autowired)
	private TsmpApiCacheProxy tsmpApiCacheProxy;
	@Setter(onMethod_ = @Autowired)
	private DgrcRoutingHelper dgrcRoutingHelper;
	@Setter(onMethod_ = @Autowired)
	private TsmpApiRegCacheProxy tsmpApiRegCacheProxy;
	@Setter(onMethod_ = @Autowired)
	private TsmpSettingService tsmpSettingService;
	@Setter(onMethod_ = @Autowired)
	private CommForwardProcService commForwardProcService;
	@Setter(onMethod_ = @Autowired)
	private WebsiteService websiteService;
	@Setter(onMethod_ = @Autowired)
	private HostHeaderCheck hostHeaderCheck;
	@Setter(onMethod_ = @Autowired)
	private DgrJtiCheck dgrJtiCheck;
	@Setter(onMethod_ = @Autowired)
	private ModeCheck modeCheck;
	@Setter(onMethod_ = @Autowired)
	private CusTokenCheck cusTokenCheck;
	@Setter(onMethod_ = @Autowired)
	private BotDetectionCheck dotBotDetectionCheck;


	@Override
	protected void doFilterInternal(@Nonnull HttpServletRequest request,@Nonnull HttpServletResponse response,@Nonnull FilterChain filterChain)
			throws ServletException, IOException {
		
		TsmpApiReg tsmpApiReg = null;
		String entry = ""; // uri 的第一個 path, 與 String uri 為同一組
		try {
			request = new CusContentCachingRequestWrapper(request);
			// [ZH]為了可以強制更改DB帳號密碼所以保留了傳參數的方式，以防當API取不到正確的值
			// [EN]uri 只會異動為 "dgrc"、"tsmpc"
			String uri = request.getRequestURI();
			String QString = request.getQueryString();
			String remoteAdd = request.getRemoteAddr();
			String remoteHost = request.getRemoteHost();
			String method01 = request.getMethod();

			if (getTsmpSettingService().getVal_REQUEST_URI_ENABLED()) {
				String debugMsg = "\n"
						+ "...request.getRequestURI():[" + method01 + "] (" + uri + ")\n"
						+ "...request.getQueryString():(" + QString + ")\n"
						+ "...request.getRemoteAddr():(" + remoteAdd + ")\n"
						+ "...request.getRemoteHost():(" + remoteHost + ")\n"
						+ "\n";

				TPILogger.tl.debug(debugMsg);
			}



			// [ZH]若 Host Header 不符合安全檢查就 true, 否則 false
			// [EN]true if the Host Header does not meet the security check, false otherwise
			if (hostHeaderCheck.check(request)) {
				int status = HttpServletResponse.SC_UNAUTHORIZED;// 401
				throw new DgrException(status, hostHeaderCheck);
			}

//			// uri 只會異動為 "dgrc"、"tsmpc" 
//			String uri = request.getRequestURI();

			// 2023/12/15,修改檢查器的回傳格式
			ResponseEntity<?> errRespEntity = dgrJtiCheck.check(uri, request);
			if (errRespEntity != null) {
				responseFlush(response, errRespEntity);
				return;
			}

			boolean isSpecialApi = isSpecialApi(uri); // 是否為 dgrv4 特權 API

			if (!isSpecialApi) {
				// [ZH]digiRunner.gtw.mode 檢查器, ex: onlyAC
				// [EN]digiRunner.gtw.mode checker, ex: onlyAC
				if (!modeCheck.check(uri)) {
					logger.debug("Request uri = " + uri);
					int status = 403;
					throw new DgrException(status, modeCheck);
				}
			}

			// [ZH]客製包打主包之判斷方法
			// [EN]How to determine whether a custom package is included in the main package
			if (cusTokenCheck.check(request)) {
				int status = 403;
				throw new DgrException(status, cusTokenCheck);
			}

			// [ZH]取出 "路由" 模式, ex: tsmc、dgrc、mixed
			// [EN]Remove "routing" mode, ex: tsmc, dgrc, mixed
			int compatibilityPaths = tsmpSettingService.getVal_DGR_PATHS_COMPATIBILITY();
			String kibanaPrefix = getTsmpSettingService().getVal_KIBANA_REPORTURL_PREFIX();
			// [ZH]判斷不包含特定 path 的路由
			// [EN]Determines routes that do not contain a specific path

			var dgrManagedRoute = Set.of(
					"/11/",
					"/17/",
					"/dgrc/",
					"/tsmpc/",
					"/tsmpg/",// [ZH]因為v3的noAuth要用tsmpg // [EN]Because v3's noAuth needs to use tsmpg
					"/dgrv4/",
					"/oauth/",
					"/mocktest/",
					"/kibana/",
					"/composer/swagger3.0/",
					"/website/",
					"/http-api/",
					"/_plugin/kibana/",
					"/_dashboards/",// [ZH]AWS上的OpenSearch的URL路徑 // [EN]URL path for OpenSearch on AWS
					"/dgrliveness", // [ZH]國壽要求一個 非/dgrv4的探針API // [EN]China Life requires a non-dgrv4 probe API
					"/liveness", // [ZH]國壽要求一個 非/dgrv4的探針API // [EN]China Life requires a non-dgrv4 probe API
					"/readiness" // 國壽要求一個 非/dgrv4的探針API // [EN]China Life requires a non-dgrv4 probe API
			);

			// [ZH]true dgr 系統管控路由
			// [EN]true dgr system routing
			boolean isDgrManagedRoute = dgrManagedRoute.stream()
					.anyMatch(uri::startsWith)
					|| isGCPkibana(request, kibanaPrefix) // [ZH]判斷是不是走/kibana2的GCP 相關URL // [EN]Determine whether to use the GCP related URL of /kibana2
					;

			// [ZH] 如果不是 dgr 系統管控路由，則添加 path
			// [EN] add path if not dgr system routing
			if (!isDgrManagedRoute) {
				if (compatibilityPaths == TSMPC_ROUTING) {
					uri = addTsmpcToUri(uri);
					entry = "tsmpc";
				}

				if (compatibilityPaths == DGRC_ROUTING) {
					uri = addDgrcToUri(uri);
					entry = "dgrc";
				}

				// [ZH]混合 路由模式
				// [EN]Mixed routing mode
				if (compatibilityPaths == TSMP_DGRCROUTING) {
					if (uri.split("/").length == 3 && uri.startsWith("/tsmpc/")) {
						// [ZH]判定它是 tsmpc
						// [EN]Determine if it is tsmpc
						entry = "tsmpc";
					} else {
						uri = addDgrcToUri(uri);
						entry = "dgrc";
					}
				}
			}

			// [ZH]dgrc only專用
			// 這是例外狀況。用來判斷以下兩個網址是不是composer，若是composer就走tsmpc，不是就走dgrc。
			// [EN]dgrc only
			// This is an exceptional condition. Used to determine whether the following two URLs are composer. If they are composer, use tsmpc, otherwise use dgrc.
			// 1.http://ip:port/a/b
			// 2.http://ip:port/tsmpc/a/b
			boolean isComposerAPI = false;
			if (compatibilityPaths == DGRC_ROUTING && uri.split("/").length == 4) {
				isComposerAPI = isComposerAPI(uri);
				if (isComposerAPI) {
					String keyword = "dgrc/";
					int flag = uri.indexOf(keyword);
					if (flag != -1) {
						uri = uri.replaceFirst("dgrc", "tsmpc"); // dgrc 變為 tsmpc // dgrc becomes tsmpc
					}
					entry = "tsmpc";
				}
			}

			// [ZH]針對URL中有tsmpc或是dgrc，進行檢查器檢查。
			// [EN]Perform a check if there is tsmpc or dgrc in the URL.
			if (isDGRCorTSMPC(uri)) {

				pathsSelector(request, uri, compatibilityPaths, isComposerAPI);
				Object apiId = request.getAttribute(GatewayFilter.API_ID);
				Object moduleName = request.getAttribute(GatewayFilter.MODULE_NAME);
				if (apiId == null && moduleName == null ) {
					TPILogger.tl.warn("uri = " + uri + ", moduleName=null and apiID=null");
					throw new DgrException(404, apiNotFoundCheck);
				} else {
					TsmpApiRegId tsmpApiRegId = new TsmpApiRegId(apiId + "", moduleName + "");
					tsmpApiReg = getTsmpApiRegCacheProxy().findById(tsmpApiRegId).orElse(null);
					
					String path = null;
					if (uri.startsWith("/dgrc")) {
						path = uri.substring(5);
						entry = "dgrc";
					}
					if (uri.startsWith("/tsmpc") || uri.startsWith("/tsmpg") // [ZH]因為v3的noAuth要用tsmpg // [EN]Because v3's noAuth needs to use tsmpg
					) {
						path = uri.substring(6);
						entry = "tsmpc";
					}
					if (!ignoreApiPathCheck.check(path)) {
						// 20231129修改五大檢查器的回傳格式
						OAuthTokenErrorResp checkResp = checkList(response, request, uri);
						// [ZH]進行五種檢查器檢查
						// [EN]Perform five types of checkers
						if (checkResp != null) {
							ObjectMapper mapper = new ObjectMapper();
							Integer httpStatus = checkResp.getStatus();
							String errorRespStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(checkResp);
							// 將內容輸出
							responseFlush(response, httpStatus, errorRespStr);
							return;
						}
					}
				} // [ZH]tsmpc , dgrc 5大檢查器 if-end // [EN]tsmpc , dgrc 5 major checkers if-end
			}

			// [ZH]重新設定uri 轉發到 /tsmpc/** 或 /dgrc/**,
			// uri 只會異動為 "dgrc"、"tsmpc", 當然 uri 也可能沒有任何異動
			// The uri will only change to "dgrc" or "tsmpc", of course the uri may not
			// change at all
			// 高併發網路流量快速通關分派裝置: 計算完成的結果, 暫存在 request.setAttribute(k,v)
			// [EN]Reset uri to forward to /tsmpc/** or /dgrc/**,
			// The uri will only change to "dgrc" or "tsmpc", of course the uri may not change at all
			// High concurrent network traffic fast clearance dispatch device: The result of the calculation is temporarily stored in request.setAttribute(k,v)
			request = rewriteRequestURI(request, uri);

		} catch (DgrException e) {
			if (e.getHttpCode() == 0) {
				TPILogger.tl.error(StackTraceUtil.logTpiShortStackTrace(e));
			} else {
				TPILogger.tl.info(StackTraceUtil.logTpiShortStackTrace(e));
			}
			checkResponse(request, response, entry, getLocale(request), e.getHttpCode(), e.getiCheck());
			return;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		} catch (Throwable e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw e;
		}

		// TPILogger.tl.debug("\n--【LOGUUID】【" + "No" + "】【before end】--\n");

		// add Last Resp Header

		var responseWrapper = new FilteredHeaderResponseWrapper(response);

//		String cspVal = String.format(cspDefaultVal, dgrCspVal);
		// [ZH]加上 CSP 標題, 例如: 
		// "*" 或 "https://10.20.30.88:18442
		// [EN]Add the CSP header, For example: 
		// https://10.20.30.88:28442"
		String dgrCspVal = getTsmpSettingService().getVal_DGR_CSP_VAL();
		
		// [ZH]是否要加上 CSP 標題
		// [EN]Whether to add CSP header
		boolean isAddCsp = isAddCsp(request.getRequestURI());
		/*
		 * [ZH]Content-Security-Policy(CSP,內容安全策略)的值, 例如:
		 * "default-src https://10.20.30.88:18442 https://10.20.30.88:28442 'self' 'unsafe-inline'; img-src https://* 'self' data:;"
		 * [EN]Content-Security-Policy (CSP) value, for example:
		 * "default-src * 'self' 'unsafe-inline'; img-src https://* 'self' data:;"
		 */


		boolean isSse = false;

		// CORS 的值
		// [ZH]CORS 改從 Setting 取值或由 API 註冊取得
		// [EN]CORS changes to obtaining values ​​from Setting or registering with the API
		String corsAllowOriginVal = getCommForwardProcService().getCorsAllowOrigin(tsmpApiReg);
		String corsAllowMethodsVal = getCommForwardProcService().getCorsAllowMethods(tsmpApiReg);
		String corsAllowHeadersVal = getCommForwardProcService().getCorsAllowHeaders(tsmpApiReg);

		responseWrapper
			.injectHeader("Access-Control-Allow-Origin", corsAllowOriginVal) // CORS
			.injectHeader("Access-Control-Allow-Headers", corsAllowHeadersVal) // "2. corsAllowHeaders = " + corsAllowHeaders
			.injectHeader("Access-Control-Allow-Methods", corsAllowMethodsVal) // CORS
			.injectHeader("X-Frame-Options", "sameorigin")
			.injectHeader("X-Content-Type-Options", "nosniff")
			.injectHeader("Referrer-Policy", "strict-origin-when-cross-origin")
			.injectHeader("Cache-Control", "no-cache")
			.injectHeader("Pragma", "no-cache");
		
		//checkmarx, Missing HSTS Header
		responseWrapper.injectHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");

		if (StringUtils.hasText(dgrCspVal) && !"*".equals(dgrCspVal.trim())) {
			responseWrapper.injectHeader("Content-Security-Policy", dgrCspVal);
		}

		if (request.getHeader("isSse") != null) {
			responseWrapper.injectHeader("Content-Type", "text/event-stream");
		}
		
		// [ZH]將取得的 CORS 值, 放入 ServletContext 裡面供其他類取用,例如:AllowCorsFilterConfig
		// [EN]Put the obtained CORS value into ServletContext for other classes to use, for example: AllowCorsFilterConfig
		request.getServletContext().setAttribute("Access-Control-Allow-Origin", corsAllowOriginVal);
		request.getServletContext().setAttribute("Access-Control-Allow-Headers", corsAllowHeadersVal);
		request.getServletContext().setAttribute("Access-Control-Allow-Methods", corsAllowMethodsVal);

		// [ZH]將取得的 DGR_CSP_VAL 放入 ServletContext 裡面供其他類取用,例如:AllowCorsFilterConfig
		// [EN]Put the obtained DGR_CSP_VAL into ServletContext for other classes to use, for example: AllowCorsFilterConfig
		request.getServletContext().setAttribute("Content-Security-Policy", dgrCspVal);

		if (response.isCommitted()) {
			return;
		}

		try {
			filterChain.doFilter(request, response);
	    } catch (Exception e) {
	    	if (response.getStatus() == 200) {
	    		// [ZH]為了解決高併發時, 2個 worker thread 搶 filter 的問題
	    		// [EN]To solve the problem of two worker threads competing for filters during high concurrency
	    		// jakarta.servlet.ServletException: Request processing failed: java.lang.IllegalStateException: AsyncWebRequest must not be null
                String sb = "\nresponse.getStatus()::" + response.getStatus() +
                        "\nrequest.getRequestURI()::" + request.getRequestURI() +
                        "\nmsg::" + e.getLocalizedMessage();
	    		TPILogger.tl.warn(sb);
	    		return ;
	    	}
	    	TPILogger.tl.error("response.getStatus(): " + response.getStatus());
	    	TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
	    	
	    	// [ZH]重新拋出異常，確保錯誤能被正確處理
	    	// [EN]Rethrow the exception to ensure the error is handled correctly
	        throw e;  
	    }

	}
	
	/**
	 * Before <br>
	 * [ZH]高併發網路流量快速通關分派裝置(控制單元) <br>
	 * 記錄 Req 進入時的 startTime , URI , method <br>
	 * [EN]High concurrent network traffic fast clearance dispatch device (control unit) <br>
	 * Record the startTime, URI, and method when Req enters <br>
	 */
	private static void fetchUriHistoryBefore(HttpServletRequest request) {
		long startTime = System.currentTimeMillis();

		// 以此 Request 記錄它的 startTime
		request.setAttribute(GatewayFilter.START_TIME, startTime);

		// 以此 Request 記錄它的 URI Key: [GET] /aa/bb , 沒有含 QueryString
		String keyUri = getUriKeyString(request.getMethod(), request.getRequestURI());

		// 對特定 uri 的值進行計算並更新
		// 如果 keyUri 不存在，設為預設值
		// 如果 keyUri 存在，維持原來的值

		// 如果 apiRespTimeMap 的 key 的值不存在, 則建立一個新的 ConcurrentHashMap<String, Long> 並存入

		if (!apiRespTimeMap.containsKey(keyUri)) {
			var innerMap = new ConcurrentHashMap<String, Long>();
			innerMap.put(GatewayFilter.ELAPSED_TIME, GatewayFilter.ELAPSED_TIME_DEF_VAL);
			innerMap.put(GatewayFilter.HTTP_CODE, (long) GatewayFilter.HTTP_CODE_DEF_VAL);
			apiRespTimeMap.put(keyUri, innerMap);
		}

	}

	/**
	 * After <br>
	 * [ZH]高併發網路流量快速通關分派裝置(控制單元) <br>
	 * 記錄 Response 的時間 <br>
	 * 計算 elapsed time 並放在 (記憶單元) <br>
	 * [EN]High concurrent network traffic fast clearance dispatching device (control unit) <br>
	 * Record the time of Response <br>
	 * Calculate the elapsed time and store it in (memory cell) <br>
	 */
	public static void fetchUriHistoryAfter(HttpServletRequest request) {
		long endTime = Instant.now().toEpochMilli();

		// 以此 Request 記錄它的 URI Key: [GET] /aa/bb , 沒有含 QueryString
		long startTime = Optional.ofNullable(request.getAttribute(GatewayFilter.START_TIME))
                .map(obj -> (Long) obj)
                .orElse(endTime); // default

		// HTTP code
		int httpCode = Optional.ofNullable(request.getAttribute(GatewayFilter.HTTP_CODE))
                .map(obj -> (Integer) obj)
                .orElse(-1); // default
		
		// 2-3 log data
		int httpCode23 = Optional.ofNullable(request.getAttribute(GatewayFilter.HTTP_CODE23))
                .map(obj -> (Integer) obj)
                .orElse(-1); // default
		long elapsedTime23 = Optional.ofNullable(request.getAttribute(GatewayFilter.ELAPSED_TIME23))
				.map(obj -> (Long)obj)
				.orElse(ELAPSED_TIME_DEF_VAL);

		String method01 = request.getMethod();
		String afterUri = request.getRequestURI(); // 這裡被轉換過 '/dgrc' 了
		String srcUri = null;
		if (afterUri.toLowerCase().indexOf("/dgrc") == 0) {
			srcUri = afterUri.substring(afterUri.indexOf("/", 1)); // 去除第一個 path (/dgrc)
		} else {
			// tsmpc, tsmpg
			srcUri = afterUri;// It has not been converted so the original value is taken.
		}

		// 計算 '耗時'
		long elapsed = endTime - startTime;

		// 更新記錄的值
		String keyUri = getUriKeyString(method01, srcUri);
		Map<String, Long> innerMap = apiRespTimeMap.computeIfAbsent(keyUri, k -> new ConcurrentHashMap<>());
		innerMap.put(GatewayFilter.ELAPSED_TIME, elapsed);
		innerMap.put(GatewayFilter.HTTP_CODE, (long) httpCode);
		innerMap.put(GatewayFilter.ELAPSED_TIME23, elapsedTime23);   // 2-3 log data
		innerMap.put(GatewayFilter.HTTP_CODE23, (long) httpCode23); // 2-3 log data

		// realtime dashboard

		if (httpCode >= 400 || httpCode <= 0) {

			RealtimeDashboardService.realtimeDashobardApiMap.merge(
					RealtimeDashboardService.FAIL, 1L, Long::sum
			);

			String key = switch (httpCode) {
				case 401 -> RealtimeDashboardService.BAD_ATTEMPT_401;
				case 403 -> RealtimeDashboardService.BAD_ATTEMPT_403;
				default -> RealtimeDashboardService.BAD_ATTEMPT_OTHERS;
			};
			RealtimeDashboardService.realtimeDashobardApiMap.merge(
					key, 1L, Long::sum
			);

		} else {
			RealtimeDashboardService.realtimeDashobardApiMap.merge(
					RealtimeDashboardService.SUCCESS, 1L, Long::sum
			);
		}

	}

	/**
	 * Print list (Online Console 'URI Status')<br>
	 * [ZH]高併發網路流量快速通關分派裝置(輸出單元) <br>
	 * [EN]High concurrent network traffic fast clearance dispatching device (output unit) <br>
	 * [ZH]分隔線長度從 60 字元增加到 75 字元<br>
	 * [EN]Separator line length increased from 60 characters to 75 characters<br>
	 * [ZH]輸出格式調整: <br>
	 * [EN]Output format adjustment: <br>
	 * [ZH]URI保持原有的 40 字元 URL 寬度<br>
	 * [EN]URI maintains original 40-character URL width<br>
	 */
	public static String fetchUriHistoryList() {
	    StringBuilder sb = new StringBuilder();
		// [ZH]調整分隔線長度到 75 個字元
		// [EN]Adjust separator line length to 75 characters
	    sb.append(String.format("%75s", "").replace(' ', '-')).append("\n");

	    apiRespTimeMap.forEach((key, value) -> {
	        if (key.length() > 40) {
	            for (int i = 0; i < key.length(); i += 40) {
	                if (i + 40 < key.length()) {
	                    sb.append(key, i, i + 40).append("\n");
	                } else {
	                    sb.append(String.format("%-40s", key.substring(i)));
	                }
	            }
	        } else {
	            sb.append(String.format("%-40s", key));
	        }

	        // 第一組數據
	        // First group data
	        Long httpCode1 = value.get(GatewayFilter.HTTP_CODE);
	        long elapsedTime1 = value.get(GatewayFilter.ELAPSED_TIME);
	        String httpCodeVal1 = (httpCode1 == null) ? "" : httpCode1.toString();
	        
	        // 第二組數據（帶 23 後綴）
	        // Second group data (with 23 suffix)
	        Long httpCode2 = value.get(GatewayFilter.HTTP_CODE23);
	        Long elapsedTime2 = value.get(GatewayFilter.ELAPSED_TIME23);
	        String httpCodeVal2 = (httpCode2 == null) ? "" : httpCode2.toString();
	        long elapsedTimeVal2 = (elapsedTime2 == null) ? 0 : elapsedTime2;
	        
	        // 使用固定寬度格式化輸出，確保對齊
	        // Use fixed-width formatting for output to ensure alignment
	        sb.append(String.format(", %3s, %7dms  , %3s, %7dms", 
	            httpCodeVal1, elapsedTime1, httpCodeVal2, elapsedTimeVal2));
	        sb.append("\n");
	    });

	    return sb.toString();
	}

	/**
	 * [ZH]高併發網路流量快速通關分派裝置(運算單元)
	 * [EN]High-Concurrency Network Traffic Fast-Pass Dispatch Device (Computing Unit)
     */
	private static String getUriKeyString(String method01, String uri) {
        return "[" + method01 + "] (" + uri + ")";
	}

	/**
	 * 
	 * @param uri     可能包含有 /tsmpc/、/dgrc/、、、、等
	 */
	private HttpServletRequest rewriteRequestURI(HttpServletRequest request, String uri) {
		String kibanaPrefix = getTsmpSettingService().getVal_KIBANA_REPORTURL_PREFIX();



		if (isDGRCorTSMPC(uri)) {
			/*
			 * [ZH]dgrc uri 要再經過 '高併發網路流量快速通關分派裝置',
			 * 高併發網路流量快速通關分派裝置: 計算完成的結果, 暫存在 request.setAttribute(k,v)
			 * [EN]dgrc uri needs to go through 'High Concurrency Network Traffic Fast Pass
			 * Dispatch Device', High Concurrency Network Traffic Fast Pass Dispatch Device:
			 * completed calculation results are temporarily stored in
			 * request.setAttribute(k,v)
			 */
			fetchUriHistoryBefore(request);


			// [ZH]不熔斷, 進行 controller 轉發
			// [EN]No circuit break, proceed with controller forwarding
			request = GatewayFilter.changeRequestURI(request, uri);
		} else if (uri.contains("dgrv4")) {
			return request;
			// [ZH]要同時符合 1. 是要走 /kibana2 以及url 內沒有 /kibana2 的才加入 預防/kibana2/login 被二次加入/kibana2
			// [EN]Must simultaneously meet two conditions: 1. Should route to /kibana2 and URL doesn't contain /kibana2 - this prevents /kibana2/login from having /kibana2 added twice
		} else if (!uri.contains("login") && isGCPkibana(request, kibanaPrefix)  ) {
			// [ZH]導向 KibanaController.java "/kibana/**"
			// [EN]Route to KibanaController.java "/kibana/**"
			request = GatewayFilter.changeRequestURI(request, kibanaPrefix + uri);
		} else {
			// [ZH]沒有要變動 uri 就不用 new 物件
			// [EN]No need to create a new object if the URI doesn't need to be modified
			return request;
		}
		return request;
	}

	/**
	 * [ZH]開頭是 dgrc 或是 tsmpc 的 path
	 * [EN]paths that start with 'dgrc' or 'tsmpc'
	 */
	private boolean isDGRCorTSMPC(String uri) {
		boolean b1 = ((uri.startsWith("/dgrc/")));
		boolean b2 = ((uri.startsWith("/tsmpc/")));
		// [ZH]因為v3的noAuth要用tsmpg
		// [EN]Because v3's noAuth needs to use tsmpg
		boolean b3 = ((uri.startsWith("/tsmpg/")));
		return (b1 || b2 || b3);
	}

	private boolean isGCPkibana(HttpServletRequest request, String kibanaPrefix) {

		String uri = request.getRequestURI();
		// 先確認是不是路由2在決跑判斷
		if (GCP_ROUTING.equals(kibanaPrefix)) {

			// 因第一次轉導不能再進入前就加入kibana 所以寫死
			if (uri.contains("/app/dashboards")) {
				return true;
			}
			if (uri.contains("/kibana")) {
				return true;
			}
			// check req header "referer" =
			// "https://10.20.30.88:18442/kibana/app/dashboards"
			// "/kibana/app/dashboards"
			// "/kibana/app/discover"
			String referer = request.getHeader("Referer");
//		boolean hasReferer = false;
			if (StringUtils.hasLength(referer)) {
				// 報表管理和監控管理也列入
				if (referer.contains("/app/") || referer.contains("/ac09/") || referer.contains("/ac05/")) {
					return true;
				}
			}

			// "7.17.5" 應該會有 GCP 的值

			String kbn = "kbn";
			String Kbn = "Kbn";
			Enumeration<String> headers = request.getHeaderNames();
			while (headers.hasMoreElements()) {
				String key = headers.nextElement();
				if (key.contains(kbn) || key.contains(Kbn)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * [ZH]重新設定 uri, 轉發到 newUri
	 * [EN]reset uri, forward to newUri
	 */
	public static HttpServletRequest changeRequestURI(HttpServletRequest request, String newUri) {
		request = new HttpServletRequestWrapper((HttpServletRequest) request) {
			@Override
			public String getRequestURI() {
				return newUri;
			}
		};
		return request;
	}

	/**
	 * [ZH]將內容輸出
	 * [EN]output the content
	 */
	private void responseFlush(HttpServletResponse httpResponse, ResponseEntity<?> respEntity) throws Exception {
		Integer httpStatus = respEntity.getStatusCode().value();

		ObjectMapper mapper = new ObjectMapper();
		Object bodyObj = respEntity.getBody();

		String errorRespStr = switch (bodyObj) {
			case OAuthTokenErrorResp oAuthTokenErrorResp -> mapper.writerWithDefaultPrettyPrinter().writeValueAsString(oAuthTokenErrorResp);
			case OAuthTokenErrorResp2 oAuthTokenErrorResp2 -> mapper.writerWithDefaultPrettyPrinter().writeValueAsString(oAuthTokenErrorResp2);
			case null -> mapper.writerWithDefaultPrettyPrinter().writeValueAsString(Map.of("msg", "bodyObj is null"));
			default -> mapper.writerWithDefaultPrettyPrinter().writeValueAsString(bodyObj);
		};

		responseFlush(httpResponse, httpStatus, errorRespStr);
	}

	/**
	 * [ZH]將內容輸出
	 * [EN]output the content
	 */
	private void responseFlush(HttpServletResponse httpResponse, Integer httpStatus, String errorRespStr) {
		httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
		httpResponse.setCharacterEncoding(StandardCharsets.UTF_8.toString());
		httpResponse.setStatus(httpStatus);
		try {
			// checkmarx, Missing HSTS Header
			httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
			httpResponse.getWriter().write(errorRespStr);
			httpResponse.getWriter().flush();
			httpResponse.getWriter().close();
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
		}
	}

	/**
	 * [ZH]是否要加上 CSP 標頭, <br>
	 * 當 URL 的 path 不是 "/kibana" 和 "/_dashboards" 和 "/website" 開頭的, <br>
	 * 才加入 CSP 標頭 <br>
	 * [EN]Whether to add CSP header, <br>
	 * When the URL path does not start with "/kibana", "/_dashboards" or "/website", <br>
	 * before adding CSP header <br>
	 */
	private boolean isAddCsp(String reqURI) {
		boolean isKibana = reqURI.indexOf("/kibana") == 0;// 是否為 "/kibana" 開頭
		boolean isDashboards = reqURI.indexOf("/_dashboards") == 0;// 是否為 "/_dashboards" 開頭
		boolean isWebsite = reqURI.indexOf("/website") == 0;// 是否為 "/website" 開頭
		boolean isKibana2 = reqURI.indexOf("/kibana2") == 0;// 是否為 "/kibana" 開頭

        return !isKibana && !isDashboards && !isWebsite && !isKibana2;
    }

	private String addDgrcToUri(String uri) {
		return "/dgrc" + uri;
	}

	private String addTsmpcToUri(String uri) {
		return "/tsmpc" + uri;
	}

	private void pathsSelector(HttpServletRequest request, String reqUrl, int paths, boolean isComposerAPI) {

		if (paths == TSMPC_ROUTING) {
			setModuleNameAndApiIdToReq(request, reqUrl, false);
		}

		if (paths == DGRC_ROUTING) {
            setModuleNameAndApiIdToReq(request, reqUrl, !isComposerAPI);
		}
		
		boolean isTsmpc =   reqUrl.startsWith("/tsmpc/");
		if (!isTsmpc) {
			isTsmpc = reqUrl.startsWith("/tsmpg/");// 因為v3的noAuth要用tsmpg
		}
		if (paths == TSMP_DGRCROUTING) {
            setModuleNameAndApiIdToReq(request, reqUrl, !isTsmpc);
		}
	}

	/**
	 * [ZH]dgrc only專用 <br>
	 * 這是例外狀況。用來判斷是不是composer，若是composer就走tsmpc，不是就走dgrc。<br>
	 * [EN]dgrc only <br>
	 * This is an exceptional situation. Used to determine whether it is composer.
	 * If it is composer, use tsmpc, otherwise use dgrc. <br>
	 */
	private boolean isComposerAPI(String reqUrl) {

		String keyword = "tsmpc/";
		int flag = reqUrl.indexOf(keyword);
		if (flag == -1) {
			keyword = "dgrc/";
			flag = reqUrl.indexOf(keyword);
		}

		String urlStr = reqUrl.substring(flag + keyword.length());

		String[] urlArr = urlStr.split("/");
		String moduleName = urlArr[0];
		String apiKey = urlArr[1];

		TsmpApiId apiId = new TsmpApiId();
		apiId.setApiKey(apiKey);
		apiId.setModuleName(moduleName);
		Optional<TsmpApi> opt_tsmpApi = getTsmpApiCacheProxy().findById(apiId);

		if (opt_tsmpApi.isPresent()) {
			// 看TSMP_API的API_SRC是C ==> Composer
			// 看TSMP_API的API_SRC是R ==> 註冊
			TsmpApi tsmpApi = opt_tsmpApi.get();
            return "C".equals(tsmpApi.getApiSrc());
		}

		return false;
	}

	public String getStringBody(HttpServletRequest request) {

		byte[] byteBody = null;
		try {
			byteBody = StreamUtils.copyToByteArray(request.getInputStream());
		} catch (IOException e) {
			logger.error(StackTraceUtil.logStackTrace(e));
		}

		String stringBody = "";
		String method = request.getMethod();
		String contentType = request.getContentType() == null ? "" : request.getContentType().toLowerCase();

		if (HttpMethod.GET.matches(method)) {

			// 處理 QueryString
			stringBody = getQueryStringAndFormData(request);
		} else {

			// 處理 QueryString + RAW
			if (contentType.contains(MediaType.TEXT_PLAIN_VALUE.toLowerCase())
					|| contentType.contains(MediaType.APPLICATION_JSON_VALUE.toLowerCase())
					|| contentType.contains(MediaType.TEXT_HTML_VALUE.toLowerCase())
					|| contentType.contains(MediaType.APPLICATION_XML_VALUE.toLowerCase())) {
				if (byteBody != null && byteBody.length != 0) {
					stringBody = new String(byteBody);
				}

				if (StringUtils.hasText(stringBody)) {
					stringBody = stringBody + " " + getQueryStringAndFormData(request);
				} else {
					stringBody = getQueryStringAndFormData(request);
				}
			}

			// 處理 QueryString + FormData
			if (contentType.contains(MediaType.MULTIPART_FORM_DATA_VALUE.toLowerCase())
					|| contentType.contains(MediaType.APPLICATION_FORM_URLENCODED_VALUE.toLowerCase())) {
				stringBody = getQueryStringAndFormData(request);
			}

			// 若沒有content Type，預設當作text處理
			if (contentType.isEmpty()) {
				if (byteBody !=null && byteBody.length != 0) {
					stringBody = new String(byteBody);
				}

				if (StringUtils.hasText(stringBody)) {
					stringBody = stringBody + " " + getQueryStringAndFormData(request);
				} else {
					stringBody = getQueryStringAndFormData(request);
				}
			}
		}

		return stringBody;
	}

	private OAuthTokenErrorResp checkList(HttpServletResponse response, HttpServletRequest request, String uri) {
		String apiId = request.getAttribute(GatewayFilter.API_ID).toString();
		String moduleName = request.getAttribute(GatewayFilter.MODULE_NAME).toString();
		String body = getStringBody(request);
		// String locale = getLocale(request);


		// Bot-Detection 檢查器
		if (dotBotDetectionCheck.check(request)) {
			int status = HttpServletResponse.SC_FORBIDDEN;// 403
//			throw new DgrException(status, dotBotDetectionCheck);
			return getCheckErrorResp("Bot Detection", "Permission denied (User-Agent is not allowed.)",
					request.getRequestURI(), status);
		}

		// API開關
		// API狀態:啟用 / 停用(TSMP_API.api_status) 啟用為true,否則就false
		if (!apiStatusCheck.check(apiId, moduleName)) {
			return getCheckErrorResp("disabled", "API was disabled", request.getRequestURI(),
					HttpStatus.SERVICE_UNAVAILABLE.value());
		}

		// SQL Injection
		// 字符: '
		// 字符: ;
		// 有符合就 true, 否則就 false

		if (sqlInjectionCheck.check(body, true)) {
			return getCheckErrorResp("Sql Injection", "Invalid parameter", request.getRequestURI(),
					HttpStatus.BAD_REQUEST.value());
		}

		// XSS檢查器
		// <script
		// </script>
		// eval(
		// expression(
		// javascript:
		// vbscript:
		// onload=
		// src=
		// 有符合就 true, 否則就 false
		if (xssCheck.check(body, true)) {
			return getCheckErrorResp("xss", "Invalid parameter", request.getRequestURI(),
					HttpStatus.BAD_REQUEST.value());
		}

		// XXE檢查器
		// <!DOCTYPE
		// <\\!DOCTYPE
		// <!ENTITY
		// <\\!ENTITY
		// 有符合就 true, 否則就 false
		if (xxeCheck.check(body, true)) {
			return getCheckErrorResp("xxe", "Invalid parameter", request.getRequestURI(),
					HttpStatus.BAD_REQUEST.value());
		}

		// Traffic
		// 取得流量上限(TSMP_CLIENT.tps)超過上限就 true, 否則就 false
		TsmpApiRegId tsmpApiRegId = new TsmpApiRegId(apiId, moduleName);
		Optional<TsmpApiReg> tsmpApiReg = getTsmpApiRegCacheProxy().findById(tsmpApiRegId);
		if (tsmpApiReg.isPresent()) {
			var isNoAuth = tsmpApiReg.get().getNoOauth().equals("1");
			// 若有勾 no auth, 則不檢查
			if (!isNoAuth) {
				// 否則, 檢查
				String cID = getCID(request); // 取得 client ID
				if (StringUtils.hasText(cID) && trafficCheck.check(cID)) {
					String message = "Client requests exceeds TPS limit. client:" + cID;
					TPILogger.tl.warn(message + ", path:" + request.getRequestURI());
					return getCheckErrorResp("tps", message, request.getRequestURI(),
							HttpStatus.TOO_MANY_REQUESTS.value());
				}
			}
		}

		return null;
	}

	private OAuthTokenErrorResp getCheckErrorResp(String checkError, String msg, String path, int status) {
		OAuthTokenErrorResp resp = new OAuthTokenErrorResp();
		resp.setError(checkError);
		resp.setMessage(msg);
		resp.setPath(path);
		resp.setStatus(status);
		resp.setTimestamp(System.currentTimeMillis() + "");
		return resp;
	}

	private void checkResponse(HttpServletRequest request, HttpServletResponse response, //
			String entry, String locale, int status, ICheck check) {
		ResHeader vo = ControllerUtil.tsmpCheckResponseBaseObj(check, locale);

		ObjectMapper mapper = new ObjectMapper();
		response.setHeader("Content-Type", "application/json; charset=utf-8");
		response.setStatus(status);
		try {
			// 檢核失敗時寫API Log
			// Write API Log when verification fails
			String simpleName = check.getClass().getSimpleName();
			if (!"ModeCheck".equals(simpleName))
				writeLog(request, response, entry);

			// checkmarx, Missing HSTS Header
			response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
			response.getWriter().append(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(vo));
			response.getWriter().flush();
			response.flushBuffer();
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
		}
	}

	private String getLocale(HttpServletRequest request) {
		String locale = "";
		for (Enumeration<?> e = request.getHeaderNames(); e.hasMoreElements();) {
			String nextHeaderName = (String) e.nextElement();
			String headerValue = request.getHeader(nextHeaderName);
			if ("locale".equalsIgnoreCase(nextHeaderName)) {
				locale = headerValue;
				return locale;
			}
		}
		return locale;
	}

	private String getQueryStringAndFormData(HttpServletRequest request) {
		Enumeration<String> keys = request.getParameterNames();
		StringBuilder strBuf = new StringBuilder();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			String[] valueArray = request.getParameterValues(key);
			if (valueArray.length >= 2) {
                for (String s : valueArray) {
                    strBuf.append(key).append(" ").append(s).append(" ");
                }
			} else {
				String value = request.getParameter(key);
				strBuf.append(key).append(" ").append(value).append(" ");
			}

		};
		return strBuf.toString();
	}

	/**
	 * [ZH]取得 cid, 依表頭 Authorization 的各種格式 <br>
	 * cid 為 v3 底層的名稱, 即 client ID <br>
	 * [EN]Get cid, according to the various formats of the header Authorization <br>
	 * cid is the name of the v3 underlying layer, i.e. client ID <br>
	 */
	private String getCID(HttpServletRequest httpReq) {
		// 解析資料
		String auth = httpReq.getHeader(CommForwardProcService.AUTHORIZATION);
        return tokenCheck.getCid(auth);
	}

	private void setModuleNameAndApiIdToReq(HttpServletRequest request, String reqUrl, boolean paths) {

		String moduleName = null;
		String apiKey = null;

		if (!paths) {
			String keyword = "tsmpc/";
			int flag = reqUrl.indexOf(keyword);
			if (flag == -1) {
				keyword = "tsmpg/";
				flag = reqUrl.indexOf(keyword);
			}
			String urlStr = reqUrl.substring(flag + keyword.length());

			String[] urlArr = urlStr.split("/");
			moduleName = urlArr[0];
			apiKey = urlArr[1];

			if (!isTsmpcApiExist(moduleName, apiKey)) {
				moduleName = null;
				apiKey = null;
				return;
			}

			// [ZH]判斷是否有勾path parameter，若沒勾，且path又有待參數，則moduleName和apiKey都塞null
			// [EN]Check if the path parameter is checked. If not, and the path has parameters, both moduleName and apiKey are null.
			boolean isURLRID = isURLRID(moduleName, apiKey);
			if (!isURLRID && urlArr.length > 2) {
				moduleName = null;
				apiKey = null;
				return;
			}

		} else {
			TsmpApiReg apiReg = getDgrcRoutingHelper().calculateRoute(reqUrl);
			if (apiReg != null) {
				apiKey = apiReg.getApiKey();
				moduleName = apiReg.getModuleName();
			} else {
				// [ZH]找出為什麼是 null, 因為 request.getRequestURI()中間可能被 LB 轉換過
				// [EN]Find out why it is null, because request.getRequestURI() may have been converted by LB
				logger.error("reqUrl:" + reqUrl + ", request.getRequestURI():" + request.getRequestURI() + ", moduleName=null and apiID=null");
			}
		}

		request.setAttribute(GatewayFilter.API_ID, apiKey);
		request.setAttribute(GatewayFilter.MODULE_NAME, moduleName);
	}

	public Boolean isURLRID(String moduleName, String apiKey) {
		TsmpApiRegId tsmpApiRegId = new TsmpApiRegId(apiKey, moduleName);
		Optional<TsmpApiReg> tsmpApiReg = getTsmpApiRegCacheProxy().findById(tsmpApiRegId);
		if (tsmpApiReg.isEmpty())
			return false;

		String urlRid = tsmpApiReg.get().getUrlRid();
        return "1".equals(urlRid);
    }

	boolean isTsmpcApiExist(String moduleName, String apiKey) {
		TsmpApiId apiId = new TsmpApiId();
		apiId.setApiKey(apiKey);
		apiId.setModuleName(moduleName);
		Optional<TsmpApi> tsmpApi = getTsmpApiCacheProxy().findById(apiId);
        return tsmpApi.isPresent();
    }

	protected void writeLog(HttpServletRequest httpReq, HttpServletResponse httpResp, //
			String entry) {
		try {
			String uuid = UUID.randomUUID().toString();
			String mbody = getCommForwardProcService().getReqMbody(httpReq);
			String aType = getAType(httpReq);

			// ES
			TsmpApiLogReq logReqVo_es = getCommForwardProcService().addEsTsmpApiLogReq1(uuid, httpReq, mbody, entry,
					aType);
			getCommForwardProcService().addEsTsmpApiLogResp1(httpResp, logReqVo_es, mbody, 0);
			// RDB
			TsmpApiLogReq logReqVo_rdb = getCommForwardProcService().addRdbTsmpApiLogReq1(uuid, httpReq, mbody, entry,
					aType);
			getCommForwardProcService().addRdbTsmpApiLogResp1(httpResp, logReqVo_rdb, mbody, 0);
		} catch (Exception e) {
			TPILogger.tl.debug(StackTraceUtil.logStackTrace(e));
		}
	}

	private String getAType(HttpServletRequest request) {
		String aType = "R";
		try {
			String apiId = (String) request.getAttribute(GatewayFilter.API_ID);
			String moduleName = (String) request.getAttribute(GatewayFilter.MODULE_NAME);
			boolean cApiKeySwitch = getCommForwardProcService().getcApiKeySwitch(moduleName, apiId);
			if (cApiKeySwitch) {
				aType = "C";
			}
		} catch (Exception e) {
			String eMsg = "\nrequest.getRequestURI():  " + request.getRequestURI() + "\n";
			eMsg += StackTraceUtil.logTpiShortStackTrace(e);
			TPILogger.tl.debug(eMsg);
		}
		return aType;
	}

	protected TsmpApiRegCacheProxy getTsmpApiRegCacheProxy() {
		return tsmpApiRegCacheProxy;
	}

	protected DgrcRoutingHelper getDgrcRoutingHelper() {
		return dgrcRoutingHelper;
	}

	protected TsmpApiCacheProxy getTsmpApiCacheProxy() {
		return tsmpApiCacheProxy;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected CommForwardProcService getCommForwardProcService() {
		return this.commForwardProcService;
	}

	public static void setApiRespThroughput() {
		// [ZH]獲取目前秒數
		// [EN]Get the current seconds
		var currentSeconds = Instant.now().getEpochSecond();


		// [ZH]檢查currentSeconds目前時間點有沒有資料，若沒有currentSeconds就初始值1，若有currentSeconds就累加1
		// [EN]Check if there is data at the current time point of currentSeconds. If there is no currentSeconds, the initial value is 1. If there is currentSeconds, it is accumulated by 1
		synchronized (GatewayFilter.apiRespThroughputObjLock) {
			var transactions = GatewayFilter.apiRespThroughput.getOrDefault(currentSeconds, 1L);
			GatewayFilter.apiRespThroughput.put(currentSeconds, transactions + 1);
		}
	}

	public static void setApiReqThroughput() {
		// [ZH]獲取目前秒數
		// [EN]Get the current seconds
		var currentSeconds = Instant.now().getEpochSecond();

		// [ZH]檢查currentSeconds目前時間點有沒有資料，若沒有currentSeconds就初始值1，若有currentSeconds就累加1
		// [EN]Check if there is data at the current time point of currentSeconds. If there is no currentSeconds, the initial value is 1. If there is currentSeconds, it is accumulated by 1
		synchronized (GatewayFilter.apiReqThroughputObjLock) {
			var transactions = GatewayFilter.apiReqThroughput.getOrDefault(currentSeconds, 1L);
			GatewayFilter.apiReqThroughput.put(currentSeconds, transactions + 1);
		}
	}

	/**
	 * 是否為 dgrv4 特權 API (不驗 token 之API) <br>
	 * 例如: version、logger、online、AA0325、AA0326 <br>
	 * 
	 * @return true: 是; false: 不是
	 */
	public static boolean isSpecialApi(String uri) {
		// dgrv4 特權、不驗 token 之API,
		boolean isSpecial = false;
		List<String> list = Arrays.asList("/dgrv4/version", "/dgrv4/logger", "/dgrv4/onlineConsole2",
				"/dgrv4/11/AA0325", "/dgrv4/11/AA0326", "/dgrv4/shutdown","/dgrv4/en_us","/dgrv4/zh_tw");
		for (String list001 : list) {
			if (uri.contains(list001)) {
				isSpecial = true;
				break;
			}
		}

		return isSpecial;
	}

	/**
	 * 是否為 dgR AC API
	 */
	public static boolean isDgrUrl(String uri) {
		boolean isDgrUrl = ((uri.startsWith("/dgrv4/11/")));
		isDgrUrl = isDgrUrl || ((uri.startsWith("/dgrv4/17/")));

		return isDgrUrl;
	}
}
