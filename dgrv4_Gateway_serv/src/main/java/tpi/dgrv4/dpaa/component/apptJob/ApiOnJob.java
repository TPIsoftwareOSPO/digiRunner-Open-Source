package tpi.dgrv4.dpaa.component.apptJob;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.constant.TsmpDpSeqStoreKey;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.daoService.SeqStoreService;
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
public class ApiOnJob extends ApptJob {

	private TPILogger logger = TPILogger.tl;

	private TsmpDpReqOrdermDao tsmpDpReqOrdermDao;
	private TsmpDpReqOrderd2dDao tsmpDpReqOrderd2dDao;
	private TsmpDpReqOrderd2Dao tsmpDpReqOrderd2Dao;
	private TsmpApiDao tsmpApiDao;
	private TsmpApiExtDao tsmpApiExtDao;
	private FileHelper fileHelper;
	private TsmpDpApiThemeDao tsmpDpApiThemeDao;
	private TsmpDpFileDao tsmpDpFileDao;
	private SeqStoreService seqStoreService;

	@Autowired
	public ApiOnJob(TsmpDpApptJob tsmpDpApptJob, TsmpDpReqOrdermDao tsmpDpReqOrdermDao,
			TsmpDpReqOrderd2dDao tsmpDpReqOrderd2dDao, TsmpDpReqOrderd2Dao tsmpDpReqOrderd2Dao, TsmpApiDao tsmpApiDao,
			TsmpApiExtDao tsmpApiExtDao, FileHelper fileHelper, TsmpDpApiThemeDao tsmpDpApiThemeDao,
			TsmpDpFileDao tsmpDpFileDao, SeqStoreService seqStoreService, ApptJobDispatcher apptJobDispatcher,
			TsmpDpApptJobDao tsmpDpApptJobDao) {
		super(tsmpDpApptJob, TPILogger.tl, apptJobDispatcher, tsmpDpApptJobDao);
		this.tsmpDpReqOrdermDao = tsmpDpReqOrdermDao;
		this.tsmpDpReqOrderd2dDao = tsmpDpReqOrderd2dDao;
		this.tsmpDpReqOrderd2Dao = tsmpDpReqOrderd2Dao;
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpApiExtDao = tsmpApiExtDao;
		this.fileHelper = fileHelper;
		this.tsmpDpApiThemeDao = tsmpDpApiThemeDao;
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.seqStoreService = seqStoreService;
	}

	@Override
	public String runApptJob() throws Exception {
		String apiOn_reqOrderNo = getTsmpDpApptJob().getInParams();
		if (StringUtils.isEmpty(apiOn_reqOrderNo)) {
			throw new Exception("未輸入單號");
		}
		
		TsmpDpReqOrderm m = getTsmpDpReqOrdermDao().findFirstByReqOrderNo(apiOn_reqOrderNo);
		if (m == null) {
			throw new Exception("查無工作單: " + apiOn_reqOrderNo);
		}
		
		Long apiOn_reqOrdermId = m.getReqOrdermId();
		List<TsmpDpReqOrderd2> d2List = getTsmpDpReqOrderd2Dao().findByRefReqOrdermId(apiOn_reqOrdermId);
		if (d2List == null || d2List.isEmpty()) {
			throw new Exception("申請單明細空白");
		}
		
//		step("準備上架" + d2List.size() + "支API");
		step("PREP_API_ON");//準備上架API
		
		return doApiOn(m, d2List);
	}

	public String doApiOn(TsmpDpReqOrderm m, List<TsmpDpReqOrderd2> d2List) {
		int successCnt = 0;

		Long reqOrdermId = m.getReqOrdermId();
		String userName = m.getCreateUser();

		TsmpDpReqOrderd2 d2 = null;
		Long apiExtId = null;
		for(int i = 0; i < d2List.size(); i++) {
			d2 = d2List.get(i);
			
			try {
				// 更新API擴充檔(上下架Flag)
				apiExtId = updateApiExt(d2, reqOrdermId, userName);
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

	public Long updateApiExt(TsmpDpReqOrderd2 d2, Long reqOrdermId, String username) throws Exception {
		TsmpApi api = getApi(d2.getApiUid());
		if (api == null) {
			throw new Exception("找不到API: " + d2.getApiUid());
		}
		
		TsmpApiExt apiExt = getApiExt(api);
		if (apiExt == null) {
			// 沒有擴增檔就要新增一個
			apiExt = new TsmpApiExt();
			apiExt.setApiKey(api.getApiKey());
			apiExt.setModuleName(api.getModuleName());
			apiExt.setRefOrdermId(reqOrdermId);
			Long apiExtId = getSeqStoreService().nextSequence(TsmpDpSeqStoreKey.TSMP_API_EXT, 2000000000L, 1L);
			apiExt.setApiExtId(apiExtId);
			apiExt.setCreateUser(username);
		} else {
			apiExt.setUpdateDateTime(DateTimeUtil.now());
			apiExt.setUpdateUser(username);
		}
		apiExt.setDpStatus("1");
		apiExt.setDpStuDateTime(DateTimeUtil.now());
		apiExt = getTsmpApiExtDao().save(apiExt);
		
		// 更新API的開放狀態
		api.setPublicFlag(d2.getPublicFlag());
		api.setUpdateTime(DateTimeUtil.now());
		api.setUpdateUser(username);
		api = getTsmpApiDao().save(api);

		return apiExt.getApiExtId();
	}

	private TsmpApi getApi(String apiUid) {
		List<TsmpApi> apis = getTsmpApiDao().findByApiUid(apiUid);
		if (apis != null && !apis.isEmpty()) {
			return apis.get(0);
		}
		return null;
	}

	private TsmpApiExt getApiExt(TsmpApi api) {
		TsmpApiExtId id = new TsmpApiExtId(api.getApiKey(), api.getModuleName());
		Optional<TsmpApiExt> opt_ext = getTsmpApiExtDao().findById(id);
		return opt_ext.orElse(null);
	}

	public void insertApiTheme(TsmpDpReqOrderd2 d2, String userName) {
		// 刪除原有的對應關係
		String apiUid = d2.getApiUid();
		getTsmpDpApiThemeDao().deleteByRefApiUid(apiUid);
		
		Long d2Id = d2.getReqOrderd2Id();
		List<TsmpDpReqOrderd2d> d2dList = getTsmpDpReqOrderd2dDao().findByReqOrderd2Id(d2Id);
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
			getTsmpDpApiThemeDao().save(apiTheme);
		});
	}

	public void copyApiAttachment(Long d2Id, Long apiExtId, String userName) throws IOException, SQLException {
		// 刪除原有的附件
		List<TsmpDpFile> oldDpFiles = getTsmpDpFileDao().findByRefFileCateCodeAndRefId( //
				TsmpDpFileType.API_ATTACHMENT.value(), apiExtId);
		oldDpFiles.forEach((dpFile) -> {
			try {
				if("Y".equals(dpFile.getIsBlob()) && dpFile.getBlobData() != null) {
					getTsmpDpFileDao().delete(dpFile);
				}else {
					this.fileHelper.remove01(dpFile.getFilePath(), dpFile.getFileName(), (filename) -> {
						getTsmpDpFileDao().delete(dpFile);
					});
				}
				
			} catch (Exception e) {
				logger.debug("" + e);
			}
		});

		List<TsmpDpFile> apiOn_newDpFiles = getTsmpDpFileDao().findByRefFileCateCodeAndRefId( //
				TsmpDpFileType.D2_ATTACHMENT.value(), d2Id);
		if (apiOn_newDpFiles == null || apiOn_newDpFiles.isEmpty()) {
			return;
		}
		
		for(TsmpDpFile apiOn_dpFile : apiOn_newDpFiles) {
			byte[] fileContent = null;
			if("Y".equals(apiOn_dpFile.getIsBlob()) && apiOn_dpFile.getBlobData() != null) {
				fileContent = apiOn_dpFile.getBlobData();
			}else {
				fileContent = getFileHelper().download01(apiOn_dpFile.getFilePath(), apiOn_dpFile.getFileName());
			}
							
			if (fileContent != null) {
				TsmpDpFile newDpFile = getFileHelper().upload(userName, TsmpDpFileType.API_ATTACHMENT, apiExtId, apiOn_dpFile.getFileName(), fileContent, "N");
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
		return this.tsmpDpReqOrdermDao;
	}

	protected TsmpDpReqOrderd2Dao getTsmpDpReqOrderd2Dao() {
		return this.tsmpDpReqOrderd2Dao;
	}

	protected TsmpDpReqOrderd2dDao getTsmpDpReqOrderd2dDao() {
		return this.tsmpDpReqOrderd2dDao;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}

	protected TsmpApiExtDao getTsmpApiExtDao() {
		return this.tsmpApiExtDao;
	}

	protected TsmpDpApiThemeDao getTsmpDpApiThemeDao() {
		return this.tsmpDpApiThemeDao;
	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}

	protected SeqStoreService getSeqStoreService() {
		return this.seqStoreService;
	}

}
