package tpi.dgrv4.dpaa.controller;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static tpi.dgrv4.gateway.controller.DgrCusController.KEY_OF_RID_COOKIE;
import static tpi.dgrv4.gateway.controller.DgrCusController.KEY_OF_TOKEN_COOKIE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.vo.BeforeControllerReq;
import tpi.dgrv4.common.vo.BeforeControllerResp;
import tpi.dgrv4.dpaa.constant.ReportType;
import tpi.dgrv4.dpaa.service.AA0506Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.AA0506Req;
import tpi.dgrv4.dpaa.vo.AA0506Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * 查詢Kibana報表網址
 * 
 * @author Mavis
 */
@RestController
public class AA0506Controller {
	@Value("${digi.cookie.samesite.value:Lax}")
	private String samesiteValue;
	
	private AA0506Service service;

	@Autowired
	public AA0506Controller(AA0506Service service) {
		super();
		this.service = service;
	}

	@PostMapping(value = "/dgrv4/11/AA0506", params = {"before"}, //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<BeforeControllerResp> queryReportUrls_before(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<BeforeControllerReq> req) {
		return ControllerUtil.getReqConstraints(req, new AA0506Req());
	}

	/**
	 * 查詢稽核日誌							
	 * 
	 * @param headers
	 * @param req
	 * @return
	 */

	@PostMapping(value = "/dgrv4/11/AA0506", //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<AA0506Resp> queryReportUrls(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<AA0506Req> req, HttpServletResponse response) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		AA0506Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = service.queryReportUrls(req.getBody());
			
			String rptType = resp.getReportType();
			if (ReportType.EMBEDDED_LINKS.equals(rptType)) {
				String rootPath = getRootPath(resp.getReportUrl());
				String cookie = getTokenCookie(headers, rootPath);
				response.addHeader(HttpHeaders.SET_COOKIE, cookie);
				cookie = getReportIdCookie(req.getBody().getReportID(), rootPath);
				response.addHeader(HttpHeaders.SET_COOKIE, cookie);
				//checkmarx, Missing HSTS Header
	            response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload"); 
	            
			}
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
	
	private String getRootPath(String reportUrl) {
		// 移除最後一段 path, 代表所有送到 rootPath 的請求都要帶上此 cookie
		List<String> pathSegments = new ArrayList<>(UriComponentsBuilder.fromUriString(reportUrl).build().getPathSegments());
        if (!CollectionUtils.isEmpty(pathSegments)) {
        	pathSegments.remove(pathSegments.size() - 1);
        }
        return "/" + String.join("/", pathSegments);
	}

	private String getTokenCookie(HttpHeaders headers, String rootPath) {

		var authHeader = Optional.ofNullable(headers.get("authorization"));

		if (authHeader.isEmpty() || authHeader.get().isEmpty()) return "";

		String authorization = new String(Base64.getEncoder().encode(authHeader.get().get(0).getBytes()), StandardCharsets.UTF_8);

		ResponseCookie cookie = ResponseCookie.from(KEY_OF_TOKEN_COOKIE, authorization) // key & value
			// 2024.05.24; Kim; Keep cookie until logout or close browser
			//.maxAge(this.tsmpSettingService.getVal_SSO_TIMEOUT() * 60) // maxAge 以秒為單位, SSO_TIMEOUT以分鐘為單位
			.path(rootPath)
			.httpOnly(true) // 禁止 JavaScript 存取 cookie, 防止 XSS Attack (Cross-Site Scripting，跨站腳本攻擊)
			.secure(true) // 讓 cookie 只能透過 https 傳遞, 即只有 HTTPS 才能讀與寫
			.sameSite(samesiteValue) // 防止 CSRF Attack (Cross-site request forgery，跨站請求偽造)
			.build();
		return cookie.toString();
	}

	private String getReportIdCookie(String reportId, String rootPath) {
		ResponseCookie cookie = ResponseCookie.from(KEY_OF_RID_COOKIE, reportId) // key & value
			// 2024.05.24; Kim; Keep cookie until logout or close browser
			//.maxAge(this.tsmpSettingService.getVal_SSO_TIMEOUT() * 60) // maxAge 以秒為單位, SSO_TIMEOUT以分鐘為單位
			.path(rootPath)
			.httpOnly(true) // 禁止 JavaScript 存取 cookie, 防止 XSS Attack (Cross-Site Scripting，跨站腳本攻擊)
			.secure(true) // 讓 cookie 只能透過 https 傳遞, 即只有 HTTPS 才能讀與寫
			.sameSite(samesiteValue) // 防止 CSRF Attack (Cross-site request forgery，跨站請求偽造)
			.build();
		return cookie.toString();
	}
}
