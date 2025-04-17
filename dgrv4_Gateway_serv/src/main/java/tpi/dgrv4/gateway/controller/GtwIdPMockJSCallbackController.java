package tpi.dgrv4.gateway.controller;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.codec.utils.JWKcodec;
import tpi.dgrv4.gateway.constant.DgrTokenGrantType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.GtwIdPWellKnownService;
import tpi.dgrv4.gateway.service.OAuthTokenService;
import tpi.dgrv4.gateway.service.TsmpSettingService;

/**
 * 模擬 GTW IdP 流程中的 TSP Callback, 用 auth code 取 token,
 * https://localhost:8080/dgrv4/mocktenancy/gtwcallback
 * @author Mini
 */
@RestController
public class GtwIdPMockJSCallbackController {
	
	private ObjectMapper objectMapper;
	private TsmpSettingService tsmpSettingService;
	private OAuthTokenService oAuthTokenService;
	
	@Autowired
	public GtwIdPMockJSCallbackController(ObjectMapper objectMapper, TsmpSettingService tsmpSettingService,
			OAuthTokenService oAuthTokenService) {
		super();
		this.objectMapper = objectMapper;
		this.tsmpSettingService = tsmpSettingService;
		this.oAuthTokenService = oAuthTokenService;
	}

	@GetMapping(value = "/dgrv4/mocktenancy/gtwcallback",
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getToken(@RequestHeader HttpHeaders httpHeaders, 
			HttpServletRequest httpReq,
			HttpServletResponse httpResp) throws IOException {
		
		TPILogger.tl.info("\n--【" + httpReq.getRequestURL().toString() + "】--");
		
		String code = httpReq.getParameter("code");
		if (StringUtils.hasLength(code)) { // 取得 auth code
			return getToken(httpResp, code);

		} else {
			String rtnCode = httpReq.getParameter("rtn_code");
			String msg = httpReq.getParameter("msg");
			if(StringUtils.hasLength(msg)) {
				msg = new String(Base64Util.base64Decode(msg));
			}
			
			Map<String, String> respDataMap = new HashMap<>();
			respDataMap.put("title", "這是模擬 client 的 redirect uri");
			respDataMap.put("redirect_uri", httpReq.getRequestURL().toString());
			respDataMap.put("desc", "由 TSP 處理,是否回到登入畫面");
			respDataMap.put("rtn_code", rtnCode);
			respDataMap.put("msg", msg);
 
			String jsonStr = getObjectMapper().writeValueAsString(respDataMap);
			jsonStr = JWKcodec.toPrettyJson(jsonStr);
			return new ResponseEntity<Object>(jsonStr, HttpStatus.OK);
		}
	}
	
	private ResponseEntity<?> getToken(HttpServletResponse httpResp, String code) throws IOException {
		String clientId = "gtwclient";
		String clientMima = "gtwclient123";
		
//		String clientId = "ldapclient";
//		String clientMima = "ldapclient123";
		
//		String clientId = "minildapclient";
//		String clientMima = "minildapclient123";

		clientMima = Base64Util.base64Encode(clientMima.getBytes());// Base64 Encode

		// 對外公開的域名或IP
		String dgrPublicDomain = getTsmpSettingService().getVal_DGR_PUBLIC_DOMAIN();
		// 對外公開的Port
		String dgrPublicPort = getTsmpSettingService().getVal_DGR_PUBLIC_PORT();

		String schemeAndDomainAndPort = GtwIdPWellKnownService.getSchemeAndDomainAndPort(dgrPublicDomain,
				dgrPublicPort);

		String tokenEndpoint = GtwIdPWellKnownService.getTokenEndpoint(schemeAndDomainAndPort);

		String clientRedirectUrl = "https://localhost:8080/dgrv4/mocktenancy/gtwcallback";
		
//		return getToken(clientId, clientMima, tokenEndpoint, clientRedirectUrl, code, httpResp);
	
		return getToken(httpResp, clientId, clientMima, clientRedirectUrl, code);
	}
    
    /*
     * 不使用URL, 直接從 OAuthTokenService 取 Token		
     */
	private ResponseEntity<?> getToken(HttpServletResponse httpResp, String clientId, String clientMima,
			String callbackUrl, String authCode) {
        String idpwd = String.format("%s:%s", clientId, clientMima);
        byte[] authorizationArr = Base64.getEncoder().withoutPadding().encode(idpwd.getBytes());
        
//        Map<String, String> header = new HashMap<>();
//        header.put("Authorization", "Basic " + new String(authorizationArr));
//        TPILogger.tl.debug(header.get("Authorization"));
        
        String authorization = "Basic " + new String(authorizationArr);
        
        Map<String, String> formData = new HashMap<>();
        formData.put("grant_type", DgrTokenGrantType.AUTHORIZATION_CODE);
        formData.put("redirect_uri", callbackUrl);
        formData.put("code", authCode);
        
        // for PKCE
        formData.put("code_verifier", "minitestpkce");
    	
		ResponseEntity<?> respObj = getOAuthTokenService().getToken(httpResp, formData, authorization,
				"/oauth/token");
		return respObj;
    }
    
    protected ObjectMapper getObjectMapper() {
    	return objectMapper;
    }
	
	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}
	
	protected OAuthTokenService getOAuthTokenService() {
		return oAuthTokenService;
	}
}
