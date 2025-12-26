package tpi.dgrv4.gateway.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.codec.utils.JWKcodec;
import tpi.dgrv4.common.constant.DgrIdPType;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.escape.ESAPI;
import tpi.dgrv4.gateway.component.GtwIdPHelper;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.GtwIdPWellKnownResp;

@Service
public class GtwIdPWellKnownService {
	
	ObjectMapper objectMapper;
	TsmpSettingService tsmpSettingService;
	TokenHelper tokenHelper;

	@Autowired
	public GtwIdPWellKnownService(ObjectMapper objectMapper, TsmpSettingService tsmpSettingService,
			TokenHelper tokenHelper) {
		super();
		this.objectMapper = objectMapper;
		this.tsmpSettingService = tsmpSettingService;
		this.tokenHelper = tokenHelper;
	}

	public ResponseEntity<?> getGtwIdPWellKnown(HttpHeaders httpHeaders, HttpServletRequest httpReq,
			HttpServletResponse httpResp, String idPType) throws Exception {
		String reqUri = httpReq.getRequestURI();
		ResponseEntity<?> respEntity = null;
		try {
			//checkmarx, Reflected XSS All Clients 
			idPType = ESAPI.encoder().encodeForHTML(idPType);
			
			// 檢查傳入的資料
			respEntity = checkReqParam(idPType);
			if (respEntity != null) {// 資料驗證有錯誤
				return respEntity;
			}

			String respJsonStr = getGtwIdPWellKnown(idPType);
			//checkmarx, Reflected XSS All Clients 
			respJsonStr = ESAPI.encoder().encodeForHTML(respJsonStr);
			return new ResponseEntity<Object>(respJsonStr, HttpStatus.OK);

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			String errMsg = TokenHelper.INTERNAL_SERVER_ERROR;
			TPILogger.tl.error(errMsg);
			//checkmarx, Reflected XSS All Clients 
			reqUri = ESAPI.encoder().encodeForHTML(reqUri);
			respEntity = getTokenHelper().getInternalServerErrorResp(reqUri, errMsg);// 500
			return respEntity;
		}
	}
	
	private String getGtwIdPWellKnown(String idPType) throws Exception {
		// 對外公開的域名或IP
		String dgrPublicDomain = getTsmpSettingService().getVal_DGR_PUBLIC_DOMAIN();
		// 對外公開的Port
		String dgrPublicPort = getTsmpSettingService().getVal_DGR_PUBLIC_PORT();
		
		String schemeAndDomainAndPort = getSchemeAndDomainAndPort(dgrPublicDomain, dgrPublicPort);
		
		String issuer = getIssuer(schemeAndDomainAndPort, idPType);
		String authorizationEndpoint = getAuthorizationEndpoint(schemeAndDomainAndPort, idPType);
		String tokenEndpoint = getTokenEndpoint(schemeAndDomainAndPort);
		String userinfoEndpoint = getUserinfoEndpoint(schemeAndDomainAndPort);
		String jwksUri = getJwksUri(schemeAndDomainAndPort);
		String callbackEndpoint = getCallbackEndpoint(schemeAndDomainAndPort, idPType);
		
		// 支援哪些 Scope
		List<String> scopesSupportedList = GtwIdPHelper.getSupportScopeList();
		
		GtwIdPWellKnownResp gtwIdPWellKnownResp = new GtwIdPWellKnownResp();
		gtwIdPWellKnownResp.setIssuer(issuer);
		gtwIdPWellKnownResp.setAuthorizationEndpoint(authorizationEndpoint);
		gtwIdPWellKnownResp.setTokenEndpoint(tokenEndpoint);
		gtwIdPWellKnownResp.setUserinfoEndpoint(userinfoEndpoint);
		gtwIdPWellKnownResp.setJwksUri(jwksUri);
		gtwIdPWellKnownResp.setIdTokenSigningAlgValuesSupported(Arrays.asList("RS256"));
		gtwIdPWellKnownResp.setScopesSupported(scopesSupportedList);
		
		if (DgrIdPType.GOOGLE.equals(idPType) //
				|| DgrIdPType.MS.equals(idPType) //
				|| DgrIdPType.OIDC.equals(idPType) //
		) {
			// OAuth 2.0 的 IdP type 才需要顯示 CallbackEndpoint
			gtwIdPWellKnownResp.setCallbackEndpoint(callbackEndpoint);
		}
		
		// 支援哪些 Code_challenge_method
		List<String> codeChallengeMethodsSupportedList = GtwIdPHelper.getCodeChallengeMethodsSupported();
		gtwIdPWellKnownResp.setCodeChallengeMethodsSupported(codeChallengeMethodsSupportedList);
		
		String respJsonStr = getObjectMapper().writeValueAsString(gtwIdPWellKnownResp);
		respJsonStr = JWKcodec.toPrettyJson(respJsonStr);
		return respJsonStr;
	}
	
	/**
	 * 取得 dgR 的 Scheme Domain Port <br>
	 * 例如: https://domain:port <br>
	 * 若 port 為 443,則不顯示 port <br>
	 * 例如: https://domain <br>
	 */
	public static String getSchemeAndDomainAndPort(String dgrPublicDomain, String dgrPublicPort) {
		String schemeAndDomainAndPort = "";
		
		// 若 https 為預設 port 443,則不顯示 port
		if("443".equals(dgrPublicPort)) {
			schemeAndDomainAndPort = String.format("https://%s", 
					dgrPublicDomain);
		}else {
			schemeAndDomainAndPort = String.format("https://%s:%s", 
					dgrPublicDomain,
					dgrPublicPort);
		}
		
		return schemeAndDomainAndPort;
	}

	/**
	 * 檢查傳入的資料
	 */
	private ResponseEntity<?> checkReqParam(String idPType) {
		ResponseEntity<?> errRespEntity = getTokenHelper().checkSupportGtwIdPType(idPType);
		if (errRespEntity != null) {// idPType 資料驗證有錯誤
			return errRespEntity;
		}
		return null;
	}
 
	/**
	 * WellKnownUrl <br>
	 * 例如: <br>
	 * https://127.0.0.1:8080/dgrv4/ssotoken/{idPType}/.well-known/openid-configuration
	 * https://127.0.0.1:8080/dgrv4/ssotoken/GOOGLE/.well-known/openid-configuration
	 */
	public static String getWellKnownUrl(String schemeAndDomainAndPort, String idPType) {
		String issuer = getIssuer(schemeAndDomainAndPort, idPType);
		String wellKnownUrl = String.format("%s/.well-known/openid-configuration", 
				issuer);
		
		return wellKnownUrl;
	}
	
	/**
	 * 1. issuer 的值 <br>
	 * 2. 核發 ID token 的 iss 值 <br>
	 * 例如: <br>
	 * https://127.0.0.1:8080/dgrv4/ssotoken/{idPType} <br>
	 * https://127.0.0.1:8080/dgrv4/ssotoken/GOOGLE <br>
	 */
	public static String getIssuer(String schemeAndDomainAndPort, String idPType) {
		String issuer = String.format("%s/dgrv4/ssotoken/%s", 
				schemeAndDomainAndPort,
				idPType);
		
		return issuer;
	}
	
	/**
	 * OpenID Connect 啟動身分驗證的入口 <br>
	 * 例如: <br>
	 * https://127.0.0.1:8080/dgrv4/ssotoken/gtwidp/{idPType}/authorization <br>
	 * https://127.0.0.1:8080/dgrv4/ssotoken/gtwidp/GOOGLE/authorization <br>
	 */
	public static String getAuthorizationEndpoint(String schemeAndDomainAndPort, String idPType) {
		String authorizationEndpoint = String.format("%s/dgrv4/ssotoken/gtwidp/%s/authorization",
				schemeAndDomainAndPort,
				idPType);
		
		return authorizationEndpoint;
	}
	
	/**
	 * OpenID Connect 讓 Client 取得 token 的端口 <br>
	 * 例如: <br>
	 * https://127.0.0.1:8080/oauth/token <br>
	 */
	public static String getTokenEndpoint(String schemeAndDomainAndPort) {
		String tokenEndpoint = String.format("%s/oauth/token", schemeAndDomainAndPort);
		
		return tokenEndpoint;
	}
	
	/**
	 * OpenID Connect 讓 Client 取得 UserInfo 的端口 <br>
	 * 例如: <br>
	 * https://127.0.0.1:8080/dgrv4/ssotoken/gtwidp/v2/userInfo <br>
	 */
	public static String getUserinfoEndpoint(String schemeAndDomainAndPort) {
		String userinfoEndpoint = String.format("%s/dgrv4/ssotoken/gtwidp/v2/userInfo", schemeAndDomainAndPort);

		return userinfoEndpoint;
	}
	
	/**
	 * digiRunner 的 callback URL <br>
	 * 例如: <br>
	 * https://127.0.0.1:8080/dgrv4/ssotoken/gtwidp/GOOGLE/gtwIdPCallback <br>
	 */
	public static String getCallbackEndpoint(String schemeAndDomainAndPort, String idPType) {
		String tokenEndpoint = String.format("%s/dgrv4/ssotoken/gtwidp/%s/gtwIdPCallback",
				schemeAndDomainAndPort,
				idPType);
		
		return tokenEndpoint;
	}
	
	/**
	 * 放 JWK Set 的公鑰內容 <br>
	 * 例如: <br>
	 * https://127.0.0.1:8080/dgrv4/ssotoken/oauth2/certs <br>
	 */
	public static String getJwksUri(String schemeAndDomainAndPort) {
		String jwksUri = String.format("%s/dgrv4/ssotoken/oauth2/certs",
				schemeAndDomainAndPort);
		
		return jwksUri;
	}
	
	protected ObjectMapper getObjectMapper() {
		return objectMapper;
	}
	
	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected TokenHelper getTokenHelper() {
		return tokenHelper;
	}
}
