package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA1109Req;
import tpi.dgrv4.dpaa.vo.AA1109Resp;
import tpi.dgrv4.entity.entity.TsmpGroupAuthorities;
import tpi.dgrv4.entity.repository.TsmpGroupAuthoritiesDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA1109Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpGroupAuthoritiesDao tsmpGroupAuthoritiesDao;

	@Autowired
	public AA1109Service(TsmpGroupAuthoritiesDao tsmpGroupAuthoritiesDao) {
		super();
		this.tsmpGroupAuthoritiesDao = tsmpGroupAuthoritiesDao;
	}


	public AA1109Resp queryTGroupAuthorityDetail(TsmpAuthorization authorization, AA1109Req req) {
		AA1109Resp resp = new AA1109Resp();

		try {
			String groupAuthoritieId = req.getGroupAuthoritieId();
						
			TsmpGroupAuthorities groupAuthVo = getTsmpGroupAuthoritiesDao().findById(groupAuthoritieId).orElse(null);
			if(groupAuthVo == null) {
				//1298:查無資料
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			resp.setGroupAuthoritieDesc(groupAuthVo.getGroupAuthoritieDesc());
			resp.setGroupAuthoritieId(groupAuthVo.getGroupAuthoritieId());
			resp.setGroupAuthoritieLevel(groupAuthVo.getGroupAuthoritieLevel());
			resp.setGroupAuthoritieName(groupAuthVo.getGroupAuthoritieName());
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			//1297:執行錯誤
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}


	protected TsmpGroupAuthoritiesDao getTsmpGroupAuthoritiesDao() {
		return tsmpGroupAuthoritiesDao;
	}

}
