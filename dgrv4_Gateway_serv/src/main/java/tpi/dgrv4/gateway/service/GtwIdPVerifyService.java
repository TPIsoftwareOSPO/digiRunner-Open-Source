package tpi.dgrv4.gateway.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.codec.utils.IdTokenUtil;
import tpi.dgrv4.codec.utils.IdTokenUtil.IdTokenData;
import tpi.dgrv4.codec.utils.JWKcodec;
import tpi.dgrv4.codec.utils.JWKcodec.JWKVerifyResult;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.escape.ESAPI;
import tpi.dgrv4.gateway.component.GtwIdPHelper;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.GtwIdPVerifyResp;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp2;

/**
 * 驗證 ID token 並取得 User 個人資料
 * 
 * @author Mini
 */

@Service
public class GtwIdPVerifyService {

	private TokenHelper tokenHelper;
	private TsmpSettingService tsmpSettingService;
	private ObjectMapper objectMapper;
	private GtwIdPJwksService gtwIdPJwksService;

	@Autowired
	public GtwIdPVerifyService(TokenHelper tokenHelper, TsmpSettingService tsmpSettingService,
			ObjectMapper objectMapper, GtwIdPJwksService gtwIdPJwksService) {
		super();
		this.tokenHelper = tokenHelper;
		this.tsmpSettingService = tsmpSettingService;
		this.objectMapper = objectMapper;
		this.gtwIdPJwksService = gtwIdPJwksService;
	}

	public ResponseEntity<?> verify(HttpServletRequest httpReq, HttpServletResponse httpResp, HttpHeaders headers) {
		String reqUri = httpReq.getRequestURI();

		ResponseEntity<?> respEntity = null;
		try {
			String idTokenJwtstr = httpReq.getParameter("id_token");
			
			return verify(idTokenJwtstr, null, reqUri);

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			String errMsg = TokenHelper.INTERNAL_SERVER_ERROR;
			TPILogger.tl.error(errMsg);
			respEntity = getTokenHelper().getInternalServerErrorResp(reqUri, errMsg);// 500
			return respEntity;
		}
	}

	/** 
	 * 1.驗 ID token 的簽章、期限, 並取得 ID token 內容 <br>
	 * 2.若查詢有 apiResp, 則加上調用 IdP API 得到的 response 結果 <br>
	 *   a. GTW IdP(API) 調用客戶 Login API 得到的 response <br>
	 *   b. GTW OAuth 2.0 IdP(GOOGLE / MS) 取 token 得到的 API response <br>
	 */
	protected ResponseEntity<?> verify(String idTokenJwtstr, String apiResp, String reqUri) throws Exception {
		
		// 沒有 id_token 值
		if (!StringUtils.hasLength(idTokenJwtstr)) {
			String errMsg = TokenHelper.MISSING_REQUIRED_PARAMETER + "id_token";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp2>(
					getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
					HttpStatus.BAD_REQUEST);// 400
		}
		
		// 取得 ID token 中的值
		IdTokenData idTokenData = IdTokenUtil.getIdTokenData(idTokenJwtstr);
		String errMsg = idTokenData.errMsg;
		if (StringUtils.hasText(errMsg)) {// 資料有錯誤
			//checkmarx, Reflected XSS All Clients 
			errMsg = ESAPI.encoder().encodeForHTML(errMsg);
			TPILogger.tl.debug(errMsg);
			return getTokenHelper().getForbiddenErrorResp(reqUri, errMsg);// 403
		}
		
		String iss = idTokenData.iss;
		
		// 因checkmarx,Reflected XSS All Clients ,所以也把idTokenJwtstr傳入參數拿掉,反正也沒用到
		// 檢查資料
		ResponseEntity<?> errRespEntity = checkReqParam(iss, reqUri);
		if (errRespEntity != null) {// 資料驗證有錯誤
			return errRespEntity;
		}
 
		// 取得 JWKS 的公鑰 JSON
		String jwksJsonStr = getGtwIdPJwksService().getJwksJsonStr();

		// 驗證 ID token
		boolean isVerify = false;
		JWKVerifyResult jwkRs = JWKcodec.verifyJWStokenByJsonString(idTokenJwtstr, iss, jwksJsonStr);
		isVerify = jwkRs.verify;
//		TPILogger.tl.debug("ID token verify : " + isVerify);

		if (isVerify) {// 驗證 ID token 成功
			GtwIdPVerifyResp gtwIdPVerifyResp = getGtwIdPVerifyResp(idTokenData, apiResp);
			
			String respJsonStr = getObjectMapper().writeValueAsString(gtwIdPVerifyResp);
			respJsonStr = JWKcodec.toPrettyJson(respJsonStr);
			//checkmarx, Reflected XSS All Clients 
			respJsonStr = ESAPI.encoder().encodeForHTML(respJsonStr);

			return new ResponseEntity<String>(respJsonStr, HttpStatus.OK);

		} else {
			errMsg = jwkRs.errorMessg;
			//checkmarx, Reflected XSS All Clients 
			errMsg = ESAPI.encoder().encodeForHTML(errMsg);
			
			TPILogger.tl.debug(errMsg);
			return getTokenHelper().getForbiddenErrorResp(reqUri, errMsg);// 403
		}
	}
	
	private GtwIdPVerifyResp getGtwIdPVerifyResp(IdTokenData idTokenData, String apiResp) throws Exception {
		String iss = idTokenData.iss;
		String userName = idTokenData.userName;
		String userAlias = idTokenData.userAlias;
		String userEmail = idTokenData.userEmail;
		String userPicture = idTokenData.userPicture;
		Long iat = idTokenData.iat;
		Long exp = idTokenData.exp;
		String aud = idTokenData.aud;
		String lightId = idTokenData.idtLightId;
		String roleName = idTokenData.idtRoleName;
		String apiResp_en = null; // GTW IdP(API / GOOGLE / MS) 調用 IdP API 得到的 response 結果
		if(StringUtils.hasText(apiResp)) {
			apiResp_en = Base64Util.base64URLEncode(apiResp.getBytes()); // 有值則做 Base64 URL Encode
		}
		
		GtwIdPVerifyResp gtwIdPVerifyResp = new GtwIdPVerifyResp();
		gtwIdPVerifyResp.setIss(iss);
		gtwIdPVerifyResp.setSub(userName);
		gtwIdPVerifyResp.setAud(aud);
		gtwIdPVerifyResp.setExp(exp);
		gtwIdPVerifyResp.setIat(iat);
		gtwIdPVerifyResp.setName(userAlias);
		gtwIdPVerifyResp.setEmail(userEmail);
		gtwIdPVerifyResp.setPicture(userPicture);
		gtwIdPVerifyResp.setLightId(lightId);
		gtwIdPVerifyResp.setRoleName(roleName);
		gtwIdPVerifyResp.setBase64urlOrigProfile(apiResp_en);
 
		return gtwIdPVerifyResp;
	}
	
	/**
	 * 檢查傳入的資料
	 */
	private ResponseEntity<?> checkReqParam(String iss, String reqUri) throws Exception {
		// ID token 沒有 iss
		if (!StringUtils.hasLength(iss)) {
			String errMsg = "Missing ID token parameter: iss";
			TPILogger.tl.debug(errMsg);
			return getTokenHelper().getForbiddenErrorResp(reqUri, errMsg);// 403
		}

		// 對外公開的域名或IP
		String dgrPublicDomain = getTsmpSettingService().getVal_DGR_PUBLIC_DOMAIN();
		// 對外公開的Port
		String dgrPublicPort = getTsmpSettingService().getVal_DGR_PUBLIC_PORT();
		
		String schemeAndDomainAndPort = GtwIdPWellKnownService.getSchemeAndDomainAndPort(dgrPublicDomain,
				dgrPublicPort);

		String matchIssuer = null;
		List<String> supportGtwIdPTypeList = GtwIdPHelper.getSupportGtwIdPType();// 目前 GTW IdP 支援的 IdP Type
		for (String idPType : supportGtwIdPTypeList) {
			String issuer = GtwIdPWellKnownService.getIssuer(schemeAndDomainAndPort, idPType);
			if (iss.equals(issuer)) {
				matchIssuer = issuer;
				break;
			}
		}

		// iss 不正確
		if (!StringUtils.hasLength(matchIssuer)) {
			String errMsg = "Invalid ID token issuer. iss: " + iss;
			//checkmarx, Reflected XSS All Clients 
			errMsg = ESAPI.encoder().encodeForHTML(errMsg);
			TPILogger.tl.debug(errMsg);
			return getTokenHelper().getForbiddenErrorResp(reqUri, errMsg);// 403
		}
		
		return null;
	}

	protected TokenHelper getTokenHelper() {
		return tokenHelper;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	protected GtwIdPJwksService getGtwIdPJwksService() {
		return gtwIdPJwksService;
	}
}
