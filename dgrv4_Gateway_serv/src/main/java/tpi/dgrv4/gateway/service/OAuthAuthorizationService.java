package tpi.dgrv4.gateway.service;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpAuthCodeStatus;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.TsmpAuthCode;
import tpi.dgrv4.entity.repository.TsmpAuthCodeDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp2;

/**
 * 2023/04/20 目前已不使用, 改用 GTW IdP 流程, 故全部註解
 * @author Mini
 */

@Service
public class OAuthAuthorizationService {

//	@Autowired
//	private OAuthTokenService oAuthTokenService;
//	 
//	@Autowired
//	private TsmpAuthCodeDao tsmpAuthCodeDao;
//
//	@Autowired
//	private ServiceConfig serviceConfig;
//	
//	@Autowired
//	private TokenHelper tokenHelper;
//
//	@Autowired
//	private TsmpSettingService tsmpSettingService;
//	
//	private Long expTime;
//	
//	public ResponseEntity<?> authorization(HttpHeaders httpHeaders, HttpServletRequest httpReq, 
//			HttpServletResponse httpRes) {
//		
//		String apiUrl = httpReq.getRequestURI();//例如 /oauth/authorization
//		try {
//			String reqUrl = httpReq.getRequestURL().toString();//例如 https://10.20.30.88:18442/oauth/authorization
//			TPILogger.tl.info("\n--【" + reqUrl.toString() + "】--");
//			
//			Map<String, String> parameters = new HashMap<>();
//			httpReq.getParameterMap().forEach((k, vs) -> {
//				if (vs.length != 0) {
//					parameters.put(k, vs[0]);
//				}
//			});
//			
//			ResponseEntity<?> checkResp = checkData(parameters, apiUrl);
//			if(checkResp != null) {//client資料驗證有錯誤
//				return checkResp;
//			}
//			
//			String clientId = parameters.get("client_id");
//			String state = parameters.get("state");
//			String scope = parameters.get("scope");
//			String redirectUri = parameters.get("redirect_uri");
//			
//			TsmpAuthCode tsmpAuthCode = getTsmpAuthCodeDao().findFirstByAuthCode(state);
//			if(tsmpAuthCode != null) {
//				//Table [TSMP_AUTH_CODE] 已有重複的 auth_code, 無法再寫入
//				TPILogger.tl.debug("Table [TSMP_AUTH_CODE] has duplicate 'auth_code' value "
//						+ "and cannot be written anymore, auth_code(state):" + state);
//				String errMsg = "Bad state";
//				TPILogger.tl.debug(errMsg);
//				return new ResponseEntity<OAuthTokenErrorResp2>(getTokenHelper().getOAuthTokenErrorResp2("invalid_request", 
//						errMsg), HttpStatus.BAD_REQUEST);//400
//			}
//			saveAndRefreshAuthCode(state, clientId);
//			
//			//302 redirect 到 Bank user 登入畫面
//			String idpUserLoginUrl = httpHeaders.getFirst("idP_User_Login_Url");
//			
//			/*暫不使用
//			if(!StringUtils.hasLength(idpUserLoginUrl)) {
//				int index1 = reqUrl.lastIndexOf(":");
//				int index2 = reqUrl.indexOf("/", index1); 
//				String schemeAndIpAndPort = reqUrl.substring(0, index2);//例如 https://10.20.30.88:18442
//				
//				idpUserLoginUrl = schemeAndIpAndPort + "/oauth/mock_BANK_UserLogin";//模擬 Bank User 登入畫面
//				idpUserLoginUrl = idpUserLoginUrl
//						+ "?scope=" + scope
//						+ "&redirect_uri=" + redirectUri
//						+ "&state=" + state;
//			}
//			*/
// 
//			TPILogger.tl.info("Redirect URL(Bank user login URL):\n" + idpUserLoginUrl);
//			
//			HttpHeaders respHeaders = new HttpHeaders();
//			respHeaders.setLocation(URI.create(idpUserLoginUrl));//Response Header 的 Location 放入要重新導向的網址
//			return new ResponseEntity<>(respHeaders, HttpStatus.MOVED_TEMPORARILY);//302
//			
//		} catch (Exception e) {
//			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
//			String errMsg = TokenHelper.Internal_Server_Error;
//			TPILogger.tl.error(errMsg);
//			return getTokenHelper().getInternalServerErrorResp(apiUrl, errMsg);//500
//		}
//	}
// 
//	private ResponseEntity<?> checkData(Map<String, String> parameters, String apiUrl) {
//		 
//		String responseType = parameters.get("response_type");
//		String scope = parameters.get("scope");
//		String clientId = parameters.get("client_id");
//		String redirectUri = parameters.get("redirect_uri");
//		String state = parameters.get("state");
//		
//		if(!StringUtils.hasLength(responseType)) {
//			String word = "response_type";
//			TPILogger.tl.debug("Query string has no " + word);//Query string 沒有 response_type
//			return getOAuthTokenService().getResponseEntityError(word);
//		}
//		
//		if(!StringUtils.hasLength(scope)) {
//			String word = "scope";
//			TPILogger.tl.debug("Query string has no " + word);//Query string 沒有 scope
//			return getOAuthTokenService().getResponseEntityError(word);
//		}
//		
//		if(!StringUtils.hasLength(clientId)) {
//			String word = "client_id";
//			TPILogger.tl.debug("Query string has no " + word);//Query string 沒有 client_id
//			return getOAuthTokenService().getResponseEntityError(word);
//		}
//		
//		if(!StringUtils.hasLength(redirectUri)) {
//			String word = "redirect_uri";
//			TPILogger.tl.debug("Query string has no " + word);//Query string 沒有 redirect_uri
//			return getOAuthTokenService().getResponseEntityError(word);
//		}
//		
//		if(!StringUtils.hasLength(state)) {
//			String word = "state";
//			TPILogger.tl.debug("Query string has no " + word);//Query string 沒有 state
//			return getOAuthTokenService().getResponseEntityError(word);
//		}
//		
//		ResponseEntity<?> checkClientResp = getTokenHelper().checkClientStatus(clientId, apiUrl);
//		if(checkClientResp != null) {//client資料驗證有錯誤
//			return checkClientResp;
//		}
//		
//		ResponseEntity<?> checkRedirectUriResp = getTokenHelper().checkRedirectUri(clientId, redirectUri, apiUrl);
//		if(checkRedirectUriResp != null) {//redirectUri驗證有錯誤
//			return checkRedirectUriResp;
//		}		
//		
//		return null;
//	}
//	
//	private TsmpAuthCode saveAndRefreshAuthCode(String state, String clientId) {
//		Date expiredTime = getCodeExpiredTime();
//		Long expireDateTime = expiredTime.getTime();
//		
//		TsmpAuthCode tsmpAuthCode = new TsmpAuthCode();
//		tsmpAuthCode.setAuthCode(state);
//		tsmpAuthCode.setExpireDateTime(expireDateTime);
//		tsmpAuthCode.setStatus(TsmpAuthCodeStatus.AVAILABLE.value());
//		tsmpAuthCode.setAuthType("dgRv4");
//		tsmpAuthCode.setClientName(clientId);
//		tsmpAuthCode.setCreateDateTime(DateTimeUtil.now());
//		tsmpAuthCode.setCreateUser(clientId);
//		tsmpAuthCode = this.getTsmpAuthCodeDao().saveAndFlush(tsmpAuthCode);
//		return tsmpAuthCode;
//	}
//
//	// 以 現在時間 + 授權碼有效期間，計算出有效日期
//	public Date getCodeExpiredTime() {
//		LocalDateTime ldt = LocalDateTime.now().plus(getExpTime(), ChronoUnit.MILLIS);
//		ZonedDateTime zdt = ldt.atZone(ZoneId.systemDefault());
//		Date expiredTime = Date.from( zdt.toInstant() );
//		return expiredTime;
//	}
//	
//	protected Long getExpTime() {
//		if (this.expTime == null) {
//			String val = getTsmpSettingService().getVal_AUTH_CODE_EXP_TIME();
//			if (!StringUtils.hasLength(val)) {
//				// 預設 10 分鐘
//				val = "600000";
//			}
//			this.expTime = Long.valueOf(val);
//		}
//		return this.expTime;
//	}
//	
//	protected OAuthTokenService getOAuthTokenService() {
//		return oAuthTokenService;
//	}
//	
//	protected TsmpAuthCodeDao getTsmpAuthCodeDao() {
//		return tsmpAuthCodeDao;
//	}
//
//	protected ServiceConfig getServiceConfig() {
//		return serviceConfig;
//	}
//	
//	protected TokenHelper getTokenHelper() {
//		return tokenHelper;
//	}
//	
//	protected TsmpSettingService getTsmpSettingService() {
//		return tsmpSettingService;
//	}
}
