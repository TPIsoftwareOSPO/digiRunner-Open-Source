package tpi.dgrv4.gateway.component.authorization;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Map;

import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.component.cipher.TsmpCoreTokenInitializer;
import tpi.dgrv4.gateway.component.JwtJweAccessTokenConverter;
import tpi.dgrv4.gateway.component.StaticResourceInitializer;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

/**
 * 2021/05/07 把 {@link TsmpCoreTokenInitializer} 一部分的邏輯搬過來，自己處理取 KeyPair 的動作，就不用依賴 Spring 注入該類別了
 * @author Kim
 *
 */
public class TsmpAuthorizationBearerParser extends TsmpAuthorizationParserImpl {

	/**
	 * Injected by {@link StaticResourceInitializer}
	 */
	private static Environment env;

	private static final String KEY_PATH = "digiRunner.token.key-store.path";

	private static final String KEY_NAME = "digiRunner.token.key-store.name";

	private static final String KEY_PWD = "digiRunner.token.key-store-password";

	private static final String KEY_TYPE = "digiRunner.token.keyStoreType";

	private static final String KEY_ALIAS = "digiRunner.token.keyAlias";

	private static TPILogger logger;

	public TsmpAuthorizationBearerParser(String authorization) {
		super(authorization);
	}

	@Override
	public TsmpAuthorization parse() throws Exception {
		TsmpAuthorization auth = new TsmpAuthorization();
		// JWS
		if (this.infos.length == 3) {
			ObjectMapper om = new ObjectMapper();
			om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);//忽略無法識別的字段
			
			// part 1 ["alg", "typ"]
			byte[] decodedBytes = decodeInformation(this.infos[0]);
			String data = new String(decodedBytes);
			auth = om.readValue(data, TsmpAuthorization.class);

			// part 2 ["node", "aud", "user_id", "user_name", "scope", "exp", "authorities", "jti", "client_id"]
			decodedBytes = decodeInformation(this.infos[1]);
			data = new String(decodedBytes);
			auth = om.readValue(data, TsmpAuthorization.class);
			
			// part 3 ["token_string"]
			auth.setTokenString(this.infos[2]);
		// 2021/05/07 增加 JWE Token 解析
		} else if (this.infos.length == 5) {
			// Decrypt the JWE with the RSA private key
			String token = StringUtils.arrayToDelimitedString(this.infos, ".");
			
			KeyPair keyPair = loadAndExtractKeyPair();
			
			//移除spring-security相關
			// 建構子的參數 跟 setKeyPair 是為了分別塞到子類與父類各別的變數
			//JwtJweAccessTokenConverter converter = new JwtJweAccessTokenConverter(keyPair);			

			//converter.setKeyPair(keyPair);			
			//Map<String, Object> map = converter.getTokenAsMap(token);
			//auth = new ObjectMapper().convertValue(map, TsmpAuthorization.class);
		}
		return auth;
	}

	/**
	 * 載入 KeyStore 並取出金鑰對
	 * @return
	 */
	private KeyPair loadAndExtractKeyPair() {
		boolean isReadyToLoad = isReadyToLoadKeyStore();
		if (!isReadyToLoad) {
			this.logger.debug("Unable to load KeyStore. Please check 'digiRunner Token Keypair Setting' in application.properties.");
			return null;
		}
		
		KeyStore keyStore = null;
		try {
			keyStore = KeyStore.getInstance(getKeyStoreType());
		} catch (Exception e) {
			this.logger.debug("Fail to get KeyStore instance with type: " + getKeyStoreType());
			return null;
		}
		
		String ksURI = getKeyStorePath() + getKeyStoreName();
		this.logger.debug("Loading KeyStore from: " + ksURI);
		
		try (FileInputStream fis = new FileInputStream(ksURI)) {
			keyStore.load(fis, getKeyStorePassword());
		} catch (FileNotFoundException e) {
			this.logger.error("KeyStore not found!");
		} catch (Exception e) {
			logger.error("Load KeyStore error!\n" + StackTraceUtil.logStackTrace(e));
		}
		
		if (!isKeyStoreLoaded(keyStore)) {
			this.logger.debug("KeyStore is not loaded");
			return null;
		}
		
		// 取出 KeyPair

		boolean hasKeyAlias = false;
		try {
			hasKeyAlias = keyStore.containsAlias(getKeyAlias());
		} catch (Exception e) {
			this.logger.error("Unable to verify key alias: " + getKeyAlias() + "\n" + StackTraceUtil.logStackTrace(e));
		}
		
		if (!hasKeyAlias) {
			this.logger.debug("No such key alias " + getKeyAlias());
			return null;
		}

		Key key = null;
		try {
			key = keyStore.getKey(getKeyAlias(), getKeyStorePassword());
		} catch (Exception e) {
			this.logger.error("Get key error!\n" + StackTraceUtil.logStackTrace(e));
		}

		if (key == null || !(key instanceof PrivateKey)) {
			this.logger.debug("Fail to get key from KeyStore " + getKeyAlias());
			return null;
		}

		PublicKey publicKey = null;
		try {
			Certificate cert = keyStore.getCertificate(getKeyAlias());
			publicKey = cert.getPublicKey();
		} catch (Exception e) {
			this.logger.error("Get public key error\n" + StackTraceUtil.logStackTrace(e));
		}
		
		if (publicKey == null) {
			this.logger.debug("Fail to get public key " + getKeyAlias());
			return null;
		}

		return new KeyPair(publicKey, (PrivateKey) key);
	}

	/**
	 * 檢查載入 KeyStore 的必要資訊是否齊全
	 */
	private boolean isReadyToLoadKeyStore() {
		if (!(
			StringUtils.hasLength(getKeyStorePath()) &&
			StringUtils.hasLength(getKeyStoreName()) &&
			StringUtils.hasLength(getKeyStoreType()) &&
			(getKeyStorePassword() != null && getKeyStorePassword().length > 0) &&
			StringUtils.hasLength(getKeyAlias())
		)) {
			return false;
		}
		return true;
	}

	/**
	 * 確認 KeyStore 是否已載入
	 * @param keyStore
	 * @return
	 */
	private boolean isKeyStoreLoaded(KeyStore keyStore) {
		try {
			return keyStore.size() > 0;
		} catch (Exception e) {
			return false;
		}
	}

	private byte[] decodeInformation(String base64EncodedStr) {
		byte[] bdata = null;
		try {
			bdata = Base64Util.base64Decode(base64EncodedStr);
		} catch (IllegalArgumentException e) {
			bdata = Base64Util.base64URLDecode(base64EncodedStr);
		}
		return bdata;
	}

	protected String getKeyStorePath() {
		return getEnvironment().getProperty(KEY_PATH);
	}

	protected String getKeyStoreName() {
		return getEnvironment().getProperty(KEY_NAME);
	}

	protected char[] getKeyStorePassword() {
		String tabp_pwd = getEnvironment().getProperty(KEY_PWD);
		if (StringUtils.hasLength(tabp_pwd)) {
			return tabp_pwd.toCharArray();
		}
		return null;
	}

	protected String getKeyStoreType() {
		return getEnvironment().getProperty(KEY_TYPE);
	}

	protected String getKeyAlias() {
		return getEnvironment().getProperty(KEY_ALIAS);
	}

	protected Environment getEnvironment() {
		return env;
	}

	public static void setEnv(Environment env) {
		TsmpAuthorizationBearerParser.env = env;
	}

	public static void setLogger(TPILogger logger) {
		TsmpAuthorizationBearerParser.logger = logger;
	}
	
	

}