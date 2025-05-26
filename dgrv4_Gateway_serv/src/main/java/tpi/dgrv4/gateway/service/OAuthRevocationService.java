package tpi.dgrv4.gateway.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.JsonNode;

import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.TsmpTokenHistory;
import tpi.dgrv4.entity.repository.TsmpTokenHistoryDao;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.component.TokenHelper.JwtPayloadData;
import tpi.dgrv4.gateway.constant.DgrTokenType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.JsonNodeUtil;
import tpi.dgrv4.gateway.vo.OAuthRevocationResp;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp;

@Service
public class OAuthRevocationService {

	public static String token_revoke_success = "token_revoke_success";
	public static String token_already_revoked = "token_already_revoked";

	private OAuthIntrospectionService oauthIntrospectionService;
	private TokenHelper tokenHelper;
	private TsmpTokenHistoryDao tsmpTokenHistoryDao;

	@Autowired
	public OAuthRevocationService(OAuthIntrospectionService oauthIntrospectionService, TokenHelper tokenHelper,
			TsmpTokenHistoryDao tsmpTokenHistoryDao) {
		super();
		this.oauthIntrospectionService = oauthIntrospectionService;
		this.tokenHelper = tokenHelper;
		this.tsmpTokenHistoryDao = tsmpTokenHistoryDao;
	}

	public ResponseEntity<?> revocation(HttpServletRequest httpReq, HttpServletResponse httpRes) {
		String reqUri = httpReq.getRequestURI();
		try {
			
			Map<String, String> parameters = new HashMap<>();
			httpReq.getParameterMap().forEach((k, vs) -> {
				if (vs.length != 0) {
					parameters.put(k, vs[0]);
				}
			});
			
			return revocation(parameters, reqUri);

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			return new ResponseEntity<OAuthTokenErrorResp>(
					getTokenHelper().getOAuthTokenErrorResp("Internal Server Error", null,
							HttpStatus.INTERNAL_SERVER_ERROR.value(), reqUri),
					HttpStatus.INTERNAL_SERVER_ERROR);// 500
		}
	}

	protected ResponseEntity<?> doRevocation(String tokenTypeHint, String jti) {
		String message = "";
		if (DgrTokenType.ACCESS_TOKEN.equalsIgnoreCase(tokenTypeHint)) {
			message = "access token ";

		} else if (DgrTokenType.REFRESH_TOKEN.equalsIgnoreCase(tokenTypeHint)) {
			message = "refresh token ";
		}

		String code = null;
		// 是否 token 已撤銷
		boolean isTokenRevoked = getTokenHelper().checkTokenRevoked(tokenTypeHint, jti);
		if (isTokenRevoked) {
			// access token 或 refresh token, 已撤銷
			code = token_already_revoked;
			message += "already revoked";

		} else {
			if (DgrTokenType.ACCESS_TOKEN.equalsIgnoreCase(tokenTypeHint)) {
				// 撤銷 access token, 狀態設為 "R"
				TsmpTokenHistory tsmpTokenHistory = getTsmpTokenHistoryDao().findFirstByTokenJti(jti);
				if (tsmpTokenHistory != null) {
					tsmpTokenHistory.setRevokedAt(DateTimeUtil.now());
					tsmpTokenHistory.setRevokedStatus("R");
					tsmpTokenHistory = getTsmpTokenHistoryDao().saveAndFlush(tsmpTokenHistory);

					code = token_revoke_success;
					message += "revoke success";
				}

			} else if (DgrTokenType.REFRESH_TOKEN.equalsIgnoreCase(tokenTypeHint)) {
				// 撤銷 refresh token, 狀態設為 "R"

				// 找出此 refresh token 的所有資料
				List<TsmpTokenHistory> tsmpTokenHistoryList = getTsmpTokenHistoryDao().findByRetokenJti(jti);
				if (!CollectionUtils.isEmpty(tsmpTokenHistoryList)) {
					for (TsmpTokenHistory tsmpTokenHistory : tsmpTokenHistoryList) {
						tsmpTokenHistory.setRftRevokedAt(DateTimeUtil.now());
						tsmpTokenHistory.setRftRevokedStatus("R");
						tsmpTokenHistory = getTsmpTokenHistoryDao().saveAndFlush(tsmpTokenHistory);
					}

					code = token_revoke_success;
					message += "revoke success";
				}
			}
			
			// TODO, In-Memory, revocation
			// 若角色為 Memory, 則用非同步,調用 Master/Slave(Landing)的 RevocationLanding API,
			// 將 撤銷 token 的資料寫入 Landing DB (TSMP_TOKEN_HISTORY)
			// sendrevocationLandingRequestToDgr();
		}

		TPILogger.tl.debug("code: " + code + ", message: " + message);
		return getResp(code, message, jti);
	}

	private ResponseEntity<?> revocation(Map<String, String> parameters, String apiUrl) {
		ResponseEntity<?> respEntity = oauthIntrospectionService.checkData(parameters, apiUrl);
		if (respEntity != null) {// 資料驗證有錯誤
			return respEntity;
		}

		String tokenStr = parameters.get("token");
		String tokenTypeHint = parameters.get("token_type_hint");// access_token 或 refresh_token
		String clientId = parameters.get("client_id");

		// 由 token 取得資料
		JwtPayloadData jwtPayloadData = getTokenHelper().getJwtPayloadData(tokenStr);
		respEntity = jwtPayloadData.errRespEntity;
		if (respEntity != null) {// 資料有錯誤
			return respEntity;
		}

		JsonNode payloadJsonNode = jwtPayloadData.payloadJsonNode;
		String tokenClientId = JsonNodeUtil.getNodeAsText(payloadJsonNode, "client_id");

		// token 的 client_id 沒有值,或 client_id 和 token 中的 client_id 值不相同
		respEntity = getTokenHelper().checkTokenClientId(clientId, tokenClientId, tokenStr, "token");
		if (respEntity != null) {// 資料驗證有錯誤
			return respEntity;
		}
		
		String jti = JsonNodeUtil.getNodeAsText(payloadJsonNode, "jti");
		return doRevocation(tokenTypeHint, jti);
	}

	private ResponseEntity<?> getResp(String code, String message, String jti) {
		OAuthRevocationResp resp = new OAuthRevocationResp();
		resp.setCode(code);// token_revoke_success 或 token_already_revoked
		resp.setMessage(message + ", jti: " + jti);

		return new ResponseEntity<OAuthRevocationResp>(resp, HttpStatus.OK);
	}

	protected TokenHelper getTokenHelper() {
		return tokenHelper;
	}

	protected TsmpTokenHistoryDao getTsmpTokenHistoryDao() {
		return tsmpTokenHistoryDao;
	}
}
