package tpi.dgrv4.gateway.component;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.codec.utils.CApiKeyUtils;
import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.codec.utils.UUID64Util;
import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.DgrIdPType;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.constant.TsmpAuthCodeStatus2;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.TsmpMailEventBuilder;
import tpi.dgrv4.dpaa.constant.TsmpDpMailType;
import tpi.dgrv4.dpaa.service.AA0011Service;
import tpi.dgrv4.dpaa.service.DgrAuditLogService;
import tpi.dgrv4.dpaa.service.PrepareMailService;
import tpi.dgrv4.dpaa.service.SsotokenService;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0011Req;
import tpi.dgrv4.dpaa.vo.AA0011Resp;
import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.entity.entity.Authorities;
import tpi.dgrv4.entity.entity.DgrAcIdpAuthCode;
import tpi.dgrv4.entity.entity.DgrAcIdpInfoApi;
import tpi.dgrv4.entity.entity.DgrAcIdpInfoLdap;
import tpi.dgrv4.entity.entity.DgrAcIdpInfoMLdapM;
import tpi.dgrv4.entity.entity.DgrAcIdpUser;
import tpi.dgrv4.entity.entity.TsmpRole;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.repository.AuthoritiesDao;
import tpi.dgrv4.entity.repository.DgrAcIdpAuthCodeDao;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoApiDao;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoLdapDao;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoMLdapMDao;
import tpi.dgrv4.entity.repository.DgrAcIdpUserDao;
import tpi.dgrv4.entity.repository.TsmpRoleDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.escape.MailHelper;
import tpi.dgrv4.gateway.constant.DgrAcIdpUserStatus;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.OAuthTokenService;
import tpi.dgrv4.gateway.service.TsmpSettingService;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

/**
 * AC IdP 流程的共用程式
 * 
 * @author Mini
 */

@Component
public class AcIdPHelper {
	
    private TsmpSettingService tsmpSettingService;
    private DgrAcIdpUserDao dgrAcIdpUserDao;
    private DgrAcIdpAuthCodeDao dgrAcIdpAuthCodeDao;
    private PrepareMailService prepareMailService;
    private MailHelper mailHelper;
	private DgrAuditLogService dgrAuditLogService;
	private OAuthTokenService oAuthTokenService;
    private SsotokenService ssotokenService;
    private TsmpRoleDao tsmpRoleDao;
    private AuthoritiesDao authoritiesDao;
    private AA0011Service aa0011Service;
    private DgrAcIdpInfoLdapDao dgrAcIdpInfoLdapDao;
	private TsmpUserDao tsmpUserDao;
	private DgrAcIdpInfoMLdapMDao dgrAcIdpInfoMLdapMDao;
	private DgrAcIdpInfoApiDao dgrAcIdpInfoApiDao;

	// Audit Log使用
	private String eventNo = AuditLogEvent.LOGIN.value(); 
    
    private String sendTime;
    
    private String mailCreateUser = "SYS(AC IdP)";
    
    // 訊息:User狀態為 '%s' 無法登入,已寄信給審核者,經審核後,將寄發Email通知您
    public static String MSG_WAITING_FOR_REVIEW_MSG = "Delegate AC user status is '%s', cannot log in. "
    		+ "A letter has been sent to the reviewer. After the review, "
    		+ "an email will be sent to notify you.";
    
    public static String MSG_EMAIL_WILL_BE_SENT_TO_NOTIFY_YOU = "Delegate AC user status is '%s', cannot log in. "
    		+ "An email will be sent to notify you.";
 
    // 訊息:Delegate AC User狀態為 '%s' 不能登入
    public static String MSG_DELEGATE_AC_USER_STATUS_CANNOT_LOG_IN = "Delegate AC user status is '%s', cannot log in.";
    
	// 訊息:Delegate AC User '%s' 不存在,不能登入
	public static String MSG_DELEGATE_AC_USER_DOES_NOT_EXIST_CANNOT_LOG_IN = "Delegate AC user '%s' and IdP type '%s' does not exist, cannot log in.";
	
	// 訊息:Delegate AC User '%s'(%s) 不存在,不能登入
	public static String MSG_DELEGATE_AC_USER_DOES_NOT_EXIST_CANNOT_LOG_IN_2 = "Delegate AC user '%s'(%s) and IdP type '%s' does not exist, cannot log in.";

    // 訊息:驗證 ID Token 失敗
    public static String MSG_ID_TOKEN_VERIFICATION_FAILED = "ID token verification failed.";
    
    // 訊息:缺少必填參數 '%s'
    public static String MSG_MISSING_REQUIRED_PARAMETER = "Missing required parameter '%s'.";
    
    // 訊息:設定檔缺少參數 '%s'
    public static String MSG_THE_PROFILE_IS_MISSING_PARAMETERS = "The profile is missing parameters '%s'.";
    
    // 訊息:此 URL 無效或已過期
    public static String MSG_THIS_URL_IS_INVALIDATE_OR_EXPIRED = "This URL is invalidate or expired.";
    
    // 訊息:參數錯誤 '%s'
    public static String MSG_PARAMETER_ERROR = "Parameter error '%s'.";
    
    // Review 審核
    // 訊息:Delegate AC User '%s' 不存在,不能登入
    public static String MSG_DELEGATE_AC_USER_DOES_NOT_EXIST = "Delegate AC user '%s' and IdP type '%s' does not exist.";
    
    // 訊息:使用者狀態為 '%s',已寄發email通知使用者
    public static String MSG_DELEGATE_AC_USER_STATUS_NOTIFY = "Delegate AC user status is '%s', "
    		+ "and an email has been sent to notify the user.";
    
	// 訊息:已寄信給審核者,經審核後,將寄發Email通知您
    public static String MSG_SENT_TO_THE_REVIEWER = "A letter has been sent to the reviewer, "
    		+ "and an email will be sent to notify you after review.";
    
	// Audit log
    // 訊息:User 狀態不正確
    public static String MSG_DELEGATE_AC_USER_STATUS_IS_INCORRECT = "Delegate AC User status '%s' is incorrect.";
	
	@Autowired
	public AcIdPHelper(TsmpSettingService tsmpSettingService, DgrAcIdpUserDao dgrAcIdpUserDao,
			DgrAcIdpAuthCodeDao dgrAcIdpAuthCodeDao, PrepareMailService prepareMailService, MailHelper mailHelper,
			DgrAuditLogService dgrAuditLogService, OAuthTokenService oAuthTokenService, SsotokenService ssotokenService,
			TsmpRoleDao tsmpRoleDao, AuthoritiesDao authoritiesDao, AA0011Service aa0011Service,
			DgrAcIdpInfoLdapDao dgrAcIdpInfoLdapDao, TsmpUserDao tsmpUserDao,
			DgrAcIdpInfoMLdapMDao dgrAcIdpInfoMLdapMDao, DgrAcIdpInfoApiDao dgrAcIdpInfoApiDao) {
		super();
		this.tsmpSettingService = tsmpSettingService;
		this.dgrAcIdpUserDao = dgrAcIdpUserDao;
		this.dgrAcIdpAuthCodeDao = dgrAcIdpAuthCodeDao;
		this.prepareMailService = prepareMailService;
		this.mailHelper = mailHelper;
		this.dgrAuditLogService = dgrAuditLogService;
		this.oAuthTokenService = oAuthTokenService;
		this.ssotokenService = ssotokenService;
		this.tsmpRoleDao = tsmpRoleDao;
		this.authoritiesDao = authoritiesDao;
		this.aa0011Service = aa0011Service;
		this.dgrAcIdpInfoLdapDao = dgrAcIdpInfoLdapDao;
		this.tsmpUserDao = tsmpUserDao;
		this.dgrAcIdpInfoMLdapMDao = dgrAcIdpInfoMLdapMDao;
		this.dgrAcIdpInfoApiDao = dgrAcIdpInfoApiDao;
	}
    
    /**
     * 依 User 狀態,寄信通知審核者 或 建立 dgRcode 重新導向到前端,以登入AC <br>
     * 1.若查無 User 資料, 建立 DGR_AC_IDP_USER,並寄信給審核者,以執行同意/拒絕動作 <br>
     * 2.若 User 存在 & 狀態為 allow <br>
     * (1).產生 dgRcode, 並儲存至 DGR_AC_IDP_AUTH_CODE <br>
     * (2).將 dgRcode(auth code) 重新導向到前端, 以登入AC <br>
     */
	public void sendMailOrCreateDgRcode(HttpServletRequest httpReq, HttpServletResponse httpResp, String idPType, String userName,
			String userAlias, String userEmail, String idTokenJwtstr, String accessTokenJwtstr,
			String refreshTokenJwtstr, String reqUri, String userIp, String userHostname, String txnUid,
			String acIdPMsgUrl, String apiResp) throws Exception {
		
		boolean isReviewAndCreate = true;// 預設要寄發審核信流程及自動建立 User
		
		// 依各別 IdP Type 來決定,是否要寄發審核信流程及自動建立 User
		if (DgrIdPType.GOOGLE.equals(idPType) 
				|| DgrIdPType.MS.equals(idPType)
				|| DgrIdPType.OIDC.equals(idPType)) {
			// AC IdP OAUTH2(GOOGLE / MS / OIDC) 寄發審核信流程 及 自動建立 User 功能是否啟用
			isReviewAndCreate = getTsmpSettingService().getVal_AC_IDP_OAUTH2_REVIEW_ENABLE();
			
		} else if (DgrIdPType.LDAP.equals(idPType) 
				|| DgrIdPType.MLDAP.equals(idPType)) {
			// AC IdP LDAP 寄發審核信流程 及 自動建立 User 功能是否啟用
			isReviewAndCreate = getTsmpSettingService().getVal_AC_IDP_LDAP_REVIEW_ENABLE();

		} else if (DgrIdPType.API.equals(idPType)) {
			// AC IdP API 寄發審核信流程 及 自動建立 User 功能是否啟用
			isReviewAndCreate = getTsmpSettingService().getVal_AC_IDP_API_REVIEW_ENABLE();
			
		} else if (DgrIdPType.CUS.equals(idPType)) {
			isReviewAndCreate = getTsmpSettingService().getVal_AC_IDP_CUS_REVIEW_ENABLE();
		}
		
    		// AC_IDP 登入時, 是否只檢查username, 忽略 IdP type; 預設為 false, 須檢查 username 和 type (true/false) 
		boolean acIdpLoginIgnoreType = getTsmpSettingService().getVal_AC_IDP_LOGIN_IGNORE_TYPE();
		
		// AC_IDP 登入時, username 是否做 base64 編碼 (true/false)(default: false) 
		boolean acIdpUsernameB64Encode = getTsmpSettingService().getVal_AC_IDP_USERNAME_B64_ENCODE();
		if(!DgrIdPType.CUS.equals(idPType) && acIdpUsernameB64Encode) {// CUS 本來就有做編碼,不再處理
			userName = ServiceUtil.encodeBase64URL(userName); // 編碼
		}
		
		DgrAcIdpUser dgrAcIdpUser = null;
		
		if (acIdpLoginIgnoreType) {
			// 只檢查 username, 忽略 IdP type, 以最先建立的user資料為準
			dgrAcIdpUser = getDgrAcIdpUserDao().findFirstByUserNameOrderByCreateDateTime(userName);
			if (dgrAcIdpUser != null) {
				idPType = dgrAcIdpUser.getIdpType();// 若有查到已建立的,則以已建立的為準; 否則,仍以登入的IdP type 為準
			}

		} else {
			// 取得 IdP User 的資料
			dgrAcIdpUser = getDgrAcIdpUserDao().findFirstByUserNameAndIdpType(userName, idPType);
		}
    	
    	
		if (dgrAcIdpUser == null) {
			// 查無 User 資料
			doNoUser(httpReq, httpResp, dgrAcIdpUser, isReviewAndCreate, reqUri, userIp, userHostname, txnUid, idPType,
					userName, userAlias, userEmail, acIdPMsgUrl, idTokenJwtstr, accessTokenJwtstr, refreshTokenJwtstr);
		} else {
			// 已有 User 資料
			doHasUser(httpReq, httpResp, dgrAcIdpUser, isReviewAndCreate, reqUri, userIp, userHostname, txnUid, idPType,
					userName, userAlias, userEmail, acIdPMsgUrl, idTokenJwtstr, accessTokenJwtstr, refreshTokenJwtstr, apiResp);
		}
	}
	
	/**
	 * 尚無 User 資料,檢查 & 判斷是否自動建立
	 */
	private void doNoUser(HttpServletRequest httpReq, HttpServletResponse httpResp, DgrAcIdpUser dgrAcIdpUser,
			boolean isReviewAndCreate, String reqUri, String userIp, String userHostname, String txnUid, String idPType,
			String userName, String userAlias, String userEmail, String acIdPMsgUrl, String idTokenJwtstr,
			String accessTokenJwtstr, String refreshTokenJwtstr) throws Exception {

		// AC_IDP 登入時, username 是否做 base64 編碼 (true/false)(default: false) 
		String acIdpUsernameB64EncodeKey = getTsmpSettingService().getKey_AC_IDP_USERNAME_B64_ENCODE();
		boolean acIdpUsernameB64EncodeValue = getTsmpSettingService().getVal_AC_IDP_USERNAME_B64_ENCODE();
		TPILogger.tl.debug(acIdpUsernameB64EncodeKey + " : " + acIdpUsernameB64EncodeValue);
		
		String errMsg = null;
		String showMsg = null;
		String lineNumber = null;
		String userStatusEn = DgrAcIdpUserStatus.REQUEST.text();
		
		String userNameForDec = userName;		
		if (DgrIdPType.CUS.equalsIgnoreCase(idPType) || acIdpUsernameB64EncodeValue) {
			userNameForDec = ServiceUtil.decodeBase64URL(userName); // CUS 或 有設定做編碼, 才要做解碼
		}
		
		if (isReviewAndCreate) { // 有 寄發審核信流程 及 自動建立 User
			// 檢查 user name 是否重複
			showMsg = checkUserDuplicate(userName, idPType);
			errMsg = showMsg;
			if (showMsg != null) {
				// 寫入 Audit Log M,登入失敗
				lineNumber = StackTraceUtil.getLineNumber();
				createAuditLogMForLoginFailed(reqUri, lineNumber, userIp, userHostname, txnUid, errMsg, idPType, userNameForDec, userAlias);

				// 重新導向到前端,顯示訊息
				TPILogger.tl.debug(showMsg);
				redirectToShowMsg(httpResp, showMsg, acIdPMsgUrl, idPType);
				return;
			}
		}
		
		if (isReviewAndCreate) { // 有 寄發審核信流程 及 自動建立 User
			// 取得審核者 Email 名單
			String reviewerEmails = getTsmpSettingService().getVal_AC_IDP_REVIEWER_MAILLIST();

			// a.建立 DGR_AC_IDP_USER (SSO IdP使用者基本資料)
	        long code1 = getCode();// 產生安全驗證碼1
	        long code2 = getCode();// 產生安全驗證碼2
			String newUserStatus = DgrAcIdpUserStatus.REQUEST.value();// 1:Request
			dgrAcIdpUser = createOrUpdateDgrAcIdpUser(dgrAcIdpUser, userName, userEmail, idPType, newUserStatus, code1,
					code2, userAlias, idTokenJwtstr, accessTokenJwtstr, refreshTokenJwtstr, reqUri, userIp,
					userHostname, txnUid);
			long acIdpUserId = dgrAcIdpUser.getAcIdpUserId();
	    	
			// b.寄信給審核者,以執行同意/拒絕動作
	        sendApplyMail(httpReq, userNameForDec, userEmail, acIdpUserId, idPType, reviewerEmails, code1, code2);

			// User狀態為 'Request' 無法登入,已寄信給審核者,經審核後,將寄發Email通知您
			showMsg = String.format(MSG_WAITING_FOR_REVIEW_MSG, userStatusEn);
			
			// Delegate AC User狀態為 'Request' 不能登入
			errMsg = String.format(MSG_DELEGATE_AC_USER_STATUS_CANNOT_LOG_IN, userStatusEn);
			
			// 寫入 Audit Log M,登入失敗
			lineNumber = StackTraceUtil.getLineNumber();
			createAuditLogMForLoginFailed(reqUri, lineNumber, userIp, userHostname, txnUid, errMsg, idPType, userNameForDec,
					userAlias);

		} else { // 沒有 寄發審核信流程 及 不會自動建立 User
			if(DgrIdPType.CUS.equalsIgnoreCase(idPType) || acIdpUsernameB64EncodeValue) {
				// Delegate AC User '%s'(%s) 不存在,不能登入
				showMsg = String.format(MSG_DELEGATE_AC_USER_DOES_NOT_EXIST_CANNOT_LOG_IN_2, userNameForDec, userName, idPType);
				errMsg = showMsg;
				
			} else {
				// Delegate AC User '%s' 不存在,不能登入
				showMsg = String.format(MSG_DELEGATE_AC_USER_DOES_NOT_EXIST_CANNOT_LOG_IN, userNameForDec, idPType);
				errMsg = showMsg;
			}
			
			// 寫入 Audit Log M,登入失敗
			lineNumber = StackTraceUtil.getLineNumber();
			createAuditLogMForLoginFailed(reqUri, lineNumber, userIp, userHostname, txnUid, errMsg, idPType, userNameForDec,
					userAlias);
		}
		
		// 重新導向到前端,顯示訊息
		TPILogger.tl.debug(showMsg);
		redirectToShowMsg(httpResp, showMsg, acIdPMsgUrl, idPType);
		return;
	}
	
	/**
	 * 已有 User 資料時執行
	 */
	private void doHasUser(HttpServletRequest httpReq, HttpServletResponse httpResp, DgrAcIdpUser dgrAcIdpUser,
			boolean isReviewAndCreate, String reqUri, String userIp, String userHostname, String txnUid,
			String idPType, String userName, String userAlias, String userEmail, String acIdPMsgUrl,
			String idTokenJwtstr, String accessTokenJwtstr, String refreshTokenJwtstr, String apiResp) throws Exception {
		
		// AC_IDP 登入時, username 是否做 base64 編碼 (true/false)(default: false) 
		boolean acIdpUsernameB64Encode = getTsmpSettingService().getVal_AC_IDP_USERNAME_B64_ENCODE();
		
		String errMsg = null;
		String showMsg = null;
		String userStatus = dgrAcIdpUser.getUserStatus();
		Long acIdpUserId = dgrAcIdpUser.getAcIdpUserId();
		
		String userNameForDec = userName;
		if (DgrIdPType.CUS.equalsIgnoreCase(idPType) || acIdpUsernameB64Encode) {
			userNameForDec = ServiceUtil.decodeBase64URL(userName); // CUS 或 有設定做編碼, 才要做解碼
		}
		
		if (DgrAcIdpUserStatus.REQUEST.isValueEquals(userStatus)) {// User 狀態為 Request
			String userStatusEn = DgrAcIdpUserStatus.REQUEST.text();

			if (isReviewAndCreate) {// 有 寄發審核信流程 及 自動建立 User
				// 取得審核者 Email 名單
				String reviewerEmails = getTsmpSettingService().getVal_AC_IDP_REVIEWER_MAILLIST();

				// a.將 code1, code2 更新到 DGR_AC_IDP_USER (SSO IdP使用者基本資料)
				long code1 = getCode();// 產生安全驗證碼1
				long code2 = getCode();// 產生安全驗證碼2
				String newUserStatus = DgrAcIdpUserStatus.REQUEST.value();// 1:Request
				dgrAcIdpUser = createOrUpdateDgrAcIdpUser(dgrAcIdpUser, userName, userEmail, idPType, newUserStatus,
						code1, code2, userAlias, idTokenJwtstr, accessTokenJwtstr, refreshTokenJwtstr, reqUri, userIp,
						userHostname, txnUid);
				acIdpUserId = dgrAcIdpUser.getAcIdpUserId();

				// b.寄信給審核者,以執行同意/拒絕動作
				sendApplyMail(httpReq, userNameForDec, userEmail, acIdpUserId, idPType, reviewerEmails, code1, code2);

				showMsg = String.format(MSG_WAITING_FOR_REVIEW_MSG, userStatusEn);

			} else {// 沒有 寄發審核信流程 及 不會自動建立 User
				showMsg = String.format(MSG_DELEGATE_AC_USER_STATUS_CANNOT_LOG_IN, userStatusEn);
			}
			
			// Delegate AC User狀態為 'Request' 不能登入
			errMsg = String.format(MSG_DELEGATE_AC_USER_STATUS_CANNOT_LOG_IN, userStatusEn);
			
			// 寫入 Audit Log M,登入失敗
			String lineNumber = StackTraceUtil.getLineNumber();
			createAuditLogMForLoginFailed(reqUri, lineNumber, userIp, userHostname, txnUid, errMsg, idPType, userNameForDec,
					userAlias);

			// 重新導向到前端,顯示訊息
			TPILogger.tl.debug(showMsg);
			redirectToShowMsg(httpResp, showMsg, acIdPMsgUrl, idPType);
			return;
			
		} else if (DgrAcIdpUserStatus.ALLOW.isValueEquals(userStatus)) {// User 狀態為 Allow
			// 2.產生 dgRcode, 並儲存至DB
			// 建立 DGR_AC_IDP_AUTH_CODE (SSO IdP授權碼記錄檔)
			DgrAcIdpAuthCode dgrAcIdpAuthCode = createDgrAcIdpAuthCode(userName, idPType, apiResp);
			String dgRcode = dgrAcIdpAuthCode.getAuthCode();

			if (DgrIdPType.GOOGLE.equals(idPType) //
					|| DgrIdPType.MS.equals(idPType) //
					|| DgrIdPType.OIDC.equals(idPType) //
			) {
				// 將 idTokenJwtstr, accessTokenJwtstr, refreshTokenJwtstr 更新到 DGR_AC_IDP_USER
				dgrAcIdpUser = createOrUpdateDgrAcIdpUser(dgrAcIdpUser, userName, userEmail, idPType,
						DgrAcIdpUserStatus.ALLOW.value(), null, null, userAlias, idTokenJwtstr, accessTokenJwtstr,
						refreshTokenJwtstr, reqUri, userIp, userHostname, txnUid);
			}

			// 3.重新導向到前端,以登入AC
			// 前端AC IdP accallback的URL
			String acIdPAccallbackUrl = getTsmpSettingService().getVal_AC_IDP_ACCALLBACK_URL();
			redirectToAcCallback(httpResp, dgRcode, acIdPAccallbackUrl, idPType);
			return;

		} else if (DgrAcIdpUserStatus.DENY.isValueEquals(userStatus)) {// User 狀態為 Deny
			String userStatusEn = DgrAcIdpUserStatus.DENY.text();

			if (isReviewAndCreate) { // 有 寄發審核信流程 及 自動建立 User
				// 寄信給 IdP User,以執行重新申請動作
				sendDenyMail(httpReq, userNameForDec, userEmail, acIdpUserId, idPType);
				showMsg = String.format(MSG_EMAIL_WILL_BE_SENT_TO_NOTIFY_YOU, userStatusEn);

			} else {// 沒有 寄發審核信流程 及 不會自動建立 User
				showMsg = String.format(MSG_DELEGATE_AC_USER_STATUS_CANNOT_LOG_IN, userStatusEn);
			}

			// Delegate AC User狀態為 'Deny' 不能登入
			errMsg = String.format(MSG_DELEGATE_AC_USER_STATUS_CANNOT_LOG_IN, userStatusEn);
			
			// 寫入 Audit Log M,登入失敗
			String lineNumber = StackTraceUtil.getLineNumber();
			createAuditLogMForLoginFailed(reqUri, lineNumber, userIp, userHostname, txnUid, errMsg, idPType, userNameForDec,
					userAlias);

			// 重新導向到前端,顯示訊息
			TPILogger.tl.debug(showMsg);
			redirectToShowMsg(httpResp, showMsg, acIdPMsgUrl, idPType);
			return;

		} else {
			// User 狀態不正確
			errMsg = String.format(MSG_DELEGATE_AC_USER_STATUS_IS_INCORRECT, userStatus);
			TPILogger.tl.debug(errMsg);
			
			// 寫入 Audit Log M,登入失敗
			String lineNumber = StackTraceUtil.getLineNumber();
			createAuditLogMForLoginFailed(reqUri, lineNumber, userIp, userHostname, txnUid, errMsg, idPType, userNameForDec,
					userAlias);

			// 重新導向到前端,顯示訊息
			redirectToShowMsg(httpResp, errMsg, acIdPMsgUrl, idPType);
			return;
		}
	}
	
	/**
	 * 檢查 user name 是否和以下重複 <br>
	 * 1.使用者帳號 <br>
	 * 2.不同 IdP type 的 Delegate AC User <br>
	 */
	private String checkUserDuplicate(String userName, String newIdpType) {
		// AC_IDP 登入時, username 是否做 base64 編碼 (true/false)(default: false) 
		boolean acIdpUsernameB64Encode = getTsmpSettingService().getVal_AC_IDP_USERNAME_B64_ENCODE();
		
		String userNameForDec = userName;
		if (DgrIdPType.CUS.equalsIgnoreCase(newIdpType) || acIdpUsernameB64Encode) {
			userNameForDec = ServiceUtil.decodeBase64URL(userName); // CUS 或 有設定做編碼, 才要做解碼
		}
		
		String errMsg = null;
		
		// 1.檢查 userName 是否與使用者帳號重複
		TsmpUser user = getTsmpUserDao().findFirstByUserName(userName);
		if (user != null) {
			// userName '%s'(IdP type '%s') 與使用者帳號重複,不能登入
			errMsg = String.format(
					"The Delegate AC User '%s' (IdP type '%s') duplicates with the %s User, cannot log in.", userNameForDec,
					newIdpType, IdPHelper.DEFULT_PAGE_TITLE);
			return errMsg;
		}

		// 2.檢查 userName 是否與不同 IdP type 的 Delegate AC User 重複,不能登入
		List<DgrAcIdpUser> dgrAcIdpUserList = getDgrAcIdpUserDao().findByUserName(userName);
		if (!CollectionUtils.isEmpty(dgrAcIdpUserList)) {
			for (DgrAcIdpUser dgrAcIdpUser : dgrAcIdpUserList) {
				String existIdpType = dgrAcIdpUser.getIdpType();
				if (!newIdpType.equals(existIdpType)) {
					// userName '%s'(IdP type '%s') 與 Delegate AC User(IdP type '%s') 重複,不能登入
					errMsg = String.format(
							"The user name '%s'(IdP type '%s') duplicates with the Delegate AC User(IdP type '%s'), cannot log in.",
							userNameForDec, newIdpType, existIdpType);
					return errMsg;
				}
			}
		}
		
		return errMsg;
	}
	
    /**
     * 重新導向到前端,顯示訊息
	 * 1.若 idPType 為 LDAP / MLDAP / API, 則 URL 改成相對路徑, 例如. "/dgrv4/ac4/idpsso/errMsg"
	 * 2.若 idPType 為 GOOGLE / MS / CUS, 則 URL 依 DB 的值為準
     */
    public void redirectToShowMsg(HttpServletResponse httpResp, String msg, String acIdPMsgUrl, String idPType) throws Exception {
		if (DgrIdPType.LDAP.equals(idPType) 
				|| DgrIdPType.MLDAP.equals(idPType) 
				|| DgrIdPType.API.equals(idPType))
		{
			URL urlObj = new URL(acIdPMsgUrl);
			acIdPMsgUrl = urlObj.getPath();// 使用相對路徑
		}

    	String msg_en = Base64Util.base64URLEncode(msg.getBytes());//訊息做 Base64Url Encode
    	String redirect = String.format(
    			"%s"
    			+ "?msg=%s", 
    			acIdPMsgUrl,
    			msg_en);
    	
    	TPILogger.tl.debug("RedirectToShowMsg Url (dgR front-end): " + redirect);
    	httpResp.sendRedirect(redirect);
    }
    
    /**
     * 重新導向到前端,以登入AC
     * 1.若 idPType 為 LDAP / MLDAP / API, 則 URL 改成相對路徑, 例如. "/dgrv4/ac4/idpsso/accallback"
	 * 2.若 idPType 為 GOOGLE / MS / OIDC / CUS, 則 URL 依 DB 的值為準
     */
	public void redirectToAcCallback(HttpServletResponse httpResp, String dgRcode, String acIdPAccallbackUrl,
			String idPType) throws Exception {
		if (DgrIdPType.LDAP.equals(idPType) 
				|| DgrIdPType.MLDAP.equals(idPType) 
				|| DgrIdPType.API.equals(idPType))
		{
			URL urlObj = new URL(acIdPAccallbackUrl);
			acIdPAccallbackUrl = urlObj.getPath();// 使用相對路徑
		}
    	
		String redirect = String.format(
				"%s" 
				+ "?dgRcode=%s", 
				acIdPAccallbackUrl, 
				dgRcode);
		
		TPILogger.tl.debug("Redirect to URL (dgR front-end): " + redirect);
		httpResp.sendRedirect(redirect);
    }
 
    /**
     * 產生安全驗證碼
     */
    public long getCode() {
        long code = RandomSeqLongUtil.getRandomLongByYYYYMMDDHHMMSS();
        return code;
    }
	
    /**
     * 寄信給審核者,以執行同意/拒絕動作
     */
    public void sendApplyMail(HttpServletRequest httpReq, String userName, String userEmail, Long acIdpUserId, String idPType, String reviewerEmails,
            long code1, long code2) throws Exception{
    	
    	// AC IdP Review審核的URL
    	String acIdPReviewUrl = getTsmpSettingService().getVal_AC_IDP_REVIEW_URL();
    				
    	List<TsmpMailEvent> mailEvents = new ArrayList<>();
    	TsmpMailEvent tsmpMailEvent = getApplyMail(userName, userEmail, acIdpUserId, idPType, reviewerEmails, code1,
    			code2, acIdPReviewUrl);
    	mailEvents.add(tsmpMailEvent);
    	
    	String identif = String.format( "userName=%s"
    			+ ",　actType=%s"
    			+ ",　idPType=%s", 
    			userName,
    			"SSOLoginApply",
    			idPType);
    	
    	getPrepareMailService().createMailSchedule(mailEvents, identif, TsmpDpMailType.SAME.text(),
    			getSendTime());
    }
    
    /*
     * 寄信給 IdP User,通知審核結果為 Allow,
     */
	public void sendAllowMail(HttpServletResponse httpResp, String acIdPMsgUrl, String userName, String userEmail,
			String idPType) throws Exception {
        List<TsmpMailEvent> mailEvents = new ArrayList<>();
        TsmpMailEvent tsmpMailEvent = getAllowMail(userName, userEmail, idPType);
        mailEvents.add(tsmpMailEvent);
        
		String identif = String.format( "userName=%s"
				+ ",　actType=%s"
				+ ",　idPType=%s", 
				userName,
				"SSOLoginAllow",
				idPType);
        try {
            getPrepareMailService().createMailSchedule(mailEvents, identif, TsmpDpMailType.SAME.text(),
                    getSendTime());
        } catch (Exception e) {
            TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
        }
    }
    
	/**
	 * 寄信給 IdP User,通知審核結果為 Deny,
	 * 並附上超連結,以執行重新申請動作
	 * @throws MalformedURLException 
	 */
    public void sendDenyMail(HttpServletRequest httpReq, String userName, String userEmail, Long acIdpUserId, String idPType) throws Exception {
    	// AC IdP Review審核的URL
    	String acIdPReviewUrl = getTsmpSettingService().getVal_AC_IDP_REVIEW_URL();
    	
		List<TsmpMailEvent> mailEvents = new ArrayList<>();
		TsmpMailEvent tsmpMailEvent = getDenyMail(idPType, userName, userEmail, acIdpUserId, acIdPReviewUrl);
		mailEvents.add(tsmpMailEvent);
 
		String identif = String.format( "userName=%s"
				+ ",　actType=%s"
				+ ",　idPType=%s", 
				userName,
				"SSOLoginDeny",
				idPType);
		
		try {
			getPrepareMailService().createMailSchedule(mailEvents, identif, TsmpDpMailType.SAME.text(),
					getSendTime());
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
    }
 
    /**
     * 寄給審核者,以執行同意/拒絕動作的信
     */
    private TsmpMailEvent getApplyMail(String userName, String userMail, long acIdpUserId, String idPType,
    		String reviewerEmails, long code1, long code2, String acIdPReviewUrl) throws Exception {
    	String subjTpltCode = "subject.sso-review";
    	String bodyTpltCode = "body.sso-review";
    	
    	// 主旨
    	String subject = getMailHelper().buildNestedContent(subjTpltCode, null);
    	
    	// 內文
    	String allowUrlStr = getAllowUrlStr(userName, idPType, code1, acIdPReviewUrl);
    	String denyUrlStr = getDenyUrlStr(userName, idPType, code2, acIdPReviewUrl);
    	
		String userNameMail = (StringUtils.hasLength(userMail)) ? (userName + "/" + userMail) : userName;
    	Map<String, Object> bodyParams = new HashMap<>();
    	bodyParams.put("userNameMail", userNameMail);
    	bodyParams.put("idPType", idPType);
    	bodyParams.put("allowUrl", allowUrlStr);
    	bodyParams.put("denyUrl", denyUrlStr);
    	bodyParams.put("applyTime", DateTimeUtil.dateTimeToString(new Date(), DateTimeFormatEnum.西元年月日時分).orElse(null));
    	String content = getMailHelper().buildNestedContent(bodyTpltCode, bodyParams);
    	
    	// 收件者
		String recipients = reviewerEmails;// 審核者Email清單,多筆以","分隔
    	
    	return new TsmpMailEventBuilder() //
    			.setSubject(subject)
    			.setContent(content)
    			.setRecipients(recipients)
    			.setCreateUser(mailCreateUser)
    			.setRefCode(bodyTpltCode)
    			.build();
    }
    
    /**
     * 寄給 IdP User, 通知審核結果為 Allow 的信
     */
	private TsmpMailEvent getAllowMail(String userName, String userEmail, String idPType) throws Exception {
        String subjTpltCode = "subject.sso-allow";
        String bodyTpltCode = "body.sso-allow";
        
        // 主旨
        String subject = getMailHelper().buildNestedContent(subjTpltCode, null);
        
        // 內文
        String content = getMailHelper().buildNestedContent(bodyTpltCode, null);
		
		boolean isQueryMail = false;
		if (DgrIdPType.LDAP.equals(idPType) 
				|| DgrIdPType.MLDAP.equals(idPType) 
				|| DgrIdPType.API.equals(idPType)) {
			isQueryMail = true;
		}
		
		// 收件者
		String recipients = null;
		if (isQueryMail && !StringUtils.hasLength(userEmail)) {
			// 當 idPType 為 LDAP / MLDAP / API,若 IdP User 沒有email
			// 收件人為 審核結果收件人
			recipients = getApprovalResultMails(idPType);

		} else {
			// 收件人為 user
			recipients = userEmail; 
		}
        
        return new TsmpMailEventBuilder() //
                .setSubject(subject)
                .setContent(content)
                .setRecipients(recipients)
                .setCreateUser(mailCreateUser)
                .setRefCode(bodyTpltCode)
                .build();
    }
	
	/**
	 * for LDAP / MLDAP / API, <br>
	 * 取得審核結果收件人名單, <br>
	 * 取得狀態為 "Y",且建立時間最新的 <br>
	 */
	private String getApprovalResultMails(String idPType) {
		String approvalResultMails = null;
		
		if (DgrIdPType.LDAP.equals(idPType)) {
			DgrAcIdpInfoLdap dgrAcIdpInfoLdap = getDgrAcIdpInfoLdapDao()
					.findFirstByLdapStatusOrderByCreateDateTimeDesc("Y");
			if (dgrAcIdpInfoLdap == null) {
				// Table [DGR_AC_IDP_INFO_LDAP] 查不到資料
				TPILogger.tl.error("Table [DGR_AC_IDP_INFO_LDAP] can't find data");
				// 設定檔缺少參數 '%s'
				String errMsg = String.format(AcIdPHelper.MSG_THE_PROFILE_IS_MISSING_PARAMETERS, "AC LDAP IdP info");
				TPILogger.tl.error(errMsg);

			} else {
				approvalResultMails = dgrAcIdpInfoLdap.getApprovalResultMail();
			}

		} else if (DgrIdPType.MLDAP.equals(idPType)) {
			DgrAcIdpInfoMLdapM dgrAcIdpInfoMLdapM = getDgrAcIdpInfoMLdapMDao()
					.findFirstByStatusOrderByCreateDateTimeDesc("Y");
			if (dgrAcIdpInfoMLdapM == null) {
				// Table [DGR_AC_IDP_INFO_MLDAP_M] 查不到資料
				TPILogger.tl.error("Table [DGR_AC_IDP_INFO_MLDAP_M] can't find data");
				// 設定檔缺少參數 '%s'
				String errMsg = String.format(AcIdPHelper.MSG_THE_PROFILE_IS_MISSING_PARAMETERS, "AC MLDAP IdP info");
				TPILogger.tl.error(errMsg);

			} else {
				approvalResultMails = dgrAcIdpInfoMLdapM.getApprovalResultMail();
			}

		} else if (DgrIdPType.API.equals(idPType)) {
			DgrAcIdpInfoApi dgrAcIdpInfoApi = getDgrAcIdpInfoApiDao().findFirstByStatusOrderByCreateDateTimeDesc("Y");
			if (dgrAcIdpInfoApi == null) {
				// Table [DGR_AC_IDP_INFO_API] 查不到資料
				TPILogger.tl.error("Table [DGR_AC_IDP_INFO_API] can't find data");
				// 設定檔缺少參數 '%s'
				String errMsg = String.format(AcIdPHelper.MSG_THE_PROFILE_IS_MISSING_PARAMETERS, "AC API IdP info");
				TPILogger.tl.error(errMsg);

			} else {
				approvalResultMails = dgrAcIdpInfoApi.getApprovalResultMail();
			}
		}
		
		if (!StringUtils.hasLength(approvalResultMails)) {
			// 設定檔缺少參數 '%s'
			String errMsg = String.format(AcIdPHelper.MSG_THE_PROFILE_IS_MISSING_PARAMETERS, "approval_result_mail");
			TPILogger.tl.debug(errMsg);
		}
		
		return approvalResultMails;
	}
 
    /**
     * 寄給 IdP User,通知審核結果為 Deny 的信
     * 並附上超連結,以執行重新申請動作
     */
	public TsmpMailEvent getDenyMail(String idPType, String userName, String userEmail, Long acIdpUserId,
			String acIdPReviewUrl) {
        String subjTpltCode = "subject.sso-deny";
        String bodyTpltCode = "body.sso-deny";
        
        // 主旨
		String subject = getMailHelper().buildNestedContent(subjTpltCode, null);
		
        // 內文
		String reApplyUrlStr = getReApplyUrlStr(acIdpUserId, userName, idPType, acIdPReviewUrl);//執行重新申請的超連結
        Map<String, Object> bodyParams = new HashMap<>();
        bodyParams.put("applyUrl", reApplyUrlStr);
        String content = getMailHelper().buildNestedContent(bodyTpltCode, bodyParams);
        
		boolean isQueryMail = false;
		if (DgrIdPType.LDAP.equals(idPType) 
				|| DgrIdPType.MLDAP.equals(idPType) 
				|| DgrIdPType.API.equals(idPType)) 
		{
			isQueryMail = true;
		}
        
		// 收件者
		String recipients = null;
		if (isQueryMail && !StringUtils.hasLength(userEmail)) {
			// 當 idPType 為 LDAP,若 IdP User 沒有email
			// 收件人為 LDAP 審核結果收件人
			recipients = getApprovalResultMails(idPType);

		} else {
			// 收件人為 user
			recipients = userEmail; 
		}

        return new TsmpMailEventBuilder() //
                .setSubject(subject)
                .setContent(content)
                .setRecipients(recipients)
                .setCreateUser(mailCreateUser)
                .setRefCode(bodyTpltCode)
                .build();
    }
  
    /**
     * 取得同意的超連結
     */
    private String getAllowUrlStr(String userName, String idPType, long code1, String acIdPReviewUrl) {
		String u = String.format("%s.%d", 
				DgrAcIdpUserStatus.ALLOW.value(), // 2:allow
				code1);
		String cApiKey = CApiKeyUtils.signCKey(u);
		String allowUrlStr = null;
		try {
			allowUrlStr = String.format(
					"%s" 
					+ "?userName=%s" 
					+ "&u=%s" 
					+ "&idPType=%s" 
					+ "&cApiKey=%s",
					acIdPReviewUrl, 
					URLEncoder.encode(userName, StandardCharsets.UTF_8.toString()),
					u, 
					idPType,
					cApiKey);
		} catch (UnsupportedEncodingException e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
    	return allowUrlStr;
    }
    
    /**
     * 取得拒絕的超連結
     */
    private String getDenyUrlStr(String userName, String idPType, long code2, String acIdPReviewUrl) {
		String u = String.format("%s.%d", 
				DgrAcIdpUserStatus.DENY.value(), // 3:deny
				code2);
		String cApiKey = CApiKeyUtils.signCKey(u);
		String denyUrlStr = null;
		try {
			denyUrlStr = String.format(
			        "%s"
			        + "?userName=%s"
			        + "&u=%s"
			        + "&idPType=%s"
			        + "&cApiKey=%s",
			        acIdPReviewUrl,
			        URLEncoder.encode(userName, StandardCharsets.UTF_8.toString()),
			        u,
			        idPType,
			        cApiKey);
		} catch (UnsupportedEncodingException e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}

    	return denyUrlStr;
    }
    
    /**
     * 取得重新申請的超連結
     */
    private String getReApplyUrlStr(long acIdpUserId, String userName, String idPType, String acIdPReviewUrl) {
		String u = String.format("%s.%d", 
				DgrAcIdpUserStatus.REQUEST.value(), // 1:request
				acIdpUserId);
		String cApiKey = CApiKeyUtils.signCKey(u);
        String applyUrl = null;
		try {
			applyUrl = String.format(
						"%s" 
						+ "?userName=%s" 
						+ "&u=%s" 
						+ "&idPType=%s" 
						+ "&cApiKey=%s", 
						acIdPReviewUrl, 
						URLEncoder.encode(userName, StandardCharsets.UTF_8.toString()), 
						u, 
						idPType,
						cApiKey);
		} catch (UnsupportedEncodingException e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
		return applyUrl;
    }
    
    /**
     * 取得多久後寄發Email時間
     */
    protected String getSendTime() {
        this.sendTime = this.getTsmpSettingService().getVal_MAIL_SEND_TIME();// 多久後寄發Email(ms)
        return this.sendTime;
    }
	
    /**
     * 建立 或 更新 DGR_AC_IDP_USER (SSO IdP使用者基本資料)
     */
	public DgrAcIdpUser createOrUpdateDgrAcIdpUser(DgrAcIdpUser dgrAcIdpUser, String userName, String userEmail,
			String idPType, String userStatus, Long code1, Long code2, String userAlias, String idTokenJwtstr,
			String accessTokenJwtstr, String refreshTokenJwtstr, String reqUri, String userIp, String userHostname,
			String txnUid) {

		String auditLogEvent = null;
		String tableAct = null;
		String oldRowStr = null;
		InnerInvokeParam iip = null;
		String roleId = null;
		String createUpdateName = "SYS(AC IdP)";
		if(dgrAcIdpUser == null) {// 若為 null, 則為建立
			// 取得 Role ID, 或建立角色 SSO
			roleId = getRoleIdOrCreate(reqUri, userIp, userHostname, txnUid, idPType);
			
			auditLogEvent = AuditLogEvent.ADD_IDP_USER.value();
			tableAct = TableAct.C.value();
			iip = getDgrAuditLogService().getInnerInvokeParam(reqUri, createUpdateName, createUpdateName, userIp, 
					userHostname, txnUid+"_cUser", null, null);
			
			String rootOrgId = getSsotokenService().getRootOrgId();// 取得根組織單位 ID
    		dgrAcIdpUser = new DgrAcIdpUser();
    		dgrAcIdpUser.setOrgId(rootOrgId);
        	dgrAcIdpUser.setCreateUser(createUpdateName);
        	dgrAcIdpUser.setCreateDateTime(DateTimeUtil.now());
        	
    	}else {//更新
    		auditLogEvent = AuditLogEvent.UPDATE_IDP_USER.value();
    		tableAct = TableAct.U.value();
    		iip = getDgrAuditLogService().getInnerInvokeParam(reqUri, createUpdateName, createUpdateName, userIp, 
    				userHostname, txnUid+"_uUser", null, null);
    		oldRowStr = getDgrAuditLogService().writeValueAsString(iip, dgrAcIdpUser); //舊資料統一轉成 String
    		
        	dgrAcIdpUser.setUpdateUser(createUpdateName);
        	dgrAcIdpUser.setUpdateDateTime(DateTimeUtil.now());
    	}
    	
    	dgrAcIdpUser.setUserName(userName);
    	dgrAcIdpUser.setUserAlias(userAlias);
    	dgrAcIdpUser.setUserStatus(userStatus);
    	dgrAcIdpUser.setUserEmail(userEmail);
    	dgrAcIdpUser.setIdpType(idPType);
    	dgrAcIdpUser.setCode1(code1);
    	dgrAcIdpUser.setCode2(code2);
    	dgrAcIdpUser.setIdTokenJwtstr(idTokenJwtstr);
    	dgrAcIdpUser.setAccessTokenJwtstr(accessTokenJwtstr);
    	dgrAcIdpUser.setRefreshTokenJwtstr(refreshTokenJwtstr);

    	getDgrAcIdpUserDao().save(dgrAcIdpUser);
 
		//寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogM(iip, lineNumber, auditLogEvent);
    	
		//寫入 Audit Log D
		lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogD(iip, lineNumber,
				DgrAcIdpUser.class.getSimpleName(), tableAct, oldRowStr, dgrAcIdpUser);// C or U
		
		if(AuditLogEvent.ADD_IDP_USER.value().equals(auditLogEvent) ) {//當建立 IdP User 時
			// 建立 Authorities 資料(使用者和角色的對應)
			Authorities authorities = new Authorities();
			authorities.setUsername(userName);
			authorities.setAuthority(roleId);
			getAuthoritiesDao().saveAndFlush(authorities);

			lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber, Authorities.class.getSimpleName(),
					TableAct.C.value(), null, authorities);// C
		}
    	
    	return dgrAcIdpUser;
    }
 
	/**
	 * 判斷是否有 "SSO" 的角色, 若查無資料, 則新增使用者角色
	 */
	private String getRoleIdOrCreate(String reqUri, String userIp, String userHostname, String txnUid, String idPType) {
		String ssoRoleId = null;
		String roleName = "SSO";
		
		TsmpRole tsmpRole = getTsmpRoleDao().findFirstByRoleName(roleName);
		if(tsmpRole != null) {
			ssoRoleId = tsmpRole.getRoleId();
			
		}else{//沒有 "SSO" 角色
			// 建立 "SSO" 角色
			AA0011Req ssotoken_req = new AA0011Req();
			ssotoken_req.setRoleName("SSO");
			ssotoken_req.setRoleAlias("SSO");
			
			//功能清單:角色維護 (AC0012)
			ssotoken_req.setFuncCodeList(Arrays.asList("AC0012"));
			
			//寫入 Audit Log
			String createUpdateName = "SYS(AC IdP)";
			InnerInvokeParam iip = getDgrAuditLogService().getInnerInvokeParam(reqUri, createUpdateName,
					createUpdateName, userIp, userHostname, txnUid + "_cRole", null, idPType);
			
			TsmpAuthorization auth = new TsmpAuthorization();
			auth.setUserName(createUpdateName);
			AA0011Resp resp = getAA0011Service().addTRole(auth, ssotoken_req, iip);
			ssoRoleId = resp.getRoleId();
		}
		return ssoRoleId;
	} 
	
    /**
     * 建立 DGR_AC_IDP_AUTH_CODE (SSO IdP授權碼記錄檔)
     */
	public DgrAcIdpAuthCode createDgrAcIdpAuthCode(String userName, String idPType, String apiResp) {
		String dgRcode = UUID64Util.UUID64(UUID.randomUUID());//產生 dgRcode(UUID 64位元)
		String codeStatus = TsmpAuthCodeStatus2.AVAILABLE.value();// 可用
		
		Date expiredTime = IdPHelper.getAuthCodeExpiredTime();// 授權碼的到期時間
		Long expireDateTime = expiredTime.getTime();
		
		DgrAcIdpAuthCode dgrAcIdpAuthCode = new DgrAcIdpAuthCode();
		dgrAcIdpAuthCode.setAuthCode(dgRcode);
		dgrAcIdpAuthCode.setExpireDateTime(expireDateTime);
		dgrAcIdpAuthCode.setStatus(codeStatus);
		dgrAcIdpAuthCode.setUserName(userName);
		dgrAcIdpAuthCode.setIdpType(idPType);
		dgrAcIdpAuthCode.setApiResp(apiResp);
		dgrAcIdpAuthCode.setCreateDateTime(DateTimeUtil.now());
		dgrAcIdpAuthCode.setCreateUser("SYSTEM");
		dgrAcIdpAuthCode = getDgrAcIdpAuthCodeDao().saveAndFlush(dgrAcIdpAuthCode);
		return dgrAcIdpAuthCode;
	}
	
    /**
     * 寫入 Audit Log M,登入失敗
     */
	public void createAuditLogMForLoginFailed(String reqUri, String lineNumber, String userIp, String userHostname, String txnUid,
			String errMsg, String idPType, String userName, String userAlias) {
		//寫入 Audit Log M,登入失敗
    	String token_loginState = "FAILED";
		String auditClientId = "N/A";
		
		String userName_b64 = OAuthTokenService.getUserName_b64(userName, userAlias, idPType);
		
		getDgrAuditLogService().createAuditLogMForLogin(lineNumber, eventNo, reqUri, userName_b64, auditClientId,
				userIp, userHostname, token_loginState, errMsg, txnUid, null, idPType);
    }
    
    protected TsmpSettingService getTsmpSettingService() {
    		return tsmpSettingService;
    }
    
    protected DgrAcIdpUserDao getDgrAcIdpUserDao() {
        return dgrAcIdpUserDao;
    }
    
	protected DgrAcIdpAuthCodeDao getDgrAcIdpAuthCodeDao() {
		return dgrAcIdpAuthCodeDao;
	}

	protected PrepareMailService getPrepareMailService() {
		return prepareMailService;
	}

	protected MailHelper getMailHelper() {
		return mailHelper;
	}

	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}

	protected OAuthTokenService getOAuthTokenService() {
		return oAuthTokenService;
	}

	protected SsotokenService getSsotokenService() {
		return ssotokenService;
	}

	protected TsmpRoleDao getTsmpRoleDao() {
		return tsmpRoleDao;
	}

	protected AuthoritiesDao getAuthoritiesDao() {
		return authoritiesDao;
	}
    
    protected AA0011Service getAA0011Service() {
    	return aa0011Service;
    }
    
	protected DgrAcIdpInfoLdapDao getDgrAcIdpInfoLdapDao() {
		return dgrAcIdpInfoLdapDao;
	}
	
	protected TsmpUserDao getTsmpUserDao() {
		return tsmpUserDao;
	}
	
	protected DgrAcIdpInfoMLdapMDao getDgrAcIdpInfoMLdapMDao() {
		return dgrAcIdpInfoMLdapMDao;
	}
protected DgrAcIdpInfoApiDao getDgrAcIdpInfoApiDao() {
		return dgrAcIdpInfoApiDao;
	}
}
