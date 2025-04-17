package tpi.dgrv4.dpaa.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.LocaleType;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpApptJobStatus;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.OAuthUtil;
import tpi.dgrv4.dpaa.util.RandomUtils;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0001Req;
import tpi.dgrv4.dpaa.vo.AA0011Req;
import tpi.dgrv4.dpaa.vo.AA0011Resp;
import tpi.dgrv4.dpaa.vo.AA1001Req;
import tpi.dgrv4.dpaa.vo.AA1001Resp;
import tpi.dgrv4.dpaa.vo.DPB0123Resp;
import tpi.dgrv4.dpaa.vo.TsmpInvokeCommLoopStatus;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.component.cipher.TsmpTAEASKHelper;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.entity.TsmpOrganization;
import tpi.dgrv4.entity.entity.TsmpRole;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.entity.Users;
import tpi.dgrv4.entity.entity.jpql.SsoAuthResult;
import tpi.dgrv4.entity.entity.jpql.TsmpSsoUserSecret;
import tpi.dgrv4.entity.repository.SsoAuthResultDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.repository.TsmpRoleDao;
import tpi.dgrv4.entity.repository.TsmpRtnCodeDao;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.entity.repository.TsmpSsoUserSecretDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.entity.repository.UsersDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.OAuthTokenService;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.OAuthTokenResp;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class SsotokenService{

	private TPILogger logger = TPILogger.tl;
	
	private String apiType = "doubleCheckLogin";
	private String mockId = "DPB0123";
	
	//Audit Log使用
	String eventNo = AuditLogEvent.LOGIN.value(); 
	String auditClientId = "";
	String tokenApiUrl = "/ssotoken/oauth/token";

	private TsmpUserDao tsmpUserDao;	
	private TsmpSettingService tsmpSettingService;	
	private TsmpSettingDao tsmpSettingDao;	
	private TsmpTAEASKHelper tsmpTAEASKHelper;	
	private ServiceConfig serviceConfig;	
	private SsoAuthResultDao ssoAuthResultDao;	
	private TsmpOrganizationDao tsmpOrganizationDao;	
	private TsmpRoleDao tsmpRoleDao;	
	private UsersDao usersDao;	
	private TsmpSsoUserSecretDao tsmpSsoUserSecretDao;	
	private AA1001Service aa1001Service;	
	private AA0011Service aa0011Service;	
	private DPB0123Service dpb0123Service;	
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;	
	private TsmpRtnCodeDao tsmpRtnCodeDao;	
	private AA0001Service aa0001Service;	
	private ApplicationContext ctx;	
	private OAuthTokenService oAuthTokenService;	
	private DgrAuditLogService dgrAuditLogService;	
	private JobHelper jobHelper;

	@Autowired
	public SsotokenService(TsmpUserDao tsmpUserDao, TsmpSettingService tsmpSettingService,
			TsmpSettingDao tsmpSettingDao, TsmpTAEASKHelper tsmpTAEASKHelper, ServiceConfig serviceConfig,
			SsoAuthResultDao ssoAuthResultDao, TsmpOrganizationDao tsmpOrganizationDao, TsmpRoleDao tsmpRoleDao,
			UsersDao usersDao, TsmpSsoUserSecretDao tsmpSsoUserSecretDao, AA1001Service aa1001Service,
			AA0011Service aa0011Service, DPB0123Service dpb0123Service, TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy,
			TsmpRtnCodeDao tsmpRtnCodeDao, AA0001Service aa0001Service, ApplicationContext ctx,
			OAuthTokenService oAuthTokenService, DgrAuditLogService dgrAuditLogService, JobHelper jobHelper) {
		super();
		this.tsmpUserDao = tsmpUserDao;
		this.tsmpSettingService = tsmpSettingService;
		this.tsmpSettingDao = tsmpSettingDao;
		this.tsmpTAEASKHelper = tsmpTAEASKHelper;
		this.serviceConfig = serviceConfig;
		this.ssoAuthResultDao = ssoAuthResultDao;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.tsmpRoleDao = tsmpRoleDao;
		this.usersDao = usersDao;
		this.tsmpSsoUserSecretDao = tsmpSsoUserSecretDao;
		this.aa1001Service = aa1001Service;
		this.aa0011Service = aa0011Service;
		this.dpb0123Service = dpb0123Service;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.tsmpRtnCodeDao = tsmpRtnCodeDao;
		this.aa0001Service = aa0001Service;
		this.ctx = ctx;
		this.oAuthTokenService = oAuthTokenService;
		this.dgrAuditLogService = dgrAuditLogService;
		this.jobHelper = jobHelper;
	}
	
	private class InvokeBean {
		private String rawData;
		private String userName;
		private String codeChallenge;
		private String locale;
		private String mockId;
		private Integer nMinutes;
		public String getRawData() {
			return rawData;
		}
		public void setRawData(String rawData) {
			this.rawData = rawData;
		}
		public String getUserName() {
			return userName;
		}
		public void setUserName(String userName) {
			this.userName = userName;
		}
		public String getLocale() {
			return locale;
		}
		public void setLocale(String locale) {
			this.locale = locale;
		}
		public String getCodeChallenge() {
			return codeChallenge;
		}
		public void setCodeChallenge(String codeChallenge) {
			this.codeChallenge = codeChallenge;
		}
		public String getMockId() {
			return mockId;
		}
		public void setMockId(String mockId) {
			this.mockId = mockId;
		}
		public Integer getnMinutes() {
			return nMinutes;
		}
		public void setnMinutes(Integer nMinutes) {
			this.nMinutes = nMinutes;
		}
	}
	
	//為了讓 Unit test 使用 override
	protected boolean isGetToken() {
		boolean isEnable = true;
		return isEnable;
	}
	
	protected void getSsotoken(Map<String, String> parameters, String scheme, HttpServletResponse res, 
			String locale, String userIp, String userHostname, String txnUid) throws Exception {
		
		String grantType = parameters.get("grant_type");
		String userName = null;
		
		if(!"refresh_token".equals(grantType)) {
			userName = doPassword(parameters, locale, userIp, userHostname, txnUid);
		}else {
			userName = null;
		}
		parameters.put("username", userName);//改放沒有特殊字的User Name 或 null
		
		if(isGetToken()) {
			getToken(parameters, scheme, res, userIp, userHostname, txnUid);
		}
	}
	
	public void getSsotoken(HttpServletRequest httpReq, HttpServletResponse res, ReqHeader reqHeader, 
			HttpHeaders headers) throws Exception {
		String scheme = httpReq.getScheme();
		Map<String, String> parameters = new HashMap<>();
		httpReq.getParameterMap().forEach((k, vs) -> {
			if (vs.length != 0) {
				parameters.put(k, vs[0]);
			}
		});
		
		String txnUid = getDgrAuditLogService().getTxnUid();
		
		logger.debug("http header size = " + headers.entrySet().size());
		for (Entry<String, List<String>> entry : headers.entrySet()) {
			String key = entry.getKey();
			List<String> val = entry.getValue();
			logger.debug(String.format("http key [%s] = %s", key, val.toString()));
		}
		String userIp = StringUtils.isEmpty(headers.getFirst("x-forwarded-for")) ? httpReq.getRemoteAddr() : headers.getFirst("x-forwarded-for");
		String userHostname = httpReq.getRemoteHost();
		getSsotoken(parameters, scheme, res, reqHeader.getLocale(), userIp, userHostname, txnUid);
	}
	
	/**
	 * call DPB0123
	 * 詢問對接企業確認 User 是否有登入
	 * 
	 * @param userName
	 * @param codeChallenge
	 * @param locale
	 * @param response
	 * @return
	 */
	protected String doService(String userName, String codeChallenge, String locale) {
		InvokeBean bean = new InvokeBean();
		try {
			bean = checkParams(userName, codeChallenge, locale);
			bean.setMockId(mockId);
			// 取得 n_minutes
			Integer nMinutes = getNMinutes(bean.getMockId());
			bean.setnMinutes(nMinutes);
			doubleCheckLogin(bean);
			
			return bean.getRawData();
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}
	
	protected String doPassword(Map<String, String> parameters, String locale, String userIp, 
			String userHostname, String txnUid) throws Exception {
		//check param
		String userName = parameters.get("username");
		String ssotoken_codeVerifier = parameters.get("codeVerifier");
		String userMail = parameters.get("userMail");
		if(!StringUtils.hasText(userName)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		if(!StringUtils.hasText(ssotoken_codeVerifier)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		// 判斷 userName 是否有特殊字,並取得原 userName
		userName = checkUserNameAndGetUserName(userName);
		
		// 判斷 user 若為 manager,不能以 SSO 方式登入
		if("manager".equals(userName)) {
			throw TsmpDpAaRtnCode._1518.throwing();
		}

		// 取得參數 flag 和 invoke URL
		boolean flag1_ssoPkce = false; //判斷 PKCE等級AuthCode驗證 是否啟用
		boolean flag2_ssoDoubleCheck = false; //判斷 Double check驗證 是否啟用
		boolean flag3_ssoAutoCreateUser = false; //判斷 自動建立User資料 是否啟用
		
		String flagName = null;
		try {
			flagName = TsmpSettingDao.Key.SSO_PKCE;
			flag1_ssoPkce = getTsmpSettingService().getVal_SSO_PKCE();
			
			flagName = TsmpSettingDao.Key.SSO_DOUBLE_CHECK;
			flag2_ssoDoubleCheck = getTsmpSettingService().getVal_SSO_DOUBLE_CHECK();
			
			flagName = TsmpSettingDao.Key.SSO_AUTO_CREATE_USER;
			flag3_ssoAutoCreateUser = getTsmpSettingService().getVal_SSO_AUTO_CREATE_USER();
		} catch (TsmpDpAaException e) {
			this.logger.debug("查無資料:{}" + flagName);
			throw TsmpDpAaRtnCode._1298.throwing();
		}
 
		//取得處理過的 UUID
		//Base64UrlEncodeWithoutPadding(SHA256(UUID))
		String uuidEn = ServiceUtil.getSHA256ToBase64UrlEncodeWithoutPadding(ssotoken_codeVerifier);
		
		if(flag1_ssoPkce) {
			Integer timeout = null;
			try {
				flagName = TsmpSettingDao.Key.SSO_TIMEOUT;
				timeout = getTsmpSettingService().getVal_SSO_TIMEOUT();
			} catch (TsmpDpAaException e) {
				this.logger.debug("查無資料:{}" + flagName);
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			// 比對資料庫是否有匹配的 User Name 和 處理過的 UUID
			LocalDateTime now = getNow();
			Date queryStartDate = getQueryStartDate(now, timeout);//取得 now - N分鐘
			checkPkce(userName, uuidEn, queryStartDate);
		}
		
		// 取得密碼,依 falg3 自動建立 User 相關資料
		String userBlock = getUserBlockAndCreateUserData(flag3_ssoAutoCreateUser, userName, userMail, userIp, userHostname, txnUid);
		
		parameters.put("password", userBlock);
		
		// 詢問對接企業確認 User 是否有登入
		if(flag2_ssoDoubleCheck) {
			String login = doService(userName, uuidEn, locale);//call DPB0123
			if(!"Y".equals(login)) {
				this.logger.debug("查無資料:{}" + flagName);
				throw TsmpDpAaRtnCode._1496.throwing();
			}
		}
		return userName;
	}
	
	private void doubleCheckLogin(InvokeBean bean) {
		// 呼叫API取得結果
		String ssotoken_rawData = bean.getRawData();
		DPB0123Resp resp = invokeDPB0123(bean.getUserName(), bean.getCodeChallenge(), bean.getLocale());
		Long ssotoken_apptJobId = resp.getApptJobId();
		// 直接調用
		if (ssotoken_apptJobId == null) {
			ssotoken_rawData = Optional.ofNullable(resp.getShowUI()) //
				.map((showUI) -> Optional.ofNullable(showUI.getCgRespBody()) //
					.map((cgRespBody) -> cgRespBody.getLogin()) //
					.orElse(new String())
				)
				.orElse(new String());
		// Mock調用
		} else {
			long loopStart = DateTimeUtil.now().getTime();
			Supplier<TsmpInvokeCommLoopStatus> getLoopStatus = () -> {
				DPB0123Resp lsResp = getDPB0123Service().queryLoopStatus(ssotoken_apptJobId, bean.getLocale());
				return lsResp.getCommLoopStatus();
			};
			Supplier<String> getData = () -> {
				DPB0123Resp resultResp = getDPB0123Service().queryResult(ssotoken_apptJobId, bean.getLocale(), mockId);
				return resultResp.getResult().getShowUI().getCgRespBody().getLogin();
			};
			ssotoken_rawData = continuousCheckStatus(ssotoken_apptJobId, bean, Boolean.TRUE, (ls) -> {
				String status = ls.getStatus();
				if (!(TsmpDpApptJobStatus.DONE.isValueEquals(status) ||
					  TsmpDpApptJobStatus.ERROR.isValueEquals(status) ||
					  TsmpDpApptJobStatus.CANCEL.isValueEquals(status)) && !isLoopTimeout(bean, loopStart)) {
					return true;
				}
				return false;
			}, getLoopStatus, getData);
		}
		bean.setRawData(ssotoken_rawData);
	}
	
	private boolean isLoopTimeout(InvokeBean bean, long loopStart) {
		long ssotoken_now = DateTimeUtil.now().getTime();
		return ((ssotoken_now - loopStart) / 1000 / 60) > bean.getnMinutes();
	}
	
	private String continuousCheckStatus(Long apptJobId, InvokeBean bean, boolean isRepeat, //
			Predicate<TsmpInvokeCommLoopStatus> isContinue, Supplier<TsmpInvokeCommLoopStatus> getLoopStatus, //
			Supplier<String> getData) {
		TsmpInvokeCommLoopStatus ssotoken_ls = getLoopStatus.get();
		while (isContinue.test(ssotoken_ls)) {
			sleepInLoop(apptJobId);
			ssotoken_ls = getLoopStatus.get();//為了拿到狀態
		}
		
		if (TsmpDpApptJobStatus.DONE.isValueEquals(ssotoken_ls.getStatus())) {
			return getData.get(); //為了拿到 resultResp.getResult().getShowUI().getCgRespBody().getData()
		} else if (TsmpDpApptJobStatus.ERROR.isValueEquals(ssotoken_ls.getStatus()) && isRepeat) {
			long loopStart = DateTimeUtil.now().getTime();
			return continuousCheckStatus(apptJobId, bean, false, (loopStatus) -> {
				String ssotoken_status = loopStatus.getStatus();
				if (!(TsmpDpApptJobStatus.DONE.isValueEquals(ssotoken_status)) && !isLoopTimeout(bean, loopStart)) {
					return true;
				}
				return false;
			}, getLoopStatus, getData);
		} else {
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}
 
	private InvokeBean checkParams(String userName, String codeChallenge, String locale) {
		if (!StringUtils.hasText(userName)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		if (!StringUtils.hasText(codeChallenge)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		if (!StringUtils.hasText(locale)) {
			locale = LocaleType.EN_US;
		}
 
		InvokeBean bean = new InvokeBean();
		bean.setUserName(userName);
		bean.setCodeChallenge(codeChallenge);
		bean.setLocale(locale);

		return bean;
	}

	private Integer getNMinutes(String mockId) {
		String nMinutes = getTsmpDpItemsParam3("MOCK_CONFIG", mockId);
		return Integer.valueOf(nMinutes);
	}
	
	protected void sleepInLoop(Long apptJobId) {
		try {
			Thread.sleep(1000L);
		} catch (InterruptedException ssotoken_e) {
			this.logger.debug("thread error: " + StackTraceUtil.logStackTrace(ssotoken_e));
		    Thread.currentThread().interrupt();
		} catch (Exception ssotoken_e) {
			this.logger.debug("thread error: " + StackTraceUtil.logStackTrace(ssotoken_e));
		}
	}

	private String getTsmpDpItemsParam3(String itemNo, String subitemNo) {
		TsmpDpItemsId id = new TsmpDpItemsId(itemNo, subitemNo, LocaleType.EN_US);
		TsmpDpItems ssotoken_s = getTsmpDpItemsCacheProxy().findById(id);
		if (ObjectUtils.isEmpty(ssotoken_s) || !StringUtils.hasText(ssotoken_s.getParam3())) {
			this.logger.debug(String.format("未設定 param3 的值: %s-%s", itemNo, subitemNo));
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		return ssotoken_s.getParam3();
	}

	protected DPB0123Resp invokeDPB0123(String userName, String codeChallenge, String locale) {
		return getDPB0123Service().queryInvoke(HttpMethod.GET.name(), userName, codeChallenge,
				mockId, apiType, locale);
	}
 
	private Users updateUserBlock(Users users, String userBlock, String userIp, String userHostname, String txnUid) throws Exception {
		InnerInvokeParam iip = getDgrAuditLogService().getInnerInvokeParam(tokenApiUrl, "SYS", "SYS", userIp, 
				userHostname, txnUid+"_uUsers", null, null);
		
		//寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_USER.value());
		
		String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, users); //舊資料統一轉成 String
		
		String encodePassword = getEncodePassword(userBlock);
		users.setPassword(encodePassword);
		users = getUsersDao().saveAndFlush(users);
		
		//寫入 Audit Log D
		lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
				Users.class.getSimpleName(), TableAct.U.value(), oldRowStr, users);// U
		
		return users;
	}
	
	/**
	 * 取得密碼,自動建立 User 相關資料
	 * 
	 * @param flag3_ssoAutoCreateUser
	 * @param userName
	 * @throws Exception 
	 */
	protected String getUserBlockAndCreateUserData(boolean flag3_ssoAutoCreateUser, String userName, 
			String userMail, String userIp, String userHostname, String txnUid) throws Exception {
		TsmpAuthorization auth = new TsmpAuthorization();
		auth.setUserName("SSO SYSTEM");				
		
		TsmpUser tsmpUser = getTsmpUserDao().findFirstByUserName(userName);
		String ssotoken_secret = null;
		String ssotoken_userBlock = null;
		if(tsmpUser != null) {//有User資料
			// 取得 secret	
			TsmpSsoUserSecret ssoUserSecret = getTsmpSsoUserSecretDao().findFirstByUserName(userName);
			if(ssoUserSecret != null) {
				ssotoken_secret = ssoUserSecret.getSecret();
				ssotoken_userBlock = getUserBlock(userName, ssotoken_secret);
				//驗證加密的密碼
				Optional<Users> opt_users = getUsersDao().findById(userName);
				Users users = null;
				if(opt_users.isPresent()) {
					users = opt_users.get();
					boolean isMach = OAuthUtil.bCryptPasswordCheck(ssotoken_userBlock, users.getPassword());
					if(!isMach) {
						//更新 User 密碼
						updateUserBlock(users, ssotoken_userBlock, userIp, userHostname, txnUid);
					}
				}
				
			}else {// 若查無資料, 則建立secret
				ssotoken_secret = createSsoUserSecret(userName);
				ssotoken_userBlock = getUserBlock(userName, ssotoken_secret);
				//更新 User 密碼
				Optional<Users> opt_users = getUsersDao().findById(userName);
				if(opt_users.isPresent()) {
					Users users = opt_users.get();
					updateUserBlock(users, ssotoken_userBlock, userIp, userHostname, txnUid);
				}
			}
		}else{//查無User資料
			if(flag3_ssoAutoCreateUser) {//自動建立User資料
				//(1). 判斷是否有 User Email
				if(!StringUtils.hasText(userMail)) {
					this.logger.debug("User Email 不存在");
					throw TsmpDpAaRtnCode._1260.throwing();
				}
				
				//(2). 取得 PWD
				TsmpSsoUserSecret ssoUserSecret = getTsmpSsoUserSecretDao().findFirstByUserName(userName);
				if(ssoUserSecret != null) {
					getTsmpSsoUserSecretDao().delete(ssoUserSecret);
				}
				ssotoken_secret = createSsoUserSecret(userName);
				ssotoken_userBlock = getUserBlock(userName, ssotoken_secret);
				
				//(3). 建立組織	
				String orgId = createOrg(auth);
				
				//(4). 建立角色	
				String roleId = createRole(auth, userIp, userHostname, txnUid);
				
				//(5). 建立 USER 資料
				createUser(auth, userName, ssotoken_userBlock, userMail, roleId, orgId, userIp, userHostname, txnUid);
			}
		}
		
		return ssotoken_userBlock;
	}
	
	protected String getEncodePassword(String password) throws Exception {
		String ssotoken_encodePassword = null;
		ssotoken_encodePassword = OAuthUtil.bCryptEncode(password);
		return ssotoken_encodePassword;
		
	}
	
	/**
	 * 取得根組織的 org_id
	 */
	public String getRootOrgId() {
    	String ssotoken_rootOrgId = null;
    	
    	// 取得 Root 組織的 org_id	
    	TsmpOrganization org1 = getTsmpOrganizationDao().findFirstByParentId("");
    	if(org1 != null) {
    		ssotoken_rootOrgId = org1.getOrgId();
    	}else {
    		TsmpOrganization org2 = getTsmpOrganizationDao().findFirstByParentId(null);
    		if(org2 != null) {
    			ssotoken_rootOrgId = org2.getOrgId();
	    	}else {
	    		this.logger.debug("查無根組織的 org id");
	    		throw TsmpDpAaRtnCode._1298.throwing();
	    	}
    	}
    	return ssotoken_rootOrgId;
	}
	
	/**
	 * 建立 TSMP_SSO_USER_SECRET (SSO的使用者的部份PW) 資料
	 * 
	 * @return
	 */
	private String createSsoUserSecret(String userName) {
		String secret = RandomUtils.randomString(4, true, true);//取得亂數
		
		TsmpSsoUserSecret ssoUserSecret = new TsmpSsoUserSecret();
		ssoUserSecret.setUserName(userName);
		ssoUserSecret.setSecret(secret);
		ssoUserSecret.setCreateDateTime(DateTimeUtil.now());
		ssoUserSecret.setCreateUser("SSO SYSTEM");
		ssoUserSecret = getTsmpSsoUserSecretDao().saveAndFlush(ssoUserSecret);
		return secret;
	}
	
	private String getUserBlock(String userName, String secret) {
		String userPwd = userName + secret;
		String userBlock = ServiceUtil.base64Encode(userPwd.getBytes());
		return userBlock;
	}
	
	/**
	 * 判斷是否有 "SSO" 的組織, 若查無資料, 則新增組織資料
	 */
	private String createOrg(TsmpAuthorization auth) {
		String ssotoken_orgId = null;
		String orgName = "SSO";
		
		TsmpOrganization tsmpOrganization = getTsmpOrganizationDao().findByOrgName(orgName);
	    if(tsmpOrganization != null) {
	    	ssotoken_orgId = tsmpOrganization.getOrgId();
	    	
	    }else{//沒有 "SSO" 組織
	    	// 建立 "SSO" 組織
	    	String ssotoken_rootOrgId = getRootOrgId();
	    	AA1001Req req = new AA1001Req();
	    	req.setParentId(ssotoken_rootOrgId);
	    	req.setOrgName("SSO");
	    	req.setOrgCode("SSO");
	    	req.setContactTel("昕力客服");
	    	req.setContactName("+886-2-8751-1610");
	    	req.setContactMail("service@tpisoftware.com");
	    	AA1001Resp resp = getAA1001Service().addTOrg(auth, req);
	    	ssotoken_orgId = resp.getOrgId();
	    }
	    return ssotoken_orgId;
	}
	
	/**
	 * 建立 USER 資料
	 */
	private void createUser(TsmpAuthorization auth, String userName, String userBlock, String userMail,
			String roleId, String orgId, String userIp, String userHostname, String txnUid) {
		
		String bCryptEncodeQuyType = OAuthUtil.bCryptEncode("1"); //1: 正常
		String base64EncodeQuyType = ServiceUtil.base64Encode(bCryptEncodeQuyType.getBytes());
		base64EncodeQuyType += ",0";
		
		ReqHeader ssotoken_reqHeader = new ReqHeader();
		ssotoken_reqHeader.setLocale( LocaleType.EN_US);
		
		AA0001Req ssotoken_req = new AA0001Req();
		ssotoken_req.setUserName(userName);
		ssotoken_req.setUserAlias(userName);
		ssotoken_req.setUserBlock(userBlock);
		ssotoken_req.setUserMail(userMail);
		ssotoken_req.setRoleIDList(Arrays.asList(roleId));
		ssotoken_req.setOrgID(orgId);
		ssotoken_req.setEncodeStatus(base64EncodeQuyType);
		
		InnerInvokeParam iip = getDgrAuditLogService().getInnerInvokeParam("/11/AA0001", "SYS", "SYS", userIp, 
				userHostname, txnUid+"_cUser", null, null);
		getAA0001Service().addTUser(auth, ssotoken_req, ssotoken_reqHeader, iip);
	}
	
	/**
	 * 判斷是否有 "SSO" 的角色, 若查無資料, 則新增使用者角色
	 */
	private String createRole(TsmpAuthorization auth, String userIp, String userHostname, String txnUid) {
		String ssotoken_roleId = null;
		String roleName = "SSO";
		
		TsmpRole tsmpRole = getTsmpRoleDao().findFirstByRoleName(roleName);
		if(tsmpRole != null) {
			ssotoken_roleId = tsmpRole.getRoleId();
			
		}else{//沒有 "SSO" 角色
			// 建立 "SSO" 角色
			AA0011Req ssotoken_req = new AA0011Req();
			ssotoken_req.setRoleName("SSO");
			ssotoken_req.setRoleAlias("SSO");
			//功能清單:舊角色維護 (AC0012),角色維護(DGL0003)
			ssotoken_req.setFuncCodeList(Arrays.asList("AC0012"));
			
			InnerInvokeParam iip = getDgrAuditLogService().getInnerInvokeParam("/dgrv4/11/AA0011", "SYS", "SYS", userIp, 
					userHostname, txnUid+"_cRole", null, null);
			AA0011Resp resp = getAA0011Service().addTRole(auth, ssotoken_req, iip);
			ssotoken_roleId = resp.getRoleId();
		}
		return ssotoken_roleId;
	}
	
	/**
	 * 將 Request.userName 經過 Base64 Decode ,判斷最後面是否有 0x80,				
	 * 若有,拿掉 0x80, 得到原 User Name , 即 {userName}				
	 * 若沒有,則 throw 1496。(登入失敗)
	 */
	protected String checkUserNameAndGetUserName(String base64EnUserName) {
		byte[] ssotoken_data1 = null;
		try {
			ssotoken_data1 = ServiceUtil.base64Decode(base64EnUserName);
		} catch (Exception e) {
			throw TsmpDpAaRtnCode._1500.throwing("username");
		}
		boolean flag = ssotoken_data1[ssotoken_data1.length-1] == (byte)0x80;
		if(!flag) {
			this.logger.debug("User Name沒有特殊字");
			throw TsmpDpAaRtnCode._1496.throwing();
		}
		
		byte[] data2 = new byte[ssotoken_data1.length-1];
		System.arraycopy(ssotoken_data1, 0, data2, 0, ssotoken_data1.length-1);//陣列複製:參數分別是來源陣列、來源起始索引、目的陣列、目的起始索引、複製長度
		
		String userName = new String(data2);
		
		return userName;
	}
	
	/** 
	 * 比對資料庫是否有匹配的 User Name 和 處理過的 UUID
	 * (1). 將 Request.codeVerifier 經過 SHA256 和 Base64URL Encode without Padding, 即 {uuid_en} , Base64UrlEncode(SHA256(UUID))	
	 * (2). 查詢 SSO_AUTH_RESULT , user_name = {userName} 和 code_challenge ＝ {uuid_en} , 
	 * 且 createDateTime > now - N分鐘 (也就是在這N分鐘內建立的) 的資料	
	 * 若查無資料,則 throw 1496。(登入失敗)		
	 */
	protected void checkPkce(String userName, String uuidEn, Date queryStartDate) {
		this.logger.debug("userName:" + userName);
		this.logger.debug("codeChallenge:" + uuidEn);
		this.logger.debug("queryStartDate:" + queryStartDate);
		
		List<SsoAuthResult> list = getSsoAuthResultDao().findByUserNameAndCreateDateTimeAfterOrderByCreateDateTimeDesc(userName, queryStartDate);
		if (CollectionUtils.isEmpty(list)) {//在N分鐘內,查無UUID資料
			//資料表 SSO_AUTH_RESULT 沒有匹配的 User Name 和 處理過的 UUID
			this.logger.debug("Data table SSO_AUTH_RESULT has no matching User Name and processed UUID");
			throw TsmpDpAaRtnCode._1496.throwing();
		}

		SsoAuthResult result = list.get(0);//取得最新的資料
		if(result == null) {
			throw TsmpDpAaRtnCode._1496.throwing();
		}
		
		if(!result.getCodeChallenge().equals(uuidEn)) {//傳入的UUID不是最新的,或查無傳入的UUID資料
			//資料表 SSO_AUTH_RESULT 沒有匹配的 User Name 和 處理過的 UUID, 或不是最新的 UUID
			this.logger.debug("The data table SSO_AUTH_RESULT does not have a matching User Name "
					+ "and processed UUID, or is not the latest UUID");
			throw TsmpDpAaRtnCode._1496.throwing();
		}
		
		if(result.getUseDateTime() != null) {//傳入的UUID,已被使用過
			//資料表 SSO_AUTH_RESULT 的 User Name 和 處理過的 UUID, 已被使用過
			this.logger.debug("The User Name and the processed UUID of the data table SSO_AUTH_RESULT, have been used");
			throw TsmpDpAaRtnCode._1496.throwing();
		}
		
		//資料表 SSO_AUTH_RESULT 的 User Name 和 處理過的 UUID, 找到匹配的資料
		this.logger.debug("User Name and processed UUID of data table SSO_AUTH_RESULT, find matching data");
		
		//更新這筆資料的 use_date_time = 現在時間, 表示已使用
		result.setUseDateTime(DateTimeUtil.now());
		getSsoAuthResultDao().saveAndFlush(result);
	}
	
	protected Date getQueryStartDate(LocalDateTime ldt, int minute) {
		Date date = minusMinute(ldt, minute);
		return date;
	}
	
	protected LocalDateTime getNow() {
		LocalDateTime now = LocalDateTime.now();
		return now;
	}
	
	/**
	 * 減N分鐘
	 * 
	 * @param dt
	 * @param days
	 * @return
	 */
	private Date minusMinute(LocalDateTime ldt, int minute) {
		ldt = ldt.minusMinutes(minute);
		return Date.from( ldt.atZone(ZoneId.systemDefault()).toInstant() );
	}
	
	
	
	private void getToken(Map<String, String> parameters, String scheme, HttpServletResponse res, 
			String userIp, String userHostname, String txnUid) throws Exception {
		
		String grantType = parameters.get("grant_type");
		String ssotoken_userName = parameters.get("username");
		
		//若 parameters 的 grant_type 不是 "password" 且 不是 "refreah_token"的, 一律轉成 "password"
		if (!"password".equals(grantType) && !"refresh_token".equals(grantType)) {
			parameters.put("grant_type", "password");
		}
		
		// 檢查 User 狀態
		checkUserInfo(ssotoken_userName, userIp, userHostname, txnUid);
		
		// 取得 AES Key						
		String ssotoken_aesKey = getAesKey();
		this.logger.debug("--TAEASK:" + ServiceUtil.dataMask(ssotoken_aesKey, 2, 2));
		
		// 取得 Client ID/PW
		String clientId = getClientId(ssotoken_aesKey);
		this.logger.debug("--clientId:" + ServiceUtil.dataMask(clientId, 2, 2));
		String clientPw = getClientPw(ssotoken_aesKey);
		this.logger.debug("--clientPw:" + ServiceUtil.dataMask(clientPw, 2, 2));
		
		// 組成 Http Header Authorization 
		String ssotoken_authorization = getAuthorization(clientId, clientPw);
		
		/*
		 * 調用 oauth/token 取得真正的 Token						
		 */
 
		// url，修改為直接從 OAuthTokenService 取 Token。
//		String reqUrl = getBridgeUrl(scheme);
		
		// method
		String method = HttpMethod.POST.toString();
		
		// http header
		Map<String,String> httpHeader = new HashMap<>();
		httpHeader.put("Authorization", ssotoken_authorization);
		
		// form data
		Map<String,String> formData = parameters;
		
		// http send...
//		HttpRespData respObj = HttpUtil.httpReqByFormData(reqUrl, method, formData, httpHeader, true);
//		logger.debug(respObj.getLogStr());
		
		ResponseEntity<OAuthTokenResp> respObj = (ResponseEntity<OAuthTokenResp>) getOAuthTokenService().getToken(formData, ssotoken_authorization, "/oauth/token");
		OAuthTokenResp oauthTokenResp = respObj.getBody();
		
		ObjectMapper objectMapper = new ObjectMapper();
		String json = objectMapper.writeValueAsString(oauthTokenResp);
		
//		InputStream inputStream = null;
//		res.setStatus(respObj.statusCode);
//		inputStream = respObj.respInputStreamObj;
//		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		//http InputStream copy into Array
//		IOUtils.copy(inputStream, bo);
		byte httpArray[] = json.toString().getBytes();
		String ssotoken_httpRespStr = new String(httpArray , StandardCharsets.UTF_8);
		logger.debug("\n oauth token result body:\n" + ssotoken_httpRespStr + "\n"); 
		
		String tokenJti = getJti(ssotoken_httpRespStr);
		
		ByteArrayInputStream bi = new ByteArrayInputStream(httpArray);
		IOUtils.copy(bi, res.getOutputStream());
		
		//更新 TSMP_USER
		updateUser(res.getStatus(), ssotoken_userName, userIp, userHostname, txnUid, tokenJti);
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
		ObjectMapper ssotoken_om = new ObjectMapper();
		JsonNode json = ssotoken_om.readTree(httpRespStr);
		String jti = json.get("jti").asText();
		return jti;
	}
	
	public HttpHeaders buildHeaderVo(HttpServletRequest req) {
		Enumeration<String> headerkeys = req.getHeaderNames();

		HttpHeaders ssotoken_headers = new HttpHeaders();

		while (headerkeys.hasMoreElements()) {
			String headerKey = headerkeys.nextElement();
			Enumeration<String> vals = req.getHeaders(headerKey);
			while (vals.hasMoreElements()) {
				String headerVal = vals.nextElement();
				ssotoken_headers.add(headerKey, headerVal);
			}
		}

		return ssotoken_headers;
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
	 *  b.若上述 pwd_fail_times >= {failThreshold}, 則更新 user_status 為 "3" (鎖定)					
	 *  c.更新 update_time 為現在時間					
	 */
	private void updateUser(int statusCode, String userName, String userIp, String userHostname, String txnUid,
			String tokenJti) {
		if(!StringUtils.hasText(userName)) {
			return;
		}
		InnerInvokeParam iip = getDgrAuditLogService().getInnerInvokeParam(tokenApiUrl, "SYS", "SYS", userIp, 
				userHostname, txnUid+"_uUser", null, null);
		
		String oldRowStr = "";
		String ssotoken_loginState = "";
		
		TsmpUser user = getTsmpUserDao().findFirstByUserName(userName);
		if (user == null) {
			ssotoken_loginState = "FAILED";
			
		}else {
			oldRowStr = getDgrAuditLogService().writeValueAsString(iip, user); //舊資料統一轉成 String
			
			if (statusCode == 200) {
				ssotoken_loginState = "SUCCESS";
				user.setPwdFailTimes(0);
				user.setLogonDate(new Date());
				user.setUpdateTime(new Date());
				user.setUpdateUser(userName);
				
				user = getTsmpUserDao().saveAndFlush(user);
				
			} else if (statusCode == 400 || statusCode == 401) {
				ssotoken_loginState = "FAILED";
				int ssotoken_failTimes = user.getPwdFailTimes();
				ssotoken_failTimes++;
				user.setPwdFailTimes(ssotoken_failTimes);
				user.setUpdateTime(new Date());
				int failThreshold = getFailThreshold();//允許User密碼錯誤次數上限
				if (ssotoken_failTimes >= failThreshold) {
					user.setUserStatus("3");
				}
				
				user = getTsmpUserDao().saveAndFlush(user);
			}
		}
		
		//寫入 Audit Log M,登入成功或失敗
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogMForLogin(lineNumber, eventNo, tokenApiUrl, 
				userName, auditClientId, userIp, userHostname, ssotoken_loginState, statusCode+"", txnUid, tokenJti, null);
		
		if (user != null) {
			//寫入 Audit Log M,更新User
			lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_USER.value());
			
			//寫入 Audit Log D,更新User
			lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
					TsmpUser.class.getSimpleName(), TableAct.U.value(), oldRowStr, user);// U
		}
	}
	
	
	
	private void checkUserInfo(String userName, String userIp, String userHostname, String txnUid) {
		if(!StringUtils.hasText(userName)) {
			return;
		}
		
		TsmpUser user = getTsmpUserDao().findFirstByUserName(userName);
		if (user == null) {
			return;
		}

		int ssotoken_failTimes = user.getPwdFailTimes();
		String ssotoken_userStatus = user.getUserStatus();

		String loginState = "FAILED";
		if ("2".equals(ssotoken_userStatus)) {
			//寫入 Audit Log M
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogMForLogin(lineNumber, eventNo, tokenApiUrl, 
					userName, auditClientId, userIp, userHostname, loginState, TsmpDpAaRtnCode._1473.getCode(), txnUid, null, null);
			
			//若 TSMP_USER.user_status (使用者狀態) = "2", 則 throw 1473。 (使用者已停權)
			throw TsmpDpAaRtnCode._1473.throwing();
		}
		if ("3".equals(ssotoken_userStatus)) {
			//寫入 Audit Log M
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogMForLogin(lineNumber, eventNo, tokenApiUrl, 
					userName, auditClientId, userIp, userHostname, loginState, TsmpDpAaRtnCode._1472.getCode(), txnUid, null, null);
			
			//若 TSMP_USER.user_status (使用者狀態) = "3", 則 throw 1472。 (使用者已鎖定)
			throw TsmpDpAaRtnCode._1472.throwing();
		}
		int failThreshold = getFailThreshold();//允許User密碼錯誤次數上限
		if (ssotoken_failTimes >= failThreshold) {
			//寫入 Audit Log M
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogMForLogin(lineNumber, eventNo, tokenApiUrl, 
					userName, auditClientId, userIp, userHostname, loginState, TsmpDpAaRtnCode._1471.getCode(), txnUid, null, null);
			
			//若 TSMP_USER.pwd_fail_times (密碼錯誤次數) >= {failThreshold}, 則 throw 1471。 (密碼錯誤超過上限)	
			throw TsmpDpAaRtnCode._1471.throwing();
		}
	}
	
	
	
	/**
	 * 取得 Client ID
	 * 查詢資料表 TSMP_SETTING.id = "TSMP_AC_CLIENT_ID" 的 value, 								
	 * 1.若 AES Key 有值,								
	 * 則 TSMP_SETTING.value 為 AES 加密過的 clientId,取得解密的 clientId						
	 * 2.否則若 AES Key 沒有值, 則 clientId 為 TSMP_SETTING.value 的值,表示此資料未加密								
	 */
	private String getClientId(String aesKey) {
		String ssotoken_acClientId = getTsmpSettingService().getVal_TSMP_AC_CLIENT_ID();
		String clientId = "";
		if (!StringUtils.hasText(aesKey)) {
			clientId = ssotoken_acClientId;
		}else {
			clientId = getTsmpTAEASKHelper().decrypt(ssotoken_acClientId);
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
	private String getClientPw(String aesKey) {
		String ssotoken_acClientPw = getTsmpSettingService().getVal_TSMP_AC_CLIENT_PW();
		String clientPw = "";
		if (!StringUtils.hasText(aesKey)) {
			clientPw = ssotoken_acClientPw;
		}else {
			clientPw = getTsmpTAEASKHelper().decrypt(ssotoken_acClientPw);
		}
		return clientPw;
	}
	
	/**
	 * 取得平台啟動時, 由 shell 輸入的 AES Key	
	 * 註: 需要交付組在啟動v3的shell或bat中設定"TAEASK"的值，這邊才會抓得到
	 */							
	private String getAesKey() {
		String ssotoken_key = System.getenv("TAEASK");
		return ssotoken_key;
	}
	
	/**
	 * 組成 Http Header Authorization
	 */
	private String getAuthorization(String clientId, String clientPw) {
		String ssotoken_info =  clientId + ":" + clientPw;
		ssotoken_info = Base64Util.base64EncodeWithoutPadding(ssotoken_info.getBytes());//Base64 Encode(無後綴)
		String authorization = "Basic " + ssotoken_info;
		return authorization;
	}

	/**
	 * 取得允許User密碼錯誤次數上限
	 * @param aesKey
	 * @return
	 */
	private int getFailThreshold() {
		int ssotoken_failThreshold = getTsmpSettingService().getVal_TSMP_FAIL_THRESHOLD();
		return ssotoken_failThreshold;
	}
	
	protected TsmpSettingDao getTsmpSettingDao() {
		return this.tsmpSettingDao;
	}
	
	protected TsmpSettingService getTsmpSettingService() {
		return this.tsmpSettingService;
	}
	
	protected TsmpUserDao getTsmpUserDao() {
		return this.tsmpUserDao;
	}
	
	protected TsmpTAEASKHelper getTsmpTAEASKHelper() {
		return this.tsmpTAEASKHelper;
	}
	
	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}
	
	protected SsoAuthResultDao getSsoAuthResultDao() {
		return this.ssoAuthResultDao;
	}
	
	protected TsmpSsoUserSecretDao getTsmpSsoUserSecretDao() {
		return this.tsmpSsoUserSecretDao;
	}
	
	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}
	
	protected TsmpRoleDao getTsmpRoleDao() {
		return this.tsmpRoleDao;
	}
	
	protected UsersDao getUsersDao() {
		return this.usersDao;
	}
	
	protected AA0011Service getAA0011Service() {
		return this.aa0011Service;
	}
	
	protected AA1001Service getAA1001Service() {
		return this.aa1001Service;
	}
	
	protected AA0001Service getAA0001Service() {
		return this.aa0001Service;
	}
	
	protected DPB0123Service getDPB0123Service() {
		return this.dpb0123Service;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return tsmpDpItemsCacheProxy;
	}

	protected TsmpRtnCodeDao getTsmpRtnCodeDao() {
		return this.tsmpRtnCodeDao;
	}

	protected JobHelper getJobHelper() {
		return this.jobHelper;
	}
	
	protected ApplicationContext getCtx() {
		return this.ctx;
	}
	
	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}
	
	protected OAuthTokenService getOAuthTokenService() {
		return this.oAuthTokenService;
	}
}