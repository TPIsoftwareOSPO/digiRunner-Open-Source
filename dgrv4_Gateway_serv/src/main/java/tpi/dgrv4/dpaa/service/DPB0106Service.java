package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0106Req;
import tpi.dgrv4.dpaa.vo.DPB0106Resp;
import tpi.dgrv4.dpaa.vo.DPB0106RespItem;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.entity.jpql.TsmpEvents;
import tpi.dgrv4.entity.repository.TsmpDpItemsDao;
import tpi.dgrv4.entity.repository.TsmpEventsDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

//@Service 停用[事件檢視器]功能 2022/09/26
public class DPB0106Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpEventsDao tsmpEventsDao;
	private TsmpDpItemsDao tsmpDpItemsDao;
	private ServiceConfig serviceConfig;

	private Integer pageSize;

	@Autowired
	public DPB0106Service(TsmpEventsDao tsmpEventsDao, TsmpDpItemsDao tsmpDpItemsDao, ServiceConfig serviceConfig) {
		super();
		this.tsmpEventsDao = tsmpEventsDao;
		this.tsmpDpItemsDao = tsmpDpItemsDao;
		this.serviceConfig = serviceConfig;
	}

	public DPB0106Resp queryEventByDateLike(TsmpAuthorization auth, DPB0106Req req, ReqHeader reqHeader) {
		DPB0106Resp resp = null;

		try {
			Long eventId = req.getEventId();
			String[] keyword = ServiceUtil.getKeywords(req.getKeyword(), " ");
			
			String startDate = req.getStartDate();
			if (StringUtils.isEmpty(startDate)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			} 
			
			String endDate = req.getEndDate();
			if (StringUtils.isEmpty(endDate)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			} 

			resp = queryEventList(eventId, keyword, startDate, endDate, reqHeader.getLocale());

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	private DPB0106Resp queryEventList(Long eventId, String[] keyword, String startDate, String endDate, String locale) {
		
		//將每一頁的最後一筆eventId用來查詢TsmpEvents出來
		TsmpEvents lastTsmpEvents = null;
		if (eventId != null) {
			Optional<TsmpEvents> tsmpEvents = getTsmpEventsDao().findById(eventId);
			if (tsmpEvents.isPresent()) {
				lastTsmpEvents = tsmpEvents.get();
			}
		}

		List<TsmpEvents> data = getTsmpEventsDao().findByEventIdAndStartDateAndEndDateAndKeyword(lastTsmpEvents,
				startDate, endDate, keyword, locale, getPageSize());
		
		// 查無資料
		if (data == null || data.isEmpty()) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}

		DPB0106Resp resp = new DPB0106Resp();

		// 產生response
		List<DPB0106RespItem> dataList = getEventsList(data, locale);
		resp.setDataList(dataList);
		return resp;
	}

	public List<DPB0106RespItem> getEventsList(List<TsmpEvents> data, String locale) {
		List<DPB0106RespItem> dataList = new ArrayList<DPB0106RespItem>();
		for (TsmpEvents tsmpEvents : data) {
			DPB0106RespItem item = new DPB0106RespItem();
			item.setEventId(tsmpEvents.getEventId());
			item.setEventTypeId(tsmpEvents.getEventTypeId());
			item.setEventTypeName(getSubitemName("EVENT_TYPE", tsmpEvents.getEventTypeId(), locale));
			item.setEventNameId(tsmpEvents.getEventNameId());
			item.setEventName(getSubitemName("EVENT_NAME", tsmpEvents.getEventNameId(), locale));
			item.setModuleName(tsmpEvents.getModuleName());
			item.setModuleVersion(tsmpEvents.getModuleVersion());
			item.setOriInfoMsg(tsmpEvents.getInfoMsg());

			// 判斷要不要截斷過長訊息
			if (StringUtils.isEmpty(tsmpEvents.getInfoMsg()) == false && tsmpEvents.getInfoMsg().length() > 100) {
				item.setInfoMsg(tsmpEvents.getInfoMsg().substring(0, 100) + "...");
				item.setIsMsgTruncated(true);
			} else {
				item.setInfoMsg(tsmpEvents.getInfoMsg());
			}

			item.setCreateDateTime(
					DateTimeUtil.dateTimeToString(tsmpEvents.getCreateDateTime(), DateTimeFormatEnum.西元年月日時分_2)
							.orElse(new String()));
			item.setArchiveFlag(tsmpEvents.getArchiveFlag());
			item.setKeepFlag(tsmpEvents.getKeepFlag());

			dataList.add(item);
		}
		return dataList;
	}

	private String getSubitemName(String itemNo, String subitemNo, String locale) {
		TsmpDpItemsId dpb0106_id = new TsmpDpItemsId();
		dpb0106_id.setItemNo(itemNo);
		dpb0106_id.setSubitemNo(subitemNo);
		dpb0106_id.setLocale(locale);
		Optional<TsmpDpItems> dpb0106_tsmpDpItems = getTsmpDpItemsDao().findById(dpb0106_id);
		if (dpb0106_tsmpDpItems.isPresent()) {
			return dpb0106_tsmpDpItems.get().getSubitemName();
		}
		return null;
	}

	protected TsmpEventsDao getTsmpEventsDao() {
		return tsmpEventsDao;
	}

	protected TsmpDpItemsDao getTsmpDpItemsDao() {
		return tsmpDpItemsDao;
	}

	protected ServiceConfig getServiceConfig() {
		return serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb0106");
		return pageSize;
	}

}
