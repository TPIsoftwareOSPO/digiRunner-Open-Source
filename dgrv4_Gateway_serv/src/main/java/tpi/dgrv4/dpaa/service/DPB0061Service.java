package tpi.dgrv4.dpaa.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.vo.DPB0061Req;
import tpi.dgrv4.dpaa.vo.DPB0061Resp;
import tpi.dgrv4.dpaa.vo.DPB0061RespItem;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0061Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpDpApptJobDao tsmpDpApptJobDao;
	private TsmpDpFileDao tsmpDpFileDao;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private FileHelper fileHelper;

	@Autowired
	public DPB0061Service(TsmpDpApptJobDao tsmpDpApptJobDao, TsmpDpFileDao tsmpDpFileDao,
			TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, FileHelper fileHelper) {
		super();
		this.tsmpDpApptJobDao = tsmpDpApptJobDao;
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.fileHelper = fileHelper;
	}

	public DPB0061Resp queryByPk(TsmpAuthorization auth, DPB0061Req req, ReqHeader reqHeader) {
		// 檢查必要參數
		final Long apptJobId = req.getApptJobId();
		if (apptJobId == null) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		Optional<TsmpDpApptJob> opt = getTsmpDpApptJobDao().findById(apptJobId);
		if (!opt.isPresent()) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		final TsmpDpApptJob job = opt.get();
		
		DPB0061Resp resp = null;
		try {
			resp = getDpb0061Resp(job, reqHeader.getLocale());
		} catch (Exception e) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		return resp;
	}

	private DPB0061Resp getDpb0061Resp(TsmpDpApptJob job, String locale) {
		DPB0061Resp dpb0061Resp = new DPB0061Resp();
		
		String itemName = getItemName(job.getRefItemNo(), locale);
		String subItemName = getSubItemName(job.getRefItemNo(), job.getRefSubitemNo(), locale);
		String status = getStatusText(job.getStatus(), locale);
		String inParams = nvl(job.getInParams());
		String execResult = getSchedMsgText(job.getExecResult(), locale);
		String execOwner = nvl(job.getExecOwner());
		String stackTrace = nvl(job.getStackTrace());
		String jobStep = getSchedMsgText(job.getJobStep(), locale);
		String startDateTime = getJobDateTime(job.getStartDateTime());
		Long fromJobId = job.getFromJobId() == null ? -1 : job.getFromJobId();
		String createDateTime = getJobDateTime(job.getCreateDateTime());
		String createUser = nvl(job.getCreateUser());
		String updateDateTime = getJobDateTime(job.getUpdateDateTime());
		String updateUser = nvl(job.getUpdateUser());
		String canExec = getCanExec(job.getStartDateTime());
		String identifData = nvl(job.getIdentifData());
		List<DPB0061RespItem> fileList = getFileList(job.getApptJobId(), job.getRefItemNo());
		
		dpb0061Resp.setApptJobId(job.getApptJobId());
		dpb0061Resp.setRefItemNo(job.getRefItemNo());
		dpb0061Resp.setItemName(itemName);
		dpb0061Resp.setRefSubitemNo(job.getRefSubitemNo());
		dpb0061Resp.setSubItemName(subItemName);
		dpb0061Resp.setStatus(status);
		dpb0061Resp.setInParams(inParams);
		dpb0061Resp.setExecResult(execResult);
		dpb0061Resp.setExecOwner(execOwner);
		dpb0061Resp.setStackTrace(stackTrace);
		dpb0061Resp.setJobStep(jobStep);
		dpb0061Resp.setStartDateTime(startDateTime);
		dpb0061Resp.setFromJobId(fromJobId);
		dpb0061Resp.setCreateDateTime(createDateTime);
		dpb0061Resp.setCreateUser(createUser);
		dpb0061Resp.setUpdateDateTime(updateDateTime);
		dpb0061Resp.setUpdateUser(updateUser);
		dpb0061Resp.setLv(job.getVersion());
		dpb0061Resp.setCanExec(canExec);
		dpb0061Resp.setIdentifData(identifData);
		dpb0061Resp.setFileList(fileList);
		
		return dpb0061Resp;
	}

	private String getItemName(String refItemNo, String locale) {
		TsmpDpItems items = getItemsById("SCHED_CATE1", refItemNo, false, locale);
		if (items == null) {
			return new String();
		}
		return items.getSubitemName();
	}

	private String getSubItemName(String refItemNo, String refSubitemNo, String locale) {
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
				return items.getSubitemName();
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

	private String getJobDateTime(Date dt) {
		return DateTimeUtil.dateTimeToString(dt, DateTimeFormatEnum.西元年月日時分秒_2).orElse(new String());
	}

	private String getCanExec(Date startDateTime) {
		return (startDateTime.compareTo(DateTimeUtil.now()) <= 0 ? "Y" : "N");
	}

	private TsmpDpItems getItemsById(String itemNo, String subitemNo, boolean errorWhenNotExists, String locale) {
		TsmpDpItemsId id = new TsmpDpItemsId(itemNo, subitemNo, locale);
		TsmpDpItems dpb0061_vo = getTsmpDpItemsCacheProxy().findById(id);
		if (errorWhenNotExists) {
			if(dpb0061_vo != null) {
				return dpb0061_vo;
			}else {
				throw TsmpDpAaRtnCode._1297.throwing();
			}
		} else {
			return dpb0061_vo;
		}
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

	private List<DPB0061RespItem> getFileList(Long apptJobId, String refItemNo) {
		List<TsmpDpFile> fileList = getTsmpDpFileDao().findByRefFileCateCodeAndRefId(//
			TsmpDpFileType.TSMP_DP_APPT_JOB.value(), apptJobId);
		if ("SEND_MAIL".equals(refItemNo)) {
			String refFileCateCode = TsmpDpFileType.MAIL_CONTENT.value();
			String fileNameSuffix = String.valueOf(apptJobId).concat(".mail");
			fileList.addAll( getTsmpDpFileDao().query_DPB0061Service_01(refFileCateCode, fileNameSuffix) );
		}
		if (CollectionUtils.isEmpty(fileList)) {
			return null;
		}
		List<DPB0061RespItem> itemList = new ArrayList<>();
		DPB0061RespItem item = null;
		String isPreviewable = null;
		String fileContent = null;
		for (TsmpDpFile file : fileList) {
			fileContent = null;
			isPreviewable = "N";
			try {
				fileContent = transFileContent(file);
				isPreviewable = (ObjectUtils.isEmpty(fileContent) ? "N" : "Y");
			} catch (Exception e) {
				this.logger.debug(String.format("%d-排程檔案內容(fileId=%d)無法轉換為純文字，%s", apptJobId, file.getFileId(), e.getMessage()));
			}
			
			item = new DPB0061RespItem();
			item.setFileId(file.getFileId());
			item.setFileName(file.getFileName());
			item.setFilePath(file.getFilePath() + file.getFileName());
			item.setIsPreviewable(isPreviewable);
			item.setFileContent(fileContent);
			itemList.add(item);
		}
		return itemList;
	}

	// 僅轉換副檔名為 ["mail", "log", "txt", "json"] 的檔案
	private String transFileContent(TsmpDpFile tsmpDpFile) throws Exception {
		String ext = geExtFileName(tsmpDpFile.getFileName());
		ext = ext.toLowerCase(Locale.ENGLISH);
		if (!ext.matches("^(mail|log|txt|json)$")) {
			throw new Exception("副檔名格式不符: " + ext);
		}
		
		byte[] content = getFileHelper().download(tsmpDpFile);
		if (content == null) {
			throw new Exception("空白的檔案內容");
		}
		
		return new String(content, StandardCharsets.UTF_8);
	}

	/**
	 * 取得副檔名
	 * @return
	 */
	private String geExtFileName(String tempFileName) {
		String extFileName = "";
		int index = tempFileName.lastIndexOf(".");
		if(index > -1) {
			extFileName = tempFileName.substring(index + 1);
		}
		return extFileName;
	}

	private String nvl(Object input) {
		if (input == null) {
			return new String();
		}
		return input.toString();
	}

	protected TsmpDpApptJobDao getTsmpDpApptJobDao() {
		return this.tsmpDpApptJobDao;
	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return this.tsmpDpItemsCacheProxy;
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}

}
