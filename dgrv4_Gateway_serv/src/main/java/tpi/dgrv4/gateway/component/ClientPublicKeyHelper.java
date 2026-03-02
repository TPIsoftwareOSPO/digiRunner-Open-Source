package tpi.dgrv4.gateway.component;

import java.security.PublicKey;
import java.util.Date;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tpi.dgrv4.codec.utils.PEMUtil;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.jpql.TsmpClientCert;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpClientCertCacheProxy;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp2;

@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Component
public class ClientPublicKeyHelper {

	private final TsmpClientCertCacheProxy tsmpClientCertCacheProxy;

	// No Auth 找不到有效的憑證
	public static final String NO_AUTH_NO_VALID_CREDENTIALS_FOUND = "'No Auth' no valid credentials found.";
	// 沒有 client ID 找不到有效的憑證
	public static final String MISSING_CLIENT_ID_NO_VALID_CERTIFICATE_FOUND = "Missing client ID, no valid certificate found.";
	// 找不到有效的憑證
	public static final String NO_VALID_CERTIFICATE_FOUND = "No valid certificate found, client_id: ";
	// 產生公鑰失敗
	public static final String FAILED_TO_GENERATE_PUBLIC_KEY = "Failed to generate public key, client_id: ";

	public class ClientPublicKeyData {
		private ResponseEntity<OAuthTokenErrorResp2> errRespEntity;
		private PublicKey clientPublicKey;

		public ResponseEntity<OAuthTokenErrorResp2> getErrRespEntity() {
			return errRespEntity;
		}

		public void setErrRespEntity(ResponseEntity<OAuthTokenErrorResp2> errRespEntity) {
			this.errRespEntity = errRespEntity;
		}

		public PublicKey getClientPublicKey() {
			return clientPublicKey;
		}

		public void setClientPublicKey(PublicKey clientPublicKey) {
			this.clientPublicKey = clientPublicKey;
		}
	}

	/**
	 * 取得 Client(客戶) 憑證中的公鑰
	 */
	public ClientPublicKeyData getClientPublicKeyData(String clientId, String noAuth) {
		ClientPublicKeyData clientPublicKeyData = new ClientPublicKeyData();

		// 1.是否為 No Auth
		if ("1".equals(noAuth)) {// No Auth,沒有驗證,就沒有 client
			// No Auth 找不到有效的憑證
			String errMsg = NO_AUTH_NO_VALID_CREDENTIALS_FOUND;
			TPILogger.tl.debug(errMsg);
			ResponseEntity<OAuthTokenErrorResp2> errResEntity = new ResponseEntity<>(
					TokenHelper.getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg), HttpStatus.BAD_REQUEST);// 400
			clientPublicKeyData.errRespEntity = errResEntity;// 400
			return clientPublicKeyData;
		}

		// 2.沒有 client ID
		if (!StringUtils.hasLength(clientId)) {
			// 沒有 client ID 找不到有效的憑證
			String errMsg = MISSING_CLIENT_ID_NO_VALID_CERTIFICATE_FOUND;
			TPILogger.tl.debug(errMsg);
			ResponseEntity<OAuthTokenErrorResp2> errResEntity = new ResponseEntity<>(
					TokenHelper.getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg), HttpStatus.BAD_REQUEST);// 400
			clientPublicKeyData.errRespEntity = errResEntity;// 400
			return clientPublicKeyData;
		}

		// 3.取得 client 的有效憑證
		TsmpClientCert tsmpClientCert = getClientCert(clientId);
		if (tsmpClientCert == null) {
			// Table [TSMP_CLIENT_CERT] 查不到 client
			TPILogger.tl.debug("Table [TSMP_CLIENT_CERT] can't find valid certificate, client_id:" + clientId);
			// 找不到有效的憑證
			String errMsg = NO_VALID_CERTIFICATE_FOUND + clientId;
			TPILogger.tl.debug(errMsg);
			ResponseEntity<OAuthTokenErrorResp2> errResEntity = new ResponseEntity<>(
					TokenHelper.getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg), HttpStatus.BAD_REQUEST);// 400
			clientPublicKeyData.errRespEntity = errResEntity;// 400
			return clientPublicKeyData;
		}

		// 4.產生 client 的公鑰
		try {
			String pubKeyStr = tsmpClientCert.getPubKey();
			PublicKey publicKey = PEMUtil.readPublicKey(pubKeyStr);
			clientPublicKeyData.clientPublicKey = publicKey;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			// 產生公鑰失敗
			String errMsg = FAILED_TO_GENERATE_PUBLIC_KEY + clientId;
			TPILogger.tl.debug(errMsg);
			ResponseEntity<OAuthTokenErrorResp2> errResEntity = new ResponseEntity<>(
					TokenHelper.getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg), HttpStatus.BAD_REQUEST);// 400
			clientPublicKeyData.errRespEntity = errResEntity;// 400
			return clientPublicKeyData;
		}

		return clientPublicKeyData;
	}

	/**
	 * 取得 client 的有效憑證, <br>
	 * 即過期時間 > 今天的日期,並依建立時間由小到大排序,取第一個 <br>
	 */
	public TsmpClientCert getClientCert(String clientId) {
		// 取得今天的日期,去掉時分秒,例如:2023/7/1
		Date dt = new Date();
		String dtStr = DateTimeUtil.dateTimeToString(dt, DateTimeFormatEnum.西元年月日_2)
				.orElseThrow(TsmpDpAaRtnCode._1295::throwing);
		Date nowDate = DateTimeUtil.stringToDateTime(dtStr, DateTimeFormatEnum.西元年月日_2)
				.orElseThrow(TsmpDpAaRtnCode._1295::throwing);
		long nowLong = nowDate.getTime();

		// 找有效的憑證(過期時間 > 今天的日期),並依建立時間由小到大排序
		List<TsmpClientCert> certList = getTsmpClientCertCacheProxy()
				.findByClientIdAndExpiredAtAfterOrderByCreateDateTime(clientId, nowLong);

		if (CollectionUtils.isEmpty(certList)) {
			return null;
		} else {
			return certList.get(0);// 取第一個
		}
	}
}
