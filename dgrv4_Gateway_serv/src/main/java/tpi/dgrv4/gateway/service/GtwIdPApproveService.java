package tpi.dgrv4.gateway.service;

import java.util.Date;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import tpi.dgrv4.codec.utils.UUID64Util;
import tpi.dgrv4.common.constant.DgrAuthCodePhase;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.DgrGtwIdpAuthCode;
import tpi.dgrv4.entity.entity.DgrGtwIdpAuthM;
import tpi.dgrv4.entity.repository.DgrGtwIdpAuthCodeDao;
import tpi.dgrv4.entity.repository.DgrGtwIdpAuthMDao;
import tpi.dgrv4.entity.repository.DgrOauthApprovalsDao;
import tpi.dgrv4.gateway.component.GtwIdPHelper;
import tpi.dgrv4.gateway.component.IdPHelper;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp2;

/**
 * @author Mini <br>
 */
/**
 * [ZH] 檢查傳入的資料, 例如: state、redirect_uri, 若正常則產生 dgRcode, 並重新導向 302 到 redirect_uri <br>
 * [EN] Check the incoming data, such as: state, redirect_uri, if normal, generate dgRcode, and redirect 302 to redirect_uri <br>
 * (GOOGLE / MS / OIDC) <br>
 * (LDAP / API / JDBC) <br>
 */
@AllArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Service
public class GtwIdPApproveService {
	
	private GtwIdPAuthService gtwIdPAuthService;
	private DgrGtwIdpAuthCodeDao dgrGtwIdpAuthCodeDao;
	private DgrGtwIdpAuthMDao dgrGtwIdpAuthMDao; 
	private TokenHelper tokenHelper;
	private OAuthTokenService oAuthTokenService;
	private OAuthAuthorizationService oAuthAuthorizationService;
	private DgrOauthApprovalsDao dgrOauthApprovalsDao; 
	private GtwIdPHelper gtwIdPHelper;
	
	public ResponseEntity<?> gtwIdPApprove(HttpHeaders headers, HttpServletRequest httpReq,
			HttpServletResponse httpResp, String idPType) throws Exception {
		
		String reqUri = httpReq.getRequestURI();
		String dgrClientRedirectUri = httpReq.getParameter("redirect_uri");
		ResponseEntity<?> errRespEntity = null;
		
		try {
			String userName = httpReq.getParameter("username");
			String dgrVGroupScopeStr = httpReq.getParameter("scope");
			String state = httpReq.getParameter("state");
			
			errRespEntity = checkReqParam(dgrVGroupScopeStr, userName, dgrClientRedirectUri, state, reqUri);
			if (errRespEntity != null) {// 資料驗證有錯誤
				getGtwIdPHelper().redirectToShowMsg(httpResp, errRespEntity, idPType, dgrClientRedirectUri);
				return null;
			}
			
			errRespEntity = gtwIdPApprove(httpResp, state, dgrVGroupScopeStr, userName, dgrClientRedirectUri, reqUri);
			if (errRespEntity != null) {// 資料驗證有錯誤
				getGtwIdPHelper().redirectToShowMsg(httpResp, errRespEntity, idPType, dgrClientRedirectUri);
				return null;
			}
 
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			String errMsg = TokenHelper.INTERNAL_SERVER_ERROR;
			TPILogger.tl.error(errMsg);
			errRespEntity = getTokenHelper().getInternalServerErrorResp(reqUri, errMsg);// 500
			getGtwIdPHelper().redirectToShowMsg(httpResp, errRespEntity, idPType, dgrClientRedirectUri);
			return null;
		}
		return null;
	}
	
	public ResponseEntity<?> gtwIdPApprove(HttpServletResponse httpResp, String state, String vGroupScopeStr,
			String userName, String dgrClientRedirectUri, String reqUri) throws Exception {

		// 1.依 state, 取得 DGR_GTW_IDP_AUTH_CODE 資料
		DgrGtwIdpAuthCode dgrGtwIdpAuthCode = getDgrGtwIdpAuthCodeDao().findFirstByAuthCodeAndPhase(state,
				DgrAuthCodePhase.STATE);
		if (dgrGtwIdpAuthCode == null) {
			// Table [DGR_GTW_IDP_AUTH_CODE] 查不到
			String errMsg1 = "Bad credentials";
			String errMsg2 = "Table [DGR_GTW_IDP_AUTH_CODE] can't find, auth_code(state):" + state;
			TPILogger.tl.debug(errMsg1 + "\n" + errMsg2);
			return new ResponseEntity<OAuthTokenErrorResp2>(
					getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_GRANT, errMsg1),
					HttpStatus.BAD_REQUEST);// 400
		}
		
		String dgrClientId = dgrGtwIdpAuthCode.getClientId();
 
		// 2.檢查 redirect_uri 
		ResponseEntity<?> errRespEntity = getTokenHelper().checkRedirectUri(dgrClientId, dgrClientRedirectUri, reqUri);
		if (errRespEntity != null) {// redirectUri 驗證有錯誤
			return errRespEntity;
		}
 
		// 3.產生 dgRcode (auth code 授權碼)
		String dgRcode = UUID64Util.UUID64(UUID.randomUUID());// 產生 dgRcode(UUID 64位元)
		Date expiredTime = IdPHelper.getAuthCodeExpiredTime();// 授權碼的到期時間
		Long expireDateTime = expiredTime.getTime();
		
		// 更新 DGR_GTW_IDP_AUTH_CODE, 寫入 dgRcode
		dgrGtwIdpAuthCode.setAuthCode(dgRcode);
		dgrGtwIdpAuthCode.setPhase(DgrAuthCodePhase.AUTH_CODE);
		dgrGtwIdpAuthCode.setExpireDateTime(expireDateTime);
		
		dgrGtwIdpAuthCode.setCreateUser("SYSTEM");
		dgrGtwIdpAuthCode.setCreateDateTime(DateTimeUtil.now());
		dgrGtwIdpAuthCode = getDgrGtwIdpAuthCodeDao().save(dgrGtwIdpAuthCode);
 
		// 4.寫入 VGroup scope
		// 取得 DGR_GTW_IDP_AUTH_M 資料
		DgrGtwIdpAuthM dgrGtwIdpAuthM = getDgrGtwIdpAuthMDao().findFirstByState(state);
		long gtwIdpAuthMId = dgrGtwIdpAuthM.getGtwIdpAuthMId();
		
		// 將 dgRcode 更新到 DGR_GTW_IDP_AUTH_M
		dgrGtwIdpAuthM.setAuthCode(dgRcode);
		dgrGtwIdpAuthM.setUpdateUser("SYSTEM");
		dgrGtwIdpAuthM.setUpdateDateTime(DateTimeUtil.now());
		dgrGtwIdpAuthM = getDgrGtwIdpAuthMDao().save(dgrGtwIdpAuthM);
		
		// 建立 DGR_GTW_IDP_AUTH_D, 寫入 vGroup scope
		getGtwIdPAuthService().createDgrGtwIdpAuthD(gtwIdpAuthMId, vGroupScopeStr);

		// 5. 重新導向 302 到 redirect_uri, 參數 code 和 state
		String redirectUrl = String.format(
				"%s" 
				+ "?code=%s" 
				+ "&state=%s",
				dgrClientRedirectUri, 
				dgRcode,
				state
		);
		
		/*
		 * [ZH] 6. 再次檢查 redirect_uri, 並轉導。
		 * 雖然前面已有檢查過了, 但 SonarQube 要求這裡也要檢查, 以避免開放重定向攻擊。
		 * [EN] 6. Check redirect_uri again and redirect.
		 * Although this has been checked before, SonarQube requires this to be checked as well to avoid open redirection attacks.
		 */
		TPILogger.tl.debug("Redirect to URL【dgR Client Redirect URL】: " + redirectUrl);
		errRespEntity = getTokenHelper().checkRedirectUri(dgrClientId, redirectUrl.substring(0, redirectUrl.indexOf("?")), reqUri);
		if (errRespEntity != null) {// redirectUri 驗證有錯誤
			return errRespEntity;
		} else {
			httpResp.sendRedirect(redirectUrl);
		}
		
		return null;
	}
	
	/**
	 * 檢查傳入的資料
	 */
	private ResponseEntity<?> checkReqParam(String dgrVGroupScopeStr, String userName, String dgrClientRedirectUri,
			String state, String reqUri) {
 
		// 1.可以不勾選 VGroup 的 API, 所以不用檢查 scope
		
		// 2.沒有 username
		if (!StringUtils.hasLength(userName)) {
			String errMsg = TokenHelper.MISSING_REQUIRED_PARAMETER + "username";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp2>(
					getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
					HttpStatus.BAD_REQUEST);// 400
		}
		
		// 3.沒有 redirect_uri
		if (!StringUtils.hasLength(dgrClientRedirectUri)) {
			String errMsg = TokenHelper.MISSING_REQUIRED_PARAMETER + "redirect_uri";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp2>(
					getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
					HttpStatus.BAD_REQUEST);// 400
		}
		
		// 4.沒有 state
		if (!StringUtils.hasLength(state)) {
			String errMsg = TokenHelper.MISSING_REQUIRED_PARAMETER + "state";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp2>(
					getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
					HttpStatus.BAD_REQUEST);// 400
		}
 
		return null;
	}
 
	protected GtwIdPAuthService getGtwIdPAuthService() {
		return gtwIdPAuthService;
	}
	
	protected DgrGtwIdpAuthMDao getDgrGtwIdpAuthMDao() {
		return dgrGtwIdpAuthMDao;
	}
	
	protected DgrGtwIdpAuthCodeDao getDgrGtwIdpAuthCodeDao() {
		return dgrGtwIdpAuthCodeDao;
	}
 
	protected TokenHelper getTokenHelper() {
		return tokenHelper;
	}
	
	protected DgrOauthApprovalsDao getDgrOauthApprovalsDao() {
		return dgrOauthApprovalsDao;
	}
	
	protected OAuthAuthorizationService getOAuthAuthorizationService() {
		return oAuthAuthorizationService;
	}
	
	protected OAuthTokenService getOAuthTokenService() {
		return oAuthTokenService;
	}
	
	protected GtwIdPHelper getGtwIdPHelper() {
		return gtwIdPHelper;
	}
}
