package tpi.dgrv4.gateway.component;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.copy.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.codec.utils.HexStringUtils;
import tpi.dgrv4.codec.utils.JWEcodec;
import tpi.dgrv4.codec.utils.JWScodec;
import tpi.dgrv4.codec.utils.OpenApiKeyUtil;
import tpi.dgrv4.codec.utils.SHA256Util;
import tpi.dgrv4.codec.utils.TimeZoneUtil;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.ServiceUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.component.cipher.TsmpCoreTokenEntityHelper;
import tpi.dgrv4.entity.constant.TsmpSequenceName;
import tpi.dgrv4.entity.daoService.SeqStoreService;
import tpi.dgrv4.entity.entity.DgrAcIdpUser;
import tpi.dgrv4.entity.entity.DgrXApiKey;
import tpi.dgrv4.entity.entity.DgrXApiKeyMap;
import tpi.dgrv4.entity.entity.OauthClientDetails;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpClientGroup;
import tpi.dgrv4.entity.entity.TsmpGroup;
import tpi.dgrv4.entity.entity.TsmpGroupApi;
import tpi.dgrv4.entity.entity.TsmpOpenApiKey;
import tpi.dgrv4.entity.entity.TsmpOpenApiKeyMap;
import tpi.dgrv4.entity.entity.TsmpTokenHistory;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.entity.Users;
import tpi.dgrv4.entity.entity.jpql.TsmpClientHost;
import tpi.dgrv4.entity.repository.DgrAcIdpUserDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpOpenApiKeyDao;
import tpi.dgrv4.entity.repository.TsmpTokenHistoryDao;
import tpi.dgrv4.gateway.component.cache.proxy.DgrXApiKeyCacheProxy;
import tpi.dgrv4.gateway.component.cache.proxy.DgrXApiKeyMapCacheProxy;
import tpi.dgrv4.gateway.component.cache.proxy.OauthClientDetailsCacheProxy;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpApiCacheProxy;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpClientCacheProxy;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpClientGroupCacheProxy;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpClientHostCacheProxy;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpGroupApiCacheProxy;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpGroupCacheProxy;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpOpenApiKeyMapCacheProxy;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpTokenHistoryCacheProxy;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpUserCacheProxy;
import tpi.dgrv4.gateway.component.cache.proxy.UsersCacheProxy;
import tpi.dgrv4.gateway.constant.DgrAcIdpUserStatus;
import tpi.dgrv4.gateway.constant.DgrDeployRole;
import tpi.dgrv4.gateway.constant.DgrTokenGrantType;
import tpi.dgrv4.gateway.constant.DgrTokenType;
import tpi.dgrv4.gateway.filter.GatewayFilter;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.JsonNodeUtil;
import tpi.dgrv4.gateway.vo.BasicAuthCacheVo;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp2;

@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Component
public class TokenHelper {
	
	public static Map<String, BasicAuthCacheVo> basicAuthCacheVoMap = new HashMap<>();
	
	// Double-Checked Locking mode has been implemented, but SonarQube may not fully understand the context and issue a warning, so annotate
	// 已經實作雙重檢查鎖定(Double-Checked Locking)模式,但SonarQube可能無法完全理解上下文發出警告,故加註解
	@SuppressWarnings("java:S3077")
	private static volatile TokenHelper instance;
	
	private final TsmpClientDao tsmpClientDao;
	private final TsmpClientCacheProxy tsmpClientCacheProxy;
	private final TsmpClientHostCacheProxy tsmpClientHostCacheProxy;
	private final OauthClientDetailsCacheProxy oauthClientDetailsCacheProxy;
	private final TsmpCoreTokenEntityHelper tsmpCoreTokenHelper;
	private final TsmpTokenHistoryDao tsmpTokenHistoryDao;
	private final TsmpTokenHistoryCacheProxy tsmpTokenHistoryCacheProxy;
	private final SeqStoreService seqStoreService;
	private final TsmpUserCacheProxy tsmpUserCacheProxy;
	private final UsersCacheProxy usersCacheProxy;
	private final TsmpGroupApiCacheProxy tsmpGroupApiCacheProxy;
	private final TsmpClientGroupCacheProxy tsmpClientGroupCacheProxy;
	private final TsmpGroupCacheProxy tsmpGroupCacheProxy;
	private final TsmpOpenApiKeyMapCacheProxy tsmpOpenApiKeyMapCacheProxy;
	private final TsmpApiCacheProxy tsmpApiCacheProxy;
	private final TsmpOpenApiKeyDao tsmpOpenApiKeyDao;
	private final DgrAcIdpUserDao dgrAcIdpUserDao;
	private final DgrXApiKeyCacheProxy dgrXApiKeyCacheProxy;
	private final DgrXApiKeyMapCacheProxy dgrXApiKeyMapCacheProxy;
	private final ServiceConfig serviceConfig;
	
	@Value("${digi.cookie.samesite.value:Lax}")
	public String samesiteValue;

	@Value("${digiRunner.gtw.deploy.role}")
	private String deployRole;

	public static final String BASIC = "basic ";
	public static final String BEARER = "bearer ";
	public static final String DGRK = "dgrk ";
	public static final String CLIENT_ID = "clientId";
	public static final String USER_NAME = "userName";
	public static final String ORG_ID = "orgId";
	public static final String JTI = "jti";
	public static final String NO_AUTH = "noAuth";
	public static final String SSOTOKEN = "ssotoken";

	public static final String AUTHORIZATION_DOES_NOT_HAVE_THE_WORD = "Authorization does not have the word: ";
	public static final String AUTHORIZATION_HAS_NO_VALUE = "Authorization has no value";
	public static final String AUTHORIZATION_AND_X_API_KEY_HAVE_NO_VALUES = "Authorization and X-Api-Key have no values";
	public static final String UNAUTHORIZED = "Unauthorized";
	public static final String UNAUTHORIZED_2 = "unauthorized";
	public static final String TOKEN_PARSING_ERROR = "Token parsing error";// token 解析錯誤
	public static final String CANNOT_CONVERT_ACCESS_TOKEN_TO_JSON = "Cannot convert access token to JSON";
	public static final String ACCESS_TOKEN_EXPIRED = "Access token expired: ";// token 過期
	public static final String INVALID_USER = "invalid_user";
	public static final String INVALID_TOKEN = "invalid_token";
	public static final String INVALID_GRANT = "invalid_grant";
	public static final String INVALID_REQUEST = "invalid_request";
	public static final String INVALID_SCOPE = "invalid_scope";
	public static final String FORBIDDEN = "Forbidden";
	public static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
	public static final String THE_CLIENT_WAS_NOT_FOUND = "The client(application) was not found. client_id: ";
	public static final String THE_JTI_WAS_NOT_FOUND = "The jti was not found. token_jti: %s, idp_type: %s";
	public static final String THE_JTI_WAS_NOT_FOUND2 = "The jti was not found. token_jti: %s";
	public static final String THE_ID_TOKEN_WAS_NOT_FOUND = "The ID token was not found. token_jti: %s";
	public static final String JWE_EXPIRED = "JWE expired: ";// JWE 過期

	// 設定檔缺少參數 '%s'
	public static final String THE_PROFILE_IS_MISSING_PARAMETERS = "The profile is missing parameters: ";
	// 缺少必需的參數
	public static final String MISSING_REQUIRED_PARAMETER = "Missing required parameter: ";
	
	/**
	 * 這是一個假的 getInstance(),
	 * 實際上不會有機會建立一個空的 TokenHelper(),
	 * 因為在啟動 Spring boot 時, 早就被 @Component 載入,
	 * 寫這段是為了配合 init() 將物件指向一個 volatile instance,
	 * 方便在其它的 class 中直接使用此 sigaletone object
	 */
	// Double-Checked Locking mode
	// 雙重檢查鎖定(Double-Checked Locking)模式
    public static TokenHelper getInstance() {
        if (instance == null) {
            synchronized (TokenHelper.class) {
				if (instance == null) {
					instance = new TokenHelper(null, null, null, null, null, null, null, null, null, null, null, null,
							null, null, null, null, null, null, null, null);
				}
            }
        }
        return instance;
    }
    
    @PostConstruct
    public void init() {
    	instance = this;
    }

	public static class JwtPayloadData {
		public ResponseEntity<?> errRespEntity;
		public JsonNode payloadJsonNode;
		public String payloadStr;
	}

	public static class BasicAuthClientData {
		private ResponseEntity<?> errRespEntity;
		private String[] cliendData = null;
		
		public ResponseEntity<?> getErrRespEntity() {
			return errRespEntity;
		}
		public void setErrRespEntity(ResponseEntity<?> errRespEntity) {
			this.errRespEntity = errRespEntity;
		}
		public String[] getCliendData() {
			return cliendData;
		}
		public void setCliendData(String[] cliendData) {
			this.cliendData = cliendData;
		}
	}

	public static class DgrkAuthData {
		private ResponseEntity<?> errRespEntity;
		private String[] authData = null;
		
		public ResponseEntity<?> getErrRespEntity() {
			return errRespEntity;
		}
		public void setErrRespEntity(ResponseEntity<?> errRespEntity) {
			this.errRespEntity = errRespEntity;
		}
		public String[] getAuthData() {
			return authData;
		}
		public void setAuthData(String[] authData) {
			this.authData = authData;
		}
	}

	/**
	 * 若 DB 中的授權期限 accessTokenValidity / refreshTokenValidity 沒有值, 預設為10分鐘
	 */
	public Long getTokenValidity(Long tokenValidity) {
		Long value = tokenValidity;
		if (value == null || value == 0) {// 若沒有值, 設為10分鐘
			value = 600L;// 10分鐘, 即600秒
		}

		return value;
	}

	/**
	 * 是否有指定的字樣,忽略大小寫
	 */
	public boolean checkHasKeyword(String authorization, String keyword) {
		if (!StringUtils.hasText(authorization)) {// 沒有 authorization
			String errMsg = TokenHelper.AUTHORIZATION_HAS_NO_VALUE;
			TPILogger.tl.debug(errMsg);
			return false;
		}

		return authorization.toLowerCase().contains(keyword.toLowerCase());// 轉小寫,再比較(忽略大小寫)
	}

	/**
	 * 是否有 Authorization
	 */
	public ResponseEntity<?> checkHasAuthorization(String authorization, String apiUrl) {
		// 沒有 Authorization
		if (!StringUtils.hasText(authorization)) {
			String errMsg = TokenHelper.AUTHORIZATION_HAS_NO_VALUE;
			TPILogger.tl.debug(errMsg);

			errMsg = TokenHelper.UNAUTHORIZED;
			TPILogger.tl.debug(errMsg);
			return getUnauthorizedErrorResp(apiUrl, errMsg);// 401
		}
		return null;
	}

	/**
	 * 取得 Request Header Authorization 的 client 資料 <br>
	 * Basic 格式 <br>
	 * 
	 * @param authorization , 例如: "Basic dHNwY2xpZW50OmRITndZMnhwWlc1ME1USXo=" <br>
	 * @return cliendData[0] : Client ID <br>
	 *         cliendData[1] : Client PW <br>
	 *         若回傳值為 null, 表示 Authorization 格式有問題 <br>
	 */
	public BasicAuthClientData getAuthClientDataForBasic(String authorization, String apiUrl) {
		BasicAuthClientData basicAuthClientData = new BasicAuthClientData();

		try {
			String keyword = TokenHelper.BASIC;
			boolean hasBasic = checkHasKeyword(authorization, keyword);
			if (!hasBasic) {
				// 沒有 Authorization
				String errMsg = MISSING_REQUIRED_PARAMETER + "Authorization";
				basicAuthClientData.errRespEntity = getUnauthorizedErrorResp(apiUrl, errMsg);
				return basicAuthClientData;
			}

			String str = authorization.substring(keyword.length());
			byte[] arr = Base64Util.base64Decode(str);// Base64解碼
			str = new String(arr);
			keyword = ":";
			int index = str.indexOf(keyword);
			if (index == -1) {
				// Authorization 沒有 ":" 字樣
				String errMsg = TokenHelper.AUTHORIZATION_DOES_NOT_HAVE_THE_WORD + keyword;
				TPILogger.tl.debug(errMsg);
				errMsg = TokenHelper.UNAUTHORIZED;
				TPILogger.tl.debug(errMsg);
				basicAuthClientData.errRespEntity = getUnauthorizedErrorResp(apiUrl, errMsg);
				return basicAuthClientData;
			} else {
				String[] cliendData = new String[2];
				cliendData[0] = str.substring(0, index);// Client ID
				cliendData[1] = str.substring(index + 1);// Client PW
				basicAuthClientData.cliendData = cliendData;
			}
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			String errMsg = TokenHelper.INTERNAL_SERVER_ERROR;
			TPILogger.tl.error(errMsg);
			basicAuthClientData.errRespEntity = getInternalServerErrorResp(apiUrl, errMsg);// 500
			return basicAuthClientData;
		}
		return basicAuthClientData;
	}

	/**
	 * 取得 Request Header Authorization 的資料 <br>
	 * DGRK 格式 <br>
	 * 
	 * @param authorization, <br>
	 *                       例如: "DGRK
	 *                       MjRlMGQwYjhhYzA0MDY0ZWVjYjcwM2NkNWEzMzA4ZWQwNjcwMDc1OA==.OsWBMekA0nz6eUpKr2z+Y3Ng4ZZHaa0oC4DRQXTK46E="
	 *                       <br>
	 * @return authData[0] : API Key <br>
	 *         authData[1] : Signature <br>
	 *         若回傳值為 null, 表示 Authorization 格式有問題 <br>
	 */
	public DgrkAuthData getAuthDataForDgrk(String authorization, String apiUrl) {
		DgrkAuthData dgrkAuthData = new DgrkAuthData();

		try {
			String keyword = TokenHelper.DGRK;
			boolean hasDgrk = checkHasKeyword(authorization, keyword);

			// 沒有 Authorization 或格式不正確
			if (!hasDgrk) {
				TPILogger.tl.debug("No Authorization or incorrect format, auth:" + authorization);
				String errMsg = TokenHelper.UNAUTHORIZED;
				TPILogger.tl.debug(errMsg);
				dgrkAuthData.errRespEntity = getUnauthorizedErrorResp(apiUrl, errMsg);
				return dgrkAuthData;
			}

			String dgrkInfoStr = authorization.substring(keyword.length());
			String[] dgrkInfoArr = dgrkInfoStr.split("\\.");// 切分 "DGRK " 後面的值

			// 格式不正確
			if (dgrkInfoArr.length != 2) {
				TPILogger.tl.debug("Incorrect Authorization format, auth:" + authorization);
				String errMsg = TokenHelper.UNAUTHORIZED;
				TPILogger.tl.debug(errMsg);
				dgrkAuthData.errRespEntity = getUnauthorizedErrorResp(apiUrl, errMsg);
				return dgrkAuthData;
			}

			dgrkAuthData.authData = dgrkInfoArr;

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			String errMsg = TokenHelper.INTERNAL_SERVER_ERROR;
			TPILogger.tl.error(errMsg);
			dgrkAuthData.errRespEntity = getInternalServerErrorResp(apiUrl, errMsg);// 500
			return dgrkAuthData;
		}

		return dgrkAuthData;
	}

	/**
	 * 檢查 client 狀態
	 */
	public ResponseEntity<?> checkClientStatus(String clientId, String reqUri) {
		String errMsg = "";

		// 沒有 clientId
		if (!StringUtils.hasText(clientId)) {
			errMsg = MISSING_REQUIRED_PARAMETER + "client_id";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<>(getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
					setContentTypeHeader(), HttpStatus.BAD_REQUEST);// 400
		}

		// 查無 client
		// 因為最終要用findFirstByClientId的cache機制,所以沒用findById
		TsmpClient client = getTsmpClientCacheProxy().findFirstByClientId(clientId);
		if (client == null) {
			return getFindTsmpClientError(clientId, reqUri);
		}

		// client 狀態為2(停用)或3(鎖定)
		String clientStatus = client.getClientStatus();
		if ("2".equals(clientStatus)) {// 停用
			TPILogger.tl.debug("Client(application) is disabled, client_id:" + clientId);// client 已停用
			errMsg = "'" + clientId + "' Client(application) disable";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<>(
					getOAuthTokenErrorResp(TokenHelper.FORBIDDEN, errMsg, HttpStatus.FORBIDDEN.value(), reqUri),
					setContentTypeHeader(), HttpStatus.FORBIDDEN);// 403
		}

		if ("3".equals(clientStatus)) {// 鎖定
			TPILogger.tl.debug("Client(application) is locked, client_id:" + clientId);// client 已鎖定
			errMsg = "'" + clientId + "' Client(application) locked";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<>(
					getOAuthTokenErrorResp(TokenHelper.FORBIDDEN, errMsg, HttpStatus.FORBIDDEN.value(), reqUri),
					setContentTypeHeader(), HttpStatus.FORBIDDEN);// 403
		}

		return null;
	}

	/**
	 * 檢查 無 client 或 client 帳密不對
	 */
	public ResponseEntity<?> checkClientMima(String clientId, String clientPw, String apiUrl) {
		// 沒有 clientId 或 clientPw
		if (!StringUtils.hasText(clientId) || !StringUtils.hasText(clientPw)) {
			TPILogger.tl.debug("No clientId or clientPw");// 沒有 clientId 或 clientPw
			String errMsg = TokenHelper.UNAUTHORIZED;
			TPILogger.tl.debug(errMsg);
			return getUnauthorizedErrorResp(apiUrl, errMsg);// 401
		}

		Optional<OauthClientDetails> optAuthClientDetails = getOauthClientDetailsCacheProxy().findById(clientId);
		if (!optAuthClientDetails.isPresent()) {
			return getFindOauthClientDetailsError(clientId, apiUrl);
		}

		OauthClientDetails authClientDetails = optAuthClientDetails.get();
		if (authClientDetails == null) {
			// Table [OAUTH_CLIENT_DETAILS] 查不到 client
			TPILogger.tl.debug("Table [OAUTH_CLIENT_DETAILS] can't find client(application), client_id:" + clientId);
			String errMsg = TokenHelper.UNAUTHORIZED;
			TPILogger.tl.debug(errMsg);
			return getUnauthorizedErrorResp(apiUrl, errMsg);// 401
		}
		
		String clientSecret = authClientDetails.getClientSecret();// DB 中的密碼 BCrypt 值
		
		boolean isMatch = verifyClientPw(clientId, clientPw, clientSecret);
		if (!isMatch) {// client 帳號或密碼不對
			// 更新 tsmpClient 密碼錯誤次數
			updateTsmpClinetPWDFailTimes(clientId);
			String errMsg1 = TokenHelper.UNAUTHORIZED;
			String errMsg2 = "The client(application) account or password is incorrect. clientId: " + clientId;
			TPILogger.tl.debug("..." + errMsg1 + "\n\t..." + errMsg2);
			return new ResponseEntity<>(
					getOAuthTokenErrorResp(errMsg1, errMsg2, HttpStatus.UNAUTHORIZED.value(), apiUrl),
					setContentTypeHeader(), HttpStatus.UNAUTHORIZED);// 401
		}
		
		return null;
	}
	
	/**
	 * 檢查 client 密碼, 使用 cache,<br>
	 * 因每次都要做密碼 hash 比對, 效能會太差, 改用 cache,<br>
	 * 當 client 的密碼比對成功, 則將 clientSecret(DB 中密碼的 Bcrypt 值) 存入 cache,<br> 
	 * 下次若 cache ID("client ID,密碼") 相同, 直接取出 cache 值和DB 中密碼的 Bcrypt 值比對,<br>
	 * 若 client 改了密碼(DB 中的Bcrypt 值會變動), 但仍輸入舊的密碼, 則會比對失敗<br>
	 * @param clientId - client帳號
	 * @param clientPw - client輸入的密碼
	 * @param clientSecret - DB 中密碼的 BCrypt 值
	 * @return
	 */
	private boolean verifyClientPw(String clientId, String clientPw, String clientSecret) {
		boolean isMatch = false;
		String cacheId = getBasicAuthCacheId(clientId, clientPw);
		
		// 依 cache id, 取得 cache 資料
		BasicAuthCacheVo cacheVo = basicAuthCacheVoMap.get(cacheId);
		if (cacheVo != null) {// 走cache
			boolean isCacheUpdate = isCacheUpdate(cacheVo);
			
			if (isCacheUpdate) {// 更新紀錄
				// 執行 bcrypt 密碼比對
				BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
				isMatch = passwordEncoder.matches(clientPw, clientSecret);// 比對密碼(做 bcrypt)
				if(isMatch) {
					// 如果比對成功,則更新cache紀錄
					updateBasicAuthCache(cacheVo, clientSecret, cacheId);
				}
				
			} else {// 取得cache資料
				isMatch = clientSecret.equals(cacheVo.getHash());// 比對密碼(用 cache 的值比)
				if (!isMatch) {// 表示 client 有改密碼, 移除舊資料
					basicAuthCacheVoMap.remove(cacheId);
				}
			}
			
		} else {// 找不到 cache 資料, 則比對密碼(做 bcrypt)
			BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
			isMatch = passwordEncoder.matches(clientPw, clientSecret);// 比對密碼(做 bcrypt)
			if(isMatch) {
				// 如果比對成功,則更新cache紀錄
				updateBasicAuthCache(cacheVo, clientSecret, cacheId);
			}
		}
		
		return isMatch;
	}
	
	/**
	 * 取得  Basic auth 的 cache id,<br>
	 * 資料格式為 "child id,clientPw"<br>
	 */
	private String getBasicAuthCacheId(String clientId, String clientPw) {
		return clientId.toLowerCase() + "," + clientPw;
	}
	
	/**
	 * Cache 是否過期要更新
	 */
	public boolean isCacheUpdate(BasicAuthCacheVo cacheVo) {
		int cacheTime = 10; // 快取多久後到期 (單位:分)
		long cacheTimestamp = cacheVo.getDataTimestamp() + (cacheTime * 60 * 1000); // 分鐘轉成毫秒
		long nowTimestamp = System.currentTimeMillis();

		return nowTimestamp > cacheTimestamp;
	}
	
	/**
	 * 更新 Basic auth 的 cache 記錄
	 */
	private void updateBasicAuthCache(BasicAuthCacheVo cacheVo, String clientSecret, String cacheId) {
		if (cacheVo == null) {
			cacheVo = new BasicAuthCacheVo();
		}
		
		cacheVo.setHash(clientSecret);
		cacheVo.setDataTimestamp(System.currentTimeMillis());
		
		basicAuthCacheVoMap.put(cacheId, cacheVo);
	}

	/**
	 * 更新 tsmpClient 密碼錯誤次數
	 * 
	 * @param clientId
	 */
	private void updateTsmpClinetPWDFailTimes(String clientId) {
		TsmpClient client = getTsmpClientDao().findById(clientId).orElse(null);
		if (client != null) {
			int pwdFailTimes = client.getPwdFailTimes();
			int failTreshhold = client.getFailTreshhold();
			if (pwdFailTimes + 1 == failTreshhold) {
				client.setPwdFailTimes(pwdFailTimes + 1);
				client.setClientStatus("3");

			} else if (pwdFailTimes + 1 > failTreshhold) {
				client.setClientStatus("3");

			} else {
				client.setPwdFailTimes(pwdFailTimes + 1);
			}

			getTsmpClientDao().save(client);
		}
	}

	/**
	 * 檢查主機清單是否吻合
	 */
	protected ResponseEntity<?> checkHostList(String clientId, String reqUri, HttpServletRequest httpReq) {
		try {
			// 1.沒有 clientId
			if (!StringUtils.hasText(clientId)) {
				TPILogger.tl.debug("No clientId");// 沒有 clientId
				String errMsg = TokenHelper.UNAUTHORIZED;
				TPILogger.tl.debug(errMsg);
				return getUnauthorizedErrorResp(reqUri, errMsg);// 401
			}

			// 2.主機清單
			List<TsmpClientHost> list = getTsmpClientHostCacheProxy().findByClientId(clientId);

			// 3.若有則進入檢查
			if (CollectionUtils.isEmpty(list)) {
				return null;
			} else {
				List<String> ipList = list.stream().map(vo -> vo.getHostIp()).collect(Collectors.toList());
				String ip = ServiceUtil.getIpAddress(httpReq);
				String dn = ServiceUtil.getFQDN(httpReq);
				if (ipList.contains(ip) || ipList.contains(dn)) {
					return null;
				} else {
					String errMsg = null;
					if (!ipList.contains(dn) && dn != null) {
						errMsg = "fqdn(" + dn + ") does not match host list, the clientId is '" + clientId + "'";
					} else {
						errMsg = "ip(" + ip + ") does not match host list, the clientId is '" + clientId + "'";
					}

					String errmsg2 = "Host list: " + ipList;
					TPILogger.tl.debug(errMsg + "\n" + errmsg2);

					return new ResponseEntity<>(
							getOAuthTokenErrorResp(TokenHelper.FORBIDDEN, errMsg, HttpStatus.FORBIDDEN.value(), reqUri),
							setContentTypeHeader(), HttpStatus.FORBIDDEN);// 403
				}
			}
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			String errMsg = TokenHelper.INTERNAL_SERVER_ERROR;
			TPILogger.tl.error(errMsg);
			return getInternalServerErrorResp(reqUri, errMsg);// 500
		}
	}

	/**
	 * 檢查 client 用戶啟日/迄日 & 每日服務時間
	 */
	protected ResponseEntity<?> checkClientStartEndDateAndServiceTime(String clientId, String reqUri) {
		try {
			// 1.沒有 clientId
			if (!StringUtils.hasText(clientId)) {
				TPILogger.tl.debug("No clientId");// 沒有 clientId
				String errMsg = TokenHelper.UNAUTHORIZED;
				TPILogger.tl.debug(errMsg);
				return getUnauthorizedErrorResp(reqUri, errMsg);// 401
			}

			// 2.查無 client
			// 因為最終要用findFirstByClientId的cache機制,所以沒用findById
			TsmpClient tsmpClient = getTsmpClientCacheProxy().findFirstByClientId(clientId);
			if (tsmpClient == null) {
				ResponseEntity<?> errRespEntity = getFindTsmpClientError(clientId, reqUri);
				return errRespEntity;
			}

			Long nowLong = System.currentTimeMillis();

			// 3.檢查 client 用戶啟日/迄日
			ResponseEntity<?> respEntity = checkClientStartEndDate(tsmpClient, nowLong, reqUri);
			if (respEntity != null) {
				return respEntity;
			}

			// 4.檢查 client 每日服務時間
			respEntity = checkClientServiceTime(tsmpClient, nowLong, reqUri);
			if (respEntity != null) {
				return respEntity;
			}

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			String errMsg = TokenHelper.INTERNAL_SERVER_ERROR;
			TPILogger.tl.error(errMsg);
			return getInternalServerErrorResp(reqUri, errMsg);// 500
		}

		return null;
	}

	/**
	 * 檢查 client 用戶啟日/迄日
	 */
	private ResponseEntity<?> checkClientStartEndDate(TsmpClient tsmpClient, Long nowLong, String apiUrl) {
		Long startDateLong = tsmpClient.getStartDate();
		Long endDateLong = tsmpClient.getEndDate();
		String timeZone = tsmpClient.getTimeZone();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-ddZ");

		if (startDateLong != null && endDateLong != null) {
			String startDateStr_yyyyMMdd = TimeZoneUtil.long2UTCstringByFormatter(startDateLong, timeZone, formatter);
			String endDateStr_yyyyMMdd = TimeZoneUtil.long2UTCstringByFormatter(endDateLong, timeZone, formatter);
			String nowStr = TimeZoneUtil.long2UTCstringByFormatter(nowLong, timeZone, formatter);

			int startDate_yyyyMMdd = Integer.parseInt(startDateStr_yyyyMMdd);
			int endDate_yyyyMMdd = Integer.parseInt(endDateStr_yyyyMMdd);
			int now_yyyyMMdd = Integer.parseInt(nowStr);

			// client 未啟用
			if (now_yyyyMMdd < startDate_yyyyMMdd) {// 未啟用
				String startDateStr = TimeZoneUtil.long2UTCstringByFormatter(startDateLong, timeZone, formatter2);
				String errMsg = "Client before start day";
				TPILogger.tl.debug(errMsg);
				String errMsg2 = errMsg + ", start day(yyyy-MM-ddZ): " + startDateStr;
				TPILogger.tl.debug(errMsg2);
				return new ResponseEntity<>(
						getOAuthTokenErrorResp(errMsg, errMsg2, HttpStatus.FORBIDDEN.value(), apiUrl),
						setContentTypeHeader(), HttpStatus.FORBIDDEN);// 403
			}

			// client 已到期
			if (now_yyyyMMdd > endDate_yyyyMMdd) {// 已到期
				String endDateStr = TimeZoneUtil.long2UTCstringByFormatter(endDateLong, timeZone, formatter2);
				String errMsg = "Client expired";
				TPILogger.tl.debug(errMsg);
				String errMsg2 = errMsg + ", end day(yyyy-MM-ddZ): " + endDateStr;
				TPILogger.tl.debug(errMsg2);
				return new ResponseEntity<>(
						getOAuthTokenErrorResp(errMsg, errMsg2, HttpStatus.FORBIDDEN.value(), apiUrl),
						setContentTypeHeader(), HttpStatus.FORBIDDEN);// 403
			}
		}

		return null;
	}

	/**
	 * 檢查 client 每日服務時間
	 */
	private ResponseEntity<?> checkClientServiceTime(TsmpClient tsmpClient, Long nowLong, String apiUrl) {
		Long startTimePerDayLong = tsmpClient.getStartTimePerDay();
		Long endTimePerDayLong = tsmpClient.getEndTimePerDay();
		String zone = tsmpClient.getTimeZone();

		if (startTimePerDayLong != null && endTimePerDayLong != null) {
			// 只取出時分(HHmm)和現在的時分(HHmm) 比較大小
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmm");
			String startTimePerDay_HHmm = TimeZoneUtil.long2UTCstringByFormatter(startTimePerDayLong, zone, formatter);
			String endTimePerDay_HHmm = TimeZoneUtil.long2UTCstringByFormatter(endTimePerDayLong, zone, formatter);
			String nowDay_HHmm = TimeZoneUtil.long2UTCstringByFormatter(nowLong, zone, formatter);

			int startTime_HHmm = Integer.parseInt(startTimePerDay_HHmm);
			int endTime_HHmm = Integer.parseInt(endTimePerDay_HHmm);
			int now_HHmm = Integer.parseInt(nowDay_HHmm);

			if (now_HHmm < startTime_HHmm || now_HHmm > endTime_HHmm) {// 不在每日服務時間內
				DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("HH:mmZ");
				String startTimePerDayStr = TimeZoneUtil.long2UTCstringByFormatter(startTimePerDayLong, zone,
						formatter2);
				String endTimePerDayStr = TimeZoneUtil.long2UTCstringByFormatter(endTimePerDayLong, zone, formatter2);
				String nowStr = TimeZoneUtil.long2UTCstringByFormatter(nowLong, zone, formatter2);

				String errMsg = "Client(application) service time not available";
				TPILogger.tl.debug(errMsg);
				String errMsg2 = errMsg + ", Start Time(HH:mmZ): " + startTimePerDayStr + ", End Time(HH:mmZ): "
						+ endTimePerDayStr + ", Now(HH:mmZ): " + nowStr;
				TPILogger.tl.debug(errMsg2);
				return new ResponseEntity<>(
						getOAuthTokenErrorResp(errMsg, errMsg2, HttpStatus.FORBIDDEN.value(), apiUrl),
						setContentTypeHeader(), HttpStatus.FORBIDDEN);// 403
			}
		}

		return null;
	}

	/**
	 * [ZH] 檢查傳入的 redirectUri 和 client 註冊在系統中的是否相同 <br>
	 * [EN] Check if the redirectUri passed in is the same as the one registered in the client system <br>
	 */
	public ResponseEntity<?> checkRedirectUri(String clientId, String redirectUri, String apiUrl) {
		String errMsg = null;

		// 沒有 redirect_uri
		// No redirect_uri
		if (!StringUtils.hasLength(redirectUri)) {
			errMsg = MISSING_REQUIRED_PARAMETER + "redirect_uri";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<>(getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
					setContentTypeHeader(), HttpStatus.BAD_REQUEST);// 400
		}

		// 取得 OAUTH_CLIENT_DETAILS 的 web_server_redirect_uri
		// Get the web_server_redirect_uri of OAUTH_CLIENT_DETAILS
		Optional<OauthClientDetails> opt_authClientDetails = getOauthClientDetailsCacheProxy().findById(clientId);
		if (!opt_authClientDetails.isPresent()) {
			return getFindOauthClientDetailsError(clientId, apiUrl);
		}

		OauthClientDetails authClientDetails = opt_authClientDetails.get();
		
		// 取得 client 在 digiRunner 設定的重新導向URI資料
		// Get the redirect URI data set by the client in digiRunner
		List<String> webServerRedirectUriList = getWebServerRedirectUriList(authClientDetails);
		String webServerRedirectUriMsg = getMsgForWebServerRedirectUri(webServerRedirectUriList);

		// redirect_uri 不正確
		// The redirect_uri is incorrect
		if (!isMatchRedirectUri(webServerRedirectUriList, redirectUri)) {
			errMsg = String.format("The redirect_uri mismatch. \n" //
					+ "client_id: %s, \n" //
					+ "redirect_uri: %s, \n" //
					+ "Table [OAUTH_CLIENT_DETAILS] web_server_redirect_uri:\n%s" //
					, //
					clientId, //
					redirectUri, //
					webServerRedirectUriMsg //
			);

			TPILogger.tl.debug(errMsg);
			errMsg = "The redirect_uri mismatch";
			return new ResponseEntity<>(getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
					setContentTypeHeader(), HttpStatus.BAD_REQUEST);// 400
		}
		return null;
	}

	/**
	 * [ZH] 檢查傳入的 redirectUri 和 client 註冊在系統中的是否相同 <br>
	 * 若 redirectUri 為 https 或 https, 則網址必須完全相同才是符合 <br>
	 * 否則, redirectUri 只要和註冊的 * 號前後相同即符合 <br>
	 * [EN]Check if the redirectUri passed in is the same as the client registered in the system <br>
	 * If redirectUri is https or https, the URL must be exactly the same to comply with <br>
	 * Otherwise, redirectUri will be in compliance as long as it is the same as the registered * before and after <br>
	 * 
	 * @param webServerRedirectUriList [ZH] client 註冊在 dgR 中的 [EN] The client is
	 *                                 registered in dgR
	 * @param reqRedirectUri           [ZH] 傳入的 redirectUri [EN] The redirectUri
	 *                                 passed in
	 */
	private boolean isMatchRedirectUri(List<String> webServerRedirectUriList, String reqRedirectUri) {
		if (isHttpsOrHttp(reqRedirectUri)) {// https or http
			// 必須完全相同才是符合
			if (webServerRedirectUriList.contains(reqRedirectUri)) {
				return true;
			}
			return false;

		} else {// 不是 https or http
			for (String uriRule : webServerRedirectUriList) {
				if (!isHttpsOrHttp(uriRule)) {// 只比對不是 https or http 的
					uriRule = uriRule.replace(".", "\\.");
					uriRule = uriRule.replace("*", ".*");
					boolean result = ServiceUtil.checkDataByPattern(reqRedirectUri, uriRule);
					if (result) {
						return true;
					}
				}
			}
			return false;
		}
	}

	/**
	 * [ZH] 網址是否為 "https://" 或 "http://" 開頭 <br>
	 * [EN] Whether the URL starts with "https://" or "http://" <br>
	 * @return true: yes; false: not
	 */
	private boolean isHttpsOrHttp(String uri) {
		if (uri == null) {
			return false;
		}

		if (uri.toLowerCase().startsWith("https://") || uri.toLowerCase().startsWith("http://")) {
			return true;
		}

		return false;
	}

	private String getMsgForWebServerRedirectUri(List<String> webServerRedirectUriList) {
		String msg = "";
		for (String uri : webServerRedirectUriList) {
			msg += uri + "\n";
		}
		return msg;
	}

	/**
	 * [ZH] 檢查 user 狀態, 2張表 Users 和 TSMP_USER <br>
	 * [EN] Check user status, 2 tables Users and TSMP_USER <br>
	 */
	public ResponseEntity<?> checkUserStatus(String userName, String apiUrl) {
		if (!StringUtils.hasLength(userName)) {
			return null;
		}

		// 1.Table [Users]
		ResponseEntity<?> respEntity = checkUserStatusByUsers(userName, apiUrl);
		if (respEntity != null) {// 資料有錯誤
			return respEntity;
		}

		// 2.Table [TSMP_USER]
		respEntity = checkUserStatusByTsmpUser(userName, apiUrl);
		if (respEntity != null) {// 資料有錯誤
			return respEntity;
		}

		return null;
	}

	/**
	 * 檢查 user 狀態, Users
	 */
	private ResponseEntity<?> checkUserStatusByUsers(String userName, String apiUrl) {
		Optional<Users> opt_users = getUsersCacheProxy().findById(userName);
		// 查無 users
		if (!opt_users.isPresent()) {
			// Table [Users] 查不到 user
			TPILogger.tl.debug("Table [Users] can't find data, username:" + userName);
			String errMsg = "Bad credentials";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<>(getOAuthTokenErrorResp2(TokenHelper.INVALID_GRANT, errMsg),
					setContentTypeHeader(), HttpStatus.BAD_REQUEST);// 400
		}

		Users users = opt_users.get();
		int userStatus = users.getUserStatus();
		if (userStatus == 0) {// 停用
			// Table [Users] 狀態已停用
			TPILogger.tl.debug("Table [Users] status disabled, username:" + userName);
			String errMsg = "User is disabled";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<>(getOAuthTokenErrorResp2(TokenHelper.INVALID_GRANT, errMsg),
					setContentTypeHeader(), HttpStatus.BAD_REQUEST);// 400
		}

		return null;
	}

	/**
	 * 檢查 user 狀態, TSMP_USER
	 */
	private ResponseEntity<?> checkUserStatusByTsmpUser(String userName, String apiUrl) {
		String errMsg = "";
		// 查無 user 或 狀態為2(停用)或3(鎖定)
		TsmpUser user = getTsmpUserCacheProxy().findFirstByUserName(userName);
		if (user == null) {
			// Table [TSMP_USER] 查不到資料
			TPILogger.tl.debug("Table [TSMP_USER] can't find data, userName:" + userName);
			errMsg = TokenHelper.UNAUTHORIZED;
			TPILogger.tl.debug(errMsg);
			return getUnauthorizedErrorResp(apiUrl, errMsg);// 401
		}

		String userStatus = user.getUserStatus();
		if ("2".equals(userStatus)) {// 停用
			TPILogger.tl.debug("User is disabled, user_name:" + userName);// user 已停用
			errMsg = "'" + userName + "' User disable";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<>(
					getOAuthTokenErrorResp(TokenHelper.FORBIDDEN, errMsg, HttpStatus.FORBIDDEN.value(), apiUrl),
					setContentTypeHeader(), HttpStatus.FORBIDDEN);// 403
		}

		if ("3".equals(userStatus)) {// 鎖定
			TPILogger.tl.debug("User is locked, user_name:" + userName);// user 已鎖定
			errMsg = "'" + userName + "' User locked";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<>(
					getOAuthTokenErrorResp(TokenHelper.FORBIDDEN, errMsg, HttpStatus.FORBIDDEN.value(), apiUrl),
					setContentTypeHeader(), HttpStatus.FORBIDDEN);// 403
		}
		return null;
	}

	/**
	 * 檢查 user 狀態, DGR_AC_IDP_USER
	 */
	public ResponseEntity<?> checkUserStatusByDgrAcIdpUser(String userName, String apiUrl, String idPType) {
		String errMsg = "";
		// 查無 user 或 狀態為1(request)或3(deny)
		DgrAcIdpUser user = getDgrAcIdpUserDao().findFirstByUserNameAndIdpType(userName, idPType);
		if (user == null) {
			// Table [DGR_AC_IDP_USER] 查不到資料
			TPILogger.tl.debug("Table [DGR_AC_IDP_USER] can't find data, userName:" + userName);
			errMsg = TokenHelper.UNAUTHORIZED;
			TPILogger.tl.debug(errMsg);
			return getUnauthorizedErrorResp(apiUrl, errMsg);// 401
		}

		String userStatus = user.getUserStatus();
		if (DgrAcIdpUserStatus.REQUEST.isValueEquals(userStatus)) {// 1:request
			TPILogger.tl.debug("User status is request, user_name:" + userName);// user request
			errMsg = "'" + userName + "' User status is request";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<>(
					getOAuthTokenErrorResp(TokenHelper.FORBIDDEN, errMsg, HttpStatus.FORBIDDEN.value(), apiUrl),
					setContentTypeHeader(), HttpStatus.FORBIDDEN);// 403
		}

		if (DgrAcIdpUserStatus.DENY.isValueEquals(userStatus)) {// 3:deny
			TPILogger.tl.debug("User status is deny', user_name:" + userName);// user deny
			errMsg = "'" + userName + "' User status is deny";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<>(
					getOAuthTokenErrorResp(TokenHelper.FORBIDDEN, errMsg, HttpStatus.FORBIDDEN.value(), apiUrl),
					setContentTypeHeader(), HttpStatus.FORBIDDEN);// 403
		}
		return null;
	}

	/**
	 * 檢查 無 user 或 user 帳密不對
	 */
	public ResponseEntity<?> checkUserSecret(String userName, String userPw, String apiUrl) {
		Optional<Users> opt_users = getUsersCacheProxy().findById(userName);
		// 查無 users
		if (!opt_users.isPresent()) {
			// Table [Users] 查不到 user
			TPILogger.tl.debug("Table [Users] can't find data, username:" + userName);
			String errMsg = "Bad credentials";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<>(getOAuthTokenErrorResp2(TokenHelper.INVALID_GRANT, errMsg),
					setContentTypeHeader(), HttpStatus.BAD_REQUEST);// 400
		}

		Users users = opt_users.get();
		String userSecret = users.getPassword();
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		boolean isMatch = passwordEncoder.matches(userPw, userSecret);// 比對密碼
		if (!isMatch) {
			// user 帳號或密碼不對
			TPILogger.tl.debug("User account or password is incorrect, username:" + userName);
			String errMsg = "Bad credentials";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<>(getOAuthTokenErrorResp2(TokenHelper.INVALID_GRANT, errMsg),
					setContentTypeHeader(), HttpStatus.BAD_REQUEST);// 400
		}

		return null;
	}

	/**
	 * 檢查 idPType 是否為支援的 IdP type
	 */
	public ResponseEntity<?> checkSupportGtwIdPType(String idPType) {
		String errMsg = null;

		// Gateway IdP 支援的 idPType
		List<String> idPTypeList = GtwIdPHelper.getSupportGtwIdPType();

		// 無效的 idPType
		boolean isSupport = idPTypeList.contains(idPType);// 轉小寫,再比較(忽略大小寫)
		if (!isSupport) {
			errMsg = "Invalid idPType: " + idPType;
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<>(getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
					setContentTypeHeader(), HttpStatus.BAD_REQUEST);// 400
		}

		return null;
	}

	/**
	 * 檢查 response_type 是否有值 且 正確的值 "code"
	 */
	public ResponseEntity<?> checkResponseType(String responseType) {
		String errMsg = null;

		// 沒有 response_type
		if (!StringUtils.hasText(responseType)) {
			errMsg = TokenHelper.MISSING_REQUIRED_PARAMETER + "response_type";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<>(getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
					setContentTypeHeader(), HttpStatus.BAD_REQUEST);// 400
		}

		// 無效的 response_type
		if (!"code".equals(responseType)) {
			errMsg = "Invalid response_type: " + responseType;
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<>(getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
					setContentTypeHeader(), HttpStatus.BAD_REQUEST);// 400
		}

		return null;
	}

	/**
	 * 檢查 scope 是否為支援的 OpenID Connect scopes
	 */
	public ResponseEntity<?> checkOidcScope(String reqScopeStr) {
		String errMsg = null;
		// 沒有 scope
		if (!StringUtils.hasLength(reqScopeStr)) {
			errMsg = MISSING_REQUIRED_PARAMETER + "scope";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<>(getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
					setContentTypeHeader(), HttpStatus.BAD_REQUEST);// 400
		}

		// 支援的 scope
		List<String> supportScope = GtwIdPHelper.getSupportScopeList();
		String[] reqScopeArr = reqScopeStr.split(" ");
		String errScopeStr = "";
		for (String reqScope : reqScopeArr) {
			boolean isSupport = supportScope.contains(reqScope.toLowerCase());// 轉小寫,再比較(忽略大小寫)
			if (!isSupport) {
				if (StringUtils.hasLength(errScopeStr)) {
					errScopeStr += ", ";
				}
				errScopeStr += reqScope;
			}
		}

		// 不是支援的 scope
		if (StringUtils.hasLength(errScopeStr)) {
			errMsg = "Some requested scopes were invalid. {invalid=[" + errScopeStr + "]}";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<>(getOAuthTokenErrorResp2(TokenHelper.INVALID_SCOPE, errMsg),
					setContentTypeHeader(), HttpStatus.BAD_REQUEST);// 400
		}

		return null;
	}

	public ResponseEntity<?> checkHasToken(String token, String msgWord) {
		String errMsg = "";
		if (token == null) {
			TPILogger.tl.debug("Body does not have " + msgWord);// Body 沒有 {token/refresh_token}
			errMsg = TokenHelper.CANNOT_CONVERT_ACCESS_TOKEN_TO_JSON;
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<>(getOAuthTokenErrorResp2(TokenHelper.INVALID_TOKEN, errMsg),
					setContentTypeHeader(), HttpStatus.UNAUTHORIZED);// 401
		}

		if ("".equals(token)) {
			TPILogger.tl.debug("Body has " + msgWord + ", but no value");// Body 有 {token/refresh_token}, 但沒有值
			TPILogger.tl.debug("Full authentication is required to access this resource");
			errMsg = TokenHelper.UNAUTHORIZED;
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<>(getOAuthTokenErrorResp2(errMsg, errMsg),
					setContentTypeHeader(), HttpStatus.UNAUTHORIZED);// 401
		}
		return null;
	}

	/**
	 * 1.取得 token (access token / refresh token) 的 payload, <br>
	 * 2.做 JWS 驗章 或 JWE 解密 <br>
	 * (1).JWS 驗章, 使用 digiRunner 的公鑰驗章, 取得 payload 做 Base64 Decode 後的值 <br>
	 * (2).JWE 解密, 使用 digiRunner 的私鑰解密, 取得 payload 解密後的值 <br>
	 */
	public JwtPayloadData getJwtPayloadData(String jwtStr) {
		JwtPayloadData jwtPayloadData = new JwtPayloadData();
		try {
			String[] jwtInfoArr = jwtStr.split("\\.");// 切分 JWT

			if (jwtInfoArr.length == 3) {// JWS
				/* JWS 驗章, 使用 digiRunner 的公鑰驗章 */

				// 1.取得 digiRunner 的公鑰
				PublicKey publicKey = getTsmpCoreTokenHelper().getKeyPair().getPublic();

				// 2.JWS 驗章
				boolean verify = JWScodec.jwsVerifyByRS(publicKey, jwtStr);
				if (!verify) {// JWS 驗證不正確
					String errMsg = "Token JWS verify error.";
					TPILogger.tl.debug(errMsg);
					jwtPayloadData.errRespEntity = getTokenFormatError();
				}

				// 3.取得 payload 做 Base64 Decode 後的值
				String payloadJsonStr = new String(Base64Util.base64URLDecode(jwtInfoArr[1]));
				ObjectMapper om = new ObjectMapper();
				jwtPayloadData.payloadStr = payloadJsonStr;
				jwtPayloadData.payloadJsonNode = om.readTree(payloadJsonStr);

			} else if (jwtInfoArr.length == 5) {// JWE
				/* JWE 解密, 使用 digiRunner 的私鑰解密 */

				// 1.取得 digiRunner 的私鑰
				PrivateKey privateKey = getTsmpCoreTokenHelper().getKeyPair().getPrivate();
				try {
					// 2.JWE 解密, 取得 payload 解密後的值
					String payloadJsonStr = JWEcodec.jweDecryption(privateKey, jwtStr);// JWE 解密
					ObjectMapper om = new ObjectMapper();
					jwtPayloadData.payloadStr = payloadJsonStr;
					jwtPayloadData.payloadJsonNode = om.readTree(payloadJsonStr);

				} catch (Exception e) {// JWE 解密錯誤
					TPILogger.tl.warn(StackTraceUtil.logStackTrace(e));
					String errMsg = "Token JWE decryption error.";
					TPILogger.tl.debug(errMsg);
					jwtPayloadData.errRespEntity = getTokenFormatError();
				}

			} else {// 錯誤格式
				jwtPayloadData.errRespEntity = getTokenFormatError();
			}

		} catch (Exception e) {
			TPILogger.tl.warn(StackTraceUtil.logStackTrace(e));
			jwtPayloadData.errRespEntity = getTokenFormatError();
		}

		return jwtPayloadData;
	}

	/**
	 * token 的 client_id 沒有值, 或 client_id 和 token 中的 client_id 值不相同
	 * 
	 * @param clientId
	 * @param tokenClientId
	 * @param tokenStr
	 * @param msg
	 * @return
	 */
	public ResponseEntity<?> checkTokenClientId(String clientId, String tokenClientId, String tokenStr, String msg) {
		if (!StringUtils.hasText(tokenClientId)) {
			TPILogger.tl.debug("client_id in " + msg + " has no value");// token 的 client_id 沒有值
			String errMsg = TokenHelper.CANNOT_CONVERT_ACCESS_TOKEN_TO_JSON;
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<>(getOAuthTokenErrorResp2(TokenHelper.INVALID_TOKEN, errMsg),
					setContentTypeHeader(), HttpStatus.BAD_REQUEST);// 400
		}

		// client_id 和 token 中的 client_id 值不相同
		if (!clientId.equals(tokenClientId)) {
			TPILogger.tl.debug("The client_id and client_id in " + msg + " are not the same");
			String errMsg = "Wrong client(application) for this " + msg + ": " + tokenStr;
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<>(getOAuthTokenErrorResp2(TokenHelper.INVALID_GRANT, errMsg),
					setContentTypeHeader(), HttpStatus.BAD_REQUEST);// 400
		}
		return null;
	}

	/**
	 * token 是否已過期
	 * 
	 * @param tokenTypeHint
	 * @param tokenStr
	 * @param exp
	 * @return
	 */
	public boolean checkTokenExpired(String tokenTypeHint, String tokenStr, Long exp) {
		boolean isTokenExpired = false;

		ResponseEntity<?> respEntity = null;
		if (DgrTokenType.ACCESS_TOKEN.equalsIgnoreCase(tokenTypeHint)) {
			respEntity = checkAccessTokenExp(tokenStr, exp);

		} else if (DgrTokenType.REFRESH_TOKEN.equalsIgnoreCase(tokenTypeHint)) {
			respEntity = checkRefreshTokenExp(tokenStr, exp);
		}

		if (respEntity != null) {// 資料驗證錯誤或過期
			isTokenExpired = true;
		}

		return isTokenExpired;
	}

	/**
	 * 是否 access token exp 沒有值 或 access token 過期
	 * 
	 * @return
	 */
	public ResponseEntity<?> checkAccessTokenExp(String tokenStr, Long exp) {
		if (exp == null || exp == 0) {
			TPILogger.tl.debug("The exp of access token is 0");// refresh_token 的 exp 為 0
			String errMsg = TokenHelper.CANNOT_CONVERT_ACCESS_TOKEN_TO_JSON;
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<>(getOAuthTokenErrorResp2(TokenHelper.INVALID_TOKEN, errMsg),
					setContentTypeHeader(), HttpStatus.BAD_REQUEST);// 400
		}

		// access token 過期
		long nowTime = System.currentTimeMillis() / 1000;// 去掉亳秒
		if (exp < nowTime) {
			// access token 過期
			TPILogger.tl.debug(TokenHelper.ACCESS_TOKEN_EXPIRED + exp);
			String errMsg = TokenHelper.ACCESS_TOKEN_EXPIRED + tokenStr;
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<>(getOAuthTokenErrorResp2(TokenHelper.INVALID_TOKEN, errMsg),
					setContentTypeHeader(), HttpStatus.UNAUTHORIZED);// 401
		}
		return null;
	}

	/**
	 * 是否 refresh token exp 沒有值 或 refresh token 過期
	 * 
	 * @param tokenStr
	 * @param exp      到期日,單位為秒,10碼
	 * @return
	 */
	public ResponseEntity<?> checkRefreshTokenExp(String tokenStr, Long exp) {
		if (exp == null || exp == 0) {
			TPILogger.tl.debug("The exp of refresh token is 0");// refresh_token 的 exp 為 0
			String errMsg = "Cannot convert refresh token to JSON";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<>(getOAuthTokenErrorResp2(TokenHelper.INVALID_TOKEN, errMsg),
					setContentTypeHeader(), HttpStatus.BAD_REQUEST);// 400
		}

		// refresh_token 過期
		long nowTime = System.currentTimeMillis() / 1000;// 去掉亳秒
		if (exp < nowTime) {// 都是用單位秒來比較
			// refresh_token 過期
			Date expireDate = new Date(exp * 1000);// 轉為 Date,要用亳秒
			String expiredStr = DateTimeUtil.dateTimeToString(expireDate, DateTimeFormatEnum.西元年月日時分秒毫秒_2)
					.orElse(exp + "");
			String errMsg1 = "Refresh token expired, exp: " + exp + ", expired at: " + expiredStr;
			TPILogger.tl.debug(errMsg1);
			String errMsg2 = errMsg1;
			if (StringUtils.hasLength(tokenStr)) {
				errMsg2 = "Invalid refresh token (expired): " + tokenStr;
				TPILogger.tl.debug(errMsg2);
			}
			return new ResponseEntity<>(getOAuthTokenErrorResp2(TokenHelper.INVALID_TOKEN, errMsg2),
					setContentTypeHeader(), HttpStatus.UNAUTHORIZED);// 401
		}

		return null;
	}

	/**
	 * 是否 access token / refresh token 已撤銷
	 */
	public boolean checkTokenRevoked(String tokenTypeHint, String jti) {
		boolean isTokenRevoked = false;

		ResponseEntity<?> respEntity = null;
		if (DgrTokenType.ACCESS_TOKEN.equalsIgnoreCase(tokenTypeHint)) {
			respEntity = checkAccessTokenRevoked(jti);

		} else if (DgrTokenType.REFRESH_TOKEN.equalsIgnoreCase(tokenTypeHint)) {
			respEntity = checkRefreshTokenRevoked(jti);
		}

		if (respEntity != null) {// 資料驗證錯誤或過期
			isTokenRevoked = true;
		}
		TPILogger.tl.trace("isTokenRevoked: " + isTokenRevoked);
		return isTokenRevoked;
	}

	/**
	 * 檢查 jti 是否有值
	 */
	private ResponseEntity<?> checkJtiHasValue(String jti) {
		if (!StringUtils.hasLength(jti)) {
			String errMsg = MISSING_REQUIRED_PARAMETER + "jti";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<>(getOAuthTokenErrorResp2(TokenHelper.INVALID_TOKEN, errMsg),
					setContentTypeHeader(), HttpStatus.BAD_REQUEST);// 400
		}

		return null;
	}

	/**
	 * 是否 access token 已撤銷
	 */
	public ResponseEntity<?> checkAccessTokenRevoked(String jti) {
		ResponseEntity<?> respEntity = checkJtiHasValue(jti);
		if (respEntity != null) {// 資料有錯誤
			return respEntity;
		}

		TsmpTokenHistory tsmpTokenHistory = getTsmpTokenHistoryCacheProxy().findFirstByTokenJti(jti);

		return checkAccessTokenRevoked(tsmpTokenHistory, jti);
	}

	/**
	 * 是否 access token 已撤銷
	 */
	public ResponseEntity<?> checkAccessTokenRevoked(TsmpTokenHistory tsmpTokenHistory, String jti) {
		boolean isRevoked = false;

		if (tsmpTokenHistory == null) {// 查無資料
			// Table [TSMP_TOKEN_HISTORY] 查不到資料
			TPILogger.tl.debug("Table [TSMP_TOKEN_HISTORY] can't find data, token_jti:" + jti);
			isRevoked = true;

		} else {
			/*
			 * 1.token 被 API 撤銷時, 應更新該筆 token 的 revoked_at 欄位, 押上時間以及更新 revoked_status = "R"
			 * 2.發新 token 時, 後踢前一筆有效 TOKEN 撤銷時, 應更新該筆 token 的 revoked_at 欄位, 押上時間以及更新
			 * revoked_status = "N" 3.當利用 Refresh Token 換取新的 Token 時, 新 Token 寫入該 Table,
			 * 當前被撤銷的 token 的 revoked_at 欄位, 押上時間以及更新 revoked_status = "RT"
			 */
			String revokedStatus = tsmpTokenHistory.getRevokedStatus();// 取出來如果是一個字母,後面會多一個空白,所以要做trim()
			revokedStatus = (revokedStatus != null) ? revokedStatus.trim() : revokedStatus;

			if (StringUtils.hasLength(revokedStatus)) {// 狀態若有值,表示已撤銷,例如,R / N / RT
				isRevoked = true;
			}
		}

		if (isRevoked) {
			// access_token 已撤銷
			ResponseEntity<?> respEntity = getTokenRevokedError(jti);// 403
			return respEntity;
		}
		return null;
	}

	/**
	 * 是否 refresh token 已撤銷
	 */
	public ResponseEntity<?> checkRefreshTokenRevoked(String jti) {
		boolean isRevoked = false;
		// 找出所有的 refresh token
		List<TsmpTokenHistory> tsmpTokenHistoryList = getTsmpTokenHistoryDao().findByRetokenJti(jti);
		if (CollectionUtils.isEmpty(tsmpTokenHistoryList)) {// 查無資料
			// Table [TSMP_TOKEN_HISTORY] 查不到資料
			TPILogger.tl.debug("Table [TSMP_TOKEN_HISTORY] can't find data, retoken_jti:" + jti);
			isRevoked = true;

		} else {
			for (TsmpTokenHistory tsmpTokenHistory : tsmpTokenHistoryList) {
				String rftRevokedStatus = tsmpTokenHistory.getRftRevokedStatus();
				if (StringUtils.hasLength(rftRevokedStatus)) {// 狀態若有值,表示已撤銷,例如,R / N
					isRevoked = true;
					break;
				}
			}
		}

		if (isRevoked) {
			// refresh token 已撤銷
			ResponseEntity<?> respEntity = getRefreshTokenRevokedError(jti);
			return respEntity;
		}

		return null;
	}

	/**
	 * 1.檢查 refresh_token 的 user_name 是否有值 <br>
	 * 2.檢查 user / IdP user 狀態 <br>
	 */
	public ResponseEntity<?> checkRefreshTokenUserName(JsonNode payloadJsonNode, String retokenUserName,
			String apiUrl) {
		if (!StringUtils.hasText(retokenUserName)) {
			TPILogger.tl.debug("user_name in refresh token has no value");
			String errMsg = "Cannot convert refresh token to JSON";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<>(getOAuthTokenErrorResp2(TokenHelper.INVALID_TOKEN, errMsg),
					setContentTypeHeader(), HttpStatus.BAD_REQUEST);// 400
		}

		String idPType = JsonNodeUtil.getNodeAsText(payloadJsonNode, "idp_type");
		boolean isGtwIdPFlow = isGtwIdPFlow(retokenUserName, idPType);
		if (isGtwIdPFlow) {
			// 不用檢查,因為沒有 delegate user 和 tsmp_user

		} else if (StringUtils.hasLength(idPType)) {// 當 grant_type 為 "delegate_auth", 會有 idp_type
			// 檢查 IdP user 狀態
			ResponseEntity<?> respEntity = checkUserStatusByDgrAcIdpUser(retokenUserName, apiUrl, idPType);
			if (respEntity != null) {// 資料有錯誤
				return respEntity;
			}

		} else {
			// 檢查 user 狀態
			ResponseEntity<?> respEntity = checkUserStatus(retokenUserName, apiUrl);
			if (respEntity != null) {// 資料有錯誤
				return respEntity;
			}
		}

		return null;
	}

	public TsmpTokenHistory updateTokenHistory(String ati, String jti, String revokedStatus) {
		TsmpTokenHistory tsmpTokenHistory = getTsmpTokenHistoryDao().findFirstByTokenJtiAndRetokenJti(ati, jti);
		if (tsmpTokenHistory != null) {
			tsmpTokenHistory.setRevokedStatus(revokedStatus);
			tsmpTokenHistory.setRevokedAt(DateTimeUtil.now());
			tsmpTokenHistory = getTsmpTokenHistoryDao().saveAndFlush(tsmpTokenHistory);
		}

		return tsmpTokenHistory;
	}

	/**
	 * 白名單記錄(後踢前), 把舊的token狀態改為 'N' (TSMP_TOKEN_HISTORY), <br>
	 * 1.處理舊的 access token 狀態: <br>
	 * (1).若 grant_type 不是 client_credientails, 找出此 client id + user 的所有記錄, <br>
	 * access token 的 revoked_status 若沒有值,更新狀態為"N" <br>
	 * 2.處理舊的 refresh token 狀態: <br>
	 * (1).若 grant_type = refresh_token 資料, rft_revoked_status 狀態不變; <br>
	 * (2).否則, 若 grant_type 不是 client_credientails, <br>
	 * refresh token 的 rft_revoked_status 若沒有值,更新狀態為"N" <br>
	 */
	public List<TsmpTokenHistory> updateTokenHistoryForWhitelist(String grantType, String clientId, String userName) {
		List<TsmpTokenHistory> tsmpTokenHistoryList = getTsmpTokenHistoryDao().findByClientIdAndUserName(clientId,
				userName);
		for (TsmpTokenHistory tokenHistory : tsmpTokenHistoryList) {
			String oldRevokedStatus = tokenHistory.getRevokedStatus();
			if (!StringUtils.hasLength(oldRevokedStatus)) {
				// 舊的 access token 資料,狀態設為"N"
				tokenHistory.setRevokedStatus("N");
				tokenHistory.setRevokedAt(DateTimeUtil.now());
			}

			if (!DgrTokenGrantType.REFRESH_TOKEN.equalsIgnoreCase(grantType)) {
				String oldRftRevokedStatus = tokenHistory.getRftRevokedStatus();
				if (!StringUtils.hasLength(oldRftRevokedStatus)) {
					// 若 grant_type = refresh_token 資料,狀態不變; 否則,改為"N"
					tokenHistory.setRftRevokedStatus("N");
					tokenHistory.setRftRevokedAt(DateTimeUtil.now());
				}
			}
			tokenHistory = getTsmpTokenHistoryDao().saveAndFlush(tokenHistory);
		}

		return tsmpTokenHistoryList;
	}

	/**
	 * 將 Long(秒/毫秒) 轉成 Date
	 * 
	 * @param time
	 * @return
	 */
	public Date convertLongToDate(Long time) {
		if (time.toString().length() == 10) {// 若為秒
			time = time * 1000;// 轉成毫秒
		}
		Date date = new Date(time);
		return date;
	}

	/**
	 * 建立 TSMP access_token 歷史紀錄
	 */
	public TsmpTokenHistory createTsmpTokenHistory(String userName, String clientId, String accessTokenJti,
			String scopeStr, Date expiredAt, String refreshTokenJti, Date reexpiredAt, Date stimeDate, Long tokenQuota,
			Long tokenUsed, Long rftQuota, Long rftUsed, String idPType, String idTokenJwt, String refreshTokenJwtstr,
			String apiResp) {

		TsmpTokenHistory tsmpTokenHistory = null;
		tsmpTokenHistory = new TsmpTokenHistory();
		// 取得流水號, 取用TSMP內部的序號
		final Long seq = getSeqStoreService().nextTsmpSequence(TsmpSequenceName.SEQ_TSMP_TOKEN_HISTORY_PK);
		if (seq != null) {
			tsmpTokenHistory.setSeqNo(seq);
		}

		tsmpTokenHistory.setUserNid(null);
		tsmpTokenHistory.setUserName(userName);
		tsmpTokenHistory.setClientId(clientId);
		tsmpTokenHistory.setTokenJti(accessTokenJti);
		tsmpTokenHistory.setScope(scopeStr);
		tsmpTokenHistory.setExpiredAt(expiredAt);
		tsmpTokenHistory.setCreateAt(DateTimeUtil.now());
		tsmpTokenHistory.setRevokedAt(null);
		tsmpTokenHistory.setRevokedStatus(null);
		tsmpTokenHistory.setRetokenJti(refreshTokenJti);
		tsmpTokenHistory.setReexpiredAt(reexpiredAt);
		tsmpTokenHistory.setStime(stimeDate);
		tsmpTokenHistory.setRftRevokedAt(null);
		tsmpTokenHistory.setRftRevokedStatus(null);
		tsmpTokenHistory.setTokenQuota(tokenQuota);
		tsmpTokenHistory.setTokenUsed(tokenUsed);
		tsmpTokenHistory.setRftQuota(rftQuota);
		tsmpTokenHistory.setRftUsed(rftUsed);
		tsmpTokenHistory.setIdpType(idPType);
		tsmpTokenHistory.setIdTokenJwtstr(idTokenJwt);
		tsmpTokenHistory.setRefreshTokenJwtstr(refreshTokenJwtstr);
		tsmpTokenHistory.setApiResp(apiResp);

		tsmpTokenHistory = getTsmpTokenHistoryDao().saveAndFlush(tsmpTokenHistory);
		return tsmpTokenHistory;
	}

	/**
	 * 驗證打 API 的 "X-Api-Key"
	 */
	public ResponseEntity<?> verifyApiForXApiKey(String xApiKey, HttpServletRequest httpReq) throws Exception {
		String reqUri = httpReq.getRequestURI();
		String moduleName = httpReq.getAttribute(GatewayFilter.MODULE_NAME).toString();
		String apiId = httpReq.getAttribute(GatewayFilter.API_ID).toString();

		// 1.找不到符合的 X-Api-Key
		String xApiKeyEn = getXApiKeyEn(xApiKey);// 取得 X-Api-Key 經過 SHA256 的值
		DgrXApiKey dgrXApiKey = getDgrXApiKeyCacheProxy().findFirstByApiKeyEn(xApiKeyEn);
		if (dgrXApiKey == null) {
			String errMsg = "X-Api-Key Not Found";
			TPILogger.tl.debug(errMsg);
			return getUnauthorizedErrorResp(reqUri, errMsg);// 401
		}

		Long xApiKeyId = dgrXApiKey.getApiKeyId();
		String clientId = dgrXApiKey.getClientId();
		Long effectiveAt = dgrXApiKey.getEffectiveAt();// X-Api-Key 生效日期, 亳秒
		Long expiredAt = dgrXApiKey.getExpiredAt();// X-Api-Key 到期日期, 亳秒

		// 2.檢查 client 狀態
		ResponseEntity<?> checkClientResp = checkClientStatus(clientId, reqUri);
		if (checkClientResp != null) {// 資料驗證有錯誤
			return checkClientResp;
		}

		// 3.檢查主機清單
		ResponseEntity<?> respEntity = checkHostList(clientId, reqUri, httpReq);
		if (respEntity != null) {// 資料驗證有錯誤
			return respEntity;
		}

		// 4.檢查 client 用戶啟日/迄日 & 每日服務時間
		respEntity = checkClientStartEndDateAndServiceTime(clientId, reqUri);
		if (respEntity != null) {// 資料驗證有錯誤
			return respEntity;
		}

		// 5.檢查是否 X-Api-Key 未生效
		long nowTime = System.currentTimeMillis();// 亳秒
		if (effectiveAt > nowTime) {// 未生效
			Date effectiveDate = new Date(effectiveAt);
			String effectiveDateStr = DateTimeUtil.dateTimeToString(effectiveDate, DateTimeFormatEnum.西元年月日時分秒毫秒_2)
					.orElse(null);
			String errMsg = "X-Api-Key is not valid, effective date is " + effectiveDateStr;
			TPILogger.tl.debug(errMsg);
			return getUnauthorizedErrorResp(reqUri, errMsg);// 401
		}

		// 6.檢查是否 X-Api-Key 已過期
		if (expiredAt < nowTime) {// 過期
			Date expireDate = new Date(expiredAt);
			String expiredStr = DateTimeUtil.dateTimeToString(expireDate, DateTimeFormatEnum.西元年月日時分秒毫秒_2).orElse(null);
			String errMsg = "X-Api-Key Has Expired, expired at: " + expiredStr;
			TPILogger.tl.debug(errMsg);
			return getUnauthorizedErrorResp(reqUri, errMsg);// 401
		}

		// 7.檢查 X-Api-Key 是否有權限打此 API
		respEntity = checkXApiKeyPermission(apiId, moduleName, xApiKeyId, reqUri);
		if (respEntity != null) {
			return respEntity;
		}

		httpReq.setAttribute(TokenHelper.CLIENT_ID, clientId);

		return null;
	}

	/**
	 * 取得 X-Api-Key 經過 SHA256 的值
	 */
	public static String getXApiKeyEn(String xApiKey) throws Exception {
		byte[] byteArr = SHA256Util.getSHA256(xApiKey.getBytes());
		String xApiKeyEn = HexStringUtils.toString(byteArr);// 大寫
		return xApiKeyEn;
	}

	/**
	 * 檢查使用的 X-Api-Key 是否有取用此 API 的權限
	 */
	protected ResponseEntity<?> checkXApiKeyPermission(String apiId, String moduleName, Long apiKeyId, String apiUrl) {
		List<DgrXApiKeyMap> dgrXApiKeyMapList = getDgrXApiKeyMapCacheProxy().findByRefApiKeyId(apiKeyId);
		List<String> xApiKeyScopeList = new ArrayList<>();
		for (DgrXApiKeyMap dgrXApiKeyMap : dgrXApiKeyMapList) {
			String groupId = dgrXApiKeyMap.getGroupId();
			if (StringUtils.hasLength(groupId)) {
				xApiKeyScopeList.add(groupId);
			}
		}

		ResponseEntity<?> respEntity = checkXApiKeyScope(apiId, moduleName, xApiKeyScopeList, apiUrl);
		if (respEntity != null) {
			return respEntity;
		}

		return null;
	}

	/**
	 * 檢查 X-Api-Key 的 scope 是否有權限打 API
	 */
	protected ResponseEntity<?> checkXApiKeyScope(String apiId, String moduleName, List<String> xApiKeyScopeList,
			String apiUrl) {
		// 取得此 API 被授權的 scope(group id)
		List<String> apiScopeList = getApiScopeList(apiId, moduleName);

		List<String> xApiKeyScopeList2 = new ArrayList<>();
		xApiKeyScopeList2.addAll(xApiKeyScopeList);

		xApiKeyScopeList2.retainAll(apiScopeList);// 交集
		if (xApiKeyScopeList2.size() == 0) {
			String errMsg = "X-Api-Key Violates Scope Authorization Access Settings";
			String errLog = errMsg + "\n" + "X-Api-Key ScopeList:" + xApiKeyScopeList.toString() + "\n"
					+ "Api ScopeList:" + apiScopeList.toString();
			TPILogger.tl.debug(errLog);
			return new ResponseEntity<>(
					getOAuthTokenErrorResp(TokenHelper.FORBIDDEN, errMsg, HttpStatus.FORBIDDEN.value(), apiUrl),
					setContentTypeHeader(), HttpStatus.FORBIDDEN);// 403
		}

		return null;
	}

	/**
	 * 驗證打 API 的 "authorization"
	 */
	public ResponseEntity<?> verifyApiForAuth(String authorization, HttpServletRequest httpReq, String payload) {
		String reqUri = httpReq.getRequestURI();
		String moduleName = httpReq.getAttribute(GatewayFilter.MODULE_NAME).toString();
		String apiId = httpReq.getAttribute(GatewayFilter.API_ID).toString();

		// 1.是否有 authorization
		ResponseEntity<?> respEntity = checkHasAuthorization(authorization, reqUri);
		if (respEntity != null) {
			return respEntity;
		}

		// 2.basic 格式 (id:pwd)
		// 是否有"basic "字樣,忽略大小寫
		boolean hasBasic = checkHasKeyword(authorization, TokenHelper.BASIC);
		if (hasBasic) {
			respEntity = verifyApiForBasic(authorization, apiId, moduleName, reqUri, httpReq);
			if (respEntity != null) {
				return respEntity;
			} else {
				return null;
			}
		}

		// 3.bearer 格式 (JWE/JWS)
		// 是否有"bearer "字樣,忽略大小寫
		boolean hasBearer = checkHasKeyword(authorization, TokenHelper.BEARER);
		if (hasBearer) {
			respEntity = verifyApiForBearer(authorization, apiId, moduleName, reqUri, httpReq);
			if (respEntity != null) {
				return respEntity;
			} else {
				return null;
			}
		}

		// 4.DGRK 格式
		// 是否有"DGRK "字樣,忽略大小寫
		boolean hasDgrk = checkHasKeyword(authorization, TokenHelper.DGRK);
		if (hasDgrk) {
			respEntity = verifyApiForDgrk(authorization, payload, apiId, moduleName, reqUri, httpReq);
			if (respEntity != null) {
				return respEntity;
			} else {
				return null;
			}
		}

		// 5.沒有以上等字樣
		String errMsg = TokenHelper.UNAUTHORIZED;
		TPILogger.tl.debug(errMsg);
		return getUnauthorizedErrorResp(reqUri, errMsg);
	}

	/**
	 * 驗證打 API 的 authorization, Basic 格式
	 */
	protected ResponseEntity<?> verifyApiForBasic(String authorization, String apiId, String moduleName, String apiUrl,
			HttpServletRequest httpReq) {
		// 1.取得 Basic Authorization 的 Client ID
		BasicAuthClientData basicAuthClientData = getAuthClientDataForBasic(authorization, apiUrl);
		ResponseEntity<?> respEntity = basicAuthClientData.errRespEntity;
		if (respEntity != null) {// 資料有錯誤
			return respEntity;
		}

		String[] cliendData = basicAuthClientData.cliendData;
		String clientId = cliendData[0];
		String clientPw = cliendData[1];

		clientPw = Base64Util.base64Encode(clientPw.getBytes());

		// 2.沒有 clientId, 或 client 狀態不正確
		respEntity = checkClientStatus(clientId, apiUrl);
		if (respEntity != null) {// 資料驗證有錯誤
			return respEntity;
		}

		// 3.沒有 clientId 或 clientPw, 查無 client 或 client 帳密不對
		respEntity = checkClientMima(clientId, clientPw, apiUrl);
		if (respEntity != null) {// 資料驗證有錯誤
			return respEntity;
		}

		// 4.檢查主機清單
		respEntity = checkHostList(clientId, apiUrl, httpReq);
		if (respEntity != null) {// 資料驗證有錯誤
			return respEntity;
		}

		// 5.檢查 client 用戶啟日/迄日 & 每日服務時間
		respEntity = checkClientStartEndDateAndServiceTime(clientId, apiUrl);
		if (respEntity != null) {// 資料驗證有錯誤
			return respEntity;
		}

		// 6.檢查 client 被授權的 grant type, 是否有 "client_credentials"
		String grantType = DgrTokenGrantType.CLIENT_CREDENTIALS; // basic 視等同 "client_credentials"
		respEntity = checkClientSupportGrantType(clientId, grantType, apiUrl);
		if (respEntity != null) {// 資料驗證有錯誤
			return respEntity;
		}

		// 7.檢查 client 的 scope(僅一般群組), 是否有權限打 API
		respEntity = checkClientGroupScope(apiId, moduleName, clientId, apiUrl);
		if (respEntity != null) {// 資料驗證有錯誤
			return respEntity;
		}

		// 8.檢查 client API可用量 和 打 API 成功後, API使用量 加1
		respEntity = checkClientApiQuota(clientId, apiUrl);
		if (respEntity != null) {// 資料驗證有錯誤
			return respEntity;
		}

		// 因為有些DB是不分大小寫,所以再去DB撈一次,來解決cid大小寫不一致,造成dashboard分組,不使用byId因為它不是根劇DB的大小寫回傳
		TsmpClient tsmpClient = getTsmpClientCacheProxy().findFirstByClientId(clientId);
		if (tsmpClient != null) {
			httpReq.setAttribute(TokenHelper.CLIENT_ID, tsmpClient.getClientId());
		} else {
			// 不應該會執行到這裡
			TPILogger.tl.error("client(application) input clientId");
			httpReq.setAttribute(TokenHelper.CLIENT_ID, clientId);
		}

		return null;
	}

	/**
	 * 驗證打 API 的 authorization, Bearer 格式 (token)
	 */
	public ResponseEntity<?> verifyApiForBearer(String authorization, String apiId, String moduleName, String apiUrl,
			HttpServletRequest httpReq) {
		try {
			String tokenStr = authorization.substring(TokenHelper.BEARER.length());

			// 1.由 token 取得資料, 驗證 JWS 簽章 或 JWE 解密
			JwtPayloadData jwtPayloadData = getJwtPayloadData(tokenStr);
			ResponseEntity<?> respEntity = jwtPayloadData.errRespEntity;
			if (respEntity != null) {// 資料有錯誤
				return respEntity;
			}
			JsonNode payloadJsonNode = jwtPayloadData.payloadJsonNode;

			// 2.是否 token exp 沒有值 或 token 過期
			Long exp = JsonNodeUtil.getNodeAsLong(payloadJsonNode, "exp");
			respEntity = checkAccessTokenExp(tokenStr, exp);
			if (respEntity != null) {// 資料有錯誤
				return respEntity;
			}

			String jti = JsonNodeUtil.getNodeAsText(payloadJsonNode, "jti");

			// 3.檢查 token 是否已撤銷
			boolean isSkipForMemory = false;// for in-memory
			if (DgrDeployRole.MEMORY.value().equalsIgnoreCase(getDeployRole())) {
				// 若角色為 Memory, 為 In-Memory GTW 流程
				// (1).先取DB資料判斷是否已撤銷
				respEntity = checkJtiHasValue(jti);
				if (respEntity != null) {// 資料有錯誤
					return respEntity;
				}

				TsmpTokenHistory tsmpTokenHistory = getTsmpTokenHistoryCacheProxy().findFirstByTokenJti(jti);

				// (2).若查無資料,則略過檢查狀態及 quota
				if (tsmpTokenHistory == null) {// 查無資料
					// Table [TSMP_TOKEN_HISTORY] 查不到資料
					TPILogger.tl.debug("Table [TSMP_TOKEN_HISTORY] can't find data, token_jti:" + jti);
					isSkipForMemory = true;
				}

			} else {
				// 用原本的方式檢查是否已撤銷
				respEntity = checkAccessTokenRevoked(jti);
				if (respEntity != null) {// 資料有錯誤
					return respEntity;
				}
			}

			String clientId = JsonNodeUtil.getNodeAsText(payloadJsonNode, "client_id");
			String idpType = JsonNodeUtil.getNodeAsText(payloadJsonNode, "idp_type");
			String userName = JsonNodeUtil.getNodeAsText(payloadJsonNode, "user_name");
			String orgId = JsonNodeUtil.getNodeAsText(payloadJsonNode, "org_id");

			if (TokenHelper.SSOTOKEN.equals(apiId)) {
				// 若為 dgR 特殊 API,不檢查
				// 例如: /dgrv4/ssotoken/gtwidp/v2/userInfo

			} else {

				// 4.檢查 client 狀態
				respEntity = checkClientStatus(clientId, apiUrl);
				if (respEntity != null) {// 資料有錯誤
					return respEntity;
				}

				// 5.檢查主機清單
				respEntity = checkHostList(clientId, apiUrl, httpReq);
				if (respEntity != null) {// 資料驗證有錯誤
					return respEntity;
				}

				// 6.檢查 client 用戶啟日/迄日 & 每日服務時間
				respEntity = checkClientStartEndDateAndServiceTime(clientId, apiUrl);
				if (respEntity != null) {// 資料驗證有錯誤
					return respEntity;
				}

				// 7.檢查 user 狀態
				if (StringUtils.hasLength(idpType)) {// 當 grant_type 為 "delegate_auth",會有 idp_type
					// 不檢查IdP user狀態,以減少DB存取
				} else {
					respEntity = checkUserStatus(userName, apiUrl);
					if (respEntity != null) {// 資料有錯誤
						return respEntity;
					}
				}

				// 8.檢查 access token 的 scope 是否有權限打 API
				JsonNode scopeArray = payloadJsonNode.get("scope");// 取得 token 的 scope(group id)
				List<String> tokenScopeList = JsonNodeUtil.convertJsonArrayToList(scopeArray);
				respEntity = checkTokenScope(apiId, moduleName, tokenScopeList, apiUrl);
				if (respEntity != null) {// 資料有錯誤
					return respEntity;
				}

				// 9.檢查 client API 可用量 和 打 API 成功後, API 使用量加1
				respEntity = checkClientApiQuota(clientId, apiUrl);
				if (respEntity != null) {// 資料驗證有錯誤
					return respEntity;
				}

				// 10.檢查 access token 可用量 和 打 API 成功後, access token 使用量加1
				if (isSkipForMemory) {// 查無資料,則略過檢查狀態及 quota
					// 不檢查可用量,
					// 但當角色為 Memory, 先用 Map 儲存 token 的使用量
					addTokenUsedMap4InMemory(jti);
				} else {
					respEntity = checkClientAccessTokenQuota(jti, apiUrl);
					if (respEntity != null) {// 資料驗證有錯誤
						return respEntity;
					}
				}
			}

			// 寫入參數,做為後面寫log時使用
			// 因為有些DB是不分大小寫,所以再去DB撈一次,來解決cid大小寫不一致,造成dashboard分組,不使用byId因為它不是根劇DB的大小寫回傳
			TsmpClient tsmpClient = getTsmpClientCacheProxy().findFirstByClientId(clientId);
			if (tsmpClient != null) {
				httpReq.setAttribute(TokenHelper.CLIENT_ID, tsmpClient.getClientId());
			} else {
				// 不應該會執行到這裡
				TPILogger.tl.error("client(application) input clientId");
				httpReq.setAttribute(TokenHelper.CLIENT_ID, clientId);
			}

			httpReq.setAttribute(TokenHelper.USER_NAME, userName);
			httpReq.setAttribute(TokenHelper.ORG_ID, orgId);
			httpReq.setAttribute(TokenHelper.JTI, jti);
			return null;

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			String errMsg = TokenHelper.INTERNAL_SERVER_ERROR;
			TPILogger.tl.error(errMsg);
			return getInternalServerErrorResp(apiUrl, errMsg);// 500
		}
	}

	/**
	 * for In-Memory GTW 流程, <br>
	 * 當角色為 Memory, 先用 Map 儲存 token 的使用量, <br>
	 * 當調用 refreshGTW API 時一起送到 Landing 加到總使用量 <br>
	 */
	private void addTokenUsedMap4InMemory(String jti) {
		if (DgrDeployRole.MEMORY.value().equalsIgnoreCase(getDeployRole())) {
			// 若角色為 Memory, 用 Map 儲存加 1 的數量(使用量)
			Long used = TPILogger.tokenUsedMap.get(jti);
			if (used == null) {
				TPILogger.tokenUsedMap.put(jti, 1L);
			} else {
				used += 1;
				TPILogger.tokenUsedMap.put(jti, used);
			}
		}
	}

	/**
	 * for In-Memory GTW 流程, <br>
	 * 當角色為 Memory, 先用 Map 儲存 API 的使用量, <br>
	 * 當調用 refreshGTW API 時一起送到 Landing 加到總使用量 <br>
	 */
	private void addApiUsedMap4InMemory(String clientId) {
		if (DgrDeployRole.MEMORY.value().equalsIgnoreCase(getDeployRole())) {
			// 若角色為 Memory, 用 Map 儲存加 1 的數量(使用量)
			Integer used = TPILogger.apiUsedMap.get(clientId);
			if (used == null) {
				TPILogger.apiUsedMap.put(clientId, 1);
			} else {
				used += 1;
				TPILogger.apiUsedMap.put(clientId, used);
			}
		}
	}

	/**
	 * 驗證打 API 的 authorization, DGRK 格式
	 */
	protected ResponseEntity<?> verifyApiForDgrk(String authorization, String payload, String apiId, String moduleName,
			String apiUrl, HttpServletRequest httpReq) {
		if (payload == null) {
			payload = "";
		}

		try {
			// 1.取得 DGRK Authorization 的資料
			DgrkAuthData dgrkAuthData = getAuthDataForDgrk(authorization, apiUrl);
			ResponseEntity<?> respEntity = dgrkAuthData.errRespEntity;
			if (respEntity != null) {
				return respEntity;
			}

			String[] authData = dgrkAuthData.authData;
			String openApiKey = authData[0];
			String signature = authData[1];

			// 2.找不到符合的 API Key, 因為有 quota 的問題,所以先維持原樣用 Dao
			TsmpOpenApiKey tsmpOpenApiKey = getTsmpOpenApiKeyDao().findFirstByOpenApiKey(openApiKey);
			if (tsmpOpenApiKey == null) {
				String errMsg = "API Key Not Found";
				TPILogger.tl.debug(errMsg);
				return getUnauthorizedErrorResp(apiUrl, errMsg);// 401
			}

			String clientId = tsmpOpenApiKey.getClientId();
			Long openApikeyId = tsmpOpenApiKey.getOpenApiKeyId();// API Key ID
			String secretKey = tsmpOpenApiKey.getSecretKey();// Secret KEY
			String status = tsmpOpenApiKey.getOpenApiKeyStatus();// API Key 狀態,1：啟用，0：停用 （預設啟用）
			Long revokedAt = tsmpOpenApiKey.getRevokedAt();// API Key 撤銷時間
			Long expiredAt = tsmpOpenApiKey.getExpiredAt();// API Key 效期, 亳秒
			int timesQuota = tsmpOpenApiKey.getTimesQuota();// 可使用次數,1.若使用次數上限為10時,當已使用1次,則此欄位為9;當已使用2次,則此欄位為8;依此類推
															// 2. 若值為 -1, 則 API Key 無使用次數限制

			// 3.檢查 client 狀態
			ResponseEntity<?> checkClientResp = checkClientStatus(clientId, apiUrl);
			if (checkClientResp != null) {// 資料驗證有錯誤
				return checkClientResp;
			}

			// 4.檢查主機清單
			respEntity = checkHostList(clientId, apiUrl, httpReq);
			if (respEntity != null) {// 資料驗證有錯誤
				return respEntity;
			}

			// 5.檢查 client 用戶啟日/迄日 & 每日服務時間
			respEntity = checkClientStartEndDateAndServiceTime(clientId, apiUrl);
			if (respEntity != null) {// 資料驗證有錯誤
				return respEntity;
			}

			// 6.驗證 signature
			boolean isPass = OpenApiKeyUtil.verifyDgrkSignature(payload, openApiKey, secretKey, signature);
			if (!isPass) {
				String errMsg = "API Key Signature Incorrect";
				TPILogger.tl.debug(errMsg);
				return getUnauthorizedErrorResp(apiUrl, errMsg);// 401
			}

			// 7.API Key 已停用
			if ("0".equals(status)) {
				String errMsg = "API Key Status is Deactived";
				TPILogger.tl.debug(errMsg);
				return getUnauthorizedErrorResp(apiUrl, errMsg);// 401
			}

			long nowTime = System.currentTimeMillis();// 亳秒

			// 8.API Key 已撤銷
			if (revokedAt != null) {
				if (revokedAt < nowTime) {// 撤銷
					Date revokedDate = new Date(revokedAt);
					String revokedStr = DateTimeUtil.dateTimeToString(revokedDate, DateTimeFormatEnum.西元年月日時分秒毫秒_2)
							.orElse(null);
					String errMsg = "API Key Has Revoked, revoked at: " + revokedStr;
					TPILogger.tl.debug(errMsg);
					return getUnauthorizedErrorResp(apiUrl, errMsg);// 401
				}
			}

			// 9.API Key 已過期
			if (expiredAt < nowTime) {// 過期
				Date expireDate = new Date(expiredAt);
				String expiredStr = DateTimeUtil.dateTimeToString(expireDate, DateTimeFormatEnum.西元年月日時分秒毫秒_2)
						.orElse(null);
				String errMsg = "API Key Has Expired, expired at: " + expiredStr;
				TPILogger.tl.debug(errMsg);
				return getUnauthorizedErrorResp(apiUrl, errMsg);// 401
			}

			// 10.API Key 超過次數限制
			if (timesQuota == 0) {
				String errMsg = "API Key Times Quota is 0";
				TPILogger.tl.debug(errMsg);
				return getUnauthorizedErrorResp(apiUrl, errMsg);// 401
			}

			// 11.檢查API Key 是否有取用此 API 的權限
			respEntity = checkOpenApiKeyPermission(apiId, moduleName, openApikeyId, apiUrl);
			if (respEntity != null) {
				return respEntity;
			}

			// 12.打 API 成功後, API Key 的 timesQuota 要減1
			if (timesQuota > 0) {
				timesQuota = timesQuota - 1;
				tsmpOpenApiKey.setTimesQuota(timesQuota);
				getTsmpOpenApiKeyDao().saveAndFlush(tsmpOpenApiKey);
			}

			httpReq.setAttribute(TokenHelper.CLIENT_ID, clientId);
			return null;

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			String errMsg = TokenHelper.INTERNAL_SERVER_ERROR;
			TPILogger.tl.error(errMsg);
			return getInternalServerErrorResp(apiUrl, errMsg);// 500
		}
	}

	/**
	 * 檢查使用的 API Key 是否有取用此 API 的權限
	 */
	protected ResponseEntity<?> checkOpenApiKeyPermission(String apiId, String moduleName, Long openApikeyId,
			String apiUrl) {
		List<TsmpOpenApiKeyMap> tsmpOpenApiKeyMapList = getTsmpOpenApiKeyMapCacheProxy()
				.findByRefOpenApiKeyId(openApikeyId);
		List<String> apiUidList = new ArrayList<>();
		for (TsmpOpenApiKeyMap tsmpOpenApiKeyMap : tsmpOpenApiKeyMapList) {
			String apiUid = tsmpOpenApiKeyMap.getRefApiUid();
			if (StringUtils.hasLength(apiUid)) {
				apiUidList.add(apiUid);
			}
		}

		TsmpApi tsmpApi = getTsmpApiCacheProxy().findByModuleNameAndApiKey(moduleName, apiId);
		String apiUid = tsmpApi.getApiUid();

		boolean isCanCallApi = apiUidList.contains(apiUid);
		// 使用的 API Key 無取用此 API 權限
		if (!isCanCallApi) {
			String errMsg = "Client Permission Denied, Api Key Deny";
			String errLog = errMsg + "\n" + "API Key apiUidList:" + apiUidList.toString() + "\n" + "API apiUid:"
					+ apiUid;
			TPILogger.tl.debug(errLog);
			return new ResponseEntity<>(
					getOAuthTokenErrorResp(TokenHelper.FORBIDDEN, errMsg, HttpStatus.FORBIDDEN.value(), apiUrl),
					setContentTypeHeader(), HttpStatus.FORBIDDEN);// 403
		}

		return null;
	}

	/**
	 * 檢查 access token 的 scope 是否有權限打 API
	 */
	protected ResponseEntity<?> checkTokenScope(String apiId, String moduleName, List<String> tokenScopeList,
			String apiUrl) {
		// 取得此 API 被授權的 scope(group id)
		List<String> apiScopeList = getApiScopeList(apiId, moduleName);

		List<String> tokenScopeList2 = new ArrayList<>();
		tokenScopeList2.addAll(tokenScopeList);

		tokenScopeList2.retainAll(apiScopeList);// 交集
		if (tokenScopeList2.size() == 0) {
			String errMsg = "Token Violates Scope Authorization Access Settings";
			String errLog = errMsg + "\n" + "Token ScopeList:" + tokenScopeList.toString() + "\n" + "Api ScopeList:"
					+ apiScopeList.toString();
			TPILogger.tl.debug(errLog);
			return new ResponseEntity<>(
					getOAuthTokenErrorResp(TokenHelper.FORBIDDEN, errMsg, HttpStatus.FORBIDDEN.value(), apiUrl),
					setContentTypeHeader(), HttpStatus.FORBIDDEN);// 403
		}

		return null;
	}

	/**
	 * for Basic Auth <br>
	 * 檢查 client 的 scope(僅一般群組) 是否有權限打 API
	 */
	protected ResponseEntity<?> checkClientGroupScope(String apiId, String moduleName, String clientId, String apiUrl) {
		// 取得 client 的 scope(group id),僅一般群組
		List<String> clientScopeList = new ArrayList<>();
		clientScopeList = getClientGroupScopeList(clientId, clientScopeList);

		// 取得此 API 被授權的 scope(group id)
		List<String> apiScopeList = getApiScopeList(apiId, moduleName);

		List<String> clientScopeList2 = new ArrayList<>();
		clientScopeList2.addAll(clientScopeList);

		clientScopeList2.retainAll(apiScopeList);// 交集
		if (clientScopeList2.size() == 0) {
			String errMsg = "Token Violates Scope Authorization Access Settings";
			String errLog = errMsg + "\n" + "Client Id: " + clientId + "\n" + "Client ScopeList: "
					+ clientScopeList.toString() + "\n" + "Api ScopeList: " + apiScopeList.toString();
			TPILogger.tl.debug(errLog);
			return new ResponseEntity<>(
					getOAuthTokenErrorResp(TokenHelper.FORBIDDEN, errMsg, HttpStatus.FORBIDDEN.value(), apiUrl),
					setContentTypeHeader(), HttpStatus.FORBIDDEN);// 403
		}

		return null;
	}

	/**
	 * 取得 client 的 scope(group id),僅一般群組
	 */
	public List<String> getClientGroupScopeList(String clientId, List<String> scopeList) {
		// scope 種類, client 一般群組 id
		List<TsmpClientGroup> tsmpClientGroupList = getTsmpClientGroupCacheProxy().findByClientId(clientId);
		if (!CollectionUtils.isEmpty(tsmpClientGroupList)) {
			for (TsmpClientGroup tsmpClientGroup : tsmpClientGroupList) {
				// 一般的 group, vgroup_flag = '0'
				TsmpGroup tsmpGroup = getTsmpGroupCacheProxy()
						.findFirstByGroupIdAndVgroupFlag(tsmpClientGroup.getGroupId(), "0");
				if (tsmpGroup != null) {
					scopeList.add(tsmpGroup.getGroupId());
				}
			}
		}
		return scopeList;
	}

	/**
	 * 取得此 API 被授權的 scope(group id)
	 */
	private List<String> getApiScopeList(String apiId, String moduleName) {
		List<String> apiScopeList = new ArrayList<>();

		List<TsmpGroupApi> tsmpGroupApiList = getTsmpGroupApiCacheProxy().findByApiKeyAndModuleName(apiId, moduleName);
		for (TsmpGroupApi tsmpGroupApi : tsmpGroupApiList) {
			String apiGroupId = tsmpGroupApi.getGroupId();
			if (StringUtils.hasLength(apiGroupId)) {
				apiScopeList.add(apiGroupId);
			}
		}

		return apiScopeList;
	}

	/**
	 * 檢查支援的 grant type
	 */
	public ResponseEntity<?> checkSupportGrantType(String grantType) {
		List<String> supportGrantType = new ArrayList<>();// 支援的 grant type
		supportGrantType.add(DgrTokenGrantType.PASSWORD);
		supportGrantType.add(DgrTokenGrantType.CLIENT_CREDENTIALS);
		supportGrantType.add(DgrTokenGrantType.REFRESH_TOKEN);
		supportGrantType.add(DgrTokenGrantType.AUTHORIZATION_CODE);

		boolean isSupport = supportGrantType.contains(grantType.toLowerCase());// 轉小寫,再比較(忽略大小寫)
		if (!isSupport) {
			String errMsg = "Unsupported grant type: " + grantType;// 沒有支援此 grant type
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<>(getOAuthTokenErrorResp2("unsupported_grant_type", errMsg),
					setContentTypeHeader(), HttpStatus.BAD_REQUEST);// 400
		}

		return null;
	}

	/**
	 * 檢查 client 被授權的 grant type
	 */
	public ResponseEntity<?> checkClientSupportGrantType(String clientId, String grantType, String apiUrl) {

		Optional<OauthClientDetails> opt_authClientDetails = getOauthClientDetailsCacheProxy().findById(clientId);
		if (!opt_authClientDetails.isPresent()) {
			return getFindOauthClientDetailsError(clientId, apiUrl);
		}

		OauthClientDetails authClientDetails = opt_authClientDetails.get();
		String authGrantTypes = authClientDetails.getAuthorizedGrantTypes();
		authGrantTypes = authGrantTypes.toLowerCase();// 轉小寫,再比較(忽略大小寫)
		List<String> authGrantTypesList = Arrays.asList(authGrantTypes.split(","));// client 被授權的 grant type

		// client 沒有被授權此 grantType
		boolean isAuthorize = authGrantTypesList.contains(grantType.toLowerCase());// 轉小寫,再比較(忽略大小寫)
		if (!isAuthorize) {
			// client 沒有被授權此 grant type
			String errMsg = "Client(application) is not authorized for this grant type '" + grantType + "', "
					+ "clientId: " + clientId;
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<>(
					getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, "Unauthorized grant type: " + grantType),
					setContentTypeHeader(), HttpStatus.BAD_REQUEST);// 400
		}
		return null;
	}

	/**
	 * 檢查 client API可用量 和 打 API 成功後, API使用量 加1
	 */
	protected ResponseEntity<?> checkClientApiQuota(String clientId, String reqUri) {
		// --- 使用 Cache ---
		// 查無 client
		// 因為最終要用findFirstByClientId的cache機制,所以沒用findById
		TsmpClient cache_tsmpClient = getTsmpClientCacheProxy().findFirstByClientId(clientId);
		if (cache_tsmpClient == null) {
			return getFindTsmpClientError(clientId, reqUri);
		}

		Integer apiQuota = cache_tsmpClient.getApiQuota();
		// 1.若 api_quota(API可用量)為 null 或 0, 為不限次數, 不檢查 且 api_used(API使用量)不加1
		if (apiQuota == null || apiQuota == 0) {
			return null;
		}

		// --- 使用 Dao ---
		Optional<TsmpClient> opt_client = getTsmpClientDao().findById(clientId);
		if (opt_client.isEmpty()) {
			return getFindTsmpClientError(clientId, reqUri);
		}

		TsmpClient tsmpClient = opt_client.get();
		apiQuota = tsmpClient.getApiQuota();
		Integer apiUsed = tsmpClient.getApiUsed() == null ? 0 : tsmpClient.getApiUsed();

		// 2.若 api_used(API使用量) >= api_quota(API可用量)
		if (apiUsed >= apiQuota) {
			String errMsg = "Client(application) quota ran out";// client 配額用完
			String errMsg2 = errMsg + ", client_id: " + clientId;
			TPILogger.tl.debug(errMsg2);
			return new ResponseEntity<>(
					getOAuthTokenErrorResp(errMsg, errMsg2, HttpStatus.FORBIDDEN.value(), reqUri),
					setContentTypeHeader(), HttpStatus.FORBIDDEN);// 403
		}

		// 3.每打一次 API, api_used(API使用量) 加1
		apiUsed += 1;
		tsmpClient.setApiUsed(apiUsed);
		tsmpClient.setUpdateTime(DateTimeUtil.now());
		tsmpClient.setUpdateUser("SYSTEM apiUsed");
		getTsmpClientDao().saveAndFlush(tsmpClient);

		// 當角色為 Memory, 先用 Map 儲存 API 使用量
		addApiUsedMap4InMemory(clientId);

		return null;
	}

	/**
	 * 檢查 client access token 可用量 和 打 API 成功後, access token 使用量 加1
	 */
	protected ResponseEntity<?> checkClientAccessTokenQuota(String jti, String apiUrl) {
		// --- 使用 Cache ---
		// 1.查詢 TSMP_TOKEN_HISTORY, 此 access token 的記錄
		TsmpTokenHistory cache_tsmpTokenHistory = getTsmpTokenHistoryCacheProxy().findFirstByTokenJti(jti);
		if (cache_tsmpTokenHistory == null) {// 查無資料
			// Table [TSMP_TOKEN_HISTORY] 查不到資料
			TPILogger.tl.debug("Table [TSMP_TOKEN_HISTORY] can't find data, token_jti:" + jti);
			// access_token 已撤銷
			ResponseEntity<?> respEntity = getTokenRevokedError(jti);// 403
			return respEntity;
		}

		Long tokenQuota = cache_tsmpTokenHistory.getTokenQuota() == null ? 0 : cache_tsmpTokenHistory.getTokenQuota();
		Long tokenUsed = cache_tsmpTokenHistory.getTokenUsed() == null ? 0 : cache_tsmpTokenHistory.getTokenUsed();

		// 2.若 token_quota(可用量) 為 null 或 0, 則為不限次數, 不檢查
		// 且 token_used(使用量) 不加1
		if (tokenQuota == null || tokenQuota == 0) {
			return null;
		}

		// --- 使用 Dao ---
		TsmpTokenHistory tsmpTokenHistory = getTsmpTokenHistoryDao().findFirstByTokenJti(jti);
		tokenQuota = tsmpTokenHistory.getTokenQuota() == null ? 0 : tsmpTokenHistory.getTokenQuota();
		tokenUsed = tsmpTokenHistory.getTokenUsed() == null ? 0 : tsmpTokenHistory.getTokenUsed();

		// 3.若 token_used(使用量) >= token_quota(可用量), access token 額度已滿不可使用
		if (tokenUsed >= tokenQuota) {
			String errMsg = "Over Access Token Allow Times";// 超過 access token 允許次數
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<>(getOAuthTokenErrorResp2(errMsg, errMsg), setContentTypeHeader(),
					HttpStatus.FORBIDDEN);// 403
		}

		// 4.每打一次 API, access token使用量 + 1, 更新 TSMP_TOKEN_HISTORY
		tokenUsed += 1;
		tsmpTokenHistory.setTokenUsed(tokenUsed);
		tsmpTokenHistory = getTsmpTokenHistoryDao().saveAndFlush(tsmpTokenHistory);

		// 當角色為 Memory, 先用 Map 儲存 token 的使用量
		addTokenUsedMap4InMemory(jti);

		return null;
	}

	/**
	 * 是否為 AC IdP 流程, <br>
	 * 若有 idP type 且 userName 開頭為 "b64."字樣, 則為 AC IdP 流程 <br>
	 */
	public static boolean isAcIdPFlow(String userName, String idPType) {
		if (!StringUtils.hasLength(userName)) {
			return false;
		}

		if (!StringUtils.hasLength(idPType)) {
			return false;
		}

		if (userName.indexOf("b64.") == 0) {// userName 開頭為 "b64."字樣
			return true;
		}

		return false;
	}

	/**
	 * 是否為 GTW IdP 流程, <br>
	 * 若有 idP type 且 userName 開頭不是 "b64."字樣, 則為 GTW IdP 流程 <br>
	 */
	public static boolean isGtwIdPFlow(String userName, String idPType) {
		if (!StringUtils.hasLength(userName)) {
			return false;
		}

		if (!StringUtils.hasLength(idPType)) {
			return false;
		}

		if (userName.indexOf("b64.") != 0) {// userName 開頭不是 "b64."字樣
			return true;
		}

		return false;
	}

	public ResponseEntity<?> getFindTsmpClientError(String clientId, String apiUrl) {
		// Table [TSMP_CLIENT] 查不到 client
		String errMsg1 = "Table [TSMP_CLIENT] can't find client(application), client_id: " + clientId;
		String errMsg2 = THE_CLIENT_WAS_NOT_FOUND + clientId;
		TPILogger.tl.debug(errMsg1 + ",\n" + errMsg2);
		return getUnauthorizedErrorResp(apiUrl, errMsg2);// 401
	}

	public ResponseEntity<?> getFindOauthClientDetailsError(String clientId, String apiUrl) {
		// Table [OAUTH_CLIENT_DETAILS] 查不到 client
		String errMsg1 = "Table [OAUTH_CLIENT_DETAILS] can't find client(application), client_id: " + clientId;
		String errMsg2 = THE_CLIENT_WAS_NOT_FOUND + clientId;
		TPILogger.tl.debug(errMsg1 + ",\n" + errMsg2);
		return getUnauthorizedErrorResp(apiUrl, errMsg2);// 401
	}

	public ResponseEntity<?> getFindTsmpTokenHistoryError(String accessTokenJti, String idPType, String apiUrl) {
		// Table [TSMP_TOKEN_HISTORY] 查不到資料
		String errMsg1 = "Table [TSMP_TOKEN_HISTORY] can't find. token_jti: " + accessTokenJti + ", idp_type" + idPType;
		String errMsg2 = String.format(THE_JTI_WAS_NOT_FOUND, accessTokenJti, idPType);
		TPILogger.tl.debug(errMsg1 + ",\n" + errMsg2);
		return getUnauthorizedErrorResp(apiUrl, errMsg2);// 401
	}

	public ResponseEntity<?> getFindTsmpTokenHistoryError(String accessTokenJti, String apiUrl) {
		// Table [TSMP_TOKEN_HISTORY] 查不到資料
		String errMsg1 = "Table [TSMP_TOKEN_HISTORY] can't find. token_jti: " + accessTokenJti;
		String errMsg2 = String.format(THE_JTI_WAS_NOT_FOUND2, accessTokenJti);
		TPILogger.tl.debug(errMsg1 + ",\n" + errMsg2);
		return getUnauthorizedErrorResp(apiUrl, errMsg2);// 401
	}

	public ResponseEntity<?> getBadRequestErrorResp(String reqUri, String errMsg1, String errMsg2) {
		return new ResponseEntity<>(getOAuthTokenErrorResp2(errMsg1, errMsg2), HttpStatus.BAD_REQUEST);// 400
	}

	public ResponseEntity<?> getUnauthorizedErrorResp(String apiUrl, String errMsg) {
		return new ResponseEntity<>(
				getOAuthTokenErrorResp(TokenHelper.UNAUTHORIZED, errMsg, HttpStatus.UNAUTHORIZED.value(), apiUrl),
				setContentTypeHeader(), HttpStatus.UNAUTHORIZED);// 401
	}

	public ResponseEntity<?> getForbiddenErrorResp(String reqUri, String errMsg) {
		return new ResponseEntity<>(getOAuthTokenErrorResp2(TokenHelper.FORBIDDEN, errMsg), HttpStatus.FORBIDDEN);// 403
	}

	public ResponseEntity<?> getInternalServerErrorResp(String apiUrl, String errMsg) {
		return new ResponseEntity<>(
				getOAuthTokenErrorResp(errMsg, errMsg, HttpStatus.INTERNAL_SERVER_ERROR.value(), apiUrl),
				setContentTypeHeader(), HttpStatus.INTERNAL_SERVER_ERROR);// 500
	}

	public ResponseEntity<?> getTokenFormatError() {
		// token 格式不對
		String errMsg = TokenHelper.CANNOT_CONVERT_ACCESS_TOKEN_TO_JSON;
		TPILogger.tl.debug(errMsg);
		return new ResponseEntity<>(getOAuthTokenErrorResp2(TokenHelper.INVALID_TOKEN, errMsg), setContentTypeHeader(),
				HttpStatus.BAD_REQUEST);// 400
	}

	public ResponseEntity<?> getApiJwsBodyFormatError() {
		// API JWS request body 格式不對
		String errMsg = "API JWS Request_body Required";
		TPILogger.tl.debug(errMsg);
		return new ResponseEntity<>(getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
				setContentTypeHeader(), HttpStatus.BAD_REQUEST);// 400
	}

	public ResponseEntity<?> getApiJweBodyFormatError() {
		// API JWE request body 格式不對
		String errMsg = "JWE Cannot Be Resolved";
		TPILogger.tl.debug(errMsg);
		return new ResponseEntity<>(getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
				setContentTypeHeader(), HttpStatus.BAD_REQUEST);// 400
	}

	public ResponseEntity<?> getApiBodyFormatError() {
		// API request body 格式不對
		String errMsg = "API request body is incorrect";
		TPILogger.tl.debug(errMsg);
		return new ResponseEntity<>(getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
				setContentTypeHeader(), HttpStatus.BAD_REQUEST);// 400
	}

	public ResponseEntity<?> getTokenRevokedError(String jti) {
		// access_token 已撤銷
		String errMsg = "Access token revoked, jti: " + jti;
		TPILogger.tl.debug(errMsg);
		return new ResponseEntity<>(getOAuthTokenErrorResp2(TokenHelper.INVALID_TOKEN, errMsg),
				setContentTypeHeader(), HttpStatus.FORBIDDEN);// 403
	}

	public ResponseEntity<?> getRefreshTokenRevokedError(String jti) {
		// refresh token 已撤銷
		String errMsg = "Refresh token revoked, jti: " + jti;
		TPILogger.tl.debug(errMsg);
		return new ResponseEntity<>(getOAuthTokenErrorResp2(TokenHelper.INVALID_TOKEN, errMsg),
				setContentTypeHeader(), HttpStatus.FORBIDDEN);// 403
	}

	public OAuthTokenErrorResp getOAuthTokenErrorResp(String error, String message, int httpCode, String apiUrl) {
		OAuthTokenErrorResp resp = new OAuthTokenErrorResp();
		resp.setTimestamp(System.currentTimeMillis() + "");
		resp.setStatus(httpCode);
		resp.setError(error);
		resp.setMessage(message);
		resp.setPath(apiUrl);

		try {
			String stackLog = StackTraceUtil
					.logTpiShortStackTrace(new Throwable(new ObjectMapper().writeValueAsString(resp)));
			if (stackLog != null) {
				TPILogger.tl.debug(stackLog);

				/*
				 * 判斷訊息內是否有"GatewayFilter.getCID("的字樣, 若有,表示由GatewayFilter.getCID()出現的錯誤,不印訊息;
				 * 否則,印出訊息
				 */
//				String word = GatewayFilter.class.getSimpleName() + ".getCID(";// 例如: "GatewayFilter.getCID("
//				int index = stackLog.indexOf(word);
//				if (index == -1) {// 沒有字樣,印出訊息
//					TPILogger.tl.debug(stackLog);
//				}
			}
		} catch (JsonProcessingException e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}

		return resp;
	}

	public OAuthTokenErrorResp2 getOAuthTokenErrorResp2(String error, String errDes) {
		OAuthTokenErrorResp2 resp = new OAuthTokenErrorResp2();
		resp.setError(error);
		resp.setErrorDescription(errDes);

		try {
			TPILogger.tl.debug(
					StackTraceUtil.logTpiShortStackTrace(new Throwable(new ObjectMapper().writeValueAsString(resp))));
		} catch (JsonProcessingException e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}

		return resp;
	}

	/**
	 * 取得 OAuthTokenErrorResp 或 OAuthTokenErrorResp2 的 錯誤訊息
	 */
	public String getErrMsgForRespEntity(ResponseEntity<?> errRespEntity) {
		if (errRespEntity == null) {
			return null;
		}

		Object obj = errRespEntity.getBody();
		String msg = null;
		if (obj instanceof OAuthTokenErrorResp) {
			OAuthTokenErrorResp resp = (OAuthTokenErrorResp) obj;
			msg = resp.getMessage();

		} else if (obj instanceof OAuthTokenErrorResp2) {
			OAuthTokenErrorResp2 resp = (OAuthTokenErrorResp2) obj;
			msg = resp.getErrorDescription();
		}

		return msg;
	}

	public String getLocalBaseUrl(HttpServletRequest httpReq) {
		String host = getServiceConfigVal("api.host");
		if (!StringUtils.hasLength(host)) {
			return null;
		}
		String scheme = httpReq.getScheme();
		String localBaseUrl = scheme + "://" + host;

		return localBaseUrl;
	}

	private String getServiceConfigVal(String key) {
		String val = getServiceConfig().get(key);
		if (!StringUtils.hasLength(val)) {
			TPILogger.tl.error(TokenHelper.THE_PROFILE_IS_MISSING_PARAMETERS + key);
			return null;
		}
		val = val.trim();
		return val;
	}

	/**
	 * 建立 cookie
	 */
	public static ResponseCookie createCookie(String cookieName, String cookieValue, long maxAge) {
		ResponseCookie cookie = ResponseCookie.from(cookieName, cookieValue) // key & value
				.maxAge(maxAge) // 以秒為單位
				.path("/").httpOnly(true) // 禁止 JavaScript 存取 cookie, 防止 XSS Attack (Cross-Site Scripting，跨站腳本攻擊)
				.secure(true) // 讓 cookie 只能透過 https 傳遞, 即只有 HTTPS 才能讀與寫
				.sameSite(TokenHelper.getInstance().samesiteValue) // 防止 CSRF Attack (Cross-site request forgery，跨站請求偽造)
				.build();

		return cookie;
	}

	/**
	 * 取得 client 在 digiRunner 設定的重新導向URI資料
	 */
	private List<String> getWebServerRedirectUriList(OauthClientDetails authClientDetails) {
		List<String> list = new ArrayList<>();
		list = getWebServerRedirectUriList(list, authClientDetails.getWebServerRedirectUri());
		list = getWebServerRedirectUriList(list, authClientDetails.getWebServerRedirectUri1());
		list = getWebServerRedirectUriList(list, authClientDetails.getWebServerRedirectUri2());
		list = getWebServerRedirectUriList(list, authClientDetails.getWebServerRedirectUri3());
		list = getWebServerRedirectUriList(list, authClientDetails.getWebServerRedirectUri4());
		list = getWebServerRedirectUriList(list, authClientDetails.getWebServerRedirectUri5());

		return list;
	}

	private List<String> getWebServerRedirectUriList(List<String> list, String webServerRedirectUri) {
		// 有值才放入 list
		if (StringUtils.hasLength(webServerRedirectUri)) {
			list.add(webServerRedirectUri);
		}

		return list;
	}

	/**
	 * 為了postman 沒有帶 APPLICATION_JSON
	 * 
	 * @return
	 */
	private MultiValueMap<String, String> setContentTypeHeader() {
		// 為了postman 沒有帶 APPLICATION_JSON
		MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
		header.put("Content-Type", Arrays.asList(MediaType.APPLICATION_JSON.toString()));
		return header;
	}
}
