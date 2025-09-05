package tpi.dgrv4.gateway.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.component.cipher.TsmpTAEASKHelper;
import tpi.dgrv4.entity.entity.TsmpSetting;
import tpi.dgrv4.entity.exceptions.DgrRtnCode;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpCoreTokenHelperCacheProxy;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpSettingCacheProxy;
import tpi.dgrv4.gateway.constant.DgrDeployRole;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class TsmpSettingService {

	@Value("${digiRunner.gtw.deploy.role}")
	private String deployRole;
	
	private TsmpSettingCacheProxy tsmpSettingCacheProxy;
	private TsmpTAEASKHelper tsmpTAEASKHelper;
	private ObjectMapper objectMapper;
	private TsmpCoreTokenHelperCacheProxy tsmpCoreTokenHelperCacheProxy;

	@Autowired
	public TsmpSettingService(TsmpSettingCacheProxy tsmpSettingCacheProxy,
			TsmpTAEASKHelper tsmpTAEASKHelper, ObjectMapper objectMapper,
			TsmpCoreTokenHelperCacheProxy tsmpCoreTokenHelperCacheProxy) {
		super();
		this.tsmpSettingCacheProxy = tsmpSettingCacheProxy;
		this.tsmpTAEASKHelper = tsmpTAEASKHelper;
		this.objectMapper = objectMapper;
		this.tsmpCoreTokenHelperCacheProxy = tsmpCoreTokenHelperCacheProxy;
	}

	// =========================================================
	// ==================== COMMON METHODS =====================
	// =========================================================
 
	/**
	 * 判斷若值為"ENC()"括起來,則解密 <br>
	 * 
	 * @return 若有做ENC加密,傳回明文; 否則,傳回原值
	 */
	public String getENCPlainVal(String val) {
		Pattern pattern = Pattern.compile("^ENC\\((\\S+)\\)$");
		if (val==null) {val="";} // Oracle 取值會是 null
		Matcher matcher = pattern.matcher(val); // 不接受 null
		if (matcher.matches()) {
			val = matcher.group(1);
			return getTsmpCoreTokenHelperCacheProxy().decrypt(val);
		}
		return val;
	}

	public String getTokenVal(String key) {
		String val = getStringVal(key);
		return getTsmpCoreTokenHelperCacheProxy().decrypt(val);
	}

	public String getTAEASKVal(String id) {
		String val = getStringVal(id);
		return getTsmpTAEASKHelper().decrypt(val);
	}

	public String getStringVal(String id) {
		return getVal(id, (gateway_val) -> {
			if (!StringUtils.hasLength(gateway_val)) {
				return new String();
			}
			return gateway_val;
		});
	}

	public Integer getIntegerVal(String id, Integer defaultVal) {
		return getVal(id, (gateway_val) -> {
			if (!StringUtils.hasText(gateway_val)) {
				return defaultVal;
			}
			try {
				return Integer.valueOf(gateway_val);
			} catch (Exception gateway_e) {
				if (defaultVal != null) {
					TPILogger.tl.debug(StackTraceUtil.logStackTrace(gateway_e));
					return defaultVal;
				}
				throw gateway_e;
			}
		});
	}

	public int getIntVal(String id, int defaultVal) {
		return getIntegerVal(id, Integer.valueOf(defaultVal)).intValue();
	}

	public Long getLongVal(String id, Long defaultVal) {
		return getVal(id, (gateway_val) -> {
			if (!StringUtils.hasText(gateway_val)) {
				return defaultVal;
			}
			try {
				return Long.valueOf(gateway_val);
			} catch (Exception gateway_e) {
				if (defaultVal != null) {
					TPILogger.tl.debug(StackTraceUtil.logStackTrace(gateway_e));
					return defaultVal;
				}
				throw gateway_e;
			}
		});
	}
	
	public long getBasicLongVal(String id, long defaultVal) {
		return getLongVal(id, Long.valueOf(defaultVal)).longValue();
	}
	
	public boolean getBooleanVal(String id, boolean defaultVal) {
		return getVal(id, (val) -> {
			if (!StringUtils.hasText(val)) {
				return defaultVal;
			}
			return Boolean.valueOf(val);
		});
	}

	public <R> List<R> getListVal(String id, String delimiter, Function<String, R> castFunc) {
		if (castFunc == null) {
			throw new IllegalArgumentException("Must provide casting function");
		}
		if (!StringUtils.hasLength(delimiter)) {
			delimiter = "";
		}
		String val = getStringVal(id);
		return Arrays.asList(val.split(delimiter)).stream().map(castFunc).collect(Collectors.toList());
	}

	/**
	 * 若值為"ENC()"括起來,則解密, <br>
	 * 使相關邏輯都能取到明文,不用自己解密 <br>
	 * 
	 * @return 若有做ENC加密,傳回明文; 否則,傳回DB中的原值
	 */	
	public <R> R getVal(String id, Function<String, R> func) {
		TsmpSetting entity = findById(id);
		String val = entity.getValue();
		if (val == null) {
			val = "";
		} // Oracle 取值會是 null
		val = getENCPlainVal(val);
		return func.apply(val);
	}

	private TsmpSetting findById(String id) {
		Optional<TsmpSetting> opt = getTsmpSettingCacheProxy().findById(id);
		if (!opt.isPresent()) {
			TPILogger.tl.debug("id=" + id);
			throw DgrRtnCode._1202.throwing();

		}
		return opt.get();
	}
	
	protected TsmpSettingCacheProxy getTsmpSettingCacheProxy() {
		return this.tsmpSettingCacheProxy;
	}

	protected TsmpTAEASKHelper getTsmpTAEASKHelper() {
		return this.tsmpTAEASKHelper;
	}

	protected ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}

	protected TsmpCoreTokenHelperCacheProxy getTsmpCoreTokenHelperCacheProxy() {
		return this.tsmpCoreTokenHelperCacheProxy;
	}
	
    public boolean getVal_TSMP_ONLINE_CONSOLE() {
    	String key = getKey_TSMP_ONLINE_CONSOLE();
    	return getBooleanVal(key, Boolean.TRUE);
    }
    
    public String getKey_TSMP_ONLINE_CONSOLE() {
    	return TsmpSettingDao.Key.TSMP_ONLINE_CONSOLE;
    }
    
	public String getKey_DGR_PATHS_COMPATIBILITY() {
		return TsmpSettingDao.Key.DGR_PATHS_COMPATIBILITY;
	}

	public int getVal_DGR_PATHS_COMPATIBILITY() {
		String key = getKey_DGR_PATHS_COMPATIBILITY();
		return getIntVal(key,2);
	}

	public String getKey_CUS_CLIENT_ID() {
		return TsmpSettingDao.Key.CUS_CLIENT_ID;
	}

	public String getVal_CUS_CLIENT_ID() {
		String key = getKey_CUS_CLIENT_ID();
		return getStringVal(key);
	}

	public String getKey_DGR_CORS_VAL() {
		return TsmpSettingDao.Key.DGR_CORS_VAL;
	}

	public String getVal_DGR_CORS_VAL() {
		String key = getKey_DGR_CORS_VAL();
		return getStringVal(key);
	}
	
	public String getKey_LDAP_CHECK_ACCT_ENABLE() {
		return TsmpSettingDao.Key.LDAP_CHECK_ACCT_ENABLE;
	}
	
	public boolean getVal_LDAP_CHECK_ACCT_ENABLE() {
		String gateway_key = getKey_LDAP_CHECK_ACCT_ENABLE();
		return getBooleanVal(gateway_key, false);
	}
	
	public String getKey_LDAP_URL() {
		return TsmpSettingDao.Key.LDAP_URL;
	}
		
	public String getVal_LDAP_URL() {
		String gateway_key = getKey_LDAP_URL();
		return getStringVal(gateway_key);
	}
	
	public String getKey_LDAP_DN() {
		return TsmpSettingDao.Key.LDAP_DN;
	}
		
	public String getVal_LDAP_DN() {
		String gateway_key = getKey_LDAP_DN();
		return getStringVal(gateway_key);
	}
	
	public String getKey_LDAP_TIMEOUT() {
		return TsmpSettingDao.Key.LDAP_TIMEOUT;
	}
		
	public String getVal_LDAP_TIMEOUT() {
		String gateway_key = getKey_LDAP_TIMEOUT();
		return getStringVal(gateway_key);
	}
	
	public String getKey_CUS_CLIENT_OPEN_API_KEY() {
		return TsmpSettingDao.Key.CUS_CLIENT_OPEN_API_KEY;
	}
		
	public String getVal_CUS_CLIENT_OPEN_API_KEY() {
		String key = getKey_CUS_CLIENT_OPEN_API_KEY();
		return getStringVal(key);
	}
	
	
	public String getKey_SSO_DOUBLE_CHECK() {
		return TsmpSettingDao.Key.SSO_DOUBLE_CHECK;
	}

	public boolean getVal_SSO_DOUBLE_CHECK() {
		String key = getKey_SSO_DOUBLE_CHECK();
		return getBooleanVal(key, false);
	}
	
	public String getKey_AUDIT_LOG_ENABLE() {
		return TsmpSettingDao.Key.AUDIT_LOG_ENABLE;
	}
	
	public boolean getVal_AUDIT_LOG_ENABLE() {
		String key = getKey_AUDIT_LOG_ENABLE();
		return getBooleanVal(key, false);
	}
	
	public String getKey_CHECK_XSS_ENABLE() {
		return TsmpSettingDao.Key.CHECK_XSS_ENABLE;
	}
		
	public boolean getVal_CHECK_XSS_ENABLE() {
		String key = getKey_CHECK_XSS_ENABLE();
		return getBooleanVal(key, false);
	}
	
	public String getKey_CHECK_XXE_ENABLE() {
		return TsmpSettingDao.Key.CHECK_XXE_ENABLE;
	}
		
	public boolean getVal_CHECK_XXE_ENABLE() {
		String key = getKey_CHECK_XXE_ENABLE();
		return getBooleanVal(key, false);
	}
	
	public String getKey_CHECK_SQL_INJECTION_ENABLE() {
		return TsmpSettingDao.Key.CHECK_SQL_INJECTION_ENABLE;
	}
		
	public boolean getVal_CHECK_SQL_INJECTION_ENABLE() {
		String key = getKey_CHECK_SQL_INJECTION_ENABLE();
		return getBooleanVal(key, false);
	}
	
	public String getKey_DGRKEEPER_IP() {
		return TsmpSettingDao.Key.DGRKEEPER_IP;
	}

	public String getVal_DGRKEEPER_IP() {
		String key = getKey_DGRKEEPER_IP();
		String val = getStringVal(key); 
		// role = Memory 另外使用一個 +10 的 port
		if (DgrDeployRole.MEMORY.value().equalsIgnoreCase(deployRole)) {
			TPILogger.tl.info("I am [Memory] Role, DGR Keeper IP = [127.0.0.1] ");
			val = "127.0.0.1";
		}
		return val; 
	}

	public int getVal_DGRKEEPER_PORT() {
		String key = getKey_DGRKEEPER_PORT();
		int val = getIntVal(key, 8080);
		// role = Memory 另外使用一個 +10 的 port
		if (DgrDeployRole.MEMORY.value().equalsIgnoreCase(deployRole)) {
			TPILogger.tl.info("I am [Memory] Role, DGR Keeper Port + [10] ");
			val = val + 10 ;
		}
		
		return val;
	}


	public String getKey_DGRKEEPER_PORT() {
		return TsmpSettingDao.Key.DGRKEEPER_PORT;
	}
	
	public String getKey_LOGGER_LEVEL() {
		return TsmpSettingDao.Key.LOGGER_LEVEL;
	}

	public String getVal_LOGGER_LEVEL() {
		String key = getKey_LOGGER_LEVEL();
		return getStringVal(key);
	}
	
	public String getKey_CHECK_IGNORE_API_PATH_ENABLE() {
		return TsmpSettingDao.Key.CHECK_IGNORE_API_PATH_ENABLE;
	}
		
	public boolean getVal_CHECK_IGNORE_API_PATH_ENABLE() {
		String key = getKey_CHECK_IGNORE_API_PATH_ENABLE();
		return getBooleanVal(key, false);
	}
	
	public String getKey_IGNORE_API_PATH() {
		return TsmpSettingDao.Key.IGNORE_API_PATH;
	}
		
	public String getVal_IGNORE_API_PATH() {
		String key = getKey_IGNORE_API_PATH();
		return getStringVal(key);
	}

	public String getKey_CHECK_API_STATUS_ENABLE() {
		return TsmpSettingDao.Key.CHECK_API_STATUS_ENABLE;
	}
		
	public boolean getVal_CHECK_API_STATUS_ENABLE() {
		String key = getKey_CHECK_API_STATUS_ENABLE();
		return getBooleanVal(key, false);
	}
	
	public String getKey_CHECK_TRAFFIC_ENABLE() {
		return TsmpSettingDao.Key.CHECK_TRAFFIC_ENABLE;
	}
		
	public boolean getVal_CHECK_TRAFFIC_ENABLE() {
		String key = getKey_CHECK_TRAFFIC_ENABLE();
		return getBooleanVal(key, false);
	}
	
	public String getKey_ES_URL() {
		return TsmpSettingDao.Key.ES_URL;
	}
		
	public String getVal_ES_URL() {
		String key = getKey_ES_URL();
		return getStringVal(key);
	}
	
	public String getKey_ES_ID_PWD() {
		return TsmpSettingDao.Key.ES_ID_PWD;
	}
		
	public String getVal_ES_ID_PWD() {
		String key = getKey_ES_ID_PWD();
		return getStringVal(key);
	}

	public String getKey_KIBANA_PWD() { 
		return TsmpSettingDao.Key.KIBANA_PWD;
	}
		
	public String getVal_KIBANA_PWD() {
		String key = getKey_KIBANA_PWD();
		return getStringVal(key);
	}
	
	public String getKey_KIBANA_USER() { 
		return TsmpSettingDao.Key.KIBANA_USER;
	}
		
	public String getVal_KIBANA_USER() {
		String key = getKey_KIBANA_USER();
		String val = getStringVal(key);
		return val;
	}
	
	public String getKey_KIBANA_VERSION() { 
		return TsmpSettingDao.Key.KIBANA_VERSION;
	}
		
	public String getVal_KIBANA_VERSION() {
		String key = getKey_KIBANA_VERSION();
		String val = getStringVal(key);
		return val;
	}
	
	public String getKey_KIBANA_HOST() { 
		return TsmpSettingDao.Key.KIBANA_HOST;
	}
		
	public String getVal_KIBANA_HOST() {
		String key = getKey_KIBANA_HOST();
		String val = getStringVal(key);
		return val;
	}
	
	public String getKey_KIBANA_PORT() { 
		return TsmpSettingDao.Key.KIBANA_PORT;
	}
		
	public String getVal_KIBANA_PORT() {
		String key = getKey_KIBANA_PORT();
		String val = getStringVal(key);
		return val;
	}
	
	public String getKey_KIBANA_TRANSFER_PROTOCOL() { 
		return TsmpSettingDao.Key.KIBANA_TRANSFER_PROTOCOL;
	}
		
	public String getVal_KIBANA_TRANSFER_PROTOCOL() {
		String key = getKey_KIBANA_TRANSFER_PROTOCOL();
		String val = getStringVal(key);
		return val;
	}

	public String getKey_KIBANA_STATUS_URL() {
		return TsmpSettingDao.Key.KIBANA_STATUS_URL;
	}

	public String getVal_KIBANA_STATUS_URL() {
		String key = getKey_KIBANA_STATUS_URL();
		String val = getStringVal(key);
		return val;
	}
	
	public String getKey_ES_TEST_TIMEOUT() {
		return TsmpSettingDao.Key.ES_TEST_TIMEOUT;
	}
		
	public int getVal_ES_TEST_TIMEOUT() {
		String key = getKey_ES_TEST_TIMEOUT();
		return getIntVal(key, 3000);
	}
	
	public String getKey_ES_MBODY_MASK_FLAG() {
		return TsmpSettingDao.Key.ES_MBODY_MASK_FLAG;
	}
		
	public boolean getVal_ES_MBODY_MASK_FLAG() {
		String key = getKey_ES_MBODY_MASK_FLAG();
		return getBooleanVal(key, false);
	}
	
	public String getKey_ES_IGNORE_API() {
		return TsmpSettingDao.Key.ES_IGNORE_API;
	}
		
	public String getVal_ES_IGNORE_API() {
		String key = getKey_ES_IGNORE_API();
		return getStringVal(key);
	}
	
	public String getKey_ES_MBODY_MASK_API() {
		return TsmpSettingDao.Key.ES_MBODY_MASK_API;
	}
		
	public String getVal_ES_MBODY_MASK_API() {
		String key = getKey_ES_MBODY_MASK_API();
		return getStringVal(key);
	}
	
	public String getKey_ES_TOKEN_MASK_FLAG() {
		return TsmpSettingDao.Key.ES_TOKEN_MASK_FLAG;
	}
		
	public boolean getVal_ES_TOKEN_MASK_FLAG() {
		String key = getKey_ES_TOKEN_MASK_FLAG();
		return getBooleanVal(key, true);
	}
	
	public String getKey_ES_MAX_SIZE_MBODY_MASK() {
		return TsmpSettingDao.Key.ES_MAX_SIZE_MBODY_MASK;
	}
		
	public int getVal_ES_MAX_SIZE_MBODY_MASK() {
		String key = getKey_ES_MAX_SIZE_MBODY_MASK();
		return getIntVal(key, 0);
	}
	
	public String getKey_DGR_TOKEN_JWE_ENABLE() {
		return TsmpSettingDao.Key.DGR_TOKEN_JWE_ENABLE;
	}
		
	public boolean getVal_DGR_TOKEN_JWE_ENABLE() {
		String key = getKey_DGR_TOKEN_JWE_ENABLE();
		return getBooleanVal(key, false);
	}
	
	public String getKey_DGR_TOKEN_WHITELIST_ENABLE() {
		return TsmpSettingDao.Key.DGR_TOKEN_WHITELIST_ENABLE;
	}
	
	public boolean getVal_DGR_TOKEN_WHITELIST_ENABLE() {
		String key = getKey_DGR_TOKEN_WHITELIST_ENABLE();
		return getBooleanVal(key, false);
	}
	
	public String getKey_ES_DGRC_MBODY_MASK_URI() {
		return TsmpSettingDao.Key.ES_DGRC_MBODY_MASK_URI;
	}
		
	public String getVal_ES_DGRC_MBODY_MASK_URI() {
		String key = getKey_ES_DGRC_MBODY_MASK_URI();
		return getStringVal(key);
	}
	
	public String getKey_DGR_TW_FAPI_ENABLE() {
		return TsmpSettingDao.Key.DGR_TW_FAPI_ENABLE;
	}
	
	public boolean getVal_DGR_TW_FAPI_ENABLE() {
		String key = getKey_DGR_TW_FAPI_ENABLE();
		return getBooleanVal(key, false);
	}
	
	public String getKey_ES_DGRC_IGNORE_URI() {
		return TsmpSettingDao.Key.ES_DGRC_IGNORE_URI;
	}
		
	public String getVal_ES_DGRC_IGNORE_URI() {
		String key = getKey_ES_DGRC_IGNORE_URI();
		return getStringVal(key);
	}
	
	// =========================================================
	// ==================== DPAA METHODS =====================
	// =========================================================
	
	public String getKey_TSMP_SYS_TYPE() {
		return TsmpSettingDao.Key.TSMP_SYS_TYPE;
	}

	public String getVal_TSMP_SYS_TYPE() {
		String key = getKey_TSMP_SYS_TYPE();
		return getStringVal(key);
	}

	public String getKey_TSMP_NODE_TPS() {
		return TsmpSettingDao.Key.TSMP_NODE_TPS;
	}

	public int getVal_TSMP_NODE_TPS() {
		String key = getKey_TSMP_NODE_TPS();
		return getIntVal(key, 10);
	}

	public String getKey_TSMP_AC_CONF() {
		return TsmpSettingDao.Key.TSMP_AC_CONF;
	}

	public Map<String, Object> getVal_TSMP_AC_CONF() {
		String key = getKey_TSMP_AC_CONF();
		return getVal(key, (val) -> {
			Map<String, Object> gateway_args = new HashMap<String, Object>();
			gateway_args.put("dp", 0);
			gateway_args.put("net", false);
			try {
				gateway_args = getObjectMapper().readValue(val, //
						new TypeReference<Map<String, Object>>(){}); // converts JSON to Map
			} catch (Exception e) {
				TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			}
			return gateway_args;
		});
	}

	public String getKey_TSMP_COMPOSER_ADDRESS() {
		return TsmpSettingDao.Key.TSMP_COMPOSER_ADDRESS;
	}

	public List<String> getVal_TSMP_COMPOSER_ADDRESS() {
		String key = getKey_TSMP_COMPOSER_ADDRESS();
		return getVal(key, (val) -> {
			if (StringUtils.isEmpty(val)) {
				return Collections.emptyList();
			} else {
				List<String> urls = Arrays.asList(val.split(","));
				return Collections.unmodifiableList(urls);
			}
		});
	}

	public String getKey_TSMP_COMPOSER_PORT() {
		return TsmpSettingDao.Key.TSMP_COMPOSER_PORT;
	}

	/**
	 * 給前端頁面使用用的路徑，主要是給AC使用
	 * @return
	 */
	public int getVal_TSMP_COMPOSER_PORT() {
		String gateway_key = getKey_TSMP_COMPOSER_PORT();
		return getIntVal(gateway_key, 1880);
	}

	public String getKey_TSMP_COMPOSER_PATH() {
		return TsmpSettingDao.Key.TSMP_COMPOSER_PATH;
	}

	/**
	 * 給前端頁面使用用的路徑，主要是給AC使用
	 * @return
	 */
	public String getVal_TSMP_COMPOSER_PATH() {
		String key = getKey_TSMP_COMPOSER_PATH();
		String gateway_path = getStringVal(key);
		if (StringUtils.isEmpty(gateway_path)) {
			return "/editor/tsmpApi";
		} else {
			return gateway_path;
		}
	}

	public String getKey_TSMP_PROXY_PORT() {
		return TsmpSettingDao.Key.TSMP_PROXY_PORT;
	}

	/**
	 * 目前無使用，保留，當初功能性與 TSMP_REPORT_ADDRESS 相同
	 * @return
	 */
	public int getVal_TSMP_PROXY_PORT() {
		String key = getKey_TSMP_PROXY_PORT();
		return getIntVal(key, 4944);
	}

	public String getKey_TSMP_APILOG_FORCE_WRITE_RDB() {
		return TsmpSettingDao.Key.TSMP_APILOG_FORCE_WRITE_RDB;
	}

	public String getVal_TSMP_APILOG_FORCE_WRITE_RDB() {
		String key = getKey_TSMP_APILOG_FORCE_WRITE_RDB();
		return getStringVal(key);
	}
	
	public String getKey_TSMP_EDITION() {
		return TsmpSettingDao.Key.TSMP_EDITION;
	}

	public String getVal_TSMP_LICENSE_KEY() {
		String key = getKey_TSMP_EDITION();
		return getStringVal(key);
	}
	
	public String getKey_TSMP_AC_CLIENT_ID() {
		return TsmpSettingDao.Key.TSMP_AC_CLIENT_ID;
	}

	public String getVal_TSMP_AC_CLIENT_ID() {
		String gateway_key = getKey_TSMP_AC_CLIENT_ID();
		return getStringVal(gateway_key);
	}
	
	public String getKey_TSMP_AC_CLIENT_PW() {
		return TsmpSettingDao.Key.TSMP_AC_CLIENT_PW;
	}
	
	public String getVal_TSMP_AC_CLIENT_PW() {
		String gateway_key = getKey_TSMP_AC_CLIENT_PW();
		return getStringVal(gateway_key);
	}
	
	public String getKey_TSMP_SIGNBLOCK_EXPIRED() {
		return TsmpSettingDao.Key.TSMP_SIGNBLOCK_EXPIRED;
	}

	public int getVal_TSMP_SIGNBLOCK_EXPIRED(){
		String key = getKey_TSMP_SIGNBLOCK_EXPIRED();
		int val = getIntVal(key, 24);//24小時
		return val;
	}
	
	public String getKey_TSMP_CORE_V3_MODULE_FILE_UPLOAD_PATH() {
		return TsmpSettingDao.Key.TSMP_CORE_V3_MODULE_FILE_UPLOAD_PATH;
	}

	public String getVal_TSMP_CORE_V3_MODULE_FILE_UPLOAD_PATH() {
		String key = getKey_TSMP_CORE_V3_MODULE_FILE_UPLOAD_PATH();
		return getStringVal(key);
	}
	
	public String getKey_TSMP_CORE_V2_MODULE_FILE_UPLOAD_PATH() {
		return TsmpSettingDao.Key.TSMP_CORE_V2_MODULE_FILE_UPLOAD_PATH;
	}

	public String getVal_TSMP_CORE_V2_MODULE_FILE_UPLOAD_PATH() {
		String key = getKey_TSMP_CORE_V2_MODULE_FILE_UPLOAD_PATH();
		return getStringVal(key);
	}

	public String getKey_CUS_MODULE_EXIST() {
		return TsmpSettingDao.Key.CUS_MODULE_EXIST;
	}

	public boolean getVal_CUS_MODULE_EXIST() {
		String key = getKey_CUS_MODULE_EXIST();
		return getBooleanVal(key, false);
	}
	
	public String getKey_DELETEMODULE_ALERT() {
		return TsmpSettingDao.Key.DELETEMODULE_ALERT;
	}

	public boolean getVal_DELETEMODULE_ALERT() {
		String gateway_key = getKey_DELETEMODULE_ALERT();
		return getBooleanVal(gateway_key, false);
	}

	public String getKey_CUS_MODULE_NAME() {
		return TsmpSettingDao.Key.CUS_MODULE_NAME;
	}

	public String getVal_CUS_MODULE_NAME() {
		String gateway_key = getKey_CUS_MODULE_NAME();
		return getStringVal(gateway_key);
	}

	public String getVal_CUS_MODULE_NAME(int index) {
		String gateway_key = getKey_CUS_MODULE_NAME();
		gateway_key = (index < 0 ? gateway_key : (gateway_key + index));
		return getStringVal(gateway_key);
	}

	public String getKey_CUS_FUNC_ENABLE() {
		return TsmpSettingDao.Key.CUS_FUNC_ENABLE;
	}

	public boolean getVal_CUS_FUNC_ENABLE() {
		String key = getKey_CUS_FUNC_ENABLE();
		return getBooleanVal(key, false);
	}

	public boolean getVal_CUS_FUNC_ENABLE(int index) {
		String key = getKey_CUS_FUNC_ENABLE();
		key = (index < 0 ? key : (key + index));
		return getBooleanVal(key, false);
	}

	public String getKey_SSO_PKCE() {
		return TsmpSettingDao.Key.SSO_PKCE;
	}

	public boolean getVal_SSO_PKCE() {
		String key = getKey_SSO_PKCE();
		return getBooleanVal(key, false);
	}
	
	public String getKey_SSO_AUTO_CREATE_USER() {
		return TsmpSettingDao.Key.SSO_AUTO_CREATE_USER;
	}

	public boolean getVal_SSO_AUTO_CREATE_USER() {
		String key = getKey_SSO_AUTO_CREATE_USER();
		return getBooleanVal(key, false);
	}
	
	public String getKey_SSO_TIMEOUT() {
		return TsmpSettingDao.Key.SSO_TIMEOUT;
	}
	
	public Integer getVal_SSO_TIMEOUT() {
		String key = getKey_SSO_TIMEOUT();
		return getIntegerVal(key, 10);
	}
	
	public String getKey_UDPSSO_LOGIN_NETWORK() {
		return TsmpSettingDao.Key.UDPSSO_LOGIN_NETWORK;
	}
	
	public String getVal_UDPSSO_LOGIN_NETWORK() {
		String key = getKey_UDPSSO_LOGIN_NETWORK();
		return getStringVal(key);
	}
	
    public String getKey_TSMP_DPAA_RUNLOOP_INTERVAL() {
    	return TsmpSettingDao.Key.TSMP_DPAA_RUNLOOP_INTERVAL;
    }

    public int getVal_TSMP_DPAA_RUNLOOP_INTERVAL() {
    	String key = getKey_TSMP_DPAA_RUNLOOP_INTERVAL();
    	return getIntegerVal(key, 1);
    }
    
    public String getKey_TSMP_FAIL_THRESHOLD() {
    	return TsmpSettingDao.Key.TSMP_FAIL_THRESHOLD;
    }
    
    public int getVal_TSMP_FAIL_THRESHOLD() {
    	String key = getKey_TSMP_FAIL_THRESHOLD();
    	return getIntegerVal(key, 6);
    }
	
	public String getKey_SERVICE_MAIL_ENABLE() {
		return TsmpSettingDao.Key.SERVICE_MAIL_ENABLE;
	}
	
	public String getVal_SERVICE_MAIL_ENABLE() {
		String gateway_key = getKey_SERVICE_MAIL_ENABLE();
		return getStringVal(gateway_key);
	}
	
	
	public String getKey_SERVICE_MAIL_HOST() {
		return TsmpSettingDao.Key.SERVICE_MAIL_HOST;
	}
	
	public String getVal_SERVICE_MAIL_HOST() {
		String gateway_key = getKey_SERVICE_MAIL_HOST();
		return getStringVal(gateway_key);
	}
	
	public String getKey_SERVICE_MAIL_PORT() {
		return TsmpSettingDao.Key.SERVICE_MAIL_PORT;
	}
	
	public String getVal_SERVICE_MAIL_PORT() {
		String gateway_key = getKey_SERVICE_MAIL_PORT();
		return getStringVal(gateway_key);
	}
	
	public String getKey_SERVICE_MAIL_AUTH() {
		return TsmpSettingDao.Key.SERVICE_MAIL_AUTH;
	}
	
	public String getVal_SERVICE_MAIL_AUTH() {
		String gateway_key = getKey_SERVICE_MAIL_AUTH();
		return getStringVal(gateway_key);
	}
	
	public String getKey_SERVICE_MAIL_STARTTLS_ENABLE() {
		return TsmpSettingDao.Key.SERVICE_MAIL_STARTTLS_ENABLE;
	}
	
	public String getVal_SERVICE_MAIL_STARTTLS_ENABLE() {
		String gateway_key = getKey_SERVICE_MAIL_STARTTLS_ENABLE();
		return getStringVal(gateway_key);
	}
	
	
	public String getKey_SERVICE_MAIL_USERNAME() {
		return TsmpSettingDao.Key.SERVICE_MAIL_USERNAME;
	}
	
	public String getVal_SERVICE_MAIL_USERNAME() {
		String gateway_key = getKey_SERVICE_MAIL_USERNAME();
		return getStringVal(gateway_key);
	}
	
	public String getKey_SERVICE_MAIL_PASSWORD() {
		return TsmpSettingDao.Key.SERVICE_MAIL_PASSWORD;
	}
	
	public String getVal_SERVICE_MAIL_PASSWORD() {
		String key = getKey_SERVICE_MAIL_PASSWORD();
		return getStringVal(key);
	}
	
	public String getKey_SERVICE_MAIL_FROM() {
		return TsmpSettingDao.Key.SERVICE_MAIL_FROM;
	}
	
	public String getVal_SERVICE_MAIL_FROM() {
		String gateway_key = getKey_SERVICE_MAIL_FROM();
		return getStringVal(gateway_key);
	}
	
	
	public String getKey_SERVICE_MAIL_X_MAILER() {
		return TsmpSettingDao.Key.SERVICE_MAIL_X_MAILER;
	}
	
	public String getVal_SERVICE_MAIL_X_MAILER() {
		String gateway_key = getKey_SERVICE_MAIL_X_MAILER();
		return getStringVal(gateway_key);
	}
	
	public String getKey_SERVICE_SECONDARY_MAIL_ENABLE() {
		return TsmpSettingDao.Key.SERVICE_SECONDARY_MAIL_ENABLE;
	}
	
	public String getVal_SERVICE_SECONDARY_MAIL_ENABLE() {
		String key = getKey_SERVICE_SECONDARY_MAIL_ENABLE();
		return getStringVal(key);
	}
	
	
	public String getKey_SERVICE_SECONDARY_MAIL_HOST() {
		return TsmpSettingDao.Key.SERVICE_SECONDARY_MAIL_HOST;
	}
	
	public String getVal_SERVICE_SECONDARY_MAIL_HOST() {
		String gateway_key = getKey_SERVICE_SECONDARY_MAIL_HOST();
		return getStringVal(gateway_key);
	}
	
	public String getKey_SERVICE_SECONDARY_MAIL_PORT() {
		return TsmpSettingDao.Key.SERVICE_SECONDARY_MAIL_PORT;
	}
	
	public String getVal_SERVICE_SECONDARY_MAIL_PORT() {
		String gateway_key = getKey_SERVICE_SECONDARY_MAIL_PORT();
		return getStringVal(gateway_key);
	}
	
	public String getKey_SERVICE_SECONDARY_MAIL_AUTH() {
		return TsmpSettingDao.Key.SERVICE_SECONDARY_MAIL_AUTH;
	}
	
	public String getVal_SERVICE_SECONDARY_MAIL_AUTH() {
		String gateway_key = getKey_SERVICE_SECONDARY_MAIL_AUTH();
		return getStringVal(gateway_key);
	}
	
	
	public String getKey_SERVICE_SECONDARY_MAIL_STARTTLS_ENABLE() {
		return TsmpSettingDao.Key.SERVICE_SECONDARY_MAIL_STARTTLS_ENABLE;
	}
	
	public String getVal_SERVICE_SECONDARY_MAIL_STARTTLS_ENABLE() {
		String gateway_key = getKey_SERVICE_SECONDARY_MAIL_STARTTLS_ENABLE();
		return getStringVal(gateway_key);
	}
	
	public String getKey_SERVICE_SECONDARY_MAIL_USERNAME() {
		return TsmpSettingDao.Key.SERVICE_SECONDARY_MAIL_USERNAME;
	}
	
	public String getVal_SERVICE_SECONDARY_MAIL_USERNAME() {
		String gateway_key = getKey_SERVICE_SECONDARY_MAIL_USERNAME();
		return getStringVal(gateway_key);
	}
	
	public String getKey_SERVICE_SECONDARY_MAIL_PASSWORD() {
		return TsmpSettingDao.Key.SERVICE_SECONDARY_MAIL_PASSWORD;
	}
	
	public String getVal_SERVICE_SECONDARY_MAIL_PASSWORD() {
		String key = getKey_SERVICE_SECONDARY_MAIL_PASSWORD();
		return getStringVal(key);
	}
	
	public String getKey_SERVICE_SECONDARY_MAIL_FROM() {
		return TsmpSettingDao.Key.SERVICE_SECONDARY_MAIL_FROM;
	}
	
	public String getVal_SERVICE_SECONDARY_MAIL_FROM() {
		String gateway_key = getKey_SERVICE_SECONDARY_MAIL_FROM();
		return getStringVal(gateway_key);
	}
	
	public String getKey_SERVICE_SECONDARY_MAIL_X_MAILER() {
		return TsmpSettingDao.Key.SERVICE_SECONDARY_MAIL_X_MAILER;
	}
	
	public String getVal_SERVICE_SECONDARY_MAIL_X_MAILER() {
		String gateway_key = getKey_SERVICE_SECONDARY_MAIL_X_MAILER();
		return getStringVal(gateway_key);
	}
	
	public String getKey_CLIENT_CREDENTIALS_DEFAULT_USERNAME() {
		return TsmpSettingDao.Key.CLIENT_CREDENTIALS_DEFAULT_USERNAME;
	}
	
	public boolean getVal_CLIENT_CREDENTIALS_DEFAULT_USERNAME() {
		String key = getKey_CLIENT_CREDENTIALS_DEFAULT_USERNAME();
		return getBooleanVal(key, false);
	}
	
	public String getKey_DGR_LOGOUT_URL() {
		return TsmpSettingDao.Key.DGR_LOGOUT_URL;
	}

	public String getVal_DGR_LOGOUT_URL() {
		String key = getKey_DGR_LOGOUT_URL();
		return getStringVal(key);
	}
	
	public String getKey_LOGOUT_API() {
		return TsmpSettingDao.Key.LOGOUT_API;
	}

	public String getVal_LOGOUT_API() {
		String key = getKey_LOGOUT_API();
		return getStringVal(key);
	}

	public String getKey_FIXED_CACHE_TIME() {
		return TsmpSettingDao.Key.FIXED_CACHE_TIME;
	}
		
	public int getVal_FIXED_CACHE_TIME() {
		String key = getKey_FIXED_CACHE_TIME();
		return getIntVal(key, 60);
	}
	
	public String getKey_ES_LOG_DISABLE() {
		return TsmpSettingDao.Key.ES_LOG_DISABLE;
	}
	
	public boolean getVal_ES_LOG_DISABLE() {
		String key = getKey_ES_LOG_DISABLE();
		return getBooleanVal(key, false);
	}
	
	public String getKey_ES_SYS_TYPE() {
		return TsmpSettingDao.Key.ES_SYS_TYPE;
	}

	public String getVal_ES_SYS_TYPE() {
		String key = getKey_ES_SYS_TYPE();
		return getStringVal(key);
	}
	
	public String getKey_ES_MONITOR_DISABLE() {
		return TsmpSettingDao.Key.ES_MONITOR_DISABLE;
	}
	
	public boolean getVal_ES_MONITOR_DISABLE() {
		String key = getKey_ES_MONITOR_DISABLE();
		return getBooleanVal(key, false);
	}
	
	public String getKey_ERRORLOG_KEYWORD() {
		return TsmpSettingDao.Key.ERRORLOG_KEYWORD;
	}

	public String getVal_ERRORLOG_KEYWORD() {
		String gateway_key = getKey_ERRORLOG_KEYWORD();
		return getStringVal(gateway_key);
	}
	
	public String getKey_AUTH_CODE_EXP_TIME() {
		return TsmpSettingDao.Key.AUTH_CODE_EXP_TIME;
	}

	public String getVal_AUTH_CODE_EXP_TIME() {
		String gateway_key = getKey_AUTH_CODE_EXP_TIME();
		return getStringVal(gateway_key);
	}
	
	public String getKey_SHUTDOWN_ENDPOINT_ALLOWED_IPS() {
		return TsmpSettingDao.Key.SHUTDOWN_ENDPOINT_ALLOWED_IPS;
	}

	public String getVal_SHUTDOWN_ENDPOINT_ALLOWED_IPS() {
		String key = getKey_SHUTDOWN_ENDPOINT_ALLOWED_IPS();
		return getStringVal(key);
	}
	
	public String getKey_MAIL_SEND_TIME() {
		return TsmpSettingDao.Key.MAIL_SEND_TIME;
	}

	public String getVal_MAIL_SEND_TIME() {
		String key = getKey_MAIL_SEND_TIME();
		return getStringVal(key);
	}
	
	// AC IdP
	public String getKey_AC_IDP_REVIEWER_MAILLIST() {
		return TsmpSettingDao.Key.AC_IDP_REVIEWER_MAILLIST;
	}

	public String getVal_AC_IDP_REVIEWER_MAILLIST() {
		String key = getKey_AC_IDP_REVIEWER_MAILLIST();
		return getStringVal(key);
	}

	public String getKey_AC_IDP_MSG_URL() {
		return TsmpSettingDao.Key.AC_IDP_MSG_URL;
	}

	public String getVal_AC_IDP_MSG_URL() {
		String key = getKey_AC_IDP_MSG_URL();
		return getStringVal(key);
	}
	
	public String getKey_AC_IDP_REVIEW_URL() {
		return TsmpSettingDao.Key.AC_IDP_REVIEW_URL;
	}
	
	public String getVal_AC_IDP_REVIEW_URL() {
		String key = getKey_AC_IDP_REVIEW_URL();
		return getStringVal(key);
	}
	
	public String getKey_AC_IDP_ACCALLBACK_URL() {
		return TsmpSettingDao.Key.AC_IDP_ACCALLBACK_URL;
	}
	
	public String getVal_AC_IDP_ACCALLBACK_URL() {
		String key = getKey_AC_IDP_ACCALLBACK_URL();
		return getStringVal(key);
	}

	public String getKey_AC_IDP_LDAP_REVIEW_ENABLE() {
		return TsmpSettingDao.Key.AC_IDP_LDAP_REVIEW_ENABLE;
	}

	public boolean getVal_AC_IDP_LDAP_REVIEW_ENABLE() {
		String gateway_key = getKey_AC_IDP_LDAP_REVIEW_ENABLE();
		return getBooleanVal(gateway_key, false);
	}

	public String getKey_AC_IDP_API_REVIEW_ENABLE() {
		return TsmpSettingDao.Key.AC_IDP_API_REVIEW_ENABLE;
	}
	
	public boolean getVal_AC_IDP_API_REVIEW_ENABLE() {
		String gateway_key = getKey_AC_IDP_API_REVIEW_ENABLE();
		return getBooleanVal(gateway_key, false);
	}
	
	public String getKey_AC_IDP_CUS_REVIEW_ENABLE() {
		return TsmpSettingDao.Key.AC_IDP_CUS_REVIEW_ENABLE;
	}
	
	public boolean getVal_AC_IDP_CUS_REVIEW_ENABLE() {
		String gateway_key = getKey_AC_IDP_CUS_REVIEW_ENABLE();
		return getBooleanVal(gateway_key, false);
	}
	
	// GTW IdP
	public String getKey_GTW_IDP_JWK1() {
		return TsmpSettingDao.Key.GTW_IDP_JWK1;
	}
	
	public String getVal_GTW_IDP_JWK1() {
		String key = getKey_GTW_IDP_JWK1();
		return getStringVal(key);
	}
	
	public String getKey_GTW_IDP_JWK2() {
		return TsmpSettingDao.Key.GTW_IDP_JWK2;
	}
	
	public String getVal_GTW_IDP_JWK2() {
		String key = getKey_GTW_IDP_JWK2();
		return getStringVal(key);
	}
	
	public String getKey_GTW_IDP_MSG_URL() {
		return TsmpSettingDao.Key.GTW_IDP_MSG_URL;
	}
	
	public String getVal_GTW_IDP_MSG_URL() {
		String key = getKey_GTW_IDP_MSG_URL();
		return getStringVal(key);
	}
	
	public String getKey_GTW_IDP_LOGIN_URL() {
		return TsmpSettingDao.Key.GTW_IDP_LOGIN_URL;
	}
	
	public String getVal_GTW_IDP_LOGIN_URL() {
		String key = getKey_GTW_IDP_LOGIN_URL();
		return getStringVal(key);
	}
	
	public String getKey_GTW_IDP_CONSENT_URL() {
		return TsmpSettingDao.Key.GTW_IDP_CONSENT_URL;
	}
	
	public String getVal_GTW_IDP_CONSENT_URL() {
		String key = getKey_GTW_IDP_CONSENT_URL();
		return getStringVal(key);
	}
	
	// 
	public String getKey_DGR_PUBLIC_DOMAIN() {
		return TsmpSettingDao.Key.DGR_PUBLIC_DOMAIN;
	}
	
	public String getVal_DGR_PUBLIC_DOMAIN() {
		String key = getKey_DGR_PUBLIC_DOMAIN();
		return getStringVal(key);
	}
	
	public String getKey_DGR_PUBLIC_PORT() {
		return TsmpSettingDao.Key.DGR_PUBLIC_PORT;
	}

	public String getVal_DGR_PUBLIC_PORT() {
		String key = getKey_DGR_PUBLIC_PORT();
		return getStringVal(key);
	}
	
	public String getKey_DGR_HOST_HEADER() {
		return TsmpSettingDao.Key.DGR_HOST_HEADER;
	}
	
	public String getVal_DGR_HOST_HEADER() {
		String key = getKey_DGR_HOST_HEADER();
		return getStringVal(key);
	}
	
	public String getKey_DGR_CSP_VAL() {
		return TsmpSettingDao.Key.DGR_CSP_VAL;
	}
	
	public String getVal_DGR_CSP_VAL() {
		String key = getKey_DGR_CSP_VAL();
		return getStringVal(key);
	}
 
	public String getKey_DGR_AC_LOGIN_PAGE() {
		return TsmpSettingDao.Key.DGR_AC_LOGIN_PAGE;
	}
	
	public String getVal_DGR_AC_LOGIN_PAGE() {
		String key = getKey_DGR_AC_LOGIN_PAGE();
		return getStringVal(key);
	}
	
	public String getKey_DGR_COOKIE_TOKEN_ENABLE() {
		return TsmpSettingDao.Key.DGR_COOKIE_TOKEN_ENABLE;
	}
	
	public boolean getVal_DGR_COOKIE_TOKEN_ENABLE() {
		String key = getKey_DGR_COOKIE_TOKEN_ENABLE();
		return getBooleanVal(key, false);
	}
	
	public String getKey_CHECK_JTI_ENABLE() {
		return TsmpSettingDao.Key.CHECK_JTI_ENABLE;
	}
		
	public boolean getVal_CHECK_JTI_ENABLE() {
		String key = getKey_CHECK_JTI_ENABLE();
		return getBooleanVal(key, false);
	}
	
	public String getKey_COMPOSER_LOG_INTERVAL() {
		return TsmpSettingDao.Key.COMPOSER_LOG_INTERVAL;
	}
		
	public String getVal_COMPOSER_LOG_INTERVAL() {
		String key = getKey_COMPOSER_LOG_INTERVAL();
		return getStringVal(key);
	}
	
	public String getKey_COMPOSER_LOG_SIZE() {
		return TsmpSettingDao.Key.COMPOSER_LOG_SIZE;
	}
		
	public String getVal_COMPOSER_LOG_SIZE() {
		String key = getKey_COMPOSER_LOG_SIZE();
		return getStringVal(key);
	}
	
	public String getKey_COMPOSER_LOG_SWICTH() {
		return TsmpSettingDao.Key.COMPOSER_LOG_SWICTH;
	}
		
	public String getVal_COMPOSER_LOG_SWICTH() {
		String key = getKey_COMPOSER_LOG_SWICTH();
		return getStringVal(key);
	}
	
	public String getKey_COMPOSER_LOG_MAX_FILES() {
		return TsmpSettingDao.Key.COMPOSER_LOG_MAX_FILES;
	}
		
	public String getVal_COMPOSER_LOG_MAX_FILES() {
		String key = getKey_COMPOSER_LOG_MAX_FILES();
		return getStringVal(key);
	}
	
	public String getKey_COMPOSER_REQUEST_TIMEOUT() {
		return TsmpSettingDao.Key.COMPOSER_REQUEST_TIMEOUT;
	}
		
	public String getVal_COMPOSER_REQUEST_TIMEOUT() {
		String key = getKey_COMPOSER_REQUEST_TIMEOUT();
		return getStringVal(key);
	}
	
	public String getKey_DGR_ON_AWS() {
		return TsmpSettingDao.Key.DGR_ON_AWS;
	}
		
	public Boolean getVal_DGR_ON_AWS() {
		String key = getKey_DGR_ON_AWS();
		return getBooleanVal(key, false);
	}
	
	public String getKey_AWS_PUBLIC_KEY() {
		return TsmpSettingDao.Key.AWS_PUBLIC_KEY;
	}
		
	public String getVal_AWS_PUBLIC_KEY() {
		String key = getKey_AWS_PUBLIC_KEY();
		return getStringVal(key);
	}

	public String getVal_KIBANA_REPORTURL_PREFIX() {
		String key = getKey_KIBANA_REPORTURL_PREFIX();
		return getStringVal(key);
	}

	private String getKey_KIBANA_REPORTURL_PREFIX() {
		return TsmpSettingDao.Key.KIBANA_REPORTURL_PREFIX;
	}

	public String getVal_KIBANA_AUTH() {
		String key = getKey_KIBANA_AUTH();
		return getStringVal(key);
	}

	private String getKey_KIBANA_AUTH() {
		return TsmpSettingDao.Key.KIBANA_AUTH;
	}


	public String getVal_KIBANA_LOGIN_URL() {
		String key = getKey_KIBANA_LOGIN_URL();
		return getStringVal(key);
	}

	private String getKey_KIBANA_LOGIN_URL() {
		return TsmpSettingDao.Key.KIBANA_LOGIN_URL;
	}

	public String getVal_KIBANA_LOGIN_REQUESTBODY() {
		String key = getKey_KIBANA_LOGIN_REQUESTBODY();
		return getStringVal(key);
	}

	private String getKey_KIBANA_LOGIN_REQUESTBODY() {
		return TsmpSettingDao.Key.KIBANA_LOGIN_REQUESTBODY;
	}

	public String getKey_CUS_NAME_SETTING() {
		return TsmpSettingDao.Key.CUS_NAME_SETTING;
	}

	public String getVal_CUS_NAME_SETTING() {
		String key = getKey_CUS_NAME_SETTING();
		return getStringVal(key);
	}
	
	public String getKey_CUS_PWD_SETTING() {
		return TsmpSettingDao.Key.CUS_PWD_SETTING;
	}

	public String getVal_CUS_PWD_SETTING() {
		String key = getKey_CUS_PWD_SETTING();
		return getStringVal(key);
	}
	
	public String getKey_CUS_LOGIN_URL() {
		return TsmpSettingDao.Key.CUS_LOGIN_URL;
	}

	public String getVal_CUS_LOGIN_URL() {
		String key = getKey_CUS_LOGIN_URL();
		return getStringVal(key);
	}

    public String getKey_REQUEST_URI_ENABLED() {
    	return TsmpSettingDao.Key.REQUEST_URI_ENABLED;
    }
	
	public boolean getVal_REQUEST_URI_ENABLED() {
		String key = getKey_REQUEST_URI_ENABLED();
    	return getBooleanVal(key, Boolean.TRUE);
	}

	public String getKey_HIGHWAY_THRESHOLD() {
    	return TsmpSettingDao.Key.HIGHWAY_THRESHOLD;
    }
	
	public Integer getVal_HIGHWAY_THRESHOLD() {
		String key = getKey_HIGHWAY_THRESHOLD();
    	return getIntegerVal(key, 1000);
	}

	public String getKey_ES_LOGFILE_FAIL_RETRY() {
		return TsmpSettingDao.Key.ES_LOGFILE_FAIL_RETRY;
	}

	public boolean getVal_ES_LOGFILE_FAIL_RETRY() {
		String key = getKey_ES_LOGFILE_FAIL_RETRY();
		return getBooleanVal(key, false);
	}
	public String getKey_ES_CHECK_CONNECTION() {
		return TsmpSettingDao.Key.ES_CHECK_CONNECTION;
	}

	public boolean getVal_ES_CHECK_CONNECTION() {
		String key = getKey_ES_CHECK_CONNECTION();
		return getBooleanVal(key, false);
	}

	public String getKey_KIBANA_REFERER_ALLOWLIST (){
		return  TsmpSettingDao.Key.KIBANA_REFERER_ALLOWLIST ;
	}

	public  String getVal_KIBANA_REFERER_ALLOWLIST (){
		String key = getKey_KIBANA_REFERER_ALLOWLIST ();
		return getStringVal(key);
	}

}