package tpi.dgrv4.gateway.service;

import java.security.PrivateKey;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
import tpi.dgrv4.gateway.component.OIDCWellKnownHelper;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.component.TokenHelper.JWTClaimsSetData;
import tpi.dgrv4.gateway.component.TokenHelper.JwtPayloadData;
import tpi.dgrv4.gateway.constant.DgrTokenType;
import tpi.dgrv4.gateway.constant.DgrTokenVersion;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.OAuthIntrospectionResp;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp2;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Component
@Service
public class OAuthIntrospectionService {

	private final TsmpCoreTokenEntityHelper tsmpCoreTokenHelper;
	private final TokenHelper tokenHelper;
	private final TsmpSettingService tsmpSettingService;
	private final DgrAcIdpUserDao dgrAcIdpUserDao;
	private final TsmpUserDao tsmpUserDao;
	private final TsmpTokenHistoryDao tsmpTokenHistoryDao;
	private final GtwIdPJwksService gtwIdPJwksService;

	public ResponseEntity<?> introspection(HttpServletRequest httpReq, HttpServletResponse httpRes, String pathVer) {
		String reqUri = httpReq.getRequestURI();
		try {

			Map<String, String> parameters = new HashMap<>();
			httpReq.getParameterMap().forEach((k, vs) -> {
				if (vs.length != 0) {
					parameters.put(k, vs[0]);
				}
			});

			return introspection(parameters, reqUri, pathVer);

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			return new ResponseEntity<OAuthTokenErrorResp>(
					TokenHelper.getOAuthTokenErrorResp("Internal Server Error", null,
							HttpStatus.INTERNAL_SERVER_ERROR.value(), reqUri),
					HttpStatus.INTERNAL_SERVER_ERROR);// 500
		}
	}
	
	protected JWTClaimsSetData getJWTClaimsSetData(String tokenStr, String reqUri) throws ParseException {
		JWTClaimsSetData jwtClaimsSetData = new JWTClaimsSetData();

		// 解析 JWT
		JWT jwt = JWTParser.parse(tokenStr);

		if (jwt instanceof SignedJWT) {
			// 這是 JWS
			SignedJWT signedJWT = (SignedJWT) jwt;

			// 取得 JWS 的 Claims 集合 (Payload 部分)
			jwtClaimsSetData.jwtClaimsSet = signedJWT.getJWTClaimsSet();

		} else if (jwt instanceof EncryptedJWT) {
			// 這是 JWE
			jwtClaimsSetData.isJWE = true;
			EncryptedJWT encryptedJWT = (EncryptedJWT) jwt;
			
			try {
				// 1.取得 digiRunner 的私鑰
				PrivateKey privateKey = getTsmpCoreTokenHelper().getKeyPair().getPrivate();

				// 準備 RSA 私鑰解密器
				RSADecrypter decrypter = new RSADecrypter(privateKey); // java.security.interfaces.RSAPrivateKey
				// 執行解密
				encryptedJWT.decrypt(decrypter);

				// 解密後才能取得 Claims
				JWTClaimsSet tokenClaims = encryptedJWT.getJWTClaimsSet();
				if (tokenClaims == null) {
					// JWE 解密失敗
					String errMsg = "Token JWE decryption failed.";
					TPILogger.tl.debug(errMsg);
					jwtClaimsSetData.errRespEntity = TokenHelper.getBadRequestErrorResp(reqUri,
							TokenHelper.INVALID_TOKEN, errMsg);
					return jwtClaimsSetData;
				}

				jwtClaimsSetData.jwtClaimsSet = tokenClaims;

			} catch (Exception e) {
				TPILogger.tl.warn(StackTraceUtil.logStackTrace(e));
				String errMsg = "Token JWE decryption error.";
				TPILogger.tl.debug(errMsg);
				jwtClaimsSetData.errRespEntity = TokenHelper.getBadRequestErrorResp(reqUri, TokenHelper.INVALID_TOKEN,
						errMsg);
			}
		}
		
		return jwtClaimsSetData;
	}

	public ResponseEntity<?> introspection(Map<String, String> parameters, String reqUri, String pathVer) throws Exception {

		ResponseEntity<?> respEntity = checkData(parameters, reqUri);
		if (respEntity != null) {// 資料驗證有錯誤
			return respEntity;
		}

		String tokenStr = parameters.get("token");
		String tokenTypeHint = parameters.get("token_type_hint");// access_token 或 refresh_token
		String clientId = parameters.get("client_id");
		
		JWTClaimsSetData jwtClaimsSetData = getJWTClaimsSetData(tokenStr, reqUri);
		
		respEntity = jwtClaimsSetData.errRespEntity;
		if (respEntity != null) {// 資料有錯誤
			return respEntity;
		}
		
		JWTClaimsSet tokenClaims = jwtClaimsSetData.jwtClaimsSet;
	    
	    // 取得 JWS 的 Issuer
		String issuer = tokenClaims.getIssuer();// iss
		
		Long exp = tokenClaims.getExpirationTime().getTime() / 1000;// 去掉亳秒
		JwtPayloadData oauthIntrospectionJwtPayloadData = null;
		
		// exp 是否沒有值 或 過期
		boolean isTokenExpired = getTokenHelper().checkTokenExpired(tokenTypeHint, tokenStr, exp);
		if (isTokenExpired) {
			return getResp(tokenClaims, false, tokenTypeHint);
		}
		
		boolean isJWE = jwtClaimsSetData.isJWE;
		if (!isJWE) {
			// 驗證簽章
			if (DgrTokenVersion.PATH_V2.equals(pathVer)) {
				// 取得 JWKS 的公鑰 JSON
				String jwksJsonStr = getGtwIdPJwksService().getJwksJsonStr();
				// 驗證 JWS 簽章
				oauthIntrospectionJwtPayloadData = getTokenHelper().verifyTokenByJwks(tokenStr, issuer, jwksJsonStr);

			} else {
				oauthIntrospectionJwtPayloadData = getTokenHelper().getJwtPayloadData(tokenStr);

			}
			
			respEntity = oauthIntrospectionJwtPayloadData.errRespEntity;
			if (respEntity != null) {// 資料有錯誤
				return respEntity;
			}
		}

		String jti = tokenClaims.getJWTID();// jti
		String tokenClientId = tokenClaims.getClaimAsString("client_id");
		
		// token 的 client_id 沒有值,或 client_id 和 token 中的 client_id 值不相同
		respEntity = getTokenHelper().checkTokenClientId(clientId, tokenClientId, tokenStr, "token");
		if (respEntity != null) {// 資料驗證有錯誤
			return respEntity;
		}

		// 是否 token 已撤銷
		boolean isTokenRevoked = getTokenHelper().checkTokenRevoked(tokenTypeHint, jti);

		boolean active = false;
		if (!isTokenRevoked) {// token 未撤銷
			active = true;
		}

		return getResp(tokenClaims, active, tokenTypeHint);
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
					TokenHelper.getOAuthTokenErrorResp2("unsupported_token_type_hint", errMsg),
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
						TokenHelper.getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
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

	protected ResponseEntity<?> getResp(JWTClaimsSet tokenClaims, boolean active, String tokenTypeHint)
			throws ParseException {
		Long exp = tokenClaims.getExpirationTime().getTime() / 1000;// exp, 拿掉亳秒
		String jti = tokenClaims.getJWTID();// jti
		String ati = tokenClaims.getClaimAsString("ati");// 若為 refresh token, 則有 ati
		Long iat = tokenClaims.getIssueTime().getTime() / 1000;// iat, 拿掉亳秒
		
		List<String> scopeList = tokenClaims.getStringListClaim("scope");
		String scopeStr = TokenHelper.getListToStr(scopeList);// 取得多個 scope, 以空格分隔的字串

		String tokenUserName = tokenClaims.getClaimAsString("user_name");
		String tokenClientId = tokenClaims.getClaimAsString("client_id");


		List<String> audList = tokenClaims.getAudience();// aud, 可能是 Array 或 String
		String audStr = TokenHelper.getListToStr(audList);// 取得多個 aud, 以空格分隔的字串

		String idPType = tokenClaims.getClaimAsString("idp_type");
		

		boolean isAcIdPFlow = TokenHelper.isAcIdPFlow(tokenUserName, idPType);
		boolean isGtwIdPFlow = TokenHelper.isGtwIdPFlow(tokenUserName, idPType);

		String userNameForQuery = TsmpAuthorization.getUserNameForQuery(tokenUserName);

		// v1的 token 沒有 sub, v2 的 token 才加了 sub
		String sub = userNameForQuery;
		if (sub == null || sub.isEmpty()) {
			sub = tokenClaims.getClaimAsString("sub");
		}
		
		String issuer = tokenClaims.getIssuer();// iss		
		if(!StringUtils.hasText(issuer)) {
			issuer = getIssuer(idPType, isGtwIdPFlow);
		}

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
		resp.setSub(sub);// 可供機器識別是哪位 user 授權了這個 token，例如說：db 記錄的 uuid
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

		String schemeAndDomainAndPort = OIDCWellKnownHelper.getSchemeAndDomainAndPort(dgrPublicDomain,
				dgrPublicPort);
		
		String issuer = null;
		if (isGtwIdPFlow) {// for GTW IdP
			issuer = OIDCWellKnownHelper.getIssuer(schemeAndDomainAndPort, idPType, DgrTokenVersion.PATH_V1);

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
