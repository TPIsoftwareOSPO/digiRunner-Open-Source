package tpi.dgrv4.gateway.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.codec.utils.CApiKeyUtils;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.jpql.TsmpReportUrl;
import tpi.dgrv4.entity.exceptions.DgrRtnCode;
import tpi.dgrv4.entity.repository.TsmpReportUrlDao;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.component.authorization.TsmpAuthorizationParser;
import tpi.dgrv4.gateway.component.authorization.TsmpAuthorizationParserFactory;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.TsmpSettingService;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

@RestController
public class DgrCusController {

	public final static String KEY_PATH = "/dgrv4/cus";

	public final static String KEY_OF_TOKEN_COOKIE = "_authorization";

	public final static String KEY_OF_RID_COOKIE = "_embedded_link_reportId";

	private TsmpReportUrlDao tsmpReportUrlDao;
	private TsmpSettingService tsmpSettingService;

	@Autowired
	public DgrCusController(TsmpReportUrlDao tsmpReportUrlDao, TsmpSettingService tsmpSettingService) {
		super();
		this.tsmpReportUrlDao = tsmpReportUrlDao;
		this.tsmpSettingService = tsmpSettingService;
	}

	@SuppressWarnings("java:S3752") // allow all methods for sonarqube scan
	@RequestMapping( //
			value = { "/dgrv4/cus/{cusAppCode}/noauth/login", //
					"/dgrv4/cus/{cusAppCode}/noauth/login/{param1}", //
					"/dgrv4/cus/{cusAppCode}/noauth/login/{param1}/{param2}" }, //
			method = { RequestMethod.GET, RequestMethod.POST })
	public void resource( //
			@PathVariable("cusAppCode") String cusAppCode, // 客製包代碼
			@PathVariable(required = false, name = "param1") String param1, //
			@PathVariable(required = false, name = "param2") String param2, //
			@RequestBody(required = false) String payload, //
			@RequestHeader HttpHeaders httpHeaders, //
			HttpServletRequest request, HttpServletResponse response) throws Throwable {

		try {

			String loginURL = getTsmpSettingService().getVal_CUS_LOGIN_URL();
			loginURL = addParam(loginURL, param1);
			loginURL = addParam(loginURL, param2);

			String queryString = request.getQueryString();
			loginURL = UriComponentsBuilder.fromUriString(loginURL).query(queryString).build().toUriString();

			Map<String, List<String>> headers = httpHeaders.toSingleValueMap().entrySet().stream().map(entry -> {
				return Map.entry(entry.getKey(), List.of(entry.getValue()));
			}).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

			HttpRespData respData = null;

			if (RequestMethod.GET.name().equalsIgnoreCase(request.getMethod())) {
				respData = HttpUtil.httpReqByGetList(loginURL, headers, true, false);
			} else {
				respData = HttpUtil.httpReqByRawDataList(loginURL, "POST", payload, headers, true, false);
			}

			respData.fetchByte(); // Because inputStream is enabled

			response.setStatus(respData.statusCode);
			if (!CollectionUtils.isEmpty(respData.respHeader)) {
				respData.respHeader.forEach((k, vs) -> {
					vs.forEach((v) -> {
						if (k != null) {
							if (!k.equalsIgnoreCase(HttpHeaders.TRANSFER_ENCODING)) {
								response.addHeader(k, v);
							}
						}
					});
				});
			}
			
			//checkmarx, Missing HSTS Header
			response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload"); 

			if (respData.statusCode != 302 && (respData.httpRespArray == null || respData.httpRespArray.length == 0)) {
				sendError(response, HttpStatus.NOT_FOUND,
						"No data responded from " + loginURL + "\n" + respData.getLogStr());
				return;
			}

			ByteArrayInputStream bi = new ByteArrayInputStream(respData.httpRespArray);
			IOUtils.copy(bi, response.getOutputStream());

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			sendError(response, HttpStatus.INTERNAL_SERVER_ERROR, StackTraceUtil.logStackTrace(e));
		}
	}

	private String addParam(String loginURL, String param) {
		try {

			if (!StringUtils.hasText(loginURL)) {
				return loginURL;
			}

			if (!StringUtils.hasText(param)) {
				return loginURL;
			}

			URI uri = new URI(loginURL);
			String path = uri.getPath();

			// 確保路徑以 '/' 結尾，參數不以 '/' 開頭
			if (!path.endsWith("/")) {
				path += "/";
			}
			if (param.startsWith("/")) {
				param = param.substring(1);
			}

			// 組合新的路徑
			String newPath = path + param;

			// 構建新的 URI
			URI newUri = new URI(uri.getScheme(), uri.getAuthority(), newPath, uri.getQuery(), uri.getFragment());

			return newUri.toString();
		} catch (URISyntaxException e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			return loginURL;
		}
	}

	@SuppressWarnings("java:S3752") // allow all methods for sonarqube scan
	@RequestMapping("/dgrv4/cus/{cusAppCode}/**")
	public void resource(@PathVariable("cusAppCode") String cusAppCode, // 客製包代碼
			@RequestHeader HttpHeaders httpHeaders,
			@CookieValue(value = KEY_OF_TOKEN_COOKIE, required = false) String authorization,
			@CookieValue(value = KEY_OF_RID_COOKIE, required = false) String reportId, // 功能(報表)代碼
			@RequestBody(required = false) String payload, HttpServletRequest request, HttpServletResponse response)
			throws Throwable {
		if (!StringUtils.hasLength(authorization)) {
			TPILogger.tl.error(TokenHelper.INVALID_TOKEN);
			sendError(response, HttpStatus.UNAUTHORIZED, (TokenHelper.INVALID_TOKEN));
			return;
		}
		if (!StringUtils.hasLength(reportId)) {
			TPILogger.tl.error(TokenHelper.INVALID_REQUEST + ":reportId is null");
			sendError(response, HttpStatus.BAD_REQUEST, (TokenHelper.INVALID_REQUEST + ":reportId is null"));
			return;
		}

		// Check if token was expired
		boolean isValid = checkAccessTokenExp(authorization, response);
		if (!isValid) {
			return;
		}

		try {
			String targetPath = getTargetPath(request, cusAppCode);
			String targetCompleteUrl = getTargetCompleteUrl(reportId, targetPath);

			TPILogger.tl.debug(String.format("%s → %s", request.getRequestURL(), targetCompleteUrl));

//			Map<String, List<String>> headers = httpHeaders.toSingleValueMap().entrySet().stream().map(entry -> {
//				return Map.entry(entry.getKey(), List.of(entry.getValue()));
//			}).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

			Map<String, List<String>> headers = new HashMap<>();
			for (Entry<String, List<String>> entry : httpHeaders.entrySet()) {
				if (HttpHeaders.COOKIE.toUpperCase().equals(entry.getKey().toUpperCase())) {
					String cookieStr = String.join(";", entry.getValue());
					headers.put(entry.getKey(), List.of(cookieStr));
				} else {
					headers.put(entry.getKey(), entry.getValue());
				}

			}

			// cApiKey
			addCApiKey(headers);

			HttpRespData respData;
			if ("GET".equals(request.getMethod())) {
				respData = HttpUtil.httpReqByGetList(targetCompleteUrl, headers, true, false);
			} else {
				respData = HttpUtil.httpReqByRawDataList(targetCompleteUrl, "POST", payload, headers, true, false);
			}

			respData.fetchByte(); // Because inputStream is enabled

			response.setStatus(respData.statusCode);
			if (!CollectionUtils.isEmpty(respData.respHeader)) {
				respData.respHeader.forEach((k, vs) -> {
					vs.forEach((v) -> {
						if (k != null) {
							if (!k.equalsIgnoreCase(HttpHeaders.TRANSFER_ENCODING)) {
								if (!"If-Modified-Since".equalsIgnoreCase(k) && !"If-None-Match".equalsIgnoreCase(k)) {
									// 20240808因客製包引用spring-boot-starter-security所以addHeader改為setHeader,否則會有相同的key
									response.setHeader(k, v);
								}
							}
						}
					});
				});
			}
			
			//Webber 2024-10-15 停用快取機制,不然會發生304問題
			response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Expires", "0");
			//checkmarx, Missing HSTS Header
			response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload"); 
			if (respData.httpRespArray == null || respData.httpRespArray.length == 0) {
				sendError(response, HttpStatus.NOT_FOUND,
						"No data responded from " + targetCompleteUrl + "\n" + respData.getLogStr());
				return;
			}

			ByteArrayInputStream bi = new ByteArrayInputStream(respData.httpRespArray);
			IOUtils.copy(bi, response.getOutputStream());
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			sendError(response, HttpStatus.INTERNAL_SERVER_ERROR, StackTraceUtil.logStackTrace(e));
		}
	}

	private boolean checkAccessTokenExp(String authorization, HttpServletResponse response) throws Throwable {
		authorization = new String(Base64.getDecoder().decode(authorization), StandardCharsets.UTF_8);
		try {
			TsmpAuthorizationParser authorizationParser = new TsmpAuthorizationParserFactory().getParser(authorization);
			TsmpAuthorization auth = authorizationParser.parse();
			Long exp = auth.getExp();

			if (exp == null || exp.equals(0L)) {
				sendError(response, HttpStatus.BAD_REQUEST, "Invalid value of exp of access token");
				return false;
			}

			// access token 過期
			long nowTime = System.currentTimeMillis() / 1000;// 去掉亳秒
			if (exp < nowTime) {
				sendError(response, HttpStatus.UNAUTHORIZED, (TokenHelper.ACCESS_TOKEN_EXPIRED + exp));
				return false;
			}

			return true;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			sendError(response, HttpStatus.INTERNAL_SERVER_ERROR, StackTraceUtil.logStackTrace(e));
		}
		return false;
	}

	private String getTargetPath(HttpServletRequest request, String cusAppCode) {
		String requestUri = request.getRequestURI();
		
		//checkmarx, ReDoS From Regex Injection, 已通過中風險
//		Pattern p = Pattern.compile("^" + KEY_PATH + "/" + cusAppCode + "(.*)$");
//		Matcher m = p.matcher(requestUri);
//		if (m.matches()) {
//			requestUri = m.group(1);
//		}
		int index = requestUri.indexOf(KEY_PATH) + KEY_PATH.length() + 1 + cusAppCode.length();
		String targetPath = requestUri.substring(index);
		
		String queryStr = request.getQueryString();
		if (StringUtils.hasLength(queryStr)) {
			targetPath += "?" + queryStr;
		}
		return targetPath;
	}

	private String getTargetCompleteUrl(String reportId, String targetPath) {
		String targetBaseUrl = getTsmpReportUrlDao().findByReportId(reportId).stream().map(TsmpReportUrl::getReportUrl)
				.findAny().orElseThrow(DgrRtnCode._1241::throwing);

		// 若有指定路徑, 則移除目標位址的最後一段路徑, 剩下的當作 basePath, 再串上 targetPath
		// ex: /website/AC0099/AC0019 → /website/AC0099/static/css/style.css
		if (StringUtils.hasLength(targetPath) && !"/".equals(targetPath)) {
			UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(targetBaseUrl);
			List<String> pathSegments = new ArrayList<>(builder.build().getPathSegments());
			if (CollectionUtils.isEmpty(pathSegments)) {
				pathSegments.add(targetPath);
			} else {
				pathSegments.set(pathSegments.size() - 1, targetPath);
			}
			return builder.replacePath(String.join("/", pathSegments)).build().toString();
			// 如果 targetPath 是空或是只有 "/", 表示是由 AA0506 過來, 要直接進入註冊在 TSMP_REPORT_URL.report_url
			// 的位址
		} else {
			return targetBaseUrl;
		}
	}

	private void addCApiKey(Map<String, List<String>> headers) {
		String uuid = UUID.randomUUID().toString().toUpperCase();
		String cApiKey = CApiKeyUtils.signCKey(uuid);
		headers.put("cuuid", List.of(uuid));
		headers.put("capi-key", List.of(cApiKey));
	}

	private void sendError(HttpServletResponse response, HttpStatus status, String errMsg) throws IOException {
		response.setContentType(MediaType.TEXT_PLAIN_VALUE);
		response.setStatus(status.value());
		response.getOutputStream().write(errMsg.getBytes());
	}

	protected TsmpReportUrlDao getTsmpReportUrlDao() {
		return tsmpReportUrlDao;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

}
