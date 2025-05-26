package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.dpaa.vo.DPB0104Items;
import tpi.dgrv4.dpaa.vo.DPB0104Req;
import tpi.dgrv4.dpaa.vo.DPB0104Resp;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.entity.TsmpDpApptRjob;
import tpi.dgrv4.entity.entity.TsmpDpApptRjobD;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.entity.repository.TsmpDpApptRjobDDao;
import tpi.dgrv4.entity.repository.TsmpDpApptRjobDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0104Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpDpApptRjobDao tsmpDpApptRjobDao;
	private TsmpDpApptRjobDDao tsmpDpApptRjobDDao;
	private TsmpDpApptJobDao tsmpDpApptJobDao;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private ServiceConfig serviceConfig;
	
	private Integer pageSize;
	
	@Autowired
	public DPB0104Service(TsmpDpApptRjobDao tsmpDpApptRjobDao, TsmpDpApptRjobDDao tsmpDpApptRjobDDao,
			TsmpDpApptJobDao tsmpDpApptJobDao, TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy,
			ServiceConfig serviceConfig) {
		super();
		this.tsmpDpApptRjobDao = tsmpDpApptRjobDao;
		this.tsmpDpApptRjobDDao = tsmpDpApptRjobDDao;
		this.tsmpDpApptJobDao = tsmpDpApptJobDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.serviceConfig = serviceConfig;
	}

	public DPB0104Resp queryRjobHistory(TsmpAuthorization auth, DPB0104Req req, ReqHeader reqHeader) {
		
		String apptRjobId = req.getApptRjobId();
		if (StringUtils.isEmpty(apptRjobId)) {
			this.logger.debug("未傳入週期排程ID");
			throw TsmpDpAaRtnCode._1296.throwing();
		}

		Optional<TsmpDpApptRjob> opt = getTsmpDpApptRjobDao().findById(apptRjobId);
		if (!opt.isPresent()) {
			this.logger.debug("週期排程ID不存在: " + apptRjobId);
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		
		TsmpDpApptJob lastRecord = getLastRecordFromPrevPage(req.getApptJobId());
		Integer pageSize = getPageSize();
		List<TsmpDpApptJob> jobList = getTsmpDpApptJobDao().query_dpb0104Service_1(apptRjobId, lastRecord, pageSize);
		if (jobList == null || jobList.isEmpty()) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}

		TsmpDpApptRjob rjob = opt.get();
		List<DPB0104Items> historyList = getHistoryList(jobList, reqHeader.getLocale());

		DPB0104Resp resp = new DPB0104Resp();
		resp.setApptRjobId(apptRjobId);
		resp.setRjobName(rjob.getRjobName());
		resp.setRemark(nvl(rjob.getRemark()));
		resp.setHistoryList(historyList);
		return resp;
	}

	private TsmpDpApptJob getLastRecordFromPrevPage(Long apptJobId) {
		if (apptJobId != null) {
			Optional<TsmpDpApptJob> opt = getTsmpDpApptJobDao().findById(apptJobId);
			if (!opt.isPresent()) {
				this.logger.debug("工作ID不存在: " + apptJobId);
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			return opt.get();
		}
		return null;
	}

	private List<DPB0104Items> getHistoryList(List<TsmpDpApptJob> jobList, String locale) {
		List<DPB0104Items> dpb0104ItemsList = new ArrayList<>();
		
		DPB0104Items dpb0104Items = null;
		String refItemName = null;
		String refSubitemNo = null;
		String refSubitemName = null;
		String sortBy = null;
		String periodNexttime = null;
		String updateDateTime = null;
		String statusName = null;
		String execResult = null;
		for (TsmpDpApptJob job : jobList) {
			refItemName = getRefItemName(job.getRefItemNo(), locale);
			refSubitemNo = nvl(job.getRefSubitemNo());
			refSubitemName = getRefSubitemName(job.getRefItemNo(), refSubitemNo, locale);
			sortBy = getSortBy(job.getPeriodItemsId(), job.getPeriodUid());
			periodNexttime = getPeriodNexttime(job.getPeriodNexttime());
			updateDateTime = getUpdateDateTime(job.getUpdateDateTime());
			statusName = getStatusName(job.getStatus(), locale);
			execResult = getSchedMsgText(job.getExecResult(), locale);
			
			dpb0104Items = new DPB0104Items();
			dpb0104Items.setApptJobId(job.getApptJobId());
			dpb0104Items.setRefItemNo(job.getRefItemNo());
			dpb0104Items.setRefItemName(refItemName);
			dpb0104Items.setRefSubitemNo(refSubitemNo);
			dpb0104Items.setRefSubitemName(refSubitemName);
			dpb0104Items.setSortBy(sortBy);
			dpb0104Items.setPeriodNexttime(periodNexttime);
			dpb0104Items.setUpdateDateTime(updateDateTime);
			dpb0104Items.setStatus(job.getStatus());
			dpb0104Items.setStatusName(statusName);
			dpb0104Items.setExecResult(execResult);
			dpb0104ItemsList.add(dpb0104Items);
		}
		
		return dpb0104ItemsList;
	}

	private String getRefItemName(String refItemNo, String locale) {
		TsmpDpItems items = getItemsById("SCHED_CATE1", refItemNo, false, locale);
		if (items != null) {
			return items.getSubitemName();
		}
		return new String();
	}

	private String getRefSubitemName(String refItemNo, String refSubitemNo, String locale) {
		if (StringUtils.isEmpty(refSubitemNo)) {
			return new String();
		}
		TsmpDpItems items = getItemsById(refItemNo, refSubitemNo, false, locale);
		if (items != null) {
			return items.getSubitemName();
		}
		return new String();
	}

	/**
	 * 回傳"N", 表示找不到排程順序, 該項目已不在排程清單中
	 * @param apptRjobDid
	 * @param apptRjobId
	 * @return
	 */
	private String getSortBy(Long apptRjobDid, String apptRjobId) {
		Optional<TsmpDpApptRjobD> opt = getTsmpDpApptRjobDDao().findById(apptRjobDid);
		if (!opt.isPresent()) {
			return "N";
		}
		TsmpDpApptRjobD rjobD = opt.get();
		if (!rjobD.getApptRjobId().equals(apptRjobId)) {
			this.logger.debug(String.format("週期排程ID不符: %s but %s", rjobD.getApptRjobId(), apptRjobId));
			return "N";
		}
		return String.valueOf(rjobD.getSortBy());
	}

	private String getPeriodNexttime(Long periodNexttime) {
		return DateTimeUtil.dateTimeToString(new Date(periodNexttime), DateTimeFormatEnum.西元年月日時分秒_2).orElse(new String());
	}

	private String getUpdateDateTime(Date updateDateTime) {
		return DateTimeUtil.dateTimeToString(updateDateTime, DateTimeFormatEnum.西元年月日時分秒_2).orElse(new String());
	}

	private String getStatusName(String status, String locale) {
		TsmpDpItems items = getItemsById("JOB_STATUS", status, false, locale);
		if (items != null) {
			return items.getSubitemName();
		}
		return new String();
	}

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

	private TsmpDpItems getItemsById(String itemNo, String subitemNo, boolean errorWhenNotExists, String locale) {
		TsmpDpItemsId id = new TsmpDpItemsId(itemNo, subitemNo, locale);
		TsmpDpItems i = getTsmpDpItemsCacheProxy().findById(id);
		if (errorWhenNotExists && i == null) {
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return i;
	}

	protected TsmpDpApptRjobDao getTsmpDpApptRjobDao() {
		return this.tsmpDpApptRjobDao;
	}

	protected TsmpDpApptRjobDDao getTsmpDpApptRjobDDao() {
		return this.tsmpDpApptRjobDDao;
	}

	protected TsmpDpApptJobDao getTsmpDpApptJobDao() {
		return this.tsmpDpApptJobDao;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return tsmpDpItemsCacheProxy;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb0104");
		return this.pageSize;
	}

}
