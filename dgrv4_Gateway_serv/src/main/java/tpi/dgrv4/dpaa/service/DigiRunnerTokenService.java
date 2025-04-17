package tpi.dgrv4.dpaa.service;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DigiRunnerTokenResp;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.OAuthTokenService;
import tpi.dgrv4.gateway.vo.OAuthTokenResp;

/**
 * <s>在 digiRunner 上運行的 api module，預設都會開啟 /oauth/token 這個入口，<br>
 * 因此可以呼叫自己 ex: /tsmpdpaa/oauth/token 就能取得 Token</s><br/>
 * <strong>2022/09/6</strong> - dgRv4 可由 gateway 自行核發 token
 * @author Kim
 */
@Service
public class DigiRunnerTokenService {

	private TPILogger logger = TPILogger.tl;

	private OAuthTokenService oAuthTokenService;
	
	@Autowired
	public DigiRunnerTokenService(OAuthTokenService oAuthTokenService) {
		super();
		this.oAuthTokenService = oAuthTokenService;
	}

	/*
	private final static ObjectMapper om;

	private final String SCHEME;

	private final String HOST;

	private final int PORT;

	private final String PATH;

	private final String URL;

	static {
		om = new ObjectMapper();
	}

	@Autowired
	public DigiRunnerTokenService(
		@Value("${service.digirunner.token.ssl.enabled:#{null}}") Boolean sslEnabled,
		@Value("${service.digirunner.token.ip:localhost}") String host,
		@Value("${service.digirunner.token.port:8080}") int port
	) {
		this.SCHEME = (sslEnabled != null && sslEnabled.booleanValue() ? "https" : "http") + "://";
		this.HOST = host;
		this.PORT = port;
		this.PATH = "/" + TsmpDpModule.DP.getName() + "/oauth/token";	// ex: "/tsmpdpaa/oauth/token"
		this.URL = this.SCHEME.concat(this.HOST).concat(":" + this.PORT).concat(this.PATH);
		this.logger.debug(String.format("%s is initialized: %s", getClass().getName(), this.URL));
	}
	*/

	/**
	 * 參數皆傳入明碼
	 * @param clientId	用戶端帳號
	 * @param clientPwd	用戶端密碼
	 * @return
	 */
	public DigiRunnerTokenResp getToken_clientCredentials(String clientId, String clientPwd) {
		if (!(StringUtils.hasLength(clientId) && StringUtils.hasLength(clientPwd))) {
			this.logger.debug("ClientId / ClientPwd is not allowed null.");
			return null;
		}
		return execute("client_credentials", clientId, clientPwd, null, null);
	}

	/**
	 * 參數皆傳入明碼
	 * @param clientId	用戶端帳號
	 * @param clientPwd	用戶端密碼
	 * @param userName	使用者名稱
	 * @param password	使用者密碼
	 * @return
	 */
	public DigiRunnerTokenResp getToken_password(String clientId, String clientPwd, String userName, String password) {
		if (!(StringUtils.hasLength(userName) && StringUtils.hasLength(password))) {
			this.logger.debug("Username / Password is not allowed null.");
			
		}
		return execute("password", clientId, clientPwd, userName, password);
	}

	@SuppressWarnings("unchecked")
	private DigiRunnerTokenResp execute(String grantType, String clientId, String clientPwd, String username, String password) {
		DigiRunnerTokenResp tokenResp = null;
 
		try {
			clientId = base64Enc(clientId);
			clientPwd = base64Enc(clientPwd);
			String basicAuth = String.format("Basic %s", base64Enc(clientId + ":" + clientPwd));

			/*
			Map<String, String> httpReqHeader = new HashMap<>();
			httpReqHeader.put("Authorization", basicAuth);
			*/
			
			Map<String, String> formData = new HashMap<>();
			formData.put("grant_type", grantType);
			
			if ("password".equals(grantType)) {
				formData.put("username", username);
				formData.put("password", base64Enc(password));
			}
			
			/*
			String url = getURL();
			String httpMethod = HttpMethod.POST.name();
			this.logger.debug(httpMethod + " " + url);
			HttpRespData response = HttpUtil.httpReqByFormData(url, httpMethod, formData, httpReqHeader, true);
 
			if (response != null) {
				tokenResp = parseRespJson(response.respInputStreamObj);
			}
			*/
			
			String apiUrl = "/oauth/token";
			ResponseEntity<OAuthTokenResp> responseEntity = (ResponseEntity<OAuthTokenResp>) getOAuthTokenService() //
				.getToken(formData, basicAuth, apiUrl);
			if (responseEntity.getStatusCode().is2xxSuccessful()) {
				OAuthTokenResp oauthTokenResp = responseEntity.getBody();

				tokenResp = Optional.ofNullable(oauthTokenResp).map(otrp -> {
					var resp = new DigiRunnerTokenResp();
					resp.setAccessToken(otrp.getAccessToken());
					resp.setTokenType(otrp.getTokenType());
					resp.setRefreshToken(otrp.getRefreshToken());
					resp.setExpiresIn(otrp.getExpiresIn());
					resp.setScope(otrp.getScope());
					resp.setOrgId(otrp.getOrgId());
					resp.setNode(otrp.getNode());
					resp.setStime(otrp.getStime());
					resp.setJti(otrp.getJti());
					return resp;
				}).orElse(null);

			}
		} catch (Exception e) {
			this.logger.debug("Get token error: " + StackTraceUtil.logStackTrace(e));
		}
		
		return tokenResp;
	}

	/*
	private DigiRunnerTokenResp parseRespJson(InputStream is) {
		DigiRunnerTokenResp resp = null;
		try {
			resp = om.readValue(is, DigiRunnerTokenResp.class);
			this.logger.debug("RESP: " + resp.toString());
		} catch (Exception e) {
			this.logger.debug("Parse response entity error: " + StackTraceUtil.logStackTrace(e));
		}
		return resp;
	}
	*/
 
	private String base64Enc(String content) {
		return Base64.getEncoder().encodeToString(content.getBytes());
	}

	/*
	protected String getURL() {
		return this.URL;
	}
	*/

	protected OAuthTokenService getOAuthTokenService() {
		return this.oAuthTokenService;
	}

}