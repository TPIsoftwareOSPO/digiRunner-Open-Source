package tpi.dgrv4.gateway.component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.constant.DgrTokenGrantType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.JsonNodeUtil;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Component
public class IdPTokenHelper {
	
	private final ObjectMapper objectMapper;
	private final TokenHelper tokenHelper;
	
	public static class TokenData {
		public ResponseEntity<?> errRespEntity;
		public String errMsg;
		public String idToken;
		public String accessToken;
		public String refreshToken;
		public String apiResp;
	}

	/**
     *  用授權碼打 IdP(GOOGLE/MS) 的 token API, <br>
     *  取得 Access token、Refresh token、ID token <br>
     */
	public TokenData getTokenData(String idPType, String clientId, String clientMima, String idpTokenUrl, String callbackUrl,
			String authCode, String codeVerifierForOauth2, String reqUri) throws IOException {
		TokenData tokenData = new TokenData();
    	
		try {
			
			if (!StringUtils.hasLength(codeVerifierForOauth2)) {// Cookies 過期,取不到值
				// 登入時間過長, 超過 5 分鐘, 請重新登入
				String errMsg = "Login time too long, exceeding 5 minutes, please log in again.";
				TPILogger.tl.debug(errMsg);
				tokenData.errRespEntity = getTokenHelper().getUnauthorizedErrorResp(reqUri, errMsg);// 401
				tokenData.errMsg = errMsg;
				return tokenData;
			}
			
			String idpwd = String.format("%s:%s", clientId, clientMima);
			Map<String, String> header = new HashMap<>();

			byte[] basicByte = idpwd.getBytes(StandardCharsets.UTF_8);
			String basicString = "Basic " + Base64Util.base64Encode(basicByte);
			header.put("Authorization", basicString);

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
				// IdP(GOOGLE/MS/OIDC) response fail message(/auth/token): HTTP Status Code
				// '401' : %s"
				String errMsg = String.format("IdP(%s) response fail message(/auth/token): HTTP Status Code '%s': %s" //
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
