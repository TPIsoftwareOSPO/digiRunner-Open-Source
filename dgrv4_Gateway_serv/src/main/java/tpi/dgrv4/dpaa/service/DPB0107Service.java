package tpi.dgrv4.dpaa.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.vo.DPB0107Req;
import tpi.dgrv4.dpaa.vo.DPB0107Resp;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.entity.jpql.TsmpEvents;
import tpi.dgrv4.entity.repository.TsmpDpItemsDao;
import tpi.dgrv4.entity.repository.TsmpEventsDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

//@Service 停用[事件檢視器]功能 2022/09/26
public class DPB0107Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpEventsDao tsmpEventsDao;
	private TsmpDpItemsDao tsmpDpItemsDao;
	
	@Autowired
	public DPB0107Service(TsmpEventsDao tsmpEventsDao, TsmpDpItemsDao tsmpDpItemsDao) {
		super();
		this.tsmpEventsDao = tsmpEventsDao;
		this.tsmpDpItemsDao = tsmpDpItemsDao;
	}

	public DPB0107Resp queryEventByPk(TsmpAuthorization auth, DPB0107Req req, ReqHeader reqHeader) {
		DPB0107Resp resp = null;

		try {
			
			Long eventId = req.getEventId();
			if (eventId==null) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			
			Optional<TsmpEvents> tsmpEvent  = getTsmpEventsDao().findById(eventId);

			// 查無資料
			if (!tsmpEvent.isPresent()) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			// 產生response
			resp = getEvent(tsmpEvent.get(), reqHeader.getLocale());

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}
	
	private DPB0107Resp getEvent(TsmpEvents tsmpEvents, String locale) {
		
		DPB0107Resp resp = new DPB0107Resp();
		
		resp.setEventId(tsmpEvents.getEventId());
		resp.setEventTypeId(tsmpEvents.getEventTypeId());
		resp.setEventTypeName(getSubitemName("EVENT_TYPE", tsmpEvents.getEventTypeId(), locale));
		resp.setEventNameId(tsmpEvents.getEventNameId());
		resp.setEventName(getSubitemName("EVENT_NAME", tsmpEvents.getEventNameId(), locale));
		resp.setModuleName(tsmpEvents.getModuleName());
		resp.setModuleVersion(tsmpEvents.getModuleVersion());
		resp.setTraceId(tsmpEvents.getTraceId());
		resp.setInfoMsg(tsmpEvents.getInfoMsg());
		resp.setArchiveFlag(tsmpEvents.getArchiveFlag());
		resp.setKeepFlag(tsmpEvents.getKeepFlag());
		resp.setNodeAlias(tsmpEvents.getNodeAlias());
		resp.setNodeId(tsmpEvents.getNodeId());
		resp.setThreadName(tsmpEvents.getThreadName());
		resp.setCreateDateTime(
				DateTimeUtil.dateTimeToString(tsmpEvents.getCreateDateTime(), DateTimeFormatEnum.西元年月日時分_2)
						.orElse(new String()));	
		resp.setCreateUser(tsmpEvents.getCreateUser());
		
		
		return resp;
	}
	
	private String getSubitemName(String itemNo, String subitemNo, String locale) {
		TsmpDpItemsId id = new TsmpDpItemsId();
		id.setItemNo(itemNo);
		id.setSubitemNo(subitemNo);
		id.setLocale(locale);
		Optional<TsmpDpItems> tsmpDpItems = getTsmpDpItemsDao().findById(id);
		if (tsmpDpItems.isPresent()) {
			return tsmpDpItems.get().getSubitemName();
		}
		return null;
	}

	protected TsmpEventsDao getTsmpEventsDao() {
		return tsmpEventsDao;
	}

	protected TsmpDpItemsDao getTsmpDpItemsDao() {
		return tsmpDpItemsDao;
	}

	
	
}
