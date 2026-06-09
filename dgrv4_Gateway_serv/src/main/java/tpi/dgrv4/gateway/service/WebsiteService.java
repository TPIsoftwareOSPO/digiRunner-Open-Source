package tpi.dgrv4.gateway.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.servlet.http.Cookie;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import lombok.Builder;
import tpi.dgrv4.codec.utils.ExpireKeyUtil;
import tpi.dgrv4.codec.utils.ProbabilityAlgUtils;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.CheckmarxCommUtils;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.cache.proxy.DgrWebsiteCacheProxy;
import tpi.dgrv4.dpaa.component.cache.proxy.DgrWebsiteDetailCacheProxy;
import tpi.dgrv4.entity.entity.DgrWebsite;
import tpi.dgrv4.entity.entity.DgrWebsiteDetail;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpSettingCacheProxy;
import tpi.dgrv4.gateway.component.check.SqlInjectionCheck;
import tpi.dgrv4.gateway.component.check.XssCheck;
import tpi.dgrv4.gateway.component.check.XxeCheck;
import tpi.dgrv4.gateway.filter.GatewayFilter;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.common.utils.ClientIpUtil;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp2;
import tpi.dgrv4.gateway.vo.WebsiteInfo;
import tpi.dgrv4.gateway.vo.WebsiteTrafficVo;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

@Service
public class WebsiteService {

	public static WebsiteInfo websiteInfo;
	private String EXPIRE_KEY = "composerExpireKey";
	public Map<String, List<WebsiteTrafficVo>> trifficMap = new HashMap<String, List<WebsiteTrafficVo>>();

	// key由外到內,timestampSec->websiteName->targetUrl-->type(req,resp)
	public Map<Long, Map<String, Map<String, Map<String, Integer>>>> targetThroughputMap = new HashMap<>();
	public static final String TYPE_REQ = "req";
	public static final String TYPE_RESP = "resp";

	private TsmpSettingCacheProxy tsmpSettingCacheProxy;
	private TsmpSettingService tsmpSettingService;
	private DgrWebsiteCacheProxy dgrWebsiteCacheProxy;
	private DgrWebsiteDetailCacheProxy dgrWebsiteDetailCacheProxy;
	private GtwIdPVerifyService gtwIdPVerifyService;
	private TokenHelper tokenHelper;
	private SqlInjectionCheck sqlInjectionCheck;
	private XssCheck xssCheck;
	private XxeCheck xxeCheck;
	private ObjectMapper objectMapper;
	private GatewayFilter gatewayFilter;

	@Autowired
	public WebsiteService(TsmpSettingCacheProxy tsmpSettingCacheProxy, TsmpSettingService tsmpSettingService,
			DgrWebsiteCacheProxy dgrWebsiteCacheProxy, DgrWebsiteDetailCacheProxy dgrWebsiteDetailCacheProxy,
			GtwIdPVerifyService gtwIdPVerifyService, TokenHelper tokenHelper, SqlInjectionCheck sqlInjectionCheck,
			XssCheck xssCheck, XxeCheck xxeCheck, ObjectMapper objectMapper, @Lazy GatewayFilter gatewayFilter) {
		super();
		this.tsmpSettingCacheProxy = tsmpSettingCacheProxy;
		this.tsmpSettingService = tsmpSettingService;
		this.dgrWebsiteCacheProxy = dgrWebsiteCacheProxy;
		this.dgrWebsiteDetailCacheProxy = dgrWebsiteDetailCacheProxy;
		this.gtwIdPVerifyService = gtwIdPVerifyService;
		this.tokenHelper = tokenHelper;
		this.sqlInjectionCheck = sqlInjectionCheck;
		this.xssCheck = xssCheck;
		this.xxeCheck = xxeCheck;
		this.objectMapper = objectMapper;
		this.gatewayFilter = gatewayFilter;
	}

	public void resource(HttpHeaders httpHeaders, HttpServletRequest request, HttpServletResponse response,
			String websiteName, String payload) throws Exception {
		String completeURL = "";
		String initialCompleteURL = "";

		HttpRespData respObj = new HttpRespData();

		try {
			payload = StringUtils.hasText(payload) ? payload : "";
			String composerResourceUrl = getResourceUrl(websiteName);
			if (composerResourceUrl != null) {
				composerReq(httpHeaders, request, response, websiteName, payload);
				return;
			}

			String requestUri = request.getRequestURI();
			String[] url = extractStringsFromUrl(requestUri);

			RouteContext routeContext = getRouteContext(websiteName);

			if (routeContext == null) {

				TPILogger.tl.debugDelay2sec("Website Proxy： [" + websiteName + "] could not find the website route ");
				return;
			}

			String resourceURL = url[1];

			String method = request.getMethod();

			// 概率演算法選擇分流
			RouteContext.DiversionConfig diversionConfig = null;
			List<String[]> datalist = new LinkedList<>();
			routeContext.diversions().forEach(diversion -> {
				String probability = String.valueOf(diversion.probability());
				String id = diversion.diversionId().toString();
				datalist.add(new String[] { probability, id });
			});
			String routing = ProbabilityAlgUtils.getProbabilityAns(datalist);
			diversionConfig = routeContext.diversions().stream()
					.filter(diversion -> diversion.diversionId().toString().equals(routing)).findFirst()
					.orElse(null);

			boolean isShowLog = routeContext.showLog();
			String uuid = UUID.randomUUID().toString();
			if (isShowLog) {
				StringBuffer reqLog = getLogReq(request, httpHeaders, payload);
				TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【Start website】--\n" + reqLog.toString());
			}
			if (diversionConfig != null) {
				String redirectedUrl = diversionConfig.targetUrl();

				completeURL = buildUrl(redirectedUrl, resourceURL);
				initialCompleteURL = completeURL;
				// completeURL = encodeUrl(completeURL);

				Map<String, List<String>> headers = new HashMap<>();

				String fullURL = getFullURL(request);
				String selectedRouteDetailId = diversionConfig.diversionId().toString();
				String selectedRouteId = routeContext.routeId().toString();
				String selectedRouteProbability = String.valueOf(diversionConfig.probability());
				String selectedRouteUrl = diversionConfig.targetUrl();

				String inputAndOut = "========== [input] & [output] ==========" + "\n"
						+ "[input]  Complete requested URL of the website = " + fullURL + "\n"
						+ "[output] Complete redirect URL of the website  = " + completeURL + "\n";

				String routerInfor = "========== [router] The selected route ==========" + "\n"
						+ "[router] detail id = " + selectedRouteDetailId + "\n" + "[router] id = " + selectedRouteId
						+ "\n" + "[router] probability = " + selectedRouteProbability + "\n" + "[router] url = "
						+ selectedRouteUrl + "\n" + "[router] method = " + method + "\n";

				TPILogger.tl.trace(inputAndOut);
				TPILogger.tl.trace(routerInfor);

				headers = getConvertHeader(request, httpHeaders);

				String queryStr = request.getQueryString();
				if (null != queryStr) {
					completeURL += "?" + queryStr;
				}

				// 增加檢查
				if (!this.isIgnorePath(resourceURL, routeContext.ignoreApiPaths())) {
					// 參考GatewayFilter的寫法
					String checkValue = this.getGatewayFilter().getStringBody(request);
					ResponseEntity<?> respEntity = null;
					// SqlInjection
					if (respEntity == null && routeContext.securityConfig().enableSqlInjectionCheck()) {
						boolean isHave = this.getSqlInjectionCheck().check(checkValue, false);
						if (isHave) {
							respEntity = new ResponseEntity<OAuthTokenErrorResp>(
									getCheckErrorResp("Sql Injection", "Invalid parameter", request.getRequestURI(),
											HttpStatus.BAD_REQUEST.value()),
									null, HttpStatus.BAD_REQUEST);

						}
					}

					// xss
					if (respEntity == null && routeContext.securityConfig().enableXssCheck()) {
						boolean isHave = this.getXssCheck().check(checkValue, false);
						if (isHave) {
							respEntity = new ResponseEntity<OAuthTokenErrorResp>(
									getCheckErrorResp("xss", "Invalid parameter", request.getRequestURI(),
											HttpStatus.BAD_REQUEST.value()),
									null, HttpStatus.BAD_REQUEST);
						}
					}

					// xxe
					if (respEntity == null && routeContext.securityConfig().enableXxeCheck()) {
						boolean isHave = this.getXxeCheck().check(checkValue, false);
						if (isHave) {
							respEntity = new ResponseEntity<OAuthTokenErrorResp>(
									getCheckErrorResp("xxe", "Invalid parameter", request.getRequestURI(),
											HttpStatus.BAD_REQUEST.value()),
									null, HttpStatus.BAD_REQUEST);
						}
					}

					// Traffic
					if (respEntity == null && routeContext.trafficConfig().enableTrafficControl()
							&& routeContext.trafficConfig().tps() > 0) {
						List<WebsiteTrafficVo> list = trifficMap.get(routeContext.routeName());
						long timestampSec = System.currentTimeMillis() / 1000;
						WebsiteTrafficVo vo = null;
						if (list != null) {
							vo = list.stream().filter(f -> f.getTargetUrl().equals(redirectedUrl)).findAny()
									.orElse(null);
							if (vo != null) {
								if (vo.getTimestampSec() == timestampSec) {
									vo.setFrequency(vo.getFrequency() + 1);
								} else {
									vo.setTimestampSec(timestampSec);
									vo.setFrequency(1);
								}
							} else {
								vo = new WebsiteTrafficVo();
								vo.setTargetUrl(redirectedUrl);
								vo.setTimestampSec(timestampSec);
								vo.setFrequency(1);
								list.add(vo);
							}
						} else {
							vo = new WebsiteTrafficVo();
							vo.setTargetUrl(redirectedUrl);
							vo.setTimestampSec(timestampSec);
							vo.setFrequency(1);

							list = new ArrayList<>();
							list.add(vo);
							trifficMap.put(routeContext.routeName(), list);
						}

						if (vo.getFrequency() > routeContext.trafficConfig().tps()) {
							respEntity = new ResponseEntity<OAuthTokenErrorResp>(
									getCheckErrorResp("tps", "exceeds TPS limit", request.getRequestURI(),
											HttpStatus.TOO_MANY_REQUESTS.value()),
									null, HttpStatus.TOO_MANY_REQUESTS);
						}
					}

					// Auth
					if (respEntity == null && routeContext.securityConfig().enableAuth()) {// 檢查 ID token
						respEntity = checkAuth(httpHeaders, request, response);
					}

					if (respEntity != null) {
						int status = respEntity.getStatusCode().value();
						Object obj = respEntity.getBody();

						String errMsg = getObjectMapper().writeValueAsString(obj);
						if (errMsg != null) {
							if (isShowLog) {
								TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End website】--\n"
										+ this.getLogResp(respEntity).toString());
							}
							response.setStatus(status);
							ByteArrayInputStream bi = new ByteArrayInputStream(errMsg.getBytes());
							IOUtils.copy(bi, response.getOutputStream());
							return;
						}
					}
				}
				StringBuffer backendLog = new StringBuffer();
				if (isShowLog) {
					backendLog.append("\n--【LOGUUID】【" + uuid + "】【Start website-to-Backend】--");
					backendLog.append("\n--【LOGUUID】【" + uuid + "】【End website-from-Backend】--\n");
				}

				this.setTargetThroughput(websiteName, TYPE_REQ, redirectedUrl);

				if (HttpMethod.GET.name().equalsIgnoreCase(method)) {

					respObj = HttpUtil.httpReqByGetList(completeURL, headers, true, false);

					// 為了解決前端渲染404的問題，所以回目標URL的HTML資料
					if (respObj.statusCode == HttpStatus.NOT_FOUND.value()) {
						respObj = HttpUtil.httpReqByGetList(redirectedUrl, headers, true, false);
					}

				} else if (HttpMethod.POST.name().equalsIgnoreCase(method)) {

					// TPILogger.tl.debug("POST website proxy：[" + websiteName + "] , requestUri = "
					// + requestUri);
					if (request instanceof MultipartHttpServletRequest) {

						// completeURL會有QueryString資料，但multiparFormData方法內部會有處理QueryString資料，這樣會重複QueryString資料。
						// 改傳入initialCompleteURL，initialCompleteURL沒有QueryString資料。
						respObj = multiparFormData(request, httpHeaders, initialCompleteURL);
					} else {
						respObj = HttpUtil.httpReqByRawDataList(completeURL, method, payload, headers, true, false);
					}

				} else if (HttpMethod.DELETE.name().equalsIgnoreCase(method)) {
					// TPILogger.tl.debug("DELETE website proxy：[" + websiteName + "] , requestUri =
					// " + requestUri);
					respObj = HttpUtil.httpReqByRawDataList(completeURL, method, payload, headers, true, false);

				} else if (HttpMethod.PATCH.name().equalsIgnoreCase(method)) {
					// TPILogger.tl.debug("PATCH website proxy：[" + websiteName + "] , requestUri =
					// " + requestUri);
					respObj = HttpUtil.httpReqByRawDataList(completeURL, method, payload, headers, true, false);

				} else if (HttpMethod.PUT.name().equalsIgnoreCase(method)) {
					// TPILogger.tl.debug("PUT website proxy：[" + websiteName + "] , requestUri = "
					// + requestUri);
					respObj = HttpUtil.httpReqByRawDataList(completeURL, method, payload, headers, true, false);

				}
				this.setTargetThroughput(websiteName, TYPE_RESP, redirectedUrl);

				respObj.fetchByte(); // because Enable inputStream

				if (isShowLog) {
					backendLog.append(respObj.getLogStr());
					TPILogger.tl.debug(backendLog.toString());

					// Must call respObj.getLogStr() first
					// Threshhold > 10,000 => print warn msg.
					Optional.ofNullable(respObj.loggerElapsedTimeMsg(uuid, completeURL, respObj.respStr))
							.ifPresent(TPILogger.tl::warn);
					request.setAttribute(GatewayFilter.HTTP_CODE23, respObj.statusCode);
					request.setAttribute(GatewayFilter.ELAPSED_TIME23, respObj.elapsedTime);
				}

				// 將請求完成的header複製一份到response
				response = getConvertResponse(respObj.respHeader, respObj.statusCode, response);
				if (isShowLog) {
					StringBuffer resLog = this.getLogResp(response, respObj.respStr,
							respObj.httpRespArray == null ? 0 : respObj.httpRespArray.length);
					TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End website】--\n" + resLog.toString());
				}

				// 將內容輸出
				// String context = respObj.getLogStr();
				if (respObj.httpRespArray != null) {
					ByteArrayInputStream bi = new ByteArrayInputStream(respObj.httpRespArray);
					IOUtils.copy(bi, response.getOutputStream());
				}
			} else {
				ResponseEntity<?> respEntity = new ResponseEntity<OAuthTokenErrorResp>(
						getCheckErrorResp("websiteName=" + websiteName + ", no diversion available",
								"data not found", request.getRequestURI(), HttpStatus.NOT_FOUND.value()),
						null, HttpStatus.NOT_FOUND);
				int status = respEntity.getStatusCode().value();
				Object obj = respEntity.getBody();

				String errMsg = getObjectMapper().writeValueAsString(obj);
				if (isShowLog) {
					TPILogger.tl.debug(
							"\n--【LOGUUID】【" + uuid + "】【End website】--\n" + this.getLogResp(respEntity).toString());
				}
				response.setStatus(status);
				ByteArrayInputStream bi = new ByteArrayInputStream(errMsg.getBytes());
				IOUtils.copy(bi, response.getOutputStream());
			}
		} catch (Exception e) {
			TPILogger.tl.error("resourceURL=" + completeURL + "\n" + StackTraceUtil.logStackTrace(e));
			throw e;
		}
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

	public StringBuffer getLogResp(HttpServletResponse httpRes, String httpRespStr, int contentLength)
			throws IOException {

		StringBuffer cfp_log = new StringBuffer();

		// print header
		writeLogger(cfp_log, "--【Http Resp Header】--");

		// List<String> headerName =
		// httpRes.getHeaderNames().stream().distinct().collect(Collectors.toList());//移除重複的
		// HeaderName
		List<String> headerName = httpRes.getHeaderNames().stream().collect(Collectors.toList());
		for (String k : headerName) {
			Collection<String> valueList = httpRes.getHeaders(k);
			writeLogger(cfp_log, "\tKey: " + k + ", Value: " + valueList);
		}
		writeLogger(cfp_log, "\tKey: " + "getStatus" + ", Value: " + httpRes.getStatus());
		writeLogger(cfp_log, "\tKey: " + "Content-Length" + ", Value: " + contentLength);
		writeLogger(cfp_log, "\tKey: " + "getCharacterEncoding" + ", Value: " + httpRes.getCharacterEncoding());
		writeLogger(cfp_log, "\tKey: " + "getContentType" + ", Value: " + httpRes.getContentType());
		writeLogger(cfp_log, "\tKey: " + "getLocale" + ", Value: " + httpRes.getLocale());

		writeLogger(cfp_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");

		// print http code
		writeLogger(cfp_log, "--【Http status code】--");
		writeLogger(cfp_log, "--" + httpRes.getStatus());
		writeLogger(cfp_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");

		// print body
		writeLogger(cfp_log, "--【Resp payload / Form Data】");
		writeLogger(cfp_log, httpRespStr);
		writeLogger(cfp_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");

		return cfp_log;
	}

	private StringBuffer getLogResp(ResponseEntity<?> respEntity) {
		StringBuffer log = new StringBuffer();

		// print header
		writeLogger(log, "--【Http Resp Header】--");

		respEntity.getHeaders().forEach((k, vlist) -> {
			vlist.forEach((v) -> {
				writeLogger(log, "\tKey: " + k + ", Value: " + v);
			});
		});

		writeLogger(log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");

		// print http code
		writeLogger(log, "--【Http status code】--");
		writeLogger(log, "--" + respEntity.getStatusCode().toString());
		writeLogger(log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");

		// print body
		writeLogger(log, "--【Resp payload / Form Data】");
		// Object to JSON
		try {
			String json = this.getObjectMapper().writeValueAsString(respEntity.getBody());
			writeLogger(log, json);
		} catch (JsonProcessingException e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
		writeLogger(log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");
		return log;
	}

	private StringBuffer getLogReq(HttpServletRequest httpReq, HttpHeaders httpHeaders, String payload)
			throws IOException, ServletException {
		if (HttpMethod.GET.name().equalsIgnoreCase(httpReq.getMethod())) {
			return getLogReqByGet(httpReq, httpHeaders);
		} else {
			if (httpReq.getContentType().toLowerCase()
					.indexOf(MediaType.APPLICATION_FORM_URLENCODED_VALUE.toLowerCase()) > -1) {
				return getLogReqByUrlEncoded(httpReq, httpHeaders);
			} else if (httpReq.getContentType().toLowerCase()
					.indexOf(MediaType.MULTIPART_FORM_DATA_VALUE.toLowerCase()) > -1) {
				return getLogReqByPostForm(httpReq, httpHeaders);
			} else {
				return getLogReqByPost(httpReq, httpHeaders, payload);
			}
		}
	}

	private StringBuffer getLogReqByGet(HttpServletRequest httpReq, HttpHeaders httpHeaders) throws IOException {

		StringBuffer dgrcGet_log = new StringBuffer();
		String reqUrl = httpReq.getRequestURI();
		String queryStr = httpReq.getQueryString();
		if (queryStr != null) {
			reqUrl += "?" + queryStr;
		}

		// print
		writeLogger(dgrcGet_log, "--【URL】--");
		writeLogger(dgrcGet_log, reqUrl);
		writeLogger(dgrcGet_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");
		writeLogger(dgrcGet_log, "【" + httpReq.getMethod() + "】\r\n");

		// print header
		writeLogger(dgrcGet_log, "--【Http Req Header】--");
		Enumeration<String> headerKeys = httpReq.getHeaderNames();
		while (headerKeys.hasMoreElements()) {
			String key = headerKeys.nextElement();
			List<String> valueList = httpHeaders.get(key);
			writeLogger(dgrcGet_log, "\tKey: " + key + ", Value: " + valueList);
		}
		writeLogger(dgrcGet_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");

		return dgrcGet_log;
	}

	private StringBuffer getLogReqByPost(HttpServletRequest httpReq, HttpHeaders httpHeaders, String payload)
			throws IOException {
		StringBuffer dgrcPostRaw_log = new StringBuffer();

		// print
		writeLogger(dgrcPostRaw_log, "--【URL】--");
		writeLogger(dgrcPostRaw_log, httpReq.getRequestURI());
		writeLogger(dgrcPostRaw_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");
		writeLogger(dgrcPostRaw_log, "【" + httpReq.getMethod() + "】\r\n");

		// print header
		writeLogger(dgrcPostRaw_log, "--【Http Req Header】--");
		Enumeration<String> headerKeys = httpReq.getHeaderNames();
		while (headerKeys.hasMoreElements()) {
			String key = headerKeys.nextElement();
			List<String> valueList = httpHeaders.get(key);
			writeLogger(dgrcPostRaw_log, "\tKey: " + key + ", Value: " + valueList);
		}
		writeLogger(dgrcPostRaw_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");

		if (!HttpMethod.DELETE.name().equalsIgnoreCase(httpReq.getMethod())) {
			// print body
			writeLogger(dgrcPostRaw_log, "--【Req payload / Form Data】");
			writeLogger(dgrcPostRaw_log, payload);
			writeLogger(dgrcPostRaw_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");
		}

		return dgrcPostRaw_log;
	}

	private StringBuffer getLogReqByPostForm(HttpServletRequest httpReq, HttpHeaders httpHeaders)
			throws IOException, ServletException {
		StringBuffer dgrcPostFormLog_log = new StringBuffer();

		// print
		writeLogger(dgrcPostFormLog_log, "--【URL】--");
		writeLogger(dgrcPostFormLog_log, httpReq.getRequestURI());
		writeLogger(dgrcPostFormLog_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");
		writeLogger(dgrcPostFormLog_log, "【" + httpReq.getMethod() + "】\r\n");

		// print header
		writeLogger(dgrcPostFormLog_log, "--【Http Req Header】--");
		Enumeration<String> headerKeys = httpReq.getHeaderNames();
		while (headerKeys.hasMoreElements()) {
			String key = headerKeys.nextElement();
			List<String> valueList = httpHeaders.get(key);
			writeLogger(dgrcPostFormLog_log, "\tKey: " + key + ", Value: " + valueList);
		}
		writeLogger(dgrcPostFormLog_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");

		// print body
		writeLogger(dgrcPostFormLog_log, "--【Req payload / Form Data】");
		Collection<Part> parts;

		parts = httpReq.getParts();
		for (Part part : parts) {
			String name = part.getName();
			String contentType = part.getContentType();
			String value = httpReq.getParameter(name);
			writeLogger(dgrcPostFormLog_log,
					"\tKey: " + name + ", Value: " + value + ", Content-Type: " + contentType);
		}

		writeLogger(dgrcPostFormLog_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");

		return dgrcPostFormLog_log;
	}

	private StringBuffer getLogReqByUrlEncoded(HttpServletRequest httpReq, HttpHeaders httpHeaders) throws IOException {
		StringBuffer dgrcUrlEncoded_log = new StringBuffer();

		// print
		writeLogger(dgrcUrlEncoded_log, "--【URL】--");
		writeLogger(dgrcUrlEncoded_log, httpReq.getRequestURI());
		writeLogger(dgrcUrlEncoded_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");
		writeLogger(dgrcUrlEncoded_log, "【" + httpReq.getMethod() + "】\r\n");

		// print header
		writeLogger(dgrcUrlEncoded_log, "--【Http Req Header】--");
		Enumeration<String> headerKeys = httpReq.getHeaderNames();
		while (headerKeys.hasMoreElements()) {
			String key = headerKeys.nextElement();
			List<String> valueList = httpHeaders.get(key);
			writeLogger(dgrcUrlEncoded_log, "\tKey: " + key + ", Value: " + valueList);
		}
		writeLogger(dgrcUrlEncoded_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");

		// print body
		writeLogger(dgrcUrlEncoded_log, "--【Req payload / Form Data】");
		httpReq.getParameterMap().forEach((k, vs) -> {
			if (vs.length != 0) {
				for (String v : vs) {
					writeLogger(dgrcUrlEncoded_log, "\tKey: " + k + ", Value: " + v);
				}
			}
		});
		writeLogger(dgrcUrlEncoded_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");

		return dgrcUrlEncoded_log;
	}

	private void writeLogger(StringBuffer log, String msg) {
		msg += "\n";
		log.append("\n" + msg);
	}

	/**
	 * check Auth, 驗證 ID token
	 */
	private ResponseEntity<?> checkAuth(HttpHeaders httpHeaders, HttpServletRequest request,
			HttpServletResponse response)
			throws Exception {
		String reqUrl = request.getRequestURL().toString();
		ResponseEntity<?> respEntity = verifyIdToken(httpHeaders, response, reqUrl);
		if (respEntity != null && !(respEntity.getBody() instanceof OAuthTokenErrorResp)
				&& !(respEntity.getBody() instanceof OAuthTokenErrorResp2)) {
			return null;
		}
		return respEntity;
	}

	/**
	 * 驗證 ID token
	 */
	private ResponseEntity<?> verifyIdToken(HttpHeaders httpHeaders, HttpServletResponse response, String reqUri)
			throws Exception {
		String authorization = Optional.ofNullable(httpHeaders.get("Authorization")).map(az -> az.get(0)).orElse("");

		// 是否有 Authorization
		ResponseEntity<?> respEntity = getTokenHelper().checkHasAuthorization(authorization, reqUri);
		if (respEntity != null) {
			return respEntity;
		}

		// 是否有"bearer "字樣,忽略大小寫
		boolean hasBearer = getTokenHelper().checkHasKeyword(authorization, TokenHelper.BEARER);
		if (hasBearer == false) {// 沒有字樣
			String errMsg = TokenHelper.UNAUTHORIZED;
			TPILogger.tl.debug(errMsg);
			return getTokenHelper().getUnauthorizedErrorResp(reqUri, errMsg);
		}

		// 取得 ID token
		String idTokenJwtstr = authorization.substring(TokenHelper.BEARER.length());

		// 驗 ID token 簽章和到期日
		respEntity = getGtwIdPVerifyService().verify(idTokenJwtstr, null, null);
		if (respEntity != null) {
			return respEntity;
		}

		return null;
	}

	private HttpRespData multiparFormData(HttpServletRequest request, HttpHeaders httpHeaders, String reqUrl)
			throws Exception {

		if (request instanceof MultipartHttpServletRequest) {
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

			List<byte[]> formBodyParts = new ArrayList<>();
			List<byte[]> formBodyParts_File2Hex = new ArrayList<>();
			Collection<Part> parts = multipartRequest.getParts();
			Map<String, String> partContentTypes = new HashMap<>();

			final String boundary = parseBoundary(multipartRequest);
			StringBuffer dgrcPostFormForward_log = new StringBuffer();

			// 將每個Part的ContentType保存到Map中
			for (Part part : parts) {
				partContentTypes.put(part.getName(), part.getContentType());
			}
			// 文字
			Map<String, String[]> parameterMap = multipartRequest.getParameterMap();

			if (!CollectionUtils.isEmpty(parameterMap)) {
				String dgrcPostForm_name;
				String[] vals;
				byte[] data;

				for (Map.Entry<String, String[]> entries : parameterMap.entrySet()) {
					dgrcPostForm_name = entries.getKey();
					vals = entries.getValue();
					for (String val : vals) {
						data = (byte[]) HttpUtil.getFormBodyPart(dgrcPostForm_name, null, val.getBytes(), boundary,
								dgrcPostFormForward_log, partContentTypes.get(dgrcPostForm_name)).get("data");
						formBodyParts.add(data);
						formBodyParts_File2Hex.add(data);
					}
				}
			}
			// 檔案
			Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
			if (!CollectionUtils.isEmpty(fileMap)) {
				String dgrcPostForm_name;
				MultipartFile mf;
				byte[] data;
				for (Map.Entry<String, MultipartFile> entries : fileMap.entrySet()) {
					dgrcPostForm_name = entries.getKey();
					mf = entries.getValue();
					// String contentType = mf.getContentType(); // Get the content type from the
					// MultipartFile
					Map<String, Object> dataMap = HttpUtil.getFormBodyPart(dgrcPostForm_name, mf.getOriginalFilename(),
							mf.getBytes(), boundary, dgrcPostFormForward_log, partContentTypes.get(dgrcPostForm_name));
					data = (byte[]) dataMap.get("data");
					formBodyParts.add(data);

					Map<String, Object> logData = (Map<String, Object>) dataMap.get("logData");
					// byte[] hexData = ("\r\n"+ HttpUtil.PREFIX_Sha256_Hex +
					// HexStringUtils.toString(SHA256Util.getSHA256((byte[])logData.get("content")))).getBytes()
					// ;

					if (logData != null) {
						formBodyParts_File2Hex.add((byte[]) logData.get("contentD"));
						formBodyParts_File2Hex.add((byte[]) logData.get("content"));
					}
				}
			}

			byte[] formBody = HttpUtil.getFormBody(formBodyParts, boundary, partContentTypes);
			byte[] formBody2Hex = HttpUtil.getFormBody(formBodyParts_File2Hex, boundary, partContentTypes);

			Map<String, List<String>> convertedHeaders = new HashMap<>();
			httpHeaders.forEach((key, value) -> convertedHeaders.put(key, value));

			boolean isEnableInputStream = true;
			boolean dgrcPostForm_isRedirect = false;

			return HttpUtil.httpReqByFormDataList( //
					reqUrl, request.getMethod(), boundary, formBody, formBody2Hex, convertedHeaders,
					isEnableInputStream, dgrcPostForm_isRedirect);

		} else {
			return new HttpRespData();
		}
	}

	private String parseBoundary(MultipartHttpServletRequest multipartReq) {
		HttpHeaders httpHeaders = multipartReq.getRequestHeaders();
		MediaType contentType = httpHeaders.getContentType();
		String dgrcPostForm_boundary;
		if (contentType == null) {
			// not exceed 70 bytes in length and consists only of 7-bit US-ASCII (printable)
			// characters
			dgrcPostForm_boundary = "DgrcBoundary" + UUID.randomUUID().toString();
		} else {
			dgrcPostForm_boundary = contentType.getParameter("boundary");
		}
		return dgrcPostForm_boundary;
	}

	private void composerReq(HttpHeaders httpHeaders, HttpServletRequest request, HttpServletResponse response,
			String websiteName, String payload) throws Exception {

		String resourceURL = null;

		String uri = request.getRequestURI();

		resourceURL = getResourceUrl(websiteName);

		// checkmarx, ReDoS From Regex Injection,把replaceFirst改為replace, 已通過中風險
		// 替換成目標網址
		resourceURL = resourceURL + uri.replace("/website/" + websiteName + "/", "");

		Enumeration<String> httpHeaderKeys = request.getHeaderNames();

		Map<String, List<String>> headers = new HashMap<>();
		while (httpHeaderKeys.hasMoreElements()) {
			String key = httpHeaderKeys.nextElement();
			List<String> valueList = httpHeaders.get(key);
			headers.put(key, valueList);

		}

		// composer的特別加工,若URI後面為UUID,轉向目標是/editor/tsmpApi/,並加上x-forwarded-for,因為會call
		// DPB0143檢查IP
		if (websiteName.equals("composer")) {
			try {

				if (request.getCookies() == null) {
					response.sendError(HttpServletResponse.SC_FORBIDDEN, "No cookies found");
					return;
				}

				Optional<Cookie> expireKeyCookie = Arrays.stream(request.getCookies())
						.filter(c -> EXPIRE_KEY.equals(c.getName()))
						.findFirst();

				boolean hasValidReferer = Optional.ofNullable(httpHeaders.get(HttpHeaders.REFERER))
						.flatMap(refs -> refs.stream().findFirst())
						.map(ref -> ref.contains("dgrv4") || ref.contains("/website/composer"))
						.orElse(false);

				boolean isValidRequest = expireKeyCookie
						.map(cookie -> {
							try {
								return ExpireKeyUtil.verifyExpireKey(cookie.getValue()) || hasValidReferer;
							} catch (Exception e) {
								TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
								return false;
							}
						})
						.orElse(hasValidReferer);

				if (!isValidRequest) {
					response.sendError(HttpServletResponse.SC_FORBIDDEN);
					return;
				}
				if (response.isCommitted()) {
					return; // 權限失敗已回應，直接結束
				}
			} catch (Exception e) {
				TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
				throw TsmpDpAaRtnCode._1297.throwing();
			}
			resourceURL = resourceURL + "?" + request.getQueryString();
			String[] arrUri = uri.split("/");
			String lastPath = arrUri[arrUri.length - 1];
			if (lastPath.split("-").length == 5) {
				resourceURL = resourceURL.replace("/editor/", "/editor/tsmpApi/");

				List<String> valueList = null;
				if (headers.get("x-forwarded-for") != null) {
					valueList = headers.get("x-forwarded-for");
				} else {
					valueList = new ArrayList<>();
				}
				String ipAddress = ClientIpUtil.getClientIp(request) ;
//                ServiceUtil.getIpAddress(request);
				if (!valueList.contains(ipAddress)) {
					valueList.add(ipAddress);
					headers.put("x-forwarded-for", valueList);
				}
			}

		}

		String method = request.getMethod();
		HttpRespData respObj = null;

		// 請求website URL
		// TPILogger.tl.debugDelay2sec("website url = " + resourceURL);

		if (method.equalsIgnoreCase("GET")) {
			respObj = HttpUtil.httpReqByGetList(resourceURL, headers, true, false);
		} else if (method.equalsIgnoreCase("POST")) {
			respObj = HttpUtil.httpReqByRawDataList(resourceURL, "POST", payload, headers, true, false);

		} else {
			TPILogger.tl.error("unknown http method is " + method);
			throw TsmpDpAaRtnCode._1297.throwing();
		}

		respObj.fetchByte(); // because Enable inputStream

		// composer的特別加工,若為http status 302,Location改寫為/website/composer,若他進入proxy
		// controller
		if (websiteName.equals("composer")) {
			Map<String, List<String>> headerMap = respObj.respHeader;
			if (!CollectionUtils.isEmpty(headerMap.get("Location"))) {
				String str = headerMap.get("Location").get(0);
				if (str.length() > 11 && "/editor/?id=".equals(str.substring(0, 12))) {
					String id = str.substring(12);
					str = "/website/composer/?id=" + id;
					List<String> list = new ArrayList<>();
					list.add(str);
					headerMap.put("Location", list);
				}
			}
		}

		// 將請求完成的header複製一份到response
		response = getConvertResponse(respObj.respHeader, respObj.statusCode, response);

		// 將內容輸出
		// String context = respObj.getLogStr();
		if (respObj.httpRespArray != null) {
			ByteArrayInputStream bi = new ByteArrayInputStream(respObj.httpRespArray);
			IOUtils.copy(bi, response.getOutputStream());
		}
	}

	public HttpServletResponse getConvertResponse(Map<String, List<String>> respHeader, int status,
			HttpServletResponse httpRes) {

		httpRes.setStatus(status);
		// checkmarx, Missing HSTS Header
		httpRes.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
		respHeader.forEach((k, vs) -> {
			vs.forEach((v) -> {
				if (k != null) {
					if (!k.equalsIgnoreCase("Transfer-Encoding")) {
						httpRes.addHeader(k, v);
					}
				}
			});
		});
		return httpRes;
	}

	/**
	 * Get the resource URL based on the provided name.
	 * 
	 * @param name The name of the resource to fetch the URL for.
	 * @return The resource URL as a String, or null if not found.
	 */
	private String getResourceUrl(String name) {
		// Use a switch statement to improve readability and scalability
		switch (name) {
			case "composer":
			case "httpApiComposer":
				return getComposerResourceUrl(name);
			// Add more cases here if needed
			default:
				return null;
		}
	}

	/**
	 * Get the composer resource URL based on the provided name.
	 * 
	 * @param name The name of the composer resource to fetch the URL for.
	 * @return The composer resource URL as a String, or null if not found.
	 */
	private String getComposerResourceUrl(String name) {
		// Retrieve the list of TSMP_COMPOSER_ADDRESS values
		List<String> caList = getTsmpSettingService().getVal_TSMP_COMPOSER_ADDRESS();

		// Use Optional to avoid nulls and improve null safety
		Optional<String> composerAddress = caList.isEmpty() ? Optional.empty() : Optional.of(caList.get(0));

		// Map the composer address to the desired resource URL
		return composerAddress.map(address -> {
			// If the name is "composer", append "/editor/" to the address
			if ("composer".equals(name)) {
				return address + "/editor/";
			} else {
				// If the name is "httpApiComposer", return the address as-is
				return address;
			}
			// If the Optional is empty (i.e., the composer address is not found), return
			// null
		}).orElse(null);
	}

	/**
	 * 從 URL 中提取路徑資訊
	 * 提取 "website" 前綴後的第一個片段(websiteName)和剩餘路徑(resourceURL)
	 *
	 * @param str 輸入的 URL 字串
	 * @return 包含兩個元素的陣列: [0] = websiteName, [1] = resourceURL (帶前綴 "/")
	 *         例如: "/website/mysite/api/users" -> ["mysite", "/api/users"]
	 *         例如: "/website/mysite" -> ["mysite", ""]
	 */
	private String[] extractStringsFromUrl(String str) {
	    if (str == null || str.isEmpty()) {
	        return new String[] { "", "" };
	    }

	    String pathPrefix = "website";

	    // 將 URL 按 "/" 分割
	    String[] parts = str.split("/");

	    // 尋找路徑前綴在陣列中的位置
	    int prefixIndex = -1;
	    for (int i = 0; i < parts.length; i++) {
	        if (parts[i].equals(pathPrefix)) {
	            prefixIndex = i;
	            break;
	        }
	    }

	    // 如果找不到路徑前綴,或路徑前綴是最後一個元素,返回空字串陣列
	    if (prefixIndex == -1 || prefixIndex + 1 >= parts.length) {
	        return new String[] { "", "" };
	    }

	    // 提取路徑前綴後的第一個片段 (websiteName)
	    String firstSegmentAfterPrefix = parts[prefixIndex + 1];

	    // 提取剩餘的路徑片段,並用 "/" 連接 (resourceURL)
	    String remainingSegmentsAfterPrefix = String.join("/",
	            Arrays.copyOfRange(parts, prefixIndex + 2, parts.length));

	    // 確保 resourceURL 有前綴 "/"，但如果是空字串則保持空字串
	    if (!remainingSegmentsAfterPrefix.isEmpty()) {
	        remainingSegmentsAfterPrefix = "/" + remainingSegmentsAfterPrefix;
	    }

	    return new String[] { firstSegmentAfterPrefix, remainingSegmentsAfterPrefix };
	}



	private String buildUrl(String url, String resourceUrl) {
	    String result = url + resourceUrl;
	    return result;
	}


	private String getFullURL(HttpServletRequest request) {
		String scheme = request.getScheme(); // http or https
		String serverName = request.getServerName(); // hostname or IP address
		int serverPort = request.getServerPort(); // port number
		String contextPath = request.getContextPath(); // context path if any
		String servletPath = request.getServletPath(); // servlet path
		String pathInfo = request.getPathInfo(); // path info if any
		String queryString = request.getQueryString(); // query string if any

		// Build the complete URL
		StringBuilder url = new StringBuilder();
		url.append(scheme).append("://").append(serverName);

		if (serverPort != 80 && serverPort != 443) {
			url.append(":").append(serverPort);
		}

		url.append(contextPath).append(servletPath);

		if (pathInfo != null) {
			url.append(pathInfo);
		}

		if (queryString != null) {
			url.append("?").append(queryString);
		}

		return url.toString();
	}

	private boolean isIgnorePath(String resourceUrl, String ignoreApiPath) {
		if (StringUtils.hasText(ignoreApiPath)) {
			String[] arrIgnoreApiPath = ignoreApiPath.split(",");
			for (String path : arrIgnoreApiPath) {
				if (path.indexOf("**") > -1) {
					path = path.replaceAll("\\*\\*", ".*");
					boolean match = CheckmarxCommUtils.sanitizeForCheckmarxMatches(resourceUrl, path);
					if (match) {
						return true;
					}
				} else if (path.equals(resourceUrl)) {
					return true;
				}
			}
		}

		return false;
	}

	protected TsmpSettingCacheProxy getTsmpSettingCacheProxy() {
		return tsmpSettingCacheProxy;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected DgrWebsiteCacheProxy getDgrWebsiteCacheProxy() {
		return dgrWebsiteCacheProxy;
	}

	protected DgrWebsiteDetailCacheProxy getDgrWebsiteDetailCacheProxy() {
		return dgrWebsiteDetailCacheProxy;
	}

	protected GtwIdPVerifyService getGtwIdPVerifyService() {
		return gtwIdPVerifyService;
	}

	protected TokenHelper getTokenHelper() {
		return tokenHelper;
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

	protected ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	protected GatewayFilter getGatewayFilter() {
		return gatewayFilter;
	}

	public Map<String, List<String>> getConvertHeader(HttpServletRequest httpReq, HttpHeaders httpHeaders) {

		Enumeration<String> httpHeaderKeys = httpReq.getHeaderNames();
		Map<String, List<String>> headers = new HashMap<>();

		// 1.取得原本 header 的值
		while (httpHeaderKeys.hasMoreElements()) {
			String key = httpHeaderKeys.nextElement();
			List<String> headerValueList = httpHeaders.get(key);

			if (!"If-Modified-Since".equalsIgnoreCase(key) && !"If-None-Match".equalsIgnoreCase(key)) {
				headers.put(key, headerValueList);
			}
		}

		return headers;
	}

	private void setTargetThroughput(String websiteName, String type, String targetUrl) {
		// 獲取目前秒數
		long currentTimeMillis = System.currentTimeMillis();
		long timestampSec = currentTimeMillis / 1000;

		try {
			Map<String, Map<String, Map<String, Integer>>> timestampSecMap = targetThroughputMap.get(timestampSec);
			if (timestampSecMap != null) {
				Map<String, Map<String, Integer>> websiteNameMap = timestampSecMap.get(websiteName);
				if (websiteNameMap != null) {
					Map<String, Integer> targetUrlMap = websiteNameMap.get(targetUrl);
					if (targetUrlMap != null) {
						if (targetUrlMap.get(type) != null) {
							targetUrlMap.put(type, targetUrlMap.get(type) + 1);
						} else {
							targetUrlMap.put(type, 1);
						}
					} else {
						targetUrlMap = new HashMap<String, Integer>();
						targetUrlMap.put(type, 1);
						websiteNameMap.put(targetUrl, targetUrlMap);
					}
				} else {
					Map<String, Integer> targetUrlMap = new HashMap<String, Integer>();
					targetUrlMap.put(type, 1);
					websiteNameMap = new HashMap<String, Map<String, Integer>>();
					websiteNameMap.put(targetUrl, targetUrlMap);
					timestampSecMap.put(websiteName, websiteNameMap);
				}
			} else {
				Map<String, Integer> targetUrlMap = new HashMap<String, Integer>();
				targetUrlMap.put(type, 1);
				Map<String, Map<String, Integer>> websiteNameMap = new HashMap<String, Map<String, Integer>>();
				websiteNameMap.put(targetUrl, targetUrlMap);
				timestampSecMap = new HashMap<String, Map<String, Map<String, Integer>>>();
				timestampSecMap.put(websiteName, websiteNameMap);
				synchronized (targetThroughputMap) {
					targetThroughputMap.put(timestampSec, timestampSecMap);
				}

			}
			removeKey();
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}

	}

	private void removeKey() {
		try {

			if (targetThroughputMap != null && targetThroughputMap.size() > 20) {
				// 保留的最大秒數
				long keepMaxSec = (System.currentTimeMillis() / 1000) - 5;
				List<Long> secKeyList = new ArrayList<>(targetThroughputMap.keySet());
				for (int i = 0; i < secKeyList.size(); i++) {
					long secKey = secKeyList.get(i);
					if (secKey <= keepMaxSec) {
						synchronized (targetThroughputMap) {
							targetThroughputMap.remove(secKey);
						}
					}
				}
			}
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
	}

	/**
	 * Website Proxy 路由上下文
	 *
	 * @param routeId        路由主表 ID
	 * @param routeName      路由名稱
	 * @param isEnabled      是否啟用
	 * @param remark         備註
	 * @param securityConfig 安全檢查配置
	 * @param trafficConfig  流量控制配置
	 * @param diversions     分流設定列表
	 * @param ignoreApiPaths 忽略檢查的 API 路徑
	 * @param showLog        是否顯示日誌
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
			boolean showLog) {

		/**
		 * 安全檢查配置
		 *
		 * @param enableAuth              是否啟用身份驗證
		 * @param enableSqlInjectionCheck 是否啟用 SQL Injection 檢查
		 * @param enableXssCheck          是否啟用 XSS 檢查
		 * @param enableXxeCheck          是否啟用 XXE 檢查
		 */
		@Builder
		public record SecurityConfig(
				boolean enableAuth,
				boolean enableSqlInjectionCheck,
				boolean enableXssCheck,
				boolean enableXxeCheck) {

			public static SecurityConfig fromWebsite(DgrWebsite website) {
				return SecurityConfig.builder()
						.enableAuth("Y".equals(website.getAuth()))
						.enableSqlInjectionCheck("Y".equals(website.getSqlInjection()))
						.enableXssCheck("Y".equals(website.getXss()))
						.enableXxeCheck("Y".equals(website.getXxe()))
						.build();
			}
		}

		/**
		 * 流量控制配置
		 *
		 * @param enableTrafficControl 是否啟用流量控制
		 * @param tps                  每秒交易數限制 (Transactions Per Second)
		 */
		@Builder
		public record TrafficConfig(
				boolean enableTrafficControl,
				int tps) {

			public static TrafficConfig fromWebsite(DgrWebsite website) {
				return TrafficConfig.builder()
						.enableTrafficControl("Y".equals(website.getTraffic()))
						.tps(website.getTps() != null ? website.getTps() : 0)
						.build();
			}
		}

		/**
		 * 分流配置
		 *
		 * @param diversionId 分流 ID
		 * @param probability 分流機率 (權重)
		 * @param targetUrl   目標 URL
		 */
		@Builder
		public record DiversionConfig(
				Long diversionId,
				int probability,
				String targetUrl) {

			public static DiversionConfig fromWebsiteDetail(DgrWebsiteDetail detail) {
				return DiversionConfig.builder()
						.diversionId(detail.getDgrWebsiteDetailId())
						.probability(detail.getProbability())
						.targetUrl(detail.getUrl())
						.build();
			}
		}

		/**
		 * 從 DgrWebsite 和 DgrWebsiteDetail 列表建立 RouteContext
		 */
		public static RouteContext fromWebsite(DgrWebsite website, List<DgrWebsiteDetail> details) {
			return RouteContext.builder()
					.routeId(website.getDgrWebsiteId())
					.routeName(website.getWebsiteName())
					.isEnabled("Y".equals(website.getWebsiteStatus()))
					.remark(website.getRemark())
					.securityConfig(SecurityConfig.fromWebsite(website))
					.trafficConfig(TrafficConfig.fromWebsite(website))
					.diversions(details.stream()
							.map(DiversionConfig::fromWebsiteDetail)
							.toList())
					.ignoreApiPaths(website.getIgnoreApi())
					.showLog("Y".equals(website.getShowLog()))
					.build();
		}
	}

	private RouteContext getRouteContext(String websiteName) {
		List<DgrWebsite> websiteList = getDgrWebsiteCacheProxy()
				.findByWebsiteNameAndWebsiteStatus(websiteName, "Y");

		if (websiteList == null || websiteList.isEmpty()) {
			return null;
		}

		DgrWebsite website = websiteList.get(0);

		List<DgrWebsiteDetail> websiteDetailList = getDgrWebsiteDetailCacheProxy()
				.findByDgrWebsiteId(website.getDgrWebsiteId());

		return RouteContext.fromWebsite(website, websiteDetailList);
	}

}
