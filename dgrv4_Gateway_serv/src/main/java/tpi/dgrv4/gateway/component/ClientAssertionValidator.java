package tpi.dgrv4.gateway.component;

import java.security.PublicKey;
import java.util.Date;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tpi.dgrv4.codec.utils.JWScodec;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.component.ClientPublicKeyHelper.ClientPublicKeyData;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.ClientAssertionJwtData;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp2;

@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Component

/**
 * 請求 token 時, 使用 "使用者斷言" 方式, <br>
 * 驗證使用者斷言 <br>
 */
public class ClientAssertionValidator {

	private final ClientPublicKeyHelper clientPublicKeyHelper;

	// client 斷言
	public static final String URN_IETF_PARAMS_OAUTH_CLIENT_ASSERTION_TYPE_JWT_BEARER = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";
	public static final String CLIENT_ASSERTION_TYPE = "client_assertion_type";
	public static final String CLIENT_ASSERTION = "client_assertion";

	/**
	 * 檢查 client 斷言 <br>
	 * 流程： <br>
	 * APIM 收到請求，先「解析」（Base64 Decode）JWT，但不進行驗證 <br>
	 * 讀取 Payload 中的 "iss": "my-client-id-123" <br>
	 * APIM 根據這個 client_id 去資料庫或快取中查詢該客戶端預先註冊的 Public Key <br>
	 * 取得公鑰後，再執行「驗章」（Verify Signature）動作 <br>
	 */
	public ClientAssertionJwtData checkClientAssertion(Map<String, String> parameters) {

		ClientAssertionJwtData clientAssertionJwtData = new ClientAssertionJwtData();
		ResponseEntity<OAuthTokenErrorResp2> responseEntity = null;
		String clientAssertionType = parameters.get(CLIENT_ASSERTION_TYPE);
		String clientAssertionJwt = parameters.get(CLIENT_ASSERTION);

		if (!StringUtils.hasLength(clientAssertionType) && !StringUtils.hasLength(clientAssertionJwt)) {
			// 二者都沒有值, 不是使用 client 斷言
			return null;
		}

		// client_assertion_type 不正確
		if (!URN_IETF_PARAMS_OAUTH_CLIENT_ASSERTION_TYPE_JWT_BEARER.equals(clientAssertionType)) {

			String errMsg = "The 'client_assertion_type' parameter is incorrect.";
			TPILogger.tl.debug(errMsg);

			responseEntity = new ResponseEntity<>(
					TokenHelper.getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
					TokenHelper.setContentTypeHeader(), HttpStatus.BAD_REQUEST);// 400
			clientAssertionJwtData.setErrRespEntity(responseEntity);
			return clientAssertionJwtData;
		}

		// 沒有 client_assertion 值
		if (!StringUtils.hasLength(clientAssertionJwt)) {
			String errMsg = TokenHelper.MISSING_REQUIRED_PARAMETER + CLIENT_ASSERTION;
			TPILogger.tl.debug(errMsg);

			responseEntity = new ResponseEntity<>(
					TokenHelper.getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
					TokenHelper.setContentTypeHeader(), HttpStatus.BAD_REQUEST);// 400
			clientAssertionJwtData.setErrRespEntity(responseEntity);
			return clientAssertionJwtData;
		}

		// 解析 client_assertion JWT 並取得 iss
		return validateClientAssertion(clientAssertionJwtData, clientAssertionJwt);
	}

	/**
	 * 驗證 Client Assertion JWT, 並取得 iss <br>
	 *
	 * @param assertionJwt 傳入的 JWT 字串 <br>
	 * @param publicKey    簽署者的 RSA 公鑰 <br>
	 * @return 是否驗證成功 <br>
	 */
	public ClientAssertionJwtData validateClientAssertion(ClientAssertionJwtData clientAssertionJwtData,
			String clientAssertionJwt) {
		ResponseEntity<OAuthTokenErrorResp2> responseEntity = null;
		try {
			// 1.解析 JWT
			SignedJWT signedJWT = SignedJWT.parse(clientAssertionJwt);

			// 2.取得 JWT 的 Claims 集合 (Payload 部分)
			JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

			// 3.使用內建方法直接取出 Issuer (iss), 即 client ID
			String issuer = claimsSet.getIssuer();
			clientAssertionJwtData.setClientId(issuer);
			TPILogger.tl.debug("The client_assertion issuer: " + issuer);
			
			if (!StringUtils.hasLength(issuer)) {
				// client_assertion 缺少 issuer
				String errMsg = "client_assertion is missing issuer [iss].";
				TPILogger.tl.debug(errMsg);
				responseEntity = new ResponseEntity<>(
						TokenHelper.getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
						HttpStatus.BAD_REQUEST);// 400
				clientAssertionJwtData.setErrRespEntity(responseEntity);
				return clientAssertionJwtData;
			}

			if (claimsSet.getExpirationTime() == null) {
				// client_assertion 缺少過期時間
				String errMsg = "client_assertion is missing expiration time [exp].";
				TPILogger.tl.debug(errMsg);
				responseEntity = new ResponseEntity<>(
						TokenHelper.getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
						HttpStatus.BAD_REQUEST);// 400
				clientAssertionJwtData.setErrRespEntity(responseEntity);
				return clientAssertionJwtData;
			}

			// 4.JWS 驗章, 使用 client(客戶) 的公鑰驗章

			// 取得 client 憑證中的公鑰
			ClientPublicKeyData clientPublicKeyData = getClientPublicKeyHelper().getClientPublicKeyData(issuer, "0");
			responseEntity = clientPublicKeyData.getErrRespEntity();
			if (responseEntity != null) {
				clientAssertionJwtData.setErrRespEntity(responseEntity);
				return clientAssertionJwtData;
			}

			// 取得公鑰
			PublicKey publicKey = clientPublicKeyData.getClientPublicKey();

			// JWS 驗證簽章是否正確
			boolean verify = JWScodec.jwsVerifyByRS(publicKey, clientAssertionJwt);
			if (!verify) {// JWS 驗證不正確
				String errMsg = "The client_assertion JWS verify error.";
				TPILogger.tl.debug(errMsg);
				responseEntity = new ResponseEntity<>(
						TokenHelper.getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
						HttpStatus.BAD_REQUEST);// 400
				clientAssertionJwtData.setErrRespEntity(responseEntity);
				return clientAssertionJwtData;
			}

			// 5. 取得 Claims 並檢查有效性 (例如 exp)
			Date now = new Date();

			// 檢查過期時間 (Expiration Time)
			if (now.after(claimsSet.getExpirationTime())) {
				// client_assertion 已過期
				String errMsg = "The client_assertion JWT has expired.";
				TPILogger.tl.debug(errMsg);
				responseEntity = new ResponseEntity<>(
						TokenHelper.getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
						HttpStatus.BAD_REQUEST);// 400
				clientAssertionJwtData.setErrRespEntity(responseEntity);
				return clientAssertionJwtData;
			}

			return clientAssertionJwtData;

		} catch (Exception e) {
			String errMsg = "An error occurred during client_assertion JWS authentication.";
			TPILogger.tl.debug(errMsg);
			TPILogger.tl.debug(StackTraceUtil.logStackTrace(e));

			responseEntity = new ResponseEntity<>(
					TokenHelper.getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg), HttpStatus.BAD_REQUEST);// 400
			clientAssertionJwtData.setErrRespEntity(responseEntity);
			return clientAssertionJwtData;
		}
	}
}
