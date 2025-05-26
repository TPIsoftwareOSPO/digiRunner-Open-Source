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
     * 打 IdP 的 UserInfo API, 取得 User 資料
     */
	public UserInfoData getUserInfoData(String userInfoUrl, String accessTokenJwtstr, String reqUri)
			throws IOException {

		UserInfoData userInfoData = new UserInfoData();

		Map<String, List<String>> header = new HashMap<>();
		header.put("Authorization", Arrays.asList("Bearer " + accessTokenJwtstr));

		HttpRespData userInfoResp = HttpUtil.httpReqByGetList(userInfoUrl, header, false, false);
		TPILogger.tl.trace(userInfoResp.getLogStr());
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
