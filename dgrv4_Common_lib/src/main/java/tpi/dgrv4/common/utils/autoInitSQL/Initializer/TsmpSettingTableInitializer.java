package tpi.dgrv4.common.utils.autoInitSQL.Initializer;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import tpi.dgrv4.common.utils.LicenseEditionTypeVo;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.utils.autoInitSQL.vo.TsmpSettingVo;

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
		        {"TSMP_APILOG_FORCE_WRITE_RDB", LicenseEditionTypeVo.Alpha.name(), "false", "是否寫入API Log 到RDB"},
		        {"TSMP_APILOG_FORCE_WRITE_RDB", LicenseEditionTypeVo.Enterprise.name(), "false", "是否寫入API Log 到RDB"},
		        {"TSMP_APILOG_FORCE_WRITE_RDB", LicenseEditionTypeVo.Enterprise_Lite.name(), "false","是否寫入API Log 到RDB"},
		        {"TSMP_APILOG_FORCE_WRITE_RDB", LicenseEditionTypeVo.Express.name(), "false", "是否寫入API Log 到RDB"}
		    }
		};
	
	public List<TsmpSettingVo> insertTsmpSetting() {
		try {
	        String id;
	        String value;
	        String memo;
	        
	        createTsmpSetting("SERVICE_MAIL_ENABLE","true","主要smtp server設定, 本系統寄件者驗證流程是否啟動");
	        createTsmpSetting("SERVICE_MAIL_HOST","smtp.gmail.com","主要smtp server設定, SMTP Server URL");
	        createTsmpSetting("SERVICE_MAIL_PORT","587","主要smtp server設定, SMTP Server PORT");
	        createTsmpSetting("SERVICE_MAIL_AUTH","true","主要smtp server設定, mail server是否支持寄件者驗證");
	        createTsmpSetting("SERVICE_MAIL_STARTTLS_ENABLE","true","主要smtp server設定, 如果它可用就自動啟用TLS連接");
	     // -- 移除預設安裝的機敏資料
//	        createTsmpSetting("SERVICE_MAIL_USERNAME","system@elite-erp.com.tw","主要smtp server設定, 寄件者的顯示名稱");
	        createTsmpSetting("SERVICE_MAIL_USERNAME","example@tpisoftware.com","主要smtp server設定, 寄件者的顯示名稱");
	        createTsmpSetting("SERVICE_MAIL_PASSWORD","eliteTpower","主要smtp server設定, 寄件者的 email 密碼, (可點選\"更新\"或點選\"更新(ENC)\"加密後儲存)");
	     // -- 移除預設安裝的機敏資料
//	        createTsmpSetting("SERVICE_MAIL_FROM","system@elite-erp.com.tw","主要smtp server設定, 本系統寄件者郵件位址");
	        createTsmpSetting("SERVICE_MAIL_FROM","example@tpisoftware.com","主要smtp server設定, 本系統寄件者郵件位址");
	        createTsmpSetting("SERVICE_MAIL_X_MAILER","Thinkpower","主要smtp server設定, 發送郵件的程序名稱");
	        createTsmpSetting("SERVICE_SECONDARY_MAIL_ENABLE","true","次要smtp server設定, 本系統寄件者驗證流程是否啟動");
	        createTsmpSetting("SERVICE_SECONDARY_MAIL_HOST","smtp.gmail.com","次要smtp server設定, SMTP Server URL");
	        createTsmpSetting("SERVICE_SECONDARY_MAIL_PORT","587","次要smtp server設定, SMTP Server PORT");
	        createTsmpSetting("SERVICE_SECONDARY_MAIL_AUTH","true","次要smtp server設定, mail server是否支持寄件者驗證");
	        createTsmpSetting("SERVICE_SECONDARY_MAIL_STARTTLS_ENABLE","true","次要smtp server設定, 如果它可用就自動啟用TLS連接");
	     // -- 移除預設安裝的機敏資料
//	        createTsmpSetting("SERVICE_SECONDARY_MAIL_USERNAME","system@elite-erp.com.tw","次要smtp server設定, 寄件者的顯示名稱");
	        createTsmpSetting("SERVICE_SECONDARY_MAIL_USERNAME","example@tpisoftware.com","次要smtp server設定, 寄件者的顯示名稱");
	        createTsmpSetting("SERVICE_SECONDARY_MAIL_PASSWORD","eliteTpower","次要smtp server設定, 寄件者的 email 密碼, (可點選\"更新\"或點選\"更新(ENC)\"加密後儲存)");
	     // -- 移除預設安裝的機敏資料	        
//	        createTsmpSetting("SERVICE_SECONDARY_MAIL_FROM","system@elite-erp.com.tw","次要smtp server設定, 本系統寄件者郵件位址");
	        createTsmpSetting("SERVICE_SECONDARY_MAIL_FROM","example@tpisoftware.com","次要smtp server設定, 本系統寄件者郵件位址");
	        createTsmpSetting("SERVICE_SECONDARY_MAIL_X_MAILER","Thinkpower","次要smtp server設定, 發送郵件的程序名稱");
	        createTsmpSetting("TSMP_EDITION","Cn88-nNO8-xx8u-un88-nVoF-Fr48-80rc-L5rF-xN#8-e1=x-6#xo-=d4#-2!=n-!#2!-=!!!-!!!","TSMP license key");
	        // 這段 hardcoded IP 會被 SonarQube 標記為 Security Hotspots, 故改為 DNS 名稱
	        createTsmpSetting("LDAP_URL","ldap://ldap.example.com:389","LDAP login URL");
	        createTsmpSetting("LDAP_DN","uid={{0}},dc=tstpi,dc=com","ldap登入的使用者DN");
	        createTsmpSetting("LDAP_TIMEOUT","3000","ldap登入的連線timeout,單位毫秒");
	        createTsmpSetting("LDAP_CHECK_ACCT_ENABLE","false","LDAP檢查帳號功能是否啟用 true/false");
	        ///* 交給 resetAdminConsoleData() 建立了
	        createTsmpSetting("TSMP_AC_CLIENT_ID","YWRtaW5Db25zb2xl","Login AC account (do not modify)");
			createTsmpSetting("TSMP_AC_CLIENT_PW","dHNtcDEyMw==","AC login password (do not modify)");
	        // */
	        createTsmpSetting("TSMP_FAIL_THRESHOLD","6","允許User密碼錯誤次數上限");
	        createTsmpSetting("SSO_PKCE","true","PKCE等級AuthCode驗證是否啟用 true/false");
	        createTsmpSetting("SSO_DOUBLE_CHECK","true","Double check驗證是否啟用 true/false");
	        createTsmpSetting("SSO_AUTO_CREATE_USER","false","自動建立User資料是否啟用 true/false");
	        createTsmpSetting("TSMP_DPAA_RUNLOOP_INTERVAL","1","RUNLOOP每次循環間隔秒數sec,設為0則停用告警偵測");
	        createTsmpSetting("TSMP_COMPOSER_ADDRESS","http://tsmp-composer:1880","COMPOSER 廣播使用,以逗號區隔 \n COMPOSER v4Dev環境:https://10.20.30.88:18440");
	        createTsmpSetting("TSMP_DELETEMODULE_ALERT","false","刪除MODULE時提示要刪除相關資料");
	        createTsmpSetting("TSMP_AC_CONF","{\"dp\":0,\"net\":false}","登入時，後端提供給AC的設定值");
//	      -- 20221118, v4, 增加 RDB 版本的 API Log
	        createTsmpSetting("TSMP_APILOG_FORCE_WRITE_RDB","false","是否寫入API Log 到RDB");
	        // 這段 hardcoded IP 會被 SonarQube 標記為 Security Hotspots, 故改為 DNS 名稱
	        createTsmpSetting("DGR_LOGOUT_URL","","Custom login page url, {{scheme}}://{{ip}}:{{port}}/{{path}} ex: \"https://hostname:port/dgr-cus-scbank-ac/\"");
	        createTsmpSetting("TSMP_COMPOSER_PORT","8440","給前端頁面使用用的PORT，主要是給AC使用");
	        createTsmpSetting("TSMP_COMPOSER_PATH","/website/composer","給前端頁面使用用的路徑，主要是給AC使用");
	        createTsmpSetting("TSMP_PROXY_PORT","4944","目前無使用，保留，當初功能性與TSMP_REPORT_ADDRESS相同");
	        createTsmpSetting("CUS_MODULE_EXIST","false","客製包是否存在 true/false");
	        createTsmpSetting("CUS_FUNC_ENABLE1","false","介接功能是否啟用 true/false");
	        createTsmpSetting("CUS_MODULE_NAME1","","客製包 Moudle 名稱");
	        // 這段 hardcoded IP 會被 SonarQube 標記為 Security Hotspots, 例如: "192.168.0.0/23,192.168.0.0/24,127.0.0.0/24", 故改為 a.b.0.0 表示
	        createTsmpSetting("UDPSSO_LOGIN_NETWORK","a.b.0.0/23,a.b.0.0/24,a.0.0.0/24","Network segments that can be logged in, multiple CIDRs are separated by commas; CIDR notation can be found at wiki: \"https://en.wikipedia.org/wiki/Classless_Inter-Domain_Routing\"");
	        createTsmpSetting("CLIENT_CREDENTIALS_DEFAULT_USERNAME","true","client_credentials取token預設userName是否啟用 true/false");
	        createTsmpSetting((id = "TSMP_REPORT_ADDRESS"), (value = "15601"), (memo = "查詢Kibana報表網址PORT, v3(38451),v4(15601)"));
	        createTsmpSetting((id = "LOGOUT_API"), (value = ""), (memo = "客製登出url{{scheme}}://{{ip}}:{{port}}/{{path}} ex: \"https://127.0.0.1:8442/dgr-cus-scbank/scb/logout\""));
//		  -- 20220303, Audit Log 增加參數, Mini Lee
	        createTsmpSetting((id = "AUDIT_LOG_ENABLE"), (value = "true"), (memo = "Audit Log記錄功能是否啟用 (true/false)"));

//	      -- 2022/04/13 DGRKEEPER設定
	        createTsmpSetting((id = "DGRKEEPER_IP"), (value = "127.0.0.1"), (memo = "DGRKEEPER Server主機IP"));
	        createTsmpSetting((id = "DGRKEEPER_PORT"), (value = "8085"), (memo = "DGRKEEPER Server主機PORT"));

//	      -- 2022/04/13 Online Console開關設定
	        createTsmpSetting((id = "TSMP_ONLINE_CONSOLE"), (value = "true"), (memo = "Online Console開關"));

//	      -- 2022/04/19 Logger Level設定
	        createTsmpSetting((id = "LOGGER_LEVEL"), (value = "INFO"), (memo = "Logger的log輸出等級設定"));

//	      -- 20220427, 檢查器的設定, tom
	        createTsmpSetting((id = "CHECK_XSS_ENABLE"), (value = "true"), (memo = "XSS檢查器是否啟用 (true/false)"));
	        createTsmpSetting((id = "CHECK_XXE_ENABLE"), (value = "true"), (memo = "XXE檢查器是否啟用 (true/false)"));
	        createTsmpSetting((id = "CHECK_SQL_INJECTION_ENABLE"), (value = "true"), (memo = "SQL Injection檢查器是否啟用 (true/false)"));
	        createTsmpSetting((id = "CHECK_IGNORE_API_PATH_ENABLE"), (value = "true"), (memo = "指定API路徑略過所有檢查器是否啟用 (true/false)"));
	        createTsmpSetting((id = "CHECK_API_STATUS_ENABLE"), (value = "true"), (memo = "API開關是否啟用 (true/false)"));
	        createTsmpSetting((id = "CHECK_TRAFFIC_ENABLE"), (value = "true"), (memo = "Traffic檢查器是否啟用 (true/false)"));
	        createTsmpSetting((id = "IGNORE_API_PATH"), (value = "/,/tptoken/oauth/token,/ssotoken/**,/v3/**,/shutdown/**,/version/**,/onlineconsole1/**,/onlineConsole/**,/udpssotoken/**,/cus/**"), (memo = "指定API路徑略過所有檢查器設定(多筆以逗號(,)隔開)"));

//	      -- 20220606, ES的設定, tom chu
	        // 這段 hardcoded IP 會被 SonarQube 標記為 Security Hotspots, 故改為 DNS 名稱
	        createTsmpSetting((id = "ES_URL"), (value = "https://es.example.com:19200/"), (memo = "ES URL, the last line must be /, multiple groups are separated by commas (,),EX:https://es.example.com:19200/,https://es.example.com:29200/"));
	        createTsmpSetting((id = "ES_ID_PWD"), (value = "ENC(cGxlYXNlIHNldCB5b3VyIGVzIGlkIGFuZCBwYXNzd29yZCwgU2V0dGluZyBpcyBFU19JRF9QV0Q=)"), (memo = "ES的ID:PWD為組合並以Base64加密後, 請點選ENC按鈕進行加密,URL多組這就多組,在ENC加密前以逗號,隔開,EX:ENC(id1:pwd1,id2:pwd2)"));
	        createTsmpSetting((id = "ES_TEST_TIMEOUT"), (value = "3000"), (memo = "ES測試連線的timeout"));
	        createTsmpSetting((id = "ES_MBODY_MASK_FLAG"), (value = "false"), (memo = "對全部做mbody遮罩,true為遮罩,false為不遮罩"));
	        createTsmpSetting((id = "ES_IGNORE_API"), (value = ""), (memo = "ES不紀錄的API,多組以逗號(,)隔開,值為moduleName/apiId"));
	        createTsmpSetting((id = "ES_MBODY_MASK_API"), (value = ""), (memo = "對tsmpc的API做mbody遮罩,多組以逗號(,)隔開,值為moduleName/apiId"));
	        createTsmpSetting((id = "ES_TOKEN_MASK_FLAG"), (value = "true"), (memo = "對token遮罩,true為遮罩,false為不遮罩"));
	        createTsmpSetting((id = "ES_MAX_SIZE_MBODY_MASK"), (value = "0"), (memo = "超過mbody內容值byte的length自動對mbody遮罩,單位為byte,值10(含)以下為不遮罩"));
	        createTsmpSetting((id = "ES_DGRC_MBODY_MASK_URI"), (value = ""), (memo = "對dgrc的URI(值含/dgrc)做mbody遮罩,多組以逗號(,)隔開,值為/dgrc/aa/bb/cc"));
	        createTsmpSetting((id = "ES_DGRC_IGNORE_URI"), (value = ""), (memo = "對dgrc的ES不紀錄的URI(值含/dgrc),多組以逗號(,)隔開,值為/dgrc/aa/bb/cc"));
	        createTsmpSetting((id = "ES_LOG_DISABLE"), (value = "true"), (memo = "是否禁止紀錄ES的LOG,true為是,false為否"));
	        createTsmpSetting((id = "DGR_PATHS_COMPATIBILITY"), (value = "2"), (memo = "url路徑相容,0:tsmpc only;1:dgrc only;2:tsmpc與dgrc相容,預設2"));
	        
//	      -- 20220712, token的設定, Mini Lee
	        createTsmpSetting((id = "DGR_TOKEN_JWE_ENABLE"), (value = "false"), (memo = "token JWE加密是否啟用,預設為false(JWS) (true/false)"));

//	      -- 20220801, token的設定, Mini Lee
	        createTsmpSetting((id = "DGR_TOKEN_WHITELIST_ENABLE"), (value = "false"), (memo = "token 白名單是否啟用,預設為false (true/false)"));

//	      -- 20220805, API訊息設定, Mini Lee
	        createTsmpSetting((id = "DGR_TW_FAPI_ENABLE"), (value = "false"), (memo = "API訊息使用TW Open Banking格式是否啟用,預設為false (true/false)"));
	                               
//	      -- 20220815, fixedCache時間, Tom
	        createTsmpSetting((id = "FIXED_CACHE_TIME"), (value = "1"), (memo = "Fixed Cache的時間,單位為分鐘"));
	        
//	      -- 20220829, es monitor host的設定, tom
	        createTsmpSetting((id = "ES_SYS_TYPE"), (value = "DGR_LOCAL"), (memo = "用來視別資料用途"));
	        createTsmpSetting((id = "ES_MONITOR_DISABLE"), (value = "true"), (memo = "是否禁止紀錄監控,true為是,false為否"));
	        
//	      -- 20220926, 查詢DGR監控資料的時間, Tom
	        createTsmpSetting((id = "DGR_QUERY_MONITOR_DAY"), (value = "7"), (memo = "查詢DGR監控資料的時間,單位為天"));
//	      -- 20221018, 重設Client API配額的頻率, Zoe
	        createTsmpSetting((id = "DGR_CLIENT_QUOTA_FRQ"), (value = "D"), (memo = "重設Client API配額的頻率,N:無,D:每日,SD:每週日,MD:每週一,MT:每月1號,預設D"));
	         
//	      -- 20221018, v4 將 application properties 的值加入 Setting, Kevin Cheng
	        createTsmpSetting((id = "DEFAULT_PAGE_SIZE"), (value = "20"), (memo = "Default Page Size"));
	        createTsmpSetting((id = "MAIL_BODY_API_FAIL_SERVICE_MAIL"), (value = "service@thinkpower.com.tw"), (memo = "由本系統寄發的信件內容尾端中, 提示的客服 email."));
	        createTsmpSetting((id = "MAIL_BODY_API_FAIL_SERVICE_TEL"), (value = "+886-2-8751-1610"), (memo = "由本系統寄發的信件內容尾端中, 提示的客服電話."
	        		+ ""));
	        createTsmpSetting((id = "ERRORLOG_KEYWORD"), (value = "tpi.dgrv4,com.thinkpower"), (memo = "只印出有包含特定文字的錯誤訊息,多組以逗號(,)隔開"));
	        createTsmpSetting((id = "FILE_TEMP_EXP_TIME"), (value = "3600000"), (memo = "暫存檔案過期時間(ms)"));
	        createTsmpSetting((id = "AUTH_CODE_EXP_TIME"), (value = "600000"), (memo = "Auth code 過期時間(ms)"));
	        createTsmpSetting((id = "QUERY_DURATION"), (value = "30"), (memo = "ES查詢日期區間上限"));
	        createTsmpSetting((id = "SHUTDOWN_ENDPOINT_ALLOWED_IPS"), (value = "127.0.0.1,0:0:0:0:0:0:0:1"), (memo = "允許存取 shutdown endpoint 的 IP host 清單, 以逗號(,)隔開, 未設定則無人可以呼叫"));
	        createTsmpSetting((id = "MAIL_SEND_TIME"), (value = "600000"), (memo = "多久後寄發Email(ms),預設10分鐘"));
	        
//	      -- 20221129, 將 CORS, Access-Control-Allow-Origin 的值加入 Setting, Kevin Cheng   
	        createTsmpSetting((id = "DGR_CORS_VAL"), (value = "*"), (memo = "default為 * ， 但可以改為網域，例如: https://dgRv4.io/"));
	        createTsmpSetting((id = "KIBANA_PWD"), (value = "ENC(mucabhaTTTk7YuJnl8OqFDNi98EFp1eMutQpzUbDuJeqAtx3poP8yIYMQ+cMbfiIGUBOI7SocRabEMNvnRZ+86b/MQ0zJJTAJtO39jtXKJoLAfdBYF+8+HmEVnkVi/ws7MGEEhY+TON6/lXovoD9NEZOMTLam89Q7lnfq2fG3i8YdCvX4eja6IWzd4jdyehWiq/yi6DkKmnlqApLSN8/ykLxQvY/eeIds/DKrUWRy1Q10FthFASl9XGIW1EhjcoWwb9aNkhvtrnTMkTG1be5i/+W3AClRZ72V/ZJUy6LJYP4sNub68VP812XLfsyMS/eL1axQO3NEWhQRrpHUjNRxg==)"), (memo = "Kibana密碼(可點選\"更新\"或點選“更新(ENC)\"加密後儲存)"));
	        
//	      -- 20221206, 加入 KIBANA_PWD 可為 ENC 或明文, 調整Kibana主機設定欄位名稱, Kevin Cheng
	        createTsmpSetting((id = "KIBANA_VERSION"), (value ="7.10"), (memo = "Kibana版本"));
	        createTsmpSetting((id = "KIBANA_USER"), (value = "tpuser"), (memo = "Kibana帳號"));
	        
	        createTsmpSetting((id = "KIBANA_TRANSFER_PROTOCOL"), (value = "https"), (memo = "Kibana的http協定"));
	        // 這段 hardcoded IP 會被 SonarQube 標記為 Security Hotspots, 故改為 DNS 名稱
	        createTsmpSetting((id = "KIBANA_HOST"), (value = "kibana.example.com"), (memo = "The IP address of the Kibana host"));
	        createTsmpSetting((id = "KIBANA_PORT"), (value = "15601"), (memo = "Kibana主機的Port"));
	        
//		  -- 20230104, v4 SSO IdP 的設定, Mini Lee
	        createTsmpSetting((id = "AC_IDP_REVIEWER_MAILLIST"), (value = "example@tpisoftware.com"), (memo = "AC IdP User的審核者,多組以逗號(,)隔開"));
	        createTsmpSetting((id = "AC_IDP_MSG_URL"), (value = "https://localhost:8080/dgrv4/ac4/idpsso/errMsg"), (memo = "前端AC IdP errMsg顯示訊息的URL"));
	        createTsmpSetting((id = "AC_IDP_REVIEW_URL"), (value = "https://localhost:8080/dgrv4/ssotoken/acidp/acIdPReview"), (memo = "AC IdP Review審核的URL"));
	        createTsmpSetting((id = "AC_IDP_ACCALLBACK_URL"), (value = "https://localhost:8080/dgrv4/ac4/idpsso/accallback"), (memo = "前端AC IdP accallback的URL"));
	        createTsmpSetting((id = "AC_IDP_LDAP_REVIEW_ENABLE"), (value = "true"), (memo = "AC IdP LDAP 寄發審核信流程 及 自動建立 User 功能是否啟用 (true/false)"));
	      
	        createTsmpSetting((id = "DGR_AC_LOGIN_PAGE"), (value = "redirect:/dgrv4/ac4/login"), (memo = "The digiRunning login page needs to be restarted after modification.The modification options include \"redirect:/dgrv4/ac4/login\" and \"redirect:/dgrv4/ac4/ldap\". "));
		
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
		    createTsmpSetting((id = "DGR_COOKIE_TOKEN_ENABLE"), (value = "true"), (memo = "Whether the cookie token is enabled, the default is true (true/false)"));
	        
//		  -- 20230608 DPKEEPER設定
	        createTsmpSetting((id = "DPKEEPER_IP"), (value = "127.0.0.1"), (memo = "DPKEEPER Server主機IP"));
	        createTsmpSetting((id = "DPKEEPER_PORT"), (value = "8086"), (memo = "DPKEEPER Server主機PORT"));

//		  -- 20230609, 安全性檢查的設定, Mini Lee
			createTsmpSetting((id = "DGR_HOST_HEADER"), (value = "*"), (memo = "檢查host header,以防止Host Header Injection,default為 *,可改為網域或IP,多組以逗號(,)隔開,ex:10.20.30.88:1920,10.20.30.88:2920"));
		
//		  -- 20230609, 將 Content-Security-Policy 的網域值加入 Setting, Mini Lee
			createTsmpSetting((id = "DGR_CSP_VAL"), (value = "*"), (memo = "Content-Security-Policy header的值,default為 *, * 為不加CSP , 若要加CSP請填入上  \"default-src * 'self' 'unsafe-inline'; img-src https://* 'self' data:;\" , default-src * 此星號可替換為網域或IP,多組以空格(\" \")隔開,ex:https://10.20.30.88:1920 https://10.20.30.88:2920"));
//        --20230804, GatewayFilter檢查器開關 
			createTsmpSetting((id = "CHECK_JTI_ENABLE"), (value = "true"), (memo = "當Request包含Authorization時將會檢查此jti於DB中是否過期,若不含此HTTP Header則不檢查, default檢查功能為啟用(true),反之則為停用(false)"));
			
	        createTsmpSetting((id = "PROFILEUPDATE_INVALIDATE_TOKEN"), (value = "false"), (memo = "是否於 User Data被異動時, 同步註銷其已核發的token, default為不註銷(false),反之則執行註銷動作(true)"));

//			-- 20230817, Composer Log File Rotation + Composer log 開關 , Min
	        createTsmpSetting((id = "COMPOSER_LOG_INTERVAL"), (value = "1d"), (memo = "rotate 間隔。格式：1s(秒)、2m(分)、3h(時)、4d(天)、5M(月)。當此 log 檔存在 {COMPOSER_LOG_INTERVAL} 時間後，無論此 log 檔大小為何，該檔會被壓縮並開新的 log 檔繼續寫入 log。"));
	        createTsmpSetting((id = "COMPOSER_LOG_SIZE"), (value = "100"), (memo = "單位：KB。當此 log 檔大於 {COMPOSER_LOG_SIZE} KB，則此 log 檔會被壓縮並開新的 log 檔繼續寫入 log。"));
	        createTsmpSetting((id = "COMPOSER_LOG_SWICTH"), (value = "true"), (memo = "Composer log 的總開關。true：開，false：關。Composer log 的總開關。"));
	        createTsmpSetting((id = "COMPOSER_LOG_MAX_FILES"), (value = "7"), (memo = "保留幾個 rotation log 文件(or壓縮檔)，只能為正整數。"));
	        createTsmpSetting((id = "COMPOSER_REQUEST_TIMEOUT"), (value = "240000"), (memo = "請求 COMPOSER API 超時時長。單位：毫秒。"));
	        
//			-- 限制不能變更及刪除自己的帳號開關 , Kevin K	        	        
	        createTsmpSetting((id = "USER_UPDATE_BY_SELF"), (value = "false"), (memo = "限制不能變更及刪除自己的帳號(預設為false關閉功能)"));

//			-- 20230828, 執行AutoInitSQL開關, Min
	        createTsmpSetting((id = "AUTO_INITSQL_FLAG"), (value = "true"), (memo = "每當啟動digiRunner Instance時, 於啟動階段自動執行initSQL(), default為執行(true), 反之則為不執行(false)"));
	        
//			-- 20231002, API DASHBAORD批次處理數量, TOM
	        createTsmpSetting((id = "API_DASHBOARD_BATCH_QUANTITY"), (value = "500000"), (memo = "統計api dashboard的分天月,批次處理的數量"));

//			-- 20231130, Open api key 展期 API URL, Kevin Cheng
	        createTsmpSetting((id = "OAK_EXPI_URL"), (value = "https://localhost:8080/website/dp_api/DPF0073"), (memo = "Open api key extension API URL. \n  Default value: https://localhost:8080/website/dp_api/DPF0073"));

//	        -- 20231207, X-Api-Key記錄明文是否啟用,預設為false (true/false), Mini Lee
	        createTsmpSetting((id = "X_API_KEY_PLAIN_ENABLE"), (value = "false"), (memo = "Whether X-Api-Key enables recording plaintext, default false (true/false)"));

//	        -- 20231228, AWS API 狀態flag, Zoe Lee     
	        createTsmpSetting((id = "DGR_ON_AWS"), (value = "true"), (memo = " AWS API flag."));
	        
	        createTsmpSetting((id = "AWS_PUBLIC_KEY"), (value = "-----BEGIN PUBLIC KEY-----\nMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDdlatRjRjogo3WojgGHFHYLugd\nUWAY9iR3fy4arWNA1KoS8kVw33cJibXr8bvwUAUparCwlvdbH6dvEOfou0/gCFQs\nHUfQrSDv+MuSUMAe8jzKE4qW+jK+xQU9a03GUnKHkkle+Q0pX/g6jXZ7r1/xAK5D\no2kQ+X5xK9cipRgEKwIDAQAB\n-----END PUBLIC KEY-----"), (memo = " AWS PUBLIC KEY."));
//			-- 20240124, KIBANA報表 前綴 , Zoe Lee
	        createTsmpSetting((id = "KIBANA_REPORTURL_PREFIX"), (value = "/kibana"), (memo = "kibana report url prefix."));
	        
//			-- 20240222, v4 AC IdP 的設定, Mini Lee
	        createTsmpSetting((id = "AC_IDP_API_REVIEW_ENABLE"), (value = "true"), (memo = "AC IdP API 寄發審核信流程 及 自動建立 User 功能是否啟用 (true/false)"));
	        
//			-- 20240506, 異動系統預設資料請先解鎖, Webber Luo
	        createTsmpSetting((id = "DEFAULT_DATA_CHANGE_ENABLED"), (value = "false"), (memo = "異動系統預設資料須啟用(true/false)，影響功能如下: 1.使用者維護，禁止(manager、DpUser)刪除操作。 2.角色維護，禁止(ADMIN)刪除操作。 3.角色清單設定，禁止(Administrator)刪除操作。 4.用戶端維護，禁止(adminConsole、DpClient)刪除操作。 5.群組維護，禁止(SMS(Admin Console))刪除操作。"));
       
//			-- 20240819, v4 AC IdP 的設定, Kevin Cheng
	        createTsmpSetting((id = "AC_IDP_CUS_REVIEW_ENABLE"), (value = "true"), (memo = "AC IdP CUS 自動建立 User 功能是否啟用 (true/false)"));

//			-- 20241022, v4 BOT_DETECTION 的設定, Kevin Cheng
	        createTsmpSetting((id = "CHECK_BOT_DETECTION"), (value = "false"), (memo = "Bot Detection檢查器是否啟用 (true/false)"));

//			-- 20241101, v4 kibana 登入的方式, Zoe Lee
			createTsmpSetting((id = "KIBANA_AUTH"), (value = "basic"), (memo = "basic/session"));
			createTsmpSetting((id = "KIBANA_LOGIN_URL"), (value = "/xxxx/yyy/login"), (memo = "Kibana登入URL"));
			createTsmpSetting((id = "KIBANA_LOGIN_REQUESTBODY"), (value = "{\n" +
					"    \"providerType\": \"basic\",\n" +
					"    \"providerName\": \"basic\",\n" +
					"    \"currentURL\": \"/\",\n" +
					"    \"params\": {\n" +
					"        \"username\": \"{{username}}\",\n" +
					"        \"password\": \"{{password}}\"\n" +
					"    }\n" +
					"}"), (memo = "Kibana登入要帶的requestBody，以{{username}}代替 username 的value  以{{password}} 代替  password 的value "));
	        
//			-- 20241107, v4 BOT_DETECTION 的設定, Kevin Cheng
	        createTsmpSetting((id = "BOT_DETECTION_LOG"), (value = "false"), (memo = "Bot Detection 檢查器 Log 是否印出 (true/false)"));
	        
//			-- 20241114, RDB連線的APIM-default-DB是否可被使用(true/false), Tom
	        createTsmpSetting((id = "APIM_DEFAULT_DB_ENABLED"), (value = "false"), (memo = "RDB連線的APIM-default-DB是否啟用(true/false)"));
//          -- 20241223, Log保留天數, Zoe Lee
			createTsmpSetting((id = "LOG_RETENTION_DAYS"), (value = "7"), (memo = "Log保留天數"));
			
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
