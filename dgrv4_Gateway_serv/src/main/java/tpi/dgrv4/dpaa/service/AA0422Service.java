package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA0422Dc;
import tpi.dgrv4.dpaa.vo.AA0422Req;
import tpi.dgrv4.dpaa.vo.AA0422Resp;
import tpi.dgrv4.entity.entity.jpql.TsmpDc;
import tpi.dgrv4.entity.repository.TsmpDcDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0422Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpDcDao tsmpDcDao;

	@Autowired
	public AA0422Service(TsmpDcDao tsmpDcDao) {
		super();
		this.tsmpDcDao = tsmpDcDao;
	}

	public AA0422Resp queryDCList_2(TsmpAuthorization authorization, AA0422Req req) {
		AA0422Resp resp = new AA0422Resp();
		List<AA0422Dc> dcList = new ArrayList<AA0422Dc>();
		try {

			List<TsmpDc> dcData = getTsmpDcDao().queryDCList_2(req.getModuleName());

			if (dcData != null && dcData.isEmpty() == false) {
				dcData.forEach((d)->{
					AA0422Dc dc = new AA0422Dc();
					
					dc.setDcID(d.getDcId());
					dc.setDcCode(d.getDcCode());
					dc.setActive(d.getActive());
					
					dcList.add(dc);
				});
				resp.setDcList(dcList);
			}

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			// 執行錯誤
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	protected TsmpDcDao getTsmpDcDao() {
		return tsmpDcDao;
	}

}
