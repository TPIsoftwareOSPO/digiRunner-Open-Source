package tpi.dgrv4.dpaa.util;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.copy.BCryptPasswordEncoder;

import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.codec.utils.BcryptUtil;
import tpi.dgrv4.codec.utils.SHA256Util;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.utils.ServiceUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.DgrAuditLogService;
import tpi.dgrv4.entity.entity.OauthClientDetails;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.repository.OauthClientDetailsDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.gateway.util.InnerInvokeParam;

public class OAuthUtil {

	private static OauthClientDetailsDao oauthClientDetailsDao;
	private static TsmpClientDao tsmpClientDao;
	private static DgrAuditLogService dgrAuditLogService;

	/**
	 * @param clientId: base64 encoded
	 * @param clientBlock: base64 encoded
	 */
	public static void saveAuth(String clientId, String resourceIds, String clientBlock, InnerInvokeParam iip) {
		if (oauthClientDetailsDao != null) {
			OauthClientDetails auth = new OauthClientDetails();
			auth.setClientId(clientId);
			auth.setResourceIds(resourceIds);
			String clientSecret = bCryptEncode(clientBlock);
			auth.setClientSecret(clientSecret);
			auth.setScope("select");
			auth.setAuthorizedGrantTypes("client_credentials,refresh_token,password,authorization_code");
			auth.setAuthorities("client");
			auth.setAdditionalInformation("{}");
			auth.setAutoapprove("");
			auth.setWebServerRedirectUri("");
			auth.setWebServerRedirectUri1("");
			auth.setWebServerRedirectUri2("");
			auth.setWebServerRedirectUri3("");
			auth.setWebServerRedirectUri4("");
			auth.setWebServerRedirectUri5("");
			auth = oauthClientDetailsDao.saveAndFlush(auth);
			
			if(dgrAuditLogService != null) {
				//寫入 Audit Log D
				String lineNumber = StackTraceUtil.getLineNumber();
				dgrAuditLogService.createAuditLogD(iip, lineNumber, 
						OauthClientDetails.class.getSimpleName(), TableAct.C.value(), null, auth);
			}
			
			updateSha512ClientSecret(clientId, clientBlock, iip);
		}
	}

	/**
	 * 更新密碼
	 * @param clientId
	 * @param clientBlock
	 * @return
	 */
	public static OauthClientDetails updateClientSecret(String clientId, String clientBlock, InnerInvokeParam iip) {
		OauthClientDetails ocd = null;

		if (oauthClientDetailsDao != null) {
			Optional<OauthClientDetails> opt = oauthClientDetailsDao.findById(clientId);
			if (opt.isPresent()) {
				ocd = opt.get();
				String clientSecret = bCryptEncode(clientBlock);
				String oldRowStr = null;
				if(dgrAuditLogService != null) {
					oldRowStr = dgrAuditLogService.writeValueAsString(iip, ocd); //舊資料統一轉成 String
				}

				ocd.setClientSecret(clientSecret);
				ocd = oauthClientDetailsDao.save(ocd);
				if(dgrAuditLogService != null) {
					//寫入 Audit Log D
					String lineNumber = StackTraceUtil.getLineNumber();
					dgrAuditLogService.createAuditLogD(iip, lineNumber, 
							OauthClientDetails.class.getSimpleName(), TableAct.U.value(), oldRowStr, ocd);
				}
			}
			
			updateSha512ClientSecret(clientId, clientBlock, null);
		}

		return ocd;
	}
	
	/**
	 * 只要有異動到 Client pwd (OAUTH_CLIENT_DETAILS.client_secret) 這個欄位,	
	 * 都要將 Client pwd 經過 SHA512URL base64 hash 的值,更新至 TSMP_CLIENT.client_secret	
	 */
	public static void updateSha512ClientSecret(String clientId, String clientBlock, InnerInvokeParam iip) {
		TsmpClient tsmpClient = tsmpClientDao.findById(clientId).orElse(null);
		if(tsmpClient != null) {
			String oldRowStr = null;
			if(dgrAuditLogService != null && iip != null) {
				oldRowStr = dgrAuditLogService.writeValueAsString(iip, tsmpClient); //舊資料統一轉成 String
			}
			
			String clientPwd = new String(Base64Util.base64Decode2(clientBlock));//Client PW,經過 Base64 Decode 的值
			String sha512Pwd = SHA256Util.getSHA512ToBase64(clientPwd);
			tsmpClient.setClientSecret(sha512Pwd);
			tsmpClient = tsmpClientDao.save(tsmpClient);
			
			if(dgrAuditLogService != null && iip != null) {
				//寫入 Audit Log D
				String lineNumber = StackTraceUtil.getLineNumber();
				dgrAuditLogService.createAuditLogD(iip, lineNumber, 
						TsmpClient.class.getSimpleName(), TableAct.U.value(), oldRowStr, tsmpClient);
			}
		}
	}

	/**
	 * 密碼編碼
	 * @param str
	 * @return
	 */
	public static String bCryptEncode(String str) {
		//checkmarx, Spring Comparison Timing Attack, 已通過中風險
		return BcryptUtil.encode(str);
	}

	/**
	 * 驗證使用者密碼是否正確
	 * @param str
	 * @param encode
	 * @return true / false
	 */
	public static boolean bCryptPasswordCheck(String str, String encode){
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		return passwordEncoder.matches(str,encode);
	}

	public void setOauthClientDetailsDao(OauthClientDetailsDao oauthClientDetailsDao) {
		OAuthUtil.oauthClientDetailsDao = oauthClientDetailsDao;
	}
	
	public void setTsmpClientDao(TsmpClientDao tsmpClientDao) {
		OAuthUtil.tsmpClientDao = tsmpClientDao;
	}
	
	public static void setDgrAuditLogService(DgrAuditLogService dgrAuditLogService) {
		OAuthUtil.dgrAuditLogService = dgrAuditLogService;
	}

	/**
	 * 取得展期URL中需要的par1值
	 * 
	 * 1. OPEN_APIKEY_ID + OPEN_APIKEY + SECRET_KEY 
	 * 2. 使用 Bcrypt 加密
	 * 3. 使用 Base64URL Encode(無後綴)
	 */
	public static String getPar1(Long openApiKeyId, String openApiKey, String secretKey) {
		StringBuilder sb = new  StringBuilder();
		sb.append(openApiKeyId);
		sb.append(openApiKey);
		sb.append(secretKey);
		
		String bcryptEncode = OAuthUtil.bCryptEncode(sb.toString());//Bcrypt 加密
		String par1 = ServiceUtil.base64EncodeWithoutPadding(bcryptEncode.getBytes());//Base64URL Encode(無後綴)
		return par1;
	}
	
	/**
	 * 更新Scope欄位
	 * 
	 */
	public static OauthClientDetails updateScope(String clientId, String scope, InnerInvokeParam iip) {
		OauthClientDetails ocd = null;
		if (oauthClientDetailsDao != null) {
			Optional<OauthClientDetails> optOcd = oauthClientDetailsDao.findById(clientId);
			if (optOcd.isPresent()) {
				ocd = optOcd.get();
				String oldRowStr = null;
				if(dgrAuditLogService != null && iip != null) {
					oldRowStr = dgrAuditLogService.writeValueAsString(iip, ocd); //舊資料統一轉成 String
				}
				ocd.setScope(ServiceUtil.nvl(scope));

				ocd = oauthClientDetailsDao.save(ocd);
				
				if(dgrAuditLogService != null && iip != null) {
					//寫入 Audit Log D
					String lineNumber = StackTraceUtil.getLineNumber();
					dgrAuditLogService.createAuditLogD(iip, lineNumber, 
							OauthClientDetails.class.getSimpleName(), TableAct.U.value(), oldRowStr, ocd);
				}

			}
		}
		return ocd;
	}
}
