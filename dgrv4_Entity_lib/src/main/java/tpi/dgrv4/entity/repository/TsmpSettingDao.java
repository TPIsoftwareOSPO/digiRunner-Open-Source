package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.TsmpSetting;

@Repository
public interface TsmpSettingDao extends JpaRepository<TsmpSetting, String> {

	public static class Key {
		public static final String TSMP_ONLINE_CONSOLE = "TSMP_ONLINE_CONSOLE";
		public static final String DGR_PATHS_COMPATIBILITY = "DGR_PATHS_COMPATIBILITY";
		public static final String CUS_CLIENT_ID = "CUS_CLIENT_ID";
		public static final String CUS_CLIENT_OPEN_API_KEY = "CUS_CLIENT_OPEN_API_KEY";

		// LDAP
		public static final String LDAP_URL = "LDAP_URL";
		public static final String LDAP_DN = "LDAP_DN";
		public static final String LDAP_TIMEOUT = "LDAP_TIMEOUT";
		public static final String LDAP_CHECK_ACCT_ENABLE = "LDAP_CHECK_ACCT_ENABLE";

		public static final String SSO_DOUBLE_CHECK = "SSO_DOUBLE_CHECK";

		// Audit Log
		public static final String AUDIT_LOG_ENABLE = "AUDIT_LOG_ENABLE";

		// 檢查器
		public static final String CHECK_XSS_ENABLE = "CHECK_XSS_ENABLE";
		public static final String CHECK_XXE_ENABLE = "CHECK_XXE_ENABLE";
		public static final String CHECK_SQL_INJECTION_ENABLE = "CHECK_SQL_INJECTION_ENABLE";
		public static final String CHECK_IGNORE_API_PATH_ENABLE = "CHECK_IGNORE_API_PATH_ENABLE";
		public static final String CHECK_API_STATUS_ENABLE = "CHECK_API_STATUS_ENABLE";
		public static final String CHECK_TRAFFIC_ENABLE = "CHECK_TRAFFIC_ENABLE";
		public static final String CHECK_JTI_ENABLE = "CHECK_JTI_ENABLE";

		public static final String IGNORE_API_PATH = "IGNORE_API_PATH";

		public static final String DGRKEEPER_IP = "DGRKEEPER_IP";
		public static final String DGRKEEPER_PORT = "DGRKEEPER_PORT";

		public static final String DPKEEPER_IP = "DPKEEPER_IP";
		public static final String DPKEEPER_PORT = "DPKEEPER_PORT";

		public static final String LOGGER_LEVEL = "LOGGER_LEVEL";

		// ES
		public static final String ES_URL = "ES_URL";
		public static final String ES_ID_PWD = "ES_ID_PWD";
		public static final String ES_TEST_TIMEOUT = "ES_TEST_TIMEOUT";
		public static final String ES_MBODY_MASK_FLAG = "ES_MBODY_MASK_FLAG";
		public static final String ES_IGNORE_API = "ES_IGNORE_API";
		public static final String ES_MBODY_MASK_API = "ES_MBODY_MASK_API";
		public static final String ES_TOKEN_MASK_FLAG = "ES_TOKEN_MASK_FLAG";
		public static final String ES_MAX_SIZE_MBODY_MASK = "ES_MAX_SIZE_MBODY_MASK";
		public static final String ES_DGRC_MBODY_MASK_URI = "ES_DGRC_MBODY_MASK_URI";
		public static final String ES_DGRC_IGNORE_URI = "ES_DGRC_IGNORE_URI";
		public static final String ES_LOG_DISABLE = "ES_LOG_DISABLE";
		public static final String ES_SYS_TYPE = "ES_SYS_TYPE";
		public static final String ES_MONITOR_DISABLE = "ES_MONITOR_DISABLE";

		// COMPOSER_ADDRESS
		public static final String TSMP_COMPOSER_ADDRESS = "TSMP_COMPOSER_ADDRESS";

		// Token
		public static final String DGR_TOKEN_JWE_ENABLE = "DGR_TOKEN_JWE_ENABLE";
		public static final String DGR_TOKEN_WHITELIST_ENABLE = "DGR_TOKEN_WHITELIST_ENABLE";

		// API
		public static final String DGR_TW_FAPI_ENABLE = "DGR_TW_FAPI_ENABLE";

		public static final String TSMP_SYS_TYPE = "TSMP_SYS_TYPE";
		public static final String TSMP_NODE_TPS = "TSMP_NODE_TPS";
		public static final String TSMP_MONITOR_HEARTBEAT = "TSMP_MONITOR_HEARTBEAT";
		public static final String TSMP_MONITOR_LOADING = "TSMP_MONITOR_LOADING";
		public static final String TSMP_MONITOR_STATUS = "TSMP_MONITOR_STATUS";
		public static final String TSMP_CACHE_REFRESH = "TSMP_CACHE_REFRESH";
		public static final String TSMP_THREAD_POOL_ARGS = "TSMP_THREAD_POOL_ARGS";
		public static final String TSMP_AL_THREAD_POOL_ARGS = "TSMP_AL_THREAD_POOL_ARGS";
		public static final String TSMP_AC_CLIENT_ID = "TSMP_AC_CLIENT_ID";
		public static final String TSMP_AC_CLIENT_PW = "TSMP_AC_CLIENT_PW";
		public static final String TSMP_REPORT_ADDRESS = "TSMP_REPORT_ADDRESS"; // ip:port
		public static final String TSMP_APILOG_FILE = "TSMP_APILOG_FILE"; //
		public static final String TSMP_APILOG_MONGO = "TSMP_APILOG_MONGO"; //
		public static final String TSMP_APILOG_ES = "TSMP_APILOG_ES"; // elasticsearch
		public static final String TSMP_TOKEN_EXPIRED = "TSMP_TOKEN_EXPIRED"; //
		public static final String TSMP_REF_TOKEN_EXPIRED = "TSMP_REF_TOKEN_EXPIRED";
		public static final String TSMP_TOKEN_PWD = "TSMP_TOKEN_PWD"; //
		public static final String TSMP_COMPOSER_URL = "TSMP_COMPOSER_URL"; //
		public static final String TSMP_COMPOSER_PORT = "TSMP_COMPOSER_PORT"; //
		public static final String TSMP_COMPOSER_PATH = "TSMP_COMPOSER_PATH"; //
		public static final String TSMP_PROXY_PORT = "TSMP_PROXY_PORT"; //

		public static final String TSMP_ES_EXCHANGE_URL = "TSMP_ES_EXCHANGE_URL"; // elasticsearch ALERT
		public static final String TSMP_ES_QUERY_MAXCOUNT = "TSMP_ES_QUERY_MAXCOUNT"; // elasticsearch query count
		public static final String TSMP_ES_LDAP_URL = "TSMP_ES_LDAP_URL"; // LDAP

		public static final String TSMP_SIGNBLOCK_EXPIRED = "TSMP_SIGNBLOCK_EXPIRED";

		public static final String KD_TSMP_NODE = "KD_TSMP_NODE";
		public static final String KD_TSMP_NODE_TASK = "KD_TSMP_NODE_TASK";
		public static final String KD_TSMP_NODE_TASK_WORK = "KD_TSMP_NODE_TASK_WORK";
		public static final String KD_TSMP_TXTOKEN = "KD_TSMP_TXTOKEN";
		public static final String KD_TSMP_TXKEY = "KD_TSMP_TXKEY";
		public static final String KD_TSMP_CLIENT_LOG = "KD_TSMP_CLIENT_LOG";

		public static final String RP_TPS_OFFSET = "RP_TPS_OFFSET";
		public static final String RP_IGNORE_TPS_FLOOR = "RP_IGNORE_TPS_FLOOR";

		public static final String TSMP_AC_CONF = "TSMP_AC_CONF";
		public static final String TSMP_EDITION = "TSMP_EDITION";

		public static final String TSMP_REGHOST_DOWN = "TSMP_REGHOST_DOWN";
		public static final String TSMP_REGHOST_CACHE = "TSMP_REGHOST_CACHE";

		public static final String RESET_USER_BLOCK_SUBJECT = "RESET_USER_BLOCK_SUBJECT";
		public static final String RESET_CLIENT_BLOCK_SUBJECT = "RESET_CLIENT_BLOCK_SUBJECT";
		public static final String CREATE_USER__SUBJECT = "CREATE_USER_SUBJECT";
		public static final String CREATE_CLIENT_SUBJECT = "CREATE_CLIENT_SUBJECT";

		public static final String TSMP_APILOG_FORCE_WRITE_RDB = "TSMP_APILOG_FORCE_WRITE_RDB";

		public static final String TSMP_CORE_V3_MODULE_FILE_UPLOAD_PATH = "TSMP_CORE_V3_MODULE_FILE_UPLOAD_PATH";
		public static final String TSMP_CORE_V2_MODULE_FILE_UPLOAD_PATH = "TSMP_CORE_V2_MODULE_FILE_UPLOAD_PATH";

		public static final String CUS_MODULE_EXIST = "CUS_MODULE_EXIST";
		public static final String CUS_MODULE_NAME = "CUS_MODULE_NAME";
		public static final String CUS_FUNC_ENABLE = "CUS_FUNC_ENABLE";
		public static final String CUS_MODULE_NAME1 = "CUS_MODULE_NAME1";

		// SSO
		public static final String SSO_PKCE = "SSO_PKCE";
		public static final String SSO_AUTO_CREATE_USER = "SSO_AUTO_CREATE_USER";
		public static final String SSO_TIMEOUT = "SSO_TIMEOUT";

		// UDPSSO
		public static final String UDPSSO_LOGIN_NETWORK = "UDPSSO_LOGIN_NETWORK";

		// SSO AC IdP
		public static final String AC_IDP_REVIEWER_MAILLIST = "AC_IDP_REVIEWER_MAILLIST";
		public static final String AC_IDP_REVIEW_URL = "AC_IDP_REVIEW_URL";
		public static final String AC_IDP_MSG_URL = "AC_IDP_MSG_URL";
		public static final String AC_IDP_ACCALLBACK_URL = "AC_IDP_ACCALLBACK_URL";
		public static final String AC_IDP_LDAP_REVIEW_ENABLE = "AC_IDP_LDAP_REVIEW_ENABLE";
		public static final String AC_IDP_API_REVIEW_ENABLE = "AC_IDP_API_REVIEW_ENABLE";
		public static final String AC_IDP_CUS_REVIEW_ENABLE = "AC_IDP_CUS_REVIEW_ENABLE";
		public static final String AC_IDP_OAUTH2_REVIEW_ENABLE = "AC_IDP_OAUTH2_REVIEW_ENABLE";
		public static final String AC_IDP_OAUTH2_USERNAME = "AC_IDP_OAUTH2_USERNAME";
		public static final String AC_IDP_LOGIN_IGNORE_TYPE = "AC_IDP_LOGIN_IGNORE_TYPE";
		public static final String AC_IDP_USERNAME_B64_ENCODE = "AC_IDP_USERNAME_B64_ENCODE";

		// Gateway IdP
		public static final String GTW_IDP_JWK1 = "GTW_IDP_JWK1";
		public static final String GTW_IDP_JWK2 = "GTW_IDP_JWK2";
		public static final String GTW_IDP_MSG_URL = "GTW_IDP_MSG_URL";
		public static final String GTW_IDP_LOGIN_URL = "GTW_IDP_LOGIN_URL";
		public static final String GTW_IDP_CONSENT_URL = "GTW_IDP_CONSENT_URL";

		public static final String DGR_PUBLIC_DOMAIN = "DGR_PUBLIC_DOMAIN";
		public static final String DGR_PUBLIC_PORT = "DGR_PUBLIC_PORT";

		public static final String DGR_COOKIE_TOKEN_ENABLE = "DGR_COOKIE_TOKEN_ENABLE";

		public static final String DELETEMODULE_ALERT = "TSMP_DELETEMODULE_ALERT";

		public static final String TSMP_DPAA_RUNLOOP_INTERVAL = "TSMP_DPAA_RUNLOOP_INTERVAL";

		public static final String TSMP_FAIL_THRESHOLD = "TSMP_FAIL_THRESHOLD";

		// MAIL SERVER
		public static final String SERVICE_MAIL_ENABLE = "SERVICE_MAIL_ENABLE";
		public static final String SERVICE_MAIL_HOST = "SERVICE_MAIL_HOST";
		public static final String SERVICE_MAIL_PORT = "SERVICE_MAIL_PORT";
		public static final String SERVICE_MAIL_AUTH = "SERVICE_MAIL_AUTH";
		public static final String SERVICE_MAIL_STARTTLS_ENABLE = "SERVICE_MAIL_STARTTLS_ENABLE";
		public static final String SERVICE_MAIL_USERNAME = "SERVICE_MAIL_USERNAME";
		public static final String SERVICE_MAIL_PASSWORD = "SERVICE_MAIL_PASSWORD";
		public static final String SERVICE_MAIL_FROM = "SERVICE_MAIL_FROM";
		public static final String SERVICE_MAIL_X_MAILER = "SERVICE_MAIL_X_MAILER";

		// SECONDARY MAIL SERVER
		public static final String SERVICE_SECONDARY_MAIL_ENABLE = "SERVICE_SECONDARY_MAIL_ENABLE";
		public static final String SERVICE_SECONDARY_MAIL_HOST = "SERVICE_SECONDARY_MAIL_HOST";
		public static final String SERVICE_SECONDARY_MAIL_PORT = "SERVICE_SECONDARY_MAIL_PORT";
		public static final String SERVICE_SECONDARY_MAIL_AUTH = "SERVICE_SECONDARY_MAIL_AUTH";
		public static final String SERVICE_SECONDARY_MAIL_STARTTLS_ENABLE = "SERVICE_SECONDARY_MAIL_STARTTLS_ENABLE";
		public static final String SERVICE_SECONDARY_MAIL_USERNAME = "SERVICE_SECONDARY_MAIL_USERNAME";
		public static final String SERVICE_SECONDARY_MAIL_PASSWORD = "SERVICE_SECONDARY_MAIL_PASSWORD";
		public static final String SERVICE_SECONDARY_MAIL_FROM = "SERVICE_SECONDARY_MAIL_FROM";
		public static final String SERVICE_SECONDARY_MAIL_X_MAILER = "SERVICE_SECONDARY_MAIL_X_MAILER";

		public static final String CLIENT_CREDENTIALS_DEFAULT_USERNAME = "CLIENT_CREDENTIALS_DEFAULT_USERNAME";

		public static final String DGR_LOGOUT_URL = "DGR_LOGOUT_URL";
		public static final String LOGOUT_API = "LOGOUT_API";

		public static final String FIXED_CACHE_TIME = "FIXED_CACHE_TIME";

		public static final String DGR_QUERY_MONITOR_DAY = "DGR_QUERY_MONITOR_DAY";

		public static final String DEFAULT_PAGE_SIZE = "DEFAULT_PAGE_SIZE";
		public static final String MAIL_BODY_API_FAIL_SERVICE_MAIL = "MAIL_BODY_API_FAIL_SERVICE_MAIL";
		public static final String MAIL_BODY_API_FAIL_SERVICE_TEL = "MAIL_BODY_API_FAIL_SERVICE_TEL";

		public static final String ERRORLOG_KEYWORD = "ERRORLOG_KEYWORD";
		public static final String FILE_TEMP_EXP_TIME = "FILE_TEMP_EXP_TIME";
		public static final String AUTH_CODE_EXP_TIME = "AUTH_CODE_EXP_TIME";
		public static final String QUERY_DURATION = "QUERY_DURATION";
		public static final String SHUTDOWN_ENDPOINT_ALLOWED_IPS = "SHUTDOWN_ENDPOINT_ALLOWED_IPS";
		public static final String MAIL_SEND_TIME = "MAIL_SEND_TIME";

		public static final String DGR_CORS_VAL = "DGR_CORS_VAL";
		public static final String DGR_HOST_HEADER = "DGR_HOST_HEADER";
		public static final String DGR_CSP_VAL = "DGR_CSP_VAL";

		// Kibana
		public static final String KIBANA_VERSION = "KIBANA_VERSION";
		public static final String KIBANA_USER = "KIBANA_USER";
		public static final String KIBANA_PWD = "KIBANA_PWD";
		public static final String KIBANA_TRANSFER_PROTOCOL = "KIBANA_TRANSFER_PROTOCOL";
		public static final String KIBANA_HOST = "KIBANA_HOST";
		public static final String KIBANA_PORT = "KIBANA_PORT";
		public static final String KIBANA_AUTH = "KIBANA_AUTH";
		public static final String KIBANA_LOGIN_URL = "KIBANA_LOGIN_URL";
		public static final String KIBANA_LOGIN_REQUESTBODY = "KIBANA_LOGIN_REQUESTBODY";
		public static final String KIBANA_STATUS_URL = "KIBANA_STATUS_URL";

		public static final String DGR_AC_LOGIN_PAGE = "DGR_AC_LOGIN_PAGE";

		// DP
		public static final String DP_ADMIN = "DP_ADMIN";
		public static final String JWKS_URI = "JWKS_URI";
		public static final String SSOTOKEN_AUTHORIZATION_URL = "SSOTOKEN_AUTHORIZATION_URL";
		public static final String DP_CLIENT_ID = "DP_CLIENT_ID";
		public static final String DP_CLIENT_PWD = "DP_CLIENT_PWD";
		public static final String DP_USER_NAME = "DP_USER_NAME";
		public static final String DP_USER_ENTRY = "DP_USER_ENTRY";

		public static final String DP_ENABLE_AUTOMATIC_REVIEW = "DP_ENABLE_AUTOMATIC_REVIEW";
		public static final String DP_LOGIN_TYPES = "DP_LOGIN_TYPES";

		public static final String OAK_EXPI_URL = "OAK_EXPI_URL";

		public static final String PROFILEUPDATE_INVALIDATE_TOKEN = "PROFILEUPDATE_INVALIDATE_TOKEN";

		public static final String COMPOSER_LOG_INTERVAL = "COMPOSER_LOG_INTERVAL";
		public static final String COMPOSER_LOG_SIZE = "COMPOSER_LOG_SIZE";
		public static final String COMPOSER_LOG_SWICTH = "COMPOSER_LOG_SWICTH";
		public static final String COMPOSER_LOG_MAX_FILES = "COMPOSER_LOG_MAX_FILES";
		public static final String COMPOSER_REQUEST_TIMEOUT = "COMPOSER_REQUEST_TIMEOUT";

		public static final String USER_UPDATE_BY_SELF = "USER_UPDATE_BY_SELF";

		public static final String AUTO_INITSQL_FLAG = "AUTO_INITSQL_FLAG";

		public static final String API_DASHBOARD_BATCH_QUANTITY = "API_DASHBOARD_BATCH_QUANTITY";

		public static final String X_API_KEY_PLAIN_ENABLE = "X_API_KEY_PLAIN_ENABLE";

		public static final String DGR_ON_AWS = "DGR_ON_AWS";

		public static final String AWS_PUBLIC_KEY = "AWS_PUBLIC_KEY";
		public static final String KIBANA_REPORTURL_PREFIX = "KIBANA_REPORTURL_PREFIX";
		public static final String DEFAULT_DATA_CHANGE_ENABLED = "DEFAULT_DATA_CHANGE_ENABLED";

		public static final String CUS_NAME_SETTING = "CUS_NAME_SETTING";
		public static final String CUS_PWD_SETTING = "CUS_PWD_SETTING";

		public static final String CHECK_BOT_DETECTION = "CHECK_BOT_DETECTION";
		public static final String BOT_DETECTION_LOG = "BOT_DETECTION_LOG";
		
		public static final String APIM_DEFAULT_DB_ENABLED = "APIM_DEFAULT_DB_ENABLED";

		// CUS
		public static final String CUS_LOGIN_URL = "CUS_LOGIN_URL";

		public static final String LOG_RETENTION_DAYS = "LOG_RETENTION_DAYS";
		
		public static final String REQUEST_URI_ENABLED = "REQUEST_URI_ENABLED";
		
		public static final String HIGHWAY_THRESHOLD = "HIGHWAY_THRESHOLD";

		public static final String ES_LOGFILE_FAIL_RETRY = "ES_LOGFILE_FAIL_RETRY";
		public static final String ES_CHECK_CONNECTION = "ES_CHECK_CONNECTION";

		public static final String AUDIT_LOG_RETENTION_DAYS = "AUDIT_LOG_RETENTION_DAYS";

		public  static final String KIBANA_REFERER_ALLOWLIST = "KIBANA_REFERER_ALLOWLIST";
		
		public  static final String FIRST_TIME_LOGIN_CHANGE_MIMA_ENABLED = "FIRST_TIME_LOGIN_CHANGE_MIMA_ENABLED";
		
		public  static final String PWD_STRENGTH = "PWD_STRENGTH";
		public  static final String PWD_STRENGTH_DESC = "PWD_STRENGTH_DESC";
		public  static final String CLIENT_PWD_STRENGTH = "CLIENT_PWD_STRENGTH";
		public  static final String CLIENT_PWD_STRENGTH_DESC = "CLIENT_PWD_STRENGTH_DESC";

        public static final String SHOW_ALL_PROPERTIES = "SHOW_ALL_PROPERTIES";

        public static final String HTTPUTIL_RESP_TIME_LOG = "HTTPUTIL_RESP_TIME_LOG";

        public static final String PROPERTIES_MASK_LIST = "PROPERTIES_MASK_LIST";
    }

	public List<TsmpSetting> findByIdLike(String id);

	public List<TsmpSetting> query_DPB9900Service_01(String lastId, String[] keywords, Integer pageSize);

	public TsmpSetting findByIdAndValue(String id, String value);

	public List<TsmpSetting> findAllByOrderByIdAsc();

}
