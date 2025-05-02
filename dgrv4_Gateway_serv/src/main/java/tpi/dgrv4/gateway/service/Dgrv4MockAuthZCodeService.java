package tpi.dgrv4.gateway.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

/**
 * 2023/04/20 目前已不使用, 改用 GTW IdP 流程, 故全部註解
 * @author Mini
 */

@Service
public class Dgrv4MockAuthZCodeService {
	
//	@Autowired
//	private OAuthTokenService oAuthTokenService;
//
//	@Autowired
//	private TokenHelper tokenHelper;
//	
//	public ResponseEntity<?> mockBankUserLogin(HttpHeaders httpHeaders, HttpServletRequest httpReq, 
//			HttpServletResponse httpRes) {
//		String apiUrl = httpReq.getRequestURI();
//		
//		try {
//			StringBuffer reqUrl = httpReq.getRequestURL();
//			TPILogger.tl.info("\n--【" + reqUrl.toString() + "】--");
//			
//			Map<String, String> parameters = new HashMap<>();
//			httpReq.getParameterMap().forEach((k, vs) -> {
//				if(vs.length != 0) {
//					parameters.put(k, vs[0]);
//				}
//			});
//			
//			String dmazs_scope = parameters.get("scope");
//			String dmazs_redirectUri = parameters.get("redirect_uri");
//			String dmazs_state = parameters.get("state");
//			
//			// chk param
//			if(!StringUtils.hasLength(dmazs_scope)) {
//				String word = "scope";
//				TPILogger.tl.debug("Query String has no " + word);//沒有 scope
//				return getOAuthTokenService().getResponseEntityError(word);
//			}
//			
//			if(!StringUtils.hasLength(dmazs_redirectUri)) {
//				String word = "redirect_uri";
//				TPILogger.tl.debug("Query String has no " + word);//沒有 redirect_uri
//				return getOAuthTokenService().getResponseEntityError(word);
//			}		
//			
//			if(!StringUtils.hasLength(dmazs_state)) {
//				String word = "state";
//				TPILogger.tl.debug("Query String has no " + word);//沒有 state
//				return getOAuthTokenService().getResponseEntityError(word);
//			}		
// 		
//			String localBaseUrl = getTokenHelper().getLocalBaseUrl(httpReq);
//			if(!StringUtils.hasLength(localBaseUrl)) {
//				String errMsg = TokenHelper.Internal_Server_Error;
//				TPILogger.tl.error(errMsg);
//				return getTokenHelper().getInternalServerErrorResp(apiUrl, errMsg);//500
//			}
//			
//			callOAuthApprove(dmazs_scope, dmazs_state, dmazs_redirectUri, httpReq, localBaseUrl);
//	 		
//		} catch (Exception e) {				
//			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
//			String errMsg = TokenHelper.Internal_Server_Error;
//			TPILogger.tl.error(errMsg);
//			return getTokenHelper().getInternalServerErrorResp(apiUrl, errMsg);//500	
//		}				
//		return null;				
//	}
//	
//	private void callOAuthApprove(String scope, String state, String redirectUri, HttpServletRequest httpReq, 
//			String localBaseUrl) throws IOException {
//		String reqUrl = localBaseUrl + "/oauth/approve";
//		
//		Map<String, List<String>> header = new HashMap<>();
////		header.put("Accept", Arrays.asList("*/*"));
//		header.put("Accept", Arrays.asList("application/json"));
//		
//		Map<String, List<String>> formData = new HashMap<>();
//		formData.put("scope", Arrays.asList(scope));
//		formData.put("username", Arrays.asList("tspuser"));
//		formData.put("state", Arrays.asList(state));
//		formData.put("redirect_uri", Arrays.asList(redirectUri));
//		
//		HttpRespData resp = HttpUtil.httpReqByX_www_form_urlencoded_UTF8List(reqUrl, "POST", 
//				formData, header, false, true);
//		TPILogger.tl.info("========================================(call /oauth/approve)");
//		TPILogger.tl.info(resp.getLogStr());
//	}
//	
//	public ResponseEntity<?> test_authorization_code_step3(String redirectUri, String authCode, HttpServletRequest httpReq) throws Exception {
//		TPILogger.tl.info("\n--【" + httpReq.getRequestURL().toString() + "】--");
//		
//		//Step 3. POST /token [使用 AuthZ Code + redirect_uri + Client ID + Client Secret 請求 token]
//		{
//			String apiUrl = httpReq.getRequestURI();
//			String localBaseUrl = getTokenHelper().getLocalBaseUrl(httpReq);
//			if(!StringUtils.hasLength(localBaseUrl)) {
//				String errMsg = TokenHelper.Internal_Server_Error;
//				TPILogger.tl.error(errMsg);
//				return getTokenHelper().getInternalServerErrorResp(apiUrl, errMsg);//500
//			}
//			
//			String reqUrl = localBaseUrl + "/oauth/token";
//			String authorization = getBasicAuthorization("tspclient", "tspclient123");
//			
//			Map<String, List<String>> header = new HashMap<>();
//			header.put("Accept", Arrays.asList("application/json"));
//			header.put("Authorization", Arrays.asList(authorization));
//			
//			Map<String, List<String>> formData = new HashMap<>();
//			formData.put("grant_type", Arrays.asList("authorization_code"));
//			formData.put("code", Arrays.asList(authCode));
//			formData.put("redirect_uri", Arrays.asList(redirectUri));
//			
//			HttpRespData respData = HttpUtil.httpReqByX_www_form_urlencoded_UTF8List(reqUrl, "POST", 
//					formData, header, false, true);
//			TPILogger.tl.info("========================================(call /oauth/token)");
//			TPILogger.tl.info(respData.getLogStr());
//			return new ResponseEntity<String>(respData.respStr, HttpStatus.OK);
//		}
//	}
//	
//	public static String getBasicAuthorization(String origClientId, String origClientPwd) {
//		String clientPwBase64ed = Base64Util.base64Encode(origClientPwd.getBytes());
// 
//		String info = origClientId + ":" + clientPwBase64ed;
//		info = Base64Util.base64Encode(info.getBytes());//Base64 Encode
//		String authorization = "Basic " + info;
//		return authorization;
//	}
//	
//	protected OAuthTokenService getOAuthTokenService() {
//		return oAuthTokenService;
//	}
//	
//	protected TokenHelper getTokenHelper() {
//		return tokenHelper;
//	}
}
