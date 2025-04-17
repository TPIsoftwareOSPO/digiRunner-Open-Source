package tpi.dgrv4.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.TsmpTokenHistory;
import tpi.dgrv4.entity.repository.TsmpTokenHistoryDao;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.component.TokenHelper.JwtPayloadData;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.JsonNodeUtil;

/**
 * 驗證收到的 Access token 是否合法，<br>
 * 當驗證成功時，<br>
 * 1.若有 GTW IdP 核發的 ID token, 則返回 User 的個人資料信息和電子郵件(ID token payload 資料) <br>
 * 2.若有 GTW IdP(API / GOOGLE / MS) 調用 IdP API 得到的 response 結果, 則第1點內容, 再加上
 * response 結果 <br>
 * 
 * @author Mini
 */

@Service
public class GtwIdPUserInfoV2Service {
	private TokenHelper tokenHelper;
	private TsmpTokenHistoryDao tsmpTokenHistoryDao;
	private GtwIdPVerifyService gtwIdPVerifyService;

	@Autowired
	public GtwIdPUserInfoV2Service(TokenHelper tokenHelper, TsmpTokenHistoryDao tsmpTokenHistoryDao,
			GtwIdPVerifyService gtwIdPVerifyService) {
		super();
		this.tokenHelper = tokenHelper;
		this.tsmpTokenHistoryDao = tsmpTokenHistoryDao;
		this.gtwIdPVerifyService = gtwIdPVerifyService;
	}

	public ResponseEntity<?> getUserInfoV2(HttpServletRequest httpReq, HttpServletResponse httResp,
			HttpHeaders httpHeaders) {
		String reqUri = httpReq.getRequestURI();
		ResponseEntity<?> respEntity = null;

		try {

			String authorization = httpHeaders.getFirst("Authorization");

			// 驗證 Access token
			respEntity = verifyAccessToken(httpReq, httpHeaders, authorization, reqUri);
			if (respEntity != null) {
				return respEntity;
			}

			// 取得 ID token 內容
			respEntity = getIdTokenData(authorization, reqUri);
			return respEntity;

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			String errMsg = TokenHelper.INTERNAL_SERVER_ERROR;
			TPILogger.tl.error(errMsg);
			respEntity = getTokenHelper().getInternalServerErrorResp(reqUri, errMsg);// 500
			return respEntity;
		}
	}

	/**
	 * 驗證 Access token
	 */
	private ResponseEntity<?> verifyAccessToken(HttpServletRequest httpReq, HttpHeaders httpHeaders,
			String authorization, String reqUri) {
		ResponseEntity<?> respEntity = null;

		// 1.是否有 authorization
		respEntity = getTokenHelper().checkHasAuthorization(authorization, reqUri);
		if (respEntity != null) {
			return respEntity;
		}
		
		// 2.是否有"bearer "字樣,忽略大小寫
		boolean hasBearer = getTokenHelper().checkHasKeyword(authorization, TokenHelper.BEARER);
		if (!hasBearer) {
			// 沒有 Authorization
			String errMsg = TokenHelper.MISSING_REQUIRED_PARAMETER + "Authorization";
			respEntity = getTokenHelper().getUnauthorizedErrorResp(reqUri, errMsg);
			return respEntity;
		}

		// 3.bearer 格式 (JWE/JWS)
		respEntity = getTokenHelper().verifyApiForBearer(authorization, TokenHelper.SSOTOKEN, TokenHelper.SSOTOKEN,
				reqUri, httpReq);
		if (respEntity != null) {
			return respEntity;
		}

		return null;
	}

	/**
	 * 取得 ID token 內容
	 */
	private ResponseEntity<?> getIdTokenData(String authorization, String reqUri) throws Exception {
		ResponseEntity<?> respEntity = null;

		// 1.取得 Access token 的 jti
		// 由 token 取得資料, 驗證 JWS 簽章 或 JWE 解密
		String tokenStr = authorization.substring(TokenHelper.BEARER.length());
		JwtPayloadData jwtPayloadData = getTokenHelper().getJwtPayloadData(tokenStr);
		respEntity = jwtPayloadData.errRespEntity;
		if (respEntity != null) {// 資料有錯誤
			return respEntity;
		}

		JsonNode payloadJsonNode = jwtPayloadData.payloadJsonNode;
		String tokenJti = JsonNodeUtil.getNodeAsText(payloadJsonNode, "jti");
		// 沒有 jti
		if (!StringUtils.hasText(tokenJti)) {
			String errMsg = "Access token has no jti.";
			TPILogger.tl.debug(errMsg);
			return getTokenHelper().getUnauthorizedErrorResp(reqUri, errMsg);// 401
		}

		// 2.用 jti 搜尋 token history
		TsmpTokenHistory tsmpTokenHistory = getTsmpTokenHistoryDao().findFirstByTokenJti(tokenJti);
		respEntity = getTokenHelper().checkAccessTokenRevoked(tsmpTokenHistory, tokenJti);
		if (respEntity != null) {// 資料有錯誤
			return respEntity;
		}

		// 3.取得 dgR 核發的 ID token JWT
		String idTokenJwtstr = tsmpTokenHistory.getIdTokenJwtstr();
		String apiResp = tsmpTokenHistory.getApiResp();
		if (!StringUtils.hasText(idTokenJwtstr)) {
			// Table [TSMP_TOKEN_HISTORY] 查不到 ID token
			String errMsg1 = "Table [TSMP_TOKEN_HISTORY] can't find ID Token. token_jti: " + tokenJti;
			String errMsg2 = String.format(TokenHelper.THE_ID_TOKEN_WAS_NOT_FOUND, tokenJti);
			TPILogger.tl.debug(errMsg1 + ",\n" + errMsg2);
			return getTokenHelper().getForbiddenErrorResp(reqUri, errMsg2);// 403
		}

		// 4.取得 ID token 內容
		respEntity = getGtwIdPVerifyService().verify(idTokenJwtstr, apiResp, reqUri);
		return respEntity;
	}

	protected TokenHelper getTokenHelper() {
		return tokenHelper;
	}

	protected TsmpTokenHistoryDao getTsmpTokenHistoryDao() {
		return tsmpTokenHistoryDao;
	}

	protected GtwIdPVerifyService getGtwIdPVerifyService() {
		return gtwIdPVerifyService;
	}
}
