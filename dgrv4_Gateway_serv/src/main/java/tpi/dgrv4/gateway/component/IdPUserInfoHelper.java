package tpi.dgrv4.gateway.component;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.codec.utils.IdTokenUtil;
import tpi.dgrv4.common.keeper.ITPILogger;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.JsonNodeUtil;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

@Component
public class IdPUserInfoHelper {
	
	private ObjectMapper objectMapper;
	private TokenHelper tokenHelper;
	
	public static class UserInfoData {
		public ResponseEntity<?> errRespEntity;
		public String errMsg;
		public String userName;
		public String userAlias;
		public String userEmail;
		public String userPicture;
	}
    
	@Autowired
    public IdPUserInfoHelper(ObjectMapper objectMapper, TokenHelper tokenHelper) {
		super();
		this.objectMapper = objectMapper;
		this.tokenHelper = tokenHelper;
	}
	
	/**
	 * GTW IdP 登入流程,
	 * 打 IdP 的 UserInfo API, 取得 User 資料
	 */
	public UserInfoData getUserInfoData(String userInfoUrl, String accessTokenJwtstr, String reqUri)
			throws IOException {
		return getUserInfoData(userInfoUrl, accessTokenJwtstr, reqUri, null, IdTokenUtil.SUB, false);
	}

	/**
     * 打 IdP 的 UserInfo API, 取得 User 資料
     */
	public UserInfoData getUserInfoData(String userInfoUrl, String accessTokenJwtstr, String reqUri,
			String acIdpOauth2UsernameKey, String idpOauth2UsernameVal, boolean isIdpUsernameB64EncodeVal)
			throws IOException {

		UserInfoData userInfoData = new UserInfoData();

		Map<String, List<String>> header = new HashMap<>();
		header.put("Authorization", Arrays.asList("Bearer " + accessTokenJwtstr));

		// 打 IdP 的 UserInfo API
		HttpRespData userInfoResp = HttpUtil.httpReqByGetList(userInfoUrl, header, false, false);
		TPILogger.tl.debug(userInfoResp.getLogStr());
		
		int statusCode = userInfoResp.statusCode;
		if (statusCode >= 300) {
			String errMsg = String.format("Userinfo API Failed, HTTP Status Code '%s' : %s", statusCode + "",
					userInfoResp.respStr);
			TPILogger.tl.debug(errMsg);
			userInfoData.errRespEntity = getTokenHelper().getUnauthorizedErrorResp(reqUri, errMsg);// 401
			userInfoData.errMsg = errMsg;
			return userInfoData;
		}

		JsonNode userInfoJson = getObjectMapper().readTree(userInfoResp.respStr);
		String sub = JsonNodeUtil.getNodeAsText(userInfoJson, "sub");// user sub
		String name = JsonNodeUtil.getNodeAsText(userInfoJson, "name");// user name
		String email = JsonNodeUtil.getNodeAsText(userInfoJson, "email");// user email
		String picture = JsonNodeUtil.getNodeAsText(userInfoJson, "picture");// user picture
 
		// userName 取 UserInfo 的 sub, 
		// 例如: "sub": "101872102234493560934"
		userInfoData.userName = sub;
		
		if (!StringUtils.hasLength(idpOauth2UsernameVal)) {// 沒有值, 則預設為 sub
			idpOauth2UsernameVal = IdTokenUtil.SUB;
		}
		
		// 核發 AC OAuth 2.0 IdP token (GOOGLE / MS / OIDC) , 
		// 其中 username 從 IdP ID token 的什麼參數取得. (預設: sub)
		if (!IdTokenUtil.SUB.equals(idpOauth2UsernameVal)) {// 若不是 'sub'
			String val = JsonNodeUtil.getNodeAsText(userInfoJson, idpOauth2UsernameVal);

			if (StringUtils.hasLength(val)) {// 有取到參數值
				userInfoData.userName = val;

			} else {
				// 在 IdP UserInfo response 找不到指定的參數,或此參數沒有值, param '{xxx}'. 請確認 setting 的
				// 'AC_IDP_OAUTH2_USERNAME'
				String errMsg = "[AC OAuth 2.0 IdP] The specified parameter was not found in the IdP UserInfo response, or the parameter has no value"
						+ ", parameter name '" + idpOauth2UsernameVal + "', please verify the setting for '"
						+ acIdpOauth2UsernameKey + "'";
				ITPILogger.tl.debug(errMsg);
				userInfoData.errRespEntity = getTokenHelper().getUnauthorizedErrorResp(reqUri, errMsg);// 401
				userInfoData.errMsg = errMsg;
				return userInfoData;
			}
		}
		
		ITPILogger.tl.debug("username : " + userInfoData.userName);
		
		if (userInfoData.userName.contains(".") && !isIdpUsernameB64EncodeVal) {// 當使用者名稱有包含 "." 且 沒有打開編碼
			// 使用者名稱 '%s', 不能包含 ".", 請確認 'AC_IDP_USERNAME_B64_ENCODE' 是否要設定''為'true'
			String errMsg = String.format(
					"Username '%s' cannot contain '.', Please confirm that Setting 'AC_IDP_USERNAME_B64_ENCODE' should be set to 'true'.",
					userInfoData.userName);
			userInfoData.errRespEntity = getTokenHelper().getUnauthorizedErrorResp(reqUri, errMsg);// 401
			userInfoData.errMsg = errMsg;
			return userInfoData;
		}
		
		// userEmail 取 UserInfo 的 email
		userInfoData.userEmail = email;
		
		// userPicture 取 IdToken 的 picture
		userInfoData.userPicture = picture;
		
		// userAlias 取 UserInfo 的 (name / email / sub)
		// 它的值來自於以下順序: user info.(name / email / sub)
		String userAlias = name;
		if(!StringUtils.hasLength(userAlias)) {
			userAlias = email;
		}
		
		if(!StringUtils.hasLength(userAlias)) {
			userAlias = sub;
		}
		
		userInfoData.userAlias = userAlias;
		
		return userInfoData;
	}

	protected ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	protected TokenHelper getTokenHelper() {
		return tokenHelper;
	}
}
