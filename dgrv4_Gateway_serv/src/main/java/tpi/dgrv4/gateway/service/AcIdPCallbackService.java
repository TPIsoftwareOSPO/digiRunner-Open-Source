package tpi.dgrv4.gateway.service;


import java.util.HashMap;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;
import tpi.dgrv4.common.utils.ClientIpUtil;

@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Service
public class AcIdPCallbackService {
	
    private final TsmpSettingService tsmpSettingService;
    private final DgrAcIdpInfoDao dgrAcIdpInfoDao;
	private final IdPWellKnownHelper idPWellKnownHelper;
	private final IdPTokenHelper idPTokenHelper;
	private final DgrAuditLogService dgrAuditLogService;
	private final IdPUserInfoHelper idPUserInfoHelper;
	private final AcIdPHelper acIdPHelper;

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
//			String userIp = !StringUtils.hasLength(httpHeaders.getFirst("x-forwarded-for")) ? httpReq.getRemoteAddr()
//					: httpHeaders.getFirst("x-forwarded-for");
            String userIp = ClientIpUtil.getClientIp(httpReq);
			String userHostname = httpReq.getRemoteHost();
			
			String idPAuthCode = httpReq.getParameter("code");
			
			String userName = "N/A";// 此時還沒有值
			String userAlias = null;// 此時還沒有值
			
			String errMsg = checkReqParam(idPAuthCode);
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
		
		// 5.取得 ID token 中的 iss
		IdTokenData idTokenDataForIss = IdTokenUtil.getIdTokenDataForIss(idTokenJwtstr);
		String issuer = idTokenDataForIss.iss;
		TPILogger.tl.debug("ID Token iss: " + issuer);
		if(!StringUtils.hasLength(issuer)) {
			// ID Token 中找不到 'iss' 值
			String errMsg = "The 'iss' value cannot be found in the ID Token.";
			TPILogger.tl.error(errMsg);
			
			// 寫入 Audit Log M,登入失敗
			String lineNumber = StackTraceUtil.getLineNumber();
			getAcIdPHelper().createAuditLogMForLoginFailed(reqUri, lineNumber, userIp, userHostname, txnUid, errMsg,
					idPType, userName, userAlias);
			
			// 重新導向到前端,顯示訊息
			getAcIdPHelper().redirectToShowMsg(httpResp, errMsg, acIdPMsgUrl, idPType);
			return;
		}
		
		// 6.組成 well-known URL
		String wellKnowUrl2 = issuer;
		if (!wellKnowUrl2.endsWith("/")) {// 最後不是 "/"
			wellKnowUrl2 += "/";
		}
		wellKnowUrl2 = wellKnowUrl2 + ".well-known/openid-configuration";
		
		TPILogger.tl.debug("Well-Known URL: " + wellKnowUrl2);
		
		// 7.打 Id(GOOGLE / MS / OIDC) Well Known URL, 取得 JSON 資料
		WellKnownData wellKnownData2 = getIdPWellKnownHelper().getWellKnownData(wellKnowUrl2, reqUri);
		errRespEntity = wellKnownData2.errRespEntity;
		if (errRespEntity != null) {
			String errMsg = wellKnownData2.errMsg;
			
			// 寫入 Audit Log M,登入失敗
			String lineNumber = StackTraceUtil.getLineNumber();
			getAcIdPHelper().createAuditLogMForLoginFailed(reqUri, lineNumber, userIp, userHostname, txnUid, errMsg,
					idPType, userName, userAlias);
			
			// 重新導向到前端,顯示訊息
			getAcIdPHelper().redirectToShowMsg(httpResp, errMsg, acIdPMsgUrl, idPType);
			return;
		}
		
		// 8.取得 JWKS URL
		String jwksUri = wellKnownData2.jwksUri;
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
		
		// 9.打 IdP(GOOGLE / MS / OIDC) 的 JWKS URI
		TPILogger.tl.debug("jwks_uri: " + jwksUri);
		HttpRespData jwksResp = HttpUtil.httpReqByGetList(jwksUri, new HashMap<>(), false, false);
		int statusCode = jwksResp.statusCode;
		boolean isCallJwksUrlSuccess = true;
		if (statusCode >= 300) {
			isCallJwksUrlSuccess = false;
			TPILogger.tl.debug(jwksResp.getLogStr());
			String errMsg = String.format("Calling the IdP JWKS URL failed. HTTP Status Code '%s' : %s"
					, statusCode + ""
					, jwksResp.respStr);
			TPILogger.tl.debug(errMsg);
		}
		
		// 10.驗證 IdP(GOOGLE / MS / OIDC) ID Token
		boolean isVerify = false;
		if (isCallJwksUrlSuccess) {// 取 JWKS 成功
			// issuer 必須和 ID token 的 iss 完全相同, 注意後面有沒有 "/" 有差別
			JWKVerifyResult jwkRs = JWKcodec.verifyJWStokenByJsonString(idTokenJwtstr, issuer, jwksResp.respStr);
			isVerify = jwkRs.verify;
			if(!isVerify) {
				// 驗證 ID Token 失敗,印出訊息
				TPILogger.tl.debug(jwksResp.getLogStr());
				TPILogger.tl.error(jwkRs.errorMessg);
				TPILogger.tl.error(StackTraceUtil.logStackTrace(jwkRs.errException));
			}
		}
		
		TPILogger.tl.debug("ID token verify : " + isVerify);
		
		// 核發 AC OAuth 2.0 IdP token, 其中 username 從 IdP ID token 的什麼參數取得 (預設: sub)
		String acIdpOauth2UsernameVal = getTsmpSettingService().getVal_AC_IDP_OAUTH2_USERNAME();
		TPILogger.tl.debug("AC_IDP_OAUTH2_USERNAME : " + acIdpOauth2UsernameVal);
		// AC_IDP 登入時, username 是否做 base64 編碼 (true/false)(default: false) 
		boolean acIdpUsernameB64EncodeVal = getTsmpSettingService().getVal_AC_IDP_USERNAME_B64_ENCODE();
		TPILogger.tl.debug("AC_IDP_USERNAME_B64_ENCODE : " + acIdpUsernameB64EncodeVal);
		
		String userEmail = null;
		
		if (isVerify) {// 方法1.驗證 ID Token 成功
			// 10.1.取得 IdP ID Token 中的 sub, name 和 email
			IdTokenData idTokenData = IdTokenUtil.getIdTokenDataForAcIdP(idTokenJwtstr, acIdpOauth2UsernameVal,
					acIdpUsernameB64EncodeVal);
			String errMsg = idTokenData.errMsg;
			if (StringUtils.hasLength(errMsg)) {
				// 寫入 Audit Log M,登入失敗
				String lineNumber = StackTraceUtil.getLineNumber();
				getAcIdPHelper().createAuditLogMForLoginFailed(reqUri, lineNumber, userIp, userHostname, txnUid, errMsg,
						idPType, userName, userAlias);
				
				// 重新導向到前端,顯示訊息
				getAcIdPHelper().redirectToShowMsg(httpResp, errMsg, acIdPMsgUrl, idPType);
				return;
			}
			userName = idTokenData.userName;
			userAlias = idTokenData.userAlias; 
			userEmail = idTokenData.userEmail;
			
		} else {// 驗證 ID Token 失敗, 用 access token 打 UserInfo 取得資料, 若仍沒有值, 才顯示錯誤訊息
			
			// 因方法1 驗證 ID Token 失敗, 故改用方法2 再用 access token 打 UserInfo 取得資料
			TPILogger.tl.debug(
					"Since method 1 failed to verify the ID token, method 2 was used instead to obtain the information by using the access token to unlock the UserInfo.");

			// 10.2.打 IdP 的 UserInfo API, 取得 sub, name 和 email
			// 取得 UserInfo URL
			String userinfoUrl = wellKnownData.userinfoEndpoint;
			TPILogger.tl.debug("userinfo_endpoint: " + userinfoUrl);
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
 
			UserInfoData userInfoData = getIdPUserInfoHelper().getUserInfoData(userinfoUrl, accessTokenJwtstr, reqUri,
					"AC_IDP_OAUTH2_USERNAME", acIdpOauth2UsernameVal, acIdpUsernameB64EncodeVal);
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
		
		// 11.依 User 狀態,寄信通知審核者 或 建立 dgRcode 重新導向到前端,以登入AC
		getAcIdPHelper().sendMailOrCreateDgRcode(httpReq, httpResp, idPType, userName, userAlias, userEmail, idTokenJwtstr,
				accessTokenJwtstr, refreshTokenJwtstr, reqUri, userIp, userHostname, txnUid, acIdPMsgUrl, apiResp);
	}
 
	/**
	 * 檢查傳入的資料
	 */
	private String checkReqParam(String idPAuthCode) {
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
