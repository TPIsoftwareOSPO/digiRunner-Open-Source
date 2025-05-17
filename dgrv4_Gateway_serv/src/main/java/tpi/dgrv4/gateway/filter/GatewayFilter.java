package tpi.dgrv4.gateway.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import tpi.dgrv4.gateway.vo.ErrorResp;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp2;
import tpi.dgrv4.gateway.vo.ResHeader;
import tpi.dgrv4.gateway.vo.TsmpApiLogReq;

@Component
public class GatewayFilter extends OncePerRequestFilter {

	@Value("${cors.allow.headers}")
	private String corsAllowHeaders;
	
	@Setter(onMethod_ = @Autowired, onParam_ = @Qualifier("async-workers"))
	ThreadPoolTaskExecutor asyncWorkerPool;

	@Setter(onMethod_ = @Autowired, onParam_ = @Qualifier("async-workers-highway"))
	@Qualifier("async-workers-highway")
	ThreadPoolTaskExecutor asyncWorkerHighwayPool;
	
	// [ZH]StringBuffer 的高併發替代品 StringBuilder(非同步) + ThreadLocal(多執行緒安全性)
	// [EN]High-concurrency alternative to StringBuffer: StringBuilder(asynchronous) + ThreadLocal(thread safety)
	private static final ThreadLocal<StringBuilder> sbBuilderHolder = ThreadLocal.withInitial(() -> new StringBuilder());

	// [ZH]高併發網路流量快速通關分派裝置(記憶單元)
	// [EN]High-concurrency network traffic fast-pass dispatch device (memory unit)
	private static final Map<String, Map<String, Long>> apiRespTimeMap = new ConcurrentHashMap<>();
	
//	public static final String cspDefaultVal = "default-src %s 'self' 'unsafe-inline'; img-src https://* 'self' data:;";

	public static final Object throughputObjLock = new Object();
	public static final LinkedHashMap<Integer, Integer> apiReqThroughput = new LinkedHashMap<Integer, Integer>() {
		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
			// [ZH]限制HashMap大小為10個
			// [EN]Limit HashMap size to 10 entries
			return size() > 10;
		}
	};
	public static final LinkedHashMap<Integer, Integer> apiRespThroughput = new LinkedHashMap<Integer, Integer>() {
		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
			// [ZH]限制HashMap大小為10個
			// [EN]Limit HashMap size to 10 entries
			return size() > 10;
		}
	};

	private static final long TSMPC_ROUTING = 0;
	private static final long DGRC_ROUTING = 1;
	private static final long TSMP_DGRCROUTING = 2;
	private static final String GCP_ROUTING = "/kibana";

	public static final String API_ID = "apiId";
	public static final String MODULE_NAME = "moduleName";
	
	// [ZH]高併發網路流量快速通關分派裝置(控制旗標)	
	// [EN]High concurrent network traffic fast clearance dispatch device (control flag)
	public static final String START_TIME = "startTime";
	public static final String END_TIME = "endTime";
	public static final String SETWORK_THREAD = "SetWorkThread";
	public static final String FAST = "fast";
	public static final String SLOW = "slow";
	public static final String ELAPSED_TIME = "elapsedTime";
	public static final String HTTP_CODE = "httpCode";
	public static final String SET_CIRCUIT_BREAKER = "setCircuitBreaker";
	public static final boolean CIRCUIT_BREAKER_STATUS = true; // [ZH]表示要熔斷 // [EN]Indicates that the circuit break is to be blown
	public static final long ELAPSED_TIME_DEF_VAL = 999999L; // [ZH]耗時的預設值 // [EN]Preset time consumption
	public static final int HTTP_CODE_DEF_VAL = -1; // [ZH]HTTP code 的預設值 // [EN]Default value of HTTP code
	public static final double PEAK_USAGE_VALUE = 0.8; // [ZH]尖峰使用量的值 // [EN]Peak usage value
	public static final long ELAPSED_TOO_LONG_VALUE = 30000L; // [ZH]耗時過久的值 // [EN]Value that took too long

	private TPILogger logger;
	private ApiStatusCheck apiStatusCheck;
	private IgnoreApiPathCheck ignoreApiPathCheck;
	private SqlInjectionCheck sqlInjectionCheck;
	private TrafficCheck trafficCheck;
	private XssCheck xssCheck;
	private XxeCheck xxeCheck;
	private TokenCheck tokenCheck;
	private ApiNotFoundCheck apiNotFoundCheck;
	private TsmpApiCacheProxy tsmpApiCacheProxy;
	private DgrcRoutingHelper dgrcRoutingHelper;
	private TsmpApiRegCacheProxy tsmpApiRegCacheProxy;
	private TsmpSettingService tsmpSettingService;
	private CommForwardProcService commForwardProcService;
	private WebsiteService websiteService;
	private HostHeaderCheck hostHeaderCheck;
	private DgrJtiCheck dgrJtiCheck;
	private ModeCheck modeCheck;
	private CusTokenCheck cusTokenCheck;
	private BotDetectionCheck dotBotDetectionCheck;

	@Autowired
	public GatewayFilter(TPILogger logger, ApiStatusCheck apiStatusCheck, IgnoreApiPathCheck ignoreApiPathCheck,
			SqlInjectionCheck sqlInjectionCheck, TrafficCheck trafficCheck, XssCheck xssCheck, XxeCheck xxeCheck,
			TokenCheck tokenCheck, ApiNotFoundCheck apiNotFoundCheck, TsmpApiCacheProxy tsmpApiCacheProxy,
			DgrcRoutingHelper dgrcRoutingHelper, TsmpApiRegCacheProxy tsmpApiRegCacheProxy,
			TsmpSettingService tsmpSettingService, CommForwardProcService commForwardProcService,
			WebsiteService websiteService, HostHeaderCheck hostHeaderCheck, DgrJtiCheck dgrJtiCheck,
			ModeCheck modeCheck, CusTokenCheck cusTokenCheck, BotDetectionCheck dotBotDetectionCheck) {
		super();
		this.logger = logger;
		this.apiStatusCheck = apiStatusCheck;
		this.ignoreApiPathCheck = ignoreApiPathCheck;
		this.sqlInjectionCheck = sqlInjectionCheck;
		this.trafficCheck = trafficCheck;
		this.xssCheck = xssCheck;
		this.xxeCheck = xxeCheck;
		this.tokenCheck = tokenCheck;
		this.apiNotFoundCheck = apiNotFoundCheck;
		this.tsmpApiCacheProxy = tsmpApiCacheProxy;
		this.dgrcRoutingHelper = dgrcRoutingHelper;
		this.tsmpApiRegCacheProxy = tsmpApiRegCacheProxy;
		this.tsmpSettingService = tsmpSettingService;
		this.commForwardProcService = commForwardProcService;
		this.websiteService = websiteService;
		this.hostHeaderCheck = hostHeaderCheck;
		this.dgrJtiCheck = dgrJtiCheck;
		this.modeCheck = modeCheck;
		this.cusTokenCheck = cusTokenCheck;
		this.dotBotDetectionCheck = dotBotDetectionCheck;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// TPILogger.tl.debug("\n--【LOGUUID】【" + "No" + "】【before start】--\n");

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
			
			showSrcURI(uri, remoteAdd, remoteHost, method01, QString); // debug 顯示 URI & src IP
			
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
			boolean b = (uri.length() >= 4 && (uri.substring(0, 4).equals("/11/")));
			b = b || (uri.length() >= 4 && (uri.substring(0, 4).equals("/17/")));
			b = b || (uri.length() >= 6 && (uri.substring(0, 6).equals("/dgrc/")));
			b = b || (uri.length() >= 7 && (uri.substring(0, 7).equals("/tsmpc/")));
			b = b || (uri.length() >= 7 && (uri.substring(0, 7).equals("/tsmpg/")));// [ZH]因為v3的noAuth要用tsmpg // [EN]Because v3's noAuth needs to use tsmpg
			b = b || (uri.length() >= 7 && (uri.substring(0, 7).equals("/dgrv4/")));
			b = b || (uri.length() >= 7 && (uri.substring(0, 7).equals("/oauth/")));
			b = b || (uri.length() >= 10 && (uri.substring(0, 10).equals("/mocktest/")));
			b = b || (uri.length() >= 8 && (uri.substring(0, 8).equals("/kibana/")));
			b = b || (uri.length() >= 21 && (uri.substring(0, 21).equals("/composer/swagger3.0/")));
			b = b || (uri.length() >= 9 && (uri.substring(0, 9).equals("/website/")));
			b = b || (uri.length() >= 10 && (uri.substring(0, 10).equals("/http-api/")));
			b = b || (uri.length() >= 16 && (uri.substring(0, 16).equals("/_plugin/kibana/")));
			b = b || (uri.length() >= 13 && (uri.substring(0, 13).equals("/_dashboards/"))); // [ZH]AWS上的OpenSearch的URL路徑 // [EN]URL path for OpenSearch on AWS
			b = b || (uri.length() >= 12 && (uri.substring(0, 12).equals("/dgrliveness")));	// [ZH]國壽要求一個 非/dgrv4的探針API // [EN]China Life requires a non-dgrv4 probe API
			b = b || (uri.length() >= 9 && (uri.substring(0, 9).equals("/liveness"))); // [ZH]國壽要求一個 非/dgrv4的探針API // [EN]China Life requires a non-dgrv4 probe API
			b = b || (uri.length() >= 10 && (uri.substring(0, 10).equals("/readiness"))); // 國壽要求一個 非/dgrv4的探針API // [EN]China Life requires a non-dgrv4 probe API
			b = b || (uri.length() >= 8 && (uri.substring(0, 8).equals("/kibana/")))            
					|| isGCPkibana(request, kibanaPrefix); // [ZH]判斷是不是走/kibana2的GCP 相關URL // [EN]Determine whether to use the GCP related URL of /kibana2
			b = !(b);

			// [ZH]true 即為 tsmpc, dgrc 路由
			// [EN]true means tsmpc, dgrc routing
			if (b) {
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
					if (uri.split("/").length == 3 && uri.length() >= 7 && uri.substring(0, 7).equals("/tsmpc/")) {
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
				if (apiId == null && moduleName == null) {
					TPILogger.tl.warn("uri = " + uri + ", moduleName=null and apiID=null");
					throw new DgrException(404, apiNotFoundCheck);
				} else {
					String path = null;
					if (uri.substring(0, 5).equals("/dgrc")) {
						path = uri.substring(5, uri.length());
						entry = "dgrc";
					}
					if (uri.substring(0, 6).equals("/tsmpc") || uri.substring(0, 6).equals("/tsmpg") // [ZH]因為v3的noAuth要用tsmpg // [EN]Because v3's noAuth needs to use tsmpg
					) {
						path = uri.substring(6, uri.length());
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
			// 高併發網路流量快速通關分派裝置: 計算完成的結果, 暫存在 request.setAttribute(k,v)
			// [EN]Reset uri to forward to /tsmpc/** or /dgrc/**,
			// The uri will only change to "dgrc" or "tsmpc", of course the uri may not change at all
			// High concurrent network traffic fast clearance dispatch device: The result of the calculation is temporarily stored in request.setAttribute(k,v)
			request = rewriteRequestURI(request, uri);
			
			// [ZH]如果有熔斷, 輸出錯誤訊息後 return
			// [EN]If there is a circuit break, output an error message and return
			boolean isCircuitBreaker = circuitBreakerResponseFlush(request, response, uri);
			if (isCircuitBreaker) {
				return;
			}
			
			// [ZH]計算API Req每秒轉發吞吐量
			// [EN]Calculate the API Req forwarding throughput per second
			setApiReqThroughput();

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
		}

		// TPILogger.tl.debug("\n--【LOGUUID】【" + "No" + "】【before end】--\n");

		// add Last Resp Header
		HttpServletResponse myResponse = (HttpServletResponse) response;
		CusContentCachingResponseWrapper responseWrapper = new CusContentCachingResponseWrapper(myResponse);

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
		if (isAddCsp && StringUtils.hasText(dgrCspVal) && !"*".equals(dgrCspVal.trim())) {
			// [ZH]如果是星號全不加 否則依照填入內容加上
			// [EN]If there are no asterisks, add them according to the entered content
			
			responseWrapper.setHeaderByForce("Content-Security-Policy", dgrCspVal);
		}
		
		// [ZH]CORS改從Setting取值
		// [EN]CORS changes the value to be obtained from Setting
		responseWrapper.setHeaderByForce("Access-Control-Allow-Origin", getTsmpSettingService().getVal_DGR_CORS_VAL());
		
		// [ZH]responseWrapper.setHeaderByForce("Access-Control-Allow-Headers",
		// [EN]"Content-Type, Authorization, SignCode, Language"); // CORS
		responseWrapper.setHeaderByForce("Access-Control-Allow-Headers", corsAllowHeaders); // "2. corsAllowHeaders = "
																							// + corsAllowHeaders
		responseWrapper.setHeaderByForce("Access-Control-Allow-Methods", "POST,GET,OPTIONS,PUT,PATCH,DELETE"); // CORS

		responseWrapper.setHeaderByForce("X-Frame-Options", "sameorigin");
		responseWrapper.setHeaderByForce("X-Content-Type-Options", "nosniff");
		responseWrapper.setHeaderByForce("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
		responseWrapper.setHeaderByForce("Referrer-Policy", "strict-origin-when-cross-origin");
		responseWrapper.setHeaderByForce("Cache-Control", "no-cache");
		responseWrapper.setHeaderByForce("Pragma", "no-cache");
		if (request.getHeader("isSse") != null) {
			responseWrapper.setHeaderByForce("Content-Type", "text/event-stream");
		}

		// [ZH]將取得的 DGR_CORS_VAL 放入 ServletContext 裡面供其他類取用,例如:AllowCorsFilterConfig
		// [EN]Put the obtained DGR_CORS_VAL into ServletContext for other classes to use, for example: AllowCorsFilterConfig
		request.getServletContext().setAttribute("Access-Control-Allow-Origin",
				getTsmpSettingService().getVal_DGR_CORS_VAL());

		// [ZH]將取得的 DGR_CSP_VAL 放入 ServletContext 裡面供其他類取用,例如:AllowCorsFilterConfig
		// [EN]Put the obtained DGR_CSP_VAL into ServletContext for other classes to use, for example: AllowCorsFilterConfig
		request.getServletContext().setAttribute("Content-Security-Policy", dgrCspVal);

		// GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH

		try {
			// ... 原有邏輯 ...
			// ... original logic ...
			filterChain.doFilter(request, response);
//			filterChain.doFilter(request, responseWrapper); //response 空白一片
	    } catch (Exception e) {
	    	if (response.getStatus() == 200) {
	    		// [ZH]為了解決高併發時, 2個 worker thread 搶 filter 的問題
	    		// [EN]To solve the problem of two worker threads competing for filters during high concurrency
	    		// jakarta.servlet.ServletException: Request processing failed: java.lang.IllegalStateException: AsyncWebRequest must not be null
	    		StringBuilder sb = new StringBuilder();
	    		sb.append("\nresponse.getStatus()::" + response.getStatus());
	    		sb.append("\nrequest.getRequestURI()::" + request.getRequestURI());
	    		sb.append("\nmsg::" + e.getLocalizedMessage());
	    		TPILogger.tl.warn(sb.toString());
	    		return ;
	    	}
	    	TPILogger.tl.error("response.getStatus(): " + response.getStatus());
	    	TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
	    	
	    	// [ZH]重新拋出異常，確保錯誤能被正確處理
	    	// [EN]Rethrow the exception to ensure the error is handled correctly
	        throw e;  
	    } finally {
	        // 清理資源
	        //cleanup();
	    }

		// You can't delete headers afterwards by the standard Servlet API. Your best
		// bet is to just prevent the header from being set.

		// System.out.println(".....filter.....2");
		// TPILogger.tl.debug("\n--【LOGUUID】【" + "No" + "】【after end】--\n");
	}
	
	/**
	 * [ZH]如果有熔斷, 將錯誤訊息輸出, 回傳 true; <br>
	 * 否則, 回傳 false <br>
	 * [EN]If there is a circuit break, output the error message and return true; <br>
	 * Otherwise, return false <br>
	 */
	private boolean circuitBreakerResponseFlush(HttpServletRequest request, HttpServletResponse response, String uri)
			throws JsonProcessingException {
		Object cbAttrObj = request.getAttribute(GatewayFilter.SET_CIRCUIT_BREAKER);
		if (!(cbAttrObj instanceof Boolean)) {
			return false;
		}

		if ((boolean) cbAttrObj) {
			// [ZH]熔斷不執行API, 顯示錯誤訊息
			// [EN]The API is not executed and an error message is displayed
			String errMsg = "Service is temporarily unavailable. Please try again later." + "Circuit breaker uri:"
					+ uri;
			ResponseEntity<ErrorResp> errRespEntity = new ResponseEntity<>(new ErrorResp("Service Unavailable", errMsg),
					HttpStatus.TOO_MANY_REQUESTS);// 429
			ObjectMapper mapper = new ObjectMapper();
			String errorRespStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(errRespEntity);
			TPILogger.tl.warn(errorRespStr);

			// [ZH]將內容輸出
			// [EN]Output the content
			responseFlush(response, errRespEntity.getStatusCode().value(), errorRespStr);
			return true; // [ZH]有熔斷 // [EN]There is a circuit break
		}

		return false; //[ZH] 沒有熔斷 // [EN]No circuit break
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
		Map<String, Long> innerMap = apiRespTimeMap.computeIfAbsent(keyUri, k -> new ConcurrentHashMap<>());
		// 只有當 innerMap 是空的時，才放入值
		if (innerMap.isEmpty()) {
			innerMap.put(GatewayFilter.ELAPSED_TIME, GatewayFilter.ELAPSED_TIME_DEF_VAL);
			innerMap.put(GatewayFilter.HTTP_CODE, (long) GatewayFilter.HTTP_CODE_DEF_VAL);
		}
	}

	/**
	 * [ZH]高併發網路流量快速通關分派裝置(控制單元) <br>
	 * 依據 method URI, 截取上一次 Request 的往返時間 elapsed time <br>
	 * [EN]High concurrent network traffic fast clearance dispatching device (control unit) <br>
	 * Based on the method URI, intercept the elapsed time of the last Request. <br>
	 */
	private static long fetchUriHistoryElapsed(HttpServletRequest request) {
		String method01 = request.getMethod();
		String keyUri = getUriKeyString(method01, request.getRequestURI());
		return getApiResultMapValue(apiRespTimeMap, keyUri, GatewayFilter.ELAPSED_TIME);
	}
	
	/**
	 * [ZH]高併發網路流量快速通關分派裝置(控制單元) <br>
	 * 依據 method URI, 截取上一次 Response HTTP code <br>
	 * [EN]High concurrent network traffic fast clearance dispatching device (control unit) <br>
	 * Based on the method URI, intercept the last Response HTTP code <br>
	 */
	private static Long fetchUriHistoryHttpCode(HttpServletRequest request) {
		String method01 = request.getMethod();
		String keyUri = getUriKeyString(method01, request.getRequestURI());
		return getApiResultMapValue(apiRespTimeMap, keyUri, GatewayFilter.HTTP_CODE);
	}
	
	/**
	 * [ZH]取得上次打 API 記錄的值, API 耗時或 HTTP code <br>
	 * [EN]Get the value of the last API record, API time or HTTP code <br>
	 */
	private static Long getApiResultMapValue(Map<String, Map<String, Long>> apiRespTimeMap, String keyUri, String key) {
		//更優雅地處理 null 值
		return Optional.ofNullable(apiRespTimeMap.get(keyUri))
                .map(m -> m.get(key)) // 正常取值
                .orElse(-1L); // null 時, 返回預設值 -1
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
		long endTime = System.currentTimeMillis();
		
		// 以此 Request 記錄它的 URI Key: [GET] /aa/bb , 沒有含 QueryString
		long startTime = (long)request.getAttribute(GatewayFilter.START_TIME);
		
		// HTTP code
		int httpCode = (int) request.getAttribute(GatewayFilter.HTTP_CODE);
		
		String method01 = request.getMethod();
		String afterUri = request.getRequestURI(); // 這裡被轉換過 '/dgrc' 了
		String srcUri = null;
		if(afterUri.toLowerCase().indexOf("/dgrc") == 0) {
			srcUri = afterUri.substring(afterUri.indexOf("/", 1)); // 去除第一個 path (/dgrc)
		}else {
			//tsmpc, tsmpg
			srcUri = afterUri;//It has not been converted so the original value is taken.
		}
		
		
		// 計算 '耗時'
		long elapsed = endTime - startTime;

		// 更新記錄的值
		String keyUri = getUriKeyString(method01, srcUri);
		Map<String, Long> innerMap = apiRespTimeMap.computeIfAbsent(keyUri, k -> new ConcurrentHashMap<>());
		innerMap.put(GatewayFilter.ELAPSED_TIME, elapsed);
		innerMap.put(GatewayFilter.HTTP_CODE, Long.valueOf(httpCode));
		
		//realtime dashboard
		if(httpCode >= 400 || httpCode <= 0) {
			Long fail = RealtimeDashboardService.realtimeDashobardApiMap.computeIfAbsent(RealtimeDashboardService.FAIL, k -> 0L);
			RealtimeDashboardService.realtimeDashobardApiMap.put(RealtimeDashboardService.FAIL, ++fail);
			if(httpCode == 401) {
				Long badAttempt401 = RealtimeDashboardService.realtimeDashobardApiMap.computeIfAbsent(RealtimeDashboardService.BAD_ATTEMPT_401, k -> 0L);
				RealtimeDashboardService.realtimeDashobardApiMap.put(RealtimeDashboardService.BAD_ATTEMPT_401, ++badAttempt401);
			}else if(httpCode == 403) {
				Long badAttempt403 = RealtimeDashboardService.realtimeDashobardApiMap.computeIfAbsent(RealtimeDashboardService.BAD_ATTEMPT_403, k -> 0L);
				RealtimeDashboardService.realtimeDashobardApiMap.put(RealtimeDashboardService.BAD_ATTEMPT_403, ++badAttempt403);
			}else {
				Long badAttemptOthers = RealtimeDashboardService.realtimeDashobardApiMap.computeIfAbsent(RealtimeDashboardService.BAD_ATTEMPT_OTHERS, k -> 0L);
				RealtimeDashboardService.realtimeDashobardApiMap.put(RealtimeDashboardService.BAD_ATTEMPT_OTHERS, ++badAttemptOthers);
			}
		}else {
			Long success = RealtimeDashboardService.realtimeDashobardApiMap.computeIfAbsent(RealtimeDashboardService.SUCCESS, k -> 0L);
			RealtimeDashboardService.realtimeDashobardApiMap.put(RealtimeDashboardService.SUCCESS, ++success);
		}
		
		// print
		// fetchUriHistoryList(); //不需要輸出以免影響 CPU 使用率
	}
	
	/**
	 * Print list <br>
	 * [ZH]高併發網路流量快速通關分派裝置(輸出單元) <br>
	 * [EN]High concurrent network traffic fast clearance dispatching device (output unit) <br>
	 */
	public static String fetchUriHistoryList() {
		StringBuilder sb = getFetchUriHistoryListInitValue();
		
		apiRespTimeMap.forEach((key, value) -> {
			if(key.length() > 40) {
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
			
			Long httpCode = value.get(GatewayFilter.HTTP_CODE);
			long elapsedTime = value.get(GatewayFilter.ELAPSED_TIME);
			String httpCodeVal = "";
			if (httpCode == null) {
				httpCodeVal = "";
			} else {
				httpCodeVal = httpCode + "";
			}
			sb.append(", " + httpCodeVal + ",  " + elapsedTime + "ms\n");			
		});
		String result = sb.toString();
		sb.setLength(0); // 清空

		return result;
	}
	
	public static StringBuilder getFetchUriHistoryListInitValue() {
		StringBuilder sb = sbBuilderHolder.get();
		sb.append(String.format("%60s", "").replace(' ', '-')+"\n");
		
		return sb;
	}
	
	public void unload() {
		sbBuilderHolder.remove();
	}
	
	/**
	 * [ZH]高併發網路流量快速通關分派裝置(運算單元)
	 * [EN]High-Concurrency Network Traffic Fast-Pass Dispatch Device (Computing Unit)
	 * @param method01
	 * @param uri
	 * @return
	 */
	private static String getUriKeyString(String method01, String uri) {
		String key = "[" + method01 + "] (" + uri + ")";
		return key;
	}
	
	/**
	 * [ZH]高併發網路流量快速通關分派裝置(運算單元)+(控制單元)
	 * [EN]High-Concurrency Network Traffic Fast-Pass Dispatch Device (Computing Unit) + (Control Unit)
	 * @param dgrcUri
	 * @return newUri
	 */
	private void fetchUriHistoryRouterControl(HttpServletRequest request) {

		// [ZH]開始記錄 URI 的資訊, 未來做為 '高併發網路流量快速通關分派裝置' 轉發的依據
		// [EN]Start recording URI information, which will be used as the basis for forwarding by the 'High-Concurrency Network Traffic Fast-Pass Dispatch Device'
		fetchUriHistoryBefore(request);
		
		long lastTime = fetchUriHistoryElapsed(request);
		Long httpCode = fetchUriHistoryHttpCode(request);
	
		// [ZH]Policy-1. <測速分流>  執行較快的: 走快車道
		// [EN]Policy-1. <Speed Test Distribution> Faster execution: Use the fast lane
		if (lastTime < tsmpSettingService.getVal_HIGHWAY_THRESHOLD()) { 
			request.setAttribute(GatewayFilter.SETWORK_THREAD, GatewayFilter.FAST);
			return;
		} 
		
		// [ZH]Policy-3. <事前預防> 執行較久的: 走慢車道 or 熔斷 or 換快車道
		// [EN]Policy-3. <Preventive Measure> Longer execution: Use slow lane OR circuit break OR switch to fast lane
		
		// [ZH]慢車道現況
		// [EN]Current status of slow lane
		int activeCount = asyncWorkerPool.getActiveCount();
		int maxPoolSize = asyncWorkerPool.getMaxPoolSize();
		
		// [ZH]快車道現況
		// [EN]Current status of fast lane
		int activeCountHighway = asyncWorkerHighwayPool.getActiveCount();
		int maxPoolSizeHighway = asyncWorkerHighwayPool.getMaxPoolSize();
		
		// [ZH]若慢車道的使用量尖峰, 判斷是否熔斷
		// [EN]Check if circuit breaking is needed when slow lane usage reaches peak
		boolean isCircuitBreaker = isCircuitBreaker(activeCount, maxPoolSize, lastTime, httpCode);
		if (isCircuitBreaker) { // 熔斷
			request.setAttribute(GatewayFilter.SET_CIRCUIT_BREAKER, GatewayFilter.CIRCUIT_BREAKER_STATUS);
			return;
		}
		
		// [ZH]Policy-2. <物盡其用> 如果慢車道的使用量已滿,且快車道為離峰,可走快車道; 否則維持慢車道
		// [EN]Policy-2. <Resource Optimization> If slow lane usage is full and fast lane is off-peak, can use fast lane; otherwise maintain slow lane
		boolean isTakeFast = isTakeFast(activeCount, maxPoolSize, activeCountHighway, maxPoolSizeHighway);
		if(isTakeFast) {
			request.setAttribute(GatewayFilter.SETWORK_THREAD, GatewayFilter.FAST);
		} else {
			request.setAttribute(GatewayFilter.SETWORK_THREAD, GatewayFilter.SLOW);
		}
	}
	
	/**
	 * [ZH]判斷是否要熔斷 <br>
	 * 自動熔斷機制:一般情況下,不會啟動熔斷機制 <br>
	 * 當慢車道使用量 >= 80% (尖峰)時, 啟動熔斷機制, <br>
	 * 若 API 的上次執行時間為 30 sec (timeout 時間) 以上, 且不在 200-499 範圍時才熔斷, <br> 
	 * 則判斷為記錄不良, 熔斷此 API 的新流量 <br>
	 * 當慢車道低於滿載的 80%, 則釋放流量, 停用熔斷判斷機制 <br>
     * [EN]Determine whether circuit breaking should be activated <br>
     * Automatic circuit breaking mechanism: Under normal circumstances, circuit breaking will not be activated <br>
     * When slow lane usage >= 80% (peak), circuit breaking mechanism is activated, <br>
     * If the API's last execution time is above 30 sec (timeout period), and the response is not within 200-499 range, <br>
     * Then it is considered a bad record, and new traffic to this API will be circuit broken <br>
     * When slow lane drops below 80% capacity, traffic is released and circuit breaking mechanism is deactivated <br>
     */
	protected boolean isCircuitBreaker(float activeCount, float maxPoolSize, long lastTime, long httpCode) {
		// [ZH]若是初始值 999999 & -1 , 表示第一次, 則不熔斷
		// [EN]If the initial value is 999999 & -1, indicating first time, then do not circuit break
		if (lastTime == GatewayFilter.ELAPSED_TIME_DEF_VAL && httpCode == GatewayFilter.HTTP_CODE_DEF_VAL) {
			// [ZH]第一次進入不熔斷
			// [EN]First entry will not trigger circuit breaking
			return false; 
		}
		
		// [ZH]慢車道使用量 (尖峰表示 '>=0.8')
		// [EN]Slow lane usage (peak indicates '>=0.8')
		float usage = activeCount / maxPoolSize;

		// [ZH]如果不滿足熔斷條件, 即"離峰"或"沒有耗時過久",直接返回 false
		// [EN]If circuit breaking conditions are not met, meaning "off-peak" or "not time-consuming", return false directly
		if (usage < GatewayFilter.PEAK_USAGE_VALUE || lastTime < GatewayFilter.ELAPSED_TOO_LONG_VALUE) { 
			return false;
		}
	    
	    // [ZH]只有當 HTTP code 不在 200-499 範圍時才熔斷, 此時 usage >= 0.8
		// [EN]Only circuit break when HTTP code is not in 200-499 range, at this point usage >= 0.8
	    boolean isCircuit = !(httpCode >= 200 && httpCode < 500);
	    if (isCircuit) {
	    	// [ZH]觸發熔斷
	    	// [EN]Trigger circuit breaking
	    	TPILogger.tl.warn("\n\tCircuit Breaker::" + "activeCount:" + activeCount +
	    			", maxPoolSize:" + maxPoolSize +
	    			", usage:" + usage +
	    			", lastTime:" + lastTime +
	    			", httpCode:" + httpCode);
	    }
	    return isCircuit;
	}
	
	/**
	 * [ZH]判斷慢車是否改走快車道 <br>
	 * 若慢車道(country road)的使用量已滿(100%), 即 已使用的路(activeCount) = 可使用的路(maxPoolSize) <br>
	 * (a).若 highway 的 activeCount 占 maxPoolSize < 80% (離峰)時, 慢車可以走快車道 <br>
	 * (b).若 highway 的 activeCount 占 maxPoolSize >= 80% 時, 慢車不能走快車道 <br>
	 * [EN]Determine if slow lane traffic should be redirected to fast lane <br>
	 * If slow lane (country road) usage is full (100%), meaning active count = pool size <br>
	 * (a). When highway's activeCount to maxPoolSize ratio < 80% (off-peak), slow lane
	 * traffic can be redirected to fast lane <br>
	 * (b). When highway's activeCount to maxPoolSize ratio >= 80%, slow lane traffic
	 * cannot be redirected to fast lane <br>
	 */
	protected boolean isTakeFast(float activeCount, float maxPoolSize, float activeCountHighway, float maxPoolSizeHighway) {
		if (activeCount != maxPoolSize) {
			// [ZH]慢車道使用量未滿(100%), 維持走慢車道
			// [EN]Slow lane usage is not full (100%), maintain slow lane routing
			
			// [ZH]走慢車道
			// [EN]Route through slow lane
			return false; 
		}
		
		// 快車道使用量
		// Fast lane usage
		float usage = activeCountHighway / maxPoolSizeHighway;
		if (usage < GatewayFilter.PEAK_USAGE_VALUE) {
			// [ZH]快車道離峰時, 可走快車道
			// [EN]During fast lane off-peak, can route through fast lane
			
			// [ZH]走快車道
			// [EN]Route through fast lane
			return true;
		}
		
		// [ZH]走慢車道
		// [EN]Route through slow lane
		return false;
	}
	
	private void showSrcURI(String uri, String remoteAddr, String remoteHost, 
			String method01, String qString) {
		boolean reqUriFlag = getTsmpSettingService().getVal_REQUEST_URI_ENABLED();
		if (reqUriFlag) {
			//...request.getRequestURI():[GET] (/dgrv4/ImGTW/refreshGTW)
			//...request.getRemoteAddr():(remoteAdd)
			//...request.getRemoteHost():(remoteHost)
			
			// [ZH]從 Thread 中取一個 sb 物件來用
			// [EN]Get a sb object from Thread to use
			StringBuilder sb = sbBuilderHolder.get();
			
			sb.append("\n...request.getRequestURI():[" + method01 + "] (" + uri + ")");
			sb.append("\n...request.getQueryString():(" + qString + ")");
			sb.append("\n...request.getRemoteAddr():(" + remoteAddr + ")");
			sb.append("\n...request.getRemoteHost():(" + remoteHost + ")\n");
			TPILogger.tl.debug(sb.toString());
			sb.setLength(0);
		}
	}
	
	/**
	 * [ZH]是否要記錄到高併發網路流量快速通關分派裝置, <br>
	 * 若 http method 為 "OPTIONS" 時, 是前端透過 "靜態網頁反向代理" 打進來的 API, <br>
	 * 就不做記錄, 即不顯示在 "URI Status" 和 "即時 dashboard" 畫面中 <br>
	 * [EN] Whether to record the high concurrent network traffic fast clearance dispatch device, <br>
	 * If the http method is "OPTIONS", it is the API that the front-end calls through the "static webpage reverse proxy". <br>
	 * Do not record, i.e. do not display in "URI Status" and "Real-time dashboard" screens <br>
	 */
	private static boolean isRecord(HttpServletRequest request) {
		String method = request.getMethod();
		if(method.equalsIgnoreCase("OPTIONS")) {
			return false;
		}
		
		return true;
	}

	/**
	 * 
	 * @param request
	 * @param uri     可能包含有 /tsmpc/、/dgrc/、、、、等
	 * @return
	 */
	private HttpServletRequest rewriteRequestURI(HttpServletRequest request, String uri) {
		String kibanaPrefix = getTsmpSettingService().getVal_KIBANA_REPORTURL_PREFIX();
		
		if (isDGRCorTSMPC(uri) && isRecord(request)) {
			/*
			 * [ZH]dgrc uri 要再經過 '高併發網路流量快速通關分派裝置',
			 * 高併發網路流量快速通關分派裝置: 計算完成的結果, 暫存在 request.setAttribute(k,v)
			 * [EN]dgrc uri needs to go through 'High Concurrency Network Traffic Fast Pass
			 * Dispatch Device', High Concurrency Network Traffic Fast Pass Dispatch Device:
			 * completed calculation results are temporarily stored in request.setAttribute(k,v)
			 */
			fetchUriHistoryRouterControl(request); 
			
			// [ZH]根據 request.getAttribute 判斷是否熔斷
			// [EN]Determine circuit breaking based on request.getAttribute
			Object cbAttrObj = request.getAttribute(GatewayFilter.SET_CIRCUIT_BREAKER);
			if (cbAttrObj instanceof Boolean && (boolean) cbAttrObj) {// 熔斷 // Circuit break
				return request;
			}
 
			// [ZH]不熔斷, 進行 controller 轉發
			// [EN]No circuit break, proceed with controller forwarding
			request = GatewayFilter.changeRequestURI(request, uri);
		} else if (uri.contains("dgrv4")) {
			return request;
			// [ZH]要同時符合 1. 是要走 /kibana2 以及url 內沒有 /kibana2 的才加入 預防/kibana2/login 被二次加入/kibana2
			// [EN]Must simultaneously meet two conditions: 1. Should route to /kibana2 and URL doesn't contain /kibana2 - this prevents /kibana2/login from having /kibana2 added twice
		} else if (isGCPkibana(request, kibanaPrefix) && !uri.contains("login")) {
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
	 * @param request uri
	 */
	private boolean isDGRCorTSMPC( String uri) {
		boolean b1 = (uri.length() >= 6 && (uri.substring(0, 6).equals("/dgrc/")));
		boolean b2 = (uri.length() >= 7 && (uri.substring(0, 7).equals("/tsmpc/")));
		// [ZH]因為v3的noAuth要用tsmpg
		// [EN]Because v3's noAuth needs to use tsmpg
		boolean b3 = (uri.length() >= 7 && (uri.substring(0, 7).equals("/tsmpg/")));
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
		Integer httpStatus = respEntity.getStatusCodeValue();
		String errorRespStr = null;
		ObjectMapper mapper = new ObjectMapper();
		Object bodyObj = respEntity.getBody();
		if (bodyObj instanceof OAuthTokenErrorResp) {
			OAuthTokenErrorResp errorResp = (OAuthTokenErrorResp) bodyObj;
			errorRespStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(errorResp);

		} else if (bodyObj instanceof OAuthTokenErrorResp2) {
			OAuthTokenErrorResp2 errorResp = (OAuthTokenErrorResp2) bodyObj;
			errorRespStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(errorResp);

		}

		responseFlush(httpResponse, httpStatus, errorRespStr);
	}

	/**
	 * [ZH]將內容輸出
	 * [EN]output the content
	 */
	private void responseFlush(HttpServletResponse httpResponse, Integer httpStatus, String errorRespStr) {
		httpResponse.setHeader("Content-Type", "application/json; charset=utf-8");
		httpResponse.setStatus(httpStatus);
		try {
			//checkmarx, Missing HSTS Header
			httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload"); 
			httpResponse.getWriter().append(errorRespStr);
			httpResponse.getWriter().flush();
			httpResponse.flushBuffer();
		} catch (JsonProcessingException e) {
			logger.error(StackTraceUtil.logStackTrace(e));
		} catch (IOException e) {
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
		boolean isKibana = (reqURI.indexOf("/kibana") == 0) ? true : false;// 是否為 "/kibana" 開頭
		boolean isDashboards = (reqURI.indexOf("/_dashboards") == 0) ? true : false;// 是否為 "/_dashboards" 開頭
		boolean isWebsite = (reqURI.indexOf("/website") == 0) ? true : false;// 是否為 "/website" 開頭
		boolean isKibana2 = (reqURI.indexOf("/kibana2") == 0) ? true : false;// 是否為 "/kibana" 開頭

		if (!isKibana && !isDashboards && !isWebsite && !isKibana2) {
			return true;
		}

		return false;
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
			if (isComposerAPI) {
				setModuleNameAndApiIdToReq(request, reqUrl, false);
			} else {
				setModuleNameAndApiIdToReq(request, reqUrl, true);
			}
		}

		boolean isTsmpc = reqUrl.substring(0, 7).equals("/tsmpc/");
		if (!isTsmpc) {
			isTsmpc = reqUrl.substring(0, 7).equals("/tsmpg/");// 因為v3的noAuth要用tsmpg
		}
		if (paths == TSMP_DGRCROUTING) {
			if (isTsmpc) {
				setModuleNameAndApiIdToReq(request, reqUrl, false);
			} else {
				setModuleNameAndApiIdToReq(request, reqUrl, true);
			}
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

		if (!opt_tsmpApi.isEmpty()) {
			// 看TSMP_API的API_SRC是C ==> Composer
			// 看TSMP_API的API_SRC是R ==> 註冊
			TsmpApi tsmpApi = opt_tsmpApi.get();
			if ("C".equals(tsmpApi.getApiSrc())) {
				return true;
			}
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

		String stringBody = new String();
		String method = request.getMethod();
		String contentType = request.getContentType() == null ? "" : request.getContentType().toLowerCase();

		if (HttpMethod.GET.matches(method)) {

			// 處理 QueryString
			stringBody = getQueryStringAndFormData(request);
		} else {

			// 處理 QueryString + RAW
			if (contentType.indexOf(MediaType.TEXT_PLAIN_VALUE.toLowerCase()) > -1
					|| contentType.indexOf(MediaType.APPLICATION_JSON_VALUE.toLowerCase()) > -1
					|| contentType.indexOf(MediaType.TEXT_HTML_VALUE.toLowerCase()) > -1
					|| contentType.indexOf(MediaType.APPLICATION_XML_VALUE.toLowerCase()) > -1) {
				if (byteBody.length != 0) {
					stringBody = new String(byteBody);
				}

				if (StringUtils.hasText(stringBody)) {
					stringBody = stringBody + " " + getQueryStringAndFormData(request);
				} else {
					stringBody = getQueryStringAndFormData(request);
				}
			}

			// 處理 QueryString + FormData
			if (contentType.indexOf(MediaType.MULTIPART_FORM_DATA_VALUE.toLowerCase()) > -1
					|| contentType.indexOf(MediaType.APPLICATION_FORM_URLENCODED_VALUE.toLowerCase()) > -1) {
				stringBody = getQueryStringAndFormData(request);
			}

			// 若沒有content Type，預設當作text處理
			if ("".equals(contentType)) {
				if (byteBody.length != 0) {
					stringBody = new String(byteBody);
				}

				if (StringUtils.hasText(stringBody)) {
					stringBody = stringBody + " " + getQueryStringAndFormData(request);
				} else {
					stringBody = getQueryStringAndFormData(request);
				}
			}
		}

		String body = stringBody == null ? "" : stringBody;

		return body;
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
			String noAuth = tsmpApiReg.get().getNoOauth();
			if ("1".equals(noAuth)) {
				// 若有勾 no auth, 則不檢查
			} else {
				// 否則, 檢查
				String cID = getCID(request); // 取得 client ID
				if (StringUtils.hasText(cID) && trafficCheck.check(cID)) {
					return getCheckErrorResp("tps", "Client requests exceeds TPS limit", request.getRequestURI(),
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

			//checkmarx, Missing HSTS Header
			response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
			response.getWriter().append(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(vo));
			response.getWriter().flush();
			response.flushBuffer();
		} catch (JsonProcessingException e) {
			logger.error(StackTraceUtil.logStackTrace(e));
		} catch (IOException e) {
			logger.error(StackTraceUtil.logStackTrace(e));
		}
	}

	private String getLocale(HttpServletRequest request) {
		String locale = "";
		for (Enumeration<?> e = request.getHeaderNames(); e.hasMoreElements();) {
			String nextHeaderName = (String) e.nextElement();
			String headerValue = request.getHeader(nextHeaderName);
			if ("locale".toLowerCase().equals(nextHeaderName.toLowerCase())) {
				locale = headerValue;
				return locale;
			}
		}
		return locale;
	}

	private String getQueryStringAndFormData(HttpServletRequest request) {
		Enumeration keys = request.getParameterNames();
		StringBuffer strBuf = new StringBuffer();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			String[] valueArray = request.getParameterValues(key);
			if (valueArray.length >= 2) {
				for (int i = 0; valueArray.length > i; i++) {
					strBuf.append(key + " " + valueArray[i] + " ");
				}
			} else {
				String value = request.getParameter(key);
				strBuf.append(key + " " + value + " ");
			}

		}
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
		String cid = tokenCheck.getCid(auth);
		return cid;
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
		if ("1".equals(urlRid))
			return true;

		return false;
	}

	boolean isTsmpcApiExist(String moduleName, String apiKey) {
		TsmpApiId apiId = new TsmpApiId();
		apiId.setApiKey(apiKey);
		apiId.setModuleName(moduleName);
		Optional<TsmpApi> tsmpApi = getTsmpApiCacheProxy().findById(apiId);
		if (tsmpApi.isEmpty())
			return false;
		return true;
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

	public WebsiteService getWebsiteService() {
		return websiteService;
	}

	public static void setApiRespThroughput() {
		// [ZH]獲取目前秒數
		// [EN]Get the current seconds
		long currentTimeMillis = System.currentTimeMillis();
		int currentSeconds = (int) (currentTimeMillis / 1000);

		// [ZH]檢查currentSeconds目前時間點有沒有資料，若沒有currentSeconds就初始值1，若有currentSeconds就累加1
		// [EN]Check if there is data at the current time point of currentSeconds. If there is no currentSeconds, the initial value is 1. If there is currentSeconds, it is accumulated by 1
		synchronized (GatewayFilter.throughputObjLock) {
			if (GatewayFilter.apiRespThroughput.containsKey(currentSeconds)) {
				Integer throughputSize = GatewayFilter.apiRespThroughput.get(currentSeconds);
				GatewayFilter.apiRespThroughput.put(currentSeconds, throughputSize + 1);
			} else {
				GatewayFilter.apiRespThroughput.put(currentSeconds, 1);
			}
		}
	}
	
	public static void setApiRespThroughput(HttpServletRequest httpReq) {
		setApiRespThroughput();
	}

	public static void setApiReqThroughput() {
		// [ZH]獲取目前秒數
		// [EN]Get the current seconds
		long currentTimeMillis = System.currentTimeMillis();
		int currentSeconds = (int) (currentTimeMillis / 1000);

		// [ZH]檢查currentSeconds目前時間點有沒有資料，若沒有currentSeconds就初始值1，若有currentSeconds就累加1
		// [EN]Check if there is data at the current time point of currentSeconds. If there is no currentSeconds, the initial value is 1. If there is currentSeconds, it is accumulated by 1
		synchronized (GatewayFilter.throughputObjLock) {
			if (apiReqThroughput.containsKey(currentSeconds)) {
				Integer throughputSize = apiReqThroughput.get(currentSeconds);
				apiReqThroughput.put(currentSeconds, throughputSize + 1);
			} else {
				apiReqThroughput.put(currentSeconds, 1);
			}
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
				"/dgrv4/11/AA0325", "/dgrv4/11/AA0326", "/dgrv4/shutdown");
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
		boolean isDgrUrl = (uri.length() >= 10 && (uri.substring(0, 10).equals("/dgrv4/11/")));
		isDgrUrl = isDgrUrl || (uri.length() >= 10 && (uri.substring(0, 10).equals("/dgrv4/17/")));

		return isDgrUrl;
	}
	
	/**
	 * 兩個方法的作用和為什麼會在高併發時出現問題 當這兩個方法保持預設值（true）時，在高併發情況下可能會出現問題：
	 * 
	 * 1.請求進入系統，第一次經過 filter 
	 * 2.開始非同步處理 
	 * 3.因為 shouldNotFilterAsyncDispatch = true，非同步分發時不會再次執行 filter 
	 * 4.此時如果有其他 filter 或處理邏輯修改了 request 的屬性
	 * 5.當非同步操作完成時，Spring 嘗試獲取 AsyncWebRequest，但可能已經被清除或變為 null
	 */
//    @Override
//    protected boolean shouldNotFilterAsyncDispatch() {
//        return false; //true, 在高併發時出現問題
//    }
//
//    @Override
//    protected boolean shouldNotFilterErrorDispatch() {
//        return false; //true, 在高併發時出現問題
//    }
}
