package tpi.dgrv4.dpaa.service;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0122Req;
import tpi.dgrv4.dpaa.vo.LdapReq;
import tpi.dgrv4.dpaa.vo.LdapResp;
import tpi.dgrv4.entity.entity.LdapAuthResult;
import tpi.dgrv4.entity.repository.LdapAuthResultDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class LdapService{

	private TPILogger logger = TPILogger.tl;

	private TsmpSettingService tsmpSettingService;
	private DPB0122Service dpb0122Service;
	private LdapAuthResultDao ldapAuthResultDao;

	@Autowired
	public LdapService(TsmpSettingService tsmpSettingService, DPB0122Service dpb0122Service,
			LdapAuthResultDao ldapAuthResultDao) {
		super();
		this.tsmpSettingService = tsmpSettingService;
		this.dpb0122Service = dpb0122Service;
		this.ldapAuthResultDao = ldapAuthResultDao;
	}

	public LdapResp checkAccountByLdap(TsmpAuthorization auth, LdapReq req, String localBaseUrl) {

		LdapResp resp = new LdapResp();
		DirContext ctx=null;
		String ldapDn = null;
		try {
			
			//LDAP檢查帳號功能是否啟用
			boolean isEnable = getTsmpSettingService().getVal_LDAP_CHECK_ACCT_ENABLE();
			if(!isEnable) {
				//LDAP未啟用
				throw TsmpDpAaRtnCode._1511.throwing();
			}
			
			checkParam(req);

			//取得LDAP相關資訊
			String loginId = req.getUserName();
			String ldapUrl = getTsmpSettingService().getVal_LDAP_URL();
			ldapDn = getTsmpSettingService().getVal_LDAP_DN();
			String timeout = getTsmpSettingService().getVal_LDAP_TIMEOUT();
			Map<String,String> ldap_paramMap = new HashMap<>();
			ldap_paramMap.put("0", loginId);
			ldapDn = ServiceUtil.buildContent(ldapDn, ldap_paramMap);
			
			//連線成功代表驗証成功
			Hashtable<String,String> ldap_HashEnv = new Hashtable<String,String>();
			ldap_HashEnv.put(Context.SECURITY_AUTHENTICATION, "simple"); // LDAP訪問安全級別(none,simple,strong)
			ldap_HashEnv.put(Context.SECURITY_PRINCIPAL, ldapDn); //AD的使用者名稱
			ldap_HashEnv.put(Context.SECURITY_CREDENTIALS, req.getPwd()); //AD的密碼
			ldap_HashEnv.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory"); 
			ldap_HashEnv.put("com.sun.jndi.ldap.connect.timeout", timeout);//連線超時設定
			ldap_HashEnv.put(Context.PROVIDER_URL, ldapUrl);// LDAP URL
			ctx = new InitialDirContext(ldap_HashEnv);
			
			//是否呼叫DPB0122(flag1_PKCE等級AuthCode驗證是否啟用 (true/false))
		    isEnable = getTsmpSettingService().getVal_SSO_PKCE();
			if(isEnable) {
				//在checkParam已檢查codeChallenge不能為空
				callDPB0122(auth, req);
			}
			
			//記錄登入成功資料
			isEnable = getTsmpSettingService().getVal_SSO_DOUBLE_CHECK();
			if(isEnable) {
				//在checkParam已檢查codeChallenge不能為空
				LdapAuthResult vo = new LdapAuthResult();
				vo.setUserName(req.getUserName());
				vo.setCodeChallenge(req.getCodeChallenge());
				getLdapAuthResultDao().save(vo);
			}
			
		    //回傳資料
			resp.setCodeChallenge(req.getCodeChallenge());
		    resp.setUserName(req.getUserName());
			return resp;
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch(AuthenticationException e) {
			this.logger.debug("ldapDn=" + ldapDn);
			this.logger.error(StackTraceUtil.logStackTrace(e));
			//驗證失敗
			throw TsmpDpAaRtnCode._1510.throwing();
		}catch (CommunicationException e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			//連線失敗
			throw TsmpDpAaRtnCode._1512.throwing();
		}catch (Exception e) {
			this.logger.debug("ldapDn=" + ldapDn);
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
				this.logger.error("Close DirContext error: " + StackTraceUtil.logStackTrace(e));
			}
		}
	}
	
	private void checkParam(LdapReq req) {
		if(!StringUtils.hasLength(req.getUserName())) {
			//[{{0}}] 為必填欄位
			throw TsmpDpAaRtnCode._1350.throwing("{{userName}}");
		}
		
		if(!StringUtils.hasLength(req.getPwd())) {
			//[{{0}}] 為必填欄位
			throw TsmpDpAaRtnCode._1350.throwing("{{pwd}}");
		}
		
		boolean isEnable = getTsmpSettingService().getVal_SSO_PKCE();
		if(isEnable) {
			if(!StringUtils.hasLength(req.getCodeChallenge())) {
				//[{{0}}] 為必填欄位
				throw TsmpDpAaRtnCode._1350.throwing("{{codeChallenge}}");
			}
		}
		
		isEnable = getTsmpSettingService().getVal_SSO_DOUBLE_CHECK();
		if(isEnable) {
			if(!StringUtils.hasLength(req.getCodeChallenge())) {
				//[{{0}}] 為必填欄位
				throw TsmpDpAaRtnCode._1350.throwing("{{codeChallenge}}");
			}
		}
	}
	
	private void callDPB0122(TsmpAuthorization auth, LdapReq req){
		try {
			DPB0122Req dpb0122Req = new DPB0122Req();
			dpb0122Req.setCodeChallenge(req.getCodeChallenge());
			dpb0122Req.setUsername(req.getUserName());
			getDpb0122Service().loginSuccNoticeAC(auth, dpb0122Req);
		}catch(Exception e) {
			logger.error("call DPB0122: " + StackTraceUtil.logStackTrace(e));
		}
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected DPB0122Service getDpb0122Service() {
		return dpb0122Service;
	}

	public LdapAuthResultDao getLdapAuthResultDao() {
		return ldapAuthResultDao;
	}
}