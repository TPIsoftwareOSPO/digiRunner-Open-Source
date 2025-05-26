package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.getKeywords;
import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.constant.TsmpDpPublicFlag;
import tpi.dgrv4.common.constant.TsmpDpRegStatus;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.component.job.DPB0006Job;
import tpi.dgrv4.dpaa.vo.DPB0006Client;
import tpi.dgrv4.dpaa.vo.DPB0006File;
import tpi.dgrv4.dpaa.vo.DPB0006Req;
import tpi.dgrv4.dpaa.vo.DPB0006Resp;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpDpClientext;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderd3;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrders;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpDpClientextDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrderd3Dao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdersDao;
import tpi.dgrv4.entity.vo.DPB0006SearchCriteria;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0006Service {

	private TPILogger logger = TPILogger.tl;;
	
	private TsmpClientDao tsmpClientDao;
	private TsmpDpClientextDao tsmpDpClientextDao;
	private TsmpDpFileDao tsmpDpFileDao;
	private JobHelper jobHelper;
	private ApplicationContext ctx;
	private ServiceConfig serviceConfig;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private TsmpDpReqOrderd3Dao tsmpDpReqOrderd3Dao;
	private TsmpDpReqOrdersDao tsmpDpReqOrdersDao;
	
	private Integer pageSize;

	@Autowired
	public DPB0006Service(TsmpClientDao tsmpClientDao, TsmpDpClientextDao tsmpDpClientextDao,
			TsmpDpFileDao tsmpDpFileDao, JobHelper jobHelper, ApplicationContext ctx, ServiceConfig serviceConfig,
			TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, TsmpDpReqOrderd3Dao tsmpDpReqOrderd3Dao,
			TsmpDpReqOrdersDao tsmpDpReqOrdersDao) {
		super();
		this.tsmpClientDao = tsmpClientDao;
		this.tsmpDpClientextDao = tsmpDpClientextDao;
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.jobHelper = jobHelper;
		this.ctx = ctx;
		this.serviceConfig = serviceConfig;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.tsmpDpReqOrderd3Dao = tsmpDpReqOrderd3Dao;
		this.tsmpDpReqOrdersDao = tsmpDpReqOrdersDao;
	}

	public DPB0006Resp queryMemberHistory(TsmpAuthorization authorization, DPB0006Req req, ReqHeader reqHeader) {
		Map<String, Object> map = queryMemberHistory2(authorization, req, reqHeader);
		return (DPB0006Resp) map.get("Resp");
	}

	public Map<String, Object> queryMemberHistory2(TsmpAuthorization authorization, DPB0006Req req, ReqHeader reqHeader) {
		DPB0006Job job = null;
		String regStatus = req.getRegStatus();
		List<String> regStatusList = getRegStatusList(regStatus);
		if (regStatusList == null || regStatusList.isEmpty()) {
			throw TsmpDpAaRtnCode.EMPTY_STATUS_OR_START_END_DATE.throwing();
		}

		String startDateStr = req.getStartDate();
		Optional<Date> opt_start = DateTimeUtil.stringToDateTime(startDateStr, DateTimeFormatEnum.西元年月日_2);
		String endDateStr = req.getEndDate();
		Optional<Date> opt_end = DateTimeUtil.stringToDateTime(endDateStr, DateTimeFormatEnum.西元年月日_2);
		/* 2020/07/03 允許不使用日期區間查詢
		if (!opt_start.isPresent() || !opt_end.isPresent()) {
			throw TsmpDpAaRtnCode.EMPTY_STATUS_OR_START_END_DATE.throwing();
		}
		*/

		Date startDate = null;
		if (opt_start.isPresent()) {
			startDate = setTime(opt_start.get(), 1);
		}
		Date endDate = null;
		if (opt_end.isPresent()) {
			endDate = setTime(opt_end.get(), 2);
		}
		String words[] = getKeywords(req.getKeyword(), " ");
		Integer pageSize = getPageSize();
		TsmpDpClientext lastRecord = getLastRecordFromPrevPage(req.getClientId());

		DPB0006SearchCriteria cri = new DPB0006SearchCriteria();
		cri.setStartDate(startDate);
		cri.setEndDate(endDate);
		cri.setRegStatusList(regStatusList);
		cri.setWords(words);
		cri.setPageSize(pageSize);
		if (lastRecord != null) {
			cri.setLastClientId(lastRecord.getClientId());
			cri.setLastUpdateDateTime(lastRecord.getUpdateDateTime());
		} else {
			// 當查詢第一頁時, 以 [流式設計] 方式 刪除退回己久的clientName
			job = addDPB0006Job();
		}
		List<TsmpDpClientext> extList = getTsmpDpClientextDao().queryLikeRegStatusBetween(cri);
		if (CollectionUtils.isEmpty(extList)) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}

		DPB0006Resp resp = new DPB0006Resp();
		List<DPB0006Client> dpb0006ClientList = getDpb0006ClientList(extList, reqHeader.getLocale());
		resp.setClientList(dpb0006ClientList);
		
		Map<String, Object> map = new HashMap<>();
		map.put("Resp", resp);
		map.put("Job", job);
		return map;
	}

	private List<String> getRegStatusList(String regStatus) {
		List<String> regStatusList = new ArrayList<>();

		if (TsmpDpRegStatus.PASS.value().equals(regStatus) ||
			TsmpDpRegStatus.RETURN.value().equals(regStatus)) {
			regStatusList.add(regStatus);
		// 不允許其他值
		} else if (regStatus == null || regStatus.isEmpty()) {
			regStatusList.add(TsmpDpRegStatus.PASS.value());
			regStatusList.add(TsmpDpRegStatus.RETURN.value());
		}

		return regStatusList;
	}

	private Date setTime(Date date, int type) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		// 起日
		if (type == 1) {
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
		// 迄日
		} else if (type == 2) {
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.SECOND, 59);
		}
		return cal.getTime();
	}

	private TsmpDpClientext getLastRecordFromPrevPage(String clientId) {
		TsmpDpClientext lastRecord = null;

		if (clientId != null && !clientId.isEmpty()) {
			Optional<TsmpDpClientext> opt = getTsmpDpClientextDao().findById(clientId);
			if (opt.isPresent()) {
				lastRecord = opt.get();
			}
		}

		return lastRecord;
	}

	private List<DPB0006Client> getDpb0006ClientList(List<TsmpDpClientext> extList, String locale) {
		List<DPB0006Client> dpb0006ClientList = new ArrayList<>();

		if (extList != null && !extList.isEmpty()) {
			String clientId = null;
			TsmpClient client = null;
			String clientName = null;
			String clientAlias = null;
			String emails = null;
			String clientStatus = null;
			String resubmitDatetime;
			String regStatus;
			String regStatusName;
			Boolean applyFlag = null;
			List<DPB0006File> dpb0006FileList;
			String publicFlagName = null;
			DPB0006Client dpb0006Client;
			String status = null;
			String statusName = null;	
			String chkStatus = null;
			String chkStatusName = null;
			String checkPointName = null;
			for(TsmpDpClientext ext : extList) {
				clientId = ext.getClientId();
				clientName = new String();
				clientAlias = new String();
				emails = new String();
				clientStatus = new String();
				status = new String();
				statusName = new String();
				chkStatus = new String();
				chkStatusName = new String();
				checkPointName = new String();
				client = getTsmpClientDao().findById(clientId).orElse(null);
				if (client != null) {
					clientName = client.getClientName();
					clientAlias = nvl(client.getClientAlias());
					emails = nvl(client.getEmails());
					clientStatus = getClientStatus(client.getClientStatus());
					status = client.getClientStatus();
					statusName = getStatus(client.getClientStatus(), locale);
				}
				dpb0006Client = new DPB0006Client();
				dpb0006Client.setRefClientId(clientId);
				dpb0006Client.setClientId(clientId);
				dpb0006Client.setClientName(clientName);
				dpb0006Client.setClientAlias(clientAlias);
				dpb0006Client.setEmails(emails);
				dpb0006Client.setClientStatus(clientStatus);
				dpb0006Client.setApplyPurpose(ext.getContentTxt());
				dpb0006Client.setReviewRemark(ext.getReviewRemark());
				dpb0006Client.setRefReviewUser(ext.getRefReviewUser());	// refReviewUser直接就是放userName
				resubmitDatetime = getResubmitDatetime(ext.getResubmitDateTime());
				dpb0006Client.setResubmitDateTime(resubmitDatetime);
				regStatus = ext.getRegStatus();
				regStatusName = TsmpDpRegStatus.getText(regStatus);
				dpb0006Client.setRegStatus(regStatusName);
				applyFlag = getApplyFlag(client, regStatus);
				dpb0006Client.setApplyFlag(applyFlag);
				dpb0006FileList = getDpb0006FileList(ext.getClientSeqId());
				dpb0006Client.setFileList(dpb0006FileList);
				dpb0006Client.setPublicFlag(ext.getPublicFlag());
				publicFlagName = getPublicFlagName(ext.getPublicFlag(), locale);
				dpb0006Client.setPublicFlagName(publicFlagName);
				dpb0006Client.setStatus(status);
				dpb0006Client.setStatusName(statusName);
				Map<String,String> statusMap = getCurrentStatus(clientId, locale);
				chkStatus = statusMap.get("chkStatus");
				chkStatusName = statusMap.get("chkStatusName");
				checkPointName = statusMap.get("checkPointName");
				dpb0006Client.setChkStatus(chkStatus);
				dpb0006Client.setChkStatusName(chkStatusName);
				dpb0006Client.setCheckPointName(checkPointName);
				
				dpb0006ClientList.add(dpb0006Client);
			}
		}

		return dpb0006ClientList;
	}
	
	private Map<String,String> getCurrentStatus(String clientId, String locale) {
		Map<String,String> map = new HashMap<>();
		map.put("chkStatus", "");
		map.put("chkStatusName", "");
		map.put("checkPointName", "");
		List<TsmpDpReqOrderd3> d3List = getTsmpDpReqOrderd3Dao().findByClientId(clientId);
		if (d3List != null && d3List.size() == 1) {
			TsmpDpReqOrderd3 d3 = d3List.get(0);
			TsmpDpReqOrders currentS = getTsmpDpReqOrdersDao().queryCurrentStatus(d3.getRefReqOrdermId());
			if (currentS != null) {
				String chkStatus = currentS.getReviewStatus();
				String chkStatusName = getSubitemName("REVIEW_STATUS", chkStatus, locale);
				Integer layer = currentS.getLayer();
				String checkPointName = getSubitemName("CHK_LAYER", String.valueOf(layer), locale);
				map.put("chkStatus", chkStatus);
				map.put("chkStatusName", chkStatusName);
				map.put("checkPointName", checkPointName);
			}
		}else {
			this.logger.debug(String.format("取得用戶註冊(%s)簽核單錯誤: D3.size=%d", clientId, (d3List == null ? 0 : d3List.size())));
		}
		
		return map;
	}
	
	private String getSubitemName(String itemNo, String subitemNo, String locale) {
		if (!StringUtils.isEmpty(itemNo) && !StringUtils.isEmpty(subitemNo) && !StringUtils.isEmpty(locale)) {
			TsmpDpItemsId id = new TsmpDpItemsId(itemNo, subitemNo, locale);
			TsmpDpItems i = getTsmpDpItemsCacheProxy().findById(id);
			if (i != null) {
				return i.getSubitemName();
			}
		}
		return new String();
	}
	
	private String getStatus(String status, String locale) {
		// 不應該有空值, 但資料庫預設值為"1-正常"
		if(StringUtils.isEmpty(status)) {
			status = "1";
		}
		TsmpDpItems vo = getTsmpDpItemsCacheProxy().findByItemNoAndParam1AndLocale("ENABLE_FLAG", status, locale);
		status = "";
		if(vo != null) {
			status = vo.getSubitemName();
		}
		
		return status;

	}

	private String getClientStatus(String clientStatus) {
		// 不應該有空值, 但資料庫預設值為"1-正常"
		if (StringUtils.isEmpty(clientStatus)) {
			return "正常";
		} else if ("1".equals(clientStatus)) {
			return "正常";
		} else if ("2".equals(clientStatus)) {
			return "停用";
		} else if ("3".equals(clientStatus)) {
			return "鎖定";
		}
		return clientStatus;
	}

	private Boolean getApplyFlag(TsmpClient client, String regStatus) {
		if (client == null) {
			return Boolean.FALSE;
		}
		
		String clientStatus = client.getClientStatus();
		// 用戶狀態為"正常"且註冊狀態為"放行"才可申請使用API
		if (
			!StringUtils.isEmpty(clientStatus) && "1".equals(clientStatus) &&
			!StringUtils.isEmpty(regStatus) && TsmpDpRegStatus.PASS.value().equals(regStatus)
		) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	private String getPublicFlagName(String publicFlag, String locale) {
		if (StringUtils.isEmpty(publicFlag)) {
			publicFlag = TsmpDpPublicFlag.EMPTY.value();
		}

		TsmpDpItemsId id = new TsmpDpItemsId("API_AUTHORITY", publicFlag, locale);
		TsmpDpItems vo = getTsmpDpItemsCacheProxy().findById(id);
		if (vo != null) {
			return vo.getSubitemName();
		}
		return new String();
	}

	private String getResubmitDatetime(Date resubmitDatetime) {
		if (resubmitDatetime != null) {
			Optional<String> opt = DateTimeUtil.dateTimeToString(resubmitDatetime, //
					DateTimeFormatEnum.西元年月日_2);
			if (opt.isPresent()) {
				return opt.get();
			}
		}
		return null;
	}

	private List<DPB0006File> getDpb0006FileList(Long refId) {
		List<DPB0006File> dpb0006FileList = new ArrayList<>();

		if (refId != null) {
			String refFileCateCode = TsmpDpFileType.MEMBER_APPLY.value();
			List<TsmpDpFile> fileList = getTsmpDpFileDao().findByRefFileCateCodeAndRefId(//
					refFileCateCode, refId);
			if (fileList != null && !fileList.isEmpty()) {
				DPB0006File dpb0006File;
				for(TsmpDpFile tsmpDpFile : fileList) {
					dpb0006File = new DPB0006File();
					dpb0006File.setFileName(tsmpDpFile.getFileName());
					dpb0006File.setFilePath(tsmpDpFile.getFilePath());
					dpb0006File.setFileId(tsmpDpFile.getFileId());
					dpb0006FileList.add(dpb0006File);
				}
			}
		}
		
		return dpb0006FileList;
	}

	protected DPB0006Job addDPB0006Job() {
		DPB0006Job job = getDPB0006Job();
		getJobHelper().add(job);
		return job;
	}
	
	protected DPB0006Job getDPB0006Job() {
		return (DPB0006Job) getCtx().getBean("dpb0006Job");
	}
	
	protected TsmpClientDao getTsmpClientDao() {
		return this.tsmpClientDao;
	}

	protected TsmpDpClientextDao getTsmpDpClientextDao() {
		return this.tsmpDpClientextDao;
	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}

	protected JobHelper getJobHelper() {
		return this.jobHelper;
	}

	protected ApplicationContext getCtx() {
		return this.ctx;
	}
	
	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb0006");
		return this.pageSize;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return this.tsmpDpItemsCacheProxy;
	}
	
	protected TsmpDpReqOrderd3Dao getTsmpDpReqOrderd3Dao() {
		return this.tsmpDpReqOrderd3Dao;
	}
	
	protected TsmpDpReqOrdersDao getTsmpDpReqOrdersDao() {
		return this.tsmpDpReqOrdersDao;
	}
}
