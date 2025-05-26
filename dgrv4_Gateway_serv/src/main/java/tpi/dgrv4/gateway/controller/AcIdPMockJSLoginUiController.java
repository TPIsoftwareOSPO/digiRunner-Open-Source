package tpi.dgrv4.gateway.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.component.IdPHelper;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

/**
 * @author Mini
 * 
 * 模擬 LDAP User 登入畫面
 */

@RestController
public class AcIdPMockJSLoginUiController {

	TokenHelper tokenHelper;
 
	@Autowired
	public AcIdPMockJSLoginUiController(TokenHelper tokenHelper) {
		super();
		this.tokenHelper = tokenHelper;
	}

	//https://localhost:8080/dgrv4/mockac/idpsso/LDAP/acIdPLogin?username=minildap&password=mini123
	@RequestMapping(value = "/dgrv4/mockac/idpsso/{idPType}/acIdPLogin", method = RequestMethod.POST)
	public ResponseEntity<?> loginUi(@RequestHeader HttpHeaders headers, 
			HttpServletRequest httpReq,
			HttpServletResponse httpResp,
			@PathVariable("idPType") String idPType) throws Exception {

		TPILogger.tl.info("\n--【" + httpReq.getRequestURL().toString() + "】--");
		
		String reqUri = httpReq.getRequestURI();
		
		// 輸入的 user
		String userName = httpReq.getParameter("username");
		String userMima = httpReq.getParameter("password");
		String isPost = httpReq.getParameter("isPost");

		try {
			//checkmarx, Frameable Login Page, 已通過中風險
			httpResp.setHeader("X-Frame-Options", "sameorigin");
			  //checkmarx, Missing HSTS Header
	        httpResp.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload"); 
	        
			
			// dgrv4/ssotoken/acidp/{idPType}/acIdPLogin
			String acIdPLoginUrl = "https://localhost:8080/dgrv4/ssotoken/acidp/" + idPType + "/acIdPLogin";
			
			if("Y".equals(isPost)) {
				// 調用後端的 login API
				callLoginApi(acIdPLoginUrl, userName, userMima);
			}else {
				// 302 到 login URL
				redirectLoginUrl(httpResp, acIdPLoginUrl, userName, userMima);
			}
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			String errMsg = TokenHelper.INTERNAL_SERVER_ERROR;
			TPILogger.tl.error(errMsg);
			return getTokenHelper().getInternalServerErrorResp(reqUri, errMsg);// 500
		}

		return null;
	}
	
	/**
	 *  調用後端的 login API
	 */
	public static HttpRespData callLoginApi(String reqUrl, String userName, String userMima) throws Exception {
		// Call API
		Map<String, List<String>> header = new HashMap<>();
		
		Map<String, List<String>> formData = new HashMap<>();
		formData.put("username", Arrays.asList(userName));
		formData.put("password", Arrays.asList(userMima));
		
		HttpRespData resp = HttpUtil.httpReqByX_www_form_urlencoded_UTF8List(reqUrl, "POST", formData, header, false,
				true);
		TPILogger.tl.info("========================================");
		TPILogger.tl.info(resp.getLogStr());
		
		return resp;
	}
 
	/**
	 *  轉導到後端的 login URL
	 */
	private void redirectLoginUrl(HttpServletResponse httpResp, String acIdPLoginUrl, String userName, String userMima)
			throws Exception {
		
		// 轉導到後端的 login API
		String redirectUrl = String.format(
				"%s" 
				+ "?username=%s" 
				+ "&password=%s",
				acIdPLoginUrl, 
				IdPHelper.getUrlEncode(userName), 
				IdPHelper.getUrlEncode(userMima)
		);
		
		TPILogger.tl.debug("Redirect to URL【dgR Login URL】: " + redirectUrl);
		httpResp.sendRedirect(redirectUrl);
	}
	
	protected TokenHelper getTokenHelper() {
		return tokenHelper;
	}
}
