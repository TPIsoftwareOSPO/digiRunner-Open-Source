package tpi.dgrv4.dpaa.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0108Req;
import tpi.dgrv4.dpaa.vo.DPB0108Resp;
import tpi.dgrv4.entity.entity.jpql.TsmpEvents;
import tpi.dgrv4.entity.repository.TsmpEventsDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
	
//@Service 停用[事件檢視器]功能 2022/09/26
public class DPB0108Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpEventsDao tsmpEventsDao;

	@Autowired
	public DPB0108Service(TsmpEventsDao tsmpEventsDao) {
		super();
		this.tsmpEventsDao = tsmpEventsDao;
	}

	public DPB0108Resp keepEventByPk(TsmpAuthorization auth, DPB0108Req req) {
		DPB0108Resp resp = null;

		try {

			Long eventId = req.getEventId();
			if (eventId == null) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			
			String keepFlag = req.getKeepFlag();
			if (StringUtils.isEmpty(keepFlag)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}

			Optional<TsmpEvents> opt_tsmpEvent = getTsmpEventsDao().findById(eventId);

			// 查無資料
			if (!opt_tsmpEvent.isPresent()) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			// 更新KeepFlag欄位
			TsmpEvents tsmpEvent= opt_tsmpEvent.get();
			tsmpEvent.setKeepFlag(keepFlag);
			getTsmpEventsDao().saveAndFlush(tsmpEvent);
			
			// 產生response
			resp = new DPB0108Resp();
			
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;

	}

	protected TsmpEventsDao getTsmpEventsDao() {
		return tsmpEventsDao;
	}

}
