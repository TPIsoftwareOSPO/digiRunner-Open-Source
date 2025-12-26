package tpi.dgrv4.gateway.service;

import java.net.URL;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.codec.utils.JWEcodec;
import tpi.dgrv4.common.constant.DgrAuthCodePhase;
import tpi.dgrv4.common.constant.DgrIdPType;
import tpi.dgrv4.common.utils.ServiceUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.component.cipher.TsmpCoreTokenEntityHelper;
import tpi.dgrv4.entity.component.cipher.TsmpTAEASKHelper;
import tpi.dgrv4.entity.entity.DgrGtwIdpAuthM;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoA;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoJdbc;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoL;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoADao;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoJdbcDao;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoLDao;
import tpi.dgrv4.escape.ESAPI;
import tpi.dgrv4.gateway.component.GtwIdPHelper;
import tpi.dgrv4.gateway.component.IdPApiHelper;
import tpi.dgrv4.gateway.component.IdPApiHelper.ApiUserInfoData;
import tpi.dgrv4.gateway.component.IdPHelper;
import tpi.dgrv4.gateway.component.IdPJdbcHelper;
import tpi.dgrv4.gateway.component.IdPUserInfoHelper.UserInfoData;
import tpi.dgrv4.gateway.component.LdapHelper;
import tpi.dgrv4.gateway.component.LdapHelper.LdapAdAuthData;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.JsonNodeUtil;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp2;

/**
 * 驗證傳入的User帳號、密碼 <br>
 * (LDAP / API / JDBC) <br>
 * 
 * @author Mini
 */
@Service
public class GtwIdPLoginService {

	private ObjectMapper objectMapper;
	private TsmpSettingService tsmpSettingService;
	private TokenHelper tokenHelper;
	private IdPHelper idPHelper;
	private GtwIdPCallbackService gtwIdPCallbackService;
	private GtwIdPAuthService gtwIdPAuthService;
	private LdapHelper ldapHelper;
	private DgrGtwIdpInfoLDao dgrGtwIdpInfoLDao;
	private DgrGtwIdpInfoJdbcDao dgrGtwIdpInfoJDao;
	private DgrGtwIdpInfoADao dgrGtwIdpInfoADao;
	private GtwIdPHelper gtwIdPHelper;
	private TsmpTAEASKHelper tsmpTAEASKHelper;
	private IdPJdbcHelper idPJdbcHelper;
	private IdPApiHelper idPApiHelper;
	private DgrGtwIdpInfoJdbcDao dgrGtwIdpInfoJdbcDao;	
	private TsmpCoreTokenEntityHelper tsmpCoreTokenEntityHelper;
	
	@Value("${digi.url.prefix:}")
    private String urlPrefix;

	public static class UserLoginData {
		public ResponseEntity<?> errRespEntity;
		public String errMsg;
		public String userName;
		public String userMima;
	}
	
	@Autowired
	public GtwIdPLoginService(ObjectMapper objectMapper, TsmpSettingService tsmpSettingService, TokenHelper tokenHelper,
			IdPHelper idPHelper, GtwIdPCallbackService gtwIdPCallbackService, GtwIdPAuthService gtwIdPAuthService,
			LdapHelper ldapHelper, DgrGtwIdpInfoLDao dgrGtwIdpInfoLDao, DgrGtwIdpInfoJdbcDao dgrGtwIdpInfoJDao,
			DgrGtwIdpInfoADao dgrGtwIdpInfoADao, GtwIdPHelper gtwIdPHelper, TsmpTAEASKHelper tsmpTAEASKHelper,
			IdPJdbcHelper idPJdbcHelper, IdPApiHelper idPApiHelper, DgrGtwIdpInfoJdbcDao dgrGtwIdpInfoJdbcDao,
			TsmpCoreTokenEntityHelper tsmpCoreTokenEntityHelper) {
		super();
		this.objectMapper = objectMapper;
		this.tsmpSettingService = tsmpSettingService;
		this.tokenHelper = tokenHelper;
		this.idPHelper = idPHelper;
		this.gtwIdPCallbackService = gtwIdPCallbackService;
		this.gtwIdPAuthService = gtwIdPAuthService;
		this.ldapHelper = ldapHelper;
		this.dgrGtwIdpInfoLDao = dgrGtwIdpInfoLDao;
		this.dgrGtwIdpInfoJDao = dgrGtwIdpInfoJDao;
		this.dgrGtwIdpInfoADao = dgrGtwIdpInfoADao;
		this.gtwIdPHelper = gtwIdPHelper;
		this.tsmpTAEASKHelper = tsmpTAEASKHelper;
		this.idPJdbcHelper = idPJdbcHelper;
		this.idPApiHelper = idPApiHelper;
		this.dgrGtwIdpInfoJdbcDao = dgrGtwIdpInfoJdbcDao;
		this.tsmpCoreTokenEntityHelper = tsmpCoreTokenEntityHelper;
	}

	public ResponseEntity<?> gtwIdPLogin(HttpHeaders httpHeaders, HttpServletRequest httpReq,
			HttpServletResponse httpResp, String idPType) throws Exception {
		
		TPILogger.tl.debug("...idPType: " + idPType);
		
		Map<String, String> parameters = new HashMap<>();
		httpReq.getParameterMap().forEach((k, vs) -> {
			if (vs.length != 0) {
				String val = vs[0];
				if ("null".equalsIgnoreCase(val)) { // 將 Query string 中的字串"null", 轉為 null
					val = null;
				}
				parameters.put(k, val);
			}
		});

		String reqUri = httpReq.getRequestURI();
		String dgrClientRedirectUri = httpReq.getParameter("redirect_uri");
		String userIp = ServiceUtil.getIpAddress(httpReq);
		
		//checkmarx, Frameable Login Page, 已通過中風險
		httpResp.setHeader("X-Frame-Options", "sameorigin");
		//checkmarx, Missing HSTS Header
		httpResp.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload"); 
        
		
		ResponseEntity<?> errRespEntity = gtwIdPLogin(httpResp, parameters, reqUri, idPType, dgrClientRedirectUri,
				userIp);
		if (errRespEntity != null) {// 資料驗證有錯誤
			// 轉導到前端, 顯示錯誤訊息
			getGtwIdPHelper().redirectToShowMsg(httpResp, errRespEntity, idPType, dgrClientRedirectUri);
		}
		
		return null;
	}

	protected ResponseEntity<?> gtwIdPLogin(HttpServletResponse httpResp, Map<String, String> parameters, String reqUri,
			String idPType, String dgrClientRedirectUri, String userIp) throws Exception {
		ResponseEntity<?> errRespEntity = null;
		try {
		
			String responseType = parameters.get("response_type");
			String dgrClientId = parameters.get("client_id");
			String oidcScopeStr = parameters.get("scope");
			String state = parameters.get("state");
			String reqCredential = parameters.get("credential");
			String reqUserName = parameters.get("username");
			String reqUserMima = parameters.get("password");
			String codeChallenge = parameters.get("code_challenge");
			String codeChallengeMethod = parameters.get("code_challenge_method");

			// 1.檢查傳入的資料
			errRespEntity = checkReqParam(idPType, responseType, dgrClientId, oidcScopeStr, dgrClientRedirectUri, state,
					reqCredential, reqUserName, reqUserMima, codeChallenge, codeChallengeMethod, reqUri);
			if (errRespEntity != null) {// 資料驗證有錯誤
				return errRespEntity;
			}
			
			// 2.做 AES 或 JWE 解密, 取得 user 登入資料
			UserLoginData userLoginData = decryptAndGetUserLoginData(reqCredential, reqUserName, reqUserMima, reqUri);
			errRespEntity = userLoginData.errRespEntity;
			if (errRespEntity != null) {// 資料驗證有錯誤
				return errRespEntity;
			}
			
			String userName = userLoginData.userName;
			String userMima = userLoginData.userMima;
 
			// 3.驗證登入的資料
			if (DgrIdPType.LDAP.equals(idPType)) {
				errRespEntity = loginByLdap(reqUri, httpResp, idPType, responseType, dgrClientId, oidcScopeStr,
						dgrClientRedirectUri, state, userName, userMima, codeChallenge, codeChallengeMethod);

			} else if (DgrIdPType.API.equals(idPType)) {
				errRespEntity = loginByApi(reqUri, httpResp, idPType, responseType, dgrClientId, oidcScopeStr,
						dgrClientRedirectUri, state, userName, userMima, userIp, codeChallenge, codeChallengeMethod);

			} else if (DgrIdPType.JDBC.equals(idPType)) {
				errRespEntity = loginByJdbc(reqUri, httpResp, idPType, responseType, dgrClientId, oidcScopeStr,
						dgrClientRedirectUri, state, userName, userMima, codeChallenge, codeChallengeMethod);

			} else {
				// 無效的 IdP Type
				String errMsg = String.format(IdPHelper.MSG_INVALID_IDPTYPE, idPType);
				TPILogger.tl.debug(errMsg);
				errRespEntity = new ResponseEntity<OAuthTokenErrorResp2>(
						getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
						HttpStatus.BAD_REQUEST);// 400
			}
			
			
			
			if (httpResp != null) {
				//checkmarx, Frameable Login Page, 已通過中風險
				httpResp.setHeader("X-Frame-Options", "sameorigin");
				//checkmarx, Missing HSTS Header
				httpResp.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload"); 
			}

			if (errRespEntity != null) {// 資料驗證有錯誤
				return errRespEntity;
			}

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			String errMsg = TokenHelper.INTERNAL_SERVER_ERROR;
			TPILogger.tl.error(errMsg);
			errRespEntity = getTokenHelper().getInternalServerErrorResp(reqUri, errMsg);// 500
			return errRespEntity;
		}

		return null;
	}
	
	/**
	 * 做 AES 或 JWE 解密 <br>
	 * 取得 user 登入資料
	 */
	protected UserLoginData decryptAndGetUserLoginData(String reqCredential, String reqUserName, String reqUserMima,
			String reqUri) {
		
		String decryptType = null;
		ResponseEntity<?> errRespEntity = null;
		
		UserLoginData userLoginData = new UserLoginData();
       
		if(StringUtils.hasLength(reqCredential) && "none".equalsIgnoreCase(reqCredential)) { // for POST, 密碼不加密
			decryptType = "NONE";
			
		}else if (StringUtils.hasLength(reqCredential)) {// 若 credential 有值
			decryptType = "JWE";
			
		} else if (StringUtils.hasLength(reqUserName) //
				&& StringUtils.hasLength(reqUserMima)) { // 若 username 和 password 有值
			decryptType = "AES";
		}
		
		TPILogger.tl.debug("...decryptType: " + decryptType);
		
		String userName = null;
		String userMima = null;
		Long exp = null;
		
		if("NONE".equals(decryptType)) { // 不做解密
			userName = reqUserName;
			userMima = reqUserMima;
			
		} else if ("AES".equals(decryptType)) {
			// 1.將 user 密碼做 AES 解密, 取得 User 密碼明文
			userName = reqUserName;
			userMima = getTsmpTAEASKHelper().decrypt(reqUserMima);
			
			// 檢查 password
			if (!StringUtils.hasText(userMima)) {
				// URL 參數 'password' AES 解密失敗
				String errMsg = "URL Parameter 'password' AES decryption failed.";
				TPILogger.tl.debug(errMsg);
				errRespEntity = getTokenHelper().getBadRequestErrorResp(reqUri, TokenHelper.INVALID_REQUEST, errMsg);// 400
				userLoginData.errRespEntity = errRespEntity;
				return userLoginData;
			}

		} else if ("JWE".equals(decryptType)) {
			// 2.做 JWE 解密, 取得 User 帳號、密碼, 以及到期時間
			
			// 取得 Private Key
			PrivateKey privateKey = getTsmpCoreTokenEntityHelper().getKeyPair().getPrivate();
			
			// JWE 解密
			try {
				String payloadJsonStr = JWEcodec.jweDecryption(privateKey, reqCredential);
				// 取得 JWE 解密後的值
				JsonNode payloadJsonNode = new ObjectMapper().readTree(payloadJsonStr);
				userName = JsonNodeUtil.getNodeAsText(payloadJsonNode, "username");
				userMima = JsonNodeUtil.getNodeAsText(payloadJsonNode, "password");
				exp = JsonNodeUtil.getNodeAsLong(payloadJsonNode, "exp");
				
			} catch (Exception e) {
				TPILogger.tl.debug(StackTraceUtil.logStackTrace(e));
				// URL 參數 'credential' JWE解密失敗
				String errMsg = "URL Parameter 'credential' JWE decryption failed.";
				TPILogger.tl.debug(errMsg);
				errRespEntity = getTokenHelper().getBadRequestErrorResp(reqUri, TokenHelper.INVALID_REQUEST,
						errMsg);// 400
				userLoginData.errRespEntity = errRespEntity;
				return userLoginData;
			}

			// 檢查 username
			if (!StringUtils.hasLength(userName)) {
				// URL 的參數 'credential' JWE 解密成功, 但缺少 'username' 值
				String errMsg = "URL parameter 'credential' JWE decrypted successfully, but 'username' value is missing.";
				TPILogger.tl.debug(errMsg);
				errRespEntity = getTokenHelper().getBadRequestErrorResp(reqUri, TokenHelper.INVALID_REQUEST,
						errMsg);// 400
				userLoginData.errRespEntity = errRespEntity;
				return userLoginData;
			}

			// 檢查 password
			if (!StringUtils.hasLength(userMima)) {
				// URL 的參數 'credential' JWE 解密成功, 但缺少 'password' 值
				String errMsg = "URL parameter 'credential' JWE decrypted successfully, but 'password' value is missing.";
				TPILogger.tl.debug(errMsg);
				errRespEntity = getTokenHelper().getBadRequestErrorResp(reqUri, TokenHelper.INVALID_REQUEST, errMsg);// 400
				userLoginData.errRespEntity = errRespEntity;
				return userLoginData;
			}

			// 檢查 exp 到期時間
			if (exp == null || exp == 0) {
				// URL 的參數 'credential' JWE 解密成功, 但缺少 'exp' 值
				String errMsg = "URL parameter 'credential' JWE decrypted successfully, but 'exp' value is missing.";
				TPILogger.tl.debug(errMsg);
				errRespEntity = getTokenHelper().getBadRequestErrorResp(reqUri, TokenHelper.INVALID_REQUEST,
						errMsg);// 400
				userLoginData.errRespEntity = errRespEntity;
				return userLoginData;
			}
			
			// 檢查exp是否過期
			if (exp < System.currentTimeMillis()) {// 已過期
				// URL 的參數 'credential' JWE 解密成功, 但 'exp' 已過期
				String errMsg = "URL parameter 'credential' JWE decrypted successfully, but 'exp' has expired: " + exp;
				TPILogger.tl.debug(errMsg);
				errRespEntity = getTokenHelper().getForbiddenErrorResp(reqUri, errMsg);// 403
				userLoginData.errRespEntity = errRespEntity;
				return userLoginData;
			}
			
		} else { 
			// 3.其他, 不正確
			String errMsg = TokenHelper.INTERNAL_SERVER_ERROR + ", decrypt type '" + decryptType + "' is failed";
			TPILogger.tl.error(errMsg);
			errRespEntity = getTokenHelper().getInternalServerErrorResp(reqUri, errMsg);// 500
			userLoginData.errRespEntity = errRespEntity;
			return userLoginData;
		}

		userLoginData.userName = userName;
		userLoginData.userMima = userMima;
		
		return userLoginData;
	}

	/**
	 * 檢查傳入的資料
	 */
	private ResponseEntity<?> checkReqParam(String idPType, String responseType, String dgrClientId,
			String oidcScopeStr, String dgrClientRedirectUri, String state, String reqCredential, String reqUserName,
			String reqUserMima, String codeChallenge, String codeChallengeMethod, String reqUri) {
		ResponseEntity<?> errRespEntity = getTokenHelper().checkSupportGtwIdPType(idPType);
		if (errRespEntity != null) {// idPType 資料驗證有錯誤
			return errRespEntity;
		}

		errRespEntity = getTokenHelper().checkResponseType(responseType);
		if (errRespEntity != null) {// response_type 資料驗證有錯誤
			return errRespEntity;
		}

		// 沒有 state
		if (!StringUtils.hasLength(state)) {
			String errMsg = TokenHelper.MISSING_REQUIRED_PARAMETER + "state";
			TPILogger.tl.debug(errMsg);
			return getTokenHelper().getBadRequestErrorResp(reqUri, TokenHelper.INVALID_REQUEST, errMsg);// 400
		}

		// 檢查 (credential) or (username、password)
		if (!StringUtils.hasLength(reqCredential) //
				&& !StringUtils.hasLength(reqUserName) //
				&& !StringUtils.hasLength(reqUserMima)) { // 沒有 (credential) 和 (username、password)
			String errMsg = TokenHelper.MISSING_REQUIRED_PARAMETER + "credential";
			TPILogger.tl.debug(errMsg);
			return getTokenHelper().getBadRequestErrorResp(reqUri, TokenHelper.INVALID_REQUEST, errMsg);// 400

		} else if (StringUtils.hasLength(reqUserName) //
				&& !StringUtils.hasLength(reqUserMima)) { // 若 username 有值, 但 password 沒有值,
			String errMsg = TokenHelper.MISSING_REQUIRED_PARAMETER + "password";
			TPILogger.tl.debug(errMsg);
			return getTokenHelper().getBadRequestErrorResp(reqUri, TokenHelper.INVALID_REQUEST, errMsg);// 400

		} else if (StringUtils.hasLength(reqUserMima) //
				&& !StringUtils.hasLength(reqUserName)) {// 若 password 有值, 但 username 沒有值,
			String errMsg = TokenHelper.MISSING_REQUIRED_PARAMETER + "username";
			TPILogger.tl.debug(errMsg);
			return getTokenHelper().getBadRequestErrorResp(reqUri, TokenHelper.INVALID_REQUEST, errMsg);// 400
		}

		// 檢查是否有 code_challenge 和 code_challenge_method
		errRespEntity = getGtwIdPHelper().checkCodeChallengeParam(codeChallenge, codeChallengeMethod);
		if (errRespEntity != null) {// 資料驗證有錯誤
			return errRespEntity;
		}

		errRespEntity = getTokenHelper().checkOidcScope(oidcScopeStr);
		if (errRespEntity != null) {// scope 資料驗證有錯誤
			return errRespEntity;
		}

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
	 * 以 JDBC 登入
	 */
	protected ResponseEntity<?> loginByJdbc(String reqUri, HttpServletResponse httpResp, String idPType,
			String responseType, String dgrClientId, String openIdScopeStr, String dgrClientRedirectUri, String state,
			String reqUserName, String userMima, String codeChallenge, String codeChallengeMethod) throws Exception {

		UserInfoData userInfoData = new UserInfoData();
		//checkmarx, Reflected XSS All Clients 
		reqUri = ESAPI.encoder().encodeForHTML(reqUri);
		idPType = ESAPI.encoder().encodeForHTML(idPType);
		try {
			ResponseEntity<?> errRespEntity = null;

			// 1.取得 dgR client 對應的 JDBC 連線資料
			// 取得狀態為 "Y",且建立時間最新的
			String status = "Y";
			DgrGtwIdpInfoJdbc dgrGtwIdpInfoJdbc = getDgrGtwIdpInfoJdbcDao()
					.findFirstByClientIdAndStatusOrderByCreateDateTimeDesc(dgrClientId, status);

			if (dgrGtwIdpInfoJdbc == null) {
				// Table [DGR_GTW_IDP_INFO_JDBC] 查不到資料
				TPILogger.tl.debug("Table [DGR_GTW_IDP_INFO_JDBC] can't find data. dgrClientId: " + dgrClientId
						+ ", status: " + status);
				// 設定檔缺少參數
				String errMsg = TokenHelper.THE_PROFILE_IS_MISSING_PARAMETERS + "GTW IdP(" + idPType + ") Info";
				TPILogger.tl.debug(errMsg);
				errRespEntity = getTokenHelper().getUnauthorizedErrorResp(reqUri, errMsg);// 401
				return errRespEntity;
			}

			// 2.驗證 user 帳號 & 密碼, 並取得 user data
			String connName = dgrGtwIdpInfoJdbc.getConnectionName();
			String sqlPtmt = dgrGtwIdpInfoJdbc.getSqlPtmt();
			String sqlParams = dgrGtwIdpInfoJdbc.getSqlParams();
			String userMimaAlg = dgrGtwIdpInfoJdbc.getUserMimaAlg();
			String userMimaColName = dgrGtwIdpInfoJdbc.getUserMimaColName();

			String idtSubColName = dgrGtwIdpInfoJdbc.getIdtSub();
			String idtNameColName = dgrGtwIdpInfoJdbc.getIdtName();
			String idtEmailColName = dgrGtwIdpInfoJdbc.getIdtEmail();
			String idtPictureColName = dgrGtwIdpInfoJdbc.getIdtPicture();

			userInfoData = getIdPJdbcHelper().checkUserAuth(connName, sqlPtmt, sqlParams, reqUserName, userMima,
					userMimaAlg, userMimaColName, idtSubColName, idtNameColName, idtEmailColName, idtPictureColName,
					reqUri);
			errRespEntity = userInfoData.errRespEntity;
			if (errRespEntity != null) {
				return errRespEntity;
			}

			// 3.建立 DGR_GTW_IDP_AUTH_M (Gateway IdP Auth記錄檔主檔)
			// 若 state 已存在,顯示錯誤訊息
			errRespEntity = getGtwIdPHelper().checkStateExists(state, reqUri);
			if (errRespEntity != null) {
				return errRespEntity;
			}

			// 建立 DGR_GTW_IDP_AUTH_M
			String dgRcode = null;// 此時還沒有 dgRcode

			DgrGtwIdpAuthM dgrGtwIdpAuthM = getGtwIdPAuthService().createDgrGtwIdpAuthM(state, dgrClientId, idPType,
					dgRcode, dgrClientRedirectUri, codeChallenge, codeChallengeMethod);
			long gtwIdpAuthMId = dgrGtwIdpAuthM.getGtwIdpAuthMId();

			// 建立 DGR_GTW_IDP_AUTH_D, 寫入 scope
			getGtwIdPAuthService().createDgrGtwIdpAuthD(gtwIdpAuthMId, openIdScopeStr);

			// 4.將 user info 存入DB
			// 此時尚沒有 dgRcode, 先存 state 的值, 到期時間填0
			long expireDateTime = 0;
			String userName = userInfoData.userName;
			String userAlias = userInfoData.userAlias;
			String userEmail = userInfoData.userEmail;
			String userPicture = userInfoData.userPicture;
			getGtwIdPCallbackService().createDgrGtwIdpAuthCode(state, null, DgrAuthCodePhase.STATE, expireDateTime,
					idPType, dgrClientId, userName, userAlias, userEmail, userPicture, null, null, null, null, null,
					null);

			// 5.轉導到 user 同意畫面
			String dgrConsentUiUrl = getDgrUserConsentUiUrl(idPType, responseType, dgrClientId, openIdScopeStr,
					dgrClientRedirectUri, state, reqUserName);

			// user 同意畫面
			TPILogger.tl.debug("Redirect to URL【User Consent UI URL】: " + dgrConsentUiUrl);
			
			//checkmarx, Frameable Login Page, 已通過中風險
			httpResp.setHeader("X-Frame-Options", "sameorigin");
			//checkmarx, Missing HSTS Header
			httpResp.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload"); 
	        
			
			httpResp.sendRedirect(dgrConsentUiUrl);

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			String errMsg = TokenHelper.INTERNAL_SERVER_ERROR;
			TPILogger.tl.error(errMsg);
			return getTokenHelper().getInternalServerErrorResp(reqUri, errMsg);// 500
		}

		return null;
	}

	/**
	 * 以 LDAP 登入
	 */
	protected ResponseEntity<?> loginByLdap(String reqUri, HttpServletResponse httpResp, String idPType,
			String responseType, String dgrClientId, String openIdScopeStr, String dgrClientRedirectUri, String state,
			String reqUserName, String userMima, String codeChallenge, String codeChallengeMethod) throws Exception {

		ResponseEntity<?> errRespEntity = null;
		//checkmarx, Reflected XSS All Clients 
		reqUri = ESAPI.encoder().encodeForHTML(reqUri);
		try {

			// 1.取得 dgR client 對應的 LDAP 連線資料
			// 取得狀態為 "Y",且建立時間最新的
			String status = "Y";
			DgrGtwIdpInfoL dgrGtwIdpInfoL = getDgrGtwIdpInfoLDao()
					.findFirstByClientIdAndStatusOrderByCreateDateTimeDesc(dgrClientId, status);

			if (dgrGtwIdpInfoL == null) {
				// Table [DGR_GTW_IDP_INFO_L] 查不到資料
				TPILogger.tl.debug("Table [DGR_GTW_IDP_INFO_L] can't find data. client_id: " + dgrClientId
						+ ", status: " + status);

				// 設定檔缺少參數
				String errMsg = TokenHelper.THE_PROFILE_IS_MISSING_PARAMETERS + "GTW IdP info(LDAP). client_id: "
						+ dgrClientId;
				TPILogger.tl.debug(errMsg);
				errRespEntity = getTokenHelper().getUnauthorizedErrorResp(reqUri, errMsg);// 401
				return errRespEntity;
			}

			String ldapUrl = dgrGtwIdpInfoL.getLdapUrl();// Ldap登入的URL
			String ldapDn = dgrGtwIdpInfoL.getLdapDn();// Ldap登入的使用者DN
			String ldapBaseDn = dgrGtwIdpInfoL.getLdapBaseDn();// Ldap基礎DN
			int ldapTimeout = dgrGtwIdpInfoL.getLdapTimeout();// Ldap登入的連線timeout,單位毫秒

			// 2.檢查 LDAP User 帳號 & 密碼是否正確
			// 3.取得 user data
			boolean isGetUserInfo = true;
			LdapAdAuthData ldapAdAuthData = getLdapHelper().checkLdapAuth(ldapUrl, ldapDn, ldapBaseDn, ldapTimeout,
					reqUserName, userMima, isGetUserInfo, null);
			String errMsg = ldapAdAuthData.errMsg;
			if (StringUtils.hasLength(errMsg)) {
				TPILogger.tl.debug(errMsg);
				errRespEntity = new ResponseEntity<OAuthTokenErrorResp2>(
						getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_USER, errMsg),
						HttpStatus.UNAUTHORIZED);// 401
				return errRespEntity;
			}

			// 4.建立 DGR_GTW_IDP_AUTH_M (Gateway IdP Auth記錄檔主檔)
			// 若 state 已存在,顯示錯誤訊息
			errRespEntity = getGtwIdPHelper().checkStateExists(state, reqUri);
			if (errRespEntity != null) {
				return errRespEntity;
			}

			// 建立 DGR_GTW_IDP_AUTH_M
			String dgRcode = null;// 此時還沒有 dgRcode
			DgrGtwIdpAuthM dgrGtwIdpAuthM = getGtwIdPAuthService().createDgrGtwIdpAuthM(state, dgrClientId, idPType,
					dgRcode, dgrClientRedirectUri, codeChallenge, codeChallengeMethod);
			long gtwIdpAuthMId = dgrGtwIdpAuthM.getGtwIdpAuthMId();

			// 建立 DGR_GTW_IDP_AUTH_D, 寫入 scope
			getGtwIdPAuthService().createDgrGtwIdpAuthD(gtwIdpAuthMId, openIdScopeStr);

			// 5.將 user info 存入DB
			// 此時尚沒有 dgRcode, 先存 state 的值, 到期時間填0
			long expireDateTime = 0;
			String userAlias = ldapAdAuthData.name;
			String userEmail = ldapAdAuthData.mail;
			String userPicture = null;
			getGtwIdPCallbackService().createDgrGtwIdpAuthCode(state, null, DgrAuthCodePhase.STATE, expireDateTime,
					idPType, dgrClientId, reqUserName, userAlias, userEmail, userPicture, null, null, null, null, null,
					null);

			// 6.轉導到 user 同意畫面
			String dgrConsentUiUrl = getDgrUserConsentUiUrl(idPType, responseType, dgrClientId, openIdScopeStr,
					dgrClientRedirectUri, state, reqUserName);

			//checkmarx, Frameable Login Page, 已通過中風險
			httpResp.setHeader("X-Frame-Options", "sameorigin");
			//checkmarx, Missing HSTS Header
			httpResp.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload"); 
	        
			
			// user 同意畫面
			TPILogger.tl.debug("Redirect to URL【User Consent UI URL】: " + dgrConsentUiUrl);
			httpResp.sendRedirect(dgrConsentUiUrl);

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			String errMsg = TokenHelper.INTERNAL_SERVER_ERROR;
			TPILogger.tl.error(errMsg);
			return getTokenHelper().getInternalServerErrorResp(reqUri, errMsg);// 500
		}

		return null;
	}

	/**
	 * 以 API 登入
	 */
	protected ResponseEntity<?> loginByApi(String reqUri, HttpServletResponse httpResp, String idPType,
			String responseType, String dgrClientId, String openIdScopeStr, String dgrClientRedirectUri, String state,
			String reqUserName, String userMima, String userIp, String codeChallenge, String codeChallengeMethod)
			throws Exception {

		ResponseEntity<?> errRespEntity = null;
		//checkmarx, Reflected XSS All Clients 
		reqUri = ESAPI.encoder().encodeForHTML(reqUri);
		try {

			// 1.取得 dgR client 對應的 API 連線資料
			// 取得狀態為 "Y",且建立時間最新的
			String status = "Y";

			DgrGtwIdpInfoA dgrGtwIdpInfoA = getDgrGtwIdpInfoADao()
					.findFirstByClientIdAndStatusOrderByCreateDateTimeDesc(dgrClientId, status);

			if (dgrGtwIdpInfoA == null) {
				// Table [DGR_GTW_IDP_INFO_A] 查不到資料
				TPILogger.tl.debug("Table [DGR_GTW_IDP_INFO_A] can't find data. client_id: " + dgrClientId
						+ ", status: " + status);

				// 設定檔缺少參數
				String errMsg = TokenHelper.THE_PROFILE_IS_MISSING_PARAMETERS + "GTW IdP info(API). client_id: "
						+ dgrClientId;
				TPILogger.tl.debug(errMsg);
				errRespEntity = getTokenHelper().getUnauthorizedErrorResp(reqUri, errMsg);// 401
				return errRespEntity;
			}

			// 2.調用登入API, 檢查 user 帳號 & 密碼是否正確
			// 3.取得 user data
			ApiUserInfoData apiUserInfoData = getIdPApiHelper().callLoginApi(reqUserName, userMima, userIp,
					dgrGtwIdpInfoA, reqUri);
			String errMsg = apiUserInfoData.errMsg;
			if (StringUtils.hasLength(errMsg)) {
				//checkmarx, Reflected XSS All Clients
				errMsg = ESAPI.encoder().encodeForHTML(errMsg.toString());
				
				errRespEntity = new ResponseEntity<OAuthTokenErrorResp2>(
						getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_USER, errMsg),
						HttpStatus.UNAUTHORIZED);// 401
				return errRespEntity;
			}

			// 4.建立 DGR_GTW_IDP_AUTH_M (Gateway IdP Auth記錄檔主檔)
			// 若 state 已存在,顯示錯誤訊息
			errRespEntity = getGtwIdPHelper().checkStateExists(state, reqUri);
			if (errRespEntity != null) {
				return errRespEntity;
			}

			// 建立 DGR_GTW_IDP_AUTH_M
			String dgRcode = null;// 此時還沒有 dgRcode
			DgrGtwIdpAuthM dgrGtwIdpAuthM = getGtwIdPAuthService().createDgrGtwIdpAuthM(state, dgrClientId, idPType,
					dgRcode, dgrClientRedirectUri, codeChallenge, codeChallengeMethod);
			long gtwIdpAuthMId = dgrGtwIdpAuthM.getGtwIdpAuthMId();

			// 建立 DGR_GTW_IDP_AUTH_D, 寫入 scope
			getGtwIdPAuthService().createDgrGtwIdpAuthD(gtwIdpAuthMId, openIdScopeStr);

			// 5.將 user info 存入DB
			// 此時尚沒有 dgRcode, 先存 state 的值, 到期時間填0
			long expireDateTime = 0;
			String userAlias = apiUserInfoData.userName;
			String userEmail = apiUserInfoData.userEmail;
			String userPicture = apiUserInfoData.userPicture;
			String apiResp = apiUserInfoData.apiResp;
			String idtLightId = apiUserInfoData.idtLightId;
			String idtRoleName = apiUserInfoData.idtRoleName;
			getGtwIdPCallbackService().createDgrGtwIdpAuthCode(state, null, DgrAuthCodePhase.STATE, expireDateTime,
					idPType, dgrClientId, reqUserName, userAlias, userEmail, userPicture, null, null, null, apiResp,
					idtLightId, idtRoleName);

			// 6.轉導到 user 同意畫面
			String dgrConsentUiUrl = getDgrUserConsentUiUrl(idPType, responseType, dgrClientId, openIdScopeStr,
					dgrClientRedirectUri, state, reqUserName);

			// user 同意畫面
			TPILogger.tl.debug("Redirect to URL【User Consent UI URL】: " + dgrConsentUiUrl);
			
			//checkmarx, Frameable Login Page, 已通過中風險
			httpResp.setHeader("X-Frame-Options", "sameorigin");
			//checkmarx, Missing HSTS Header
			httpResp.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload"); 
	        
			
			httpResp.sendRedirect(dgrConsentUiUrl);

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			String errMsg = TokenHelper.INTERNAL_SERVER_ERROR;
			TPILogger.tl.error(errMsg);
			return getTokenHelper().getInternalServerErrorResp(reqUri, errMsg);// 500
		}

		return null;
	}

	/**
	 * 取得 user 同意畫面 URL
	 */
	private String getDgrUserConsentUiUrl(String idPType, String responseType, String dgrClientId,
			String openIdScopeStr, String dgrClientRedirectUri, String state, String reqUserName) throws Exception {

		// 前端GTW IdP User同意畫面的URL
		// TODO, Mini,(要恢復)
		String dgrConsentUiUrl = getTsmpSettingService().getVal_GTW_IDP_CONSENT_URL();

		// TODO, Mini, test測試用
//		String dgrConsentUiUrl = "https://localhost:8080/dgrv4/ac4/gtwidp/{idPType}/consent";

		// TODO, Mini, test (前端未加入畫面前測試用)
//		String dgrConsentUiUrl = "https://localhost:18080/dgrv4/mockac/gtwidp/{idPType}/consentui";

		dgrConsentUiUrl = dgrConsentUiUrl.replace("{idPType}", idPType);

		URL urlObj = new URL(dgrConsentUiUrl);
		dgrConsentUiUrl = urlObj.getPath();// 使用相對路徑
		
		 if(StringUtils.hasText(urlPrefix)) {
			 dgrConsentUiUrl = urlPrefix + dgrConsentUiUrl;
		 }

		String redirectUrl = String.format(
				"%s" + "?response_type=%s" + "&client_id=%s" + "&scope=%s" + "&redirect_uri=%s" + "&state=%s"
						+ "&username=%s",
				dgrConsentUiUrl, IdPHelper.getUrlEncode(responseType), IdPHelper.getUrlEncode(dgrClientId),
				IdPHelper.getUrlEncode(openIdScopeStr), IdPHelper.getUrlEncode(dgrClientRedirectUri),
				IdPHelper.getUrlEncode(state), IdPHelper.getUrlEncode(reqUserName));
		return redirectUrl;
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

	protected IdPHelper getIdPHelper() {
		return idPHelper;
	}

	protected GtwIdPCallbackService getGtwIdPCallbackService() {
		return gtwIdPCallbackService;
	}

	protected GtwIdPAuthService getGtwIdPAuthService() {
		return gtwIdPAuthService;
	}

	protected LdapHelper getLdapHelper() {
		return ldapHelper;
	}

	protected DgrGtwIdpInfoLDao getDgrGtwIdpInfoLDao() {
		return dgrGtwIdpInfoLDao;
	}

	protected DgrGtwIdpInfoJdbcDao getDgrGtwIdpInfoJDao() {
		return dgrGtwIdpInfoJDao;
	}

	protected DgrGtwIdpInfoADao getDgrGtwIdpInfoADao() {
		return dgrGtwIdpInfoADao;
	}

	protected GtwIdPHelper getGtwIdPHelper() {
		return gtwIdPHelper;
	}

	protected TsmpTAEASKHelper getTsmpTAEASKHelper() {
		return tsmpTAEASKHelper;
	}

	protected IdPJdbcHelper getIdPJdbcHelper() {
		return idPJdbcHelper;
	}

	protected IdPApiHelper getIdPApiHelper() {
		return idPApiHelper;
	}

	protected DgrGtwIdpInfoJdbcDao getDgrGtwIdpInfoJdbcDao() {
		return dgrGtwIdpInfoJdbcDao;
	}

	protected TsmpCoreTokenEntityHelper getTsmpCoreTokenEntityHelper() {
		return tsmpCoreTokenEntityHelper;
	}
}
