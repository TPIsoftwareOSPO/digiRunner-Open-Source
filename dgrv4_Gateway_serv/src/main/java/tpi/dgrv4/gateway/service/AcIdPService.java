package tpi.dgrv4.gateway.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.DgrAuditLogService;
import tpi.dgrv4.dpaa.service.TptokenService;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.entity.component.cipher.TsmpTAEASKHelper;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.component.authorization.TsmpAuthorizationParser;
import tpi.dgrv4.gateway.component.authorization.TsmpAuthorizationParserFactory;
import tpi.dgrv4.gateway.constant.DgrTokenGrantType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp2;
import tpi.dgrv4.gateway.vo.OAuthTokenResp;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AcIdPService {

	private TPILogger logger = TPILogger.tl;

	private OAuthTokenService oAuthTokenService;
	private TsmpSettingService tsmpSettingService;
	private TptokenService tptokenService;
	private TokenHelper tokenHelper;
	private TsmpTAEASKHelper tsmpTAEASKHelper;
	private DgrAuditLogService dgrAuditLogService;
	private ObjectMapper objectMapper;
	
	//Audit Log使用
	private String eventNo = AuditLogEvent.LOGIN.value(); 
 
	@Autowired
	public AcIdPService(OAuthTokenService oAuthTokenService, TsmpSettingService tsmpSettingService,
			TptokenService tptokenService, TokenHelper tokenHelper, TsmpTAEASKHelper tsmpTAEASKHelper,
			DgrAuditLogService dgrAuditLogService, ObjectMapper objectMapper) {
		super();
		this.oAuthTokenService = oAuthTokenService;
		this.tsmpSettingService = tsmpSettingService;
		this.tptokenService = tptokenService;
		this.tokenHelper = tokenHelper;
		this.tsmpTAEASKHelper = tsmpTAEASKHelper;
		this.dgrAuditLogService = dgrAuditLogService;
		this.objectMapper = objectMapper;
	}

	public ResponseEntity<?> getAcToken(HttpHeaders httpHeaders, HttpServletRequest httpReq, 
			HttpServletResponse httpRes, String dgRcode) throws Exception {
		
		String txnUid = getDgrAuditLogService().getTxnUid();
		String userIp = !StringUtils.hasLength(httpHeaders.getFirst("x-forwarded-for")) ? httpReq.getRemoteAddr()
				: httpHeaders.getFirst("x-forwarded-for");
		String userHostname = httpReq.getRemoteHost();
		String apiUrl = httpReq.getRequestURI();

		ResponseEntity<?> resp = getAcToken(httpRes, dgRcode, userIp, userHostname, txnUid, apiUrl);
		return resp;
	}
	
	public ResponseEntity<?> getAcToken(HttpServletResponse httpRes, String dgRcode, String userIp,
			String userHostname, String txnUid, String apiUrl) throws Exception {
		if(!StringUtils.hasLength(dgRcode)) {
			String errMsg = "Missing dgRcode";//Query String 沒有 dgRcode
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp2>(getTokenHelper().getOAuthTokenErrorResp2(
					TokenHelper.INVALID_REQUEST, errMsg), HttpStatus.BAD_REQUEST);//400
		}
		
		if(isGetToken()) {
			return getToken(httpRes, dgRcode, userIp, userHostname, txnUid, apiUrl);
		}
		return null;
	}
 
	/**
	 * 取得 AC token
	 */
	private ResponseEntity<?> getToken(HttpServletResponse httpRes, String dgRcode, String userIp, String userHostname,
			String txnUid, String apiUrl) throws Exception {
		// 取得 AES Key						
		String aesKey = getTptokenService().getAesKey();
		this.logger.debug("--TAEASK:" + ServiceUtil.dataMask(aesKey, 2, 2));
		
		// 取得 Client ID/PW
		String clientId = getTptokenService().getClientId(aesKey);
		this.logger.debug("--clientId:" + ServiceUtil.dataMask(clientId, 2, 2));
		String clientPw = getTptokenService().getClientPw(aesKey);
		this.logger.debug("--clientPw:" + ServiceUtil.dataMask(clientPw, 2, 2));
		
		// 組成 Http Header Authorization 
		String authorization = getTptokenService().getAuthorization(clientId, clientPw);
		
		/*
		 * 直接從 OAuthTokenService 取 Token
		 */
		
		// http header
		Map<String, String> httpHeader = new HashMap<>();
		httpHeader.put("Authorization", authorization);
		
		// form data
		Map<String, String> formData = new HashMap<>();
		formData.put("grant_type", DgrTokenGrantType.DELEGATE_AUTH);// "delegate_auth",客製,SSO與IdP介接
		formData.put("code", dgRcode);

		ResponseEntity<?> respObj = getOAuthTokenService().getToken(httpRes, formData, authorization, "/oauth/token");
		Object bodyObj = respObj.getBody();
		int statusCode = respObj.getStatusCode().value();

		if (bodyObj == null) return new ResponseEntity<OAuthTokenErrorResp2>(getTokenHelper().getOAuthTokenErrorResp2(
				TokenHelper.INVALID_REQUEST, "getOAuthTokenService().getToken(...) response body is null"), HttpStatus.BAD_REQUEST);


		String token_loginState = "";
		String tokenJti = null;
		String userName = null;
		String idPType = null;
		String idPSub = null;

		
		TsmpAuthorization auth = null;
		String lineNumber = null;
		if(bodyObj instanceof OAuthTokenResp oAuthTokenResp) {
			token_loginState = "SUCCESS";

			tokenJti = oAuthTokenResp.getJti();

			String accessTokenJwtStr = oAuthTokenResp.getAccessToken();
			TsmpAuthorizationParser authorizationParser = new TsmpAuthorizationParserFactory()
					.getParser("Bearer " + accessTokenJwtStr);
			auth = authorizationParser.parse();
			InnerInvokeParam iip = getDgrAuditLogService().getInnerInvokeParam(auth, apiUrl, userIp, userHostname,
					txnUid);

			//寫入 Audit Log M,登入成功
			lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogMForLogin(iip, lineNumber, eventNo, apiUrl,
					userName, clientId, userIp, userHostname, token_loginState, statusCode+"", txnUid, tokenJti);
			return respObj;
			
		}else if(bodyObj instanceof OAuthTokenErrorResp) {
			token_loginState = "FAILED";
			lineNumber = StackTraceUtil.getLineNumber();
			
		}else if(bodyObj instanceof OAuthTokenErrorResp2) {
			token_loginState = "FAILED";
			lineNumber = StackTraceUtil.getLineNumber();
			
		}else {
			token_loginState = "FAILED";
			lineNumber = StackTraceUtil.getLineNumber();
		}
		
		//寫入 Audit Log M,登入失敗
		getDgrAuditLogService().createAuditLogMForLogin(lineNumber, eventNo, apiUrl, 
				userName, clientId, userIp, userHostname, token_loginState, statusCode+"", txnUid, tokenJti, idPType);
		
		return respObj;
	}
	
	//為了讓 Unit test 使用 override
	protected boolean isGetToken() {
		boolean isEnable = true;
		return isEnable;
	}
	
	protected OAuthTokenService getOAuthTokenService() {
		return this.oAuthTokenService;
	}
	
	protected TsmpSettingService getTsmpSettingService() {
		return this.tsmpSettingService;
	}
	
	protected TptokenService getTptokenService() {
		return tptokenService;
	}
	
	protected TsmpTAEASKHelper getTsmpTAEASKHelper() {
		return this.tsmpTAEASKHelper;
	}
	
	protected TokenHelper getTokenHelper() {
		return tokenHelper;
	}
	
	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}
	
	protected ObjectMapper getObjectMapper() {
		return objectMapper;
	}
}
