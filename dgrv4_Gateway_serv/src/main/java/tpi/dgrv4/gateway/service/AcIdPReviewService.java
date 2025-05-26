package tpi.dgrv4.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.codec.utils.CApiKeyUtils;
import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.DgrAuditLogService;
import tpi.dgrv4.entity.entity.DgrAcIdpUser;
import tpi.dgrv4.entity.repository.DgrAcIdpUserDao;
import tpi.dgrv4.escape.CheckmarxUtils;
import tpi.dgrv4.gateway.component.AcIdPHelper;
import tpi.dgrv4.gateway.constant.DgrAcIdpUserStatus;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;

@Service
public class AcIdPReviewService {
 
    private TsmpSettingService tsmpSettingService;
    private DgrAcIdpUserDao dgrAcIdpUserDao;
    private AcIdPHelper acIdPHelper;
    private DgrAuditLogService dgrAuditLogService;
 
    @Autowired
	public AcIdPReviewService(TsmpSettingService tsmpSettingService, DgrAcIdpUserDao dgrAcIdpUserDao,
			AcIdPHelper acIdPHelper, DgrAuditLogService dgrAuditLogService) {
		super();
		this.tsmpSettingService = tsmpSettingService;
		this.dgrAcIdpUserDao = dgrAcIdpUserDao;
		this.acIdPHelper = acIdPHelper;
		this.dgrAuditLogService = dgrAuditLogService;
	}

	public void acIdPReview(HttpHeaders httpHeaders, HttpServletRequest httpReq, HttpServletResponse httpResp)
			throws Exception {
 
		String reqUri = httpReq.getRequestURI();
		String txnUid = getDgrAuditLogService().getTxnUid();
		String userIp = !StringUtils.hasLength(httpHeaders.getFirst("x-forwarded-for")) ? httpReq.getRemoteAddr()
				: httpHeaders.getFirst("x-forwarded-for");
		String userHostname = httpReq.getRemoteHost();
		String userName = httpReq.getParameter("userName");
		String u = httpReq.getParameter("u");
		String idPType = httpReq.getParameter("idPType");
		String cApiKey = httpReq.getParameter("cApiKey");
		
		String acIdPMsgUrl = null;
		try {
			// 前端AC IdP errMsg顯示訊息的URL
			acIdPMsgUrl = getTsmpSettingService().getVal_AC_IDP_MSG_URL();
			
			// 檢查傳入的資料
			String errMsg = checkReqParam(userName, u, idPType, cApiKey);
			if(StringUtils.hasLength(errMsg)) {
				// 重新導向到前端,顯示訊息
				getAcIdPHelper().redirectToShowMsg(httpResp, errMsg, acIdPMsgUrl, idPType);
				return;
			}

			acIdPReview(httpHeaders, userName, u, idPType, cApiKey, httpReq, httpResp, acIdPMsgUrl, reqUri, userIp,
					userHostname, txnUid);

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));

			// 重新導向到前端,顯示訊息
			String errMsg = "System error";
			TPILogger.tl.error(errMsg);
			getAcIdPHelper().redirectToShowMsg(httpResp, errMsg, acIdPMsgUrl, idPType);
			return;
		}
	}

	public void acIdPReview(HttpHeaders httpHeaders, String userName, String u, String idPType, String cApiKey,
			HttpServletRequest httpReq, HttpServletResponse httpResp, String acIdPMsgUrl, String reqUri, String userIp,
			String userHostname, String txnUid) throws Exception {

		String[] arr = u.split("\\.");
		String newUserStatus = arr[0];
		String reqCode = arr[1];

		long reqCodeLong = 0;
		try {
			reqCodeLong = Long.valueOf(reqCode);
		} catch (Exception e) {
			// 參數錯誤 '%s'
    		String errMsg = String.format(AcIdPHelper.MSG_PARAMETER_ERROR, "u的第2個數字");
    		TPILogger.tl.debug(errMsg);
    		
    		// 重新導向到前端,顯示訊息
    		getAcIdPHelper().redirectToShowMsg(httpResp, errMsg, acIdPMsgUrl, idPType);
    		return;
		}
		
		// 取得審核者 Email 名單
		String reviewerEmails = getTsmpSettingService().getVal_AC_IDP_REVIEWER_MAILLIST();
		DgrAcIdpUser dgrAcIdpUser = getDgrAcIdpUserDao().findFirstByUserNameAndIdpType(userName, idPType);
		if (dgrAcIdpUser == null) {
			// 使用者不存在
			String errMsg = String.format(AcIdPHelper.MSG_DELEGATE_AC_USER_DOES_NOT_EXIST, userName);
			TPILogger.tl.debug(errMsg);

			// 重新導向到前端,顯示訊息
			getAcIdPHelper().redirectToShowMsg(httpResp, errMsg, acIdPMsgUrl, idPType);
			return;
		}
        
		String userEmail = dgrAcIdpUser.getUserEmail();
		String userAlias = dgrAcIdpUser.getUserAlias();
		long acIdpUserId = (dgrAcIdpUser.getAcIdpUserId()) == null ? 0L : dgrAcIdpUser.getAcIdpUserId();

		if (DgrAcIdpUserStatus.REQUEST.isValueEquals(newUserStatus)) { // "1": request, AC IdP User 在信件按下"重新申請"
			String updateName = "SYS(AC IdP)";
	        InnerInvokeParam iip = getDgrAuditLogService().getInnerInvokeParam(reqUri, updateName, "N/A", userIp, 
					userHostname, txnUid, null, null);
			doRequest(httpHeaders, httpReq, httpResp, dgrAcIdpUser, acIdPMsgUrl, reqCodeLong, acIdpUserId, userName,
					userEmail, idPType, reviewerEmails, userAlias, updateName, iip);
			return;

		} else if (DgrAcIdpUserStatus.ALLOW.isValueEquals(newUserStatus)) { // "2": allow, 審核者在信件按下"同意"
			String updateUserName = "AC IdP Reviewer";
	        InnerInvokeParam iip = getDgrAuditLogService().getInnerInvokeParam(reqUri, updateUserName, "N/A", userIp, 
					userHostname, txnUid, null, null);
			doAllow(httpResp, dgrAcIdpUser, acIdPMsgUrl, reqCodeLong, userName, userEmail, idPType, updateUserName,
					iip);
			return;

		} else if (DgrAcIdpUserStatus.DENY.isValueEquals(newUserStatus)) { // "3" : deny, 審核者在信件按下"拒絕"
			String updateUserName = "AC IdP Reviewer";
	        InnerInvokeParam iip = getDgrAuditLogService().getInnerInvokeParam(reqUri, updateUserName, "N/A", userIp, 
					userHostname, txnUid, null, null);
			doDeny(httpReq, httpResp, dgrAcIdpUser, acIdPMsgUrl, reqCodeLong, acIdpUserId, userName, userEmail, idPType,
					updateUserName, iip);
			return;
		}
    }
	
	/**
	 * "1": request, IdP User 在信件按下"重新申請"
	 */
	private void doRequest(HttpHeaders httpHeaders, HttpServletRequest httpReq, HttpServletResponse httpResp,
			DgrAcIdpUser dgrAcIdpUser, String acIdPMsgUrl, long reqCodeLong, long acIdpUserId, String userName,
			String userEmail, String idPType, String reviewerEmails, String userAlias, String updateUserName,
			InnerInvokeParam iip) throws Exception {

		// 1.檢查 URL 無效或已過期
    	if(reqCodeLong != acIdpUserId) {
    		TPILogger.tl.debug("reqCodeLong("+ reqCodeLong +") and acIdpUserId(" + acIdpUserId + ") are not the same");
    		
    		// 此 URL 無效或已過期
    		String errMsg = AcIdPHelper.MSG_THIS_URL_IS_INVALIDATE_OR_EXPIRED;
    		TPILogger.tl.debug(errMsg);
    		
    		// 重新導向到前端,顯示訊息
    		getAcIdPHelper().redirectToShowMsg(httpResp, errMsg, acIdPMsgUrl, idPType);
    		return;
    	}
    	
		// 2.產生新的 code1、code2, 更新到 DGR_AC_IDP_USER
		// 寄信給審核者,以執行同意/拒絕動作
        long code1 = getAcIdPHelper().getCode();// 產生安全驗證碼1
        long code2 = getAcIdPHelper().getCode();// 產生安全驗證碼2
        
    	// a.更新 DGR_AC_IDP_USER (SSO IdP使用者基本資料)
		String newUserStatus = DgrAcIdpUserStatus.REQUEST.value();// 1:Request
		dgrAcIdpUser = updateDgrAcIdpUser(dgrAcIdpUser, newUserStatus, code1, code2, updateUserName, iip);
		acIdpUserId = dgrAcIdpUser.getAcIdpUserId();
    	
    	// b.寄信給審核者,以執行同意/拒絕動作
		getAcIdPHelper().sendApplyMail(httpReq, userName, userEmail, acIdpUserId, idPType, reviewerEmails, code1, code2);
		
		// 3.重新導向到前端,顯示訊息
    	// 已寄信給審核者,經審核後,將寄發Email通知您
		String errMsg = AcIdPHelper.MSG_SENT_TO_THE_REVIEWER;
		TPILogger.tl.debug(errMsg);
		getAcIdPHelper().redirectToShowMsg(httpResp, errMsg, acIdPMsgUrl, idPType);
		return;
	}
 
	/**
	 * "2": allow, 審核者在信件按下"同意"
	 */
	private void doAllow(HttpServletResponse httpResp, DgrAcIdpUser dgrAcIdpUser, String acIdPMsgUrl, long reqCodeLong,
			String userName, String userEmail, String idPType, String updateUserName, InnerInvokeParam iip)
			throws Exception {

		// 1.檢查 URL 無效或已過期
		Long code1 = (dgrAcIdpUser.getCode1()) == null ? 0L : dgrAcIdpUser.getCode1();
    	if(reqCodeLong != code1) {
    		TPILogger.tl.debug("reqCodeLong("+ reqCodeLong +") and code1(" + code1 + ") are not the same");
    		
    		// 此 URL 無效或已過期
    		String errMsg = AcIdPHelper.MSG_THIS_URL_IS_INVALIDATE_OR_EXPIRED;
    		TPILogger.tl.debug(errMsg);
    		
    		// 重新導向到前端,顯示訊息
    		getAcIdPHelper().redirectToShowMsg(httpResp, errMsg, acIdPMsgUrl, idPType);
    		return;
    	}

		// 2.更新 DGR_AC_IDP_USER 資料
    	// User 狀態改為 2:Allow & 清空 code1、code2
    	code1 = null;
    	Long code2 = null;
    	updateDgrAcIdpUser(dgrAcIdpUser, DgrAcIdpUserStatus.ALLOW.value(), code1, code2, updateUserName, iip);

		// 3.寄信給 IdP User,通知審核結果為 Allow
    	getAcIdPHelper().sendAllowMail(httpResp, acIdPMsgUrl, userName, userEmail, idPType);
    	
    	// 4.重新導向到前端,顯示訊息
    	// 使用者狀態設定為 Allow,已寄發email通知使用者
		String errMsg = String.format(AcIdPHelper.MSG_DELEGATE_AC_USER_STATUS_NOTIFY, DgrAcIdpUserStatus.ALLOW.text());
		TPILogger.tl.debug(errMsg);
		getAcIdPHelper().redirectToShowMsg(httpResp, errMsg, acIdPMsgUrl, idPType);
		return;
	}
	
	/**
	 * "3": deny, 審核者在信件按下"拒絕"
	 */
	private void doDeny(HttpServletRequest httpReq, HttpServletResponse httpResp, DgrAcIdpUser dgrAcIdpUser,
			String acIdPMsgUrl, long reqCodeLong, long acIdpUserId, String userName, String userEmail, String idPType,
			String updateUserName, InnerInvokeParam iip) throws Exception {
		
		// 1.檢查 URL 無效或已過期
		Long code2 = (dgrAcIdpUser.getCode2()) == null ? 0L : dgrAcIdpUser.getCode2();
    	if(reqCodeLong != code2) {
    		TPILogger.tl.debug("reqCodeLong("+ reqCodeLong +") and code2(" + code2 + ") are not the same");
    		
    		// 此 URL 無效或已過期
    		String errMsg = AcIdPHelper.MSG_THIS_URL_IS_INVALIDATE_OR_EXPIRED;
    		TPILogger.tl.debug(errMsg);
    		
    		// 重新導向到前端,顯示訊息
    		getAcIdPHelper().redirectToShowMsg(httpResp, errMsg, acIdPMsgUrl, idPType);
    		return;
    	}
    	
    	// 2.更新 DGR_AC_IDP_USER 資料
    	// User 狀態改為 3:Deny & 清空 code1、code2
    	Long code1 = null;
		code2 = null;
        updateDgrAcIdpUser(dgrAcIdpUser, DgrAcIdpUserStatus.DENY.value(), code1, code2, updateUserName, iip);
        
        // 3.寄信給 IdP User,通知審核結果為 Deny,
        getAcIdPHelper().sendDenyMail(httpReq, userName, userEmail, acIdpUserId, idPType);
        
    	// 4.重新導向到前端,顯示訊息
    	// 使用者狀態設定為 Deny,已寄發email通知使用者
		String errMsg = String.format(AcIdPHelper.MSG_DELEGATE_AC_USER_STATUS_NOTIFY, DgrAcIdpUserStatus.DENY.text());
		TPILogger.tl.debug(errMsg);
		getAcIdPHelper().redirectToShowMsg(httpResp, errMsg, acIdPMsgUrl, idPType);
		return;
	}
	
	/**
	 * 更新 DGR_AC_IDP_USER 的 code1, code2, userStats
	 */
	private DgrAcIdpUser updateDgrAcIdpUser(DgrAcIdpUser dgrAcIdpUser, String userStatus, Long code1, Long code2,
			String updateUserName, InnerInvokeParam iip) {

		String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, dgrAcIdpUser); // 舊資料統一轉成 String
 
		dgrAcIdpUser.setUserStatus(userStatus);
		dgrAcIdpUser.setCode1(code1);
		dgrAcIdpUser.setCode2(code2);
		dgrAcIdpUser.setUpdateDateTime(DateTimeUtil.now());
		dgrAcIdpUser.setUpdateUser(updateUserName);
		dgrAcIdpUser = getDgrAcIdpUserDao().saveAndFlush(dgrAcIdpUser);

		// 寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_IDP_USER.value());

		// 寫入 Audit Log D
		lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogD(iip, lineNumber, DgrAcIdpUser.class.getSimpleName(), TableAct.U.value(),
				oldRowStr, dgrAcIdpUser);// U
		
		return dgrAcIdpUser;
	}
	
	/**
	 * 檢查傳入的資料
	 */
	private String checkReqParam(String userName, String u, String idPType, String cApiKey) {
		String errMsg = null;
 
		// 沒有 userName
		if(!StringUtils.hasLength(userName)) {
			// 缺少必填參數 '%s'
			errMsg = String.format(AcIdPHelper.MSG_MISSING_REQUIRED_PARAMETER, "userName");
			TPILogger.tl.debug(errMsg);
			return errMsg;
		}
		
		// 沒有 u
		if(!StringUtils.hasLength(u)) {
			// 缺少必填參數 '%s'
			errMsg = String.format(AcIdPHelper.MSG_MISSING_REQUIRED_PARAMETER, "u");
			TPILogger.tl.debug(errMsg);
			return errMsg;
		}
		
		// 沒有 idPType
		if(!StringUtils.hasLength(idPType)) {
			// 缺少必填參數 '%s'
			errMsg = String.format(AcIdPHelper.MSG_MISSING_REQUIRED_PARAMETER, "idPType");
			TPILogger.tl.debug(errMsg);
			return errMsg;
		}
 
		// 沒有 cApiKey
		if(!StringUtils.hasLength(cApiKey)) {
    		// 缺少必填參數 '%s'
    		errMsg = String.format(AcIdPHelper.MSG_MISSING_REQUIRED_PARAMETER, "cApiKey");
    		TPILogger.tl.debug(errMsg);
    		return errMsg;
		}
		
    	// 驗證 cApiKey
        boolean isValidate = CApiKeyUtils.verifyCKey(u, cApiKey);
		if (!isValidate) {// 驗證不正確
			// 參數錯誤 '%s'
    		errMsg = String.format(AcIdPHelper.MSG_PARAMETER_ERROR, "cApiKey");
    		TPILogger.tl.debug(errMsg);
    		return errMsg;
        }
		
		return errMsg;
	}
    
	
	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected DgrAcIdpUserDao getDgrAcIdpUserDao() {
		return dgrAcIdpUserDao;
	}

	protected AcIdPHelper getAcIdPHelper() {
		return acIdPHelper;
	}

	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}
}
