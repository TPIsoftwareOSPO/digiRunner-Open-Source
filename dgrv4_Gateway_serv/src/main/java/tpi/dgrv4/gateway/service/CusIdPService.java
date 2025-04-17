package tpi.dgrv4.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.common.constant.DgrIdPType;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.DgrAuditLogService;
import tpi.dgrv4.gateway.component.AcIdPHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class CusIdPService {

	private DgrAuditLogService dgrAuditLogService;
	private AcIdPHelper acIdPHelper;
	private TsmpSettingService tsmpSettingService;

	@Autowired
	public CusIdPService(DgrAuditLogService dgrAuditLogService, AcIdPHelper acIdPHelper,
			TsmpSettingService tsmpSettingService) {
		super();
		this.dgrAuditLogService = dgrAuditLogService;
		this.acIdPHelper = acIdPHelper;
		this.tsmpSettingService = tsmpSettingService;
	}

	protected void getCusLoginFailedHandler(HttpHeaders httpHeaders, HttpServletRequest httpReq,
			HttpServletResponse httpResp, String customErrorMsg) throws RedirectException {

		getCusLoginFailedHandler(httpHeaders, httpReq, httpResp, customErrorMsg, true);
	}

	/**
	 * 處理 CUS 帳號登入失敗情況。
	 */
	protected void getCusLoginFailedHandler(HttpHeaders httpHeaders, HttpServletRequest httpReq,
			HttpServletResponse httpResp, String customErrorMsg, boolean isAcIdp) throws RedirectException {

		// 設定錯誤訊息，如果 customErrorMsg 為 null，則使用預設訊息
		String errMsg = customErrorMsg != null ? customErrorMsg : "No error message provided";
		// 取得訊息顯示 URL
		String msgUrl = "";

		if (isAcIdp) {
			msgUrl = getTsmpSettingService().getVal_AC_IDP_MSG_URL();
		} else {
			msgUrl = getTsmpSettingService().getVal_GTW_IDP_MSG_URL();
		}

		// 建立登入失敗的日誌 第三方登入無法驗證登入失敗與否，取消紀錄, Kevin Cheng, 2024/08/21
//		createAuditLogMForLoginFailed(httpHeaders, httpReq, errMsg);
		// 重新導向至顯示訊息頁面
		redirectToShowMsg(httpResp, errMsg, msgUrl, DgrIdPType.CUS);
	}

	/**
	 * 建立登入失敗的日誌。
	 */
	private void createAuditLogMForLoginFailed(HttpHeaders httpHeaders, HttpServletRequest httpReq, String errMsg) {

		// 取得請求的 URI
		String reqUri = httpReq.getRequestURI();
		// 取得 UID
		String txnUid = getDgrAuditLogService().getTxnUid();
		// 取得使用者 IP
		String userIp = !StringUtils.hasLength(httpHeaders.getFirst("x-forwarded-for")) ? httpReq.getRemoteAddr()
				: httpHeaders.getFirst("x-forwarded-for");
		// 取得使用者主機名
		String userHostname = httpReq.getRemoteHost();

		// 設定使用者名稱
		String userName = "CUS_Login";

		// 取得發生錯誤的行號
		String lineNumber = StackTraceUtil.getLineNumber();

		// 建立登入失敗的日誌
		getAcIdPHelper().createAuditLogMForLoginFailed(reqUri, lineNumber, userIp, userHostname, txnUid, errMsg,
				DgrIdPType.CUS, userName, null);
	}

	/**
	 * 將使用者重新導向至顯示訊息頁面。
	 */
	private void redirectToShowMsg(HttpServletResponse httpResp, String errMsg, String msgUrl, String type)
			throws RedirectException {
		try {
			// 進行重新導向
			getAcIdPHelper().redirectToShowMsg(httpResp, errMsg, msgUrl, type);
			throw new RedirectException("Redirecting to new URL");

		} catch (RedirectException e) {
			throw e;

		} catch (Exception e) {
			TPILogger.tl.error("Login error occurred, followed by redirect failure. Received error message: " + errMsg
					+ ", Error message URL: " + msgUrl + ", Received Type: " + type);
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw new RedirectException("Login error occurred, followed by redirect failure.");
		}
	}

	public static class RedirectException extends Exception {
		private static final long serialVersionUID = 1L;

		public RedirectException(String message) {
			super(message);
		}
	}

	protected AcIdPHelper getAcIdPHelper() {
		return acIdPHelper;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}

}
