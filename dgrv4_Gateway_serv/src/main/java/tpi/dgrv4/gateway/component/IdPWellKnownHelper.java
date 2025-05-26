package tpi.dgrv4.gateway.component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.JsonNodeUtil;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

@Component
public class IdPWellKnownHelper {
	
	private ObjectMapper objectMapper;
	private TokenHelper tokenHelper;
	
	public static class WellKnownData {
		public ResponseEntity<?> errRespEntity;
		public String errMsg;
		
		public String issuer;
		public String authorizationEndpoint;
		public String tokenEndpoint;
		public String userinfoEndpoint;
		public String jwksUri;
		public String scopeStr;
	}

	@Autowired
	public IdPWellKnownHelper(ObjectMapper objectMapper, TokenHelper tokenHelper) {
		super();
		this.objectMapper = objectMapper;
		this.tokenHelper = tokenHelper;
	}

	/*
	 * 打 IdP Well Known URL, 取得 JSON 資料
	 */
	public WellKnownData getWellKnownData(String wellKnownUrl, String reqUri)
			throws IOException {
		WellKnownData wellKnownData = new WellKnownData();
		
		try {
			Map<String, List<String>> header = new HashMap<>();
			HttpRespData wellKnownResp = HttpUtil.httpReqByGetList(wellKnownUrl, header, false, false);
			TPILogger.tl.debug(wellKnownResp.getLogStr());
			int statusCode = wellKnownResp.statusCode;
			String respStr = wellKnownResp.respStr;
			if (statusCode >= 300) {
				String errMsg = String.format("Well-known API failed, HTTP status code '%s' : %s",
						statusCode + "",
						respStr);
				TPILogger.tl.debug(errMsg);
				wellKnownData.errRespEntity = getTokenHelper().getUnauthorizedErrorResp(reqUri, errMsg);// 401
				wellKnownData.errMsg = errMsg;
				return wellKnownData;
			}
			
			// response 沒有資料
			if(!StringUtils.hasLength(respStr)) {
				String errMsg = String.format("Well-known API failed, response no information. well known URL: %s",
						wellKnownUrl);
				TPILogger.tl.debug(errMsg);
				wellKnownData.errRespEntity = getTokenHelper().getUnauthorizedErrorResp(reqUri, errMsg);// 401
				wellKnownData.errMsg = errMsg;
				return wellKnownData;
			}
			
			JsonNode wellKnownJson = getObjectMapper().readTree(respStr);
			
			// 取得 issuer
			// 例如: "issuer": "https://accounts.google.com"
			wellKnownData.issuer = JsonNodeUtil.getNodeAsText(wellKnownJson, "issuer");
			
			// 取得 auth URL
			// 例如: "authorization_endpoint": "https://accounts.google.com/o/oauth2/v2/auth"
			wellKnownData.authorizationEndpoint = JsonNodeUtil.getNodeAsText(wellKnownJson, "authorization_endpoint");
			
			// 取得 token URL
			// 例如: "token_endpoint": "https://oauth2.googleapis.com/token"
			wellKnownData.tokenEndpoint = JsonNodeUtil.getNodeAsText(wellKnownJson, "token_endpoint");
			
			// 取得 UserInfo URL
			// 例如: "userinfo_endpoint": "https://openidconnect.googleapis.com/v1/userinfo"
			wellKnownData.userinfoEndpoint = JsonNodeUtil.getNodeAsText(wellKnownJson, "userinfo_endpoint");
			
			// 取得 Jwks Uri
			// 例如: "jwks_uri": "https://www.googleapis.com/oauth2/v3/certs"
			wellKnownData.jwksUri = JsonNodeUtil.getNodeAsText(wellKnownJson, "jwks_uri");
			
			// 取得 scope
			wellKnownData.scopeStr = getScopeStr(wellKnownJson);

		} catch (Exception e) {
			String errMsg = "Well-known API failed. well known URL: " + wellKnownUrl;
			TPILogger.tl.debug(errMsg);
			TPILogger.tl.debug(StackTraceUtil.logStackTrace(e));
			
			wellKnownData.errRespEntity = getTokenHelper().getUnauthorizedErrorResp(reqUri, errMsg);// 401
			wellKnownData.errMsg = errMsg;
		}

		return wellKnownData;
	}
 
	/**
	 * 取得 scope <br>
	 * 例如: "scopes_supported": ["openid", "email", "profile"] <br>
	 * 
	 * @param wellKnownNode
	 * @return scope 字串, 多個值以空白分隔, 例如 "openid email profile"
	 */
	private String getScopeStr(JsonNode wellKnownNode) {
		JsonNode scopeArray = wellKnownNode.get("scopes_supported");
		String scopeStr = JsonNodeUtil.convertJsonArrayToString(scopeArray, " ");// 多個值以空白分隔
		
		return scopeStr;
	}
	
	protected ObjectMapper getObjectMapper() {
		return objectMapper;
	}
 
	protected TokenHelper getTokenHelper() {
		return tokenHelper;
	}
}
