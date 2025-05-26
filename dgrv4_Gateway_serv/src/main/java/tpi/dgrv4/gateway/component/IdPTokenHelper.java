package tpi.dgrv4.gateway.component;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.constant.DgrTokenGrantType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.JsonNodeUtil;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

@Component
public class IdPTokenHelper {
	
	private ObjectMapper objectMapper;
	private TokenHelper tokenHelper;
	
	public static class TokenData {
		public ResponseEntity<?> errRespEntity;
		public String errMsg;
		public String idToken;
		public String accessToken;
		public String refreshToken;
		public String apiResp;
	}
	
	@Autowired
    public IdPTokenHelper(ObjectMapper objectMapper, TokenHelper tokenHelper) {
		super();
		this.objectMapper = objectMapper;
		this.tokenHelper = tokenHelper;
	}

	/**
     *  用授權碼打 IdP(GOOGLE/MS) 的 token API, <br>
     *  取得 Access token、Refresh token、ID token <br>
     */
	public TokenData getTokenData(String idPType, String clientId, String clientMima, String idpTokenUrl, String callbackUrl,
			String authCode, String codeVerifierForOauth2, String reqUri) throws IOException {
		TokenData tokenData = new TokenData();
    	
        try {
        	String idpwd = String.format("%s:%s", clientId, clientMima);
        	byte[] Authorization = Base64.getEncoder().withoutPadding().encode(idpwd.getBytes());
        	
        	Map<String, String> header = new HashMap<>();
        	header.put("Authorization", "Basic " + new String(Authorization));
        	
        	Map<String, String> formData = new HashMap<>();
			formData.put("grant_type", DgrTokenGrantType.AUTHORIZATION_CODE);
			formData.put("redirect_uri", callbackUrl);
			formData.put("code", authCode);
			formData.put("code_verifier", codeVerifierForOauth2);// PKCE
        	
        	HttpRespData tokenResp = HttpUtil.httpReqByX_www_form_urlencoded_UTF8(idpTokenUrl, "POST", formData, header,
        			false);
        	TPILogger.tl.info(tokenResp.getLogStr());
        	int statusCode = tokenResp.statusCode;
        	
        	if (statusCode >= 300) {
        		// IdP(GOOGLE/MS) Token API Failed, HTTP Status Code '401' : %s"
				String errMsg = String.format("IdP(%s) Token API Failed, HTTP Status Code '%s': %s" //
						, idPType //
						, statusCode + "" //
						, tokenResp.respStr //
				);
        		TPILogger.tl.debug(errMsg);
        		tokenData.errRespEntity = getTokenHelper().getUnauthorizedErrorResp(reqUri, errMsg);// 401
        		tokenData.errMsg = errMsg;
        		return tokenData;
        	}
        	
        	String apiResp = tokenResp.respStr;
        	JsonNode tokenJson = getObjectMapper().readTree(apiResp);
        	String idToken = JsonNodeUtil.getNodeAsText(tokenJson, "id_token");// ID token
        	String accessToken = JsonNodeUtil.getNodeAsText(tokenJson, "access_token");// Access token
        	String refreshToken = JsonNodeUtil.getNodeAsText(tokenJson, "refresh_token");// Refresh token
        	
        	tokenData.idToken = idToken;
        	tokenData.accessToken = accessToken;
        	tokenData.refreshToken = refreshToken;
        	tokenData.apiResp = apiResp;
			
		} catch (Exception e) {
			TPILogger.tl.debug(StackTraceUtil.logStackTrace(e));
			
			String errMsg = String.format("Failed to get token");
			TPILogger.tl.debug(errMsg);
			tokenData.errRespEntity = getTokenHelper().getUnauthorizedErrorResp(reqUri, errMsg);// 401
			tokenData.errMsg = errMsg;
		}
        
        return tokenData;
    }
 
	protected ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	protected TokenHelper getTokenHelper() {
		return tokenHelper;
	}
}
