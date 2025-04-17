package tpi.dgrv4.gateway.component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.SHA256Util;

/**
 * AC IdP & GTW IdP 流程的共用程式
 * 
 * @author Mini
 */

@Component
public class IdPHelper {

	private TokenHelper tokenHelper;

	// 訊息:無效的 IdP type
	public static String MSG_INVALID_IDPTYPE = "Invalid IdP type: %s";

	// 訊息:沒有支援此 IdP type
	public static String MSG_UNSUPPORTED_IDP_TYPE = "Unsupported IdP type: %s";

	// 訊息:缺少必填參數 '%s'
	public static String MSG_MISSING_REQUIRED_PARAMETER = "Missing required parameter '%s'.";

	// 找不到可用的 Cus Idp Info 時的錯誤訊息。
	public static final String MSG_NO_AVAILABLE_CUS_IDP_INFO = "No available Cus Idp Info found";

	// 找不到指定的 Cus Idp Info 時的錯誤訊息。
	public static final String MSG_SPECIFIED_CUS_IDP_INFO_NOT_FOUND = "Specified Cus Idp Info not found. Specified Cus Idp Info ID: %s";

	// 預設的登入畫面圖示和標題
	public static final String DEFULT_ICON_FILE = "data:image/gif;base64,R0lGODdhLAAnAHAAACwAAAAALAAnAIcAAAAAADMAAGYAAJkAAMwAAP8AKwAAKzMAK2YAK5kAK8wAK/8AVQAAVTMAVWYAVZkAVcwAVf8AgAAAgDMAgGYAgJkAgMwAgP8AqgAAqjMAqmYAqpkAqswAqv8A1QAA1TMA1WYA1ZkA1cwA1f8A/wAA/zMA/2YA/5kA/8wA//8zAAAzADMzAGYzAJkzAMwzAP8zKwAzKzMzK2YzK5kzK8wzK/8zVQAzVTMzVWYzVZkzVcwzVf8zgAAzgDMzgGYzgJkzgMwzgP8zqgAzqjMzqmYzqpkzqswzqv8z1QAz1TMz1WYz1Zkz1cwz1f8z/wAz/zMz/2Yz/5kz/8wz//9mAABmADNmAGZmAJlmAMxmAP9mKwBmKzNmK2ZmK5lmK8xmK/9mVQBmVTNmVWZmVZlmVcxmVf9mgABmgDNmgGZmgJlmgMxmgP9mqgBmqjNmqmZmqplmqsxmqv9m1QBm1TNm1WZm1Zlm1cxm1f9m/wBm/zNm/2Zm/5lm/8xm//+ZAACZADOZAGaZAJmZAMyZAP+ZKwCZKzOZK2aZK5mZK8yZK/+ZVQCZVTOZVWaZVZmZVcyZVf+ZgACZgDOZgGaZgJmZgMyZgP+ZqgCZqjOZqmaZqpmZqsyZqv+Z1QCZ1TOZ1WaZ1ZmZ1cyZ1f+Z/wCZ/zOZ/2aZ/5mZ/8yZ///MAADMADPMAGbMAJnMAMzMAP/MKwDMKzPMK2bMK5nMK8zMK//MVQDMVTPMVWbMVZnMVczMVf/MgADMgDPMgGbMgJnMgMzMgP/MqgDMqjPMqmbMqpnMqszMqv/M1QDM1TPM1WbM1ZnM1czM1f/M/wDM/zPM/2bM/5nM/8zM////AAD/ADP/AGb/AJn/AMz/AP//KwD/KzP/K2b/K5n/K8z/K///VQD/VTP/VWb/VZn/Vcz/Vf//gAD/gDP/gGb/gJn/gMz/gP//qgD/qjP/qmb/qpn/qsz/qv//1QD/1TP/1Wb/1Zn/1cz/1f///wD//zP//2b//5n//8z///8AAAAAAAAAAAAAAAAI/wD3CRxIsKDBgwgTKlzIsKHDhxAhylMnL6JFgvPUrdOobt5FiPo2rgsWb51IfR8ZTjRZ0uS6Yxsrpjw4zyVMdfGAtXSpLt/MgfomktQYL5hJdUOLbmSHMqU8kS7jEV3X0mhLjTIjhkRKdZ0xkzBdroNHVSPJYBqbOmSnsWvUtl+VRhW7tGFGsiQ3BiM7sqRfjcdwopVaMnBHhXpZTn3ZNh4YAGDoUjUKNfC6hCt39p37GAAAGCXhDWWZN6bCkMC8ljUJz+9IGFbdUk19NB69hvnaUo4X6XNX14/BxNsJT12yiITHLvLsO2pnz6OPfdTXsqiWSITj8Q3G3Ldai5oAXJZi+bt3GL/xwnRX9jEGcxgbWz4HoMsvd+YxIqbpzvw81Xi7dBeZIvx5hkZDyhTIH1r/LadggcQodMOD/IEW3zoU8ndDQhkWqEgwvXXYnUJiiGhigWIwBI17J4oYA3sOZdJihplcNOGM3W2YUoI4AgDjT5O0mMZPBrFIYX5EHkQMhREmmVCJ3aXoJEMsTmnllVhmqeVHAQEAOw==";
	public static final String DEFULT_PAGE_TITLE = "digiRunner";

	@Autowired
	public IdPHelper(TokenHelper tokenHelper) {
		super();
		this.tokenHelper = tokenHelper;
	}

	/**
	 * 取得打 IdP(Google/MS) Auth API(使用 PKCE) 的 URL
	 */
	public static String getRedirectUrl(String clientId, String authUrl, String scope, String callbackUrl,
			String codeVerifierForOauth2) throws Exception {
		String codeChallengeForOauth2 = SHA256Util.getSHA256ToBase64Url(codeVerifierForOauth2);
		
		String redirectUrl = String.format(
				"%s" 
				+ "?response_type=code" 
				+ "&client_id=%s" 
				+ "&scope=%s" 
				+ "&redirect_uri=%s"
				+ "&code_challenge=%s" 
				+ "&code_challenge_method=S256"
				, authUrl
				, clientId
				, URLEncoder.encode(scope, StandardCharsets.UTF_8.toString())
				, URLEncoder.encode(callbackUrl, StandardCharsets.UTF_8.toString())
				, codeChallengeForOauth2
		);
 
		return redirectUrl;
	}
	
	public static String getUrlEncode(String data) throws Exception {
		if (StringUtils.hasLength(data)) {
			data = URLEncoder.encode(data, StandardCharsets.UTF_8.toString());
		}
		return data;
	}

	/**
	 * 取得授權碼有效日期, <br>
	 * 和 Microsoft、Line、Open banking 相同,預設10分鐘, <br>
	 * 以現在時間 + 授權碼有效期限, 計算出有效日期 <br>
	 */
	public static Date getAuthCodeExpiredTime() {
		int val = 10 * 60 * 1000;// 10分鐘, 單位毫秒
		LocalDateTime ldt = LocalDateTime.now().plus(val, ChronoUnit.MILLIS);
		ZonedDateTime zdt = ldt.atZone(ZoneId.systemDefault());
		Date expiredTime = Date.from(zdt.toInstant());
		return expiredTime;
	}

	protected TokenHelper getTokenHelper() {
		return tokenHelper;
	}
}
