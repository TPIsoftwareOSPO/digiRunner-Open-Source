package tpi.dgrv4.dpaa.service;


import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.UdpLdapReq;
import tpi.dgrv4.dpaa.vo.UdpLdapResp;
import tpi.dgrv4.entity.entity.LdapAuthResult;
import tpi.dgrv4.entity.repository.LdapAuthResultDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class UdpLdapService {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpSettingService tsmpSettingService;
	private LdapAuthResultDao ldapAuthResultDao;
	
	@Autowired
	public UdpLdapService(TsmpSettingService tsmpSettingService, LdapAuthResultDao ldapAuthResultDao) {
		super();
		this.tsmpSettingService = tsmpSettingService;
		this.ldapAuthResultDao = ldapAuthResultDao;
	}

	public UdpLdapResp udpCheckAccountByLdap(String userIp, HttpServletRequest httpReq, 
			TsmpAuthorization auth, UdpLdapReq req, String localBaseUrl) {

		UdpLdapResp resp = new UdpLdapResp();
		DirContext ctx=null;
		String ldapDn = null;
		try {
			
			/*
			 * 2022/7/3 修改, 凡是由統一入口網進入(Udp SSO),預設走 LDAP,不用再檢查 LDAP_CHECK_ACCT_ENABLE 的值(僅 SSO 使用)
			 * 
			//LDAP檢查帳號功能是否啟用
			boolean isEnable = getTsmpSettingService().getVal_LDAP_CHECK_ACCT_ENABLE();
			if(!isEnable) {
				//LDAP未啟用
				throw TsmpDpAaRtnCode._1511.throwing();
			}
			*/
			
			checkParam(req);

			//取得LDAP相關資訊
			String loginId = req.getUserName();
			String ldapUrl = getTsmpSettingService().getVal_LDAP_URL();
			ldapDn = getTsmpSettingService().getVal_LDAP_DN();
			String timeout = getTsmpSettingService().getVal_LDAP_TIMEOUT();
			Map<String,String> paramMap = new HashMap<>();
			paramMap.put("0", loginId);
			ldapDn = ServiceUtil.buildContent(ldapDn, paramMap);
			
			//連線成功代表驗証成功
			Hashtable<String,String> HashEnv = new Hashtable<String,String>();
			HashEnv.put(Context.SECURITY_AUTHENTICATION, "simple"); // LDAP訪問安全級別(none,simple,strong)
			HashEnv.put(Context.SECURITY_PRINCIPAL, ldapDn); //AD的使用者名稱
			HashEnv.put(Context.SECURITY_CREDENTIALS, req.getPwd()); //AD的密碼
			HashEnv.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory"); 
			HashEnv.put("com.sun.jndi.ldap.connect.timeout", timeout);//連線超時設定
			HashEnv.put(Context.PROVIDER_URL, ldapUrl);// LDAP URL
			ctx = new InitialDirContext(HashEnv);
			
			//記錄登入成功資料
			boolean isEnable = getTsmpSettingService().getVal_SSO_DOUBLE_CHECK();
			if(isEnable) {
				//在checkParam已檢查codeChallenge不能為空
				LdapAuthResult vo = new LdapAuthResult();
				vo.setUserName(req.getUserName());
				vo.setCodeChallenge(req.getCodeChallenge());
				vo.setUserIp(userIp);
				vo.setCreateUser("UDPSSO");
				getLdapAuthResultDao().save(vo);
			}
			
		    //回傳資料
			resp.setCodeChallenge(req.getCodeChallenge());
		    resp.setUserName(req.getUserName());
			return resp;
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch(AuthenticationException e) {
			this.logger.debug(String.format("ldapDn = %s", ldapDn));
			this.logger.error(StackTraceUtil.logStackTrace(e));
			//驗證失敗
			throw TsmpDpAaRtnCode._1510.throwing();
		}catch (CommunicationException e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			//連線失敗
			throw TsmpDpAaRtnCode._1512.throwing();
		}catch (Exception e) {
			this.logger.debug(String.format("ldapDn = %s", ldapDn));
			this.logger.error(StackTraceUtil.logStackTrace(e));
			//執行錯誤
			throw TsmpDpAaRtnCode._1297.throwing();
		} finally {
			try {
				if(ctx != null) {
					ctx.close();
					ctx = null;
				}
			}catch(Exception e) {
				this.logger.error(String.format("Close DirContext error: %s", StackTraceUtil.logStackTrace(e)));
			}
		}
	}
	
	private void checkParam(UdpLdapReq req) {
		if(!StringUtils.hasLength(req.getUserName())) {
			//[{{0}}] 為必填欄位
			throw TsmpDpAaRtnCode._1350.throwing("{{userName}}");
		}
		
		if(!StringUtils.hasLength(req.getPwd())) {
			//[{{0}}] 為必填欄位
			throw TsmpDpAaRtnCode._1350.throwing("{{pwd}}");
		}
 
		boolean isEnable = getTsmpSettingService().getVal_SSO_DOUBLE_CHECK();
		if(isEnable) {
			if(!StringUtils.hasLength(req.getCodeChallenge())) {
				//[{{0}}] 為必填欄位
				throw TsmpDpAaRtnCode._1350.throwing("{{codeChallenge}}");
			}
		}
	}
 
	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}
 
	public LdapAuthResultDao getLdapAuthResultDao() {
		return ldapAuthResultDao;
	}
}