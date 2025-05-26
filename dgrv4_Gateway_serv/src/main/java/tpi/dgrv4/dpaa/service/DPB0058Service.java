package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.getKeywords;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.dpaa.vo.DPB0058Req;
import tpi.dgrv4.dpaa.vo.DPB0058Resp;
import tpi.dgrv4.dpaa.vo.DPB0058RespItem;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.gateway.TCP.Packet.NotifyClientRefreshMemListPacket;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0058Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpDpApptJobDao tsmpDpApptJobDao;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private ApptJobDispatcher apptJobDispatcher;
	private ServiceConfig serviceConfig;

	private Integer pageSize;

	@Autowired
	public DPB0058Service(TsmpDpApptJobDao tsmpDpApptJobDao, TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy,
			ApptJobDispatcher apptJobDispatcher, ServiceConfig serviceConfig) {
		super();
		this.tsmpDpApptJobDao = tsmpDpApptJobDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.apptJobDispatcher = apptJobDispatcher;
		this.serviceConfig = serviceConfig;
	}

	public DPB0058Resp queryJobLikeList(TsmpAuthorization auth, DPB0058Req req, ReqHeader reqHeader) {
		// 檢查必要參數
		final Date sdt = checkDt(req.getStartDate(), false);
		final Date edt = checkDt(req.getEndDate(), true);
		final String status = req.getStatus();
		if (edt.compareTo(sdt) <= 0 ||
			(!StringUtils.isEmpty(status) && !checkJobStatus(status, reqHeader.getLocale()))
		) {
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		this.logger.trace(String.format("duration: %s ~ %s", printDt(sdt), printDt(edt)));
		
		// refresh memList
		refreshMemList();
		
		// query
		String[] words = getKeywords(req.getKeyword(), " ");
		TsmpDpApptJob lastRecord = getLastRecordFromPrevPage(req.getApptJobId());
		List<TsmpDpApptJob> jobs = getTsmpDpApptJobDao().query_dpb0058Service( //
				sdt, edt, status, words, lastRecord, reqHeader.getLocale(), getPageSize());
		if (jobs == null || jobs.isEmpty()) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		
		DPB0058Resp resp = new DPB0058Resp();
		List<DPB0058RespItem> dpb0058RespItems = getDpb0058RespItems(jobs, reqHeader.getLocale());
		resp.setDataList(dpb0058RespItems);
		return resp;
	}

	// refresh memList
	protected void refreshMemList() {
        try {

            synchronized (TPILogger.lc) {//
                TPILogger.lc.send(new NotifyClientRefreshMemListPacket());
            }
        } catch (Exception e) {
            this.logger.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode.SYSTEM_ERROR.throwing();
        }

    }

	private boolean checkJobStatus(String input, String locale) {
		TsmpDpItems items = getItemsById("JOB_STATUS", input, false, locale);
		return items != null;
	}

	private TsmpDpApptJob getLastRecordFromPrevPage(Long apptJobId) {
		if (apptJobId != null) {
			Optional<TsmpDpApptJob> opt = getTsmpDpApptJobDao().findById(apptJobId);
			if (opt.isPresent()) {
				return opt.get();
			}
		}
		return null;
	}

	private List<DPB0058RespItem> getDpb0058RespItems(List<TsmpDpApptJob> jobs, String locale) {
		List<DPB0058RespItem> dpb0058RespItems = new ArrayList<>();
		DPB0058RespItem dpb0058RespItem = null;
		
		String createUser = null;
		String updateUser = null;
		String refItemNo = null;
		String refSubitemNo = null;
		String identifData = null;
		String status = null;
		String startDateTime = null;
		String jobStep = null;
		String execResult = null;
		String createDateTime = null;
		String updateDateTime = null;
		String canExec = null;
		for(TsmpDpApptJob job : jobs) {
			refItemNo = getRefItemNo(job.getRefItemNo(), locale);
			refSubitemNo = getRefSubitemNo(job.getRefItemNo(), job.getRefSubitemNo(), locale);
			identifData = nvl(job.getIdentifData());
			status = getStatusText(job.getStatus(), locale);
			startDateTime = getJobDateTime(job.getStartDateTime());
			jobStep = getSchedMsgText(job.getJobStep(), locale);
			execResult = getSchedMsgText(job.getExecResult(), locale);
			createDateTime = getJobDateTime(job.getCreateDateTime());
			createUser = nvl(job.getCreateUser());
			updateDateTime = getJobDateTime(job.getUpdateDateTime());
			updateUser = nvl(job.getUpdateUser());
			canExec = getCanExec(job.getStartDateTime());
			
			dpb0058RespItem = new DPB0058RespItem();
			dpb0058RespItem.setApptJobId(job.getApptJobId());
			dpb0058RespItem.setRefItemNo(refItemNo);
			dpb0058RespItem.setRefSubitemNo(refSubitemNo);
			dpb0058RespItem.setIdentifData(identifData);
			dpb0058RespItem.setStatus(status);
			dpb0058RespItem.setStartDateTime(startDateTime);
			dpb0058RespItem.setJobStep(jobStep);
			dpb0058RespItem.setExecResult(execResult);
			dpb0058RespItem.setCreateDateTime(createDateTime);
			dpb0058RespItem.setCreateUser(createUser);
			dpb0058RespItem.setUpdateDateTime(updateDateTime);
			dpb0058RespItem.setUpdateUser(updateUser);
			dpb0058RespItem.setCanExec(canExec);
			dpb0058RespItems.add(dpb0058RespItem);
		}
		return dpb0058RespItems;
	}

	private String getRefItemNo(String refItemNo, String locale) {
		/*
		TsmpDpItems items = getItemsById(TsmpDpReqReviewType.ITEM_NO, refItemNo, false);
		if (items != null) {
			String itemName = items.getSubitemName();
			return refItemNo.concat(" - ").concat(itemName);
		}
		*/
		/*
		List<TsmpDpItems> items = getTsmpDpItemsDao().findByItemNo(refItemNo);
		if (items != null && !items.isEmpty()) {
			return refItemNo.concat("-").concat(items.get(0).getItemName());
		}
		return new String();
		*/

		TsmpDpItems items = getItemsById("SCHED_CATE1", refItemNo, false, locale);
		if (items == null) {
			return new String();
		}
		return items.getSubitemNo().concat(" - ").concat(items.getSubitemName());
	}

	private String getRefSubitemNo(String refItemNo, String refSubitemNo, String locale) {
		// 看排程大分類的param1是否有設定子類別
		TsmpDpItems items = getItemsById("SCHED_CATE1", refItemNo, false, locale);
		if (items == null) {
			return new String();
		}
		String param1 = items.getParam1();
		if ("-1".equals(param1)) {
			return new String();
		} else if (!StringUtils.isEmpty(refSubitemNo)) {
			items = getItemsById(refItemNo, refSubitemNo, false, locale);
			if (items != null) {
				return refSubitemNo.concat(" - ").concat(items.getSubitemName());
			}
		}
		return new String();
	}

	private String getStatusText(String status, String locale) {
		if (!StringUtils.isEmpty(status)) {
			TsmpDpItems items = getItemsById("JOB_STATUS", status, false, locale);
			if (items != null) {
				return status.concat("：").concat(items.getSubitemName());
			}
		}
		return new String();
	}

	/**
	 * 取得排程 "進度" & "執行結果" 欄位要顯示在畫面的值,
	 * 因儲存時會用 TSMP_DP_ITEMS 的 SUBITEM_NO 值, 
	 * 顯示在畫面時,要轉換成 TSMP_DP_ITEMS 的 SUBITEM_NAME, 若找不到對應的資料, 則顯示 TSMP_DP_APPT_JOB 原資料
	 * 
	 * @param schedMsg
	 * @return
	 */
	private String getSchedMsgText(String schedMsg, String locale) {
		if (!StringUtils.isEmpty(schedMsg)) {
			TsmpDpItems items = getItemsById("SCHED_MSG", schedMsg, false, locale);
			if (items != null) {
				return items.getSubitemName();
			}else {
				return schedMsg;
			}
		}
		return "";
	}
	
	private String getJobDateTime(Date dt) {
		return DateTimeUtil.dateTimeToString(dt, DateTimeFormatEnum.西元年月日時分秒_2).orElse(new String());
	}

	private String getCanExec(Date startDateTime) {
		return (startDateTime.compareTo(DateTimeUtil.now()) <= 0 ? "Y" : "N");
	}

	private TsmpDpItems getItemsById(String itemNo, String subitemNo, boolean errorWhenNotExists, String locale) {
		TsmpDpItemsId id = new TsmpDpItemsId(itemNo, subitemNo, locale);
		TsmpDpItems i = getTsmpDpItemsCacheProxy().findById(id);
		if (errorWhenNotExists && i == null) {
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return i;
	}

	private Date checkDt(String dtStr, boolean isEnd) {
		Optional<LocalDate> dpb0058_opt = DateTimeUtil.stringToLocalDate(dtStr, DateTimeFormatEnum.西元年月日_2);
		if (!dpb0058_opt.isPresent()) {
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		LocalDate ld = dpb0058_opt.get();
		if (isEnd) {
			ld = ld.plusDays(1L);
		}
		// set time to 00:00:00
		final ZonedDateTime dpb0058_ldt = ld.atStartOfDay(ZoneId.systemDefault());
		return Date.from(dpb0058_ldt.toInstant());
	}
	
	private String printDt(Date dpb0058_dt) {
		return DateTimeUtil.dateTimeToString(dpb0058_dt, DateTimeFormatEnum.西元年月日時分秒_2).orElse(null);
	}

	private String nvl(Object dpb0058_input) {
		if (dpb0058_input == null) {
			return new String();
		}
		return dpb0058_input.toString();
	}


	protected TsmpDpApptJobDao getTsmpDpApptJobDao() {
		return this.tsmpDpApptJobDao;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return this.tsmpDpItemsCacheProxy;
	}

	protected ApptJobDispatcher getApptJobDispatcher() {
		return this.apptJobDispatcher;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb0058");
		return this.pageSize;
	}

}
