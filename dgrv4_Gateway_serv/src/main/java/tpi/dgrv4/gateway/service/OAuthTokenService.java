package tpi.dgrv4.gateway.service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.JWK;

import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.codec.utils.IdTokenUtil;
import tpi.dgrv4.codec.utils.IdTokenUtil.IdTokenData;
import tpi.dgrv4.codec.utils.JWEcodec;
import tpi.dgrv4.codec.utils.JWScodec;
import tpi.dgrv4.codec.utils.ProbabilityAlgUtils;
import tpi.dgrv4.codec.utils.SHA256Util;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.DgrAuthCodePhase;
import tpi.dgrv4.common.constant.TsmpAuthCodeStatus2;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;

import tpi.dgrv4.entity.component.cipher.TsmpCoreTokenEntityHelper;

import tpi.dgrv4.entity.entity.Authorities;
import tpi.dgrv4.entity.entity.DgrAcIdpAuthCode;
import tpi.dgrv4.entity.entity.DgrAcIdpUser;
import tpi.dgrv4.entity.entity.DgrGtwIdpAuthCode;
import tpi.dgrv4.entity.entity.DgrGtwIdpAuthD;
import tpi.dgrv4.entity.entity.DgrGtwIdpAuthM;
import tpi.dgrv4.entity.entity.OauthClientDetails;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpTokenHistory;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.repository.AuthoritiesDao;
import tpi.dgrv4.entity.repository.DgrAcIdpAuthCodeDao;
import tpi.dgrv4.entity.repository.DgrAcIdpUserDao;
import tpi.dgrv4.entity.repository.DgrGtwIdpAuthCodeDao;
import tpi.dgrv4.entity.repository.DgrGtwIdpAuthDDao;
import tpi.dgrv4.entity.repository.DgrGtwIdpAuthMDao;
import tpi.dgrv4.entity.repository.DgrOauthApprovalsDao;
import tpi.dgrv4.entity.repository.OauthClientDetailsDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpClientGroupDao;
import tpi.dgrv4.entity.repository.TsmpGroupDao;
import tpi.dgrv4.entity.repository.TsmpTokenHistoryDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.entity.repository.UsersDao;
import tpi.dgrv4.gateway.component.GtwIdPHelper;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.component.TokenHelper.BasicAuthClientData;
import tpi.dgrv4.gateway.component.TokenHelper.JwtPayloadData;
import tpi.dgrv4.gateway.constant.DgrCodeChallengeMethod;
import tpi.dgrv4.gateway.constant.DgrDeployRole;
import tpi.dgrv4.gateway.constant.DgrOpenIDConnectScope;
import tpi.dgrv4.gateway.constant.DgrTokenGrantType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.AdaptiveThreadPoolExecutor;
import tpi.dgrv4.gateway.util.DigiRunnerGtwDeployProperties;
import tpi.dgrv4.gateway.util.JsonNodeUtil;
import tpi.dgrv4.gateway.vo.ClientCredentialCacheVo;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp2;
import tpi.dgrv4.gateway.vo.OAuthTokenResp;
import tpi.dgrv4.gateway.vo.TsmpApiLogReq;

@Service
public class OAuthTokenService {

	private TsmpUserDao tsmpUserDao;
	private AuthoritiesDao authoritiesDao;
	private OauthClientDetailsDao oauthClientDetailsDao;
	private UsersDao usersDao;
	private TsmpCoreTokenEntityHelper tsmpCoreTokenHelper;
	private DgrOauthApprovalsDao dgrOauthApprovalsDao;
	private TokenHelper tokenHelper;
	private TsmpSettingService tsmpSettingService;
	private TsmpTokenHistoryDao tsmpTokenHistoryDao;
	private TsmpClientDao tsmpClientDao;
	private DgrAcIdpUserDao dgrAcIdpUserDao;
	private DgrAcIdpAuthCodeDao dgrAcIdpAuthCodeDao;
	private DgrGtwIdpAuthMDao dgrGtwIdpAuthMDao;
	private DgrGtwIdpAuthDDao dgrGtwIdpAuthDDao;
	private DgrGtwIdpAuthCodeDao dgrGtwIdpAuthCodeDao;
	private TsmpClientGroupDao tsmpClientGroupDao;
	private TsmpGroupDao tsmpGroupDao;
	private CommForwardProcService commForwardProcService;
	private DigiRunnerGtwDeployProperties digiRunnerGtwDeployProperties;
	private AsyncSendNotifyLandingRequestToDgrService asyncSendNotifyLandingRequestToDgrService;

	private AdaptiveThreadPoolExecutor executor = new AdaptiveThreadPoolExecutor();

	private static ObjectMapper objectMapper = new ObjectMapper();
	
	public static Map<String, ClientCredentialCacheVo> clientCredentialCacheVoMap = new HashMap<>();

	@Autowired
	public OAuthTokenService(TsmpUserDao tsmpUserDao, AuthoritiesDao authoritiesDao,
			OauthClientDetailsDao oauthClientDetailsDao, UsersDao usersDao,
			TsmpCoreTokenEntityHelper tsmpCoreTokenHelper, DgrOauthApprovalsDao dgrOauthApprovalsDao,
			TokenHelper tokenHelper, TsmpSettingService tsmpSettingService, TsmpTokenHistoryDao tsmpTokenHistoryDao,
			TsmpClientDao tsmpClientDao, DgrAcIdpUserDao dgrAcIdpUserDao, DgrAcIdpAuthCodeDao dgrAcIdpAuthCodeDao,
			DgrGtwIdpAuthMDao dgrGtwIdpAuthMDao, DgrGtwIdpAuthDDao dgrGtwIdpAuthDDao,
			DgrGtwIdpAuthCodeDao dgrGtwIdpAuthCodeDao, TsmpClientGroupDao tsmpClientGroupDao, TsmpGroupDao tsmpGroupDao,
			CommForwardProcService commForwardProcService, DigiRunnerGtwDeployProperties digiRunnerGtwDeployProperties,
			AsyncSendNotifyLandingRequestToDgrService asyncSendNotifyLandingRequestToDgrService) {
		super();
		this.tsmpUserDao = tsmpUserDao;
		this.authoritiesDao = authoritiesDao;
		this.oauthClientDetailsDao = oauthClientDetailsDao;
		this.usersDao = usersDao;
		this.tsmpCoreTokenHelper = tsmpCoreTokenHelper;
		this.dgrOauthApprovalsDao = dgrOauthApprovalsDao;
		this.tokenHelper = tokenHelper;
		this.tsmpSettingService = tsmpSettingService;
		this.tsmpTokenHistoryDao = tsmpTokenHistoryDao;
		this.tsmpClientDao = tsmpClientDao;
		this.dgrAcIdpUserDao = dgrAcIdpUserDao;
		this.dgrAcIdpAuthCodeDao = dgrAcIdpAuthCodeDao;
		this.dgrGtwIdpAuthMDao = dgrGtwIdpAuthMDao;
		this.dgrGtwIdpAuthDDao = dgrGtwIdpAuthDDao;
		this.dgrGtwIdpAuthCodeDao = dgrGtwIdpAuthCodeDao;
		this.tsmpClientGroupDao = tsmpClientGroupDao;
		this.tsmpGroupDao = tsmpGroupDao;
		this.commForwardProcService = commForwardProcService;
		this.digiRunnerGtwDeployProperties = digiRunnerGtwDeployProperties;
		this.asyncSendNotifyLandingRequestToDgrService = asyncSendNotifyLandingRequestToDgrService;
	}

	public static class OAuthTokenData {
		// for grant_type = "delegate_auth" 或 "authorization_code" 或 "cookie_token"
		public String idPType = null;
		public String idPUserName = null;// 例如: 101872102234493560934

		// for grant_type = "authorization_code", "cookie_token"
		public String idPUserEmail = null;
		public String idPUserAlias = null;// 例如: 李OO Mini Lee
		public String idPUserPicture = null;
		public String apiResp = null;// for GTW IdP(API) 調用 Login API 得到的 response
									//for AC IdP(GOOGLE/MS): 儲存 GOOGLE/MS IdP 的 response (含 Access token, Refresh token, ID token)	

		public String idtLightId = null;// for GTW IdP(API), 調用 Login API 得到的 lightId、roleName
		public String idtRoleName = null;
		
		/**
		 * for GTW IdP 時, grant_type = "authorization_code", <br>
		 * IdP type 為 LDAP / JDBC: 有 OIDC scope & user 在同意畫面勾選的存取範圍(虛擬群組 id) <br>
		 * IdP type 為 GOOGLE / MS: 只有 OIDC scope <br>
		 */
		public String oidcAndVgroupScopeStr = null;// 多個以空格" "分隔

		public Long tokenQuota = null;
		public Long tokenUsed = null;
		public Long rftQuota = null;
		public Long rftUsed = null;
	}

	public ResponseEntity<?> getOAuthToken(HttpServletRequest httpReq, HttpHeaders headers,
			HttpServletResponse httpResp) {

		Map<String, String> parameters = new HashMap<>();
		httpReq.getParameterMap().forEach((k, vs) -> {
			if (vs.length != 0) {
				parameters.put(k, vs[0]);
			}
		});

		String reqUri = httpReq.getRequestURI();
		String authorization = headers.getFirst("Authorization");

		// 製作 token
		return getToken_logged(httpReq, httpResp, parameters, authorization, reqUri);
	}

	public ResponseEntity<?> getToken(HttpServletResponse httpResp, Map<String, String> parameters,
			String authorization, String reqUri) {
		TPILogger.tl.info("\n--【OAuthTokenService.getToken()】【1】--");
		return getToken_logged(null, httpResp, parameters, authorization, reqUri);
	}

	public ResponseEntity<?> getToken(Map<String, String> parameters, String authorization, String reqUri) {
		TPILogger.tl.info("\n--【OAuthTokenService.getToken()】【2】--");
		return getToken_logged(null, null, parameters, authorization, reqUri);
	}

	/** 會記錄 ES 與 RDB Log */
	/**
	 * @param httpReq
	 * @param parameters
	 * @param authorization
	 * @param reqUri
	 * @return
	 */
	protected ResponseEntity<?> getToken_logged(HttpServletRequest httpReq, HttpServletResponse httpResp,
			Map<String, String> parameters, String authorization, String reqUri) {
		OAuthTokenData oauthTokenData = new OAuthTokenData();

		ResponseEntity<?> errRespEntity = getToken1(oauthTokenData, httpReq, httpResp, parameters, authorization,
				reqUri);

		// 統一在外層寫入 ES 與 RDB Log
		if (errRespEntity == null) {
			return null;
		}

		TsmpApiLogReq logReqVo_es = null;
		TsmpApiLogReq logReqVo_rdb = null;
		if (httpReq != null) {
			try {
				// 第一組 ES Req
				String uuid = UUID.randomUUID().toString();
				Map<String, String> esLogParams = getEsLogParams(httpReq);
				Map<String, List<String>> esHeaderMap = getCommForwardProcService().getEsHeaderMap(httpReq);
				String mbody = getCommForwardProcService().getReqMbody(httpReq);
				logReqVo_es = getCommForwardProcService() //
						.addEsTsmpApiLogReq1(uuid, esLogParams, esHeaderMap, mbody, "", "M");
				// 第一組 RDB Req
				Map<String, String> rdbLogParams = getRdbLogParams(httpReq);
				logReqVo_rdb = getCommForwardProcService() //
						.addRdbTsmpApiLogReq1(uuid, rdbLogParams, mbody, "", "M");
			} catch (Exception e) {
			}
		}

		Object bodyObj = errRespEntity.getBody();
		if (bodyObj instanceof OAuthTokenResp) {// 正確取得token
			if (httpReq != null) {
				try {
					Map<String, List<String>> headerMap = new HashMap<>();
					errRespEntity.getHeaders().forEach((k, vlist) -> {
						headerMap.put(k, vlist);
					});
					String mbody = getObjectMapper().writeValueAsString(errRespEntity.getBody());
					int contentLength = mbody.getBytes().length;
					// 第一組 ES Resp
					getCommForwardProcService().addEsTsmpApiLogResp1(headerMap, logReqVo_es, mbody,
							errRespEntity.getStatusCodeValue(), contentLength);
					// 第一組 RDB Resp
					getCommForwardProcService().addRdbTsmpApiLogResp1(headerMap, logReqVo_rdb, mbody,
							errRespEntity.getStatusCodeValue(), contentLength);
				} catch (Exception e) {
				}
			}

		} else {// 資料有錯誤
			if (httpReq != null) {
				try {
					// 第一組 ES Resp
					String mbody = getObjectMapper().writeValueAsString(errRespEntity.getBody());
					getCommForwardProcService().addEsTsmpApiLogResp1(errRespEntity, logReqVo_es, mbody);
					// 第一組 RDB Resp
					getCommForwardProcService().addRdbTsmpApiLogResp1(errRespEntity, logReqVo_rdb, mbody);
				} catch (Exception e) {
				}
			}
		}

		return errRespEntity;
	}

	protected ResponseEntity<?> getToken1(OAuthTokenData oauthTokenData, HttpServletRequest httpReq,
			HttpServletResponse httpResp, Map<String, String> parameters, String authorization, String reqUri) {

		ResponseEntity<?> errRespEntity = null;

		String grantType = parameters.get("grant_type");
		String codeVerifier = parameters.get("code_verifier");
		String cookie_jti = null;
		String cookie_idPType = null;
		if (StringUtils.hasLength(grantType)) {
			// 若 body 有 grant type, 優先執行此流程

		} else {
			boolean isCookieToken = getTsmpSettingService().getVal_DGR_COOKIE_TOKEN_ENABLE();
			// 從 cookies 取得 jti、idPType 的值
			// 直接登入 AC 的沒有 idPType
			cookie_jti = GtwIdPHelper.getStateFromCookies(httpReq, GtwIdPHelper.COOKIE_JTI);
			cookie_idPType = GtwIdPHelper.getStateFromCookies(httpReq, GtwIdPHelper.COOKIE_IDP_TYPE);
			if (StringUtils.hasLength(cookie_jti) && isCookieToken) {
				grantType = DgrTokenGrantType.COOKIE_TOKEN;
			} else if (!isCookieToken) {
				TPILogger.tl.debug("The set 'DGR_COOKIE_TOKEN_ENABLE' value is false");
			}
		}

		// Body 沒有 grant_type
		if (grantType == null) {
			String errMsg = "Missing grant type";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp2>(
					getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
					HttpStatus.BAD_REQUEST);// 400
		}

		String clientId = null;
		String clientPw = null;
		if (DgrTokenGrantType.COOKIE_TOKEN.equalsIgnoreCase(grantType)) {
			// 沒有 Authorization, 不用檢查

		} else if (DgrTokenGrantType.AUTHORIZATION_CODE.equalsIgnoreCase(grantType)) {
			// client_id 和 client_secret 也可以放在 body
			clientId = parameters.get("client_id");// client id
			clientPw = parameters.get("client_secret");// client_secret
		}

		if (!StringUtils.hasLength(clientId)) {
			if (DgrTokenGrantType.COOKIE_TOKEN.equalsIgnoreCase(grantType)) {
				// 沒有 Authorization, 不用檢查

			} else {
				// 沒有 Basic Authorization 或 格式不正確
				BasicAuthClientData basicAuthClientData = getTokenHelper().getAuthClientDataForBasic(authorization, reqUri);
				errRespEntity = basicAuthClientData.getErrRespEntity();
				
				if (errRespEntity != null) {
					return errRespEntity;
				} else {
					String[] cliendData = basicAuthClientData.getCliendData();
					clientId = cliendData[0];
					clientPw = cliendData[1];
				}
			}
		}

		return getToken2(oauthTokenData, httpReq, httpResp, parameters, grantType, clientId, clientPw, cookie_jti,
				cookie_idPType, codeVerifier, reqUri);
	}

	protected ResponseEntity<?> getToken2(OAuthTokenData oauthTokenData, HttpServletRequest httpReq,
			HttpServletResponse httpResp, Map<String, String> parameters, String grantType, String clientId,
			String clientPw, String cookie_jti, String cookie_idPType, String codeVerifier, String reqUri) {

		ResponseEntity<?> errRespEntity = null;
		TsmpTokenHistory tsmpTokenHistory = null;
		if (DgrTokenGrantType.COOKIE_TOKEN.equalsIgnoreCase(grantType)) {
			// 沒有 Header 和 Body

			// 1.查詢 TSMP_TOKEN_HISTORY 取得資料
			tsmpTokenHistory = getTsmpTokenHistoryDao().findFirstByTokenJtiAndIdpType(cookie_jti, cookie_idPType);
			if (tsmpTokenHistory == null) {
				errRespEntity = getTokenHelper().getFindTsmpTokenHistoryError(cookie_jti, cookie_idPType, reqUri);
				return errRespEntity;
			}

			clientId = tsmpTokenHistory.getClientId();
			errRespEntity = verifyForCookieToken(tsmpTokenHistory, cookie_jti, cookie_idPType, clientId, reqUri);
			if (errRespEntity != null) {
				return errRespEntity;
			}

		} else {
			// 檢查 client 帳號密碼
			errRespEntity = verifyAuth(grantType, clientId, clientPw, codeVerifier, reqUri);
			if (errRespEntity != null) {// 資料驗證有錯誤
				return errRespEntity;
			}

			// 檢查 Body 且依 grant type 檢查資料
			errRespEntity = verifyBody(oauthTokenData, parameters, grantType, clientId, reqUri);
			if (errRespEntity != null) {// 資料驗證有錯誤
				return errRespEntity;
			}
		}

		// 計算 client token 可用量
		errRespEntity = doClientTokenQuota(oauthTokenData, parameters, grantType, clientId, grantType, reqUri);
		if (errRespEntity != null) {
			return errRespEntity;
		}

		OAuthTokenResp resp = new OAuthTokenResp();
		try {
			String node = "";
			List<String> audIdList = new ArrayList<>();
			String userName = null;
			String userName_b64 = null;
			String orgId = null;
			List<String> scopeList = new ArrayList<>();
			Long stime = 0L;// 單位毫秒,13碼
			Long accessTokenExp = 0L;// 單位秒,10碼
			Long refreshTokenExp = 0L;// 單位秒,10碼
			List<String> roleIdList = new ArrayList<>();
			String refreshTokenJti = "";
			Long accessTokenValidity = 0L;
			boolean isHasRefreshToken = true;
			String authClientDetailsAuthorities = "";
			String oldAccessTokenJti = null;// Request refresh_token 中的 ati

			String origRefreshToken = null; // 來源 refresh token
			if (DgrTokenGrantType.COOKIE_TOKEN.equalsIgnoreCase(grantType)) {
				// 製作 ID Token 要用的資料
				origRefreshToken = tsmpTokenHistory.getRefreshTokenJwtstr();
				String idTokenJwtstr = tsmpTokenHistory.getIdTokenJwtstr();
				IdTokenData idTokenData = IdTokenUtil.getIdTokenData(idTokenJwtstr);

				oauthTokenData.idPUserEmail = idTokenData.userEmail;
				oauthTokenData.idPUserAlias = idTokenData.userAlias;
				oauthTokenData.idPUserPicture = idTokenData.userPicture;
				oauthTokenData.apiResp = tsmpTokenHistory.getApiResp();
				oauthTokenData.idtLightId = idTokenData.idtLightId;
				oauthTokenData.idtRoleName = idTokenData.idtRoleName;

			} else if (DgrTokenGrantType.REFRESH_TOKEN.equalsIgnoreCase(grantType)) {
				origRefreshToken = parameters.get("refresh_token");

			}
			
			ClientCredentialCacheVo cacheVo = null;
			
			// token JWE加密是否啟用,預設為false(JWS)
			boolean isJwe = getTsmpSettingService().getVal_DGR_TOKEN_JWE_ENABLE();

			if (DgrTokenGrantType.REFRESH_TOKEN.equalsIgnoreCase(grantType)
					|| DgrTokenGrantType.COOKIE_TOKEN.equalsIgnoreCase(grantType)) {
				// grantType="refresh_token" 時, accessToken 的 exp 由DB取得計算
				// 但其他資料, 都由 refresh token 的 payload 取得

				// 1.由 refresh_token 取得資料
				JwtPayloadData jwtPayloadData = getTokenHelper().getJwtPayloadData(origRefreshToken);
				errRespEntity = jwtPayloadData.errRespEntity;
				if (errRespEntity != null) {// 資料有錯誤
					return errRespEntity;
				}

				// 由 token 取得資料
				JsonNode payloadJsonNode = jwtPayloadData.payloadJsonNode;

				refreshTokenJti = JsonNodeUtil.getNodeAsText(payloadJsonNode, "jti");

				node = JsonNodeUtil.getNodeAsText(payloadJsonNode, "node");

				JsonNode audArray = JsonNodeUtil.getNodeAsArrayNode(payloadJsonNode, "aud");
				audIdList = JsonNodeUtil.convertJsonArrayToList(audArray);

				userName = JsonNodeUtil.getNodeAsText(payloadJsonNode, "user_name");
				orgId = JsonNodeUtil.getNodeAsText(payloadJsonNode, "org_id");

				JsonNode scopeArray = payloadJsonNode.get("scope");
				scopeList = JsonNodeUtil.convertJsonArrayToList(scopeArray);

				stime = JsonNodeUtil.getNodeAsLong(payloadJsonNode, "stime"); // 單位毫秒,13碼
				refreshTokenExp = JsonNodeUtil.getNodeAsLong(payloadJsonNode, "exp"); // 單位秒,10碼

				JsonNode authoritiesArray = JsonNodeUtil.getNodeAsArrayNode(payloadJsonNode, "authorities");
				roleIdList = JsonNodeUtil.convertJsonArrayToList(authoritiesArray);

				oldAccessTokenJti = JsonNodeUtil.getNodeAsText(payloadJsonNode, "ati");

				// 2.由DB取得資料
				Optional<OauthClientDetails> opt_authClientDetails = getOauthClientDetailsDao().findById(clientId);
				if (!opt_authClientDetails.isPresent()) {
					errRespEntity = getTokenHelper().getFindOauthClientDetailsError(clientId, reqUri);
					return errRespEntity;
				}

				OauthClientDetails authClientDetails = opt_authClientDetails.get();
				accessTokenValidity = authClientDetails.getAccessTokenValidity();// access token 授權期限, 單位為秒
				accessTokenValidity = getTokenHelper().getTokenValidity(accessTokenValidity);// 若授權期限沒有值, 設為10分鐘
				accessTokenExp = getTokenExp(accessTokenValidity, System.currentTimeMillis());// 由現在時間開始算

				String idPType = JsonNodeUtil.getNodeAsText(payloadJsonNode, "idp_type");
				if (StringUtils.hasLength(idPType)) {
					oauthTokenData.idPType = idPType;
				}

				// 是否為 GTW IdP 流程
				boolean isGtwIdPFlow = TokenHelper.isGtwIdPFlow(userName, idPType);
				if (isGtwIdPFlow) {
					String scopeStr = "";
					for (String scope : scopeList) {
						scopeStr += scope + " ";
					}
					// ID Token 使用
					oauthTokenData.oidcAndVgroupScopeStr = scopeStr.trim();
				}

				if (DgrTokenGrantType.REFRESH_TOKEN.equalsIgnoreCase(grantType) && isGtwIdPFlow) {
					TsmpTokenHistory tsmpTokenHistory_rf = getTsmpTokenHistoryDao()
							.findFirstByTokenJtiAndRetokenJti(oldAccessTokenJti, refreshTokenJti);
					if (tsmpTokenHistory_rf != null) {
						// 製作 ID Token 要用的資料
						origRefreshToken = tsmpTokenHistory_rf.getRefreshTokenJwtstr();
						String idTokenJwtstr = tsmpTokenHistory_rf.getIdTokenJwtstr();
						IdTokenData idTokenData = IdTokenUtil.getIdTokenData(idTokenJwtstr);

						oauthTokenData.idPUserEmail = idTokenData.userEmail;
						oauthTokenData.idPUserAlias = idTokenData.userAlias;
						oauthTokenData.idPUserPicture = idTokenData.userPicture;
						oauthTokenData.apiResp = tsmpTokenHistory_rf.getApiResp();
						oauthTokenData.idtRoleName = idTokenData.idtRoleName;
						oauthTokenData.idtLightId = idTokenData.idtLightId;
					}
				}

			} else {
				node = "executor1";

				if (DgrTokenGrantType.AUTHORIZATION_CODE.equalsIgnoreCase(grantType)) {
					userName = oauthTokenData.idPUserName;

				} else if (DgrTokenGrantType.DELEGATE_AUTH.equalsIgnoreCase(grantType)) {
					userName = oauthTokenData.idPUserName;

				} else {
					userName = parameters.get("username");
				}

				stime = System.currentTimeMillis();
				refreshTokenJti = UUID.randomUUID().toString();

				if (DgrTokenGrantType.DELEGATE_AUTH.equalsIgnoreCase(grantType)) {
					// 取得 DGR_AC_IDP_USER 的組織和角色
					if (StringUtils.hasText(userName)) {
						DgrAcIdpUser dgrAcIdpUser = getDgrAcIdpUserDao().findFirstByUserNameAndIdpType(userName,
								oauthTokenData.idPType);
						if (dgrAcIdpUser != null) {
							orgId = dgrAcIdpUser.getOrgId();
							roleIdList = getRoleIdList(userName);
							String userAlias = (dgrAcIdpUser.getUserAlias() == null) ? "" : dgrAcIdpUser.getUserAlias();
							userName_b64 = getUserName_b64(userName, userAlias, oauthTokenData.idPType);// IdP id_token
						}
					}

				} else if (DgrTokenGrantType.AUTHORIZATION_CODE.equalsIgnoreCase(grantType)) {
					// GTW IdP User 沒有組織、角色
					orgId = null;
					roleIdList = null;

				} else {
					// 取得 TSMP_USER 的組織和角色
					if (StringUtils.hasText(userName)) {
						TsmpUser user = getTsmpUserDao().findFirstByUserName(userName);
						if (user != null) {
							orgId = user.getOrgId();
							roleIdList = getRoleIdList(userName);
						}
					}
				}

				Optional<OauthClientDetails> opt_oauth = getOauthClientDetailsDao().findById(clientId);
				if (!opt_oauth.isPresent()) {
					errRespEntity = getTokenHelper().getFindOauthClientDetailsError(clientId, reqUri);
					return errRespEntity;
				}

				OauthClientDetails authClientDetails = opt_oauth.get();
				String aud = StringUtils.hasText(authClientDetails.getResourceIds())
						? authClientDetails.getResourceIds()
						: "";
				audIdList = Arrays.asList(aud.split(","));

				if (DgrTokenGrantType.AUTHORIZATION_CODE.equalsIgnoreCase(grantType)) { // for GTW IdP
					// scope 種類1: client 的 group ID(僅一般群組)
					scopeList = getTokenHelper().getClientGroupScopeList(clientId, scopeList);

					// scope 種類2: OIDC scope & user 在同意畫面勾選的存取範圍(虛擬群組 ID)
					String[] dgrScopeArr = oauthTokenData.oidcAndVgroupScopeStr.split(" ");
					List<String> dgrScopeList = Arrays.asList(dgrScopeArr);
					scopeList.addAll(dgrScopeList);

				} else {
					// scope 種類: client 的 group ID(僅一般群組)
					scopeList = getTokenHelper().getClientGroupScopeList(clientId, scopeList);
				}
				
				accessTokenValidity = authClientDetails.getAccessTokenValidity();// access token 授權期限, 單位為秒
				accessTokenValidity = getTokenHelper().getTokenValidity(accessTokenValidity);// 若授權期限沒有值, 設為10分鐘
				accessTokenExp = getTokenExp(accessTokenValidity, stime);// 由 stime 開始算
				
				// client_credentials 為解決大量產生 token 時, 寫入 TSMP_TOKEN_HISTORY I/O 效能瓶頸, 改用 cache
				// 上面的資料檢查成功, 才能取 cache 的值
				if (DgrTokenGrantType.CLIENT_CREDENTIALS.equalsIgnoreCase(grantType)) {
					// 依 cache id, 取得 cache 資料
					cacheVo = clientCredentialCacheVoMap.get(clientId);
					if (cacheVo != null) {// 走cache
						boolean isCacheUpdate = isUpdateClientCredentialCache(cacheVo, accessTokenValidity, isJwe,
								scopeList);
						if (!isCacheUpdate) {// 快取仍有效, 取快取值回傳
							return new ResponseEntity<OAuthTokenResp>(cacheVo.getoAuthTokenResp(), HttpStatus.OK);
						}
					}
				}

				Long refreshTokenValidity = authClientDetails.getRefreshTokenValidity();// refresh token 授權期限, 單位為杪
				refreshTokenValidity = getTokenHelper().getTokenValidity(refreshTokenValidity);// 若授權期限沒有值, 設為10分鐘
				refreshTokenExp = getTokenExp(refreshTokenValidity, stime);// 由 stime 開始算

				String authGrantTypes = authClientDetails.getAuthorizedGrantTypes();
				List<String> authGrantTypesList = new ArrayList<>();
				authGrantTypesList = Arrays.asList(authGrantTypes.split(","));
				// client 是否有被授權 refresh_token
				isHasRefreshToken = authGrantTypesList.contains(DgrTokenGrantType.REFRESH_TOKEN);

				// client_credentials 時使用
				authClientDetailsAuthorities = StringUtils.hasLength(authClientDetails.getAuthorities())
						? authClientDetails.getAuthorities()
						: "";
			}

			// client_credentials 不會有 refresh token 資料
			if (DgrTokenGrantType.CLIENT_CREDENTIALS.equalsIgnoreCase(grantType)) {
				isHasRefreshToken = false;
				roleIdList = Arrays.asList(authClientDetailsAuthorities.split(","));// 例如: "authorities":["client"]
			}

			// 取得 Public Key
			PublicKey publicKey = getTsmpCoreTokenHelper().getKeyPair().getPublic();
			// 取得 Private Key
			PrivateKey privateKey = getTsmpCoreTokenHelper().getKeyPair().getPrivate();

			String tokenUserName = userName;
			if (StringUtils.hasLength(userName_b64)) {// 為 AC IdP 流程
				tokenUserName = userName_b64;
			}

			Long iat = System.currentTimeMillis() / 1000;// 去掉亳秒
			String accessTokenJti = UUID.randomUUID().toString();

			// 是否為 AC IdP 流程
			boolean isAcIdPFlow = TokenHelper.isAcIdPFlow(tokenUserName, oauthTokenData.idPType);

			// 是否為 GTW IdP 流程
			boolean isGtwIdPFlow = TokenHelper.isGtwIdPFlow(tokenUserName, oauthTokenData.idPType);

			String accessTokenJwtstr = getAccessToken(isJwe, publicKey, privateKey, grantType, accessTokenJti, node,
					audIdList, stime, tokenUserName, orgId, scopeList, roleIdList, accessTokenExp, clientId, iat,
					oauthTokenData.idPType, isAcIdPFlow, isGtwIdPFlow);

			String refreshTokenJwtstr = null;
			if (isHasRefreshToken) {
				refreshTokenJwtstr = getRefreshToken(isJwe, publicKey, privateKey, grantType, accessTokenJti, node,
						audIdList, stime, tokenUserName, orgId, scopeList, roleIdList, refreshTokenExp, clientId, iat,
						refreshTokenJti, oauthTokenData.idPType, isAcIdPFlow, isGtwIdPFlow);
			}

			String idTokenJwtstr = null;
			// GTW IdP 流程的才有 ID Token
			if (DgrTokenGrantType.AUTHORIZATION_CODE.equalsIgnoreCase(grantType)
					|| (DgrTokenGrantType.COOKIE_TOKEN.equalsIgnoreCase(grantType) && isGtwIdPFlow)
					|| (DgrTokenGrantType.REFRESH_TOKEN.equalsIgnoreCase(grantType) && isGtwIdPFlow)) {
				Long idTokenExp = accessTokenExp;
				idTokenJwtstr = getIdToken(oauthTokenData, accessTokenJwtstr, oauthTokenData.oidcAndVgroupScopeStr,
						clientId, userName, iat, idTokenExp);
			}

			String scopeStr = "";// 多個scope,用空格隔開
			if (!CollectionUtils.isEmpty(scopeList)) {
				for (String scopeTemp : scopeList) {
					scopeStr += scopeTemp + " ";
				}
				scopeStr = scopeStr.trim();
			}

			String tokenType = "bearer";
			resp.setAccessToken(accessTokenJwtstr);
			resp.setExpiresIn(accessTokenValidity - 1);
			resp.setJti(accessTokenJti);
			resp.setNode(node);
			resp.setOrgId(orgId);
			resp.setRefreshToken(refreshTokenJwtstr);
			resp.setScope(scopeStr);
			resp.setStime(stime);
			resp.setTokenType(tokenType);

			// AC IdP 和 GTW IdP 的流程才有 idPType
			resp.setIdpType(oauthTokenData.idPType);
			resp.setIdToken(idTokenJwtstr);

			// 將 jti, idPType 寫入 cookie
			addCookie(httpResp, grantType, accessTokenJti, refreshTokenExp, oauthTokenData.idPType);

			// 更新/建立 TSMP_TOKEN_HISTORY
			doTokenHistory(oauthTokenData, isHasRefreshToken, userName, clientId, accessTokenJti, refreshTokenJti,
					scopeStr, stime, accessTokenExp, refreshTokenExp, grantType, oldAccessTokenJti,
					oauthTokenData.idPType, idTokenJwtstr, refreshTokenJwtstr);

			// 若角色為 Memory, 則用非同步,調用 Master/Slave(Landing)的 NotifyLanding API,
			// 將 token 資料寫入 Landing DB (TSMP_TOKEN_HISTORY)
			sendNotifyLandingRequestToDgr(oauthTokenData, isHasRefreshToken, userName, clientId, accessTokenJti,
					refreshTokenJti, scopeStr, stime, accessTokenExp, refreshTokenExp, grantType, oldAccessTokenJti,
					oauthTokenData.idPType, idTokenJwtstr, refreshTokenJwtstr);

			if (DgrTokenGrantType.CLIENT_CREDENTIALS.equalsIgnoreCase(grantType)) {
				// 若核發 token, 則更新 cache 紀錄
				updateClientCredentialCache(cacheVo, clientId, resp, scopeList.toString(), accessTokenValidity,
						accessTokenExp, isJwe);
			}
			
			errRespEntity = new ResponseEntity<OAuthTokenResp>(resp, HttpStatus.OK);
			return errRespEntity;

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			String errMsg = TokenHelper.INTERNAL_SERVER_ERROR;
			TPILogger.tl.error(errMsg);
			errRespEntity = getTokenHelper().getInternalServerErrorResp(reqUri, errMsg);// 500
			return errRespEntity;
		}
	}
	
	/**
	 * Cache 是否要更新 <br>
	 * 需更新的時機為 Cache 過期、Access token 授權時間改變、Group ID 改變、token JWS 或 JWE 改變
	 */
	public boolean isUpdateClientCredentialCache(ClientCredentialCacheVo cacheVo, long accessTokenValidity,
			boolean isJwe, List<String> scopeList) {
		long expTimestamp = cacheVo.getExpTimestamp(); // 毫秒
		long nowTimestamp = System.currentTimeMillis();

		if (nowTimestamp > expTimestamp) {// Cache 過期
			return true;
		}

		if (accessTokenValidity != cacheVo.getAccessTokenValidity()) {// Access token 授權時間改變
			return true;
		}

		String scopeListCache = cacheVo.getScopeList();
		if (!scopeList.toString().equals(scopeListCache)) {// Group ID 改變
			return true;
		}

		boolean isJweCache = cacheVo.isJwe();
		if (isJwe != isJweCache) {// token JWS 或 JWE 改變
			return true;
		}

		return false;
	}
	
	/**
	 * 更新 Client Credential Cache 內容
	 */
	private void updateClientCredentialCache(ClientCredentialCacheVo cacheVo, String clientId,
			OAuthTokenResp oAuthTokenResp, String scopeList, long accessTokenValidity, long accessTokenExp,
			boolean isJwe) {
		if (accessTokenValidity <= 10) {// 授權期限 <= 10秒, 不做快取
			clientCredentialCacheVoMap.remove(clientId);
			return;
		}

		if (cacheVo == null) {
			cacheVo = new ClientCredentialCacheVo();
		}

		cacheVo.setExpTimestamp((accessTokenExp - 10) * 1000);// token 到期時間 - 10秒, 再轉成亳秒
		cacheVo.setAccessTokenValidity(accessTokenValidity);// Access token 授權時間, 單位: 分
		cacheVo.setoAuthTokenResp(oAuthTokenResp);
		cacheVo.setScopeList(scopeList);
		cacheVo.setJwe(isJwe);

		clientCredentialCacheVoMap.put(clientId, cacheVo);
	}

	private void sendNotifyLandingRequestToDgr(OAuthTokenData oauthTokenData, boolean isHasRefreshToken,
			String userName, String clientId, String accessTokenJti, String refreshTokenJti, String scopeStr,
			Long stime, Long accessTokenExp, Long refreshTokenExp, String grantType, String oldAccessTokenJti,
			String idPType, String idTokenJwtstr, String refreshTokenJwtstr) {

		// 獲取部署角色
		String role = getDigiRunnerGtwDeployPropertiesr().getDeployRole();
		// 如果部署角色不是 Memory，則直接返回
		if (!DgrDeployRole.MEMORY.value().equalsIgnoreCase(role)) {
			return;
		}

		// 創建一個異步任務用於發送登陸通知請求
		Runnable task = getAsyncSendNotifyLandingRequestToDgrService().getTask(oauthTokenData, isHasRefreshToken,
				userName, clientId, accessTokenJti, refreshTokenJti, scopeStr, stime, accessTokenExp, refreshTokenExp,
				grantType, oldAccessTokenJti, oauthTokenData.idPType, idTokenJwtstr, refreshTokenJwtstr);
		// 執行異步任務
		executor.execute(task);
	}
	
	/**
	 * 值寫入 cookie: <br>
	 * 除了 grant type 為 client_credentials 以外的, <br>
	 * 將 jti 和 idPType 寫入 cookie <br>
	 * 若沒有 idPType 值, 則只寫入 jti <br>
	 */
	private void addCookie(HttpServletResponse httpResp, String grantType, String accessTokenJti, Long refreshTokenExp,
			String idPType) {
		if (DgrTokenGrantType.CLIENT_CREDENTIALS.equalsIgnoreCase(grantType)) {
			return;
		}

		// 除了 client_credentials 以外的 grant type, 將 jti 和 idPType 寫入 cookie
		long maxAge = getCookieMaxAge(refreshTokenExp);
		if (httpResp != null) {
			ResponseCookie jtiCookie = TokenHelper.createCookie(GtwIdPHelper.COOKIE_JTI, accessTokenJti, maxAge);
			httpResp.addHeader(HttpHeaders.SET_COOKIE, jtiCookie.toString());

			if (!StringUtils.hasLength(idPType)) { // 若沒有 idPType
				maxAge = 0; // 刪除 cookie, 有效時間設為 0
			}

			ResponseCookie idPTypeCookie = TokenHelper.createCookie(GtwIdPHelper.COOKIE_IDP_TYPE, idPType, maxAge);
			httpResp.addHeader(HttpHeaders.SET_COOKIE, idPTypeCookie.toString());
			//checkmarx, Missing HSTS Header
			httpResp.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload"); 
	        
		}
	}

	/**
	 * 取得 cookie 最大生存時間
	 */
	private long getCookieMaxAge(Long refreshTokenExp) {
		long maxAge = 0;

		// Cookie token是否啟用
		boolean isCookieToken = getTsmpSettingService().getVal_DGR_COOKIE_TOKEN_ENABLE();
		if (isCookieToken) {// 啟用
			// 以 refresh token 的到期日,計算離現在時間還有多少秒,作為 cookie 在幾秒之後自動失效
			long nowTime = System.currentTimeMillis() / 1000;// 單位秒,10碼
			maxAge = refreshTokenExp - nowTime;// Cookie最大生存時間,以秒為單位,負數的話為瀏覽器進程,關閉瀏覽器Cookie消失
			if (maxAge < 0) {
				maxAge = 0;
			}
		} else {// 不啟用
			maxAge = 0;// 刪除 cookie, 有效時間設為 0
		}

		return maxAge;
	}

	/**
	 * for AC IdP 流程 digiRunner token 中的 userName , 由 IdP id_token 的多欄位組合 <br>
	 * b64.idp_type.base64URLEncode(name/email/sub).sub <br>
	 * 例如: b64.GOOGLE.base64URLEncode(李OO Mini Lee).14234543534535 <br>
	 * 例如: b64.LDAP.base64URLEncode(minildap).minildap <br>
	 */
	public static String getUserName_b64(String userName, String userAlias, String idPType) {
		if (!StringUtils.hasLength(userAlias)) {
			userAlias = userName;
		}

		String userAlias_en = Base64Util.base64URLEncode(userAlias.getBytes());
		String userName_b64 = String.format("b64" + ".%s" + ".%s" + ".%s", idPType, userAlias_en, userName);
		return userName_b64;
	}

	/**
	 * 取得 access token / refresh token 的 exp
	 */
	private long getTokenExp(Long tokenValidity, long startTime) {
		long tokenExp = startTime + (tokenValidity * 1000);// 由現在時間開始算
		tokenExp /= 1000;// 去掉亳秒

		return tokenExp;
	}

	/**
	 * 更新/建立 TSMP_TOKEN_HISTORY
	 */
	public TsmpTokenHistory doTokenHistory(OAuthTokenData oauthTokenData, boolean isHasRefreshToken, String userName,
			String clientId, String accessTokenJti, String refreshTokenJti, String scopeStr, Long stime,
			Long accessTokenExp, Long refreshTokenExp, String grantType, String oldAccessTokenJti, String idPType,
			String idTokenJwt, String refreshTokenJwtstr) {

		boolean isRecordWhitelist = false;// token 白名單是否啟用(後踢前)
		if (DgrTokenGrantType.CLIENT_CREDENTIALS.equalsIgnoreCase(grantType)) {
			// client_credientails 只有 client 沒有 user, 固定不使用後踢前
			isRecordWhitelist = false;
		} else {
			// 取得設定值,白名單是否啟用(即後踢前)
			isRecordWhitelist = getTsmpSettingService().getVal_DGR_TOKEN_WHITELIST_ENABLE();
		}

		if (isRecordWhitelist) {
			// 啟用白名單(後踢前)
			getTokenHelper().updateTokenHistoryForWhitelist(grantType, clientId, userName);
		} else {
			// 不啟用白名單
			if (DgrTokenGrantType.REFRESH_TOKEN.equalsIgnoreCase(grantType)
					|| DgrTokenGrantType.COOKIE_TOKEN.equalsIgnoreCase(grantType)) {
				// 更新舊的 access token 的 revoked_status 為 "RT"
				getTokenHelper().updateTokenHistory(oldAccessTokenJti, refreshTokenJti, "RT");
			}
		}

		// 建立 TSMP_TOKEN_HISTORY
		TsmpTokenHistory tsmpTokenHistory = createTokenHistory(oauthTokenData, isHasRefreshToken, userName, clientId,
				accessTokenJti, refreshTokenJti, scopeStr, stime, accessTokenExp, refreshTokenExp, idPType, idTokenJwt,
				refreshTokenJwtstr);
		return tsmpTokenHistory;
	}

	/**
	 * 建立 TSMP_TOKEN_HISTORY
	 */
	private TsmpTokenHistory createTokenHistory(OAuthTokenData oauthTokenData, boolean isHasRefreshToken,
			String userName, String clientId, String accessTokenJti, String refreshTokenJti, String scopeStr,
			Long stime, Long accessTokenExp, Long refreshTokenExp, String idPType, String idTokenJwt,
			String refreshTokenJwtstr) {

		Date stimeDate = getTokenHelper().convertLongToDate(stime);
		Date expiredAt = getTokenHelper().convertLongToDate(accessTokenExp);// access token exp

		String retokenJti = null;// refresh token jti
		Date reexpiredAt = null;// refresh token exp

		if (isHasRefreshToken) {
			// 若採用 refresh_token 機制, 該欄位放入 refresh_token 之 jti
			retokenJti = refreshTokenJti;
			reexpiredAt = getTokenHelper().convertLongToDate(refreshTokenExp);
		} else {
			/*
			 * 若未使用 refresh_token 機制, 欄位 retoken_jti 放入 access token jti, 欄位 reexpired_at 放入
			 * access token exp
			 */
			retokenJti = accessTokenJti;
			reexpiredAt = expiredAt;
		}

		if (StringUtils.hasLength(scopeStr)) {
			scopeStr = scopeStr.replace(" ", ",");// 多個scope改用","分隔
		}

		return getTokenHelper().createTsmpTokenHistory(userName, clientId, accessTokenJti, scopeStr, expiredAt,
				retokenJti, reexpiredAt, stimeDate, oauthTokenData.tokenQuota, oauthTokenData.tokenUsed,
				oauthTokenData.rftQuota, oauthTokenData.rftUsed, idPType, idTokenJwt, refreshTokenJwtstr,
				oauthTokenData.apiResp);
	}

	public static String getAccessToken(boolean isJwe, PublicKey publicKey, PrivateKey privateKey, String grantType,
			String accessTokenJti, String node, List<String> audList, Long stime, String userName, String orgId,
			List<String> scopeList, List<String> roleIdList, Long accessTokenExp, String clientId, Long iat,
			String idPType, boolean isAcIdPFlow, boolean isGtwIdPFlow) throws Exception {
		Map<String, Object> payloadMap = new HashMap<>();

		if (DgrTokenGrantType.PASSWORD.equalsIgnoreCase(grantType)
				|| (DgrTokenGrantType.COOKIE_TOKEN.equalsIgnoreCase(grantType) && !isAcIdPFlow && !isGtwIdPFlow)) {
			// 沒有 idp_type
			payloadMap.put("node", node);
			payloadMap.put("aud", audList);
			payloadMap.put("user_name", userName);
			payloadMap.put("org_id", orgId);
			payloadMap.put("scope", scopeList);
			payloadMap.put("stime", stime);
			payloadMap.put("iat", iat);
			payloadMap.put("exp", accessTokenExp);
			payloadMap.put("authorities", roleIdList);
			payloadMap.put("jti", accessTokenJti);
			payloadMap.put("client_id", clientId);
			// payloadMap.put("idp_type", idPType);

		} else if (DgrTokenGrantType.REFRESH_TOKEN.equalsIgnoreCase(grantType)) {
			/* AC IdP Flow : 1.有 org_id, authorities, 2.有 idp_type */
			/* GTW IdP Flow : 1.沒有 org_id, authorities, 2.有 idp_type */
			/* 其他 Flow : 1.有 org_id, authorities, 2.沒有 idp_type */
			payloadMap.put("node", node);
			payloadMap.put("aud", audList);
			payloadMap.put("user_name", userName);
			if (!isGtwIdPFlow) {
				payloadMap.put("org_id", orgId);
			}
			payloadMap.put("scope", scopeList);
			payloadMap.put("stime", stime);
			payloadMap.put("iat", iat);
			payloadMap.put("exp", accessTokenExp);
			if (!isGtwIdPFlow) {
				payloadMap.put("authorities", roleIdList);
			}
			payloadMap.put("jti", accessTokenJti);
			payloadMap.put("client_id", clientId);

			if (StringUtils.hasLength(idPType)) {// 若 idPType 有值
				// 有 idp_type
				payloadMap.put("idp_type", idPType);
			}

		} else if (DgrTokenGrantType.CLIENT_CREDENTIALS.equalsIgnoreCase(grantType)) {
			// 沒有 user_name, org_id
			// 沒有 idp_type
			payloadMap.put("node", node);
			payloadMap.put("aud", audList);
			// payloadMap.put("user_name", userName);
			// payloadMap.put("org_id", orgId);
			payloadMap.put("scope", scopeList);
			payloadMap.put("stime", stime);
			payloadMap.put("iat", iat);
			payloadMap.put("exp", accessTokenExp);
			payloadMap.put("authorities", roleIdList);
			payloadMap.put("jti", accessTokenJti);
			payloadMap.put("client_id", clientId);
			// payloadMap.put("idp_type", idPType);

		} else if (DgrTokenGrantType.AUTHORIZATION_CODE.equalsIgnoreCase(grantType)
				|| (DgrTokenGrantType.COOKIE_TOKEN.equalsIgnoreCase(grantType) && isGtwIdPFlow)) {
			// 沒有 org_id, authorities
			// 有 idp_type
			payloadMap.put("node", node);
			payloadMap.put("aud", audList);
			payloadMap.put("user_name", userName);
			// payloadMap.put("org_id", orgId);
			payloadMap.put("scope", scopeList);
			payloadMap.put("stime", stime);
			payloadMap.put("iat", iat);
			payloadMap.put("exp", accessTokenExp);
			// payloadMap.put("authorities", roleIdList);
			payloadMap.put("jti", accessTokenJti);
			payloadMap.put("client_id", clientId);
			payloadMap.put("idp_type", idPType);

		} else if (DgrTokenGrantType.DELEGATE_AUTH.equalsIgnoreCase(grantType)
				|| (DgrTokenGrantType.COOKIE_TOKEN.equalsIgnoreCase(grantType) && isAcIdPFlow)) {
			// 有 idp_type
			payloadMap.put("node", node);
			payloadMap.put("aud", audList);
			payloadMap.put("user_name", userName);
			payloadMap.put("org_id", orgId);
			payloadMap.put("scope", scopeList);
			payloadMap.put("stime", stime);
			payloadMap.put("iat", iat);
			payloadMap.put("exp", accessTokenExp);
			payloadMap.put("authorities", roleIdList);
			payloadMap.put("jti", accessTokenJti);
			payloadMap.put("client_id", clientId);
			payloadMap.put("idp_type", idPType);
		}

		String payload = OAuthTokenService.objectMapper.writeValueAsString(payloadMap);
		String accessToken = getTokenJwt(isJwe, payload, publicKey, privateKey);

		return accessToken;
	}

	public static String getRefreshToken(boolean isJwe, PublicKey publicKey, PrivateKey privateKey, String grantType,
			String accessTokenJti, String node, List<String> audList, Long stime, String userName, String orgId,
			List<String> scopeList, List<String> roleIdList, Long refreshTokenExp, String clientId, Long iat,
			String refreshTokenJti, String idPType, boolean isAcIdPFlow, boolean isGtwIdPFlow) throws Exception {
		Map<String, Object> payloadMap = new HashMap<>();

		if (DgrTokenGrantType.PASSWORD.equalsIgnoreCase(grantType)
				|| (DgrTokenGrantType.COOKIE_TOKEN.equalsIgnoreCase(grantType) && !isAcIdPFlow && !isGtwIdPFlow)) {
			// 沒有 idp_type
			payloadMap.put("node", node);
			payloadMap.put("aud", audList);
			payloadMap.put("user_name", userName);
			payloadMap.put("org_id", orgId);
			payloadMap.put("scope", scopeList);
			payloadMap.put("ati", accessTokenJti);// Access Token jti
			payloadMap.put("stime", stime);
			payloadMap.put("iat", iat);
			payloadMap.put("exp", refreshTokenExp);
			payloadMap.put("authorities", roleIdList);
			payloadMap.put("jti", refreshTokenJti);
			payloadMap.put("client_id", clientId);
			// payloadMap.put("idp_type", idPType);

		} else if (DgrTokenGrantType.REFRESH_TOKEN.equalsIgnoreCase(grantType)) {
			/* AC IdP Flow : 1.有 org_id, authorities, 2.有 idp_type */
			/* GTW IdP Flow : 1.沒有 org_id, authorities, 2.有 idp_type */
			/* 其他 Flow : 1.有 org_id, authorities, 2.沒有 idp_type */
			payloadMap.put("node", node);
			payloadMap.put("aud", audList);
			payloadMap.put("user_name", userName);
			if (isGtwIdPFlow == false) {
				payloadMap.put("org_id", orgId);
			}
			payloadMap.put("scope", scopeList);
			payloadMap.put("ati", accessTokenJti);// Access Token jti
			payloadMap.put("stime", stime);
			payloadMap.put("iat", iat);
			payloadMap.put("exp", refreshTokenExp);
			if (isGtwIdPFlow == false) {
				payloadMap.put("authorities", roleIdList);
			}
			payloadMap.put("jti", refreshTokenJti);
			payloadMap.put("client_id", clientId);

			if (StringUtils.hasLength(idPType)) {// 若 idPType 有值
				// 有 idp_type
				payloadMap.put("idp_type", idPType);
			}

		} else if (DgrTokenGrantType.CLIENT_CREDENTIALS.equalsIgnoreCase(grantType)) {
			// 沒有 refrsh_token

		} else if (DgrTokenGrantType.AUTHORIZATION_CODE.equalsIgnoreCase(grantType)
				|| (DgrTokenGrantType.COOKIE_TOKEN.equalsIgnoreCase(grantType) && isGtwIdPFlow)) {
			// 沒有 org_id, authorities
			// 有 idp_type
			payloadMap.put("node", node);
			payloadMap.put("aud", audList);
			payloadMap.put("user_name", userName);
			// payloadMap.put("org_id", orgId);
			payloadMap.put("scope", scopeList);
			payloadMap.put("ati", accessTokenJti);// Access Token jti
			payloadMap.put("stime", stime);
			payloadMap.put("iat", iat);
			payloadMap.put("exp", refreshTokenExp);
			// payloadMap.put("authorities", roleIdList);
			payloadMap.put("jti", refreshTokenJti);
			payloadMap.put("client_id", clientId);
			payloadMap.put("idp_type", idPType);

		} else if (DgrTokenGrantType.DELEGATE_AUTH.equalsIgnoreCase(grantType)
				|| (DgrTokenGrantType.COOKIE_TOKEN.equalsIgnoreCase(grantType) && isAcIdPFlow)) {
			// 有 idp_type
			payloadMap.put("node", node);
			payloadMap.put("aud", audList);
			payloadMap.put("user_name", userName);
			payloadMap.put("org_id", orgId);
			payloadMap.put("scope", scopeList);
			payloadMap.put("ati", accessTokenJti);// Access Token jti
			payloadMap.put("stime", stime);
			payloadMap.put("iat", iat);
			payloadMap.put("exp", refreshTokenExp);
			payloadMap.put("authorities", roleIdList);
			payloadMap.put("jti", refreshTokenJti);
			payloadMap.put("client_id", clientId);
			payloadMap.put("idp_type", idPType);
		}

		String payload = OAuthTokenService.objectMapper.writeValueAsString(payloadMap);
		String refreshToken = getTokenJwt(isJwe, payload, publicKey, privateKey);

		return refreshToken;
	}

	/**
	 * 轉成 JWT (JWS / JWE) 格式
	 */
	private static String getTokenJwt(boolean isJwe, String payload, PublicKey publicKey, PrivateKey privateKey)
			throws Exception {
		String token = null;
		if (isJwe) {
			// 加密
			token = JWEcodec.jweEncryption(publicKey, payload);
		} else {
			// 簽章
			token = JWScodec.jwsSign(privateKey, payload);
		}
		return token;
	}

	/**
	 * 取得 ID Token
	 * 注意: 當欄位有增加, GtwIdPVerifyResp 要同步增加
	 */
	private String getIdToken(OAuthTokenData oauthTokenData, String accessTokenJwtstr, String reqScopeStr,
			String clientId, String userName, Long iat, Long idTokenExp) throws Exception {

		reqScopeStr = reqScopeStr.toLowerCase();// 轉小寫,再比較(忽略大小寫)
		String[] reqScopeArr = reqScopeStr.split(" ");
		List<String> reqScopeList = Arrays.asList(reqScopeArr);

		boolean isHasIdToken = false;// 是否有 ID Token

		// 有以下 scope 時, 才有 ID Token
		if (reqScopeList.contains(DgrOpenIDConnectScope.OPENID) || reqScopeList.contains(DgrOpenIDConnectScope.EMAIL)
				|| reqScopeList.contains(DgrOpenIDConnectScope.PROFILE)) {
			isHasIdToken = true;
		}

		if (!isHasIdToken) {
			return null;
		}

		// 對外公開的域名或IP, ex: www.tpisoftware.com
		String dgrPublicDomain = getTsmpSettingService().getVal_DGR_PUBLIC_DOMAIN();
		// 對外公開的Port, ex: 80
		String dgrPublicPort = getTsmpSettingService().getVal_DGR_PUBLIC_PORT();

		String schemeAndDomainAndPort = GtwIdPWellKnownService.getSchemeAndDomainAndPort(dgrPublicDomain,
				dgrPublicPort);

		String iss = GtwIdPWellKnownService.getIssuer(schemeAndDomainAndPort, oauthTokenData.idPType);
		String at_hash = getAtHash(accessTokenJwtstr);
		String userEmail = oauthTokenData.idPUserEmail;
		String userAlias = oauthTokenData.idPUserAlias;
		String picture = oauthTokenData.idPUserPicture;

		String idtLightId = oauthTokenData.idtLightId;
		String idtRoleName = oauthTokenData.idtRoleName;
		Map<String, Object> payloadMap = new HashMap<>();
		payloadMap.put("iss", iss); // REQUIRED, 發行 ID token 的唯一識別碼，通常會使用網址表示
		payloadMap.put("aud", clientId); // REQUIRED, Token 的接受者, 應用程式所註冊的 client_id
		payloadMap.put("sub", userName); // REQUIRED, 使用者的唯一識別碼
		payloadMap.put("at_hash", at_hash); // OPTIONAL, 從 Access Token 取得部分的資訊雜湊後的結果
		payloadMap.put("iat", iat); // REQUIRED, Token 的發行時間
		payloadMap.put("exp", idTokenExp); // REQUIRED, Token 失效或過期的時間

		if (reqScopeList.contains(DgrOpenIDConnectScope.EMAIL)) {
			if (StringUtils.hasLength(userEmail)) {// 有值才顯示
				payloadMap.put("email", userEmail);
			}
		}

		if (reqScopeList.contains(DgrOpenIDConnectScope.PROFILE)) {
			if (StringUtils.hasLength(userAlias)) {// 有值才顯示
				payloadMap.put("name", userAlias);
			}

			if (StringUtils.hasLength(picture)) {// 有值才顯示
				payloadMap.put("picture", picture);
			}
		}
		
		if (StringUtils.hasLength(idtLightId)) {// 有值才顯示
			payloadMap.put("lightId", idtLightId);
		}

		if (StringUtils.hasLength(idtRoleName)) {// 有值才顯示
			Boolean isArray = checkIdtRoleNameIsArray(idtRoleName);
			Boolean isObject = checkIdtRoleNameIsObject(idtRoleName);
			if (isArray)
				payloadMap.put("roleName", getObjectMapper().readValue(idtRoleName, List.class));
			else if (isObject)
				payloadMap.put("roleName", getObjectMapper().readValue(idtRoleName, Object.class));
			else
				payloadMap.put("roleName", idtRoleName);
		}

		String payload = null;
		try {
			payload = getObjectMapper().writeValueAsString(payloadMap);
		} catch (JsonProcessingException e) {
			TPILogger.tl.debug(StackTraceUtil.logStackTrace(e));
		}

		String idToken = getIdTokenJws(payload);
		return idToken;
	}

	private Boolean checkIdtRoleNameIsObject(String idtRoleName) {
		try {
			return getObjectMapper().readTree(idtRoleName).isObject();
		} catch (JsonProcessingException e) {
			return false;
		}
	}

	private Boolean checkIdtRoleNameIsArray(String idtRoleName) {
		try {
			return getObjectMapper().readTree(idtRoleName).isArray();
		} catch (JsonProcessingException e) {
			return false;
		}
	}
	/**
	 * 產生 ID Token 的 at_hash, 從 Access Token 取得部分的資訊雜湊後的結果, 是用來確認 ID Token 與 Access
	 * Token 兩個關係的一致性
	 */
	protected String getAtHash(String accessTokenJwtstr) {
		// 1.將 Access Token JWT 格式的值, 做 SHA-256, 取左半部份
		byte[] digest = SHA256Util.getSHA256(accessTokenJwtstr.getBytes());
		byte[] result = new byte[digest.length / 2];
		for (int i = 0; i < result.length; i++) {// 取左半部份
			result[i] = digest[i];
		}

		// 2.將左半部份,做 Base64UrlEncode, 得到 at_hash
		String atHash = Base64Util.base64URLEncode(result);

		return atHash;
	}

	/**
	 * 將 ID Token 的 payload 轉成 JWS 格式
	 */
	private String getIdTokenJws(String idTokenPayload) throws Exception {
		// 隨機選 JWK(2擇1)
		String jwkStr = getJwk();
		JWK jwk = JWK.parse(jwkStr);
		PrivateKey privateKey = jwk.toRSAKey().toKeyPair().getPrivate();

		// 取得 JWK 中的 kid 值
		JsonNode rootNode = getObjectMapper().readTree(jwkStr);
		String kid = JsonNodeUtil.getNodeAsText(rootNode, "kid");

		// 簽章
		String idTokenJws = JWScodec.jwsSign(privateKey, idTokenPayload, kid);
		return idTokenJws;
	}

	/**
	 * 隨機選JWK(2擇1), 用來做 ID Token 簽章
	 */
	private String getJwk() {
		// 1.二組JWK的機率與資料,機率各一半(50%)
		List<String[]> dataList = new LinkedList<String[]>();
		dataList.add(new String[] { "50", "1" });
		dataList.add(new String[] { "50", "2" });

		// 2.取出機率對應的值
		String ans = ProbabilityAlgUtils.getProbabilityAns(dataList);

		// 3.用機率結果, 決定用哪一組JWK
		String jwkStr = null;
		if ("1".equals(ans)) {
			jwkStr = getTsmpSettingService().getVal_GTW_IDP_JWK1();// 第1組
		} else {
			jwkStr = getTsmpSettingService().getVal_GTW_IDP_JWK2();// 第2組
		}

		return jwkStr;
	}

	/**
	 * 檢查 client 帳號密碼
	 */
	protected ResponseEntity<?> verifyAuth(String grantType, String clientId, String clientMima, String codeVerifier,
			String reqUri) {
		// 沒有 clientId, 或 client 狀態不正確
		ResponseEntity<?> respEntity = getTokenHelper().checkClientStatus(clientId, reqUri);
		if (respEntity != null) {// 資料驗證有錯誤
			return respEntity;
		}

		if (!StringUtils.hasLength(clientMima)) {// 沒有密碼
			if (DgrTokenGrantType.AUTHORIZATION_CODE.equalsIgnoreCase(grantType)) {// authorization_code 沒有密碼
				// 如果是 Auth code flow, 若是 Public client + PKCE 可以沒有密碼

				// 檢查 client 是否有授權 Public
				respEntity = getTokenHelper().checkClientSupportGrantType(clientId, "Public", reqUri);
				if (respEntity != null) {// 若沒有授權 Public client, 就必須有 client_secret
					String errMsg = "Missing client_secret";
					TPILogger.tl.debug(errMsg);
					return new ResponseEntity<OAuthTokenErrorResp2>(
							getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
							HttpStatus.BAD_REQUEST);// 400

				} else {// 若有授權 Public client 且沒有密碼, 就必須有 PKCE 的 code_verifier
					if (!StringUtils.hasLength(codeVerifier)) {// 沒有 code_verifier
						TPILogger.tl.debug("Public client needs to use PKCE");
						String errMsg = "Missing code_verifier";
						TPILogger.tl.debug(errMsg);
						return new ResponseEntity<OAuthTokenErrorResp2>(
								getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
								HttpStatus.BAD_REQUEST);// 400
					}
				}
			} else if (DgrTokenGrantType.REFRESH_TOKEN.equalsIgnoreCase(grantType)) {// refresh_token 沒有密碼

				// 檢查 client 是否有授權 Public
				respEntity = getTokenHelper().checkClientSupportGrantType(clientId, "Public", reqUri);
				if (respEntity != null) {// 若沒有授權 Public client, 就必須有 client_secret
					String errMsg = "Missing client_secret";
					TPILogger.tl.debug(errMsg);
					return new ResponseEntity<OAuthTokenErrorResp2>(
							getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
							HttpStatus.BAD_REQUEST);// 400
				}


			} else {// 其他 grant type 沒有密碼
				String errMsg = "Missing client_secret";
				TPILogger.tl.debug(errMsg);
				return new ResponseEntity<OAuthTokenErrorResp2>(
						getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
						HttpStatus.BAD_REQUEST);// 400
			}

		}else {// 有密碼
			// 查無 client 或 client 帳密不對
			respEntity = getTokenHelper().checkClientMima(clientId, clientMima, reqUri);
			if (respEntity != null) {// client資料驗證有錯誤
				return respEntity;
			}
		}

		return null;
	}

	/**
	 * 檢查 Body 資料
	 */
	protected ResponseEntity<?> verifyBody(OAuthTokenData oauthTokenData, Map<String, String> parameters,
			String grantType, String clientId, String reqUri) {
		ResponseEntity<?> respEntity = null;

		// 1.Body 沒有 grant_type
		if (grantType == null) {
			String errMsg = "Missing grant type";// Body 沒有 grant_type
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp2>(
					getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
					HttpStatus.BAD_REQUEST);// 400
		}

		// 2.Body 有 grant_type, 但沒有值
		if ("".equals(grantType)) {
			TPILogger.tl.debug("Body has grant_type, but no value");// Body 有 grant_type, 但沒有值
			String errMsg = "Full authentication is required to access this resource";// 訪問此資源需要完全身份驗證
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp2>(
					getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.UNAUTHORIZED_2, errMsg),
					HttpStatus.UNAUTHORIZED);// 401
		}

		// 3.檢查是否為支援的 grant type
		if (DgrTokenGrantType.DELEGATE_AUTH.equalsIgnoreCase(grantType)) {
			// 不用檢查
		} else {
			respEntity = getTokenHelper().checkSupportGrantType(grantType);
			if (respEntity != null) {
				return respEntity;
			}
		}

		// 4.檢查是否為 client 被授權的 grant type
		if (DgrTokenGrantType.DELEGATE_AUTH.equalsIgnoreCase(grantType)) {
			// 不用檢查
		} else {
			respEntity = getTokenHelper().checkClientSupportGrantType(clientId, grantType, reqUri);
			if (respEntity != null) {
				return respEntity;
			}
		}

		// 5.依 grant type 檢查資料
		if (DgrTokenGrantType.PASSWORD.equalsIgnoreCase(grantType)) {
			// 依 grant type = "password" 檢查資料
			respEntity = verifyBodyForPassword(parameters, reqUri);
			if (respEntity != null) {
				return respEntity;
			}
		} else if (DgrTokenGrantType.REFRESH_TOKEN.equalsIgnoreCase(grantType)) {
			// 依 grant type = "refresh_token" 檢查資料
			respEntity = verifyBodyForRefreshToken(oauthTokenData, parameters, clientId, reqUri);
			if (respEntity != null) {
				return respEntity;
			}
		} else if (DgrTokenGrantType.AUTHORIZATION_CODE.equalsIgnoreCase(grantType)) {
			// 依 grant type = "authorization_code" 檢查資料
			respEntity = verifyBodyForAuthorizationCode(oauthTokenData, parameters, clientId, reqUri);
			if (respEntity != null) {
				return respEntity;
			}
		} else if (DgrTokenGrantType.DELEGATE_AUTH.equalsIgnoreCase(grantType)) {
			// 依 grant type = "delegate_auth" 檢查資料
			respEntity = verifyBodyForDelegateAuth(oauthTokenData, parameters, reqUri);
			if (respEntity != null) {
				return respEntity;
			}
		}

		return null;
	}

	/**
	 * 檢查 Body 資料, grant type = "password"
	 */
	private ResponseEntity<?> verifyBodyForPassword(Map<String, String> parameters, String reqUri) {
		String errMsg = "";
		String userName = parameters.get("username");
		// 1.Body 沒有 username
		if (userName == null) {
			TPILogger.tl.debug("Body has no username");// Body 沒有 username
			errMsg = "Bad credentials";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp2>(
					getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_GRANT, errMsg),
					HttpStatus.BAD_REQUEST);// 400
		}

		// 2.Body 有 username, 但沒有值
		if ("".equals(userName)) {
			TPILogger.tl.debug("Body has username, but no value");// Body 有 username, 但沒有值
			errMsg = "Full authentication is required to access this resource";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp2>(
					getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.UNAUTHORIZED_2, errMsg),
					HttpStatus.UNAUTHORIZED);// 401
		}

		// 3.Body 沒有 password
		String userPw = parameters.get("password");
		if (!StringUtils.hasText(userPw)) {
			TPILogger.tl.debug("Body has no password");// Body 沒有 password
			errMsg = "Bad credentials";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp2>(
					getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_GRANT, errMsg),
					HttpStatus.BAD_REQUEST);// 400
		}

		// 4.Body 有 password, 但沒有值
		if ("".equals(userPw)) {
			TPILogger.tl.debug("Body has password, but no value");// Body 有 password, 但沒有值
			errMsg = "Full authentication is required to access this resource";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp2>(
					getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.UNAUTHORIZED_2, errMsg),
					HttpStatus.UNAUTHORIZED);// 401
		}

		// 5.檢查 user 狀態
		ResponseEntity<?> respEntity = getTokenHelper().checkUserStatus(userName, reqUri);
		if (respEntity != null) {// 資料有錯誤
			return respEntity;
		}

		// 6.檢查 無 user 或 user 帳密不對
		respEntity = getTokenHelper().checkUserSecret(userName, userPw, reqUri);
		if (respEntity != null) {// 資料有錯誤
			return respEntity;
		}

		return null;
	}

	/**
	 * 檢查 Body 資料, grant type = "refresh_token"
	 */
	private ResponseEntity<?> verifyBodyForRefreshToken(OAuthTokenData oauthTokenData, Map<String, String> parameters,
			String clientId, String reqUri) {
		String refreshToken = parameters.get("refresh_token");

		// 1.Body 沒有 refresh_token
		ResponseEntity<?> respEntity = getTokenHelper().checkHasToken(refreshToken, "refresh_token");
		if (respEntity != null) {// 資料驗證有錯誤
			return respEntity;
		}

		// 2.由 refresh_token 取得資料, 驗證 JWS 簽章 或 JWE 解密
		JwtPayloadData jwtPayloadData = getTokenHelper().getJwtPayloadData(refreshToken);
		respEntity = jwtPayloadData.errRespEntity;
		if (respEntity != null) {// 資料有錯誤
			return respEntity;
		}

		JsonNode payloadJsonNode = jwtPayloadData.payloadJsonNode;
		String retokenClientId = JsonNodeUtil.getNodeAsText(payloadJsonNode, "client_id");
		Long retokenExp = JsonNodeUtil.getNodeAsLong(payloadJsonNode, "exp");
		String retokenUserName = JsonNodeUtil.getNodeAsText(payloadJsonNode, "user_name");
		String idPType = JsonNodeUtil.getNodeAsText(payloadJsonNode, "idp_type");

		// 3.refresh token 的 client_id 沒有值,
		// 或 Headers 的 Authorization 和 Body 的 refresh_token 中的 client_id 值不相同
		respEntity = getTokenHelper().checkTokenClientId(clientId, retokenClientId, refreshToken, "refresh token");
		if (respEntity != null) {// 資料驗證有錯誤
			return respEntity;
		}

		// 4.refresh token exp 沒有值 或 refresh token 過期
		respEntity = getTokenHelper().checkRefreshTokenExp(refreshToken, retokenExp);
		if (respEntity != null) {// 資料驗證有錯誤
			return respEntity;
		}

		// 5.refresh token 是否已撤銷
		String retokenJti = JsonNodeUtil.getNodeAsText(payloadJsonNode, "jti");
		respEntity = getTokenHelper().checkRefreshTokenRevoked(retokenJti);
		if (respEntity != null) {// 資料有錯誤
			return respEntity;
		}

		// 6.refresh_token 的 user_name 沒有值 和 檢查 user / IdP user 狀態
		respEntity = getTokenHelper().checkRefreshTokenUserName(payloadJsonNode, retokenUserName, reqUri);
		if (respEntity != null) {// 資料有錯誤
			return respEntity;
		}

		// 7.檢查 refresh token 可用量 和 重取 token 成功後, refresh token 使用量加1
		boolean isAcIdPFlow = TokenHelper.isAcIdPFlow(retokenUserName, idPType);
		if (isAcIdPFlow) {
			// AC IdP 流程的 refresh token 不用檢查

		} else {
			respEntity = checkClientRefreshTokenQuota(oauthTokenData, clientId, retokenJti, reqUri);
			if (respEntity != null) {// 資料驗證有錯誤
				return respEntity;
			}
		}

		return null;
	}

	/**
	 * 檢查 Body 資料, grant type = "authorization_code"
	 */
	private ResponseEntity<?> verifyBodyForAuthorizationCode(OAuthTokenData oauthTokenData,
			Map<String, String> parameters, String clientId, String reqUri) {
		String errMsg = "";
		String code = parameters.get("code");// auth code
		String redirectUri = parameters.get("redirect_uri");
		String codeVerifier = parameters.get("code_verifier");

		// 1.Body 沒有 code
		if (!StringUtils.hasLength(code)) {
			errMsg = "Missing required parameter: code";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp2>(
					getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
					HttpStatus.BAD_REQUEST);// 400
		}

		// 2.檢查傳入的 redirect_uri 和 client 註冊在 digiRunner 中的
		// (OAUTH_CLIENT_DETAILS.web_server_redirect_uri) 是否相同
		ResponseEntity<?> respEntity = getTokenHelper().checkRedirectUri(clientId, redirectUri, reqUri);
		if (respEntity != null) {
			return respEntity;
		}

		// 3.取得 App 打 dgR auth API 時,
		// (1).傳入的 OIDC scope
		// (2).或 GTW IdP (LDAP / JDBC) 時, user 在同意畫面勾選的存取範圍
		DgrGtwIdpAuthM dgrGtwIdpAuthM = getDgrGtwIdpAuthMDao().findFirstByAuthCodeAndClientId(code, clientId);
		if (dgrGtwIdpAuthM == null) {
			// Table [DGR_GTW_IDP_AUTH_M] 查不到資料
			TPILogger.tl.debug(
					"Table [DGR_GTW_IDP_AUTH_M] can't find data. auth_code:" + code + ", client_id: " + clientId);
			errMsg = "Invalid auth code";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp2>(
					getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
					HttpStatus.BAD_REQUEST);// 400
		}

		long gtwIdpAuthMId = dgrGtwIdpAuthM.getGtwIdpAuthMId();
		String state = dgrGtwIdpAuthM.getState();

		// 取得 scope
		List<DgrGtwIdpAuthD> dgrGtwIdpAuthDList = getDgrGtwIdpAuthDDao().findByRefGtwIdpAuthMId(gtwIdpAuthMId);
		if (CollectionUtils.isEmpty(dgrGtwIdpAuthDList)) {
			// Table [DGR_GTW_IDP_AUTH_D] 查不到資料
			TPILogger.tl.debug("Table [DGR_GTW_IDP_AUTH_D] can't find data. ref_gtw_idp_auth_m_id: " + gtwIdpAuthMId);
			errMsg = TokenHelper.UNAUTHORIZED;
			TPILogger.tl.debug(errMsg);
			return getTokenHelper().getUnauthorizedErrorResp(reqUri, errMsg);// 401
		}

		String scopeStr = "";
		for (DgrGtwIdpAuthD dgrGtwIdpAuthD : dgrGtwIdpAuthDList) {
			scopeStr += dgrGtwIdpAuthD.getScope() + " ";
		}
		oauthTokenData.oidcAndVgroupScopeStr = scopeStr.trim();

		// 4.檢查 auth code 狀態 & 是否過期, 更新 auth code 狀態為已使用
		// 5.檢查 code verifier(PKCE)是否匹配
		String codeChallenge = dgrGtwIdpAuthM.getCodeChallenge();
		String codeChallengeMethod = dgrGtwIdpAuthM.getCodeChallengeMethod();
		respEntity = checkDgrGtwIdpAuthCodeAndUpdate(oauthTokenData, code, clientId, state, codeVerifier, codeChallenge,
				codeChallengeMethod, reqUri);
		if (respEntity != null) {
			return respEntity;
		}

		return null;
	}

	/**
	 * for AC IdP 流程, 檢查 Body 資料, grant type = "delegate_auth"
	 */
	private ResponseEntity<?> verifyBodyForDelegateAuth(OAuthTokenData oauthTokenData, Map<String, String> parameters,
			String reqUri) {
		String errMsg = "";
		String code = parameters.get("code");// dgRcode

		// 1.Body 沒有 code
		if (code == null) {
			TPILogger.tl.debug("Body has no code");// Body 沒有 code
			errMsg = "Missing code";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp2>(
					getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
					HttpStatus.BAD_REQUEST);// 400
		}

		// 2.Body 有 code, 但沒有值
		if ("".equals(code)) {
			TPILogger.tl.debug("Body has code, but no value");// Body 有 code, 但沒有值
			errMsg = "Missing code";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp2>(
					getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.UNAUTHORIZED_2, errMsg),
					HttpStatus.UNAUTHORIZED);// 401
		}

		// 5.檢查 code, 即 dgRcode(auth code) 狀態 & 是否過期, 更新 auth code 狀態為已使用
		ResponseEntity<?> respEntity = checkDgrAcIdpAuthCodeAndUpdate(oauthTokenData, code, reqUri);
		if (respEntity != null) {
			return respEntity;
		}

		// 6.檢查 IdP user 狀態
		respEntity = getTokenHelper().checkUserStatusByDgrAcIdpUser(oauthTokenData.idPUserName, reqUri,
				oauthTokenData.idPType);
		if (respEntity != null) {// 資料有錯誤
			return respEntity;
		}

		return null;
	}

	/**
	 * 檢查資料, grant type = "cookie token"
	 */
	protected ResponseEntity<?> verifyForCookieToken(TsmpTokenHistory tsmpTokenHistory, String accessTokenJti,
			String idPType, String clientId, String reqUri) {
		ResponseEntity<?> errRespEntity = null;
		String errMsg = "";

		// 1.沒有 jti
		if (!StringUtils.hasLength(accessTokenJti)) {
			errMsg = "Missing cookie jti";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp2>(
					getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
					HttpStatus.BAD_REQUEST);// 400
		}

		// 2.沒有 idPType
		if (!StringUtils.hasLength(idPType)) {
			errMsg = "Missing cookie idPType";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp2>(
					getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
					HttpStatus.BAD_REQUEST);// 400
		}

		// 3.TSMP_TOKEN_HISTORY
		if (tsmpTokenHistory == null) {
			errRespEntity = getTokenHelper().getFindTsmpTokenHistoryError(accessTokenJti, idPType, reqUri);
			return errRespEntity;
		}

		String refreshTokenJwtstr = tsmpTokenHistory.getRefreshTokenJwtstr();
		String retokenJti = tsmpTokenHistory.getRetokenJti();
		Date reexpiredAt = tsmpTokenHistory.getReexpiredAt();
		Long retokenExp = 0L;
		if (reexpiredAt != null) {
			retokenExp = reexpiredAt.getTime() / 1000;// 去掉亳秒
		}

		// 4.沒有 refreshTokenJwtstr
		if (!StringUtils.hasLength(refreshTokenJwtstr)) {
			errMsg = "Missing refreshTokenJwtstr";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp2>(
					getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.UNAUTHORIZED_2, errMsg),
					HttpStatus.UNAUTHORIZED);// 401
		}

		// 5.沒有 clientId, 或 client 狀態不正確
		errRespEntity = getTokenHelper().checkClientStatus(clientId, reqUri);
		if (errRespEntity != null) {// 資料驗證有錯誤
			return errRespEntity;
		}

		// 6.是否 access token 已撤銷
		errRespEntity = getTokenHelper().checkAccessTokenRevoked(tsmpTokenHistory, accessTokenJti);
		if (errRespEntity != null) {// 資料有錯誤
			return errRespEntity;
		}

		// 7.refresh token exp 沒有值 或 refresh token 過期
		errRespEntity = getTokenHelper().checkRefreshTokenExp(null, retokenExp);
		if (errRespEntity != null) {// 資料驗證有錯誤
			return errRespEntity;
		}

		// 8.refresh token 是否已撤銷
		errRespEntity = getTokenHelper().checkRefreshTokenRevoked(retokenJti);
		if (errRespEntity != null) {// 資料有錯誤
			return errRespEntity;
		}

		return null;
	}

	/**
	 * Table: DGR_GTW_IDP_AUTH_CODE, <br>
	 * Gateway IdP : <br>
	 * 1.檢查 auth code 狀態 & 是否過期 <br>
	 * 2.檢查 code verifier(PKCE)是否匹配 <br>
	 * 3.更新 auth code 狀態為已使用 <br>
	 */
	private ResponseEntity<?> checkDgrGtwIdpAuthCodeAndUpdate(OAuthTokenData oauthTokenData, String authCode,
			String clientId, String state, String codeVerifier, String codeChallenge, String codeChallengeMethod,
			String reqUri) {

		DgrGtwIdpAuthCode dgrGtwIdpAuthCode = getDgrGtwIdpAuthCodeDao().findFirstByAuthCodeAndPhase(authCode,
				DgrAuthCodePhase.AUTH_CODE);

		// 1.查無 DGR_GTW_IDP_AUTH_CODE
		if (dgrGtwIdpAuthCode == null) {
			// Table [DGR_GTW_IDP_AUTH_CODE] 查不到
			TPILogger.tl.debug("Table [DGR_GTW_IDP_AUTH_CODE] can't find data. auth_code:" + authCode);
			String errMsg = TokenHelper.UNAUTHORIZED;
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp>(
					getTokenHelper().getOAuthTokenErrorResp(errMsg, errMsg, HttpStatus.UNAUTHORIZED.value(), reqUri),
					HttpStatus.UNAUTHORIZED);// 401
		}

		// 2.auth code 狀態是否為 "可用"
		String authCodeStatus = dgrGtwIdpAuthCode.getStatus();
		if (!TsmpAuthCodeStatus2.AVAILABLE.value().equals(authCodeStatus)) {// 若 auth code 狀態不是"可用"
			if (TsmpAuthCodeStatus2.USED.value().equals(authCodeStatus)) {// auth code 狀態為 "已使用"
				String stateEngText = TsmpAuthCodeStatus2.USED.engText();
				authCodeStatus = "'" + authCodeStatus + "' (" + stateEngText + ")";// 狀態的英文
			} else {
				authCodeStatus = "'" + authCodeStatus + "'";
			}
			String errMsg1 = TokenHelper.UNAUTHORIZED;
			String errMsg2 = "Auth code status is " + authCodeStatus + ". auth_code: " + authCode;
			TPILogger.tl.debug(errMsg1 + "\n" + errMsg2);
			return new ResponseEntity<OAuthTokenErrorResp>(
					getTokenHelper().getOAuthTokenErrorResp(errMsg1, errMsg2, HttpStatus.UNAUTHORIZED.value(), reqUri),
					HttpStatus.UNAUTHORIZED);// 401
		}

		// 3.判斷 auth code 是否過期
		long authCodeExpire = dgrGtwIdpAuthCode.getExpireDateTime();
		long nowTime = System.currentTimeMillis();
		if (authCodeExpire < nowTime) {// auth code 過期
			String expireTime = DateTimeUtil.dateTimeToString(new Date(authCodeExpire), DateTimeFormatEnum.西元年月日時分秒毫秒)
					.orElse(null);
			String errMsg1 = TokenHelper.UNAUTHORIZED;
			String errMsg2 = "Auth code expired. auth code exp: " + authCodeExpire + " (" + expireTime + ")";
			TPILogger.tl.debug(errMsg2);
			return new ResponseEntity<OAuthTokenErrorResp>(
					getTokenHelper().getOAuthTokenErrorResp(errMsg1, errMsg2, HttpStatus.UNAUTHORIZED.value(), reqUri),
					HttpStatus.UNAUTHORIZED);// 401
		}

		oauthTokenData.idPType = dgrGtwIdpAuthCode.getIdpType();
		oauthTokenData.idPUserName = dgrGtwIdpAuthCode.getUserName();
		oauthTokenData.idPUserEmail = dgrGtwIdpAuthCode.getUserEmail();
		oauthTokenData.idPUserAlias = dgrGtwIdpAuthCode.getUserAlias();
		oauthTokenData.idPUserPicture = dgrGtwIdpAuthCode.getUserPicture();
		oauthTokenData.apiResp = dgrGtwIdpAuthCode.getApiResp();
		oauthTokenData.idtLightId = dgrGtwIdpAuthCode.getUserLightId();
		oauthTokenData.idtRoleName = dgrGtwIdpAuthCode.getUserRoleName();
		// 4.更新 auth code 狀態為已使用
		dgrGtwIdpAuthCode.setStatus(TsmpAuthCodeStatus2.USED.value());// 已使用
		dgrGtwIdpAuthCode.setClientId(clientId);

		dgrGtwIdpAuthCode.setUpdateUser("SYSTEM");
		dgrGtwIdpAuthCode.setUpdateDateTime(DateTimeUtil.now());
		dgrGtwIdpAuthCode = getDgrGtwIdpAuthCodeDao().saveAndFlush(dgrGtwIdpAuthCode);

		// 5.判斷 code_challenge 和 code_verifier 是否匹配
		// 若 PKCE 值不對,code 要算有使用過,所以在此之前先改為已使用
		ResponseEntity<?> errRespEntity = checkPkce(codeChallenge, codeChallengeMethod, codeVerifier);
		if (errRespEntity != null) {// 資料有錯誤
			return errRespEntity;
		}

		return null;
	}

	/**
	 * 檢查 PKCE 的值
	 */
	private ResponseEntity<?> checkPkce(String codeChallenge, String codeChallengeMethod, String codeVerifier) {
		if (StringUtils.hasLength(codeChallenge)) {
			// 如果有 code_challenge, 則必須有 code_verifier
			if (!StringUtils.hasLength(codeVerifier)) {// 沒有 code_verifier
				// 有 code_challenge, 但沒有 code_verifier
				TPILogger.tl.debug("There is code_challenge, but no code_verifier");
				String errMsg = "Missing code_verifier";
				TPILogger.tl.debug(errMsg);
				return new ResponseEntity<OAuthTokenErrorResp2>(
						getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
						HttpStatus.BAD_REQUEST);// 400
			}
		}

		if (StringUtils.hasLength(codeVerifier)) {
			// 如果有 code_verifier, 則必須有 code_challenge
			if (!StringUtils.hasLength(codeChallenge)) {// 沒有 code_challenge
				// 有 code_verifier, 但沒有 code_challenge
				TPILogger.tl.debug("There is code_verifier, but no code_challenge");
				String errMsg = "Invalid code_verifier";
				TPILogger.tl.debug(errMsg);
				return new ResponseEntity<OAuthTokenErrorResp2>(
						getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
						HttpStatus.BAD_REQUEST);// 400
			}
		}

		if (StringUtils.hasLength(codeChallenge) && StringUtils.hasLength(codeVerifier)) {
			// 比對二者是否匹配
			String codeVerifierEn = null;
			if (DgrCodeChallengeMethod.S256.equals(codeChallengeMethod)) {
				codeVerifierEn = SHA256Util.getSHA256ToBase64Url(codeVerifier);
			}

			if (!codeChallenge.equals(codeVerifierEn)) {
				String errMsg = String.format(
						"The code_verifier mismatch. \n" + "codeChallenge: %s, \n" + "t(codeVerifier): %s",
						codeChallenge, codeVerifierEn);
				TPILogger.tl.debug(errMsg);

				errMsg = "Invalid code_verifier";
				TPILogger.tl.debug(errMsg);
				return new ResponseEntity<OAuthTokenErrorResp2>(
						getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
						HttpStatus.BAD_REQUEST);// 400
			}
		}

		return null;
	}

	/**
	 * Table: DGR_AC_IDP_AUTH_CODE, <br>
	 * SSO AC IdP : <br>
	 * 1.檢查 dgRcode 狀態 & 是否過期, <br>
	 * 2.更新 dgRcode 狀態為已使用 <br>
	 */
	private ResponseEntity<?> checkDgrAcIdpAuthCodeAndUpdate(OAuthTokenData oauthTokenData, String dgRcode,
			String reqUri) {

		DgrAcIdpAuthCode dgrAcIdpAuthCode = getDgrAcIdpAuthCodeDao().findFirstByAuthCode(dgRcode);

		// 1.查無 DGR_AC_IDP_AUTH_CODE
		if (dgrAcIdpAuthCode == null) {
			// Table [DGR_AC_IDP_AUTH_CODE] 查不到
			TPILogger.tl.debug("Table [DGR_AC_IDP_AUTH_CODE] can't find data, auth_code:" + dgRcode);
			String errMsg = TokenHelper.UNAUTHORIZED;
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp>(
					getTokenHelper().getOAuthTokenErrorResp(errMsg, errMsg, HttpStatus.UNAUTHORIZED.value(), reqUri),
					HttpStatus.UNAUTHORIZED);// 401
		}

		// 2.auth code 狀態是否為 "可用"
		String authCodeStatus = dgrAcIdpAuthCode.getStatus();
		if (!TsmpAuthCodeStatus2.AVAILABLE.value().equals(authCodeStatus)) {// 若 auth code 狀態不是"可用"
			// auth code 狀態為 "已使用"
			TPILogger.tl.debug("Auth code(dgRcode) is " + authCodeStatus + ", auth_code:" + dgRcode);
			String errMsg = TokenHelper.UNAUTHORIZED;
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp>(
					getTokenHelper().getOAuthTokenErrorResp(errMsg, errMsg, HttpStatus.UNAUTHORIZED.value(), reqUri),
					HttpStatus.UNAUTHORIZED);// 401
		}

		// 3.判斷 auth code 是否過期
		long authCodeExpire = dgrAcIdpAuthCode.getExpireDateTime();
		long nowTime = System.currentTimeMillis();
		if (authCodeExpire < nowTime) {// auth code 過期
			String expireTime = DateTimeUtil.dateTimeToString(new Date(authCodeExpire), DateTimeFormatEnum.西元年月日時分秒毫秒)
					.orElse(null);
			TPILogger.tl.debug("Auth code(dgRcode) expired, auth code exp:" + authCodeExpire + " (" + expireTime + ")");
			String errMsg = TokenHelper.UNAUTHORIZED;
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp>(
					getTokenHelper().getOAuthTokenErrorResp(errMsg, errMsg, HttpStatus.UNAUTHORIZED.value(), reqUri),
					HttpStatus.UNAUTHORIZED);// 401
		}

		oauthTokenData.idPUserName = dgrAcIdpAuthCode.getUserName();
		oauthTokenData.idPType = dgrAcIdpAuthCode.getIdpType();
		oauthTokenData.apiResp = dgrAcIdpAuthCode.getApiResp();

		// 4.更新 auth code 狀態為已使用
		dgrAcIdpAuthCode.setStatus(TsmpAuthCodeStatus2.USED.value());// 已使用
		dgrAcIdpAuthCode.setUpdateDateTime(DateTimeUtil.now());
		dgrAcIdpAuthCode.setUpdateUser("SYSTEM");
		dgrAcIdpAuthCode = getDgrAcIdpAuthCodeDao().saveAndFlush(dgrAcIdpAuthCode);

		return null;
	}

	private List<String> getRoleIdList(String userName) {
		List<String> roleIdList = new ArrayList<>();

		List<Authorities> authoritiesList = getAuthoritiesDao().findByUsername(userName);
		if (!CollectionUtils.isEmpty(authoritiesList)) {
			for (Authorities authorities : authoritiesList) {
				String roleId = authorities.getAuthority();
				roleIdList.add(roleId);
			}
		}

		return roleIdList;
	}

	/**
	 * 不用處理: 1.grantType 為 "refresh_token" 2.AC IdP 的 "delegate_auth" <br>
	 * 其他: 計算 client token 可用量
	 */
	private ResponseEntity<?> doClientTokenQuota(OAuthTokenData oauthTokenData, Map<String, String> parameters,
			String grantType, String clientId, String retokenJti, String reqUri) {
		if (DgrTokenGrantType.REFRESH_TOKEN.equalsIgnoreCase(grantType)
				|| DgrTokenGrantType.DELEGATE_AUTH.equalsIgnoreCase(grantType)) {
			// 不用處理
			return null;
		}

		// 查詢 TSMP_CLIENT
		Optional<TsmpClient> opt_client = getTsmpClientDao().findById(clientId);
		if (!opt_client.isPresent()) {
			ResponseEntity<?> errRespEntity = getTokenHelper().getFindTsmpClientError(clientId, reqUri);
			return errRespEntity;
		}

		TsmpClient tsmpClient = opt_client.get();
		Integer accessTokenQuota = tsmpClient.getAccessTokenQuota() == null ? 0 : tsmpClient.getAccessTokenQuota();
		Integer refreshTokenQuota = tsmpClient.getRefreshTokenQuota() == null ? 0 : tsmpClient.getRefreshTokenQuota();

		oauthTokenData.tokenQuota = accessTokenQuota.longValue();
		oauthTokenData.tokenUsed = 0L;
		if (DgrTokenGrantType.CLIENT_CREDENTIALS.equalsIgnoreCase(grantType)) {
			// 沒有 refresh token
			oauthTokenData.rftQuota = null;
			oauthTokenData.rftUsed = null;
		} else {
			oauthTokenData.rftQuota = refreshTokenQuota.longValue();
			oauthTokenData.rftUsed = 0L;
		}

		return null;
	}

	/**
	 * 檢查 client refresh token 可用量 和重取 token 成功後, refresh token使用量 加1
	 */
	private ResponseEntity<?> checkClientRefreshTokenQuota(OAuthTokenData oauthTokenData, String clientId,
			String retokenJti, String reqUri) {
		// 查詢 TSMP_CLIENT
		Optional<TsmpClient> opt_client = getTsmpClientDao().findById(clientId);
		if (!opt_client.isPresent()) {
			ResponseEntity<?> errRespEntity = getTokenHelper().getFindTsmpClientError(clientId, reqUri);
			return errRespEntity;
		}

		TsmpClient tsmpClient = opt_client.get();
		Integer accessTokenQuota = tsmpClient.getAccessTokenQuota() == null ? 0 : tsmpClient.getAccessTokenQuota();

		// 查詢 TSMP_TOKEN_HISTORY, 此 refresh token 的最新記錄
		TsmpTokenHistory tsmpTokenHistory = getTsmpTokenHistoryDao()
				.findFirstByRetokenJtiOrderByCreateAtDesc(retokenJti);
		if (tsmpTokenHistory == null) {
			// Table [TSMP_TOKEN_HISTORY] 查不到資料
			TPILogger.tl.debug("Table [TSMP_TOKEN_HISTORY] can't find data, retoken_jti:" + retokenJti);

			// refresh token 已撤銷
			ResponseEntity<?> respEntity = getTokenHelper().getRefreshTokenRevokedError(retokenJti);
			return respEntity;
		}

		Long rftQuota = tsmpTokenHistory.getRftQuota() == null ? 0 : tsmpTokenHistory.getRftQuota();
		Long rftUsed = tsmpTokenHistory.getRftUsed() == null ? 0 : tsmpTokenHistory.getRftUsed();
		Long newRftQuota = null;
		Long newRftUsed = null;
		if (rftQuota == null || rftQuota == 0) {
			// 若 rft_quota 為 null 或 0, 則為不限次數, 不檢查 且 使用量不加1
			newRftQuota = 0L;
			newRftUsed = 0L;

		} else if (rftUsed >= rftQuota) {
			// 否則, 若 rft_used >= rft_quota, refresh token 額度已滿不可使用
			String errMsg = "Over Refresh Token Allow Times";// 超過 refresh token 允許次數
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp2>(getTokenHelper().getOAuthTokenErrorResp2(errMsg, errMsg),
					HttpStatus.FORBIDDEN);// 403

		} else {
			// 否則, rft_used 加1
			newRftQuota = rftQuota;
			newRftUsed = rftUsed + 1;
		}

		oauthTokenData.tokenQuota = accessTokenQuota.longValue();
		oauthTokenData.tokenUsed = 0L;
		oauthTokenData.rftQuota = newRftQuota;
		oauthTokenData.rftUsed = newRftUsed;

		return null;
	}

	private Map<String, String> getEsLogParams(HttpServletRequest httpReq) {
		Map<String, String> logParams = new HashMap<>();
		logParams.put("url", httpReq.getRequestURI());
		logParams.put("queryString", httpReq.getQueryString());
		//這段 Regex 已被 Tom Review 過了, 故取消 hotspot 標記
		Pattern p = Pattern.compile("/?([^/]+).*"); // NOSONAR
		Matcher m = p.matcher(httpReq.getRequestURI());
		if (m.matches()) {
			logParams.put("moduleName", "/" + m.group(1));
		} else {
			logParams.put("moduleName", httpReq.getRequestURI());
		}
		logParams.put("txid", httpReq.getRequestURI());
		logParams.put("httpMethod", httpReq.getMethod());
		logParams.put("orgId", "");
		logParams.put("jti", "");
		logParams.put("clientId", "");
		logParams.put("cip",
				StringUtils.hasText(httpReq.getHeader("x-forwarded-for")) ? httpReq.getHeader("x-forwarded-for")
						: httpReq.getRemoteAddr());
		logParams.put("contentLength", httpReq.getHeader(HttpHeaders.CONTENT_LENGTH));
		return logParams;
	}

	private Map<String, String> getRdbLogParams(HttpServletRequest httpReq) {
		Map<String, String> logParams = new HashMap<>();
		logParams.put("uri", httpReq.getRequestURI());

		//這段 Regex 已被 Tom Review 過了, 故取消 hotspot 標記
		Pattern p = Pattern.compile("/?([^/]+).*"); // NOSONAR

		Matcher m = p.matcher(httpReq.getRequestURI());
		if (m.matches()) {
			logParams.put("moduleName", "/" + m.group(1));
		} else {
			logParams.put("moduleName", httpReq.getRequestURI());
		}
		logParams.put("txid", httpReq.getRequestURI());
		logParams.put("httpMethod", httpReq.getMethod());
		logParams.put("orgId", "");
		logParams.put("jti", "");
		logParams.put("clientId", "");
		logParams.put("userName", "");
		logParams.put("cip",
				StringUtils.hasText(httpReq.getHeader("x-forwarded-for")) ? httpReq.getHeader("x-forwarded-for")
						: httpReq.getRemoteAddr());
		return logParams;
	}

	

	/** 
	 * 提供客製包使用的 cus Token,
	 * 仿造 AC Access Token
	 * */
	public String getCusAccessToken(String yourUserName) throws Exception {
		// 取得 Public Key
		PublicKey publicKey = getTsmpCoreTokenHelper().getKeyPair().getPublic();
		// 取得 Private Key
		PrivateKey privateKey = getTsmpCoreTokenHelper().getKeyPair().getPrivate();

		Map<String, Object> payloadMap = new HashMap<>();
		payloadMap.put("node", "executor1");
		payloadMap.put("aud", new String[]{"YWRtaW5BUEk"});
		payloadMap.put("user_name", yourUserName);
		payloadMap.put("org_id", "100000");
		payloadMap.put("scope", new String[]{"1000"});
		payloadMap.put("stime", System.currentTimeMillis() / 1000);
		payloadMap.put("iat", System.currentTimeMillis() / 1000);
		payloadMap.put("exp", (System.currentTimeMillis() + 30000) / 1000); // 效期 30 秒,要把 ms 變成 sec
		payloadMap.put("authorities", new String[]{"1000"});
		payloadMap.put("jti", UUID.randomUUID().toString());
		payloadMap.put("client_id", "cusA");
		String payload = OAuthTokenService.objectMapper.writeValueAsString(payloadMap);
		boolean isJwe = false;
		String accessToken = getTokenJwt(isJwe, payload, publicKey, privateKey);
		return accessToken;
	}
	
	/**
	 * 驗證 客製包使用的 cus Token
	 */
	public boolean verifyCusAccessToken(String accessToken) throws Exception {
		
		JwtPayloadData jwtPayloadData = getTokenHelper().getJwtPayloadData(accessToken);
		JsonNode payloadJsonNode = jwtPayloadData.payloadJsonNode;
		Long exp = JsonNodeUtil.getNodeAsLong(payloadJsonNode, "exp");
		// access token 過期
		long nowTime = System.currentTimeMillis() / 1000;// 去掉亳秒
		if (exp < nowTime) {
			TPILogger.tl.debug(TokenHelper.ACCESS_TOKEN_EXPIRED + exp);
			String errMsg = TokenHelper.ACCESS_TOKEN_EXPIRED + accessToken;
			TPILogger.tl.debug(errMsg);	
			return false;
		}
		
		// 取得 Public Key
		PublicKey publicKey = getTsmpCoreTokenHelper().getKeyPair().getPublic();
		JWScodec.jwsVerifyByRS(publicKey, accessToken);
		return true;
	}
	
	protected ObjectMapper getObjectMapper() {
		return OAuthTokenService.objectMapper;
	}

	protected TsmpUserDao getTsmpUserDao() {
		return tsmpUserDao;
	}

	protected AuthoritiesDao getAuthoritiesDao() {
		return authoritiesDao;
	}

	protected OauthClientDetailsDao getOauthClientDetailsDao() {
		return oauthClientDetailsDao;
	}

	protected UsersDao getUsersDao() {
		return usersDao;
	}

	protected TsmpCoreTokenEntityHelper getTsmpCoreTokenHelper() {
		return tsmpCoreTokenHelper;
	}

	protected DgrOauthApprovalsDao getDgrOauthApprovalsDao() {
		return dgrOauthApprovalsDao;
	}

	protected TokenHelper getTokenHelper() {
		return tokenHelper;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected TsmpTokenHistoryDao getTsmpTokenHistoryDao() {
		return tsmpTokenHistoryDao;
	}

	protected TsmpClientDao getTsmpClientDao() {
		return tsmpClientDao;
	}

	protected CommForwardProcService getCommForwardProcService() {
		return commForwardProcService;
	}

	protected DgrAcIdpUserDao getDgrAcIdpUserDao() {
		return dgrAcIdpUserDao;
	}

	protected DgrAcIdpAuthCodeDao getDgrAcIdpAuthCodeDao() {
		return dgrAcIdpAuthCodeDao;
	}

	protected DgrGtwIdpAuthMDao getDgrGtwIdpAuthMDao() {
		return dgrGtwIdpAuthMDao;
	}

	protected DgrGtwIdpAuthDDao getDgrGtwIdpAuthDDao() {
		return dgrGtwIdpAuthDDao;
	}

	protected DgrGtwIdpAuthCodeDao getDgrGtwIdpAuthCodeDao() {
		return dgrGtwIdpAuthCodeDao;
	}

	protected TsmpClientGroupDao getTsmpClientGroupDao() {
		return tsmpClientGroupDao;
	}

	protected TsmpGroupDao getTsmpGroupDao() {
		return tsmpGroupDao;
	}

	protected AdaptiveThreadPoolExecutor getAdaptiveThreadPoolExecutor() {
		return executor;
	}

	protected AsyncSendNotifyLandingRequestToDgrService getAsyncSendNotifyLandingRequestToDgrService() {
		return asyncSendNotifyLandingRequestToDgrService;
	}

	protected DigiRunnerGtwDeployProperties getDigiRunnerGtwDeployPropertiesr() {
		return digiRunnerGtwDeployProperties;
	}
}