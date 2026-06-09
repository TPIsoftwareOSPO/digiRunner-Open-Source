package tpi.dgrv4.gateway.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tpi.dgrv4.codec.utils.JWKcodec;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.escape.ESAPI;
import tpi.dgrv4.gateway.component.GtwIdPHelper;
import tpi.dgrv4.gateway.component.OIDCWellKnownHelper;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.GtwIdPWellKnownResp;

@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Service
public class GtwIdPWellKnownService {
	
	private final ObjectMapper objectMapper;
	private final TsmpSettingService tsmpSettingService;
	private final TokenHelper tokenHelper;

	public ResponseEntity<?> getGtwIdPWellKnown(HttpHeaders httpHeaders, HttpServletRequest httpReq,
			HttpServletResponse httpResp, String idPType, String pathVer) throws Exception {
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

			String respJsonStr = getGtwIdPWellKnown(idPType, pathVer);
			//checkmarx, Reflected XSS All Clients 
			respJsonStr = ESAPI.encoder().encodeForHTML(respJsonStr);
			return new ResponseEntity<Object>(respJsonStr, HttpStatus.OK);

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			String errMsg = TokenHelper.INTERNAL_SERVER_ERROR;
			TPILogger.tl.error(errMsg);
			//checkmarx, Reflected XSS All Clients 
			reqUri = ESAPI.encoder().encodeForHTML(reqUri);
			respEntity = TokenHelper.getInternalServerErrorResp(reqUri, errMsg);// 500
			return respEntity;
		}
	}
	
	/**
	 * 取得 well-known 內容
	 */
	private String getGtwIdPWellKnown(String idPType, String pathVer) throws Exception {
		// 對外公開的域名或IP
		String dgrPublicDomain = getTsmpSettingService().getVal_DGR_PUBLIC_DOMAIN();
		// 對外公開的Port
		String dgrPublicPort = getTsmpSettingService().getVal_DGR_PUBLIC_PORT();
		
		String schemeAndDomainAndPort = OIDCWellKnownHelper.getSchemeAndDomainAndPort(dgrPublicDomain, dgrPublicPort);
		
		String issuer = OIDCWellKnownHelper.getIssuer(schemeAndDomainAndPort, idPType, pathVer);
		String authorizationEndpoint = OIDCWellKnownHelper.getAuthorizationEndpoint(schemeAndDomainAndPort, idPType);
		String tokenEndpoint = OIDCWellKnownHelper.getTokenEndpoint(schemeAndDomainAndPort, pathVer);
		String userinfoEndpoint = OIDCWellKnownHelper.getUserinfoEndpoint(schemeAndDomainAndPort);
		String jwksUri = OIDCWellKnownHelper.getJwksUri(schemeAndDomainAndPort);
		String callbackEndpoint = OIDCWellKnownHelper.getCallbackEndpoint(schemeAndDomainAndPort, idPType);
		
		// 支援哪些 Scope (scopes_supported)
		List<String> scopesSupportedList = GtwIdPHelper.getScopesSupportedList();
		
		// 支援哪些 Code_challenge_method
		List<String> codeChallengeMethodsSupportedList = GtwIdPHelper.getCodeChallengeMethodsSupported();
		
		GtwIdPWellKnownResp gtwIdPWellKnownResp = new GtwIdPWellKnownResp();
		gtwIdPWellKnownResp.setIssuer(issuer);
		gtwIdPWellKnownResp.setAuthorizationEndpoint(authorizationEndpoint);
		gtwIdPWellKnownResp.setTokenEndpoint(tokenEndpoint);
		gtwIdPWellKnownResp.setUserinfoEndpoint(userinfoEndpoint);
		gtwIdPWellKnownResp.setJwksUri(jwksUri);
		gtwIdPWellKnownResp.setIdTokenSigningAlgValuesSupported(Arrays.asList("RS256"));
		gtwIdPWellKnownResp.setScopesSupported(scopesSupportedList);
		
		if (OIDCWellKnownHelper.isShowCallbackEndpoint(idPType)) {
			// OAuth 2.0 的 IdP type 才需要顯示 CallbackEndpoint
			gtwIdPWellKnownResp.setCallbackEndpoint(callbackEndpoint);
		}

		gtwIdPWellKnownResp.setCodeChallengeMethodsSupported(codeChallengeMethodsSupportedList);
		
		String respJsonStr = getObjectMapper().writeValueAsString(gtwIdPWellKnownResp);
		respJsonStr = JWKcodec.toPrettyJson(respJsonStr);
		return respJsonStr;
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
