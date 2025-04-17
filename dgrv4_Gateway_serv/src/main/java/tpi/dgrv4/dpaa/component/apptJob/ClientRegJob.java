package tpi.dgrv4.dpaa.component.apptJob;

import java.sql.SQLException;
import java.util.List;

import javax.sql.rowset.serial.SerialException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.constant.TsmpDpRegStatus;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.DgrAuditLogService;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.entity.TsmpDpClientext;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderd3;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderm;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.entity.repository.TsmpDpClientextDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrderd3Dao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdermDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.component.job.appt.ApptJob;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

/**
 * <b>排程工作</b><br>
 * 簽核類型: 用戶註冊<br>
 * 工作說明: 最後一關審核同意時, 要發送apptJob更新tsmp_dp_clientext.reg_status = '2' (放行)
 * @author Kim
 *
 */
@SuppressWarnings("serial")
public class ClientRegJob extends ApptJob {

	private TsmpDpReqOrdermDao tsmpDpReqOrdermDao;
	private TsmpDpReqOrderd3Dao tsmpDpReqOrderd3Dao;
	private TsmpDpClientextDao tsmpDpClientextDao;
	private TsmpDpFileDao tsmpDpFileDao;
	private FileHelper fileHelper;
	private DgrAuditLogService dgrAuditLogService;

	@Autowired
	public ClientRegJob(TsmpDpApptJob tsmpDpApptJob, TsmpDpReqOrdermDao tsmpDpReqOrdermDao,
			TsmpDpReqOrderd3Dao tsmpDpReqOrderd3Dao, TsmpDpClientextDao tsmpDpClientextDao, TsmpDpFileDao tsmpDpFileDao,
			FileHelper fileHelper, DgrAuditLogService dgrAuditLogService, ApptJobDispatcher apptJobDispatcher,
			TsmpDpApptJobDao tsmpDpApptJobDao) {
		super(tsmpDpApptJob, TPILogger.tl, apptJobDispatcher, tsmpDpApptJobDao);
		this.tsmpDpReqOrdermDao = tsmpDpReqOrdermDao;
		this.tsmpDpReqOrderd3Dao = tsmpDpReqOrderd3Dao;
		this.tsmpDpClientextDao = tsmpDpClientextDao;
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.fileHelper = fileHelper;
		this.dgrAuditLogService = dgrAuditLogService;
	}

	@Override
	public String runApptJob() throws Exception {
		
		String cr_reqOrderNo = getTsmpDpApptJob().getInParams();
		if (StringUtils.isEmpty(cr_reqOrderNo)) {
			throw new Exception("未輸入單號");
		}
		
		TsmpDpReqOrderm cr_m = getTsmpDpReqOrdermDao().findFirstByReqOrderNo(cr_reqOrderNo);
		if (cr_m == null) {
			throw new Exception("查無工作單: " + cr_reqOrderNo);
		}
		
		Long reqOrdermId = cr_m.getReqOrdermId();
		TsmpDpReqOrderd3 d3 = getTsmpDpReqOrderd3Dao().findFirstByRefReqOrdermId(reqOrdermId);
		if (d3 == null) {
			throw new Exception("申請單明細空白");
		}
		
//		step("準備更新用戶註冊狀態: " + d3.getClientId());
		step("PREP_CLIENT_REG");
		
		
		
		TsmpDpClientext ext = getTsmpDpClientextDao().findById(d3.getClientId()).orElse(null);
		if (ext == null) {
			throw new Exception("找不到用戶延伸檔: " + d3.getClientId());
		}
		
		InnerInvokeParam iip = InnerInvokeParam.getInstance(new HttpHeaders(), null, new TsmpAuthorization());
		
		//寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_CLIENT.value());
		
		String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, ext); //舊資料統一轉成 String
		
		ext.setRegStatus(TsmpDpRegStatus.PASS.value());
		ext.setUpdateDateTime(DateTimeUtil.now());
		ext.setUpdateUser("SYS");
		ext = getTsmpDpClientextDao().save(ext);
		
		//寫入 Audit Log D
		lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogD(iip, lineNumber,
				TsmpDpClientext.class.getSimpleName(), TableAct.U.value(), oldRowStr, ext);
		
		// 把申請單的附件複製到用戶註冊附件(把 TSMP_DP_REQ_ORDERM/... 底下的檔案複製到 MEMBER_APPLY/... 底下)
		copyAttachmentToMemberApply(reqOrdermId, ext.getClientSeqId());
		
		/* 改用訊息設定檔
		return "success, clientSeqId=" + ext.getClientSeqId();
		*/
		return "SUCCESS";
	}

	private void copyAttachmentToMemberApply(Long reqOrdermId, Long clientSeqId) throws Exception {
		List<TsmpDpFile> attachmentList = getTsmpDpFileDao().findByRefFileCateCodeAndRefId( //
			TsmpDpFileType.M_ATTACHMENT.value(), reqOrdermId);
		if (attachmentList == null || attachmentList.isEmpty()) {
			return;
		}
		
		String fileName = null;
		for (TsmpDpFile dpFile : attachmentList) {
			fileName = dpFile.getFileName();
			downloadAndUpload(reqOrdermId, clientSeqId, fileName);
			/*Path memberApplyFile = downloadAndUpload(reqOrdermId, clientSeqId, fileName);
			if (memberApplyFile != null) {
				TsmpDpFile newDpFile = new TsmpDpFile();
				newDpFile.setFileName(memberApplyFile.getFileName().toString());
				newDpFile.setFilePath(FileHelper.getTsmpDpFilePath(TsmpDpFileType.MEMBER_APPLY, clientSeqId));
				newDpFile.setRefFileCateCode(TsmpDpFileType.MEMBER_APPLY.value());
				newDpFile.setRefId(clientSeqId);
				newDpFile.setCreateUser("SYS");
				getTsmpDpFileDao().save(newDpFile);
			}*/
		}
	}

	private TsmpDpFile downloadAndUpload(Long reqOrdermId, Long clientSeqId, String fileName) throws SerialException, SQLException  {
		byte[] fileContent = getFileHelper().downloadByTsmpDpFile(TsmpDpFileType.M_ATTACHMENT, reqOrdermId, fileName);
		if (fileContent == null) {
			return null;
		}
		return getFileHelper().upload("SYS", TsmpDpFileType.MEMBER_APPLY, clientSeqId, fileName, fileContent, "N");
	}
	/*private Path downloadAndUpload(Long reqOrdermId, Long clientSeqId, String fileName) throws IOException {
		byte[] fileContent = getFileHelper().download(TsmpDpFileType.M_ATTACHMENT, reqOrdermId, fileName);
		if (fileContent == null) {
			return null;
		}
		//return getFileHelper().upload(TsmpDpFileType.MEMBER_APPLY, clientSeqId, fileName, fileContent);
	}*/

	protected TsmpDpReqOrdermDao getTsmpDpReqOrdermDao() {
		return this.tsmpDpReqOrdermDao;
	}

	protected TsmpDpReqOrderd3Dao getTsmpDpReqOrderd3Dao() {
		return this.tsmpDpReqOrderd3Dao;
	}

	protected TsmpDpClientextDao getTsmpDpClientextDao() {
		return this.tsmpDpClientextDao;
	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}

	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}
	
	

}
