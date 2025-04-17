package tpi.dgrv4.dpaa.service;						
						
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0122Req;
import tpi.dgrv4.dpaa.vo.DPB0122Resp;
import tpi.dgrv4.entity.entity.jpql.SsoAuthResult;
import tpi.dgrv4.entity.repository.SsoAuthResultDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service						
public class DPB0122Service {						
				 				
	private TPILogger logger = TPILogger.tl;
	
	private TsmpSettingService tsmpSettingService;
	private SsoAuthResultDao ssoAuthResultDao;
						
	@Autowired
	public DPB0122Service(TsmpSettingService tsmpSettingService, SsoAuthResultDao ssoAuthResultDao) {
		super();
		this.tsmpSettingService = tsmpSettingService;
		this.ssoAuthResultDao = ssoAuthResultDao;
	}

	public DPB0122Resp loginSuccNoticeAC(TsmpAuthorization auth, DPB0122Req req) {					
		DPB0122Resp resp = new DPB0122Resp();	
		try {				
			String userName = req.getUsername();
			String codeChallenge = req.getCodeChallenge();
			// chk param			
			if(StringUtils.isEmpty(userName)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}			
			if(StringUtils.isEmpty(codeChallenge)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}			
			
			boolean flag1_ssoPkce = false;//判斷 PKCE等級AuthCode驗證 是否啟用
			try {
				flag1_ssoPkce = getTsmpSettingService().getVal_SSO_PKCE();
			} catch (TsmpDpAaException e) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			if(flag1_ssoPkce) {//若 PKCE等級AuthCode驗證 有啟用, 為 "true", 則新增資料
				//建立 SSO_AUTH_RESULT 資料
				createSsoAuthResult(userName, codeChallenge);
			}
						
		} catch (TsmpDpAaException e) {				
			throw e;			
		} catch (Exception e) {				
			this.logger.error(StackTraceUtil.logStackTrace(e));		
			throw TsmpDpAaRtnCode._1297.throwing();			
		}				
		return resp;				
	}					
	
	private SsoAuthResult createSsoAuthResult(String userName, String codeChallenge) {
		SsoAuthResult s = new SsoAuthResult();
		s.setUserName(userName);
		s.setCodeChallenge(codeChallenge);
		s = this.getSsoAuthResultDao().saveAndFlush(s);
		return s;
	}
	
	protected TsmpSettingService getTsmpSettingService() {
		return this.tsmpSettingService;
	}
	
	protected SsoAuthResultDao getSsoAuthResultDao() {
		return this.ssoAuthResultDao;
	}
}						
						