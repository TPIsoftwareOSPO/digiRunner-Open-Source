package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.getKeywords;
import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpRjobStatus;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0102Items;
import tpi.dgrv4.dpaa.vo.DPB0102Req;
import tpi.dgrv4.dpaa.vo.DPB0102Resp;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpDpApptRjob;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.repository.TsmpDpApptRjobDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0102Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpDpApptRjobDao tsmpDpApptRjobDao;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private BcryptParamHelper bcryptParamHelper;
	private ServiceConfig serviceConfig;
	
	private Integer pageSize;

	@Autowired
	public DPB0102Service(TsmpDpApptRjobDao tsmpDpApptRjobDao, TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy,
			BcryptParamHelper bcryptParamHelper, ServiceConfig serviceConfig) {
		super();
		this.tsmpDpApptRjobDao = tsmpDpApptRjobDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.bcryptParamHelper = bcryptParamHelper;
		this.serviceConfig = serviceConfig;
	}

	public DPB0102Resp queryRjobList(TsmpAuthorization auth, DPB0102Req req, ReqHeader reqHeader) {
		String locale = ServiceUtil.getLocale(reqHeader.getLocale());

		checkParams(req, locale);

		String status = req.getStatus();
		TsmpDpApptRjob lastRecord = getLastRecordFromPrevPage(req.getApptRjobId());
		String[] keywords = getKeywords(req.getKeyword(), " ");
		Integer pageSize = getPageSize();
		List<TsmpDpApptRjob> rjobList = getTsmpDpApptRjobDao().query_dpb0102Service_1(status, lastRecord, keywords, locale, pageSize);
		if (rjobList == null || rjobList.isEmpty()) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}

		DPB0102Resp resp = new DPB0102Resp();
		List<DPB0102Items> dataList = getDataList(rjobList, locale);
		resp.setDataList(dataList);
		return resp;
	}

	private void checkParams(DPB0102Req req, String locale) {
		String status = req.getStatus();
		if (status != null) {
			try {
				status = getBcryptParamHelper().decode(status, "RJOB_STATUS", locale);
				req.setStatus(status);
			} catch (BcryptParamDecodeException e) {
				throw TsmpDpAaRtnCode._1299.throwing();
			}
		}
	}

	private TsmpDpApptRjob getLastRecordFromPrevPage(String apptRjobId) {
		if (!StringUtils.isEmpty(apptRjobId)) {
			return getTsmpDpApptRjobDao().findById(apptRjobId).orElse(null);
		}
		return null;
	}

	private List<DPB0102Items> getDataList(List<TsmpDpApptRjob> rjobList, String locale) {
		List<DPB0102Items> dataList = new ArrayList<>();
		
		DPB0102Items dpb0102Items = null;
		String statusName = null;
		String nextDateTime = null;
		String effPeriod = null;
		for (TsmpDpApptRjob rjob : rjobList) {
			statusName = getStatusName(rjob.getStatus(), locale);
			nextDateTime = getNextDateTime(rjob.getStatus(), rjob.getNextDateTime());
			effPeriod = getEffPeriod(rjob.getEffDateTime(), rjob.getInvDateTime());
			
			dpb0102Items = new DPB0102Items();
			dpb0102Items.setApptRjobId(rjob.getApptRjobId());
			dpb0102Items.setLv(rjob.getVersion());
			dpb0102Items.setRjobName(rjob.getRjobName());
			dpb0102Items.setStatusName(statusName);
			dpb0102Items.setCronDesc(nvl(rjob.getCronDesc()));
			dpb0102Items.setNextDateTime(nextDateTime);
			dpb0102Items.setEffPeriod(effPeriod);
			dpb0102Items.setRemark(nvl(rjob.getRemark()));
			setFlags(dpb0102Items, rjob.getStatus());
			dataList.add(dpb0102Items);
		}

		return dataList;
	}

	private String getStatusName(String status, String locale) {
		if (!StringUtils.isEmpty(status)) {
			TsmpDpItems dpb0102_rjobStatus = getItemsById("RJOB_STATUS", status, false, locale);
			if (dpb0102_rjobStatus != null) {
				return dpb0102_rjobStatus.getSubitemName();
			}
		}
		return new String();
	}

	private TsmpDpItems getItemsById(String itemNo, String subitemNo, boolean errorWhenNotExists, String locale) {
		TsmpDpItemsId id = new TsmpDpItemsId(itemNo, subitemNo, locale);
		TsmpDpItems dpb0102_i = getTsmpDpItemsCacheProxy().findById(id);
		if (errorWhenNotExists && dpb0102_i == null) {
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return dpb0102_i;
	}

	/**
	 * 如果狀態是"暫停"或"作廢", 則後端傳回一個"-", 用以隱藏下次執行時間
	 * @param status
	 * @param nextDt
	 * @return
	 */
	private String getNextDateTime(String status, Long nextDt) {
		if (nextDt == null ||
			StringUtils.isEmpty(status) ||
			TsmpDpRjobStatus.PAUSE.value().equals(status) ||
			TsmpDpRjobStatus.DISABLED.value().equals(status)
		) {
			return "-";
		}
		return DateTimeUtil.dateTimeToString(new Date(nextDt), DateTimeFormatEnum.西元年月日時分秒_2).orElse(new String());
	}

	private String getEffPeriod(Long effDt, Long invDt) {
		String effPeriod = new String();
		String effDateTime = new String();
		String invDateTime = new String();
		if (effDt != null) {
			effDateTime = DateTimeUtil.dateTimeToString(new Date(effDt), DateTimeFormatEnum.西元年月日時分秒_2).orElse(new String());
		}
		if (invDt != null) {
			invDateTime = DateTimeUtil.dateTimeToString(new Date(invDt), DateTimeFormatEnum.西元年月日時分秒_2).orElse(new String());
		}
		if (!StringUtils.isEmpty(effDateTime)) {
			effPeriod += effDateTime;
			if (!StringUtils.isEmpty(invDateTime)) {
				effPeriod += " ~ " + invDateTime;
			}
		} else if (!StringUtils.isEmpty(invDateTime)) {
			effPeriod += invDateTime;
		}
		return effPeriod;
	}

	protected void setFlags(DPB0102Items dpb0102Items, String status) {
		dpb0102Items.setDetailFlag(true);	// 預設可用
		dpb0102Items.setUpdateFlag(false);
		dpb0102Items.setPauseVisible(false);
		dpb0102Items.setPauseFlag(false);
		dpb0102Items.setActiveVisible(false);
		dpb0102Items.setActiveFlag(false);
		dpb0102Items.setSkipFlag(false);
		dpb0102Items.setInactiveFlag(false);
		dpb0102Items.setHistoryFlag(true);	// 預設可用
		
		// 作廢
		if (TsmpDpRjobStatus.DISABLED.value().equals(status)) {
			dpb0102Items.setUpdateFlag(false);
			dpb0102Items.setPauseVisible(true);		// 顯示但不能按
			dpb0102Items.setPauseFlag(false);
			dpb0102Items.setActiveVisible(true);	// 顯示但不能按
			dpb0102Items.setActiveFlag(false);
			dpb0102Items.setSkipFlag(false);
			dpb0102Items.setInactiveFlag(false);
		// 啟動
		} else if (TsmpDpRjobStatus.ACTIVE.value().equals(status)) {
			dpb0102Items.setUpdateFlag(true);
			dpb0102Items.setPauseVisible(true);
			dpb0102Items.setPauseFlag(true);
			dpb0102Items.setActiveVisible(false);
			dpb0102Items.setActiveFlag(false);
			dpb0102Items.setSkipFlag(true);
			dpb0102Items.setInactiveFlag(true);
		// 暫停
		} else if (TsmpDpRjobStatus.PAUSE.value().equals(status)) {
			dpb0102Items.setUpdateFlag(true);
			dpb0102Items.setPauseVisible(false);
			dpb0102Items.setPauseFlag(false);
			dpb0102Items.setActiveVisible(true);
			dpb0102Items.setActiveFlag(true);
			dpb0102Items.setSkipFlag(true);
			dpb0102Items.setInactiveFlag(true);
		// 執行中
		} else if (TsmpDpRjobStatus.IN_PROGRESS.value().equals(status)) {
			dpb0102Items.setUpdateFlag(false);
			dpb0102Items.setPauseVisible(true);		// 顯示但不能按
			dpb0102Items.setPauseFlag(false);
			dpb0102Items.setActiveVisible(true);	// 顯示但不能按
			dpb0102Items.setActiveFlag(false);
			dpb0102Items.setSkipFlag(false);
			dpb0102Items.setInactiveFlag(false);
		}
	}

	protected TsmpDpApptRjobDao getTsmpDpApptRjobDao() {
		return this.tsmpDpApptRjobDao;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return this.tsmpDpItemsCacheProxy;
	}

	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb0102");
		return this.pageSize;
	}

}
