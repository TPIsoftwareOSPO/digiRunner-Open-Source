package tpi.dgrv4.dpaa.service;

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
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.component.cipher.TsmpTAEASKHelper;
import tpi.dgrv4.entity.entity.TsmpSetting;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpCoreTokenHelperCacheProxy;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpSettingCacheProxy;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service(value = "dpaaTsmpSettingService")
public class TsmpSettingService {

	private TsmpSettingCacheProxy tsmpSettingCacheProxy;
	private TsmpTAEASKHelper tsmpTAEASKHelper;
	private TsmpCoreTokenHelperCacheProxy tsmpCoreTokenHelperCacheProxy;
	private ObjectMapper objectMapper;

	@Autowired
	public void setTsmpSettingService(TsmpSettingCacheProxy tsmpSettingCacheProxy, TsmpTAEASKHelper tsmpTAEASKHelper,
			TsmpCoreTokenHelperCacheProxy tsmpCoreTokenHelperCacheProxy, ObjectMapper objectMapper) {
		// Because using constructor injection will cause a circular dependency, use method injection instead
		this.tsmpSettingCacheProxy = tsmpSettingCacheProxy;
		this.tsmpTAEASKHelper = tsmpTAEASKHelper;
		this.tsmpCoreTokenHelperCacheProxy = tsmpCoreTokenHelperCacheProxy;
		this.objectMapper = objectMapper;
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
		if (val == null) {
			val = "";
		} // Oracle 取值會是 null
		Matcher matcher = pattern.matcher(val);// 不接受 null
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
		return getVal(id, (val) -> {
			if (!StringUtils.hasLength(val)) {
				return new String();
			}
			return val;
		});
	}

	public Integer getIntegerVal(String id, Integer defaultVal) {
		return getVal(id, (val) -> {
			if (!StringUtils.hasText(val)) {
				return defaultVal;
			}
			try {
				return Integer.valueOf(val);
			} catch (Exception e) {
				if (defaultVal != null) {
					TPILogger.tl.debug(StackTraceUtil.logStackTrace(e));
					return defaultVal;
				}
				throw e;
			}
		});
	}

	public int getIntVal(String id, int defaultVal) {
		return getIntegerVal(id, Integer.valueOf(defaultVal)).intValue();
	}

	public Long getLongVal(String id, Long defaultVal) {
		return getVal(id, (val) -> {
			if (!StringUtils.hasText(val)) {
				return defaultVal;
			}
			try {
				return Long.valueOf(val);
			} catch (Exception e) {
				if (defaultVal != null) {
					TPILogger.tl.debug(StackTraceUtil.logStackTrace(e));
					return defaultVal;
				}
				throw e;
			}
		});
	}

	public long getBasicLongVal(String id, long defaultVal) {
		return getLongVal(id, Long.valueOf(defaultVal)).longValue();
	}

	public Boolean getBooleanVal(String id, boolean defaultVal) {
		return getVal(id, (val) -> {
			if (!StringUtils.hasText(val)) {
				return defaultVal;
			}
			return Boolean.valueOf(val);
		});
	}

	public boolean getBasicBooleanVal(String id, boolean defaultVal) {
		return getBooleanVal(id, defaultVal).booleanValue();
	}

	public <R> List<R> getListVal(String id, String delimiter, Function<String, R> castFunc) {
		if (castFunc == null) {
			throw new IllegalArgumentException("Must provide casting function");
		}
		if (!StringUtils.hasLength(delimiter)) {
			delimiter = "";
		}
		String val = getStringVal(id);
		if (!StringUtils.hasLength(val)) {
			return Collections.emptyList();
		} else {
			return Arrays.asList(val.split(delimiter)).stream().map(castFunc).collect(Collectors.toList());
		}
	}

	/**
	 * 若值為"ENC()"括起來,則解密, <br>
	 * 使相關邏輯都能取到明文,不用自己解密 <br>
	 * 
	 * @return 若有做ENC加密,傳回明文; 否則,傳回DB中的原值
	 */
	private <R> R getVal(String id, Function<String, R> func) {
		TsmpSetting entity = findById(id);
		String val = entity.getValue();
		if (val == null) {
			val = "";
		} // Oracle 取值會是 null
		val = getENCPlainVal(val);// 不接受 null
		return func.apply(val);
	}

	private TsmpSetting findById(String id) {
		Optional<TsmpSetting> opt = getTsmpSettingCacheProxy().findById(id);

		if (opt.isEmpty()) {
			TPILogger.tl.debug("id=" + id);
			throw TsmpDpAaRtnCode.NO_ITEMS_DATA.throwing();
		}

		return opt.get();
	}

	protected TsmpSettingCacheProxy getTsmpSettingCacheProxy() {
		return this.tsmpSettingCacheProxy;
	}

	protected TsmpTAEASKHelper getTsmpTAEASKHelper() {
		return this.tsmpTAEASKHelper;
	}

	protected TsmpCoreTokenHelperCacheProxy getTsmpCoreTokenHelperCacheProxy() {
		return this.tsmpCoreTokenHelperCacheProxy;
	}

	protected ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}

	// =========================================================
	// ==================== CUSTOM METHODS =====================
	// =========================================================


	public String getKey_AUDIT_LOG_RETENTION_DAYS() {
		return TsmpSettingDao.Key.AUDIT_LOG_RETENTION_DAYS;
	}

	public int getVal_AUDIT_LOG_RETENTION_DAYS() {
		String key = getKey_AUDIT_LOG_RETENTION_DAYS();
		return getIntegerVal(key, 0);
	}

	public String getKey_TSMP_EDITION() {
		return TsmpSettingDao.Key.TSMP_EDITION;
	}

	public String getVal_TSMP_LICENSE_KEY() {
		String key = getKey_TSMP_EDITION();
		return getStringVal(key);
	}

	public String getKey_TSMP_DPAA_RUNLOOP_INTERVAL() {
		return TsmpSettingDao.Key.TSMP_DPAA_RUNLOOP_INTERVAL;
	}

	public int getVal_TSMP_DPAA_RUNLOOP_INTERVAL() {
		String key = getKey_TSMP_DPAA_RUNLOOP_INTERVAL();
		return getIntegerVal(key, 1);
	}

	public String getKey_TSMP_SYS_TYPE() {
		return TsmpSettingDao.Key.TSMP_SYS_TYPE;
	}

	public String getVal_TSMP_SYS_TYPE() {
		String key = getKey_TSMP_SYS_TYPE();
		return getStringVal(key);
	}

	public String getKey_OAK_EXPI_URL() {
		return TsmpSettingDao.Key.OAK_EXPI_URL;
	}

	public String getVal_OAK_EXPI_URL() {
		String gateway_key = getKey_OAK_EXPI_URL();
		return getStringVal(gateway_key);
	}

	public String getKey_TSMP_SIGNBLOCK_EXPIRED() {
		return TsmpSettingDao.Key.TSMP_SIGNBLOCK_EXPIRED;
	}

	public int getVal_TSMP_SIGNBLOCK_EXPIRED() {
		String key = getKey_TSMP_SIGNBLOCK_EXPIRED();
		int val = getIntVal(key, 24);// 24小時
		return val;
	}

	public String getKey_TSMP_AC_CLIENT_ID() {
		return TsmpSettingDao.Key.TSMP_AC_CLIENT_ID;
	}

	public String getVal_TSMP_AC_CLIENT_ID() {
		String key = getKey_TSMP_AC_CLIENT_ID();
		return getStringVal(key);
	}

	public String getKey_TSMP_AC_CLIENT_PW() {
		return TsmpSettingDao.Key.TSMP_AC_CLIENT_PW;
	}

	public String getVal_TSMP_AC_CLIENT_PW() {
		String key = getKey_TSMP_AC_CLIENT_PW();
		return getStringVal(key);
	}

	public String getKey_TSMP_FAIL_THRESHOLD() {
		return TsmpSettingDao.Key.TSMP_FAIL_THRESHOLD;
	}

	public int getVal_TSMP_FAIL_THRESHOLD() {
		String key = getKey_TSMP_FAIL_THRESHOLD();
		return getIntegerVal(key, 6);
	}

	public String getKey_AUDIT_LOG_ENABLE() {
		return TsmpSettingDao.Key.AUDIT_LOG_ENABLE;
	}

	public boolean getVal_AUDIT_LOG_ENABLE() {
		String key = getKey_AUDIT_LOG_ENABLE();
		return getBooleanVal(key, false);
	}

	public String getKey_SSO_PKCE() {
		return TsmpSettingDao.Key.SSO_PKCE;
	}

	public boolean getVal_SSO_PKCE() {
		String key = getKey_SSO_PKCE();
		return getBooleanVal(key, false);
	}

	public String getKey_SSO_TIMEOUT() {
		return TsmpSettingDao.Key.SSO_TIMEOUT;
	}

	public Integer getVal_SSO_TIMEOUT() {
		String key = getKey_SSO_TIMEOUT();
		return getIntegerVal(key, 10);
	}

	public String getKey_SSO_DOUBLE_CHECK() {
		return TsmpSettingDao.Key.SSO_DOUBLE_CHECK;
	}

	public boolean getVal_SSO_DOUBLE_CHECK() {
		String key = getKey_SSO_DOUBLE_CHECK();
		return getBooleanVal(key, false);
	}

	public String getKey_SSO_AUTO_CREATE_USER() {
		return TsmpSettingDao.Key.SSO_AUTO_CREATE_USER;
	}

	public boolean getVal_SSO_AUTO_CREATE_USER() {
		String key = getKey_SSO_AUTO_CREATE_USER();
		return getBooleanVal(key, false);
	}

	public String getKey_LDAP_URL() {
		return TsmpSettingDao.Key.LDAP_URL;
	}

	public String getVal_LDAP_URL() {
		String key = getKey_LDAP_URL();
		return getStringVal(key);
	}

	public String getKey_LDAP_DN() {
		return TsmpSettingDao.Key.LDAP_DN;
	}

	public String getVal_LDAP_DN() {
		String key = getKey_LDAP_DN();
		return getStringVal(key);
	}

	public String getKey_LDAP_TIMEOUT() {
		return TsmpSettingDao.Key.LDAP_TIMEOUT;
	}

	public String getVal_LDAP_TIMEOUT() {
		String key = getKey_LDAP_TIMEOUT();
		return getStringVal(key);
	}

	public String getKey_LDAP_CHECK_ACCT_ENABLE() {
		return TsmpSettingDao.Key.LDAP_CHECK_ACCT_ENABLE;
	}

	public boolean getVal_LDAP_CHECK_ACCT_ENABLE() {
		String key = getKey_LDAP_CHECK_ACCT_ENABLE();
		return getBooleanVal(key, false);
	}

	public String getKey_SERVICE_MAIL_ENABLE() {
		return TsmpSettingDao.Key.SERVICE_MAIL_ENABLE;
	}

	public String getVal_SERVICE_MAIL_ENABLE() {
		String key = getKey_SERVICE_MAIL_ENABLE();
		return getStringVal(key);
	}

	public String getKey_SERVICE_MAIL_HOST() {
		return TsmpSettingDao.Key.SERVICE_MAIL_HOST;
	}

	public String getVal_SERVICE_MAIL_HOST() {
		String key = getKey_SERVICE_MAIL_HOST();
		return getStringVal(key);
	}

	public String getKey_SERVICE_MAIL_PORT() {
		return TsmpSettingDao.Key.SERVICE_MAIL_PORT;
	}

	public String getVal_SERVICE_MAIL_PORT() {
		String key = getKey_SERVICE_MAIL_PORT();
		return getStringVal(key);
	}

	public String getKey_SERVICE_MAIL_AUTH() {
		return TsmpSettingDao.Key.SERVICE_MAIL_AUTH;
	}

	public String getVal_SERVICE_MAIL_AUTH() {
		String key = getKey_SERVICE_MAIL_AUTH();
		return getStringVal(key);
	}

	public String getKey_SERVICE_MAIL_STARTTLS_ENABLE() {
		return TsmpSettingDao.Key.SERVICE_MAIL_STARTTLS_ENABLE;
	}

	public String getVal_SERVICE_MAIL_STARTTLS_ENABLE() {
		String key = getKey_SERVICE_MAIL_STARTTLS_ENABLE();
		return getStringVal(key);
	}

	public String getKey_SERVICE_MAIL_USERNAME() {
		return TsmpSettingDao.Key.SERVICE_MAIL_USERNAME;
	}

	public String getVal_SERVICE_MAIL_USERNAME() {
		String key = getKey_SERVICE_MAIL_USERNAME();
		return getStringVal(key);
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
		String key = getKey_SERVICE_MAIL_FROM();
		return getStringVal(key);
	}

	public String getKey_SERVICE_MAIL_X_MAILER() {
		return TsmpSettingDao.Key.SERVICE_MAIL_X_MAILER;
	}

	public String getVal_SERVICE_MAIL_X_MAILER() {
		String key = getKey_SERVICE_MAIL_X_MAILER();
		return getStringVal(key);
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
		String key = getKey_SERVICE_SECONDARY_MAIL_HOST();
		return getStringVal(key);
	}

	public String getKey_SERVICE_SECONDARY_MAIL_PORT() {
		return TsmpSettingDao.Key.SERVICE_SECONDARY_MAIL_PORT;
	}

	public String getVal_SERVICE_SECONDARY_MAIL_PORT() {
		String key = getKey_SERVICE_SECONDARY_MAIL_PORT();
		return getStringVal(key);
	}

	public String getKey_SERVICE_SECONDARY_MAIL_AUTH() {
		return TsmpSettingDao.Key.SERVICE_SECONDARY_MAIL_AUTH;
	}

	public String getVal_SERVICE_SECONDARY_MAIL_AUTH() {
		String key = getKey_SERVICE_SECONDARY_MAIL_AUTH();
		return getStringVal(key);
	}

	public String getKey_SERVICE_SECONDARY_MAIL_STARTTLS_ENABLE() {
		return TsmpSettingDao.Key.SERVICE_SECONDARY_MAIL_STARTTLS_ENABLE;
	}

	public String getVal_SERVICE_SECONDARY_MAIL_STARTTLS_ENABLE() {
		String key = getKey_SERVICE_SECONDARY_MAIL_STARTTLS_ENABLE();
		return getStringVal(key);
	}

	public String getKey_SERVICE_SECONDARY_MAIL_USERNAME() {
		return TsmpSettingDao.Key.SERVICE_SECONDARY_MAIL_USERNAME;
	}

	public String getVal_SERVICE_SECONDARY_MAIL_USERNAME() {
		String key = getKey_SERVICE_SECONDARY_MAIL_USERNAME();
		return getStringVal(key);
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
		String key = getKey_SERVICE_SECONDARY_MAIL_FROM();
		return getStringVal(key);
	}

	public String getKey_SERVICE_SECONDARY_MAIL_X_MAILER() {
		return TsmpSettingDao.Key.SERVICE_SECONDARY_MAIL_X_MAILER;
	}

	public String getVal_SERVICE_SECONDARY_MAIL_X_MAILER() {
		String key = getKey_SERVICE_SECONDARY_MAIL_X_MAILER();
		return getStringVal(key);
	}

	public String getKey_LOGOUT_API() {
		return TsmpSettingDao.Key.LOGOUT_API;
	}

	public String getVal_LOGOUT_API() {
		String key = getKey_LOGOUT_API();
		return getStringVal(key);
	}

	public String getKey_TSMP_COMPOSER_ADDRESS() {
		return TsmpSettingDao.Key.TSMP_COMPOSER_ADDRESS;
	}

	public List<String> getVal_TSMP_COMPOSER_ADDRESS() {
		String key = getKey_TSMP_COMPOSER_ADDRESS();
		return getListVal(key, ",", Function.identity());
	}

	public String getKey_TSMP_CORE_V2_MODULE_FILE_UPLOAD_PATH() {
		return TsmpSettingDao.Key.TSMP_CORE_V2_MODULE_FILE_UPLOAD_PATH;
	}

	public String getVal_TSMP_CORE_V2_MODULE_FILE_UPLOAD_PATH() {
		String key = getKey_TSMP_CORE_V2_MODULE_FILE_UPLOAD_PATH();
		return getStringVal(key);
	}

	public String getKey_TSMP_CORE_V3_MODULE_FILE_UPLOAD_PATH() {
		return TsmpSettingDao.Key.TSMP_CORE_V3_MODULE_FILE_UPLOAD_PATH;
	}

	public String getVal_TSMP_CORE_V3_MODULE_FILE_UPLOAD_PATH() {
		String key = getKey_TSMP_CORE_V3_MODULE_FILE_UPLOAD_PATH();
		return getStringVal(key);
	}

	public String getKey_DELETEMODULE_ALERT() {
		return TsmpSettingDao.Key.DELETEMODULE_ALERT;
	}

	public boolean getVal_DELETEMODULE_ALERT() {
		String key = getKey_DELETEMODULE_ALERT();
		return getBooleanVal(key, false);
	}

	public String getKey_TSMP_AC_CONF() {
		return TsmpSettingDao.Key.TSMP_AC_CONF;
	}

	public Map<String, Object> getVal_TSMP_AC_CONF() {
		String key = getKey_TSMP_AC_CONF();
		return getVal(key, (val) -> {
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("dp", 0);
			args.put("net", false);
			try {
				args = getObjectMapper().readValue(val, //
						new TypeReference<Map<String, Object>>() {
						}); // converts JSON to Map
			} catch (Exception e) {
				TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			}
			return args;
		});
	}

	public String getKey_TSMP_APILOG_FORCE_WRITE_RDB() {
		return TsmpSettingDao.Key.TSMP_APILOG_FORCE_WRITE_RDB;
	}

	public String getVal_TSMP_APILOG_FORCE_WRITE_RDB() {
		String key = getKey_TSMP_APILOG_FORCE_WRITE_RDB();
		return getStringVal(key);
	}

	public String getKey_DGR_LOGOUT_URL() {
		return TsmpSettingDao.Key.DGR_LOGOUT_URL;
	}

	public String getVal_DGR_LOGOUT_URL() {
		String key = getKey_DGR_LOGOUT_URL();
		return getStringVal(key);
	}
	
	public String getKey_BOT_DETECTION_LOG() {
		return TsmpSettingDao.Key.BOT_DETECTION_LOG;
	}

	public String getVal_BOT_DETECTION_LOG() {
		String key = getKey_BOT_DETECTION_LOG();
		return getStringVal(key);
	}

	public String getKey_TSMP_COMPOSER_PORT() {
		return TsmpSettingDao.Key.TSMP_COMPOSER_PORT;
	}

	/**
	 * 給前端頁面使用用的路徑，主要是給AC使用
	 * 
	 * @return
	 */
	public int getVal_TSMP_COMPOSER_PORT() {
		String key = getKey_TSMP_COMPOSER_PORT();
		return getIntVal(key, 1880);
	}

	public String getKey_TSMP_COMPOSER_PATH() {
		return TsmpSettingDao.Key.TSMP_COMPOSER_PATH;
	}

	/**
	 * 給前端頁面使用用的路徑，主要是給AC使用
	 * 
	 * @return
	 */
	public String getVal_TSMP_COMPOSER_PATH() {
		String key = getKey_TSMP_COMPOSER_PATH();
		String path = getStringVal(key);
		if (StringUtils.isEmpty(path)) {
			return "/editor/tsmpApi";
		} else {
			return path;
		}
	}

	public String getKey_TSMP_PROXY_PORT() {
		return TsmpSettingDao.Key.TSMP_PROXY_PORT;
	}

	/**
	 * 目前無使用，保留，當初功能性與 TSMP_REPORT_ADDRESS 相同
	 * 
	 * @return
	 */
	public int getVal_TSMP_PROXY_PORT() {
		String key = getKey_TSMP_PROXY_PORT();
		return getIntVal(key, 4944);
	}

	public String getKey_CUS_MODULE_EXIST() {
		return TsmpSettingDao.Key.CUS_MODULE_EXIST;
	}

	public boolean getVal_CUS_MODULE_EXIST() {
		String key = getKey_CUS_MODULE_EXIST();
		return getBooleanVal(key, false);
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

	public String getKey_CUS_MODULE_NAME() {
		return TsmpSettingDao.Key.CUS_MODULE_NAME;
	}

	public String getVal_CUS_MODULE_NAME() {
		String key = getKey_CUS_MODULE_NAME();
		return getStringVal(key);
	}

	public String getVal_CUS_MODULE_NAME(int index) {
		String key = getKey_CUS_MODULE_NAME();
		key = (index < 0 ? key : (key + index));
		return getStringVal(key);
	}

	public String getKey_UDPSSO_LOGIN_NETWORK() {
		return TsmpSettingDao.Key.UDPSSO_LOGIN_NETWORK;
	}

	public String getVal_UDPSSO_LOGIN_NETWORK() {
		String key = getKey_UDPSSO_LOGIN_NETWORK();
		return getStringVal(key);
	}

	public boolean getVal_TSMP_ONLINE_CONSOLE() {
		String key = getKey_TSMP_ONLINE_CONSOLE();
		return getBooleanVal(key, Boolean.TRUE);
	}

	public String getKey_TSMP_ONLINE_CONSOLE() {
		return TsmpSettingDao.Key.TSMP_ONLINE_CONSOLE;
	}

	public String getKey_DGR_QUERY_MONITOR_DAY() {
		return TsmpSettingDao.Key.DGR_QUERY_MONITOR_DAY;
	}

	public int getVal_DGR_QUERY_MONITOR_DAY() {
		String key = getKey_DGR_QUERY_MONITOR_DAY();
		return getIntVal(key, 7);
	}

	public String getKey_DEFAULT_PAGE_SIZE() {
		return TsmpSettingDao.Key.DEFAULT_PAGE_SIZE;
	}

	public Integer getVal_DEFAULT_PAGE_SIZE() {
		String key = getKey_DEFAULT_PAGE_SIZE();
		return getIntegerVal(key, 20);
	}

	public String getKey_FILE_TEMP_EXP_TIME() {
		return TsmpSettingDao.Key.FILE_TEMP_EXP_TIME;
	}

	public Long getVal_FILE_TEMP_EXP_TIME() {
		String key = getKey_FILE_TEMP_EXP_TIME();
		return getBasicLongVal(key, 3600000L);
	}

	public String getKey_MAIL_BODY_API_FAIL_SERVICE_MAIL() {
		return TsmpSettingDao.Key.MAIL_BODY_API_FAIL_SERVICE_MAIL;
	}

	public String getVal_MAIL_BODY_API_FAIL_SERVICE_MAIL() {
		String key = getKey_MAIL_BODY_API_FAIL_SERVICE_MAIL();
		return getStringVal(key);
	}

	public String getKey_MAIL_BODY_API_FAIL_SERVICE_TEL() {
		return TsmpSettingDao.Key.MAIL_BODY_API_FAIL_SERVICE_TEL;
	}

	public String getVal_MAIL_BODY_API_FAIL_SERVICE_TEL() {
		String key = getKey_MAIL_BODY_API_FAIL_SERVICE_TEL();
		return getStringVal(key);
	}

	public String getKey_ERRORLOG_KEYWORD() {
		return TsmpSettingDao.Key.ERRORLOG_KEYWORD;
	}

	public String getVal_ERRORLOG_KEYWORD() {
		String key = getKey_ERRORLOG_KEYWORD();
		return getStringVal(key);
	}

	public String getKey_AUTH_CODE_EXP_TIME() {
		return TsmpSettingDao.Key.AUTH_CODE_EXP_TIME;
	}

	public String getVal_AUTH_CODE_EXP_TIME() {
		String key = getKey_AUTH_CODE_EXP_TIME();
		return getStringVal(key);
	}

	public String getKey_JWKS_URI() {
		return TsmpSettingDao.Key.JWKS_URI;
	}

	public String getVal_JWKS_URI() {
		String key = getKey_JWKS_URI();
		return getStringVal(key);
	}

	public String getKey_DP_ADMIN() {
		return TsmpSettingDao.Key.DP_ADMIN;
	}

	public String getVal_DP_ADMIN() {
		String key = getKey_DP_ADMIN();
		return getStringVal(key);
	}

	public String getKey_QUERY_DURATION() {
		return TsmpSettingDao.Key.QUERY_DURATION;
	}

	public String getVal_QUERY_DURATION() {
		String key = getKey_QUERY_DURATION();
		return getStringVal(key);
	}

	public String getKey_MAIL_SEND_TIME() {
		return TsmpSettingDao.Key.MAIL_SEND_TIME;
	}

	public String getVal_MAIL_SEND_TIME() {
		String key = getKey_MAIL_SEND_TIME();
		return getStringVal(key);
	}

	public String getKey_DGR_PATHS_COMPATIBILITY() {
		return TsmpSettingDao.Key.DGR_PATHS_COMPATIBILITY;
	}

	public int getVal_DGR_PATHS_COMPATIBILITY() {
		String key = getKey_DGR_PATHS_COMPATIBILITY();
		return getIntVal(key, 2);
	}

	public String getKey_PROFILEUPDATE_INVALIDATE_TOKEN() {
		return TsmpSettingDao.Key.PROFILEUPDATE_INVALIDATE_TOKEN;
	}

	public boolean getVal_PROFILEUPDATE_INVALIDATE_TOKEN() {
		String key = getKey_PROFILEUPDATE_INVALIDATE_TOKEN();
		return getBooleanVal(key, false);
	}

	public String getKey_USER_UPDATE_BY_SELF() {
		return TsmpSettingDao.Key.USER_UPDATE_BY_SELF;
	}

	public boolean getVal_USER_UPDATE_BY_SELF() {
		String key = getKey_USER_UPDATE_BY_SELF();
		return getBooleanVal(key, false);
	}

	public String getKey_API_DASHBOARD_BATCH_QUANTITY() {
		return TsmpSettingDao.Key.API_DASHBOARD_BATCH_QUANTITY;
	}

	public int getVal_API_DASHBOARD_BATCH_QUANTITY() {
		String key = getKey_API_DASHBOARD_BATCH_QUANTITY();
		return getIntVal(key, 500000);
	}

	public String getKey_CHECK_BOT_DETECTION() {
		return TsmpSettingDao.Key.CHECK_BOT_DETECTION;
	}

	public String getVal_CHECK_BOT_DETECTION() {
		String key = getKey_CHECK_BOT_DETECTION();
		return getStringVal(key);
	}

	// X_API_KEY_PLAIN_ENABLE
	public String getKey_X_API_KEY_PLAIN_ENABLE() {
		return TsmpSettingDao.Key.X_API_KEY_PLAIN_ENABLE;
	}

	public boolean getVal_X_API_KEY_PLAIN_ENABLE() {
		String key = getKey_X_API_KEY_PLAIN_ENABLE();
		return getBooleanVal(key, false);
	}

	public String getKey_DEFAULT_DATA_CHANGE_ENABLED() {
		return TsmpSettingDao.Key.DEFAULT_DATA_CHANGE_ENABLED;
	}

	public boolean getVal_DEFAULT_DATA_CHANGE_ENABLED() {
		String key = getKey_DEFAULT_DATA_CHANGE_ENABLED();
		return getBooleanVal(key, false);
	}
	
	public String getKey_APIM_DEFAULT_DB_ENABLED() {
		return TsmpSettingDao.Key.APIM_DEFAULT_DB_ENABLED;
	}

	public boolean getVal_APIM_DEFAULT_DB_ENABLED() {
		String key = getKey_APIM_DEFAULT_DB_ENABLED();
		return getBooleanVal(key, false);
	}

	public String getKey_LOG_RETENTION_DAYS() {
		return TsmpSettingDao.Key.LOG_RETENTION_DAYS;
	}

	public String getVal_LOG_RETENTION_DAYS() {
		String key = getKey_LOG_RETENTION_DAYS();
		return getStringVal(key);
	}
	
	public String getKey_FIRST_TIME_LOGIN_CHANGE_MIMA_ENABLED() {
		return TsmpSettingDao.Key.FIRST_TIME_LOGIN_CHANGE_MIMA_ENABLED;
	}

	public boolean getVal_FIRST_TIME_LOGIN_CHANGE_MIMA_ENABLED() {
		String key = getKey_FIRST_TIME_LOGIN_CHANGE_MIMA_ENABLED();
		return getBooleanVal(key, false);
	}
	
	public String getKey_PWD_STRENGTH() {
		return TsmpSettingDao.Key.PWD_STRENGTH;
	}

	public String getVal_PWD_STRENGTH() {
		String key = getKey_PWD_STRENGTH();
		return getStringVal(key);
	}
	
	public String getKey_PWD_STRENGTH_DESC() {
		return TsmpSettingDao.Key.PWD_STRENGTH_DESC;
	}

	public String getVal_PWD_STRENGTH_DESC() {
		String key = getKey_PWD_STRENGTH_DESC();
		return getStringVal(key);
	}
	
	public String getKey_CLIENT_PWD_STRENGTH() {
		return TsmpSettingDao.Key.CLIENT_PWD_STRENGTH;
	}

	public String getVal_CLIENT_PWD_STRENGTH() {
		String key = getKey_CLIENT_PWD_STRENGTH();
		return getStringVal(key);
	}
	
	public String getKey_CLIENT_PWD_STRENGTH_DESC() {
		return TsmpSettingDao.Key.CLIENT_PWD_STRENGTH_DESC;
	}

	public String getVal_CLIENT_PWD_STRENGTH_DESC() {
		String key = getKey_CLIENT_PWD_STRENGTH_DESC();
		return getStringVal(key);
	}
	
}
