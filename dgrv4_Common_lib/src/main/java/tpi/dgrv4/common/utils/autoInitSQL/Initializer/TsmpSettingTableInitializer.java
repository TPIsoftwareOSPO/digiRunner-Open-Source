package tpi.dgrv4.common.utils.autoInitSQL.Initializer;

import org.springframework.stereotype.Service;
import tpi.dgrv4.common.utils.LicenseEditionTypeVo;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.utils.autoInitSQL.vo.TsmpSettingVo;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TsmpSettingTableInitializer {

	private List<TsmpSettingVo> tsmpSettinglist = new LinkedList<>();
	
	private List<TsmpSettingVo> tsmpSettingUpdatelist = new LinkedList<>();
	
	private LicenseEditionTypeVo currentLicense;
	
	/**
	 * TSMP_SETTING的值會依照License而調整
	 * @param1 TSMP_SETTING 的 Key  
	 * @param2 License 版本
	 * @param3 TSMP_SETTING 的 value 
	 * @return 
	 */
	public static String[][][] updateTsmpSettingArray = {
			{
					{"TSMP_APILOG_FORCE_WRITE_RDB", LicenseEditionTypeVo.Alpha.name(), "false", "Write API log into RDB enablement ( true/false )"},
					{"TSMP_APILOG_FORCE_WRITE_RDB", LicenseEditionTypeVo.Enterprise.name(), "false", "Write API log into RDB enablement ( true/false )"},
					{"TSMP_APILOG_FORCE_WRITE_RDB", LicenseEditionTypeVo.Enterprise_Lite.name(), "false", "Write API log into RDB enablement ( true/false )"},
					{"TSMP_APILOG_FORCE_WRITE_RDB", LicenseEditionTypeVo.Express.name(), "false", "Write API log into RDB enablement ( true/false )"}
			}
	};

	public List<TsmpSettingVo> insertTsmpSetting() {
		try {
			String id;
			String value;
			String memo;

			createTsmpSetting("SERVICE_MAIL_ENABLE","true","Main SMTP server setting : Sender authentication process enablement ( true/false )");
			createTsmpSetting("SERVICE_MAIL_HOST","smtp.gmail.com","Main SMTP server setting : SMTP server domain name ( smtp.gmail.com )");
			createTsmpSetting("SERVICE_MAIL_PORT","587","Main SMTP server setting : SMTP server service-port");
			createTsmpSetting("SERVICE_MAIL_AUTH","true","Main SMTP server setting : Sender authentication enablement ( true/false )");
			createTsmpSetting("SERVICE_MAIL_STARTTLS_ENABLE","true","Main SMTP server setting : TLS enablement ( true/false )");
			// -- 移除預設安裝的機敏資料
//	        createTsmpSetting("SERVICE_MAIL_USERNAME","system@elite-erp.com.tw","主要smtp server設定, 寄件者的顯示名稱");
			createTsmpSetting("SERVICE_MAIL_USERNAME","example@tpisoftware.com","Main SMTP server setting : Sender display name");
			createTsmpSetting("SERVICE_MAIL_PASSWORD","eliteTpower","Main SMTP server setting : Sender email password");
			// -- 移除預設安裝的機敏資料
//	        createTsmpSetting("SERVICE_MAIL_FROM","system@elite-erp.com.tw","主要smtp server設定, 本系統寄件者郵件位址");
			createTsmpSetting("SERVICE_MAIL_FROM","example@tpisoftware.com","Main SMTP server setting : Sender email address ( system@elite-erp.com.tw )");
			createTsmpSetting("SERVICE_MAIL_X_MAILER","Thinkpower","Main SMTP server setting : mail delivery process name");
			createTsmpSetting("SERVICE_SECONDARY_MAIL_ENABLE","true","Secondary SMTP server setting : Sender authentication process enablement ( true/false )");
			createTsmpSetting("SERVICE_SECONDARY_MAIL_HOST","smtp.gmail.com","Secondary SMTP server setting : SMTP server domain name ( smtp.gmail.com )");
			createTsmpSetting("SERVICE_SECONDARY_MAIL_PORT","587","Secondary SMTP server setting : SMTP server service-port");
			createTsmpSetting("SERVICE_SECONDARY_MAIL_AUTH","true","Secondary SMTP server setting : Sender authentication enablement ( true/false )");
			createTsmpSetting("SERVICE_SECONDARY_MAIL_STARTTLS_ENABLE","true","Secondary SMTP server setting : TLS enablement ( true/false )");
			// -- 移除預設安裝的機敏資料
//	        createTsmpSetting("SERVICE_SECONDARY_MAIL_USERNAME","system@elite-erp.com.tw","次要smtp server設定, 寄件者的顯示名稱");
			createTsmpSetting("SERVICE_SECONDARY_MAIL_USERNAME","example@tpisoftware.com","Secondary SMTP server setting : Sender display name");
			createTsmpSetting("SERVICE_SECONDARY_MAIL_PASSWORD","eliteTpower","Secondary SMTP server setting : Sender email password");
			// -- 移除預設安裝的機敏資料
//	        createTsmpSetting("SERVICE_SECONDARY_MAIL_FROM","system@elite-erp.com.tw","次要smtp server設定, 本系統寄件者郵件位址");
			createTsmpSetting("SERVICE_SECONDARY_MAIL_FROM","example@tpisoftware.com","Secondary SMTP server setting : Sender email address ( system@elite-erp.com.tw )");
			createTsmpSetting("SERVICE_SECONDARY_MAIL_X_MAILER","Thinkpower","Secondary SMTP server setting : mail delivery process name");
			createTsmpSetting("TSMP_EDITION","Cn88-nNO8-xx8u-un88-nVoF-Fr48-80rc-L5rF-xN#8-e1=x-6#xo-=d4#-2!=n-!#2!-=!!!-!!!","TSMP license key");
			// 這段 hardcoded IP 會被 SonarQube 標記為 Security Hotspots, 故改為 DNS 名稱
			createTsmpSetting("LDAP_URL","ldap://ldap.example.com:389","ldap login URL");
			createTsmpSetting("LDAP_DN","uid={{0}},dc=tstpi,dc=com","ldap login user DN");
			createTsmpSetting("LDAP_TIMEOUT","3000","Connection timeout for ldap login, in milliseconds(ms)");
			createTsmpSetting("LDAP_CHECK_ACCT_ENABLE","false","LDAP check account function enablement - true/false");

			createTsmpSetting("TSMP_AC_CLIENT_ID","YWRtaW5Db25zb2xl","Login AC account (do not modify)");
			createTsmpSetting("TSMP_AC_CLIENT_PW","dHNtcDEyMw==","AC login password (do not modify)");

			createTsmpSetting("TSMP_FAIL_THRESHOLD","6","Allowed \"User password\" fail THRESHOLD");
			createTsmpSetting("SSO_PKCE","true","PKCE Level AuthCode verification enablement - true/false");
			createTsmpSetting("SSO_DOUBLE_CHECK","true","Enablement of Double-check verification - true/false");
			createTsmpSetting("SSO_AUTO_CREATE_USER","false","Enablement of SSO auto - user account creation - true/false");
			createTsmpSetting("TSMP_DPAA_RUNLOOP_INTERVAL","1","The RUNLOOP sett for DPAA, if it is set to 0, the alarm detection will be disabled");
			createTsmpSetting("TSMP_COMPOSER_ADDRESS","http://tsmp-composer:1880","COMPOSER broadcasting, separated by commas");
			createTsmpSetting("TSMP_DELETEMODULE_ALERT","false","Enable the Prompt when deleting MODULE - true/false");
			createTsmpSetting("TSMP_AC_CONF","{\"dp\":0,\"net\":false}","When logging in, the setting value for AC provided by the backend.");
//	      -- 20221118, v4, 增加 RDB 版本的 API Log
			createTsmpSetting("TSMP_APILOG_FORCE_WRITE_RDB","false","Write API log into RDB enablement ( true/false )");
			// 這段 hardcoded IP 會被 SonarQube 標記為 Security Hotspots, 故改為 DNS 名稱
			createTsmpSetting("DGR_LOGOUT_URL","","Customized Login Page URL{{scheme}}://{{ip}}:{{port}}/{{path}} ex: \"https://hostname:port/dgr-cus-scbank-ac/\"");
			createTsmpSetting("TSMP_COMPOSER_PORT","8440","The Composer acess port used for AC");
			createTsmpSetting("TSMP_COMPOSER_PATH","/website/composer","The Composer path used for AC");
			createTsmpSetting("TSMP_PROXY_PORT","4944","Currently inused ; Reserved as the functionality - TSMP_REPORT_ADDRESS");
			createTsmpSetting("CUS_MODULE_EXIST","false","\"Customized Module\" exist indicator (true/false)");
			createTsmpSetting("CUS_FUNC_ENABLE1","false","\"Customized Function\" enablement (true/false)");
			createTsmpSetting("CUS_MODULE_NAME1","","Customized Moudle name");
			// 這段 hardcoded IP 會被 SonarQube 標記為 Security Hotspots, 例如: "192.168.0.0/23,192.168.0.0/24,127.0.0.0/24", 故改為 a.b.0.0 表示
			createTsmpSetting("UDPSSO_LOGIN_NETWORK","a.b.0.0/23,a.b.0.0/24,a.0.0.0/24","Network segments that can be logged in, multiple CIDRs are separated by commas");
			createTsmpSetting("CLIENT_CREDENTIALS_DEFAULT_USERNAME","true","client_credentials get token \"default userName\" enablement (true/false )");
			createTsmpSetting((id = "TSMP_REPORT_ADDRESS"), (value = "15601"), (memo = "TSMP report address - v3(38451),v4(15601)"));
			createTsmpSetting((id = "LOGOUT_API"), (value = ""), (memo = "Customization Function - Sign out url{{scheme}}://{{ip}}:{{port}}/{{path}} ex: \"https://127.0.0.1:8442/dgr-cus-scbank/scb/logout\""));
//		  -- 20220303, Audit Log 增加參數, Mini Lee
			createTsmpSetting((id = "AUDIT_LOG_ENABLE"), (value = "true"), (memo = "\"Audit Log\" Recode Function enablement (true/false )"));

//	      -- 2022/04/13 DGRKEEPER設定
			createTsmpSetting((id = "DGRKEEPER_IP"), (value = "127.0.0.1"), (memo = "DGRKEEPER Server Host IP"));
			createTsmpSetting((id = "DGRKEEPER_PORT"), (value = "8085"), (memo = "DGRKEEPER Server Host PORT"));

//	      -- 2022/04/13 Online Console開關設定
			createTsmpSetting((id = "TSMP_ONLINE_CONSOLE"), (value = "true"), (memo = "Online Console enablement - true/false"));

//	      -- 2022/04/19 Logger Level設定
			createTsmpSetting((id = "LOGGER_LEVEL"), (value = "INFO"), (memo = "Logger log output level setting"));

//	      -- 20220427, 檢查器的設定, tom
			createTsmpSetting((id = "CHECK_XSS_ENABLE"), (value = "false"), (memo = "\"XSS Checker\" enablement (true/false )"));
			createTsmpSetting((id = "CHECK_XXE_ENABLE"), (value = "false"), (memo = "\"XXE Checker\" enablement (true/false )"));
			createTsmpSetting((id = "CHECK_SQL_INJECTION_ENABLE"), (value = "false"), (memo = "\"SQL Injection Checker\" enablement (true/false )"));
			createTsmpSetting((id = "CHECK_IGNORE_API_PATH_ENABLE"), (value = "true"), (memo = "\"API Path IGNORE Checker\" enablement (true/false )"));
			createTsmpSetting((id = "CHECK_API_STATUS_ENABLE"), (value = "true"), (memo = "\"API Switch\" enablement (true/false )"));
			createTsmpSetting((id = "CHECK_TRAFFIC_ENABLE"), (value = "true"), (memo = "\"Traffic Checker\" enablement (true/false )"));
			createTsmpSetting((id = "IGNORE_API_PATH"), (value = "/,/tptoken/oauth/token,/ssotoken/**,/v3/**,/shutdown/**,/version/**,/onlineconsole1/**,/onlineConsole/**,/udpssotoken/**,/cus/**"), (memo = "Specify the API path to skip all checker settings (multiple items separated by commas (,))"));

//	      -- 20220606, ES的設定, tom chu
			// 這段 hardcoded IP 會被 SonarQube 標記為 Security Hotspots, 故改為 DNS 名稱
			createTsmpSetting((id = "ES_URL"), (value = "https://es.example.com:19200/"), (memo = "The URL of ES, there must be a / line at the end, multiple groups are separated by commas (,), EX: https://es.example.com:19200/, https://es.example.com:29200/"));
			createTsmpSetting((id = "ES_ID_PWD"), (value = "ENC(cGxlYXNlIHNldCB5b3VyIGVzIGlkIGFuZCBwYXNzd29yZCwgU2V0dGluZyBpcyBFU19JRF9QV0Q=)"), (memo = "ES's ID:PWD is combined and encrypted with Base64; you can press the button\"ENC\" to encrypt with ENC. If you want to set for multiple URLs - Separated by commas before ENC encryption, EX:ENC(id1:pwd1,id2:pwd2)"));
			createTsmpSetting((id = "ES_TEST_TIMEOUT"), (value = "3000"), (memo = "ES connection timeout setting for testing"));
			createTsmpSetting((id = "ES_MBODY_MASK_FLAG"), (value = "false"), (memo = "Mask all mbody, true is masking, false is not masking"));
			createTsmpSetting((id = "ES_IGNORE_API"), (value = ""), (memo = "Ignore the assigned API transation recording in the ES, API list can be list and separated by commas (,), and the value is moduleName/apiId"));
			createTsmpSetting((id = "ES_MBODY_MASK_API"), (value = ""), (memo = "Do mbody mask for the API of tsmpc, API list can be separated by commas (,), and the value is moduleName/apiId"));
			createTsmpSetting((id = "ES_TOKEN_MASK_FLAG"), (value = "true"), (memo = "Mask token, true is masking, false is not masking"));
			createTsmpSetting((id = "ES_MAX_SIZE_MBODY_MASK"), (value = "0"), (memo = "Auto mbody mask when the length exceeding the mbody content value byte, the unit is byte, and the value below 10 (inclusive) will not be masked"));
			createTsmpSetting((id = "ES_DGRC_MBODY_MASK_URI"), (value = ""), (memo = "Make an mbody mask on the URI of dgrc (value contains /dgrc), URL lists are separate by commas, Value format / dgrc/aa/bb/cc"));
			createTsmpSetting((id = "ES_DGRC_IGNORE_URI"), (value = ""), (memo = "ES does not record URIs for dgrc(Value contains /dgrc), URL lists are separate by commas (,),Value is /dgrc/aa/bb/cc"));
			createTsmpSetting((id = "ES_LOG_DISABLE"), (value = "true"), (memo = "Whether to prohibit the recording of ES LOG, true means yes, false means no"));
			createTsmpSetting((id = "DGR_PATHS_COMPATIBILITY"), (value = "2"), (memo = "URL Path compatibility, 0: tsmpc only, 1 : dgrc only, 2 : tsmpc dgrc Compatible"));

//	      -- 20220712, token的設定, Mini Lee
			createTsmpSetting((id = "DGR_TOKEN_JWE_ENABLE"), (value = "false"), (memo = "\"Token JWE encryption\"  enablement (true/false ), default:false"));

//	      -- 20220801, token的設定, Mini Lee
			createTsmpSetting((id = "DGR_TOKEN_WHITELIST_ENABLE"), (value = "false"), (memo = "\"Token whitelist \" enablement (true/false ), default:false"));

//	      -- 20220805, API訊息設定, Mini Lee
			createTsmpSetting((id = "DGR_TW_FAPI_ENABLE"), (value = "false"), (memo = "\"API information follow TW Open Banking Format\" enablement (true/false ), default:false"));

//	      -- 20220815, fixedCache時間, Tom
			createTsmpSetting((id = "FIXED_CACHE_TIME"), (value = "1"), (memo = "Static cache time, set in minutes"));

//	      -- 20220829, es monitor host的設定, tom
			createTsmpSetting((id = "ES_SYS_TYPE"), (value = "DGR_LOCAL"), (memo = "Used to identify data purposes"));
			createTsmpSetting((id = "ES_MONITOR_DISABLE"), (value = "true"), (memo = "Whether to prohibit record ES monitoring, true means yes, false means no"));

//	      -- 20220926, 查詢DGR監控資料的時間, Tom
			createTsmpSetting((id = "DGR_QUERY_MONITOR_DAY"), (value = "7"), (memo = "DGR monitoring available data period. Unit :Day"));
//	      -- 20221018, 重設Client API配額的頻率, Zoe
			createTsmpSetting((id = "DGR_CLIENT_QUOTA_FRQ"), (value = "D"), (memo = "Reset the Frequency of Client API quota, N:None,D:daily,SD:Every Sunday,MD:every Monday,MT:1st of every month,Default:D"));

//	      -- 20221018, v4 將 application properties 的值加入 Setting, Kevin Cheng
			createTsmpSetting((id = "DEFAULT_PAGE_SIZE"), (value = "20"), (memo = "Default Page Size"));
			createTsmpSetting((id = "MAIL_BODY_API_FAIL_SERVICE_MAIL"), (value = "service@thinkpower.com.tw"), (memo = "System email body : Service email account (service@thinkpower.com.tw)"));
			createTsmpSetting((id = "MAIL_BODY_API_FAIL_SERVICE_TEL"), (value = "+886-2-8751-1610"), (memo = "System email body : Service number ( 02-12345678 )"
					+ ""));
			createTsmpSetting((id = "ERRORLOG_KEYWORD"), (value = "tpi.dgrv4,com.thinkpower"), (memo = "Print error messages contains specific texts, set up multiple texts - separates by commas (,)"));
			createTsmpSetting((id = "FILE_TEMP_EXP_TIME"), (value = "3600000"), (memo = "Temporary file expiry time"));
			createTsmpSetting((id = "AUTH_CODE_EXP_TIME"), (value = "600000"), (memo = "\"Auth code Expired Time\" setting(ms)"));
			createTsmpSetting((id = "QUERY_DURATION"), (value = "30"), (memo = "ES query date interval upper limit"));
			createTsmpSetting((id = "SHUTDOWN_ENDPOINT_ALLOWED_IPS"), (value = "127.0.0.1,0:0:0:0:0:0:0:1"), (memo = "List of IP hosts allowed to access the shutdown endpoint, separated by commas (,), if the setting empty, no one allow to call"));
			createTsmpSetting((id = "MAIL_SEND_TIME"), (value = "600000"), (memo = "How long does it take to send the mail (ms) after writing the mail into the schedule, the default is 10 minutes"));

//	      -- 20221129, 將 CORS, Access-Control-Allow-Origin 的值加入 Setting, Kevin Cheng
			createTsmpSetting((id = "DGR_CORS_VAL"), (value = "*"), (memo = "CORS default setting *, can change domain,Example:https://dgRv4.io/"));
			createTsmpSetting((id = "KIBANA_PWD"), (value = "ENC(mucabhaTTTk7YuJnl8OqFDNi98EFp1eMutQpzUbDuJeqAtx3poP8yIYMQ+cMbfiIGUBOI7SocRabEMNvnRZ+86b/MQ0zJJTAJtO39jtXKJoLAfdBYF+8+HmEVnkVi/ws7MGEEhY+TON6/lXovoD9NEZOMTLam89Q7lnfq2fG3i8YdCvX4eja6IWzd4jdyehWiq/yi6DkKmnlqApLSN8/ykLxQvY/eeIds/DKrUWRy1Q10FthFASl9XGIW1EhjcoWwb9aNkhvtrnTMkTG1be5i/+W3AClRZ72V/ZJUy6LJYP4sNub68VP812XLfsyMS/eL1axQO3NEWhQRrpHUjNRxg==)"), (memo = "Kibana's password( Press button \"Update\" or \"Update ENC\" to encrypt and update)"));

//	      -- 20221206, 加入 KIBANA_PWD 可為 ENC 或明文, 調整Kibana主機設定欄位名稱, Kevin Cheng
			createTsmpSetting((id = "KIBANA_VERSION"), (value ="7.10"), (memo = "Kibana version"));
			createTsmpSetting((id = "KIBANA_USER"), (value = "tpuser"), (memo = "Kibana's account"));

			createTsmpSetting((id = "KIBANA_TRANSFER_PROTOCOL"), (value = "https"), (memo = "Kibana's http Protocol"));
			// 這段 hardcoded IP 會被 SonarQube 標記為 Security Hotspots, 故改為 DNS 名稱
			createTsmpSetting((id = "KIBANA_HOST"), (value = "kibana.example.com"), (memo = "Kibana's Host IP"));
			createTsmpSetting((id = "KIBANA_PORT"), (value = "15601"), (memo = "Kibana's Host Port"));

//		  -- 20230104, v4 SSO IdP 的設定, Mini Lee
			createTsmpSetting((id = "AC_IDP_REVIEWER_MAILLIST"), (value = "example@tpisoftware.com"), (memo = "ldp AC User's Reviewer,can set multiple,Please use (,) to contenate"));
			createTsmpSetting((id = "AC_IDP_MSG_URL"), (value = "https://localhost:8080/dgrv4/ac4/idpsso/errMsg"), (memo = "Frontend AC IdP error message display URL"));
			createTsmpSetting((id = "AC_IDP_REVIEW_URL"), (value = "https://localhost:8080/dgrv4/ssotoken/acidp/acIdPReview"), (memo = "AC IdP Review approval URL"));
			createTsmpSetting((id = "AC_IDP_ACCALLBACK_URL"), (value = "https://localhost:8080/dgrv4/ac4/idpsso/accallback"), (memo = "Frontend AC IdP AC callback URL"));
			createTsmpSetting((id = "AC_IDP_LDAP_REVIEW_ENABLE"), (value = "true"), (memo = "Is the AC IdP LDAP sending the approval email and auto-creating users enabled? (true/false)"));

			createTsmpSetting((id = "DGR_AC_LOGIN_PAGE"), (value = "redirect:/dgrv4/ac4/login"), (memo = "The digiRunner login page must be restarted after modifications. The available modification options include 'redirect:/dgrv4/ac4/login' and 'redirect:/dgrv4/ac4/ldap'."));

//		  -- 20230410, v4 GTW IdP 的設定, Mini Lee
	        createTsmpSetting((id = "GTW_IDP_JWK1"), (value = "{\"p\":\"4zMSqD0dhGjwrxAdoHDYVl6gHSyX5Lr-oA8SSIAswXFsSoU6dup0uU9vtEGxME7JS83rQowPG1rzIZjaf3078skm_Ry4jNq6b3KjPCnmLZXD7STAw6_pfe9oZYFDE5m05B129zfYa83yiKhEFY96CyWCBYqUPIENqsJ5ybQcr80\",\"kty\":\"RSA\",\"q\":\"y0dI4g2EsCDC-5D5zJAsjsp9jFfh1qIIYy_zWHWm4-o8R5kYR9k4CX-QiqM5w9fZSu5eCX98T4NmlkfoKwf68a49bPAyvIsBbor7GBZU-nTGYjsjsF4XnFgv-UTuc5WCWDV119Ph3ebYxYFeLo-bnKrPr52JZyHE_FNqudn0yB8\",\"d\":\"AYk60TXj1goNm7kvSltXawShh__WxuzUsIkAMJaA4QXSGabkUzHTkxiP13mHSGiX80mxMsyqJTZHF7kZESzJhu3C3Kzbf4G68aB4PbDLM-3q12DywbfGX_TR5UzstsDWlJ259bl6QibLbBRTvZyIgzGMWBWOc40MV7wRhvHDl9Gyfk7MDtRJgYJfhWO4GmIl-nwECgzqEPmZrhUFSTEAQzzSL2lEpXIGii_cURM3fDxi8RTb4_LT7Ox_2MyCpIIqrlrOzYv39RKeuyJ6S5NV7TdxLnOpxAbxzhmeQGjvSTj0AcH7HT5b6pVF3MRFO1_V4Pji1ezZ_E4Vdd0yaLm3sQ\",\"e\":\"AQAB\",\"use\":\"sig\",\"kid\":\"39f9cd84-34bd-4fd8-84c7-8676f9ff741e\",\"qi\":\"hvMpK7FBbSKND8mBHeG5XiJHl1O2nOZSFh3vGN9tbTfowFK8pbGgiABniYzHZq5ghm7qepxQrfi_-o6r1-zlh3rdMpjU5WWI5UfTseXixfNoSac2QHk4fwItjkPVynyYyjDWR8HdJDN_BR3QyUGvms8BEqrnAIm96RzwBmPsmoU\",\"dp\":\"uMizJe3-8dA_4MIktnbRHP39D31TVI7ZxOg9IIZO4E4Vm05cTJdHs-ftnBfJutZ5VZP3AbrUFpWUJQEixInglggQE9CmMLk85KPCK46QTQb_wQIhXYbXSrvKlrPZEDn1K4rjVRIwjQ2FcqwYI8j7o9EvvL4G7mav7PAbCXfZ1Qk\",\"alg\":\"RS256\",\"dq\":\"MpugrYaoDiFZ6b-CMUeDkFkhQJtwgjr805TQhKllz0A1ma2nudt-c_7qQVm5u-Q1GM6XYs32aOVR2QA18OCfvSOf70stlnsU9CxtruWAaopACZynmfUS872Q1AIxS11hggxtNjpt9QzP0vwOMpFWMH7mDdauqpphrGAoJfT5WAk\",\"n\":\"tGjDpJF0f32LXfSJbI_0PJTlrgxI1oefXXMHJtsNi8phBQsACimkqrqyBiJMIPm94Y5yANrQOusJLQxKcqDlQqfRZ45NPgpMx8J2cGrY4GYz-mXpXdXVqzb8Yo46k4WolMk14mFA-g9nKwKrjlkPeSVobvq9hqbymIEJo-skguR2gkpGOHDPgzkvkST02a3kiIoswmPDkhfUTgZBoYrocxg_pPRDOIN2v5YKGtcGQESev914dWTScHAX5m3-fGj8j-6Ua8gxGddN0iFHbfCa_WegEkl8K7EynZTcfkARzvs9kVBOOzMFUj40fTFnvlAJcik_Kc6wGE1_mz2kjTVx0w\"}"), (memo = "Gateway IdP JWKS, 1st key pair"));
	        createTsmpSetting((id = "GTW_IDP_JWK2"), (value = "{\"p\":\"8t5x_y43MXT4eidK_cFsAwaV8ENl_Kc8WX1l87D_tDVjaxUOpPfMjUEho95Kn-nkkTi3DV83ht90xiva8l5DKKZkh_OwSBFhRfVfJEwG0XPh5KfHmU07tgqRJYB1zf6htNHI98MR9qTu2q1_dv9_BzR-sPPg_fpvw4LR2i6I1q0\",\"kty\":\"RSA\",\"q\":\"0h3rCbCq3C3aJQIk3XyhknxQjyIhtFX3Y5TELqzodSZ-WfyhJQX8fmUkiIfiib-0zVA3QpD_4ZWjdPdX8Nn6d8hatnyz1CmUbDAGKq5d8fFtaqWyaTQj9eX9bLwH1f6EgZbIi5psq0E7GAIT-mQBw2Gg5IX2p5lo22a53HsXp-U\",\"d\":\"E3iS_g9bolHnplwmp4iVFSCdFMNnZ6wbiwvPEoo8BlGx-x_wf1xnpi-jqXoOqPxRuMvO5Q0s92tmu3xzCBoidCPV-ISpKkbzYZMKGKIiSqC31Efldl0sLjaiRyWocTuM8H0SgZEZFt4FjlZYlBVTatinPetqHSm-K7FUe5hRXNeKWx_SfyohXz5YbRAANX_MJ3JI3jbl-uppXRdIt016gF6SntTQKRSrgk6VS2kcNc2XlwdShbFIqvcVKLoh5ubzMHPo9NdRWeaWJlFmNrSpOtsUb6Cdzmf2iKxcpUOQSoPCFPu68GPrS1qwtTAPs-L4786b7Nf1tpyNLTeyTCTHyQ\",\"e\":\"AQAB\",\"use\":\"sig\",\"kid\":\"f818d971-8dbb-47b3-9291-f42b1ea06e18\",\"qi\":\"gqX1mmcVzoD-JMussMGiOfRzz2ydIqvehQvd5cKw9bRcraUhVuPw6wnD6JmqYNZWAI4a7efMXgWkOCrwKb6kJahpuWQfz7s9YMo0Zl3iOGd07oyb6Y3IW4lXlS8ZCr8YsF7VIaoxEunN2dQJUDy1X45HGLa8BH-dfUvp3JAmNBI\",\"dp\":\"FSU886TIdWvjvm7xXoqapuDJ6TNVC9xbqsb6O29rs_r5_vbEYaSZkKrdDPFrueSZW_N-LJHfucR23FIxK-z9F-r00clrzbqFp5unfveHmHDoeAoLnNNWoZDl9kfq-dZzqdSiFMBNLhZKHYwBjxDLtIrjhPCW5EYLuRAIyWBH1bk\",\"alg\":\"RS256\",\"dq\":\"a_IM3wSRMt6nlJ2-XL97rmsJZA9v61rC5rj19NjF7_GfthFQpFmn9zN1CmNtIcGIXHZafWtK2hTrTdsIpecGg2U-HUSBinz2EIK3mFPOVc7nnIOV3fB4jQrkIGmVSP4iCwVw8C-cpnqzpkjjBJ8-PKc6ZkzghAgPU7A5yii-5XU\",\"n\":\"x1bbsaHjyzGlq_jDJELPGY_Vqj1P9tzWPGZheJqgWDVHjlfsndbPYAJQOdCyUeZk8OJ-ms546lwav81kf53iniysiKIb_a9nvJe2FZBsUziLhvo2CanDEdkwUeXzHl9icEEQhY2Id3TZe79AdknXvH-nUt3oyd3w-x8FDknmpwD_YPJsqq4quWLazeAe924MLFuz532AZJ0QpsaR4lf3rzmwVIIjr6MJuqIOU2_fN2r-8S7W08ubk0wvDX7a3O-rKowIQwM9nHlyb-uUC830vq5mL4F3I5EEhjzX2FKXxl00GUJttxFnyJw0JUd002dj5tOH7-4akq1SinujTP3jwQ\"}"), (memo = "Gateway IdP JWKS, 2nd key pair"));
	        //對外公開的域名或IP
	        createTsmpSetting((id = "DGR_PUBLIC_DOMAIN"), (value = "localhost"), (memo = "Public domain name or IP, ex: www.tpisoftware.com"));
	        //對外公開的Port
	        createTsmpSetting((id = "DGR_PUBLIC_PORT"), (value = "18080"), (memo = "Public port, ex: 80"));

//	      -- 20230421, v4 GTW IdP 的設定, Mini Lee
			// 前端 GTW IdP errMsg 顯示訊息的URL
	        createTsmpSetting((id = "GTW_IDP_MSG_URL"), (value = "https://localhost:8080/dgrv4/ac4/gtwidp/errMsg"), (memo = "URL of frontend GTW IdP error message display message"));
			// 前端GTW IdP User登入畫面的URL
	        createTsmpSetting((id = "GTW_IDP_LOGIN_URL"), (value = "https://localhost:8080/dgrv4/ac4/gtwidp/{idPType}/login"), (memo = "URL of front-end GTW IdP user login screen"));
			// 前端GTW IdP User同意畫面的URL
			createTsmpSetting((id = "GTW_IDP_CONSENT_URL"), (value = "https://localhost:8080/dgrv4/ac4/gtwidp/{idPType}/consent"), (memo = "URL of the front-end GTW IdP user consent screen"));

//		  -- 20230505, token的設定, Mini Lee
			// Cookie token是否啟用,預設為true (true/false)
			createTsmpSetting((id = "DGR_COOKIE_TOKEN_ENABLE"), (value = "true"), (memo = "Specifies whether the cookie token is enabled. The default value is true (true/false)."));

//		  -- 20230608 DPKEEPER設定
			createTsmpSetting((id = "DPKEEPER_IP"), (value = "127.0.0.1"), (memo = "DPKEEPER Server Host IP"));
			createTsmpSetting((id = "DPKEEPER_PORT"), (value = "8086"), (memo = "DPKEEPER Server Host Port"));

//		  -- 20230609, 安全性檢查的設定, Mini Lee
			createTsmpSetting((id = "DGR_HOST_HEADER"), (value = "*"), (memo = "Checks the Host header to prevent Host Header Injection. The default is '*', but it can be changed to a specific domain or IP address. Multiple values should be separated by a comma (,), e.g., 10.20.30.88:1920,10.20.30.88:2920."));

//		  -- 20230609, 將 Content-Security-Policy 的網域值加入 Setting, Mini Lee
			createTsmpSetting((id = "DGR_CSP_VAL"), (value = "*"), (memo = "The value of the Content-Security-Policy (CSP) header. The default is '*', but it can be changed to a specific domain or IP address. Multiple values should be separated by a space (' '), e.g., https://10.20.30.88:1920 https://10.20.30.88:2920."));
//        --20230804, GatewayFilter檢查器開關
			createTsmpSetting((id = "CHECK_JTI_ENABLE"), (value = "true"), (memo = "When the request includes Authorization, it will check if the jti is expired in the database. If this HTTP header is not included, no check will be performed. By default, the check function is enabled (true), and it will be disabled (false) otherwise."));

			createTsmpSetting((id = "PROFILEUPDATE_INVALIDATE_TOKEN"), (value = "false"), (memo = "Whether to revoke issued tokens when user data is modified. The default is false (not revoked). If set to true, tokens will be revoked upon user data changes."));

//			-- 20230817, Composer Log File Rotation + Composer log 開關 , Min
			createTsmpSetting((id = "COMPOSER_LOG_INTERVAL"), (value = "1d"), (memo = "Rotation interval format: 1s (seconds), 2m (minutes), 3h (hours), 4d (days), 5M (months). After the log file exists for the duration of {COMPOSER_LOG_INTERVAL}, regardless of its size, the log file will be compressed and a new log file will be created for continued logging."));
			createTsmpSetting((id = "COMPOSER_LOG_SIZE"), (value = "100"), (memo = "Unit: KB. When the log file exceeds {COMPOSER_LOG_SIZE} KB, it will be compressed and a new log file will be created for continued logging."));
			createTsmpSetting((id = "COMPOSER_LOG_SWICTH"), (value = "true"), (memo = "Composer log global switch: true (enabled), false (disabled)."));
			createTsmpSetting((id = "COMPOSER_LOG_MAX_FILES"), (value = "7"), (memo = "The number of rotation log files (or compressed files) to retain must be a positive integer."));
			createTsmpSetting((id = "COMPOSER_REQUEST_TIMEOUT"), (value = "240000"), (memo = "Timeout duration for requesting the COMPOSER API, in milliseconds."));

//			-- 限制不能變更及刪除自己的帳號開關 , Kevin K
			createTsmpSetting((id = "USER_UPDATE_BY_SELF"), (value = "false"), (memo = "Restrict users from modifying or deleting their own accounts (default: false, feature disabled)."));

//			-- 20230828, 執行AutoInitSQL開關, Min
			createTsmpSetting((id = "AUTO_INITSQL_FLAG"), (value = "true"), (memo = "Each time the digiRunner instance is started, the initSQL() function is automatically executed during the startup phase. The default is to execute (true), and it will not execute if set to false."));

//			-- 20231002, API DASHBAORD批次處理數量, TOM
			createTsmpSetting((id = "API_DASHBOARD_BATCH_QUANTITY"), (value = "500000"), (memo = "API statistics dashboard at daily, monthly, and batch processing quantities"));

//			-- 20231130, Open api key 展期 API URL, Kevin Cheng
			createTsmpSetting((id = "OAK_EXPI_URL"), (value = "https://localhost:8080/website/dp_api/DPF0073"), (memo = "Open API key extension API URL. Default: https://localhost:8080/website/dp_api/DPF0073."));

//	        -- 20231207, X-Api-Key記錄明文是否啟用,預設為false (true/false), Mini Lee
			createTsmpSetting((id = "X_API_KEY_PLAIN_ENABLE"), (value = "false"), (memo = "Whether X-Api-Key allows recording in plaintext. Default: false (true/false)."));

//	        -- 20231228, AWS API 狀態flag, Zoe Lee
			createTsmpSetting((id = "DGR_ON_AWS"), (value = "true"), (memo = " AWS API flag."));

			createTsmpSetting((id = "AWS_PUBLIC_KEY"), (value = "-----BEGIN PUBLIC KEY-----\nMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDdlatRjRjogo3WojgGHFHYLugd\nUWAY9iR3fy4arWNA1KoS8kVw33cJibXr8bvwUAUparCwlvdbH6dvEOfou0/gCFQs\nHUfQrSDv+MuSUMAe8jzKE4qW+jK+xQU9a03GUnKHkkle+Q0pX/g6jXZ7r1/xAK5D\no2kQ+X5xK9cipRgEKwIDAQAB\n-----END PUBLIC KEY-----"), (memo = "AWS Public Key"));
//			-- 20240124, KIBANA報表 前綴 , Zoe Lee
			createTsmpSetting((id = "KIBANA_REPORTURL_PREFIX"), (value = "/kibana"), (memo = "kibana report url prefix."));

//			-- 20240222, v4 AC IdP 的設定, Mini Lee
			createTsmpSetting((id = "AC_IDP_API_REVIEW_ENABLE"), (value = "true"), (memo = "Is the AC IdP API sending the approval email and auto-creating users enabled? (true/false)"));

//			-- 20240506, 異動系統預設資料請先解鎖, Webber Luo
			createTsmpSetting((id = "DEFAULT_DATA_CHANGE_ENABLED"), (value = "false"), (memo = "System default data modification must be enabled (true/false). The following features will be affected:\n\nUser maintenance: Prevent deletion for (manager, DpUser).\nRole maintenance: Prevent deletion for (ADMIN).\nRole list configuration: Prevent deletion for (Administrator).\nClient maintenance: Prevent deletion for (adminConsole, DpClient).\nGroup maintenance: Prevent deletion for (SMS (Admin Console))."));

//			-- 20240819, v4 AC IdP 的設定, Kevin Cheng
			createTsmpSetting((id = "AC_IDP_CUS_REVIEW_ENABLE"), (value = "true"), (memo = "Is the AC IdP CUS auto-create user feature enabled? (true/false)"));

//			-- 20241022, v4 BOT_DETECTION 的設定, Kevin Cheng
			createTsmpSetting((id = "CHECK_BOT_DETECTION"), (value = "false"), (memo = "Is the Bot Detection checker enabled? (true/false)"));

//			-- 20241101, v4 kibana 登入的方式, Zoe Lee
			createTsmpSetting((id = "KIBANA_AUTH"), (value = "basic"), (memo = "basic/session"));
			createTsmpSetting((id = "KIBANA_LOGIN_URL"), (value = "/xxxx/yyy/login"), (memo = "Kibana login URL."));
			createTsmpSetting((id = "KIBANA_LOGIN_REQUESTBODY"), (value = "{\n" +
					"    \"providerType\": \"basic\",\n" +
					"    \"providerName\": \"basic\",\n" +
					"    \"currentURL\": \"/\",\n" +
					"    \"params\": {\n" +
					"        \"username\": \"{{username}}\",\n" +
					"        \"password\": \"{{password}}\"\n" +
					"    }\n" +
					"}"), (memo = "Request body for Kibana login, where {{username}} replaces the username value and {{password}} replaces the password value."));

//			-- 20241107, v4 BOT_DETECTION 的設定, Kevin Cheng
			createTsmpSetting((id = "BOT_DETECTION_LOG"), (value = "false"), (memo = "Is the Bot Detection checker log output enabled? (true/false)"));

//			-- 20241114, RDB連線的APIM-default-DB是否可被使用(true/false), Tom
			createTsmpSetting((id = "APIM_DEFAULT_DB_ENABLED"), (value = "false"), (memo = "Is the RDB connection to APIM-default-DB enabled? (true/false)"));
//          -- 20241223, Log保留天數, Zoe Lee
			createTsmpSetting((id = "LOG_RETENTION_DAYS"), (value = "7"), (memo = "Log retention period"));

//			-- 2024/12/25 requestURI 開關設定
			createTsmpSetting((id = "REQUEST_URI_ENABLED"), (value = "false"), (memo = "Obtain request URI information at the Web server entry point. (gatewayfiler.java)"));

//	        -- 2024/12/27 HIGHWAY_THRESHOLD, Webber
			createTsmpSetting((id = "HIGHWAY_THRESHOLD"), (value = "1000"), (memo = "High speed channel for API requests"));

//			-- 2025/02/03 kibana  status API
			createTsmpSetting((id = "KIBANA_STATUS_URL"), (value = "/kibana/api/status"), (memo = " Kibana status API. defult: /kibana/api/status"));

//			-- 2025/03/25 ES log file status, Zoe Lee
			createTsmpSetting((id = "ES_LOGFILE_FAIL_RETRY"), (value = "false"), (memo = "When the file status is .fail (failed to write to ES after retrying 3 times)"));

//			-- 2025/03/25 ES log file write disk, Zoe Lee
			createTsmpSetting((id = "ES_CHECK_CONNECTION"), (value = "false"), (memo = "Whether to detect the ES connection when deciding whether to write the file to the disk."));

//			-- 2025/06/10 Audit log retention days, Kevin Cheng
			createTsmpSetting((id = "AUDIT_LOG_RETENTION_DAYS"), (value = "0"), (memo = "Audit log retention days, default: 0, setting to 0 means using gov_long time setting"));

			//  --2025/07/31 kibana allowlist ,Zoe Lee
			createTsmpSetting((id = "KIBANA_REFERER_ALLOWLIST"),(value = "/app/dashboards,/app/discover,/app/monitoring"),(memo = "Allowed Referer list for Kibana. Multiple paths should be separated by \",\"."));



		} catch (Exception e) {
			StackTraceUtil.logStackTrace(e);
			throw e;
		}

		return tsmpSettinglist;
	}


	/**
	 * TSMP_SETTING的值會依照License而調整
	 * @return
	 */
	public List<TsmpSettingVo> setSettingsByLicense(LicenseEditionTypeVo licenseEditionTypeVo) {
		currentLicense = licenseEditionTypeVo;

		for (String[][] settings : updateTsmpSettingArray) {
		    for (String[] setting : settings) {
				updateTsmpSetting(setting[1], setting[0], setting[2], setting[3]);
		    }
		}

    	List<TsmpSettingVo> tsmpSettingVos = new LinkedList<TsmpSettingVo>();
    	tsmpSettingVos = tsmpSettingUpdatelist.stream().collect(Collectors.toList());
    	tsmpSettingUpdatelist.clear();
		return tsmpSettingVos;

	}
	
	
	/**
	 * TSMP_SETTING的值會依照License而調整
	 * @param license
	 * @param id
	 * @param value
	 * @param memo 
	 * @return 
	 */
	private void updateTsmpSetting(String license, String id, String value, String memo) {
		if (license.equals(currentLicense.name())) {
			TsmpSettingVo tsmpSetting = new TsmpSettingVo();
			tsmpSetting.setId(id);
			tsmpSetting.setValue(value);
			tsmpSetting.setMemo(memo);
			tsmpSettingUpdatelist.add(tsmpSetting);
		}
	}

	
    protected void createTsmpSetting(String id, String value, String memo) {
            TsmpSettingVo tsmpSetting = new TsmpSettingVo();
            tsmpSetting.setId(id);
            tsmpSetting.setValue(value);
            tsmpSetting.setMemo(memo);
            tsmpSettinglist.add(tsmpSetting);
    }
    

}
