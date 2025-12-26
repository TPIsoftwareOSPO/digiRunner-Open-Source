package tpi.dgrv4.gateway.service;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.constant.DgrIdPType;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.DgrGtwIdpAuthD;
import tpi.dgrv4.entity.entity.DgrGtwIdpAuthM;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoO;
import tpi.dgrv4.entity.repository.DgrGtwIdpAuthDDao;
import tpi.dgrv4.entity.repository.DgrGtwIdpAuthMDao;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoODao;
import tpi.dgrv4.escape.ESAPI;
import tpi.dgrv4.gateway.component.GtwIdPHelper;
import tpi.dgrv4.gateway.component.IdPHelper;
import tpi.dgrv4.gateway.component.IdPWellKnownHelper;
import tpi.dgrv4.gateway.component.IdPWellKnownHelper.WellKnownData;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class GtwIdPAuthService {

	private ObjectMapper objectMapper;
	private TsmpSettingService tsmpSettingService;
	private TokenHelper tokenHelper;
	private DgrGtwIdpAuthMDao dgrGtwIdpAuthMDao;
	private DgrGtwIdpAuthDDao dgrGtwIdpAuthDDao;
	private DgrGtwIdpInfoODao dgrGtwIdpInfoODao; 
	private GtwIdPHelper gtwIdPHelper; 
	private IdPWellKnownHelper idPWellKnownHelper;
	
	@Autowired
	public GtwIdPAuthService(ObjectMapper objectMapper, TsmpSettingService tsmpSettingService, TokenHelper tokenHelper,
			DgrGtwIdpAuthMDao dgrGtwIdpAuthMDao, DgrGtwIdpAuthDDao dgrGtwIdpAuthDDao,
			DgrGtwIdpInfoODao dgrGtwIdpInfoODao, GtwIdPHelper gtwIdPHelper, IdPWellKnownHelper idPWellKnownHelper) {
		super();
		this.objectMapper = objectMapper;
		this.tsmpSettingService = tsmpSettingService;
		this.tokenHelper = tokenHelper;
		this.dgrGtwIdpAuthMDao = dgrGtwIdpAuthMDao;
		this.dgrGtwIdpAuthDDao = dgrGtwIdpAuthDDao;
		this.dgrGtwIdpInfoODao = dgrGtwIdpInfoODao;
		this.gtwIdPHelper = gtwIdPHelper;
		this.idPWellKnownHelper = idPWellKnownHelper;
	}

	public ResponseEntity<?> gtwIdPAuth(HttpHeaders headers, HttpServletRequest httpReq, HttpServletResponse httpResp, 
			String idPType) throws Exception {
		
		String reqUri = httpReq.getRequestURI();
		
		// TSP 在發出授權請求(authorization API),傳入的 redirect_uri 值
		String dgrClientRedirectUri = httpReq.getParameter("redirect_uri");
		ResponseEntity<?> errRespEntity = null;
		try {
			String responseType = httpReq.getParameter("response_type");
			String dgrClientId = httpReq.getParameter("client_id");
			String oidcScopeStr = httpReq.getParameter("scope");
			String state = httpReq.getParameter("state");
 
			// PKCE
			String codeChallenge = httpReq.getParameter("code_challenge");
			String codeChallengeMethod = httpReq.getParameter("code_challenge_method");
			
			// 檢查傳入的資料
			errRespEntity = checkReqParam(idPType, responseType, dgrClientId, oidcScopeStr, dgrClientRedirectUri, state,
					codeChallenge, codeChallengeMethod, reqUri);
			if (errRespEntity != null) {// 資料驗證有錯誤
				return errRespEntity;
			}
			
			if (DgrIdPType.GOOGLE.equals(idPType) // GOOGLE
					|| DgrIdPType.MS.equals(idPType) // MS
					|| DgrIdPType.OIDC.equals(idPType) // OIDC
			) {
				long maxAge = 60L * 5L;// 以秒為單位, 設定5分鐘
				// state 寫入 cookie
				ResponseCookie stateCookie = TokenHelper.createCookie(GtwIdPHelper.COOKIE_STATE, state, maxAge);
				httpResp.addHeader(HttpHeaders.SET_COOKIE, stateCookie.toString());
				//checkmarx, Missing HSTS Header
				httpResp.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload"); 
		        
				// codeVerifier 寫入 cookie
				String codeVerifierForOauth2 = UUID.randomUUID().toString() + UUID.randomUUID().toString();// for GOOGLE / MS
				ResponseCookie codeVerifierCookie = TokenHelper.createCookie(GtwIdPHelper.COOKIE_CODE_VERIFIER,
						codeVerifierForOauth2, maxAge);
				httpResp.addHeader(HttpHeaders.SET_COOKIE, codeVerifierCookie.toString());

				errRespEntity = gtwIdPAuth_oauth2(httpResp, responseType, dgrClientRedirectUri, idPType, dgrClientId,
						state, oidcScopeStr, codeChallenge, codeChallengeMethod, codeVerifierForOauth2, reqUri);
				if (errRespEntity != null) {// 資料驗證有錯誤
					return errRespEntity;
				}

			} else if (DgrIdPType.JDBC.equals(idPType) // JDBC
					|| DgrIdPType.LDAP.equals(idPType) // LDAP
					|| DgrIdPType.API.equals(idPType) // API
			) {
				// state 用參數傳遞, 不使用 cookie
				errRespEntity = gtwIdPAuth_other(httpResp, responseType, dgrClientRedirectUri, idPType,
						dgrClientId, state, oidcScopeStr, codeChallenge, codeChallengeMethod, reqUri);
				if (errRespEntity != null) {// 資料驗證有錯誤
					return errRespEntity;
				}
			}
			
			return null;
			
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			String errMsg = TokenHelper.INTERNAL_SERVER_ERROR;
			TPILogger.tl.error(errMsg);
			errRespEntity = getTokenHelper().getInternalServerErrorResp(reqUri, errMsg);// 500
			return errRespEntity;
		}
	}
	
	/**
	 * for Oauth2.0 GOOGLE / MS IdP 流程
	 */
	public ResponseEntity<?> gtwIdPAuth_oauth2(HttpServletResponse httpResp, String responseType,
			String dgrClientRedirectUri, String idPType, String dgrClientId, String state, String dgrScopeStr,
			String codeChallenge, String codeChallengeMethod, String codeVerifierForOauth2, String reqUri) throws Exception {

		ResponseEntity<?> errRespEntity = null;
		//checkmarx, Reflected XSS All Clients 
		reqUri = ESAPI.encoder().encodeForHTML(reqUri);
		
		// 1.取得 dgR client 對應的 IdP 相關資料
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
		String idPWellKnownUrl = dgrGtwIdpInfoO.getWellKnownUrl();
		String dgrCallbackUrl = dgrGtwIdpInfoO.getCallbackUrl();
		String idPAuthUrl = dgrGtwIdpInfoO.getAuthUrl();
		String idPScopeStr = dgrGtwIdpInfoO.getScope();

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
 
		// 2.打 IdP Well Known URL, 取得 JSON 資料
		WellKnownData wellKnownData = null;
		if (!StringUtils.hasLength(idPAuthUrl) 
				|| !StringUtils.hasLength(idPScopeStr)) {
			wellKnownData = getIdPWellKnownHelper().getWellKnownData(idPWellKnownUrl, reqUri);
			errRespEntity = wellKnownData.errRespEntity;
			if (errRespEntity != null) {
				return errRespEntity;
			}
			
			// 3.由 IdP Well Known JSON 中取得資料
			// 若 authUrl 沒有值, 則從 Well Known 取得
			if (!StringUtils.hasLength(idPAuthUrl)) {
				idPAuthUrl = wellKnownData.authorizationEndpoint;
			}
			
			// 若 scope 沒有值, 則從 Well Known 取得
			if (!StringUtils.hasLength(idPScopeStr)) {
				idPScopeStr = wellKnownData.scopeStr;
			}
		}
		
		if (!StringUtils.hasLength(idPAuthUrl)) {
			// 設定檔缺少參數
			String errMsg = TokenHelper.THE_PROFILE_IS_MISSING_PARAMETERS + "authUrl";
			TPILogger.tl.debug(errMsg);
			return getTokenHelper().getUnauthorizedErrorResp(reqUri, errMsg);// 401
		}
		
		if (!StringUtils.hasLength(idPScopeStr)) {
			// 設定檔缺少參數
			String errMsg = TokenHelper.THE_PROFILE_IS_MISSING_PARAMETERS + "scope";
			TPILogger.tl.debug(errMsg);
			return getTokenHelper().getUnauthorizedErrorResp(reqUri, errMsg);// 401
		}
		
		// 4.建立 DGR_GTW_IDP_AUTH_M (Gateway IdP Auth記錄檔主檔)
		String dgRcode = null;// 此時還沒有 dgRcode
		DgrGtwIdpAuthM dgrGtwIdpAuthM = createDgrGtwIdpAuthM(state, dgrClientId, idPType, dgRcode, dgrClientRedirectUri,
				codeChallenge, codeChallengeMethod);
		long gtwIdpAuthMId = dgrGtwIdpAuthM.getGtwIdpAuthMId();
		
		// 建立 DGR_GTW_IDP_AUTH_D
		createDgrGtwIdpAuthD(gtwIdpAuthMId, dgrScopeStr);

		// 5.重新導向到 IdP(GOOGLE / MS) 的 Auth API(使用 PKCE), 到 OAuth 同意畫面
		String redirectUrl = IdPHelper.getRedirectUrl(idPClientId, idPAuthUrl, idPScopeStr, dgrCallbackUrl, codeVerifierForOauth2);
		TPILogger.tl.debug("Redirect to URL【" + idPType + " IdP Auth URL】: " + redirectUrl);
		httpResp.sendRedirect(redirectUrl);
		
		return null;
	}
	
	/**
	 *  for JDBC / LDAP / API IdP 流程 
	 */
	public ResponseEntity<?> gtwIdPAuth_other(HttpServletResponse httpResp, String responseType,
			String dgrClientRedirectUri, String idPType, String dgrClientId, String state, String dgrScopeStr,
			String codeChallenge, String codeChallengeMethod, String reqUri) throws Exception {
		
		// 取得 user 登入畫面 URL
		String dgrLoginUiUrl = getLoginUiUrl(idPType);
		
		URL urlObj = new URL(dgrLoginUiUrl);
		dgrLoginUiUrl = urlObj.getPath();// 使用相對路徑
		
		String redirectUrl = String.format(
				"%s" 
				+ "?response_type=%s" 
				+ "&client_id=%s" 
				+ "&scope=%s" 
				+ "&redirect_uri=%s"
				+ "&state=%s", 
				dgrLoginUiUrl, 
				URLEncoder.encode(responseType, StandardCharsets.UTF_8.toString()),
				URLEncoder.encode(dgrClientId, StandardCharsets.UTF_8.toString()),
				URLEncoder.encode(dgrScopeStr, StandardCharsets.UTF_8.toString()),
				URLEncoder.encode(dgrClientRedirectUri, StandardCharsets.UTF_8.toString()),
				URLEncoder.encode(state, StandardCharsets.UTF_8.toString())
		);
		
		if (StringUtils.hasLength(codeChallenge)) {// 如果有值,加到URL
			redirectUrl = String.format(
				"%s" 
				+ "&code_challenge=%s", 
				redirectUrl, 
				URLEncoder.encode(codeChallenge, StandardCharsets.UTF_8.toString())
			);
		}
		
		if (StringUtils.hasLength(codeChallengeMethod)) {// 如果有值,加到URL
			redirectUrl = String.format(
				"%s" 
				+ "&code_challenge_method=%s", 
				redirectUrl, 
				URLEncoder.encode(codeChallengeMethod, StandardCharsets.UTF_8.toString())
			);
		}
		
		// 轉導到 user 登入畫面
		TPILogger.tl.debug("Redirect to URL【User Login UI URL】: " + redirectUrl);
		httpResp.sendRedirect(redirectUrl);
		
		return null;
	}
	
	/**
	 * for JDBC / LDAP / API IdP <br> 
	 * 取得 user login 畫面 URL <br>
	 * 例如: <br>
	 * https://localhost:8080/dgrv4/mockac/gtwidp/{idPType}/loginui <br>
	 * https://localhost:8080/dgrv4/mockac/gtwidp/JDBC/loginui <br>
	 */
	public String getLoginUiUrl(String idPType) {
		// 前端GTW IdP User登入畫面的URL
		// TODO, Mini,(要恢復)
		String dgrLoginUrl = getTsmpSettingService().getVal_GTW_IDP_LOGIN_URL();
		
		// TODO, Mini, test測試用
//		String dgrLoginUrl = "https://localhost:8080/dgrv4/ac4/gtwidp/{idPType}/login";
		
		// TODO, Mini, test (前端未加入畫面前測試用)
//		String dgrLoginUrl = "https://localhost:18080/dgrv4/mockac/gtwidp/{idPType}/loginui";
		
		dgrLoginUrl = dgrLoginUrl.replace("{idPType}", idPType);
		
		return dgrLoginUrl;
	}

	/**
	 * 檢查傳入的資料
	 */
	private ResponseEntity<?> checkReqParam(String idPType, String responseType, String dgrClientId,
			String oidcScopeStr, String dgrClientRedirectUri, String state, String codeChallenge,
			String codeChallengeMethod, String reqUri) {

		// 檢查 idPType 是否為支援的 IdP type
		ResponseEntity<?> errRespEntity = getTokenHelper().checkSupportGtwIdPType(idPType);
		if (errRespEntity != null) {// idPType 資料驗證有錯誤
			return errRespEntity;
		}

		// 檢查 response_type 是否有值 且 正確的值 "code"
		errRespEntity = getTokenHelper().checkResponseType(responseType);
		if (errRespEntity != null) {// response_type 資料驗證有錯誤
			return errRespEntity;
		}
		
		// 檢查是否有 code_challenge 和 code_challenge_method
		errRespEntity = getGtwIdPHelper().checkCodeChallengeParam(codeChallenge, codeChallengeMethod);
		if (errRespEntity != null) {// 資料驗證有錯誤
			return errRespEntity;
		}

		// 檢查 scope 是否為支援的 OpenID Connect scopes
		errRespEntity = getTokenHelper().checkOidcScope(oidcScopeStr);
		if (errRespEntity != null) {// scope 資料驗證有錯誤
			return errRespEntity;
		}
		
		// 檢查 state 是否已存在
		errRespEntity = getGtwIdPHelper().checkStateExists(state, reqUri);
		if (errRespEntity != null) {// state 資料驗證有錯誤
			return errRespEntity;
		}
		
		// 檢查傳入的 redirectUri 和 client 註冊在系統中的是否相同
		errRespEntity = getTokenHelper().checkRedirectUri(dgrClientId, dgrClientRedirectUri, reqUri);
		if (errRespEntity != null) {// redirectUri 驗證有錯誤
			return errRespEntity;
		}
 
		errRespEntity = getTokenHelper().checkClientStatus(dgrClientId, reqUri);
		if (errRespEntity != null) {// client 資料驗證有錯誤
			return errRespEntity;
		}

		return null;
	}
  
	/**
	 * 建立 DGR_GTW_IDP_AUTH_M (Gateway IdP Auth記錄檔主檔)
	 */
	public DgrGtwIdpAuthM createDgrGtwIdpAuthM(String state, String clientId, String idPType, String dgRcode,
			String dgrClientRedirectUri, String codeChallenge, String codeChallengeMethod) {
		DgrGtwIdpAuthM dgrGtwIdpAuthM = new DgrGtwIdpAuthM();
		dgrGtwIdpAuthM.setState(state);
		dgrGtwIdpAuthM.setIdpType(idPType);
		dgrGtwIdpAuthM.setClientId(clientId);
		dgrGtwIdpAuthM.setAuthCode(dgRcode);
		dgrGtwIdpAuthM.setRedirectUri(dgrClientRedirectUri);
		dgrGtwIdpAuthM.setCodeChallenge(codeChallenge);
		dgrGtwIdpAuthM.setCodeChallengeMethod(codeChallengeMethod);

		dgrGtwIdpAuthM.setCreateUser("SYSTEM");
		dgrGtwIdpAuthM.setCreateDateTime(DateTimeUtil.now());

		dgrGtwIdpAuthM = getDgrGtwIdpAuthMDao().save(dgrGtwIdpAuthM);
		return dgrGtwIdpAuthM;
	}
	
	/**
	 * 建立 DGR_GTW_IDP_AUTH_D (Gateway IdP Auth記錄檔明細檔)
	 */
	public void createDgrGtwIdpAuthD(long refGtwIdpAuthMId, String scopeStr) {
		String[] scopeArr = scopeStr.split(" ");
		for (String scope : scopeArr) {
			if (StringUtils.hasLength(scope)) {// 有值才寫入
				DgrGtwIdpAuthD dgrGtwIdpAuthD = new DgrGtwIdpAuthD();
				dgrGtwIdpAuthD.setRefGtwIdpAuthMId(refGtwIdpAuthMId);
				dgrGtwIdpAuthD.setScope(scope);

				dgrGtwIdpAuthD.setCreateUser("SYSTEM");
				dgrGtwIdpAuthD.setCreateDateTime(DateTimeUtil.now());
				dgrGtwIdpAuthD = getDgrGtwIdpAuthDDao().save(dgrGtwIdpAuthD);
			}
		}
	}
	
	protected ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}
 
	protected TokenHelper getTokenHelper() {
		return tokenHelper;
	}
	
	protected DgrGtwIdpAuthMDao getDgrGtwIdpAuthMDao() {
		return dgrGtwIdpAuthMDao;
	}
	
	protected DgrGtwIdpAuthDDao getDgrGtwIdpAuthDDao() {
		return dgrGtwIdpAuthDDao;
	}
 
	protected IdPWellKnownHelper getIdPWellKnownHelper() {
		return idPWellKnownHelper;
	}
 
	protected DgrGtwIdpInfoODao getDgrGtwIdpInfoODao() {
		return dgrGtwIdpInfoODao;
	}
 
	protected GtwIdPHelper getGtwIdPHelper() {
		return gtwIdPHelper;
	}
}
