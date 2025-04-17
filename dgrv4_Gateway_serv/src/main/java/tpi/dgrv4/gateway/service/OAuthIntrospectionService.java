package tpi.dgrv4.gateway.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.codec.utils.IdTokenUtil;
import tpi.dgrv4.codec.utils.IdTokenUtil.IdTokenData;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.component.cipher.TsmpCoreTokenEntityHelper;
import tpi.dgrv4.entity.entity.DgrAcIdpUser;
import tpi.dgrv4.entity.entity.TsmpTokenHistory;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.repository.DgrAcIdpUserDao;
import tpi.dgrv4.entity.repository.TsmpTokenHistoryDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.component.TokenHelper.JwtPayloadData;
import tpi.dgrv4.gateway.constant.DgrTokenType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.JsonNodeUtil;
import tpi.dgrv4.gateway.vo.OAuthIntrospectionResp;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp2;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

import java.util.HashMap;
import java.util.Map;

@Service
public class OAuthIntrospectionService {

	private TsmpCoreTokenEntityHelper tsmpCoreTokenHelper;
	private TokenHelper tokenHelper;
	private TsmpSettingService tsmpSettingService;
	private DgrAcIdpUserDao dgrAcIdpUserDao;
	private TsmpUserDao tsmpUserDao;
	private TsmpTokenHistoryDao tsmpTokenHistoryDao;

	@Autowired
	public OAuthIntrospectionService(TsmpCoreTokenEntityHelper tsmpCoreTokenHelper, TokenHelper tokenHelper,
			TsmpSettingService tsmpSettingService, DgrAcIdpUserDao dgrAcIdpUserDao, TsmpUserDao tsmpUserDao,
			TsmpTokenHistoryDao tsmpTokenHistoryDao) {
		super();
		this.tsmpCoreTokenHelper = tsmpCoreTokenHelper;
		this.tokenHelper = tokenHelper;
		this.tsmpSettingService = tsmpSettingService;
		this.dgrAcIdpUserDao = dgrAcIdpUserDao;
		this.tsmpUserDao = tsmpUserDao;
		this.tsmpTokenHistoryDao = tsmpTokenHistoryDao;
	}

	public ResponseEntity<?> introspection(HttpServletRequest httpReq, HttpServletResponse httpRes) {
		String reqUri = httpReq.getRequestURI();
		try {

			Map<String, String> parameters = new HashMap<>();
			httpReq.getParameterMap().forEach((k, vs) -> {
				if (vs.length != 0) {
					parameters.put(k, vs[0]);
				}
			});

			return introspection(parameters, reqUri);

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			return new ResponseEntity<OAuthTokenErrorResp>(
					getTokenHelper().getOAuthTokenErrorResp("Internal Server Error", null,
							HttpStatus.INTERNAL_SERVER_ERROR.value(), reqUri),
					HttpStatus.INTERNAL_SERVER_ERROR);// 500
		}
	}

	public ResponseEntity<?> introspection(Map<String, String> parameters, String reqUri) {

		ResponseEntity<?> respEntity = checkData(parameters, reqUri);
		if (respEntity != null) {// 資料驗證有錯誤
			return respEntity;
		}

		String tokenStr = parameters.get("token");
		String tokenTypeHint = parameters.get("token_type_hint");// access_token 或 refresh_token
		String clientId = parameters.get("client_id");

		// 由 token 取得資料
		JwtPayloadData oauthIntrospection_jwtPayloadData = getTokenHelper().getJwtPayloadData(tokenStr);
		respEntity = oauthIntrospection_jwtPayloadData.errRespEntity;
		if (respEntity != null) {// 資料有錯誤
			return respEntity;
		}

		JsonNode payloadJsonNode = oauthIntrospection_jwtPayloadData.payloadJsonNode;
		String jti = JsonNodeUtil.getNodeAsText(payloadJsonNode, "jti");
		String tokenClientId = JsonNodeUtil.getNodeAsText(payloadJsonNode, "client_id");
		Long exp = JsonNodeUtil.getNodeAsLong(payloadJsonNode, "exp");

		// token 的 client_id 沒有值,或 client_id 和 token 中的 client_id 值不相同
		respEntity = getTokenHelper().checkTokenClientId(clientId, tokenClientId, tokenStr, "token");
		if (respEntity != null) {// 資料驗證有錯誤
			return respEntity;
		}

		// 是否 token exp 沒有值 或 token 過期
		boolean isTokenExpired = getTokenHelper().checkTokenExpired(tokenTypeHint, tokenStr, exp);

		// 是否 token 已撤銷
		boolean isTokenRevoked = getTokenHelper().checkTokenRevoked(tokenTypeHint, jti);

		boolean active = false;
		if (isTokenExpired == false && isTokenRevoked == false) {// token 未過期且未撤銷
			active = true;
		}

		return getResp(payloadJsonNode, active, tokenTypeHint);
	}

	public ResponseEntity<?> checkData(Map<String, String> parameters, String reqUri) {

		String token = parameters.get("token");
		String tokenTypeHint = parameters.get("token_type_hint");// access_token 或 refresh_token
		String clientId = parameters.get("client_id");
		String clientSecret = parameters.get("client_secret");
		String clientBlock = "";

		// 沒有符合的 token_type_hint 文字
		if (!DgrTokenType.ACCESS_TOKEN.equalsIgnoreCase(tokenTypeHint)
				&& !DgrTokenType.REFRESH_TOKEN.equalsIgnoreCase(tokenTypeHint)) {
			String errMsg = "Unsupported token_type_hint: " + tokenTypeHint;
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp2>(
					getTokenHelper().getOAuthTokenErrorResp2("unsupported_token_type_hint", errMsg),
					HttpStatus.BAD_REQUEST);// 400
		}

		// 沒有 clientId 或 client 狀態不正確
		ResponseEntity<?> respEntity = getTokenHelper().checkClientStatus(clientId, reqUri);
		if (respEntity != null) {// 資料驗證有錯誤
			return respEntity;
		}

		if (!StringUtils.hasLength(clientSecret)) {// 沒有密碼
			// 檢查 client 是否有授權 Public
			respEntity = getTokenHelper().checkClientSupportGrantType(clientId, "Public", reqUri);
			if (respEntity != null) {// 若沒有授權 Public client, 就必須有 client_secret
				String errMsg = "Missing client_secret. client_id: " + clientId;
				TPILogger.tl.debug(errMsg);
				return new ResponseEntity<OAuthTokenErrorResp2>(
						getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
						HttpStatus.BAD_REQUEST);// 400
			}

		} else {
			clientBlock = Base64Util.base64Encode(clientSecret.getBytes());

			// 查無 client 或 client 帳密不對
			respEntity = getTokenHelper().checkClientMima(clientId, clientBlock, reqUri);
			if (respEntity != null) {// client資料驗證有錯誤
				return respEntity;
			}
		}

		// 沒有 token 資料
		respEntity = getTokenHelper().checkHasToken(token, "token");
		if (respEntity != null) {// 資料驗證有錯誤
			return respEntity;
		}

		return null;
	}

	protected ResponseEntity<?> getResp(JsonNode payloadJsonNode, boolean active, String tokenTypeHint) {

		Long exp = JsonNodeUtil.getNodeAsLong(payloadJsonNode, "exp");
		String jti = JsonNodeUtil.getNodeAsText(payloadJsonNode, "jti");
		String ati = JsonNodeUtil.getNodeAsText(payloadJsonNode, "ati");// 若為 refresh token, 則有 ati

		JsonNode scopeArray = JsonNodeUtil.getNodeAsArrayNode(payloadJsonNode, "scope");
		String scopeStr = JsonNodeUtil.convertJsonArrayToString(scopeArray, " ");

		String tokenUserName = JsonNodeUtil.getNodeAsText(payloadJsonNode, "user_name");
		String tokenClientId = JsonNodeUtil.getNodeAsText(payloadJsonNode, "client_id");
		Long iat = JsonNodeUtil.getNodeAsLong(payloadJsonNode, "iat");

		JsonNode audArray = JsonNodeUtil.getNodeAsArrayNode(payloadJsonNode, "aud");
		String audStr = JsonNodeUtil.convertJsonArrayToString(audArray, " ");

		String idPType = JsonNodeUtil.getNodeAsText(payloadJsonNode, "idp_type");

		boolean isAcIdPFlow = TokenHelper.isAcIdPFlow(tokenUserName, idPType);
		boolean isGtwIdPFlow = TokenHelper.isGtwIdPFlow(tokenUserName, idPType);

		String userNameForQuery = TsmpAuthorization.getUserNameForQuery(tokenUserName);

		String issuer = getIssuer(idPType, isGtwIdPFlow);
		String userAlias = getUserAlias(tokenTypeHint, idPType, jti, ati, userNameForQuery, isAcIdPFlow, isGtwIdPFlow);

		OAuthIntrospectionResp resp = new OAuthIntrospectionResp();
		resp.setActive(active);
		resp.setScope(scopeStr);
		resp.setClientId(tokenClientId);
		resp.setUsername(userAlias); // 可供人識別是哪位 user 授權了這個 token，例如說：網銀上的別名
		resp.setTokenType(tokenTypeHint.toLowerCase());
		resp.setExp(exp);
		resp.setIat(iat);
		resp.setNbf(iat);// token 的生效時間,目前token中沒有,放iat
		resp.setSub(userNameForQuery);// 可供機器識別是哪位 user 授權了這個 token，例如說：db 記錄的 uuid
		resp.setAud(audStr);
		resp.setIss(issuer);// 簽發人
		resp.setJti(jti);

		return new ResponseEntity<OAuthIntrospectionResp>(resp, HttpStatus.OK);
	}

	/**
	 * 取得 issuer 的值, <br>
	 * 1. GTW IdP 流程: <br>
	 * https://127.0.0.1:8080/dgrv4/ssotoken/{idPType} <br>
	 * https://127.0.0.1:8080/dgrv4/ssotoken/GOOGLE <br>
	 * 2. 其他流程: <br>
	 * https://127.0.0.1:8080/dgrv4 <br>
	 */
	private String getIssuer(String idPType, boolean isGtwIdPFlow) {
		// 對外公開的域名或IP
		String dgrPublicDomain = getTsmpSettingService().getVal_DGR_PUBLIC_DOMAIN();
		// 對外公開的Port
		String dgrPublicPort = getTsmpSettingService().getVal_DGR_PUBLIC_PORT();

		String schemeAndDomainAndPort = GtwIdPWellKnownService.getSchemeAndDomainAndPort(dgrPublicDomain,
				dgrPublicPort);
		
		String issuer = null;
		if (isGtwIdPFlow) {// for GTW IdP
			issuer = GtwIdPWellKnownService.getIssuer(schemeAndDomainAndPort, idPType);

		} else {// 其他
			issuer = String.format("%s/dgrv4", schemeAndDomainAndPort);
		}

		return issuer;
	}

	private String getUserAlias(String tokenTypeHint, String idPType, String jti, String ati, String userNameForQuery,
			boolean isAcIdPFlow, boolean isGtwIdPFlow) {
		String userAlias = null;
		if (isGtwIdPFlow) {// for GTW IdP 流程
			userAlias = getUserAliasForGtwIdP(tokenTypeHint, idPType, jti, ati, userNameForQuery);

		} else if (isAcIdPFlow) {// for AC IdP 流程
			DgrAcIdpUser dgrAcIdpUser = getDgrAcIdpUserDao().findFirstByUserNameAndIdpType(userNameForQuery, idPType);
			if (dgrAcIdpUser == null) {
				// Table 查不到 user
				TPILogger.tl.debug("Table [DGR_AC_IDP_USER] can not find user, user_name: " + userNameForQuery
						+ ", idp_type: " + idPType);
				return null;
			}
			userAlias = dgrAcIdpUser.getUserAlias();

		} else {// for 其他流程
			if (!StringUtils.hasLength(userNameForQuery)) {// grant type 為 client_credentials, 沒有 username
				return null;
			}

			TsmpUser tsmpUser = getTsmpUserDao().findFirstByUserName(userNameForQuery);
			if (tsmpUser == null) {
				// Table 查不到 user
				TPILogger.tl.debug("Table [TSMP_USER] can not find user, user_name: " + userNameForQuery);
				return null;
			}
			userAlias = tsmpUser.getUserAlias();
		}

		return userAlias;
	}

	/**
	 * for GTW IdP 流程, 取得 user alias
	 */
	private String getUserAliasForGtwIdP(String tokenTypeHint, String idPType, String jti, String ati,
			String userNameForQuery) {
		String userAlias = null;
		TsmpTokenHistory tsmpTokenHistory = null;
		// 1.查詢 TSMP_TOKEN_HISTORY
		if (DgrTokenType.ACCESS_TOKEN.equalsIgnoreCase(tokenTypeHint)) {// for access_token
			tsmpTokenHistory = getTsmpTokenHistoryDao().findFirstByTokenJtiAndIdpType(jti, idPType);
			if (tsmpTokenHistory == null) {
				// Table 查不到資料
				TPILogger.tl.debug(
						"Table [TSMP_TOKEN_HISTORY] can not find user, token_jti: " + jti + ", idp_type: " + idPType);
				return null;
			}
		} else {// for refresh_token
			tsmpTokenHistory = getTsmpTokenHistoryDao().findFirstByTokenJtiAndRetokenJtiAndIdpType(ati, jti, idPType);
			if (tsmpTokenHistory == null) {
				// Table 查不到資料
				TPILogger.tl.debug("Table [TSMP_TOKEN_HISTORY] can not find user, token_jti: " + ati + ", retoken_jti: "
						+ jti + ", idp_type: " + idPType);
				return null;
			}
		}

		// 2.取得 id token 中的 user alias
		String idTokenJwtstr = tsmpTokenHistory.getIdTokenJwtstr();
		try {
			IdTokenData idTokenData = IdTokenUtil.getIdTokenData(idTokenJwtstr);
			if (idTokenData == null) {
				return null;
			}

			userAlias = idTokenData.userAlias;
		} catch (JsonProcessingException e) {
			TPILogger.tl.debug(StackTraceUtil.logStackTrace(e));
			return null;
		}
		return userAlias;
	}

	protected TsmpCoreTokenEntityHelper getTsmpCoreTokenHelper() {
		return tsmpCoreTokenHelper;
	}

	protected TokenHelper getTokenHelper() {
		return tokenHelper;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected DgrAcIdpUserDao getDgrAcIdpUserDao() {
		return dgrAcIdpUserDao;
	}

	protected TsmpUserDao getTsmpUserDao() {
		return tsmpUserDao;
	}

	protected TsmpTokenHistoryDao getTsmpTokenHistoryDao() {
		return tsmpTokenHistoryDao;
	}
}
