package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA1105Req;
import tpi.dgrv4.dpaa.vo.AA1105Resp;
import tpi.dgrv4.entity.entity.jpql.TsmpSecurityLevel;
import tpi.dgrv4.entity.repository.TsmpSecurityLevelDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA1105Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpSecurityLevelDao tsmpSecurityLevelDao; 

	@Autowired
	public AA1105Service(TsmpSecurityLevelDao tsmpSecurityLevelDao) {
		super();
		this.tsmpSecurityLevelDao = tsmpSecurityLevelDao;
	}

	public AA1105Resp querySecurityLevelDetail(TsmpAuthorization authorization, AA1105Req req) {
		AA1105Resp resp = new AA1105Resp();

		try {
			checkParam(req);
			
			String securityLevelId = req.getSecurityLevelId();
			String securityLevelName = req.getSecurityLevelName();
			
			TsmpSecurityLevel vo = getTsmpSecurityLevelDao().findFirstBySecurityLevelIdAndSecurityLevelName(securityLevelId, securityLevelName);
			resp.setSecurityLevelId(vo.getSecurityLevelId());
			resp.setSecurityLevelName(vo.getSecurityLevelName());
			resp.setSecurityLevelDesc(vo.getSecurityLevelDesc());
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			//1297:執行錯誤
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}
	
	private void checkParam(AA1105Req req) {
		String securityLevelId = req.getSecurityLevelId();
		String securityLevelName = req.getSecurityLevelName();
		
		if(StringUtils.isEmpty(securityLevelId)) {
			//缺少必填參數
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		
		if(StringUtils.isEmpty(securityLevelName)) {
			//缺少必填參數
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		
		TsmpSecurityLevel vo = getTsmpSecurityLevelDao().findFirstBySecurityLevelIdAndSecurityLevelName(securityLevelId, securityLevelName);
		if(vo == null) {
			//查無資料
			throw TsmpDpAaRtnCode._1298.throwing();
		}
	}

	protected TsmpSecurityLevelDao getTsmpSecurityLevelDao() {
		return tsmpSecurityLevelDao;
	}

}
