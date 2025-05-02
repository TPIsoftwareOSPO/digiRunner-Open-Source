package tpi.dgrv4.gateway.component;

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

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.utils.ServiceUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * @author Mini
 * 
 * LDAP 的共用程式
 */

@Component
public class LdapHelper {
	public static class LdapAdAuthData {
		public String errMsg;
		public String name;
		public String mail;
	}
 
	/**
	 * AD LDAP 登入認證
	 * 檢查 LDAP User 帳號 & 密碼是否正確
	 * 
	 * @param ldapUrl LDAP登入的URL
	 * @param ldapDn  LDAP登入的使用者DN
	 * @param ldapBaseDn
	 * @param ldapTimeout LDAP登入的連線timeout,單位毫秒
	 * @param reqUserName
	 * @param userMima
	 * @param isGetUserInfo 是否要找出 user 在 LDAP / AD 中的資料
	 * @param orderNo 當 AC IdP type 為 "MLDP" 時,才有值;其他,都為 null
	 * @return
	 * @throws Exception
	 */
	public LdapAdAuthData checkLdapAuth(String ldapUrl, String ldapDn, String ldapBaseDn, int ldapTimeout,
			String reqUserName, String userMima, boolean isGetUserInfo, Integer orderNo) throws Exception {
		LdapAdAuthData ldapAdAuthData = new LdapAdAuthData();
		String errMsg = null;
		DirContext ctx = null;
		
		try  {
			Map<String, String> ldap_paramMap = new HashMap<>();
			ldap_paramMap.put("0", reqUserName);
			ldapDn = ServiceUtil.buildContent(ldapDn, ldap_paramMap);
			
			// 連線成功代表驗證成功
			Hashtable<String, String> ldap_HashEnv = new Hashtable<String, String>();
			ldap_HashEnv.put(Context.SECURITY_AUTHENTICATION, "simple"); // LDAP訪問安全級別(none,simple,strong)
			ldap_HashEnv.put(Context.SECURITY_PRINCIPAL, ldapDn); // AD的使用者名稱
			ldap_HashEnv.put(Context.SECURITY_CREDENTIALS, userMima); // AD的密碼
			ldap_HashEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			ldap_HashEnv.put("com.sun.jndi.ldap.connect.timeout", ldapTimeout + "");// 連線超時設定
			ldap_HashEnv.put(Context.PROVIDER_URL, ldapUrl);// LDAP URL
			ctx = new InitialLdapContext(ldap_HashEnv, null);
			
			// 驗證成功
			String logMsg = "LDAP verification suceesfully.";
			logMsg = getLogMsg(logMsg, reqUserName, ldapUrl, ldapDn, orderNo);
			TPILogger.tl.debug(logMsg);

			if (isGetUserInfo) {
				// 取得 user info
				ldapAdAuthData = getUserInfo(ldapAdAuthData, reqUserName, ldapDn, ctx, ldapBaseDn);
			}

		} catch (AuthenticationException e) {
			// LDAP驗證失敗
			errMsg = "LDAP verification failed. User account or password is incorrect.";
			String logMsg = getLogMsg(errMsg, reqUserName, ldapUrl, ldapDn, orderNo);
			TPILogger.tl.debug(logMsg);
			
		} catch (CommunicationException e) {
			// LDAP連線失敗
			errMsg = "LDAP connection failed.";
			String logMsg = getLogMsg(errMsg, reqUserName, ldapUrl, ldapDn, orderNo);
			TPILogger.tl.debug(logMsg);
			
		} catch (Exception e) {
			// 執行錯誤
			errMsg = "Execution error.";
			String logMsg = getLogMsg(errMsg, reqUserName, ldapUrl, ldapDn, orderNo);
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
	 * 取得 user 在 LDAP / AD 中記錄的資料
	 */
	private LdapAdAuthData getUserInfo(LdapAdAuthData ldapAdAuthData, String reqUserName, String ldapDn,
			DirContext ctx, String searchBase) {
        try {
			if (!StringUtils.hasLength(searchBase)) {
				// 設定檔缺少參數 '%s'
				String errMsg = "LDAP search userInfo failed."
						+ String.format(AcIdPHelper.MSG_THE_PROFILE_IS_MISSING_PARAMETERS, "ldap_base_dn");
				String logMsg = getLogMsg2(errMsg, ldapDn, reqUserName, searchBase);
				TPILogger.tl.debug(logMsg);

				return ldapAdAuthData;
			}
 
			//checkmarx, LDAP Injection
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

				// 優先使用 "name", 若不存在再找 "displayName", 若都不存在就不提供屬性
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
}
