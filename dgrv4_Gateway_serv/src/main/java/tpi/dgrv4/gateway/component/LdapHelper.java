package tpi.dgrv4.gateway.component;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.JWKcodec.TrustAllHostnameVerifier;
import tpi.dgrv4.codec.utils.JWKcodec.TrustAllManager;
import tpi.dgrv4.common.utils.ServiceUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * [ZH]AD / LDAP 的共用程式 <br>
 * [EN]AD / LDAP utility <br>
 * 
 * @author mini.lee <br>
 */

@Component
public class LdapHelper {
	public static class LdapAdAuthData {
		public String errMsg;
		public String logMsg;
		public String name;
		public String mail;
	}
 
	/**
	 * [ZH] AD / LDAP 登入認證 <br>
	 * 檢查 LDAP User 帳號 & 密碼是否正確 <br>
	 * [EN] AD / LDAP login authentication <br>
	 * Check if the LDAP User account & password are correct <br>
	 * 
	 * @param ldapUrl       [ZH]LDAP登入的URL [EN]LDAP login URL
	 * @param ldapDn        [ZH]LDAP登入的使用者DN [EN]LDAP login user DN
	 * @param ldapBaseDn    [ZH]LDAP 基礎識別名稱 [EN]LDAP base identification name
	 * @param ldapTimeout   [ZH]LDAP登入的連線timeout,單位毫秒 [EN]LDAP login connection
	 *                      timeout, in milliseconds
	 * @param reqUserName   [ZH]User 輸入的 username [EN]User input username
	 * @param userMima      [ZH]User 輸入的密碼 [EN]User input password
	 * @param isGetUserInfo [ZH]是否要找出 user 在 LDAP / AD 中的資料 [EN]Whether to find the
	 *                      user's information in LDAP / AD
	 * @param orderNo       [ZH]當 AC IdP type 為 "MLDP" 時,才有值;其他,都為 null [EN]When AC
	 *                      IdP type is "MLDP", it has a value; otherwise, it is
	 *                      null
	 */
	public LdapAdAuthData checkLdapAuth(String ldapUrl, String ldapDn, String ldapBaseDn, int ldapTimeout,
			String reqUserName, String userMima, boolean isGetUserInfo, Integer orderNo) throws Exception {
		// [ZH] LDAP 建立連線...
		// [EN] LDAP establishing connection...
		TPILogger.tl.debug("...LDAP establishing connection...");
		
		boolean isLdaps = ldapUrl.toLowerCase().startsWith("ldaps://");
		
		// 停用 SSL 驗證
		disableSSLVerification();
        
		LdapAdAuthData ldapAdAuthData = new LdapAdAuthData();
		String errMsg = null;
		String logMsg = null;
		DirContext ctx = null;
		
		try  {
			
			// [ZH] 將 user 輸入的 username 取替掉 DN 的 {{0}}, 例如: uid={{0}},dc=example,dc=com 變為 uid=user1,dc=example,dc=com
			// [EN] Replace {{0}} of DN with the username entered in User. For example, uid={{0}},dc=example,dc=com becomes uid=user1,dc=example,dc=com
			Map<String, String> ldapParamMap = new HashMap<>();
			ldapParamMap.put("0", reqUserName);
			ldapDn = ServiceUtil.buildContent(ldapDn, ldapParamMap);
			
			Hashtable<String, String> ldapHashEnv = new Hashtable<>();
			
			ldapHashEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			
			// LDAP URL
			ldapHashEnv.put(Context.PROVIDER_URL, ldapUrl);
			
			// [ZH] LDAP訪問安全級別(none,simple,strong) 
			// [EN] LDAP access security level (none, simple, strong)
			ldapHashEnv.put(Context.SECURITY_AUTHENTICATION, "simple"); 
			
			// [ZH] AD的使用者名稱 
			// [EN] AD Username
			ldapHashEnv.put(Context.SECURITY_PRINCIPAL, ldapDn); 
			
			// [ZH] AD的使用者密碼 
			// [EN] AD user password
			ldapHashEnv.put(Context.SECURITY_CREDENTIALS, userMima); 
			
	        // 針對 LDAPS 的特殊設定
	        if (isLdaps) {
	        	ldapHashEnv.put(Context.SECURITY_PROTOCOL, "ssl");
	        }
			
			// [ZH] 連線超時設定 
			// [EN] Connection timeout setting
			ldapHashEnv.put("com.sun.jndi.ldap.connect.timeout", ldapTimeout + "");
			
			// [ZH] 連線成功代表驗證成功
			// [EN] Successful connection means successful verification
			ctx = new InitialLdapContext(ldapHashEnv, null);
			
			// [ZH] 驗證成功
			// [EN] Verification successful
			String successMsg = "LDAP verification suceesfully.";
			successMsg = getLogMsg(successMsg, reqUserName, ldapUrl, ldapDn, orderNo);
			TPILogger.tl.debug(successMsg);

			if (isGetUserInfo) {
				// [ZH] 取得 user info
				// [EN] Get user info
				ldapAdAuthData = getUserInfo(ldapAdAuthData, reqUserName, ldapDn, ctx, ldapBaseDn);
			}

		} catch (AuthenticationException e) {
			// [ZH] LDAP驗證失敗
			// [EN] LDAP authentication failed
			errMsg = "LDAP verification failed. User account or password is incorrect."; 
			logMsg = getLogMsg(errMsg, reqUserName, ldapUrl, ldapDn, orderNo) + "\n"
					+ StackTraceUtil.logStackTrace(e);
			TPILogger.tl.debug(logMsg);
			
		} catch (CommunicationException e) {
			// [ZH] LDAP連線失敗
			// [EN] LDAP connection failed
			errMsg = "LDAP connection failed.";
			logMsg = getLogMsg(errMsg, reqUserName, ldapUrl, ldapDn, orderNo) + "\n"
					+ StackTraceUtil.logStackTrace(e);
			TPILogger.tl.debug(logMsg);
			
		} catch (Exception e) {
			// [ZH] LDAP連線錯誤
			// [EN] LDAP connection error
			errMsg = "LDAP connection error.";
			logMsg = getLogMsg(errMsg, reqUserName, ldapUrl, ldapDn, orderNo)+ "\n"
					+ StackTraceUtil.logStackTrace(e);
			TPILogger.tl.error(logMsg);
			
		} finally {
			try {
				if (ctx != null) {
					ctx.close();
					ctx = null;
				}
			} catch (Exception e) {
				TPILogger.tl.error("LDAP close dirContext error: " + StackTraceUtil.logStackTrace(e));
			}
		}
		
		ldapAdAuthData.errMsg = errMsg;
		ldapAdAuthData.logMsg = logMsg;
		
		return ldapAdAuthData;
	}
	
	private String getLogMsg(String msg, String userName, String ldapUrl, String ldapDn, Integer orderNo) {
		StringBuffer sb = new StringBuffer();
		sb.append("..." + msg + "\n");
		sb.append("\t...userName: " + userName + "\n");
		if(orderNo != null) {
			sb.append("\t...orderNo: " + orderNo + "\n");
		}
		sb.append("\t...ldapUrl: " + ldapUrl + "\n");
		sb.append("\t...ldapDn: " + ldapDn + "\n");
		
		return sb.toString();
	}
	
	private String getLogMsg2(String msg1, String ldapDn, String reqUserName, String searchBase) {
		return getLogMsg2(msg1, ldapDn, reqUserName, searchBase, null);
	}
			
	private String getLogMsg2(String msg1, String ldapDn, String reqUserName, String searchBase, String msg2) {
		StringBuffer sb = new StringBuffer();
		sb.append("..." + msg1 + "\n");
		sb.append("\t...ldapDn: " + ldapDn + "\n");
		sb.append("\t...searchBase: " + searchBase + "\n");
		sb.append("\t...sAMAccountName: " + reqUserName + "\n");
		if(msg2 != null) {
			sb.append("\t" + msg2 + "\n");
		}
		return sb.toString();
	}
	
	/**
	 * [ZH] 取得 user 在 LDAP / AD 中記錄的資料
	 * [EN] Get user information recorded in LDAP / AD
	 */
	private LdapAdAuthData getUserInfo(LdapAdAuthData ldapAdAuthData, String reqUserName, String ldapDn,
			DirContext ctx, String searchBase) {
        try {
			if (!StringUtils.hasLength(searchBase)) {
				// [ZH] 設定檔缺少參數 '%s'
				// [EN] Configuration file missing parameter '%s'
				String errMsg = "LDAP search userInfo failed."
						+ String.format(AcIdPHelper.MSG_THE_PROFILE_IS_MISSING_PARAMETERS, "ldap_base_dn");
				String logMsg = getLogMsg2(errMsg, ldapDn, reqUserName, searchBase);
				TPILogger.tl.debug(logMsg);

				return ldapAdAuthData;
			}
 
			// checkmarx, LDAP Injection
			String filter = "sAMAccountName={0}";
			Object[] filterArgs = new Object[] { reqUserName };
			
			SearchControls searchControls = getSearchControls();
			NamingEnumeration<SearchResult> answer = ctx.search(searchBase, filter, filterArgs,
					searchControls);

            if (answer.hasMore()) {
				Attributes attrs = answer.next().getAttributes();
				Attribute nameAttr = attrs.get("name");
				Attribute displayNameAttr = attrs.get("displayName");
				Attribute mailAttr = attrs.get("mail");

				// [ZH] 優先使用 "name", 若不存在再找 "displayName", 若都不存在就不提供屬性
				// [EN] Use "name" first, if it does not exist then look for "displayName", if
				// neither of them exists then no attribute is provided
				if (nameAttr != null) {
					ldapAdAuthData.name = (String) nameAttr.get();
				} else if (displayNameAttr != null) {
					ldapAdAuthData.name = (String) displayNameAttr.get();
				} else {
					String errMsg = "LDAP user cannot find name and displayName.";
					String logMsg = getLogMsg2(errMsg, ldapDn, reqUserName, searchBase);
					TPILogger.tl.debug(logMsg);
				}

				if (mailAttr != null) {
					ldapAdAuthData.mail = (String) mailAttr.get();
				} else {
					String errMsg = "LDAP user cannot find mail.";
					String logMsg = getLogMsg2(errMsg, ldapDn, reqUserName, searchBase);
					TPILogger.tl.debug(logMsg);
				}
				
            } else {
				String errMsg = "LDAP user not found.";
				String logMsg = getLogMsg2(errMsg, ldapDn, reqUserName, searchBase);
				TPILogger.tl.debug(logMsg);
            }
            
        } catch (Exception ex) {
            String errMsg1 = "LDAP search userInfo failed.";
            String errMsg2 = StackTraceUtil.logStackTrace(ex);
			String logMsg = getLogMsg2(errMsg1, ldapDn, reqUserName, searchBase, errMsg2);
			TPILogger.tl.debug(logMsg);
        }
        
        return ldapAdAuthData;
    }
    
	private static SearchControls getSearchControls() {
		SearchControls cons = new SearchControls();
		cons.setSearchScope(SearchControls.SUBTREE_SCOPE);
		String[] attrIDs = { "sAMAccountName", "name", "displayName", "mail" };
		cons.setReturningAttributes(attrIDs);
		return cons;
	}
	
    private static void disableSSLVerification() {
        try {
			TrustManager[] trustAllCerts = new TrustManager[] { new TrustAllManager() };
            
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            
            SSLContext.setDefault(sslContext);
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new TrustAllHostnameVerifier());
            
            // LDAP 特定的 SSL 設定
            System.setProperty("com.sun.jndi.ldap.object.disableEndpointIdentification", "true");
            System.setProperty("javax.net.ssl.trustStore", "");
            System.setProperty("javax.net.ssl.trustStorePassword", "");
            System.setProperty("javax.net.ssl.trustStoreType", "JKS");
            
        } catch (Exception e) {
        	TPILogger.tl.debug("SSL setup failed: " + e.getMessage());
        }
    }
}
