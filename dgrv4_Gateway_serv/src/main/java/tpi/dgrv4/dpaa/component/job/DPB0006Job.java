package tpi.dgrv4.dpaa.component.job;

import org.springframework.http.HttpHeaders;
import tpi.dgrv4.common.constant.*;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.DgrAuditLogService;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.*;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderd3;
import tpi.dgrv4.entity.repository.*;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.component.job.DeferrableJob;
import tpi.dgrv4.gateway.component.job.JobHelperImpl;
import tpi.dgrv4.gateway.component.job.JobManager;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 總共刪除以下Tables:<br/>
 * tsmpGroup<br/>
 * tsmpClient<br/>
 * tsmpClientGroup<br/>
 * tsmpGroupApi<br/>
 * oauthClientDetail<br/>
 * tsmpDpFile(包含硬碟檔案)<br/>
 * tsmpDpClientext
 * 20200109 加入以下邏輯:
 * 	檢查是否存在tsmp_dp_clientext但沒有tsmp_client的資料,
 * 	若有則刪除tsmp_dp_clientext
 * @author Kim
 *
 */
@SuppressWarnings("serial")
public class DPB0006Job extends DeferrableJob {

	private TPILogger logger = TPILogger.tl;

	private TsmpClientDao tsmpClientDao;
	private TsmpGroupDao tsmpGroupDao;
	private TsmpClientGroupDao tsmpClientGroupDao;
	private TsmpGroupApiDao tsmpGroupApiDao;
	private TsmpDpClientextDao tsmpDpClientextDao;
	private OauthClientDetailsDao oauthClientDetailsDao;
	private TsmpDpFileDao tsmpDpFileDao;
	private FileHelper fileHelper;
	private TsmpDpReqOrderd3Dao tsmpDpReqOrderd3Dao;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private DgrAuditLogService dgrAuditLogService;
	
	public DPB0006Job(TsmpClientDao tsmpClientDao, TsmpGroupDao tsmpGroupDao, TsmpClientGroupDao tsmpClientGroupDao,
			TsmpGroupApiDao tsmpGroupApiDao, TsmpDpClientextDao tsmpDpClientextDao,
			OauthClientDetailsDao oauthClientDetailsDao, TsmpDpFileDao tsmpDpFileDao, FileHelper fileHelper,
			TsmpDpReqOrderd3Dao tsmpDpReqOrderd3Dao, TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy,
			DgrAuditLogService dgrAuditLogService) {
		super();
		this.tsmpClientDao = tsmpClientDao;
		this.tsmpGroupDao = tsmpGroupDao;
		this.tsmpClientGroupDao = tsmpClientGroupDao;
		this.tsmpGroupApiDao = tsmpGroupApiDao;
		this.tsmpDpClientextDao = tsmpDpClientextDao;
		this.oauthClientDetailsDao = oauthClientDetailsDao;
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.fileHelper = fileHelper;
		this.tsmpDpReqOrderd3Dao = tsmpDpReqOrderd3Dao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.dgrAuditLogService = dgrAuditLogService;
	}

	@Override
	public void runJob(JobHelperImpl jobHelper, JobManager jobManager) {
		try {
			this.logger.debug("--- Begin DPB0006Job ---");
			InnerInvokeParam iip = InnerInvokeParam.getInstance(new HttpHeaders(), null, new TsmpAuthorization());
			
			//寫入 Audit Log M
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.DELETE_CLIENT.value());
			
			// 找出不一致的會員資料
			List<TsmpDpClientext> inconsistentExts = getTsmpDpClientextDao().query_dpb0006Job_inconsistentExt();
			if (inconsistentExts != null && !inconsistentExts.isEmpty()) {
				this.logger.debug("Inconsistent client extension:");
				inconsistentExts.forEach((ext) -> {
					this.logger.debug(String.format("\t%d-%s", ext.getClientSeqId(), ext.getClientId()));
					getTsmpDpClientextDao().delete(ext);
				});
			}

			Integer expDay = getExpDay();
			// 找出註冊狀態為3=退回, 且更新時間為expDay 天前
			List<TsmpDpClientext> expiredExtList = getExpiredExpList(expDay);
			if (expiredExtList == null || expiredExtList.isEmpty()) {
				this.logger.debug("No expired client.");
				return;
			}
			this.logger.debug("Expired clients: " + expiredExtList.size());

			String clientId;
			Long clientSeqId;
			List<TsmpClientGroup> cgList;
			for(TsmpDpClientext ext : expiredExtList) {
				clientId = ext.getClientId();
				clientSeqId = ext.getClientSeqId();

				cgList = this.getTsmpClientGroupDao().findByClientId(clientId);
				if (cgList != null && !cgList.isEmpty()) {
					cgList.forEach((cg) -> {
						this.getTsmpClientGroupDao().delete(cg);
						this.getTsmpGroupApiDao().deleteByGroupId(cg.getGroupId());
						if (this.getTsmpGroupDao().existsById(cg.getGroupId())) {
							this.getTsmpGroupDao().deleteById(cg.getGroupId());
						}
					});
				}
				OauthClientDetails authVo = this.getOauthClientDetailsDao().findById(clientId).orElse(null);
				if(authVo != null) {
					String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, authVo); //舊資料統一轉成 String
					
					this.getOauthClientDetailsDao().delete(authVo);
					
					//寫入 Audit Log D
					lineNumber = StackTraceUtil.getLineNumber();
					getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
							OauthClientDetails.class.getSimpleName(), TableAct.D.value(), oldRowStr, null);
				}

				String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, ext); //舊資料統一轉成 String
				
				this.getTsmpDpClientextDao().delete(ext);
				
				//寫入 Audit Log D
				lineNumber = StackTraceUtil.getLineNumber();
				getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
						TsmpDpClientext.class.getSimpleName(), TableAct.D.value(), oldRowStr, null);
				
				TsmpClient clientVo = this.getTsmpClientDao().findById(clientId).orElse(null);
				if (clientVo != null) {
					oldRowStr = getDgrAuditLogService().writeValueAsString(iip, clientVo); //舊資料統一轉成 String
					
					this.getTsmpClientDao().delete(clientVo);
					
					//寫入 Audit Log D
					lineNumber = StackTraceUtil.getLineNumber();
					getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
							TsmpClient.class.getSimpleName(), TableAct.D.value(), oldRowStr, null);
				}
				deleteUploadedFile(clientId, clientSeqId);
			}
		} catch (Exception dpb0006_e) {
			logger.debug("" + dpb0006_e);
		} finally {
			this.logger.debug("--- Finish DPB0006Job ---");
		}
	}

	@Override
	public void replace(DeferrableJob source) {
		this.logger.trace(String.format("%s-%s is replaced.", this.getClass().getSimpleName(), this.getId()));
	}

	private Integer getExpDay() {
		TsmpDpItems dpb0006_vo = getTsmpDpItemsCacheProxy().findByItemNoAndSubitemNoAndLocale("HOUSEKEEPING", "short", LocaleType.EN_US);
		String expDay = null;
		try {
			expDay = dpb0006_vo.getParam1();
			return Integer.valueOf(expDay);
		} catch (Exception e) {
			this.logger.warn(String.format("Invalid setting: exp-day = %s, set to default 30.", expDay));
		}
		return Integer.valueOf(30);
	}

	private List<TsmpDpClientext> getExpiredExpList(Integer expDay){
		LocalDate ld = LocalDate.now();
		ld = ld.minusDays(expDay);
		Date expDate = Date.from(ld.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		
		Optional<String> opt = DateTimeUtil.dateTimeToString(expDate, DateTimeFormatEnum.西元年月日時分秒_2);
		if (opt.isPresent()) {
			this.logger.debug("expDate = " + opt.get());
		}
		
		return this.getTsmpDpClientextDao().query_dpb0006Job(expDate);
	}

	/**
	 * 被退回時, 附件的位置有分新、舊兩種情況:
	 * <ul>
	 * 	<li><b>舊</b>: 申請時就直接上傳到 /MEMBER_APPLY 底下, 所以刪只需要刪 /MEMBER_APPLY 底下的檔案</li>
	 * 	<li><b>新</b>: 申請時是當作簽核單的附件, 所以會上傳到 /TSMP_DP_REQ_ORDERM 底下, 直到簽核通過才會複製到 /MEMBER_APPLY,
	 * 所以被退回後應該檔案還是只有再 /TSMP_DP_REQ_ORDERM 底下</li>
	 * </ul>
	 * @param clientSeqId
	 * @throws Exception
	 */
	private void deleteUploadedFile(String clientId, Long clientSeqId) throws Exception {
		// 刪除用戶註冊附件
		deleteMemberApplyAttachment(clientSeqId);
		
		// 刪除申請單附件
		deleteD3Attachment(clientId);
	}

	private void deleteMemberApplyAttachment(Long clientSeqId) throws Exception {
		List<TsmpDpFile> fileList = this.getTsmpDpFileDao().findByRefFileCateCodeAndRefId( //
				TsmpDpFileType.MEMBER_APPLY.value(), clientSeqId);

		if (fileList == null || fileList.isEmpty()) {
			return;
		}

		for(TsmpDpFile file : fileList) {
			if("Y".equals(file.getIsBlob()) && file.getBlobData() != null) {
				this.getTsmpDpFileDao().delete(file);
			}else {
				this.getFileHelper().remove01(file.getFilePath(), file.getFileName(), (filename) -> {
					this.getTsmpDpFileDao().delete(file);
				});
			}
			
		}
	}

	private void deleteD3Attachment(String clientId) throws Exception {
		// 正常 d3List 應該只要有一筆
		List<TsmpDpReqOrderd3> d3List = this.getTsmpDpReqOrderd3Dao().findByClientId(clientId);
		if (d3List == null || d3List.isEmpty()) {
			return;
		}

		Long reqOrdermId = null;
		List<TsmpDpFile> dpFileList = null;
		for (TsmpDpReqOrderd3 d3 : d3List) {
			reqOrdermId = d3.getRefReqOrdermId();
			dpFileList = this.getTsmpDpFileDao().findByRefFileCateCodeAndRefId( //
				TsmpDpFileType.M_ATTACHMENT.value(), reqOrdermId);
			if (dpFileList == null || dpFileList.isEmpty()) {
				continue;
			}
			
			// 申請單中的每個附件都要刪除, 正常應該也只能有一筆
			for (TsmpDpFile dpFile : dpFileList) {
				if("Y".equals(dpFile.getIsBlob()) && dpFile.getBlobData() != null) {
					this.getTsmpDpFileDao().delete(dpFile);
				}else {
					this.getFileHelper().remove01(dpFile.getFilePath(), dpFile.getFileName(), (filename) -> {
						this.getTsmpDpFileDao().delete(dpFile);
					});
				}
			}
		}
		
	}
	
	protected TsmpClientDao getTsmpClientDao() {
		return tsmpClientDao;
	}
	
	protected TsmpGroupDao getTsmpGroupDao() {
		return tsmpGroupDao;
	}
	
	protected TsmpClientGroupDao getTsmpClientGroupDao() {
		return tsmpClientGroupDao;
	}
	
	protected TsmpGroupApiDao getTsmpGroupApiDao() {
		return tsmpGroupApiDao;
	}
	
	protected TsmpDpClientextDao getTsmpDpClientextDao() {
		return tsmpDpClientextDao;
	}
	
	protected OauthClientDetailsDao getOauthClientDetailsDao() {
		return oauthClientDetailsDao;
	}
	
	protected TsmpDpFileDao getTsmpDpFileDao() {
		return tsmpDpFileDao;
	}
	
	protected FileHelper getFileHelper() {
		return fileHelper;
	}
	
	protected TsmpDpReqOrderd3Dao getTsmpDpReqOrderd3Dao() {
		return tsmpDpReqOrderd3Dao;
	}
		
	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return tsmpDpItemsCacheProxy;
	}

	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}
	
	
 
}
