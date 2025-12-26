package tpi.dgrv4.gateway.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.codec.utils.IdTokenUtil;
import tpi.dgrv4.codec.utils.IdTokenUtil.IdTokenData;
import tpi.dgrv4.codec.utils.JWKcodec;
import tpi.dgrv4.codec.utils.JWKcodec.JWKVerifyResult;
import tpi.dgrv4.codec.utils.UUID64Util;
import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.DgrAuthCodePhase;
import tpi.dgrv4.common.constant.DgrIdPType;
import tpi.dgrv4.common.constant.TsmpAuthCodeStatus2;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.DgrAuditLogService;
import tpi.dgrv4.entity.entity.DgrGtwIdpAuthCode;
import tpi.dgrv4.entity.entity.DgrGtwIdpAuthM;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoO;
import tpi.dgrv4.entity.entity.OauthClientDetails;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoDao;
import tpi.dgrv4.entity.repository.DgrGtwIdpAuthCodeDao;
import tpi.dgrv4.entity.repository.DgrGtwIdpAuthMDao;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoODao;
import tpi.dgrv4.entity.repository.OauthClientDetailsDao;
import tpi.dgrv4.escape.ESAPI;
import tpi.dgrv4.gateway.component.GtwIdPHelper;
import tpi.dgrv4.gateway.component.IdPHelper;
import tpi.dgrv4.gateway.component.IdPTokenHelper;
import tpi.dgrv4.gateway.component.IdPTokenHelper.TokenData;
import tpi.dgrv4.gateway.component.IdPUserInfoHelper;
import tpi.dgrv4.gateway.component.IdPUserInfoHelper.UserInfoData;
import tpi.dgrv4.gateway.component.IdPWellKnownHelper;
import tpi.dgrv4.gateway.component.IdPWellKnownHelper.WellKnownData;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp2;

/**
 * User在 IdP(GOOGLE / MS / OIDC) 登入成功後,重新導向回來 <br>
 * (GOOGLE / MS / OIDC) <br>
 * @author Mini_
 */
@Service
public class GtwIdPCallbackService {

    private TsmpSettingService tsmpSettingService;
    private GtwIdPAuthService gtwIdPAuthService;
    private ObjectMapper objectMapper;
    private DgrAuditLogService dgrAuditLogService;
    private OAuthTokenService oAuthTokenService;
    private DgrAcIdpInfoDao dgrAcIdpInfoDao;
    private DgrGtwIdpAuthMDao dgrGtwIdpAuthMDao;
    private DgrGtwIdpAuthCodeDao dgrGtwIdpAuthCodeDao;
    private OauthClientDetailsDao oauthClientDetailsDao;
    private TokenHelper tokenHelper;
    private IdPWellKnownHelper idPWellKnownHelper;
    private IdPUserInfoHelper idPUserInfoHelper;
    private IdPTokenHelper idPTokenHelper;
    private GtwIdPHelper gtwIdPHelper;
    private DgrGtwIdpInfoODao dgrGtwIdpInfoODao;
    
    @Autowired
	public GtwIdPCallbackService(TsmpSettingService tsmpSettingService, GtwIdPAuthService gtwIdPAuthService,
			ObjectMapper objectMapper, DgrAuditLogService dgrAuditLogService, OAuthTokenService oAuthTokenService,
			DgrAcIdpInfoDao dgrAcIdpInfoDao, DgrGtwIdpAuthMDao dgrGtwIdpAuthMDao,
			DgrGtwIdpAuthCodeDao dgrGtwIdpAuthCodeDao, OauthClientDetailsDao oauthClientDetailsDao,
			TokenHelper tokenHelper, IdPWellKnownHelper idPWellKnownHelper, IdPUserInfoHelper idPUserInfoHelper,
			IdPTokenHelper idPTokenHelper, GtwIdPHelper gtwIdPHelper, DgrGtwIdpInfoODao dgrGtwIdpInfoODao) {
		super();
		this.tsmpSettingService = tsmpSettingService;
		this.gtwIdPAuthService = gtwIdPAuthService;
		this.objectMapper = objectMapper;
		this.dgrAuditLogService = dgrAuditLogService;
		this.oAuthTokenService = oAuthTokenService;
		this.dgrAcIdpInfoDao = dgrAcIdpInfoDao;
		this.dgrGtwIdpAuthMDao = dgrGtwIdpAuthMDao;
		this.dgrGtwIdpAuthCodeDao = dgrGtwIdpAuthCodeDao;
		this.oauthClientDetailsDao = oauthClientDetailsDao;
		this.tokenHelper = tokenHelper;
		this.idPWellKnownHelper = idPWellKnownHelper;
		this.idPUserInfoHelper = idPUserInfoHelper;
		this.idPTokenHelper = idPTokenHelper;
		this.gtwIdPHelper = gtwIdPHelper;
		this.dgrGtwIdpInfoODao = dgrGtwIdpInfoODao;
	}

	public ResponseEntity<?> gtwIdPCallback(HttpHeaders httpHeaders, HttpServletRequest httpReq,
			HttpServletResponse httpResp, String idPType) throws Exception {
		
		String reqUri = httpReq.getRequestURI();
		String userIp = !StringUtils.hasLength(httpHeaders.getFirst("x-forwarded-for")) ? httpReq.getRemoteAddr()
				: httpHeaders.getFirst("x-forwarded-for");
		String userHostname = httpReq.getRemoteHost();
		
		try {
			ResponseEntity<?> errRespEntity = gtwIdPCallback(httpHeaders, httpReq, httpResp, idPType, reqUri, userIp,
					userHostname);
			if (errRespEntity != null) {// 資料驗證有錯誤
				return errRespEntity;
			}
			
			return null;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			String errMsg = TokenHelper.INTERNAL_SERVER_ERROR;
			TPILogger.tl.error(errMsg);
			return getTokenHelper().getInternalServerErrorResp(reqUri, errMsg);// 500
		}
	}
	
	public ResponseEntity<?> gtwIdPCallback(HttpHeaders httpHeaders, HttpServletRequest httpReq,
			HttpServletResponse httpResp, String idPType, String reqUri,
			String userIp, String userHostname) throws Exception {
		
		ResponseEntity<?> errRespEntity = null;
		if (idPType.equals(DgrIdPType.GOOGLE) // GOOGLE
				|| idPType.equals(DgrIdPType.MS) // MS
				|| idPType.equals(DgrIdPType.OIDC) // OIDC
		) {
			String state = GtwIdPHelper.getStateFromCookies(httpReq, GtwIdPHelper.COOKIE_STATE);// 從 cookies 取得 state 的值
			// 從 cookies 取得 codeVerifier 的值
			String codeVerifierForOauth2 = GtwIdPHelper.getStateFromCookies(httpReq, GtwIdPHelper.COOKIE_CODE_VERIFIER);
			String idPAuthCode = httpReq.getParameter("code");

			errRespEntity = gtwIdPCallback_oauth2(httpResp, idPType, state, idPAuthCode, reqUri, userIp, userHostname,
					codeVerifierForOauth2);
			if (errRespEntity != null) {// 資料驗證有錯誤
				return errRespEntity;
			}
			
		}else {
			// 無效的 idP Type
			String errMsg = String.format(IdPHelper.MSG_INVALID_IDPTYPE, idPType);
			TPILogger.tl.debug(errMsg);
			errRespEntity = new ResponseEntity<OAuthTokenErrorResp2>(
					getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
					HttpStatus.BAD_REQUEST);// 400
		}
		
		return null;
	}
  
	/**
	 * for GOOGLE / MS / OIDC IdP 流程
	 */
	public ResponseEntity<?> gtwIdPCallback_oauth2(HttpServletResponse httpResp, String idPType, String state,
			String idPAuthCode, String reqUri, String userIp, String userHostname, String codeVerifierForOauth2)
			throws Exception {
		
		//checkmarx, Reflected XSS All Clients 
		reqUri = ESAPI.encoder().encodeForHTML(reqUri);
		idPType = ESAPI.encoder().encodeForHTML(idPType);
		state = ESAPI.encoder().encodeForHTML(state);

		// 1.檢查傳入的資料
		ResponseEntity<?> errRespEntity = getGtwIdPHelper().checkCookieParam(state, reqUri);
		if (errRespEntity != null) {// 資料驗證有錯誤
			return errRespEntity;
		}
		
		errRespEntity = checkReqParam(idPType, idPAuthCode);
		if (errRespEntity != null) {// 資料驗證有錯誤
			return errRespEntity;
		}
    	
		// 2.依 state 取得 dgR 的 client_id
    	// DGR_GTW_IDP_AUTH_M (Gateway IdP Auth記錄檔主檔)
		DgrGtwIdpAuthM dgrGtwIdpAuthM = getDgrGtwIdpAuthMDao().findFirstByState(state);
		if(dgrGtwIdpAuthM == null) {
			//Table [DGR_GTW_IDP_AUTH_M] 查不到資料
			String errMsg = "Table [DGR_GTW_IDP_AUTH_M] can't find data, state:" + state;
			TPILogger.tl.debug();
			errMsg = "The state was not found. state: " + state;
			TPILogger.tl.debug(errMsg);
			return getTokenHelper().getUnauthorizedErrorResp(reqUri, errMsg);// 401
		}
		
		String dgrClientRedirectUri = dgrGtwIdpAuthM.getRedirectUri();// TSP 在發出授權請求(auth API)時,傳入的 redirect_uri
		if (!StringUtils.hasLength(dgrClientRedirectUri)) {
			// 設定檔缺少參數
			String errMsg = TokenHelper.THE_PROFILE_IS_MISSING_PARAMETERS + "DGR_GTW_IDP_AUTH_M.redirect_uri";
			TPILogger.tl.debug(errMsg);
			errRespEntity = getTokenHelper().getUnauthorizedErrorResp(reqUri, errMsg);// 401
			return errRespEntity;
		}
		
		String dgrClientId = dgrGtwIdpAuthM.getClientId();
    	
    	// 3.取得 dgR client 對應的 IdP 相關資料
		// 取得狀態為 "Y",且建立時間最新的
		String status = "Y";
		DgrGtwIdpInfoO dgrGtwIdpInfoO = getDgrGtwIdpInfoODao()
				.findFirstByClientIdAndIdpTypeAndStatusOrderByCreateDateTimeDesc(dgrClientId, idPType, status);

		if (dgrGtwIdpInfoO == null) {
			// Table [DGR_GTW_IDP_INFO_O] 查不到資料
			TPILogger.tl.debug("Table [DGR_GTW_IDP_INFO_O] can't find data. dgrClientId: " + dgrClientId
					+ ", idpType: " + idPType + ", status: " + status);
			// 設定檔缺少參數
			String errMsg = TokenHelper.THE_PROFILE_IS_MISSING_PARAMETERS + "GTW IdP Info";
			TPILogger.tl.debug(errMsg);
			errRespEntity = getTokenHelper().getUnauthorizedErrorResp(reqUri, errMsg);// 401
			return errRespEntity;
		}

		String idPClientId = dgrGtwIdpInfoO.getIdpClientId();
		String idPClientMima = dgrGtwIdpInfoO.getIdpClientMima();
		String idPWellKnownUrl = dgrGtwIdpInfoO.getWellKnownUrl();
		String dgrCallbackUrl = dgrGtwIdpInfoO.getCallbackUrl();
		String idPAccessTokenUrl = dgrGtwIdpInfoO.getAccessTokenUrl();

		if (!StringUtils.hasLength(idPWellKnownUrl)) {
			// 設定檔缺少參數
			String errMsg = TokenHelper.THE_PROFILE_IS_MISSING_PARAMETERS + "wellKnownUrl";
			TPILogger.tl.debug(errMsg);
			errRespEntity = getTokenHelper().getUnauthorizedErrorResp(reqUri, errMsg);// 401
			return errRespEntity;
		}

		if (!StringUtils.hasLength(dgrCallbackUrl)) {
			// 設定檔缺少參數
			String errMsg = TokenHelper.THE_PROFILE_IS_MISSING_PARAMETERS + "callbackUrl";
			TPILogger.tl.debug(errMsg);
			errRespEntity = getTokenHelper().getUnauthorizedErrorResp(reqUri, errMsg);// 401
			return errRespEntity;
		}
 
		// 4.打 IdP(GOOGLE / MS / OIDC) Well Known URL, 取得 JSON 資料
		WellKnownData wellKnownData = getIdPWellKnownHelper().getWellKnownData(idPWellKnownUrl,
				reqUri);
		errRespEntity = wellKnownData.errRespEntity;
		if(errRespEntity != null) {
			return errRespEntity;
		}
		
		// 5.由 Well Known JSON 中取得資料
		
		// 若 accessTokenUrl 沒有值, 則從 Well Known 取得
		if (!StringUtils.hasLength(idPAccessTokenUrl)) {
			idPAccessTokenUrl = wellKnownData.tokenEndpoint;
		}

		if (!StringUtils.hasLength(idPAccessTokenUrl)) {
			// 設定檔缺少參數
			String errMsg = TokenHelper.THE_PROFILE_IS_MISSING_PARAMETERS + "accessTokenUrl";
			TPILogger.tl.debug(errMsg);
			return getTokenHelper().getUnauthorizedErrorResp(reqUri, errMsg);// 401
		}
		
		// 取得 Jwks Uri
		String jwksUri = wellKnownData.jwksUri;
		if (!StringUtils.hasLength(jwksUri)) {
			// 設定檔缺少參數
			String errMsg = TokenHelper.THE_PROFILE_IS_MISSING_PARAMETERS + "jwksUri";
			TPILogger.tl.debug(errMsg);
			return getTokenHelper().getUnauthorizedErrorResp(reqUri, errMsg);// 401
		}
		
		// 取得 issuer
		String issuer = wellKnownData.issuer;
		if (!StringUtils.hasLength(issuer)) {
			// 設定檔缺少參數
			String errMsg = TokenHelper.THE_PROFILE_IS_MISSING_PARAMETERS + "issuer";
			TPILogger.tl.debug(errMsg);
			return getTokenHelper().getUnauthorizedErrorResp(reqUri, errMsg);// 401
		}
		
		// 6.打 IdP(GOOGLE / MS / OIDC) 的 token API, 取得 Access Token 和 ID Token
		TokenData tokenData = getIdPTokenHelper().getTokenData(idPType, idPClientId, idPClientMima, idPAccessTokenUrl,
				dgrCallbackUrl, idPAuthCode, codeVerifierForOauth2, reqUri);
		errRespEntity = tokenData.errRespEntity;
		if (errRespEntity != null) {
			return errRespEntity;
		}
		
		String idTokenJwtstr = tokenData.idToken;
		String accessTokenJwtstr = tokenData.accessToken;
		String refreshTokenJwtstr = tokenData.refreshToken;
		String apiResp = tokenData.apiResp;
 
		if(!StringUtils.hasLength(idTokenJwtstr)) {
			// 設定檔缺少參數
			String errMsg = TokenHelper.THE_PROFILE_IS_MISSING_PARAMETERS + "idTokenJwtstr";
			TPILogger.tl.debug(errMsg);
			return getTokenHelper().getUnauthorizedErrorResp(reqUri, errMsg);// 401
		}
		
		// 7.驗證 IdP(GOOGLE / MS / OIDC) ID Token
		boolean isVerify = false;            
		JWKVerifyResult jwkRs = JWKcodec.verifyJWStoken(idTokenJwtstr, jwksUri, issuer);
		isVerify = jwkRs.verify;
		TPILogger.tl.debug(idPType + " ID token verify : " + isVerify);
		
		String userName = null;
		String userEmail = null;
		String userAlias = null;
		String userPicture = null;
		
		if (isVerify) {// 驗證 ID Token 成功
			// 8.1.取得 IdP ID Token 中的 sub, name, email, picture
			IdTokenData idTokenData = IdTokenUtil.getIdTokenData(idTokenJwtstr);
			userName = idTokenData.userName;
			userAlias = idTokenData.userAlias; 
			userEmail = idTokenData.userEmail;
			userPicture = idTokenData.userPicture;
			
		}else {// 驗證 ID Token 失敗, 再用 access token 打 UserInfo 取得 email, 若仍沒有值, 才顯示錯誤訊息
			// 8.2.打 IdP 的 UserInfo API, 取得 sub, name 和 email
			
			// 取得 UserInfo URL
			String userinfoUrl = wellKnownData.userinfoEndpoint;
			if(!StringUtils.hasLength(userinfoUrl)) {
				// 設定檔缺少參數
				String errMsg = TokenHelper.THE_PROFILE_IS_MISSING_PARAMETERS + "userinfoUrl";
				TPILogger.tl.debug(errMsg);
				return getTokenHelper().getUnauthorizedErrorResp(reqUri, errMsg);// 401
			}
			
			// 打 UserInfo API, 取得 UserInfo 中的 sub, name, email, picture
			UserInfoData userInfoData = getIdPUserInfoHelper().getUserInfoData(userinfoUrl, accessTokenJwtstr, reqUri);
			errRespEntity = userInfoData.errRespEntity;
			if(errRespEntity != null) {
				return errRespEntity;
			}
			userName = userInfoData.userName;
			userAlias = userInfoData.userAlias; 
			userEmail = userInfoData.userEmail;
			userPicture = userInfoData.userPicture;
		}
		
		if(!StringUtils.hasLength(userName)) {
			String errMsg = "ID token verification failed";
			TPILogger.tl.debug(errMsg);
			
			//寫入 Audit Log M,登入失敗
			//GTW IdP 先不用記錄
//			String lineNumber = StackTraceUtil.getLineNumber();
//			createAuditLogMForLoginFailed(reqUri, lineNumber, userIp, userHostname, errMsg, idPType, userName, userAlias);
			
			return getTokenHelper().getUnauthorizedErrorResp(reqUri, errMsg);// 401
		}

		// 9.產生 dgRcode
		String dgRcode = UUID64Util.UUID64(UUID.randomUUID());// 產生 dgRcode(UUID 64位元)

		// 10.將 dgRcode 更新到 DGR_GTW_IDP_AUTH_M (Gateway IdP Auth記錄檔主檔)
		dgrGtwIdpAuthM.setAuthCode(dgRcode);
		dgrGtwIdpAuthM.setUpdateUser("SYSTEM");
		dgrGtwIdpAuthM.setUpdateDateTime(DateTimeUtil.now());
		dgrGtwIdpAuthM = getDgrGtwIdpAuthMDao().save(dgrGtwIdpAuthM);
		
		// 11.儲存 dgRcode (auth code 授權碼)
		// 建立 DGR_GTW_IDP_AUTH_CODE (Gateway IdP授權碼記錄檔)
		Date expiredTime = IdPHelper.getAuthCodeExpiredTime();// 授權碼的到期時間
		long expireDateTime = expiredTime.getTime();
		createDgrGtwIdpAuthCode(state, dgRcode, DgrAuthCodePhase.AUTH_CODE, expireDateTime, idPType, dgrClientId,
				userName, userAlias, userEmail, userPicture, idTokenJwtstr, accessTokenJwtstr, refreshTokenJwtstr, apiResp,
				null, null);

		// 12.重新轉導 302 到租戶, dgR client 的 redirect url
		Optional<OauthClientDetails> opt_authClientDetails = getOauthClientDetailsDao().findById(dgrClientId);
		if (!opt_authClientDetails.isPresent()) {
			// Table [OAUTH_CLIENT_DETAILS] 查不到 client
			return getTokenHelper().getFindOauthClientDetailsError(dgrClientId, reqUri);
		}
 
		String redirectUrl = String.format(
				"%s" 
				+ "?code=%s" 
				+ "&state=%s",
				dgrClientRedirectUri, 
				dgRcode ,
				URLEncoder.encode(state, StandardCharsets.UTF_8.toString()) // 編碼
		);
		TPILogger.tl.debug("Redirect to URL【dgR Client 的 Redirect URL】: " + redirectUrl);
		httpResp.sendRedirect(redirectUrl);
		
		return null;
    }
	
	/**
	 * 檢查傳入的資料
	 */
	private ResponseEntity<?> checkReqParam(String idPType, String idPAuthCode) {
		// 沒有支援的 idPType
		ResponseEntity<?> errRespEntity = getTokenHelper().checkSupportGtwIdPType(idPType);
		if (errRespEntity != null) {// idPType 資料驗證有錯誤
			return errRespEntity;
		}
		
		// 沒有 code
		if (!StringUtils.hasLength(idPAuthCode)) {
			String errMsg = TokenHelper.MISSING_REQUIRED_PARAMETER + "code";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp2>(
					getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
					HttpStatus.BAD_REQUEST);// 400
		}
 
		return null;
	}
 
	/**
     * 建立/更新 DGR_GTW_IDP_AUTH_CODE (Gateway IdP授權碼記錄檔)
     */
	public DgrGtwIdpAuthCode createDgrGtwIdpAuthCode(String state, String dgRcode, String phase, long expireDateTime,
			String idPType, String dgrClientId, String userName, String userAlias, String userEmail, String userPicture,
			String idTokenJwtstr, String accessTokenJwtstr, String refreshTokenJwtstr, String apiResp,
			String idtLightId, String idtRoleName) {

		String codeStatus = TsmpAuthCodeStatus2.AVAILABLE.value();// 可用
		
//		Date expiredTime = IdPHelper.getCodeExpiredTime();//到期時間,現在時間+30秒
//		Long expireDateTime = expiredTime.getTime();
		
		DgrGtwIdpAuthCode dgrGtwIdpAuthCode = new DgrGtwIdpAuthCode();
		
		if (DgrAuthCodePhase.STATE.equals(phase)) {
			dgrGtwIdpAuthCode.setAuthCode(state);
		} else if (DgrAuthCodePhase.AUTH_CODE.equals(phase)) {
			dgrGtwIdpAuthCode.setAuthCode(dgRcode);
		}

		if (StringUtils.hasLength(state)) {
			dgrGtwIdpAuthCode.setState(state);// 寫入state
		}
		
		dgrGtwIdpAuthCode.setPhase(phase);
		dgrGtwIdpAuthCode.setStatus(codeStatus);
		dgrGtwIdpAuthCode.setExpireDateTime(expireDateTime);
		dgrGtwIdpAuthCode.setIdpType(idPType);
		dgrGtwIdpAuthCode.setClientId(dgrClientId);
		dgrGtwIdpAuthCode.setUserName(userName);
		dgrGtwIdpAuthCode.setUserAlias(userAlias);
		dgrGtwIdpAuthCode.setUserEmail(userEmail);
		dgrGtwIdpAuthCode.setUserPicture(userPicture);
		dgrGtwIdpAuthCode.setIdTokenJwtstr(idTokenJwtstr);
		dgrGtwIdpAuthCode.setAccessTokenJwtstr(accessTokenJwtstr);
		dgrGtwIdpAuthCode.setRefreshTokenJwtstr(refreshTokenJwtstr);
		dgrGtwIdpAuthCode.setApiResp(apiResp);
		dgrGtwIdpAuthCode.setUserLightId(idtLightId);
		dgrGtwIdpAuthCode.setUserRoleName(idtRoleName);
		dgrGtwIdpAuthCode.setCreateUser("SYSTEM");
		dgrGtwIdpAuthCode.setCreateDateTime(DateTimeUtil.now());
		dgrGtwIdpAuthCode = getDgrGtwIdpAuthCodeDao().save(dgrGtwIdpAuthCode);
    	return dgrGtwIdpAuthCode;
	}
 
	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}
	 
    protected GtwIdPAuthService getGtwIdPAuthService() {
    	return gtwIdPAuthService;
    }
    
    protected ObjectMapper getObjectMapper() {
    	return objectMapper;
    }
    
	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}
	
	protected OAuthTokenService getOAuthTokenService() {
		return oAuthTokenService;
	}
	
	protected DgrAcIdpInfoDao getDgrAcIdpInfoDao() {
		return dgrAcIdpInfoDao;
	}
	
	protected DgrGtwIdpAuthMDao getDgrGtwIdpAuthMDao() {
		return dgrGtwIdpAuthMDao;
	}
	
    protected DgrGtwIdpAuthCodeDao getDgrGtwIdpAuthCodeDao() {
        return dgrGtwIdpAuthCodeDao;
    }
    
	protected OauthClientDetailsDao getOauthClientDetailsDao() {
		return oauthClientDetailsDao;
	}
	
	protected TokenHelper getTokenHelper() {
		return tokenHelper;
	}
	
	protected IdPWellKnownHelper getIdPWellKnownHelper() {
		return idPWellKnownHelper;
	}
	
	protected IdPUserInfoHelper getIdPUserInfoHelper() {
		return idPUserInfoHelper;
	}
	
	protected IdPTokenHelper getIdPTokenHelper() {
		return idPTokenHelper;
	}
 
	protected GtwIdPHelper getGtwIdPHelper() {
		return gtwIdPHelper;
	}
	
	protected DgrGtwIdpInfoODao getDgrGtwIdpInfoODao() {
		return dgrGtwIdpInfoODao;
	}
}
