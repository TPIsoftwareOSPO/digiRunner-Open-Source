package tpi.dgrv4.gateway.service;

import java.util.Collections;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.DgrIdPType;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.DgrAuditLogService;
import tpi.dgrv4.entity.entity.DgrAcIdpInfoApi;
import tpi.dgrv4.entity.entity.DgrAcIdpInfoLdap;
import tpi.dgrv4.entity.entity.DgrAcIdpInfoMLdapD;
import tpi.dgrv4.entity.entity.DgrAcIdpInfoMLdapM;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoApiDao;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoLdapDao;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoMLdapDDao;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoMLdapMDao;
import tpi.dgrv4.escape.CheckmarxUtils;
import tpi.dgrv4.gateway.component.AcIdPHelper;
import tpi.dgrv4.gateway.component.IdPApiHelper;
import tpi.dgrv4.gateway.component.IdPApiHelper.ApiUserInfoData;
import tpi.dgrv4.gateway.component.IdPHelper;
import tpi.dgrv4.gateway.component.LdapHelper;
import tpi.dgrv4.gateway.component.LdapHelper.LdapAdAuthData;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.constant.DgrMLdapPolicy;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class AcIdPLoginService {

	private AcIdPHelper acIdPHelper;
	private DgrAuditLogService dgrAuditLogService;
	private TsmpSettingService tsmpSettingService;
	private LdapHelper ldapHelper;
	private DgrAcIdpInfoLdapDao dgrAcIdpInfoLdapDao;
	private DgrAcIdpInfoMLdapMDao dgrAcIdpInfoMLdapMDao;
	private DgrAcIdpInfoMLdapDDao dgrAcIdpInfoMLdapDDao;
	private DgrAcIdpInfoApiDao dgrAcIdpInfoApiDao;
	private TokenHelper tokenHelper;
	private IdPApiHelper idPApiHelper;
	
	
	@Autowired
	public AcIdPLoginService(AcIdPHelper acIdPHelper, DgrAuditLogService dgrAuditLogService,
			TsmpSettingService tsmpSettingService, LdapHelper ldapHelper, DgrAcIdpInfoLdapDao dgrAcIdpInfoLdapDao,
			DgrAcIdpInfoMLdapMDao dgrAcIdpInfoMLdapMDao, DgrAcIdpInfoMLdapDDao dgrAcIdpInfoMLdapDDao,
			DgrAcIdpInfoApiDao dgrAcIdpInfoApiDao, TokenHelper tokenHelper, IdPApiHelper idPApiHelper) {
		super();
		this.acIdPHelper = acIdPHelper;
		this.dgrAuditLogService = dgrAuditLogService;
		this.tsmpSettingService = tsmpSettingService;
		this.ldapHelper = ldapHelper;
		this.dgrAcIdpInfoLdapDao = dgrAcIdpInfoLdapDao;
		this.dgrAcIdpInfoMLdapMDao = dgrAcIdpInfoMLdapMDao;
		this.dgrAcIdpInfoMLdapDDao = dgrAcIdpInfoMLdapDDao;
		this.dgrAcIdpInfoApiDao = dgrAcIdpInfoApiDao;
		this.tokenHelper = tokenHelper;
		this.idPApiHelper = idPApiHelper;
	}

	/*
	 * 驗證 User 帳號 & 密碼
	 */
	public void acIdPLogin(HttpHeaders httpHeaders, HttpServletRequest httpReq, HttpServletResponse httpResp,
			String idPType) throws Exception {
		TPILogger.tl.debug("...idPType: " + idPType);
		
		String reqUri = httpReq.getRequestURI();
		String txnUid = getDgrAuditLogService().getTxnUid();
		String userIp = !StringUtils.hasLength(httpHeaders.getFirst("x-forwarded-for")) ? httpReq.getRemoteAddr()
				: httpHeaders.getFirst("x-forwarded-for");
		String userHostname = httpReq.getRemoteHost();
		String userName = httpReq.getParameter("username");
		String userMima = httpReq.getParameter("password");
		String acIdPMsgUrl = null;

		try {
			// 前端AC IdP errMsg顯示訊息的URL
			// 例如. https://localhost:8080/dgrv4/ac4/idpsso/errMsg
			acIdPMsgUrl = getTsmpSettingService().getVal_AC_IDP_MSG_URL();  
			
			// 檢查傳入的資料
			String errMsg = checkReqParam(userName, userMima);
			if (StringUtils.hasLength(errMsg)) {
				// 寫入 Audit Log M,登入失敗
				String lineNumber = StackTraceUtil.getLineNumber();
				String userAlias = null;// 此時還沒有值
				getAcIdPHelper().createAuditLogMForLoginFailed(reqUri, lineNumber, userIp, userHostname, txnUid, errMsg,
						idPType, userName, userAlias);

				// 重新導向到前端,顯示訊息
				getAcIdPHelper().redirectToShowMsg(httpResp, errMsg, acIdPMsgUrl, idPType);
				return;
			}

			if (DgrIdPType.LDAP.equals(idPType) 
					|| DgrIdPType.MLDAP.equals(idPType) 
					|| DgrIdPType.CUS.equals(idPType) 
					|| DgrIdPType.API.equals(idPType)) 
			{
				loginByIdpType(httpReq, httpResp, idPType, userName, userMima, acIdPMsgUrl, reqUri, userIp,
						userHostname, txnUid);
				return;
				
			} else {
				// 沒有支援此 IdP type
				errMsg = String.format(IdPHelper.MSG_UNSUPPORTED_IDP_TYPE, idPType);
				TPILogger.tl.debug(errMsg);
				if (StringUtils.hasLength(errMsg)) {
					// 寫入 Audit Log M,登入失敗
					String lineNumber = StackTraceUtil.getLineNumber();
					String userAlias = null;// 此時還沒有值
					getAcIdPHelper().createAuditLogMForLoginFailed(reqUri, lineNumber, userIp, userHostname, txnUid,
							errMsg, idPType, userName, userAlias);

					// 重新導向到前端,顯示訊息
					getAcIdPHelper().redirectToShowMsg(httpResp, errMsg, acIdPMsgUrl, idPType);
					return;
				}
			}

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));

			// 重新導向到前端,顯示訊息
			String errMsg = "System error";
			TPILogger.tl.error(errMsg);
			getAcIdPHelper().redirectToShowMsg(httpResp, errMsg, acIdPMsgUrl, idPType);
			return;
		}
	}
	
	/**
	 * 1.以 "LDAP" 登入: 只有一台 LDAP 驗證 <br>
	 * 2.或以 "MLDAP" 登入: 多台 LDAP 驗證 <br>
	 * 3.或以 "API" 登入: 調用 API 驗證 <br>
	 */
	private void loginByIdpType(HttpServletRequest httpReq, HttpServletResponse httpResp, String idPType,
			String userName, String userMima, String acIdPMsgUrl, String reqUri, String userIp, String userHostname,
			String txnUid) throws Exception {
		String errMsg = null;
		String userAlias = null;
		String userEmail = null;
		String apiResp = null;
		
		// 1.檢查 User 帳號 & 密碼是否正確
		LdapAdAuthData ldapAdAuthData = null;
		if (DgrIdPType.LDAP.equals(idPType)) {// 只有一台 LDAP 驗證
			ldapAdAuthData = checkAuthByLdap(httpReq, httpResp, idPType, userName, userMima, acIdPMsgUrl, reqUri,
					userIp, userHostname, txnUid);
			errMsg = ldapAdAuthData.errMsg;
			userAlias = ldapAdAuthData.name;
			userEmail = ldapAdAuthData.mail;
			
		} else if (DgrIdPType.MLDAP.equals(idPType)) {// 多台 LDAP 驗證
			ldapAdAuthData = checkAuthByMLdap(httpReq, httpResp, idPType, userName, userMima, acIdPMsgUrl, reqUri,
					userIp, userHostname, txnUid);
			errMsg = ldapAdAuthData.errMsg;
			userAlias = ldapAdAuthData.name;
			userEmail = ldapAdAuthData.mail;
			
		} else if (DgrIdPType.API.equals(idPType)) {// 調用 API 驗證
			ApiUserInfoData apiUserInfoData = null;
			apiUserInfoData = checkAuthByApi(httpReq, httpResp, idPType, userName, userMima, userIp);
			errMsg = apiUserInfoData.errMsg;
			userAlias = apiUserInfoData.userName;
			userEmail = apiUserInfoData.userEmail;
			apiResp = apiUserInfoData.apiResp;
			
		} else {
			ldapAdAuthData = new LdapAdAuthData();
			// 沒有支援此 IdP type
			errMsg = String.format(IdPHelper.MSG_UNSUPPORTED_IDP_TYPE, idPType);
			TPILogger.tl.debug(errMsg);
		}
		
		if (StringUtils.hasLength(errMsg)) {
			// 寫入 Audit Log M,登入失敗
			String lineNumber = StackTraceUtil.getLineNumber();
			getAcIdPHelper().createAuditLogMForLoginFailed(reqUri, lineNumber, userIp, userHostname, txnUid, errMsg,
					idPType, userName, userAlias);
			
			// 重新導向到前端,顯示訊息
			getAcIdPHelper().redirectToShowMsg(httpResp, errMsg, acIdPMsgUrl, idPType);
			return;
		}

		// 2.依 User 狀態,寄信通知審核者 或 建立 dgRcode 重新導向到前端,以登入AC
		String idTokenJwtstr = null;
		String accessTokenJwtstr = null;
		String refreshTokenJwtstr = null;
		
		//checkmarx, Frameable Login Page, 已通過中風險
		httpResp.setHeader("X-Frame-Options", "sameorigin");
		//checkmarx, Missing HSTS Header
		httpResp.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload"); 
        
		
		getAcIdPHelper().sendMailOrCreateDgRcode(httpReq, httpResp, idPType, userName, userAlias, userEmail,
				idTokenJwtstr, accessTokenJwtstr, refreshTokenJwtstr, reqUri, userIp, userHostname, txnUid,
				acIdPMsgUrl, apiResp);
	}

	/**
	 * 以 "LDAP" 登入 <br>
	 * 只有一台 LDAP 驗證
	 */
	private LdapAdAuthData checkAuthByLdap(HttpServletRequest httpReq, HttpServletResponse httpResp, String idPType,
			String reqUserName, String userMima, String acIdPMsgUrl, String reqUri, String userIp, String userHostname,
			String txnUid) throws Exception {

		LdapAdAuthData ldapAdAuthData = new LdapAdAuthData();
		
		// 1.取得 LDAP IdP info 資訊
		// 取得狀態為 "Y",且建立時間最新的
		DgrAcIdpInfoLdap dgrAcIdpInfoLdap = getDgrAcIdpInfoLdapDao().findFirstByLdapStatusOrderByCreateDateTimeDesc("Y");
		if (dgrAcIdpInfoLdap == null) {
			// Table [DGR_AC_IDP_INFO_LDAP] 查不到資料
			TPILogger.tl.error("Table [DGR_AC_IDP_INFO_LDAP] can't find data");
			
			// 設定檔缺少參數 '%s'
			String errMsg = String.format(AcIdPHelper.MSG_THE_PROFILE_IS_MISSING_PARAMETERS, "AC LDAP IdP info");
			TPILogger.tl.error(errMsg);
			ldapAdAuthData.errMsg = errMsg;
			return ldapAdAuthData;
		}

		String ldapUrl = dgrAcIdpInfoLdap.getLdapUrl();// LDAP登入的URL
		String ldapDn = dgrAcIdpInfoLdap.getLdapDn();// LDAP登入的使用者DN
		String ldapBaseDn = dgrAcIdpInfoLdap.getLdapBaseDn();// LDAP基礎DN
		int ldapTimeout = dgrAcIdpInfoLdap.getLdapTimeout();// LDAP登入的連線timeout,單位毫秒
 
		// 2.檢查 LDAP User 帳號 & 密碼是否正確
		// 3.取得 user data
		boolean isGetUserInfo = true;
		ldapAdAuthData = getLdapHelper().checkLdapAuth(ldapUrl, ldapDn, ldapBaseDn, ldapTimeout,
				reqUserName, userMima, isGetUserInfo, null);
		
		return ldapAdAuthData;
	}
	
	/**
	 * 以 "MLDAP" 登入 <br>
	 * 多台 LDAP 驗證
	 */
	private LdapAdAuthData checkAuthByMLdap(HttpServletRequest httpReq, HttpServletResponse httpResp, String idPType,
			String reqUserName, String userMima, String acIdPMsgUrl, String reqUri, String userIp, String userHostname,
			String txnUid) throws Exception {
		
		LdapAdAuthData ldapAdAuthData = new LdapAdAuthData();
		
		// 1.取得 MLDAP IdP info 主檔
		// 狀態為"Y",且建立時間最新的
		DgrAcIdpInfoMLdapM dgrAcIdpInfoMLdapM = getDgrAcIdpInfoMLdapMDao()
				.findFirstByStatusOrderByCreateDateTimeDesc("Y");
		if (dgrAcIdpInfoMLdapM == null) {
			// Table [DGR_AC_IDP_INFO_MLDAP_M] 查不到資料
			TPILogger.tl.error("Table [DGR_AC_IDP_INFO_MLDAP_M] can't find data");
			// 設定檔缺少參數 '%s'
			String errMsg = String.format(AcIdPHelper.MSG_THE_PROFILE_IS_MISSING_PARAMETERS, "AC MLDAP IdP info master");
			TPILogger.tl.error(errMsg);
			ldapAdAuthData.errMsg = errMsg;
			return ldapAdAuthData;
		}
		
		int ldapTimeout = dgrAcIdpInfoMLdapM.getLdapTimeout();// LDAP登入的連線timeout,單位毫秒
		String policy = dgrAcIdpInfoMLdapM.getPolicy();
		long acIdpInfoMLdapMId = dgrAcIdpInfoMLdapM.getAcIdpInfoMLdapMId();
		String msg = String.format("...policy: %s(%s)", policy, DgrMLdapPolicy.getText(policy));
		TPILogger.tl.debug(msg);
		
		// 2.取得 MLDAP IdP info 明細檔
		// 先依順序取出資料
		List<DgrAcIdpInfoMLdapD> dgrAcIdpInfoMLdapDList = getDgrAcIdpInfoMLdapDDao()
				.findAllByRefAcIdpInfoMLdapMIdOrderByOrderNoAsc(acIdpInfoMLdapMId);

		// 若為隨機驗證,則打亂順序
		if (DgrMLdapPolicy.RANDOM.isValueEquals(policy)) {// 若為隨機
			Collections.shuffle(dgrAcIdpInfoMLdapDList);//打亂順序
		}

		if (CollectionUtils.isEmpty(dgrAcIdpInfoMLdapDList)) {
			// Table [DGR_AC_IDP_INFO_MLDAP_D] 查不到資料
			TPILogger.tl.error("Table [DGR_AC_IDP_INFO_MLDAP_D] can't find data");
			// 設定檔缺少參數 '%s'
			String errMsg = String.format(AcIdPHelper.MSG_THE_PROFILE_IS_MISSING_PARAMETERS, "AC MLDAP IdP info detail");
			TPILogger.tl.error(errMsg);
			ldapAdAuthData.errMsg = errMsg;
			return ldapAdAuthData;
		}
		
		for (DgrAcIdpInfoMLdapD dgrAcIdpInfoMLdapD : dgrAcIdpInfoMLdapDList) {
			int orderNo = dgrAcIdpInfoMLdapD.getOrderNo();
			String ldapUrl = dgrAcIdpInfoMLdapD.getLdapUrl();// LDAP登入的URL
			String ldapDn = dgrAcIdpInfoMLdapD.getLdapDn();// LDAP登入的使用者DN
			String ldapBaseDn = dgrAcIdpInfoMLdapD.getLdapBaseDn();// LDAP基礎DN

			// 3.檢查 LDAP User 帳號 & 密碼是否正確
			// 4.取得 user data
			boolean isGetUserInfo = true;
			ldapAdAuthData = getLdapHelper().checkLdapAuth(ldapUrl, ldapDn, ldapBaseDn, ldapTimeout, reqUserName,
					userMima, isGetUserInfo, orderNo);

			String errMsg = ldapAdAuthData.errMsg;
			if (!StringUtils.hasLength(errMsg)) {// 若沒有錯誤訊息,表示登入成功
				break;// 不用再檢查下去
			}
		}
		
		return ldapAdAuthData;
	}

	/**
	 * 以 "API" 登入 <br>
	 * 調用 API 驗證
	 */
	private ApiUserInfoData checkAuthByApi(HttpServletRequest httpReq, HttpServletResponse httpResp, String idPType,
			 String reqUserName, String userMima, String userIp) throws Exception {
		ApiUserInfoData apiUserInfoData = new ApiUserInfoData();
		String reqUri = httpReq.getRequestURI();

		// 1.取得 dgR client 對應的 API 連線資料
		// 取得狀態為 "Y",且建立時間最新的
		String status = "Y";
		DgrAcIdpInfoApi dgrAcIdpInfoApi = getDgrAcIdpInfoApiDao().findFirstByStatusOrderByCreateDateTimeDesc(status);
		if (dgrAcIdpInfoApi == null) {
			// Table [DGR_GTW_IDP_INFO_A] 查不到資料
			TPILogger.tl.debug("Table [DGR_AC_IDP_INFO_API] can't find data. " + " status: " + status);

			// 設定檔缺少參數 '%s'
			String errMsg = String.format(AcIdPHelper.MSG_THE_PROFILE_IS_MISSING_PARAMETERS, "AC API IdP info");
			TPILogger.tl.debug(errMsg);
			apiUserInfoData.errMsg = errMsg;
			return apiUserInfoData;
		}

		// 2.調用登入API, 檢查 user 帳號 & 密碼是否正確
		// 3.取得 user data
		apiUserInfoData = getIdPApiHelper().callLoginApi(reqUserName, userMima, userIp, dgrAcIdpInfoApi, reqUri);
		return apiUserInfoData;
	}
	
	/**
	 * 檢查傳入的資料
	 */
	private String checkReqParam(String userName, String userMima) {
		String errMsg = null;

		// 沒有 username
		if (!StringUtils.hasLength(userName)) {
			// 缺少必填參數 '%s'
			errMsg = String.format(AcIdPHelper.MSG_MISSING_REQUIRED_PARAMETER, "username");
			TPILogger.tl.debug(errMsg);
			return errMsg;
		}

		// 沒有 password
		if (!StringUtils.hasLength(userMima)) {
			// 缺少必填參數 '%s'
			errMsg = String.format(AcIdPHelper.MSG_MISSING_REQUIRED_PARAMETER, "password");
			TPILogger.tl.debug(errMsg);
			return errMsg;
		}

		return errMsg;
	}
	
	protected IdPApiHelper getIdPApiHelper() {
		return idPApiHelper;
	}
	
	protected TokenHelper getTokenHelper() {
		return tokenHelper;
	}
	
	protected DgrAcIdpInfoApiDao getDgrAcIdpInfoApiDao() {
		return dgrAcIdpInfoApiDao;
	}
	
	protected AcIdPHelper getAcIdPHelper() {
		return acIdPHelper;
	}

	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected LdapHelper getLdapHelper() {
		return ldapHelper;
	}

	protected DgrAcIdpInfoLdapDao getDgrAcIdpInfoLdapDao() {
		return dgrAcIdpInfoLdapDao;
	}
	
	protected DgrAcIdpInfoMLdapMDao getDgrAcIdpInfoMLdapMDao() {
		return dgrAcIdpInfoMLdapMDao;
	}
	
	protected DgrAcIdpInfoMLdapDDao getDgrAcIdpInfoMLdapDDao() {
		return dgrAcIdpInfoMLdapDDao;
	}
}
