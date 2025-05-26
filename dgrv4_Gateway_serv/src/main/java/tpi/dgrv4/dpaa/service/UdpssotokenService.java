package tpi.dgrv4.dpaa.service;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
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
public class UdpssotokenService{
	private TPILogger logger = TPILogger.tl;
	
	private String mockId = "DPB0123Udp";
	private String apiType = "udpDoubleCheckLogin";
	
	//Audit Log使用
	String tokenApiUrl = "/udpssotoken/oauth/token";
	String eventNo = AuditLogEvent.LOGIN.value();
	String auditClientId = "";
	
	private TsmpUserDao tsmpUserDao;	
	private TsmpSettingDao tsmpSettingDao;	
	private TsmpSettingService tsmpSettingService;	
	private TsmpTAEASKHelper tsmpTAEASKHelper;	
	private ServiceConfig serviceConfig;	
	private SsoAuthResultDao ssoAuthResultDao;	
	private TsmpSsoUserSecretDao tsmpSsoUserSecretDao;	
	private TsmpOrganizationDao tsmpOrganizationDao;	
	private TsmpRoleDao tsmpRoleDao;	
	private UsersDao usersDao;	
	private AA1001Service aa1001Service;	
	private AA0011Service aa0011Service;	
	private AA0001Service aa0001Service;	
	private DPB0123Service dpb0123Service;	
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;	
	private TsmpRtnCodeDao tsmpRtnCodeDao;	
	private ApplicationContext ctx;	
	private JobHelper jobHelper;	
	private DgrAuditLogService dgrAuditLogService;	
	private OAuthTokenService oAuthTokenService;

	@Autowired
	public UdpssotokenService(TsmpUserDao tsmpUserDao, TsmpSettingDao tsmpSettingDao,
			TsmpSettingService tsmpSettingService, TsmpTAEASKHelper tsmpTAEASKHelper, ServiceConfig serviceConfig,
			SsoAuthResultDao ssoAuthResultDao, TsmpSsoUserSecretDao tsmpSsoUserSecretDao,
			TsmpOrganizationDao tsmpOrganizationDao, TsmpRoleDao tsmpRoleDao, UsersDao usersDao,
			AA1001Service aa1001Service, AA0011Service aa0011Service, AA0001Service aa0001Service,
			DPB0123Service dpb0123Service, TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, TsmpRtnCodeDao tsmpRtnCodeDao,
			ApplicationContext ctx, JobHelper jobHelper, DgrAuditLogService dgrAuditLogService,
			OAuthTokenService oAuthTokenService) {
		super();
		this.tsmpUserDao = tsmpUserDao;
		this.tsmpSettingDao = tsmpSettingDao;
		this.tsmpSettingService = tsmpSettingService;
		this.tsmpTAEASKHelper = tsmpTAEASKHelper;
		this.serviceConfig = serviceConfig;
		this.ssoAuthResultDao = ssoAuthResultDao;
		this.tsmpSsoUserSecretDao = tsmpSsoUserSecretDao;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.tsmpRoleDao = tsmpRoleDao;
		this.usersDao = usersDao;
		this.aa1001Service = aa1001Service;
		this.aa0011Service = aa0011Service;
		this.aa0001Service = aa0001Service;
		this.dpb0123Service = dpb0123Service;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.tsmpRtnCodeDao = tsmpRtnCodeDao;
		this.ctx = ctx;
		this.jobHelper = jobHelper;
		this.dgrAuditLogService = dgrAuditLogService;
		this.oAuthTokenService = oAuthTokenService;
	}

	private class InvokeBean {
		private String rawData;
		private String userName;
		private String codeChallenge;
		private String locale;
		private String mockId;
		private Integer nMinutes;
		private String userIp;
		
		public String getRawData() {
			return rawData;
		}
		public void setRawData(String rawData) {
			this.rawData = rawData;
		}
		public String getCodeChallenge() {
			return codeChallenge;
		}
		public void setCodeChallenge(String codeChallenge) {
			this.codeChallenge = codeChallenge;
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
		public Integer getnMinutes() {
			return nMinutes;
		}
		public void setnMinutes(Integer nMinutes) {
			this.nMinutes = nMinutes;
		}
		public String getMockId() {
			return mockId;
		}
		public void setMockId(String mockId) {
			this.mockId = mockId;
		}
		public String getUserIp() {
			return userIp;
		}
		public void setUserIp(String userIp) {
			this.userIp = userIp;
		}
	}
	
	//為了讓 Unit test 使用 override
	protected boolean isGetToken() {
		boolean isEnable = true;
		return isEnable;
	}
	
	protected void getUdpssotoken(Map<String, String> parameters, String scheme, HttpServletResponse res, 
			String userIp, String locale, String userHostname, String txnUid) throws Exception {
		// 將統一入口網登入URL 放在 Http Response Header 中
		String udpLoginUrl = parameters.get("udpLoginUrl");
		if(!StringUtils.hasText(udpLoginUrl)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		if(res != null) {
			res.addHeader("udpLoginUrl", udpLoginUrl);
			//checkmarx, Missing HSTS Header
            res.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload"); 
            
		}
		
		// 判斷User IP,是否在可登入此伺服器環境的網段中	
		checkIpInNetwork(userIp);
		
		String grantType = parameters.get("grant_type");
		String userName = null;
		
		if(!"refresh_token".equals(grantType)) {
			userName = doPassword(parameters, userIp, locale, userHostname, txnUid);
		}else {
			userName = null;
		}
		parameters.put("username", userName);//改放沒有特殊字的User Name 或 null
		
		if(isGetToken() && res != null) {
			getToken(parameters, scheme, res, userIp, userHostname, txnUid);
		}
		
		//2022/6/20增加,渣打SCB需求,清空Users密碼
		if(!StringUtils.isEmpty(userName)) {
			Optional<Users> opt_users = getUsersDao().findById(userName);
			if(opt_users.isPresent()) {
				Users users = opt_users.get();
				updateUserBlock(users, "", userIp, userHostname, txnUid, 2);
			}
		}
	}
	
	public void getUdpssotoken(HttpHeaders headers, HttpServletRequest httpReq, HttpServletResponse res, ReqHeader reqHeader) throws Exception {
		String scheme = httpReq.getScheme();
		
		Map<String, String> parameters = new HashMap<>();
		httpReq.getParameterMap().forEach((k, vs) -> {
			if (vs.length != 0) {
				parameters.put(k, vs[0]);
			}
		});
		
		String txnUid = getDgrAuditLogService().getTxnUid();
 
		logger.debug(String.format("http header size = %d", headers.entrySet().size()));
		for (Entry<String, List<String>> entry : headers.entrySet()) {
			String key = entry.getKey();
			List<String> val = entry.getValue();
			logger.debug(String.format("http key [%s] = %s", key, val.toString()));
		}
		String userIp = StringUtils.isEmpty(headers.getFirst("x-forwarded-for")) ? httpReq.getRemoteAddr() : headers.getFirst("x-forwarded-for");
		String userHostname = httpReq.getRemoteHost();
		getUdpssotoken(parameters, scheme, res, userIp, reqHeader.getLocale(), userHostname, txnUid);
	}
	
	
	
	protected String doPassword(Map<String, String> parameters, String userIp, String locale, 
			String userHostname, String txnUid) throws Exception {
		//check param
		String userName = parameters.get("username");
		String codeVerifier = parameters.get("codeVerifier");
		String userMail = parameters.get("userMail");
		if(!StringUtils.hasText(userName)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		if(!StringUtils.hasText(codeVerifier)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		
		// 判斷 userName 是否有特殊字,並取得原 userName
		userName = checkUserNameAndGetUserName(userName);
		
		// 判斷 user 若為 manager,不能以 SSO 方式登入
		if("manager".equals(userName)) {
			throw TsmpDpAaRtnCode._1518.throwing();
		}
		
		String userName_forLdap = userName;//Ldap 帳號可以有空格,但 digiRunner 帳號不能有空格
		userName = userName.replace(" ", "_");//將 userName 的空白取代為底線 "_", 因 digiRunner 帳號不能有空格
		
		// 取得參數 flag 和 invoke URL
		boolean flag2_ssoDoubleCheck = false; //判斷 Double check驗證 是否啟用
		boolean flag3_ssoAutoCreateUser = false; //判斷 自動建立User資料 是否啟用
		String flagName = null;
		try {
			flagName = TsmpSettingDao.Key.SSO_DOUBLE_CHECK;
			flag2_ssoDoubleCheck = getTsmpSettingService().getVal_SSO_DOUBLE_CHECK();
			
			flagName = TsmpSettingDao.Key.SSO_AUTO_CREATE_USER;
			flag3_ssoAutoCreateUser = getTsmpSettingService().getVal_SSO_AUTO_CREATE_USER();
 
		} catch (TsmpDpAaException e) {
			this.logger.debug(String.format("查無資料:%s", flagName));
			throw TsmpDpAaRtnCode._1298.throwing();
		}
 
		// 取得處理過的 UUID
		// Base64UrlEncodeWithoutPadding(SHA256(UUID))
		String uuidEn = ServiceUtil.getSHA256ToBase64UrlEncodeWithoutPadding(codeVerifier);
 
		// 取得密碼,依 falg3 自動建立 User 相關資料
		String userBlock = getUserBlockAndCreateUserData(flag3_ssoAutoCreateUser, userName, userMail, userIp, 
				userHostname, txnUid);
		
		parameters.put("password", userBlock);
		
		// 詢問對接企業確認 User 是否有登入
		if(flag2_ssoDoubleCheck) {
			String login = doService(userName_forLdap, uuidEn, userIp, locale);//call DPB0123Udp
			if(!"Y".equals(login)) {
				this.logger.debug(String.format("查無資料:%s" + flagName));
				throw TsmpDpAaRtnCode._1496.throwing();
			}
		}
		return userName;
	}

	/**
	 * call DPB0123Udp
	 * 詢問對接企業(統一入口網)確認 User 是否有登入
	 * 
	 * @param userName
	 * @param codeChallenge
	 * @param locale
	 * @param response
	 * @return
	 */
	protected String doService(String userName_forLdap, String codeChallenge, String userIp, String locale) {
		InvokeBean bean = new InvokeBean();
		try {
			bean = checkParams(userName_forLdap, codeChallenge, locale, userIp);
			bean.setMockId(mockId);
			// 取得 n_minutes
			Integer nMinutes = getNMinutes(bean.getMockId());
			bean.setnMinutes(nMinutes);
			udpDoubleCheckLogin(bean);
			
			return bean.getRawData();
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}
	
	/**
	 * 判斷User IP,是否在可登入此伺服器環境的網段中	
	 * 
	 * @param userIp
	 */
	private void checkIpInNetwork(String userIp) {
		boolean isIpInNetwork = false;
		
		// 取得參數 flag
		String flagName = null;
		String loginNetwork = null;
		try {
			flagName = TsmpSettingDao.Key.UDPSSO_LOGIN_NETWORK;
			loginNetwork = getTsmpSettingService().getVal_UDPSSO_LOGIN_NETWORK();
		} catch (TsmpDpAaException e) {
			this.logger.debug(String.format("查無資料:%s" + flagName));
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		
		if(!StringUtils.hasText(loginNetwork)) {
			this.logger.debug(String.format("不能為空值:%s" + flagName));
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		
		String[] networkArray = loginNetwork.split(",");
		for (String network : networkArray) {
			boolean flag = ServiceUtil.isIpInNetwork(userIp, network);
			if(flag) {
				isIpInNetwork = true;
				break;
			}
		}
		
		if(!isIpInNetwork) {
			this.logger.debug(String.format("[TSMP_SETTING] User IP 不在可登入的網段中, userIp=%s, network=%s", userIp, loginNetwork));
			throw TsmpDpAaRtnCode._1513.throwing();
		}
	}
	
	private void udpDoubleCheckLogin(InvokeBean bean) {
		// 呼叫API取得結果
		String rawData = bean.getRawData();
		DPB0123Resp resp = invokeDPB0123(bean.getUserName(), bean.getCodeChallenge(), bean.getUserIp(), bean.getLocale());
		Long apptJobId = resp.getApptJobId();
		// 直接調用
		if (apptJobId == null) {
			rawData = Optional.ofNullable(resp.getShowUI()) //
				.map((showUI) -> Optional.ofNullable(showUI.getCgRespBody()) //
					.map((cgRespBody) -> cgRespBody.getLogin()) //
					.orElse(new String())
				)
				.orElse(new String());
		// Mock調用
		} else {
			long loopStart = DateTimeUtil.now().getTime();
			Supplier<TsmpInvokeCommLoopStatus> getLoopStatus = () -> {
				DPB0123Resp lsResp = getDPB0123Service().queryLoopStatus(apptJobId, bean.getLocale());
				return lsResp.getCommLoopStatus();
			};
			Supplier<String> getData = () -> {
				DPB0123Resp resultResp = getDPB0123Service().queryResult(apptJobId, bean.getLocale(), mockId);
				return resultResp.getResult().getShowUI().getCgRespBody().getLogin();
			};
			rawData = continuousCheckStatus(apptJobId, bean, Boolean.TRUE, (ls) -> {
				String status = ls.getStatus();
				if (!(TsmpDpApptJobStatus.DONE.isValueEquals(status) ||
					  TsmpDpApptJobStatus.ERROR.isValueEquals(status) ||
					  TsmpDpApptJobStatus.CANCEL.isValueEquals(status)) && !isLoopTimeout(bean, loopStart)) {
					return true;
				}
				return false;
			}, getLoopStatus, getData);
		}
		bean.setRawData(rawData);
	}
	

	private boolean isLoopTimeout(InvokeBean bean, long loopStart) {
		long now = DateTimeUtil.now().getTime();
		return ((now - loopStart) / 1000 / 60) > bean.getnMinutes();
	}
 
	private InvokeBean checkParams(String userName_forLdap, String codeChallenge, String locale, String userIp) {
		if (!StringUtils.hasText(userName_forLdap)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		if (!StringUtils.hasText(codeChallenge)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		} 
		if (!StringUtils.hasText(userIp)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		if (!StringUtils.hasText(locale)) {
			locale = LocaleType.EN_US;
		}
 
		InvokeBean bean = new InvokeBean();
		userName_forLdap = Base64Util.base64EncodeWithoutPadding(userName_forLdap.getBytes());
		bean.setUserName(userName_forLdap);
		bean.setCodeChallenge(codeChallenge);
		bean.setLocale(locale);
		bean.setUserIp(userIp);
		
		return bean;
	}
	
	private String continuousCheckStatus(Long apptJobId, InvokeBean bean, boolean isRepeat, //
			Predicate<TsmpInvokeCommLoopStatus> isContinue, Supplier<TsmpInvokeCommLoopStatus> getLoopStatus, //
			Supplier<String> getData) {
		TsmpInvokeCommLoopStatus ls = getLoopStatus.get();
		while (isContinue.test(ls)) {
			sleepInLoop(apptJobId);
			ls = getLoopStatus.get();//為了拿到狀態
		}
		
		if (TsmpDpApptJobStatus.DONE.isValueEquals(ls.getStatus())) {
			return getData.get(); //為了拿到 resultResp.getResult().getShowUI().getCgRespBody().getData()
		} else if (TsmpDpApptJobStatus.ERROR.isValueEquals(ls.getStatus()) && isRepeat) {
			long loopStart = DateTimeUtil.now().getTime();
			return continuousCheckStatus(apptJobId, bean, false, (loopStatus) -> {
				String status = loopStatus.getStatus();
				if (!(TsmpDpApptJobStatus.DONE.isValueEquals(status)) && !isLoopTimeout(bean, loopStart)) {
					return true;
				}
				return false;
			}, getLoopStatus, getData);
		} else {
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}

	protected void sleepInLoop(Long apptJobId) {
		try {
			Thread.sleep(1000L);
		} catch (InterruptedException e) {
			this.logger.debug("thread error: " + StackTraceUtil.logStackTrace(e));
		    Thread.currentThread().interrupt();
		} catch (Exception e) {
			this.logger.debug("thread error: " + StackTraceUtil.logStackTrace(e));
		}
	}

	private String getTsmpDpItemsParam3(String itemNo, String subitemNo) {
		TsmpDpItemsId id = new TsmpDpItemsId(itemNo, subitemNo, LocaleType.EN_US);
		TsmpDpItems s = getTsmpDpItemsCacheProxy().findById(id);
		if (ObjectUtils.isEmpty(s) || !StringUtils.hasText(s.getParam3())) {
			this.logger.debug(String.format("未設定 param3 的值: %s-%s", itemNo, subitemNo));
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		return s.getParam3();
	}

	protected DPB0123Resp invokeDPB0123(String userName, String codeChallenge, String userIp, String locale) {
		return getDPB0123Service().queryInvoke(HttpMethod.GET.name(), userName, codeChallenge, 
				mockId, apiType, userIp, locale);
	}
	
	private Integer getNMinutes(String mockId) {
		String nMinutes = getTsmpDpItemsParam3("MOCK_CONFIG", mockId);
		return Integer.valueOf(nMinutes);
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
		String secret = null;
		String userBlock = null;
		if(tsmpUser != null) {//有User資料
			// 取得 secret	
			TsmpSsoUserSecret ssoUserSecret = getTsmpSsoUserSecretDao().findFirstByUserName(userName);
			if(ssoUserSecret != null) {
				secret = ssoUserSecret.getSecret();
				userBlock = getUserBlock(userName, secret);
				//驗證加密的密碼
				Optional<Users> opt_users = getUsersDao().findById(userName);
				Users users = null;
				if(opt_users.isPresent()) {
					users = opt_users.get();
					boolean isMach = OAuthUtil.bCryptPasswordCheck(userBlock, users.getPassword());
					if(!isMach) {
						//更新 User 密碼
						updateUserBlock(users, userBlock, userIp, userHostname, txnUid, 1);
					}
				}
				
			}else {// 若查無資料, 則建立secret
				secret = createSsoUserSecret(userName);
				userBlock = getUserBlock(userName, secret);
				//更新 User 密碼
				Optional<Users> opt_users = getUsersDao().findById(userName);
				if(opt_users.isPresent()) {
					Users users = opt_users.get();
					updateUserBlock(users, userBlock, userIp, userHostname, txnUid, 1);
				}
			}
		}else{//查無User資料
			if(flag3_ssoAutoCreateUser) {//自動建立User資料
				//(1). 判斷是否有 User Email
				if(!StringUtils.hasText(userMail) || "null".equalsIgnoreCase(userMail)) {
					this.logger.debug("User Email 不存在");
					throw TsmpDpAaRtnCode._1260.throwing();
				}
				
				//(2). 取得 PWD
				TsmpSsoUserSecret ssoUserSecret = getTsmpSsoUserSecretDao().findFirstByUserName(userName);
				if(ssoUserSecret != null) {
					getTsmpSsoUserSecretDao().delete(ssoUserSecret);
				}
				secret = createSsoUserSecret(userName);
				userBlock = getUserBlock(userName, secret);
				
				//(3). 建立組織	
				String orgId = createOrg(auth);
				
				//(4). 建立角色	
				String roleId = createRole(auth, userIp, userHostname, txnUid);
				
				//(5). 建立 USER 資料
				createUser(auth, userName, userBlock, userMail, roleId, orgId, userIp, userHostname, txnUid);
			}
		}
		
		return userBlock;
	}
 
	protected String getEncodePassword(String password) throws Exception {
		String encodePassword = null;
		encodePassword = OAuthUtil.bCryptEncode(password);
		return encodePassword;
		
	}
	
	/**
	 * 
	 * @param userName 建立 TSMP_SSO_USER_SECRET (SSO的使用者的部份PW) 資料
	 * @param createUser
	 * @return
	 */
	public String createSsoUserSecret(String userName, String createUser) {
		String secret = RandomUtils.randomString(4, true, true);//取得亂數
		
		TsmpSsoUserSecret ssoUserSecret = new TsmpSsoUserSecret();
		ssoUserSecret.setUserName(userName);
		ssoUserSecret.setSecret(secret);
		ssoUserSecret.setCreateDateTime(DateTimeUtil.now());
		ssoUserSecret.setCreateUser(createUser);
		ssoUserSecret = getTsmpSsoUserSecretDao().saveAndFlush(ssoUserSecret);
		return secret;
	}
	
	private Users updateUserBlock(Users users, String userBlock, String userIp, String userHostname,
			String txnUid, int index) throws Exception {
		InnerInvokeParam iip = getDgrAuditLogService().getInnerInvokeParam(tokenApiUrl, "SYS", "SYS", userIp, 
				userHostname, txnUid+"_uUsers" + index, null, null);
		
		//寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_USER.value());
		
		String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, users); //舊資料統一轉成 String
		
		String encodePassword = "";
		if(!StringUtils.isEmpty(userBlock)) {
			encodePassword = getEncodePassword(userBlock);
		}
		
		users.setPassword(encodePassword);
		users = getUsersDao().saveAndFlush(users);
		
		//寫入 Audit Log D
		lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
				Users.class.getSimpleName(), TableAct.U.value(), oldRowStr, users);// U
		
		return users;
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
	
	/**
	 * 取得根組織的 org_id
	 */
	private String getRootOrgId() {
    	String rootOrgId = null;
    	
    	// 取得 Root 組織的 org_id	
    	TsmpOrganization org1 = getTsmpOrganizationDao().findFirstByParentId("");
    	if(org1 != null) {
    		rootOrgId = org1.getOrgId();
    	}else {
    		TsmpOrganization org2 = getTsmpOrganizationDao().findFirstByParentId(null);
    		if(org2 != null) {
    			rootOrgId = org2.getOrgId();
	    	}else {
	    		this.logger.debug("查無根組織的 org id");
	    		throw TsmpDpAaRtnCode._1298.throwing();
	    	}
    	}
    	return rootOrgId;
	}
	
	/**
	 * 判斷是否有 "SSO" 的組織, 若查無資料, 則新增組織資料
	 */
	private String createOrg(TsmpAuthorization auth) {
		String orgId = null;
		String orgName = "SSO";
		
		TsmpOrganization tsmpOrganization = getTsmpOrganizationDao().findByOrgName(orgName);
	    if(tsmpOrganization != null) {
	    	orgId = tsmpOrganization.getOrgId();
	    	
	    }else{//沒有 "SSO" 組織
	    	// 建立 "SSO" 組織
	    	String rootOrgId = getRootOrgId();
	    	AA1001Req req = new AA1001Req();
	    	req.setParentId(rootOrgId);
	    	req.setOrgName("SSO");
	    	req.setOrgCode("SSO");
	    	req.setContactTel("昕力客服");
	    	req.setContactName("+886-2-8751-1610");
	    	req.setContactMail("service@tpisoftware.com");
	    	AA1001Resp resp = getAA1001Service().addTOrg(auth, req);
	    	orgId = resp.getOrgId();
	    }
	    return orgId;
	}
	
	private String getUserBlock(String userName, String secret) {
		String userPwd = userName + secret;
		String userBlock = ServiceUtil.base64Encode(userPwd.getBytes());
		return userBlock;
	}
	
	/**
	 * 判斷是否有 "SSO" 的角色, 若查無資料, 則新增使用者角色
	 */
	private String createRole(TsmpAuthorization auth, String userIp, String userHostname, String txnUid) {
		String roleId = null;
		String roleName = "SSO";
		
		TsmpRole tsmpRole = getTsmpRoleDao().findFirstByRoleName(roleName);
		if(tsmpRole != null) {
			roleId = tsmpRole.getRoleId();
			
		}else{//沒有 "SSO" 角色
			// 建立 "SSO" 角色
			AA0011Req req = new AA0011Req();
			req.setRoleName("SSO");
			req.setRoleAlias("SSO");
			//功能清單:舊角色維護 (AC0012),角色維護(DGL0003)
			req.setFuncCodeList(Arrays.asList("AC0012"));
			
			InnerInvokeParam iip = getDgrAuditLogService().getInnerInvokeParam("/dgrv4/11/AA0011", "SYS", "SYS", userIp, 
					userHostname, txnUid+"_cRole", null, null);
			AA0011Resp resp = getAA0011Service().addTRole(auth, req, iip);
			roleId = resp.getRoleId();
		}
		return roleId;
	}
	
	
	
	/**
	 * 將 Request.userName 經過 Base64 Decode ,判斷最後面是否有 0x80,				
	 * 若有,拿掉 0x80, 得到原 User Name , 即 {userName}				
	 * 若沒有,則 throw 1496。(登入失敗)
	 */
	protected String checkUserNameAndGetUserName(String base64EnUserName) {
		byte[] data1 = null;
		try {
			data1 = ServiceUtil.base64Decode(base64EnUserName);
		} catch (Exception e) {
			throw TsmpDpAaRtnCode._1500.throwing("username");
		}
		boolean flag = data1[data1.length-1] == (byte)0x80;
		if(!flag) {
			this.logger.debug("User Name沒有特殊字");
			throw TsmpDpAaRtnCode._1496.throwing();
		}
		
		byte[] data2 = new byte[data1.length-1];
		System.arraycopy(data1, 0, data2, 0, data1.length-1);//陣列複製:參數分別是來源陣列、來源起始索引、目的陣列、目的起始索引、複製長度
		
		String userName = new String(data2);
		
		return userName;
	}

	protected LocalDateTime getNow() {
		LocalDateTime now = LocalDateTime.now();
		return now;
	}
	
	/**
	 * 建立 USER 資料
	 */
	private void createUser(TsmpAuthorization auth, String userName, String userBlock, String userMail,
			String roleId, String orgId, String userIp, String userHostname, String txnUid) {
		
		String bCryptEncodeQuyType = OAuthUtil.bCryptEncode("1"); //1: 正常
		String base64EncodeQuyType = ServiceUtil.base64Encode(bCryptEncodeQuyType.getBytes());
		base64EncodeQuyType += ",0";
		
		ReqHeader reqHeader = new ReqHeader();
		reqHeader.setLocale( LocaleType.EN_US);
		
		AA0001Req req = new AA0001Req();
		req.setUserName(userName);
		req.setUserAlias(userName);
		req.setUserBlock(userBlock);
		req.setUserMail(userMail);
		req.setRoleIDList(Arrays.asList(roleId));
		req.setOrgID(orgId);
		req.setEncodeStatus(base64EncodeQuyType);
		
		InnerInvokeParam iip = getDgrAuditLogService().getInnerInvokeParam("/11/AA0001", "SYS", "SYS", userIp, 
				userHostname, txnUid+"_cUser", null, null);
		getAA0001Service().addTUser(auth, req, reqHeader, iip);
	}
	
	private void getToken(Map<String, String> parameters, String scheme, HttpServletResponse res, 
			String userIp, String userHostname, String txnUid) throws Exception {
		
		String grantType = parameters.get("grant_type");
		String userName = parameters.get("username");
		
		//若 parameters 的 grant_type 不是 "password" 且 不是 "refreah_token"的, 一律轉成 "password"
		if (!"password".equals(grantType) && !"refresh_token".equals(grantType)) {
			parameters.put("grant_type", "password");
		}
		
		// 檢查 User 狀態
		checkUserInfo(userName, userIp, userHostname, txnUid);
		
		// 取得 AES Key						
		String aesKey = getAesKey();
		this.logger.debug("--TAEASK:" + ServiceUtil.dataMask(aesKey, 2, 2));
		
		// 取得 Client ID/PW
		String clientId = getClientId(aesKey);
		this.logger.debug("--clientId:" + ServiceUtil.dataMask(clientId, 2, 2));
		String clientPw = getClientPw(aesKey);
		this.logger.debug("--clientPw:" + ServiceUtil.dataMask(clientPw, 2, 2));
		
		// 組成 Http Header Authorization 
		String authorization = getAuthorization(clientId, clientPw);
		
		/*
		 * 調用 oauth/token 取得真正的 Token						
		 */
 
		// url，修改為直接從 OAuthTokenService 取 Token。
//		String reqUrl = getBridgeUrl(scheme);
		
		// method
		String method = HttpMethod.POST.toString();
		
		// http header
		Map<String,String> httpHeader = new HashMap<>();
		httpHeader.put("Authorization", authorization);
		
		// form data
		Map<String,String> formData = parameters;
		
		// http send...
//		HttpRespData respObj = HttpUtil.httpReqByFormData(reqUrl, method, formData, httpHeader, true);
//		logger.debug(respObj.getLogStr());
		
		ResponseEntity<OAuthTokenResp> respObj = (ResponseEntity<OAuthTokenResp>) getOAuthTokenService().getToken(formData, authorization, "/oauth/token");
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
		String httpRespStr = new String(httpArray , StandardCharsets.UTF_8);
		logger.debug("\n oauth token result body:\n" + httpRespStr + "\n"); 
		
		String tokenJti = getJti(httpRespStr);
		
		ByteArrayInputStream bi = new ByteArrayInputStream(httpArray);
		IOUtils.copy(bi, res.getOutputStream());
		
		//更新 TSMP_USER
		updateUser(res.getStatus(), userName, userIp, userHostname, txnUid, tokenJti);
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
		String loginState = "";
		
		TsmpUser user = getTsmpUserDao().findFirstByUserName(userName);
		if (user == null) {
			loginState = "FAILED";
			
		}else {
			oldRowStr = getDgrAuditLogService().writeValueAsString(iip, user); //舊資料統一轉成 String
			
			if (statusCode == 200) {
				loginState = "SUCCESS";
				user.setPwdFailTimes(0);
				user.setLogonDate(new Date());
				user.setUpdateTime(new Date());
				user.setUpdateUser(userName);
				
				user = getTsmpUserDao().saveAndFlush(user);
				
			} else if (statusCode == 400 || statusCode == 401) {
				loginState = "FAILED";
				int failTimes = user.getPwdFailTimes();
				failTimes++;
				user.setPwdFailTimes(failTimes);
				user.setUpdateTime(new Date());
				int failThreshold = getFailThreshold();//允許User密碼錯誤次數上限
				if (failTimes >= failThreshold) {
					user.setUserStatus("3");
				}
				
				user = getTsmpUserDao().saveAndFlush(user);
			}
		}
		
		//寫入 Audit Log M,登入成功或失敗
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogMForLogin(lineNumber, eventNo, tokenApiUrl, 
				userName, auditClientId, userIp, userHostname, loginState, statusCode+"", txnUid, tokenJti, null);
		
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
	
	public HttpHeaders buildHeaderVo(HttpServletRequest req) {
		Enumeration<String> headerkeys = req.getHeaderNames();

		HttpHeaders headers = new HttpHeaders();

		while (headerkeys.hasMoreElements()) {
			String headerKey = headerkeys.nextElement();
			Enumeration<String> vals = req.getHeaders(headerKey);
			while (vals.hasMoreElements()) {
				String headerVal = vals.nextElement();
				headers.add(headerKey, headerVal);
			}
		}

		return headers;
	}
	
	private void checkUserInfo(String userName, String userIp, String userHostname, String txnUid) {
		if(!StringUtils.hasText(userName)) {
			return;
		}
		
		TsmpUser user = getTsmpUserDao().findFirstByUserName(userName);
		if (user == null) {
			return;
		}

		int failTimes = user.getPwdFailTimes();
		String userStatus = user.getUserStatus();
		
		String loginState = "FAILED";
		if ("2".equals(userStatus)) {
			//寫入 Audit Log M
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogMForLogin(lineNumber, eventNo, tokenApiUrl, 
					userName, auditClientId, userIp, userHostname, loginState, TsmpDpAaRtnCode._1473.getCode(), txnUid, null, null);
			
			//若 TSMP_USER.user_status (使用者狀態) = "2", 則 throw 1473。 (使用者已停權)
			throw TsmpDpAaRtnCode._1473.throwing();
		}
		if ("3".equals(userStatus)) {
			//寫入 Audit Log M
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogMForLogin(lineNumber, eventNo, tokenApiUrl, 
					userName, auditClientId, userIp, userHostname, loginState, TsmpDpAaRtnCode._1472.getCode(), txnUid, null, null);
			
			//若 TSMP_USER.user_status (使用者狀態) = "3", 則 throw 1472。 (使用者已鎖定)
			throw TsmpDpAaRtnCode._1472.throwing();
		}
		int failThreshold = getFailThreshold();//允許User密碼錯誤次數上限
		if (failTimes >= failThreshold) {
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
		String acClientId = getTsmpSettingService().getVal_TSMP_AC_CLIENT_ID();
		String clientId = "";
		if (!StringUtils.hasText(aesKey)) {
			clientId = acClientId;
		}else {
			clientId = getTsmpTAEASKHelper().decrypt(acClientId);
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
		String acClientPw = getTsmpSettingService().getVal_TSMP_AC_CLIENT_PW();
		String clientPw = "";
		if (!StringUtils.hasText(aesKey)) {
			clientPw = acClientPw;
		}else {
			clientPw = getTsmpTAEASKHelper().decrypt(acClientPw);
		}
		return clientPw;
	}
	
	/**
	 * 取得平台啟動時, 由 shell 輸入的 AES Key	
	 * 註: 需要交付組在啟動v3的shell或bat中設定"TAEASK"的值，這邊才會抓得到
	 */							
	private String getAesKey() {
		String key = System.getenv("TAEASK");
		return key;
	}
	
	/**
	 * 組成 Http Header Authorization
	 */
	private String getAuthorization(String clientId, String clientPw) {
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
	
	protected TsmpSettingDao getTsmpSettingDao() {
		return this.tsmpSettingDao;
	}
	
	protected TsmpSettingService getTsmpSettingService() {
		return this.tsmpSettingService;
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
	
	protected AA1001Service getAA1001Service() {
		return this.aa1001Service;
	}
	
	protected AA0001Service getAA0001Service() {
		return this.aa0001Service;
	}
	
	protected AA0011Service getAA0011Service() {
		return this.aa0011Service;
	}
	
	protected DPB0123Service getDPB0123Service() {
		return this.dpb0123Service;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return tsmpDpItemsCacheProxy;
	}

	protected ApplicationContext getCtx() {
		return this.ctx;
	}

	protected TsmpRtnCodeDao getTsmpRtnCodeDao() {
		return this.tsmpRtnCodeDao;
	}
	
	protected JobHelper getJobHelper() {
		return this.jobHelper;
	}
	
	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}
	
	protected OAuthTokenService getOAuthTokenService() {
		return this.oAuthTokenService;
	}
}