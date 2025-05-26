package tpi.dgrv4.dpaa.component.apptJob;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.entity.jpql.TsmpApiExt;
import tpi.dgrv4.entity.entity.jpql.TsmpApiExtId;
import tpi.dgrv4.entity.entity.jpql.TsmpDpApiTheme;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderd2;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderd2d;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderm;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpApiExtDao;
import tpi.dgrv4.entity.repository.TsmpDpApiThemeDao;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrderd2Dao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrderd2dDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdermDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.component.job.appt.ApptJob;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;

@SuppressWarnings("serial")
public class ApiOnUpdateJob extends ApptJob {

	private TPILogger logger = TPILogger.tl;

	private TsmpDpReqOrdermDao tsmpDpReqOrdermDao;
	private TsmpDpReqOrderd2dDao tsmpDpReqOrderd2dDao;
	private TsmpApiDao tsmpApiDao;
	private TsmpApiExtDao tsmpApiExtDao;
	private TsmpDpReqOrderd2Dao tsmpDpReqOrderd2Dao;
	private TsmpDpApiThemeDao tsmpDpApiThemeDao;
	private TsmpDpFileDao tsmpDpFileDao;
	private FileHelper fileHelper;

	@Autowired
	public ApiOnUpdateJob(TsmpDpApptJob tsmpDpApptJob, TsmpDpReqOrdermDao tsmpDpReqOrdermDao,
			TsmpDpReqOrderd2dDao tsmpDpReqOrderd2dDao, TsmpApiDao tsmpApiDao, TsmpApiExtDao tsmpApiExtDao,
			TsmpDpReqOrderd2Dao tsmpDpReqOrderd2Dao, TsmpDpApiThemeDao tsmpDpApiThemeDao, TsmpDpFileDao tsmpDpFileDao,
			FileHelper fileHelper, ApptJobDispatcher apptJobDispatcher, TsmpDpApptJobDao tsmpDpApptJobDao) {
		super(tsmpDpApptJob, TPILogger.tl, apptJobDispatcher, tsmpDpApptJobDao);
		this.tsmpDpReqOrdermDao = tsmpDpReqOrdermDao;
		this.tsmpDpReqOrderd2dDao = tsmpDpReqOrderd2dDao;
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpApiExtDao = tsmpApiExtDao;
		this.tsmpDpReqOrderd2Dao = tsmpDpReqOrderd2Dao;
		this.tsmpDpApiThemeDao = tsmpDpApiThemeDao;
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.fileHelper = fileHelper;
	}

	@Override
	public String runApptJob() throws Exception {
		String apiOnUpdate_reqOrderNo = getTsmpDpApptJob().getInParams();
		if (StringUtils.isEmpty(apiOnUpdate_reqOrderNo)) {
			throw new Exception("未輸入單號");
		}
		
		TsmpDpReqOrderm m = getTsmpDpReqOrdermDao().findFirstByReqOrderNo(apiOnUpdate_reqOrderNo);
		if (m == null) {
			throw new Exception("查無工作單: " + apiOnUpdate_reqOrderNo);
		}
		
		Long reqOrdermId = m.getReqOrdermId();
		List<TsmpDpReqOrderd2> apiOnUpdate_d2List = this.getTsmpDpReqOrderd2Dao().findByRefReqOrdermId(reqOrdermId);
		if (apiOnUpdate_d2List == null || apiOnUpdate_d2List.isEmpty()) {
			throw new Exception("申請單明細空白");
		}
		
//		step("準備異動" + d2List.size() + "支API");
		step("PREP_API_UPDATE");//準備異動API
		
		return doApiOnUpdate(m.getCreateUser(), apiOnUpdate_d2List);
	}

	public String doApiOnUpdate(String userName, List<TsmpDpReqOrderd2> d2List) {
		int successCnt = 0;

		TsmpDpReqOrderd2 d2 = null;
		Long apiExtId = null;
		for(int i = 0; i < d2List.size(); i++) {
			d2 = d2List.get(i);
			
			try {
				// 檢查API擴充檔(異動日期)
				apiExtId = checkApiExt(d2, userName);
				// 更新API主題對應檔
				insertApiTheme(d2, userName);
				// 複製API附件
				copyApiAttachment(d2.getReqOrderd2Id(), apiExtId, userName);
				
				successCnt++;
			} catch (Exception e) {
				logger.debug("" + e);
			} finally {
				step((i + 1) + "/" + d2List.size());
			}
		}
		
		return successCnt + "/" + d2List.size();
	}

	public Long checkApiExt(TsmpDpReqOrderd2 d2, String userName) throws Exception {
		TsmpApi api = getApi(d2.getApiUid());
		if (api == null) {
			throw new Exception("找不到API: " + d2.getApiUid());
		}
		
		TsmpApiExt apiExt = getApiExt(api);
		if (apiExt == null || "0".equals(apiExt.getDpStatus())) {
			throw new Exception("無法異動已下架的API");
		}
		
		apiExt.setDpStuDateTime(DateTimeUtil.now());	// 紀錄異動日期
		apiExt.setUpdateDateTime(DateTimeUtil.now());
		apiExt.setUpdateUser(userName);
		apiExt = this.getTsmpApiExtDao().save(apiExt);
		
		// 更新API的開放狀態
		api.setPublicFlag(d2.getPublicFlag());
		api.setUpdateTime(DateTimeUtil.now());
		api.setUpdateUser(userName);
		api = this.getTsmpApiDao().save(api);
		
		return apiExt.getApiExtId();
	}

	private TsmpApi getApi(String apiUid) {
		List<TsmpApi> apiOnUpdate_apis = this.getTsmpApiDao().findByApiUid(apiUid);
		if (apiOnUpdate_apis != null && !apiOnUpdate_apis.isEmpty()) {
			return apiOnUpdate_apis.get(0);
		}
		return null;
	}

	private TsmpApiExt getApiExt(TsmpApi api) {
		TsmpApiExtId apiOnUpdate_id = new TsmpApiExtId(api.getApiKey(), api.getModuleName());
		Optional<TsmpApiExt> opt_ext = this.getTsmpApiExtDao().findById(apiOnUpdate_id);
		return opt_ext.orElse(null);
	}

	public void insertApiTheme(TsmpDpReqOrderd2 d2, String userName) {
		// 刪除原有的對應關係
		String apiUid = d2.getApiUid();
		getTsmpDpApiThemeDao().deleteByRefApiUid(apiUid);
		
		Long d2Id = d2.getReqOrderd2Id();
		List<TsmpDpReqOrderd2d> d2dList = this.getTsmpDpReqOrderd2dDao().findByReqOrderd2Id(d2Id);
		if (d2dList == null || d2dList.isEmpty()) {
			// throw new Exception("申請單明細錯誤(d2d)");
			return;
		}

		d2dList.forEach((d2d) -> {
			TsmpDpApiTheme apiTheme = new TsmpDpApiTheme();
			apiTheme.setRefApiThemeId(d2d.getRefThemeId());
			apiTheme.setRefApiUid(d2d.getApiUid());
			apiTheme.setCreateDateTime(DateTimeUtil.now());
			apiTheme.setCreateUser(userName);
			this.getTsmpDpApiThemeDao().save(apiTheme);
		});
	}

	public void copyApiAttachment(Long d2Id, Long apiExtId, String userName) throws IOException, SQLException {
		// 刪除原有的附件
		List<TsmpDpFile> oldDpFiles = this.getTsmpDpFileDao().findByRefFileCateCodeAndRefId( //
				TsmpDpFileType.API_ATTACHMENT.value(), apiExtId);
		oldDpFiles.forEach((apiOnUpdate_dpFile) -> {
			try {
				if("Y".equals(apiOnUpdate_dpFile.getIsBlob()) && apiOnUpdate_dpFile.getBlobData() != null) {
					this.getTsmpDpFileDao().delete(apiOnUpdate_dpFile);
				}else {
					this.getFileHelper().remove01(apiOnUpdate_dpFile.getFilePath(), apiOnUpdate_dpFile.getFileName(), (filename) -> {
						this.getTsmpDpFileDao().delete(apiOnUpdate_dpFile);
					});
				}
			} catch (Exception e) {
				logger.debug("" + e);
			}
		});

		List<TsmpDpFile> newDpFiles = this.getTsmpDpFileDao().findByRefFileCateCodeAndRefId( //
				TsmpDpFileType.D2_ATTACHMENT.value(), d2Id);
		if (newDpFiles == null || newDpFiles.isEmpty()) {
			return;
		}
		
		for(TsmpDpFile apiOnUpdate_dpFile : newDpFiles) {
			byte[] fileContent = null;
			if("Y".equals(apiOnUpdate_dpFile.getIsBlob()) && apiOnUpdate_dpFile.getBlobData() != null) {
				fileContent = apiOnUpdate_dpFile.getBlobData();
			}else {
				fileContent = getFileHelper().download01(apiOnUpdate_dpFile.getFilePath(), apiOnUpdate_dpFile.getFileName());
			}
			
			if (fileContent != null) {
				TsmpDpFile newDpFile = getFileHelper().upload(userName, TsmpDpFileType.API_ATTACHMENT, apiExtId, apiOnUpdate_dpFile.getFileName(), fileContent, "N");
				/*Path uploadFile = fileHelper.upload(TsmpDpFileType.API_ATTACHMENT, apiExtId //
						, dpFile.getFileName(), fileContent);
				if (uploadFile != null) {
					TsmpDpFile newDpFile = new TsmpDpFile();
					newDpFile.setFileName(uploadFile.getFileName().toString());
					newDpFile.setFilePath(FileHelper.getTsmpDpFilePath(TsmpDpFileType.API_ATTACHMENT, apiExtId));
					newDpFile.setRefFileCateCode(TsmpDpFileType.API_ATTACHMENT.value());
					newDpFile.setRefId(apiExtId);
					newDpFile.setCreateUser(userName);
					this.tsmpDpFileDao.save(newDpFile);
				}*/
			}
		}
	}
	
	protected TsmpDpReqOrdermDao getTsmpDpReqOrdermDao() {
		return tsmpDpReqOrdermDao;
	}
	
	
	protected TsmpDpReqOrderd2Dao getTsmpDpReqOrderd2Dao() {
		return tsmpDpReqOrderd2Dao;
	}
	
	protected TsmpDpReqOrderd2dDao getTsmpDpReqOrderd2dDao() {
		return tsmpDpReqOrderd2dDao;
	}
	
	protected TsmpApiDao getTsmpApiDao() {
		return tsmpApiDao;
	}
	
	protected TsmpDpApiThemeDao getTsmpDpApiThemeDao() {
		return tsmpDpApiThemeDao;
	}
	
	protected TsmpDpFileDao getTsmpDpFileDao() {
		return tsmpDpFileDao;
	}
	
	protected TsmpApiExtDao getTsmpApiExtDao() {
		return tsmpApiExtDao;
	}
	
	protected FileHelper getFileHelper() {
		return fileHelper;
	}
}
