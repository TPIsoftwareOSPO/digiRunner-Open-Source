package tpi.dgrv4.gateway.service;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.IdTokenUtil;
import tpi.dgrv4.codec.utils.IdTokenUtil.IdTokenData;
import tpi.dgrv4.codec.utils.JWKcodec;
import tpi.dgrv4.codec.utils.JWKcodec.JWKVerifyResult;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.DgrAuditLogService;
import tpi.dgrv4.entity.entity.DgrAcIdpInfo;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoDao;
import tpi.dgrv4.gateway.component.AcIdPHelper;
import tpi.dgrv4.gateway.component.GtwIdPHelper;
import tpi.dgrv4.gateway.component.IdPTokenHelper;
import tpi.dgrv4.gateway.component.IdPTokenHelper.TokenData;
import tpi.dgrv4.gateway.component.IdPUserInfoHelper;
import tpi.dgrv4.gateway.component.IdPUserInfoHelper.UserInfoData;
import tpi.dgrv4.gateway.component.IdPWellKnownHelper;
import tpi.dgrv4.gateway.component.IdPWellKnownHelper.WellKnownData;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class AcIdPCallbackService {
	
    private TsmpSettingService tsmpSettingService;
    private DgrAcIdpInfoDao dgrAcIdpInfoDao;
	private IdPWellKnownHelper idPWellKnownHelper;
	private IdPTokenHelper idPTokenHelper;
	private DgrAuditLogService dgrAuditLogService;
	private IdPUserInfoHelper idPUserInfoHelper;
	private AcIdPHelper acIdPHelper;
 
	@Autowired	
    public AcIdPCallbackService(TsmpSettingService tsmpSettingService, DgrAcIdpInfoDao dgrAcIdpInfoDao,
			IdPWellKnownHelper idPWellKnownHelper, IdPTokenHelper idPTokenHelper, DgrAuditLogService dgrAuditLogService,
			IdPUserInfoHelper idPUserInfoHelper, AcIdPHelper acIdPHelper) {
		super();
		this.tsmpSettingService = tsmpSettingService;
		this.dgrAcIdpInfoDao = dgrAcIdpInfoDao;
		this.idPWellKnownHelper = idPWellKnownHelper;
		this.idPTokenHelper = idPTokenHelper;
		this.dgrAuditLogService = dgrAuditLogService;
		this.idPUserInfoHelper = idPUserInfoHelper;
		this.acIdPHelper = acIdPHelper;
	}

	/**
     * 以 IdP(GOOGLE / MS / OIDC) 的授權碼, 取得 IdP 的 token, <br>
     * 若 user 狀態為 allow, 則重新導向到前端以登入AC
     */
	public void acIdPCallback(HttpHeaders httpHeaders, HttpServletRequest httpReq, HttpServletResponse httpResp,
			String idPType) throws Exception {
		
		// 前端AC IdP errMsg顯示訊息的URL
		String acIdPMsgUrl = getTsmpSettingService().getVal_AC_IDP_MSG_URL();
				
		try {
			String reqUri = httpReq.getRequestURI();
			String txnUid = getDgrAuditLogService().getTxnUid();
			String userIp = !StringUtils.hasLength(httpHeaders.getFirst("x-forwarded-for")) ? httpReq.getRemoteAddr()
					: httpHeaders.getFirst("x-forwarded-for");
			String userHostname = httpReq.getRemoteHost();
			
			String idPAuthCode = httpReq.getParameter("code");
			
			String userName = "N/A";// 此時還沒有值
			String userAlias = null;// 此時還沒有值
			
			String errMsg = checkReqParam(idPType, idPAuthCode);
			if(StringUtils.hasLength(errMsg)) {
				// 寫入 Audit Log M,登入失敗
				String lineNumber = StackTraceUtil.getLineNumber();
				getAcIdPHelper().createAuditLogMForLoginFailed(reqUri, lineNumber, userIp, userHostname, txnUid, errMsg,
						idPType, userName, userAlias);
				
				// 重新導向到前端,顯示訊息
				getAcIdPHelper().redirectToShowMsg(httpResp, errMsg, acIdPMsgUrl, idPType);
				return;
			}

			acIdPCallback(httpReq, httpResp, idPType, idPAuthCode, acIdPMsgUrl, reqUri, userIp, userHostname, txnUid);

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));

			// 重新導向到前端,顯示訊息
			String errMsg = "System error";
			TPILogger.tl.error(errMsg);
			getAcIdPHelper().redirectToShowMsg(httpResp, errMsg, acIdPMsgUrl, idPType);
			return;
		}
	}
    
	public void acIdPCallback(HttpServletRequest httpReq, HttpServletResponse httpResp, String idPType, String idPAuthCode,
			String acIdPMsgUrl, String reqUri, String userIp, String userHostname, String txnUid) throws Exception {
		idPType = idPType.toUpperCase();
		
		String userName = "N/A";// 此時還沒有值
		String userAlias = null;// 此時還沒有值

		// 1.取得 IdP(GOOGLE / MS / OIDC) info 資料
		DgrAcIdpInfo dgrAcIdpInfo = getDgrAcIdpInfoDao().findFirstByIdpTypeAndClientStatusOrderByCreateDateTimeDesc(idPType, "Y");
		if(dgrAcIdpInfo == null) {
			// Table [DGR_AC_IDP_INFO] 查不到資料
			TPILogger.tl.error("Table [DGR_AC_IDP_INFO] can't find data");
			// 設定檔缺少參數 '%s'
			String errMsg = String.format(AcIdPHelper.MSG_THE_PROFILE_IS_MISSING_PARAMETERS,
					"AC IdP(" + idPType + ") info");
			TPILogger.tl.error(errMsg);
			
			// 寫入 Audit Log M,登入失敗
			String lineNumber = StackTraceUtil.getLineNumber();
			getAcIdPHelper().createAuditLogMForLoginFailed(reqUri, lineNumber, userIp, userHostname, txnUid, errMsg,
					idPType, userName, userAlias);
    		
    		// 重新導向到前端,顯示訊息
    		getAcIdPHelper().redirectToShowMsg(httpResp, errMsg, acIdPMsgUrl, idPType);
    		return;
		}
		
    	String idPClientId = dgrAcIdpInfo.getClientId();
    	String idPClientMima = dgrAcIdpInfo.getClientMima();
    	String wellKnownUrl = dgrAcIdpInfo.getWellKnownUrl();
    	String dgrCallbackUrl = dgrAcIdpInfo.getCallbackUrl();
    	String idPAccessTokenUrl = dgrAcIdpInfo.getAccessTokenUrl();
    	
    	if(!StringUtils.hasLength(wellKnownUrl)) {
    		// 設定檔缺少參數 '%s'
    		String errMsg = String.format(AcIdPHelper.MSG_THE_PROFILE_IS_MISSING_PARAMETERS, "wellKnownUrl");
    		TPILogger.tl.error(errMsg);
    		
			// 寫入 Audit Log M,登入失敗
			String lineNumber = StackTraceUtil.getLineNumber();
			getAcIdPHelper().createAuditLogMForLoginFailed(reqUri, lineNumber, userIp, userHostname, txnUid, errMsg,
					idPType, userName, userAlias);
    		
    		// 重新導向到前端,顯示訊息
    		getAcIdPHelper().redirectToShowMsg(httpResp, errMsg, acIdPMsgUrl, idPType);
    		return;
    	}
    	
    	// 2.打 Id(GOOGLE / MS / OIDC) Well Known URL, 取得 JSON 資料
    	WellKnownData wellKnownData = getIdPWellKnownHelper().getWellKnownData(wellKnownUrl, reqUri);
		ResponseEntity<?> errRespEntity = wellKnownData.errRespEntity;
		if (errRespEntity != null) {
			String errMsg = wellKnownData.errMsg;
			
			// 寫入 Audit Log M,登入失敗
			String lineNumber = StackTraceUtil.getLineNumber();
			getAcIdPHelper().createAuditLogMForLoginFailed(reqUri, lineNumber, userIp, userHostname, txnUid, errMsg,
					idPType, userName, userAlias);
			
    		// 重新導向到前端,顯示訊息
			getAcIdPHelper().redirectToShowMsg(httpResp, errMsg, acIdPMsgUrl, idPType);
    		return;
		}
    	
		// 3.由 Well Known JSON 中取得資料
		
		// 若 accessTokenUrl 沒有值, 則從 Well Known 取得
    	if(!StringUtils.hasLength(idPAccessTokenUrl)) {
    		idPAccessTokenUrl = wellKnownData.tokenEndpoint;
    	}
    	
		if(!StringUtils.hasLength(idPAccessTokenUrl)) {
    		// 設定檔缺少參數 '%s'
    		String errMsg = String.format(AcIdPHelper.MSG_THE_PROFILE_IS_MISSING_PARAMETERS, "accessTokenUrl");
    		TPILogger.tl.error(errMsg);
    		
			// 寫入 Audit Log M,登入失敗
			String lineNumber = StackTraceUtil.getLineNumber();
			getAcIdPHelper().createAuditLogMForLoginFailed(reqUri, lineNumber, userIp, userHostname, txnUid, errMsg,
					idPType, userName, userAlias);
    		
    		// 重新導向到前端,顯示訊息
    		getAcIdPHelper().redirectToShowMsg(httpResp, errMsg, acIdPMsgUrl, idPType);
    		return;
		}
		
		// 取得 Jwks Uri
		String jwksUri = wellKnownData.jwksUri;
		if(!StringUtils.hasLength(jwksUri)) {
    		// 缺少必填參數 '%s'
    		String errMsg = String.format(AcIdPHelper.MSG_MISSING_REQUIRED_PARAMETER, "jwksUri");
    		TPILogger.tl.error(errMsg);
    		
			// 寫入 Audit Log M,登入失敗
			String lineNumber = StackTraceUtil.getLineNumber();
			getAcIdPHelper().createAuditLogMForLoginFailed(reqUri, lineNumber, userIp, userHostname, txnUid, errMsg,
					idPType, userName, userAlias);
    		
    		// 重新導向到前端,顯示訊息
    		getAcIdPHelper().redirectToShowMsg(httpResp, errMsg, acIdPMsgUrl, idPType);
    		return;
		}
		
		// 取得 issuer
		String issuer = wellKnownData.issuer;
		if(!StringUtils.hasLength(issuer)) {
    		// 缺少必填參數 '%s'
    		String errMsg = String.format(AcIdPHelper.MSG_MISSING_REQUIRED_PARAMETER, "issuer");
    		TPILogger.tl.error(errMsg);
    		
			// 寫入 Audit Log M,登入失敗
			String lineNumber = StackTraceUtil.getLineNumber();
			getAcIdPHelper().createAuditLogMForLoginFailed(reqUri, lineNumber, userIp, userHostname, txnUid, errMsg,
					idPType, userName, userAlias);
    		
    		// 重新導向到前端,顯示訊息
    		getAcIdPHelper().redirectToShowMsg(httpResp, errMsg, acIdPMsgUrl, idPType);
    		return;
		}
		
		// 4.打 IdP(GOOGLE / MS / OIDC) 的 token API, 取得 Access Token 和 ID Token
		// 從 cookies 取得 codeVerifier 的值
		String apiResp = null;
		String codeVerifierForOauth2 = GtwIdPHelper.getStateFromCookies(httpReq, GtwIdPHelper.COOKIE_CODE_VERIFIER);
		TokenData tokenData = getIdPTokenHelper().getTokenData(idPType, idPClientId, idPClientMima, idPAccessTokenUrl,
				dgrCallbackUrl, idPAuthCode, codeVerifierForOauth2, reqUri);
		errRespEntity = tokenData.errRespEntity;
		if (errRespEntity != null) {
			String errMsg = tokenData.errMsg;
					
			// 寫入 Audit Log M,登入失敗
			String lineNumber = StackTraceUtil.getLineNumber();
			getAcIdPHelper().createAuditLogMForLoginFailed(reqUri, lineNumber, userIp, userHostname, txnUid, errMsg,
					idPType, userName, userAlias);

			// 重新導向到前端,顯示訊息
			getAcIdPHelper().redirectToShowMsg(httpResp, errMsg, acIdPMsgUrl, idPType);
			return;
		}
		
		String idTokenJwtstr = tokenData.idToken;
		String accessTokenJwtstr = tokenData.accessToken;
		String refreshTokenJwtstr = tokenData.refreshToken;
		apiResp = tokenData.apiResp;
 
		if(!StringUtils.hasLength(idTokenJwtstr)) {
    		// 缺少必填參數 '%s'
    		String errMsg = String.format(AcIdPHelper.MSG_MISSING_REQUIRED_PARAMETER, "idTokenJwtstr");
    		TPILogger.tl.error(errMsg);
    		
			// 寫入 Audit Log M,登入失敗
			String lineNumber = StackTraceUtil.getLineNumber();
			getAcIdPHelper().createAuditLogMForLoginFailed(reqUri, lineNumber, userIp, userHostname, txnUid, errMsg,
					idPType, userName, userAlias);
    		
    		// 重新導向到前端,顯示訊息
    		getAcIdPHelper().redirectToShowMsg(httpResp, errMsg, acIdPMsgUrl, idPType);
    		return;
		}
		
		// 5.驗證 IdP(GOOGLE / MS / OIDC) ID Token
		boolean isVerify = false;            
		JWKVerifyResult jwkRs = JWKcodec.verifyJWStoken(idTokenJwtstr, jwksUri, issuer);
		isVerify = jwkRs.verify;
		TPILogger.tl.debug("ID token verify : " + isVerify);
		
		String userEmail = null;
		
		if (isVerify) {// 驗證 ID Token 成功
			// 6.1.取得 IdP ID Token 中的 sub, name 和 email
			IdTokenData idTokenData = IdTokenUtil.getIdTokenData(idTokenJwtstr);
			userName = idTokenData.userName;
			userAlias = idTokenData.userAlias; 
			userEmail = idTokenData.userEmail;
			
		}else {// 驗證 ID Token 失敗, 再用 access token 打 UserInfo 取得 email, 若仍沒有值, 才顯示錯誤訊息
			// 6.2.打 IdP 的 UserInfo API, 取得 sub, name 和 email
			
			// 取得 UserInfo URL
			String userinfoUrl = wellKnownData.userinfoEndpoint;
			if(!StringUtils.hasLength(userinfoUrl)) {
	    		// 缺少必填參數 '%s'
	    		String errMsg = String.format(AcIdPHelper.MSG_MISSING_REQUIRED_PARAMETER, "userinfoUrl");
	    		TPILogger.tl.error(errMsg);
	    		
				// 寫入 Audit Log M,登入失敗
				String lineNumber = StackTraceUtil.getLineNumber();
				getAcIdPHelper().createAuditLogMForLoginFailed(reqUri, lineNumber, userIp, userHostname, txnUid, errMsg,
						idPType, userName, userAlias);
	    		
	    		// 重新導向到前端,顯示訊息
	    		getAcIdPHelper().redirectToShowMsg(httpResp, errMsg, acIdPMsgUrl, idPType);
	    		return;
			}
 
			UserInfoData userInfoData = getIdPUserInfoHelper().getUserInfoData(userinfoUrl, accessTokenJwtstr, reqUri);
			errRespEntity = userInfoData.errRespEntity;
			if(errRespEntity != null) {
				String errMsg = userInfoData.errMsg;
				
				// 寫入 Audit Log M,登入失敗
				String lineNumber = StackTraceUtil.getLineNumber();
				getAcIdPHelper().createAuditLogMForLoginFailed(reqUri, lineNumber, userIp, userHostname, txnUid, errMsg,
						idPType, userName, userAlias);
				
	    		// 重新導向到前端,顯示訊息
				getAcIdPHelper().redirectToShowMsg(httpResp, errMsg, acIdPMsgUrl, idPType);
	    		return;
			}
			userName = userInfoData.userName;
			userAlias = userInfoData.userAlias; 
			userEmail = userInfoData.userEmail;
		}
		
		if(!StringUtils.hasLength(userName)) {
			String errMsg = AcIdPHelper.MSG_ID_TOKEN_VERIFICATION_FAILED;
			TPILogger.tl.debug(errMsg);
			
			// 寫入 Audit Log M,登入失敗
			String lineNumber = StackTraceUtil.getLineNumber();
			getAcIdPHelper().createAuditLogMForLoginFailed(reqUri, lineNumber, userIp, userHostname, txnUid, errMsg,
					idPType, userName, userAlias);
			
			// 重新導向到前端,顯示訊息
			getAcIdPHelper().redirectToShowMsg(httpResp, errMsg, acIdPMsgUrl, idPType);
			return;
		}
		
		// 7.依 User 狀態,寄信通知審核者 或 建立 dgRcode 重新導向到前端,以登入AC
		getAcIdPHelper().sendMailOrCreateDgRcode(httpReq, httpResp, idPType, userName, userAlias, userEmail, idTokenJwtstr,
				accessTokenJwtstr, refreshTokenJwtstr, reqUri, userIp, userHostname, txnUid, acIdPMsgUrl, apiResp);
	}
 
	/**
	 * 檢查傳入的資料
	 */
	private String checkReqParam(String idPType, String idPAuthCode) {
		String errMsg = null;
 
		// 沒有 code
		if(!StringUtils.hasLength(idPAuthCode)) {
    		// 缺少必填參數 '%s'
    		errMsg = String.format(AcIdPHelper.MSG_MISSING_REQUIRED_PARAMETER, "authCode");
    		TPILogger.tl.debug(errMsg);
    		return errMsg;
		}
		
		return errMsg;
	}
 
    protected TsmpSettingService getTsmpSettingService() {
    	return this.tsmpSettingService;
    }
 
	protected DgrAcIdpInfoDao getDgrAcIdpInfoDao() {
		return dgrAcIdpInfoDao;
	}
	
	protected IdPWellKnownHelper getIdPWellKnownHelper() {
		return idPWellKnownHelper;
	}
	
	protected IdPTokenHelper getIdPTokenHelper() {
		return idPTokenHelper;
	}
	
	protected IdPUserInfoHelper getIdPUserInfoHelper() {
		return idPUserInfoHelper;
	}
	
	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}
	
	protected  AcIdPHelper getAcIdPHelper() {
		return acIdPHelper;
	}
}
