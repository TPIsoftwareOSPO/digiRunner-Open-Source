package tpi.dgrv4.dpaa.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0109Req;
import tpi.dgrv4.dpaa.vo.DPB0109Resp;
import tpi.dgrv4.entity.entity.jpql.TsmpEvents;
import tpi.dgrv4.entity.repository.TsmpEventsDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

//@Service 停用[事件檢視器]功能 2022/09/26
public class DPB0109Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpEventsDao tsmpEventsDao;

	@Autowired
	public DPB0109Service(TsmpEventsDao tsmpEventsDao) {
		super();
		this.tsmpEventsDao = tsmpEventsDao;
	}

	public DPB0109Resp archiveEventByPk(TsmpAuthorization auth, DPB0109Req req) {
		DPB0109Resp resp = null;

		try {

			Long eventId = req.getEventId();
			if (eventId == null) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}

			String archiveFlag = req.getArchiveFlag();
			if (StringUtils.isEmpty(archiveFlag)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}

			Optional<TsmpEvents> opt_tsmpEvent = getTsmpEventsDao().findById(eventId);

			// 查無資料
			if (!opt_tsmpEvent.isPresent()) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			// 更新ArchiveFlag欄位
			TsmpEvents tsmpEvent = opt_tsmpEvent.get();
			tsmpEvent.setArchiveFlag(archiveFlag);
			getTsmpEventsDao().saveAndFlush(tsmpEvent);

			// 產生response
			resp = new DPB0109Resp();

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
