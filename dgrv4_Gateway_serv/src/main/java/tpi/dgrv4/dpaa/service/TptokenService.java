package tpi.dgrv4.dpaa.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.LdapReq;
import tpi.dgrv4.dpaa.vo.LdapResp;
import tpi.dgrv4.entity.component.cipher.TsmpTAEASKHelper;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.entity.jpql.TsmpSsoUserSecret;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.entity.repository.TsmpSsoUserSecretDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.OAuthTokenService;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp2;
import tpi.dgrv4.gateway.vo.OAuthTokenResp;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class TptokenService {
	
	//Audit Log使用
	String eventNo = AuditLogEvent.LOGIN.value(); 
	String tokenApiUrl = "/tptoken/oauth/token";
	String auditClientId = "";
	
	private TsmpUserDao tsmpUserDao;
	private TsmpSettingDao tsmpSettingDao;
	private ServiceConfig serviceConfig;
	private TsmpSettingService tsmpSettingService;
	private TsmpTAEASKHelper tsmpTAEASKHelper;
	private DgrAuditLogService dgrAuditLogService;
	private TsmpSsoUserSecretDao tsmpSsoUserSecretDao;
	private SsotokenService ssotokenService;
	private LdapService ldapService;	
	private OAuthTokenService oAuthTokenService;
	
	@Autowired
	public TptokenService(TsmpUserDao tsmpUserDao, TsmpSettingDao tsmpSettingDao, ServiceConfig serviceConfig,
			TsmpSettingService tsmpSettingService, TsmpTAEASKHelper tsmpTAEASKHelper,
			DgrAuditLogService dgrAuditLogService, TsmpSsoUserSecretDao tsmpSsoUserSecretDao,
			SsotokenService ssotokenService, LdapService ldapService, OAuthTokenService oAuthTokenService) {
		super();
		this.tsmpUserDao = tsmpUserDao;
		this.tsmpSettingDao = tsmpSettingDao;
		this.serviceConfig = serviceConfig;
		this.tsmpSettingService = tsmpSettingService;
		this.tsmpTAEASKHelper = tsmpTAEASKHelper;
		this.dgrAuditLogService = dgrAuditLogService;
		this.tsmpSsoUserSecretDao = tsmpSsoUserSecretDao;
		this.ssotokenService = ssotokenService;
		this.ldapService = ldapService;
		this.oAuthTokenService = oAuthTokenService;
	}

	public void getTptoken(HttpServletRequest httpReq, HttpServletResponse httpRes, HttpHeaders httpHeaders,
			ReqHeader reqHeader) throws Exception {
		String scheme = httpReq.getScheme();
		
	    String serverName = "127.0.0.1";
	    String serverPort = httpReq.getServerPort() == 80 ? "" : ":" + httpReq.getServerPort();
	    String contextPath = httpReq.getContextPath();
	    String localBaseUrl = scheme + "://" + serverName + serverPort + contextPath;
		
		Map<String, String> parameters = new HashMap<>();
		httpReq.getParameterMap().forEach((k, vs) -> {
			if (vs.length != 0) {
				parameters.put(k, vs[0]);
			}
		});
 
		String txnUid = getDgrAuditLogService().getTxnUid();
		
		TPILogger.tl.trace(String.format("http header size = %d", httpHeaders.entrySet().size()));
		for (Entry<String, List<String>> entry : httpHeaders.entrySet()) {
			String key = entry.getKey();
			List<String> val = entry.getValue();
			TPILogger.tl.trace(String.format("http key [%s] = %s", key, val.toString()));
		}
		String userIp = StringUtils.hasLength(httpHeaders.getFirst("x-forwarded-for")) 
				? httpHeaders.getFirst("x-forwarded-for") : httpReq.getRemoteAddr();
		String userHostname = httpReq.getRemoteHost();
		
		getTptoken(parameters, scheme, httpRes, userIp, userHostname, txnUid, 
				reqHeader.getLocale(), localBaseUrl);
	}
	
	protected void getTptoken(Map<String, String> parameters, String scheme, HttpServletResponse httpRes, 
			String userIp, String userHostname, String txnUid, 
			String locale, String localBaseUrl) throws Exception {
		String flagName = null;
		boolean flag_ldap = false; //判斷 LDAP_CHECK_ACCT_ENABLE 是否啟用
		try {
			flagName = TsmpSettingDao.Key.LDAP_CHECK_ACCT_ENABLE;
			flag_ldap = getTsmpSettingService().getVal_LDAP_CHECK_ACCT_ENABLE();
		} catch (TsmpDpAaException e) {
			TPILogger.tl.debug("No data found:" + flagName);
//			throw TsmpDpAaRtnCode._1298.throwing();
		}
		
		//1. refresh_token 固定執行 tptoken
		String grantType = parameters.get("grant_type");
		if("refresh_token".equalsIgnoreCase(grantType)) {
			//refresh_token 用 parameters.get("username") 會得到 null
			TPILogger.tl.info("--Execute doTptoken");
			doTptoken(parameters, scheme, httpRes, userIp, userHostname, txnUid);
			return;
		}
 
		//2. manager 固定執行 tptoken
		//判斷 user 是否為 manager
		boolean isManager = isManager(parameters);
		if(isManager) {
			TPILogger.tl.info("--Execute doTptoken");
			doTptoken(parameters, scheme, httpRes, userIp, userHostname, txnUid);
			return;
		}
		
		//3. 若有啟用 LDAP,執行 ssotoken
		if(flag_ldap) {
			if(isDoLdap()) {
				TPILogger.tl.info("--Execute doLdap");
				doLdap(parameters, localBaseUrl);
			}
			TPILogger.tl.info("--Execute doSsotoken");
			doSsotoken(parameters, scheme, httpRes, locale, userIp, userHostname, txnUid);
			return;
		}
		
		//4. 其他,執行 tptoken
		TPILogger.tl.info("--Execute doTptoken");
		doTptoken(parameters, scheme, httpRes, userIp, userHostname, txnUid);
	}
	
	private void doLdap(Map<String, String> parameters, String localBaseUrl) {
		String userName = parameters.get("username");
		String pwd = parameters.get("password");
		String codeVerifier	 = parameters.get("codeVerifier");
		String codeChallenge = ServiceUtil.getSHA256ToBase64UrlEncodeWithoutPadding(codeVerifier);
		pwd = new String(ServiceUtil.base64Decode(pwd));//須做 Base64Decode ,才是 LDAP 接受的值
		
		TsmpAuthorization auth = new TsmpAuthorization();
		auth.setUserName(userName);	
		
		LdapReq ldapReq = new LdapReq();
		ldapReq.setUserName(userName);
		ldapReq.setPwd(pwd);
		ldapReq.setCodeChallenge(codeChallenge);
		LdapResp ldapResp = getLdapService().checkAccountByLdap(auth, ldapReq, localBaseUrl);//呼叫 LDAP
	}
	
	/**
	 * 取得 ssotoken
	 */
	private void doSsotoken(Map<String, String> parameters, String scheme, HttpServletResponse res, 
			String locale, String userIp, String userHostname, String txnUid) throws Exception {
		String userMail = parameters.get("userMail");
		String codeVerifier = parameters.get("codeVerifier");//UUID
		String grantType = parameters.get("grant_type");
		String userName = parameters.get("username");
		
		if(!StringUtils.hasText(userName)) {
			return;
		}
		
		userName = userName.replace(" ", "_");//將 userName 的空白取代為底線 "_"
		String userNameEncode = getEncoedeUserName(userName);//取得後面有加特殊字0x80,再經 Base64Encode 的  User Name
		
		Map<String, String> ssoParameters = new HashMap<>();
		ssoParameters.put("grant_type", grantType);
		ssoParameters.put("username", userNameEncode);
		ssoParameters.put("codeVerifier", codeVerifier);
		ssoParameters.put("userMail", userMail);
		
		this.getSsotokenService().getSsotoken(ssoParameters, scheme, res, locale, 
				userIp, userHostname, txnUid);
	}	
	
	/**
	 * 取得 tptoken
	 */
	private void doTptoken(Map<String, String> parameters, String scheme, HttpServletResponse httpResp, 
			String userIp, String userHostname, String txnUid) throws Exception {
		
		//check param
		String grantType = parameters.get("grant_type");
		String userName = parameters.get("username");
		
		// 檢查是否為SSO User
		if(StringUtils.hasLength(userName)) {
			checkSsoUser(userName);
		}
		
		//若 parameters 的 grant_type 不是 "password" 且 不是 "refreah_token"的, 一律轉成 "password"
		if (!"password".equals(grantType) && !"refresh_token".equals(grantType)) {
			parameters.put("grant_type", "password");
		}
		// 檢查 User 狀態
		checkUserInfo(userName, userIp, userHostname, txnUid);
		
		// 取得 AES Key						
		String aesKey = getAesKey();
		TPILogger.tl.trace("--TAEASK:" + ServiceUtil.dataMask(aesKey, 2, 2));
		
		// 取得 Client ID/PW
		String clientId = getClientId(aesKey);
		if (clientId == null) {
			TPILogger.tl.error("\n\n !!!!! clientId: null !!!!!\n\n"); // maybe decocde error
			System.err.println("\n\n !!!!! clientId: null !!!!!\n\n");
			Thread.sleep(3000);
		}
		TPILogger.tl.trace("--clientId:" + ServiceUtil.dataMask(clientId, 2, 2));

		String clientPw = getClientPw(aesKey);
		if (clientPw == null) {
			TPILogger.tl.error("\n\n !!!!! clientPw: null !!!!!\n\n"); // maybe decocde error
			System.err.println("\n\n !!!!! clientId: null !!!!!\n\n");
			Thread.sleep(3000);
		}
		TPILogger.tl.trace("--clientPw:" + ServiceUtil.dataMask(clientPw, 2, 2));
		
		
		// 組成 Http Header Authorization 
		String authorization = getAuthorization(clientId, clientPw);
		
		/*
		 * 不使用URL, 直接從 OAuthTokenService 取 Token		
		 */
		// http header
		Map<String,String> httpHeader = new HashMap<>();
		httpHeader.put("Authorization", authorization);
		httpHeader.put("__Authorization", authorization);
		
		// form data
		Map<String,String> formData = parameters;
		
		if(isGetToken()) {
			ObjectMapper objectMapper = new ObjectMapper();
			ResponseEntity<?> respObj = getOAuthTokenService().getToken(httpResp, formData, authorization,
					"/oauth/token");
			Object bodyObj = respObj.getBody();
			String json = null;
			if(bodyObj instanceof OAuthTokenResp) {
				json = objectMapper.writeValueAsString((OAuthTokenResp) bodyObj);
				
			}else if(bodyObj instanceof OAuthTokenErrorResp) {
				json = objectMapper.writeValueAsString((OAuthTokenErrorResp) bodyObj);
				
			}else if(bodyObj instanceof OAuthTokenErrorResp2) {
				json = objectMapper.writeValueAsString((OAuthTokenErrorResp2) bodyObj);
			}else {
				json = "";
			}
						
			httpResp.setStatus(respObj.getStatusCodeValue());
			byte httpArray[] = json.toString().getBytes();
			String httpRespStr = new String(httpArray , StandardCharsets.UTF_8);
			TPILogger.tl.trace("\n oauth token result body:\n" + httpRespStr + "\n"); 
			
			String tokenJti = getJti(httpRespStr);
			
			ByteArrayInputStream bi = new ByteArrayInputStream(httpArray);
			IOUtils.copy(bi, httpResp.getOutputStream());
			
			//更新 TSMP_USER
			updateUser(httpResp.getStatus(), userName, userIp, userHostname, txnUid, tokenJti);
		}
	}
	
	/**
	 * 判斷輸入的 user 是否為 manager,
	 * 1.查詢 tsmp_user.user_id = '1000000000',判斷 tsmp_user.user_name 和輸入的 user name 是否相同,
	 * 2.若 tsmp_user.user_id = '1000000000'不存在,則判斷輸入 的 user name 是否為 "manager", 
	 * 3.若輸入的 user name 為上述其中一個,則固定執行 doTptoken
	 */
	private boolean isManager(Map<String, String> parameters) {
		boolean isManager = false;
		 
		String userName = parameters.get("username");//輸入的 user name
		Optional<TsmpUser> user_opt = getTsmpUserDao().findById("1000000000");
		if(user_opt.isPresent()) {
			TsmpUser tsmpUser = user_opt.get();
			String userName_manager = tsmpUser.getUserName();
			if(userName_manager.equals(userName)) {
				isManager = true;
			}
		}else {
			if("manager".equalsIgnoreCase(userName)) {
				isManager = true;
			}
		}
 
		return isManager;
	}
	
	/**
	 * 若有 access_token 則回傳 jti , 否則為 null
	 * @param httpRespStr
	 * @return
	 * @throws IOException
	 */
	private String getJti(String httpRespStr) throws IOException {
		int index = httpRespStr.indexOf("access_token");
		if (index == -1) {
			return null; // 沒有取到 access_token
		}
		ObjectMapper om = new ObjectMapper();
		JsonNode json = om.readTree(httpRespStr);
		String jti = json.get("jti").asText();
		return jti;
	}
	
	/**
	 * 更新 TSMP_USER					
	 * 用 username 搜尋資料表 TSMP_USER, 若查無資料,則 return					
	 * 1.若回傳的 Response Http code = 200，重設密碼錯誤次數為0					
	 *  a.更新 pwd_fail_times 為 0,					
	 *  b.更新 update_time, logon_date 為 現在時間,					
	 *  c.更新 update_user 為傳入的 username					
	 * 2.若回傳的 Response Http code = 400 或 401, 更新密碼錯誤次數					
	 *  a.將 TSMP_USER.pwd_fail_times + 1, 更新至 pwd_fail_times					
	 *  b. 若上述 pwd_fail_times >= {failThreshold}, 則更新 user_status 為 "3" (鎖定)					
	 *  c.更新 update_time 為現在時間					
	 */
	private void updateUser(int statusCode, String userName, String userIp, String userHostname, String txnUid, 
			String tokenJti) {
		if(!StringUtils.hasLength(userName)) {
			return;
		}
		InnerInvokeParam iip = getDgrAuditLogService().getInnerInvokeParam(tokenApiUrl, "SYS", "SYS", userIp, 
				userHostname, txnUid+"_uUser", null, null);
		
		String oldRowStr = "";
		String token_loginState = "";
		
		TsmpUser user = getTsmpUserDao().findFirstByUserName(userName);
		if (user == null) {
			token_loginState = "FAILED";
			
		}else {
			oldRowStr = getDgrAuditLogService().writeValueAsString(iip, user); //舊資料統一轉成 String
			
			if (statusCode == 200) {
				token_loginState = "SUCCESS";
				user.setPwdFailTimes(0);
				user.setLogonDate(new Date());
				user.setUpdateTime(new Date());
				user.setUpdateUser(userName);
				
				user = getTsmpUserDao().saveAndFlush(user);
				
			} else if (statusCode == 400 || statusCode == 401) {
				token_loginState = "FAILED";
				int failTimes = user.getPwdFailTimes();
				failTimes++;
				user.setPwdFailTimes(failTimes);
				user.setUpdateTime(new Date());
				
				int token_failThreshold = getFailThreshold();//允許User密碼錯誤次數上限
				if (failTimes >= token_failThreshold) {
					user.setUserStatus("3");
				}
				
				user = getTsmpUserDao().saveAndFlush(user);
			}
		}
		
		//寫入 Audit Log M,登入成功或失敗
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogMForLogin(lineNumber, eventNo, tokenApiUrl, 
				userName, auditClientId, userIp, userHostname, token_loginState, statusCode+"", txnUid, tokenJti, null);
		
		if (user != null) {
			//寫入 Audit Log M
			lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_USER.value());
			
			//寫入 Audit Log D
			lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
					TsmpUser.class.getSimpleName(), TableAct.U.value(), oldRowStr, user);// U
		}
	}
	
	public HttpHeaders buildHeaderVo(HttpServletRequest req) {
		Enumeration<String> headerkeys = req.getHeaderNames();

		HttpHeaders token_headers = new HttpHeaders();

		while (headerkeys.hasMoreElements()) {
			String headerKey = headerkeys.nextElement();
			Enumeration<String> vals = req.getHeaders(headerKey);
			while (vals.hasMoreElements()) {
				String headerVal = vals.nextElement();
				token_headers.add(headerKey, headerVal);
			}
		}

		return token_headers;
	}
	
	private void checkUserInfo(String userName,String userIp, String userHostname, String txnUid) {
		if(!StringUtils.hasLength(userName)) {
			return;
		}
		
		TsmpUser user = getTsmpUserDao().findFirstByUserName(userName);
		if (user == null) {
			return;
		}

		int failTimes = user.getPwdFailTimes();
		String userStatus = user.getUserStatus();

		if ("2".equals(userStatus)) {
			//若 TSMP_USER.user_status (使用者狀態) = "2", 則 throw 1473。 (使用者已停權)
			
			//寫入 Audit Log M,登入成功或失敗
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogMForLogin(lineNumber, eventNo, tokenApiUrl, 
					userName, auditClientId, userIp, userHostname, "FAILED", "1473", txnUid, null, null);
			
			throw TsmpDpAaRtnCode._1473.throwing();
		}
		if ("3".equals(userStatus)) {
			//若 TSMP_USER.user_status (使用者狀態) = "3", 則 throw 1472。 (使用者已鎖定)
			
			//寫入 Audit Log M,登入成功或失敗
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogMForLogin(lineNumber, eventNo, tokenApiUrl, 
					userName, auditClientId, userIp, userHostname, "FAILED", "1472", txnUid, null, null);
			
			throw TsmpDpAaRtnCode._1472.throwing();
		}
		int failThreshold = getFailThreshold();//允許User密碼錯誤次數上限
		if (failTimes >= failThreshold) {
			//若 TSMP_USER.pwd_fail_times (密碼錯誤次數) >= {failThreshold}, 則 throw 1471。 (密碼錯誤超過上限)	
			
			//寫入 Audit Log M,登入成功或失敗
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogMForLogin(lineNumber, eventNo, tokenApiUrl, 
					userName, auditClientId, userIp, userHostname, "FAILED", "1471", txnUid, null, null);
			
			throw TsmpDpAaRtnCode._1471.throwing();
		}
	}
	
	/**
	 * 取得平台啟動時, 由 shell 輸入的 AES Key	
	 * 註: 需要交付組在啟動v3的shell或bat中設定"TAEASK"的值，這邊才會抓得到
	 */							
	public String getAesKey() {
		String key = System.getenv("TAEASK");
		return key;
	}
	
	/**
	 * 取得 Client ID
	 * 查詢資料表 TSMP_SETTING.id = "TSMP_AC_CLIENT_ID" 的 value, 								
	 * 1.若 AES Key 有值,								
	 * 則 TSMP_SETTING.value 為 AES 加密過的 clientId,取得解密的 clientId						
	 * 2.否則若 AES Key 沒有值, 則 clientId 為 TSMP_SETTING.value 的值,表示此資料未加密								
	 */
	public String getClientId(String aesKey) {
		String acClientId = getTsmpSettingService().getVal_TSMP_AC_CLIENT_ID();
		String clientId = "";
		if (!StringUtils.hasLength(aesKey)) {
			// 沒有 Key, 不用解密
			clientId = acClientId;  
		}else {
			// 有 Key, 解密
			try {
				clientId = getTsmpTAEASKHelper().decrypt(acClientId);
			} catch (Exception e) {
				TPILogger.tl.error("AES key exists! 'acClientId' must be ciphertext, value: " + acClientId);
				throw e;
			}
		}
		return clientId;
	}
	
	/**
	 * 取得 Client PW								
	 * 查詢資料表 TSMP_SETTING.id = "TSMP_AC_CLIENT_PW" 的 value,								
	 * 1.若 AES Key 有值								
	 * 則 TSMP_SETTING.value 為 AES 加密過的 clientPw, 取得解密的 clientPw							
	 * 2.否則若 AES Key 沒有值, 則 clientPw 為 TSMP_SETTING.value 的值,表示此資料未加密								
	 */
	public String getClientPw(String aesKey) {
		String acClientPw = getTsmpSettingService().getVal_TSMP_AC_CLIENT_PW();
		String clientPw = "";
		if (!StringUtils.hasLength(aesKey)) {
			clientPw = acClientPw;
		}else {
			clientPw = getTsmpTAEASKHelper().decrypt(acClientPw);
		}
		return clientPw;
	}
	
	/**
	 * 組成 Http Header Authorization
	 */
	public String getAuthorization(String clientId, String clientPw) {
		String info =  clientId + ":" + clientPw;
		info = Base64Util.base64EncodeWithoutPadding(info.getBytes());//Base64 Encode(無後綴)
		String authorization = "Basic " + info;
		return authorization;
	}

	/**
	 * 取得允許User密碼錯誤次數上限
	 * @param aesKey
	 * @return
	 */
	private int getFailThreshold() {
		int failThreshold = getTsmpSettingService().getVal_TSMP_FAIL_THRESHOLD();
		return failThreshold;
	}
	
	/**
	 * 判斷 user name,是否存在於 TSMP_SSO_USER_SECRET 表中,
	 * 若有,表示不應該由此登入,要由合法的SSO入口登入
	 * 
	 * @param userName
	 * @return
	 */
	private void checkSsoUser(String userName) {
		TsmpSsoUserSecret ssoUserSecret = getTsmpSsoUserSecretDao().findFirstByUserName(userName);
		if(ssoUserSecret != null) {
			throw TsmpDpAaRtnCode._1517.throwing();
		}
	}
	
	//for UnitTest
	protected boolean isGetToken() {
		return true;
	}
	
	//for UnitTest
	protected boolean isDoLdap() {
		return true;
	}
	
	/**
	 * 取得後面有加特殊字0x80,再經 Base64Encode 的 User Name
	 */
	private String getEncoedeUserName(String userNameOrig) {
		String userNameEncode = ServiceUtil.getAdd0x80ToBase64Encoede(userNameOrig);
		return userNameEncode;
	}
	
	protected TsmpUserDao getTsmpUserDao() {
		return this.tsmpUserDao;
	}
	
	protected TsmpSettingDao getTsmpSettingDao() {
		return this.tsmpSettingDao;
	}
	
	protected TsmpSettingService getTsmpSettingService() {
		return this.tsmpSettingService;
	}
	
	protected TsmpTAEASKHelper getTsmpTAEASKHelper() {
		return this.tsmpTAEASKHelper;
	}
	
	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}
	
	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}
	
	protected TsmpSsoUserSecretDao getTsmpSsoUserSecretDao() {
		return tsmpSsoUserSecretDao;
	}
	
	protected SsotokenService getSsotokenService() {
		return ssotokenService;
	}
	
	protected LdapService getLdapService() {
		return this.ldapService;
	}
	
	protected OAuthTokenService getOAuthTokenService() {
		return this.oAuthTokenService;
	}
}
