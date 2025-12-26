package tpi.dgrv4.gateway.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import tpi.dgrv4.codec.utils.IdTokenUtil;
import tpi.dgrv4.codec.utils.IdTokenUtil.IdTokenData;
import tpi.dgrv4.codec.utils.JWKcodec;
import tpi.dgrv4.codec.utils.JWKcodec.JWKVerifyResult;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.cache.proxy.DgrWebSocketMappingCacheProxy;
import tpi.dgrv4.entity.entity.DgrWebSocketMapping;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.httpu.utils.HttpUtil;

@Component
public class WebSocketClientConn {

	private static TPILogger logger = TPILogger.tl;

	private static DgrWebSocketMappingCacheProxy dgrWebSocketMappingCacheProxy;

	private static TsmpSettingService tsmpSettingService;

	public static Session startWS(String siteName, List<String> auths) throws Exception {
		try {

			WebSocketContainer container = ContainerProvider.getWebSocketContainer();

			// 設定message最大10M
			container.setDefaultMaxBinaryMessageBufferSize(10 * 1024 * 1024);
			container.setDefaultMaxTextMessageBufferSize(10 * 1024 * 1024);

			DgrWebSocketMapping wsVo = getDgrWebSocketMappingCacheProxy().findFirstBySiteName(siteName).orElse(null);
			if (wsVo != null) {
				// 當Auth 為Y時 要驗證 idToken
				if ("Y".equalsIgnoreCase(wsVo.getAuth())) {

					if (CollectionUtils.isEmpty(auths)) {
						throw TsmpDpAaRtnCode._1219.throwing();
					}
					for (String auth : auths) {

						if (!validateIdToken(auth)) {
							throw TsmpDpAaRtnCode._1533.throwing();
						}
					}

				}
				ClientEndpointConfig.Configurator configurator = new ClientEndpointConfig.Configurator() {
					//不論AUTH是Y還是N都要放入
					@Override
					public void beforeRequest(Map<String, List<String>> headers) {
						if (!CollectionUtils.isEmpty(auths)) {
							headers.put("Authorization", new ArrayList<>(auths));
						}
					}

				};

				ClientEndpointConfig clientEndpointConfig = ClientEndpointConfig.Builder.create()
						.configurator(configurator).build();

				// 取消 wss 安全性驗證
				SSLContext sslContext = HttpUtil.disableWssValidation();
				clientEndpointConfig.getUserProperties().put("org.apache.tomcat.websocket.SSL_CONTEXT", sslContext);

				String uri = wsVo.getTargetWs();
				Session session = container.connectToServer(WebSocketClientHandler.class, clientEndpointConfig,
						URI.create(uri));
				return session;
			} else {
				logger.error(siteName + " websocket data not found");
			}

		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			throw e;
		}

		return null;
	}

	public static boolean validateIdToken(String idToken) throws JsonMappingException, JsonProcessingException {

		// 從 ID Token 中提取出有效負載（Payload）部分。
		idToken = extractIdTokenPayload(idToken);
	
		// 對外公開的域名或IP
		String dgrPublicDomain = getTsmpSettingService().getVal_DGR_PUBLIC_DOMAIN();
		// 對外公開的Port
		String dgrPublicPort = getTsmpSettingService().getVal_DGR_PUBLIC_PORT();
		// 獲取 JWKS URI，這是一個包含公鑰集合的 JSON 物件的位置，用於驗證 JWT 的簽名。
		String jwksUri = String.format("https://%s:%s/dgrv4/ssotoken/oauth2/certs", dgrPublicDomain, dgrPublicPort);
		// 若 JWKS URI 為空，則指定一個預設的 URI。
		if (!StringUtils.hasText(jwksUri)) {
			throw TsmpDpAaRtnCode._1474.throwing("{{jwksUri}}");
		}

		// 從 ID Token 中解析出發行者（Issuer）信息。
		IdTokenData idTokenData = getIdTokenData(idToken);
		String issuer = idTokenData.iss;

		// 利用 JWKcodec 工具檢查 JWT 的簽名是否有效。
		JWKVerifyResult jwk = JWKcodec.verifyJWStoken(idToken, jwksUri, issuer);

		// 驗證 JWK 結果是否有效。
		boolean isValidate = isValidate(jwk);

		// 如果驗證不通過，記錄錯誤信息並拋出異常。
		if (!isValidate) {
			logger.error("ID Token : " + idToken); // 記錄 ID Token
			logger.error("ID Token verification failed. ErrorMsg: " + jwk.errorMessg); // 記錄錯誤信息
		}

		// 返回驗證結果。
		return isValidate;
	}

	public static IdTokenData getIdTokenData(String idToken) throws JsonMappingException, JsonProcessingException {

		// 使用 extractIdTokenPayload 方法處理原始的 ID Token 字串，提取有效的 Token 負載。
		idToken = extractIdTokenPayload(idToken);

		// 使用 IdTokenUtil 工具類的 getIdTokenData 方法解析 Token 負載並返回 IdTokenData 對象。
		return IdTokenUtil.getIdTokenData(idToken);
	}

	public static boolean isValidate(JWKVerifyResult j) {
		return j.verify;
	}

	public static String hasIdToken(String idToken) {

		// 使用 StringUtils.hasText() 方法來檢查 idToken 是否為 null 或只包含空白。
		if (!StringUtils.hasText(idToken)) {
			// 如果 ID Token 為空，記錄一條錯誤信息並拋出一個異常。
			logger.error("ID Token is Null");

		}

		// 如果 ID Token 非空，直接返回該 Token。
		return idToken;
	}

	public static String extractIdTokenPayload(String idToken) {
		// 首先確認 idToken 是否非空，如果為空則 hasIdToken 方法會拋出異常。
		idToken = hasIdToken(idToken);

		// 檢查 idToken 是否以 "Bearer" 開頭（不區分大小寫）。
		if (idToken.toLowerCase().startsWith("bearer")) {
			// 如果是，則按空格分割 Token 為兩部分。預期會有兩個元素：Bearer 和 實際的 Token。
			String[] parts = idToken.split(" ", 2);

			// 如果分割後不是兩部分，記錄錯誤並拋出異常。
			if (parts.length != 2) {
				logger.error("Input ID Token should be split into exactly two parts");
				logger.error("parts : " + Arrays.toString(parts));
			}

			// 分割後，將 idToken 設置為第二部分，即實際的 Token 負載。
			idToken = parts[1];
		}

		// 返回處理後的 Token 負載。
		return idToken;
	}

	protected static TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	public static void setTsmpSettingService(TsmpSettingService tsmpSettingService) {
		WebSocketClientConn.tsmpSettingService = tsmpSettingService;
	}

	protected static DgrWebSocketMappingCacheProxy getDgrWebSocketMappingCacheProxy() {
		return dgrWebSocketMappingCacheProxy;
	}

	public static void setDgrWebSocketMappingCacheProxy(DgrWebSocketMappingCacheProxy dgrWebSocketMappingCacheProxy) {
		WebSocketClientConn.dgrWebSocketMappingCacheProxy = dgrWebSocketMappingCacheProxy;
	}

}
