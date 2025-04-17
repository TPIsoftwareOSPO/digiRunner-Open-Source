package tpi.dgrv4.dpaa.component.apptJob;

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
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderd2;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderm;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpApiExtDao;
import tpi.dgrv4.entity.repository.TsmpDpApiThemeDao;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrderd2Dao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdermDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.component.job.appt.ApptJob;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;

@SuppressWarnings("serial")
public class ApiOffJob extends ApptJob {

	private TPILogger logger = TPILogger.tl;

	private TsmpDpReqOrdermDao tsmpDpReqOrdermDao;
	private TsmpDpReqOrderd2Dao tsmpDpReqOrderd2Dao;
	private TsmpApiDao tsmpApiDao;
	private TsmpApiExtDao tsmpApiExtDao;
	private TsmpDpFileDao tsmpDpFileDao;
	private TsmpDpApiThemeDao tsmpDpApiThemeDao;
	private FileHelper fileHelper;
	private SeqStoreService seqStoreService;

	@Autowired
	public ApiOffJob(TsmpDpApptJob tsmpDpApptJob, TsmpDpReqOrdermDao tsmpDpReqOrdermDao,
			TsmpDpReqOrderd2Dao tsmpDpReqOrderd2Dao, TsmpApiDao tsmpApiDao, TsmpApiExtDao tsmpApiExtDao,
			TsmpDpFileDao tsmpDpFileDao, TsmpDpApiThemeDao tsmpDpApiThemeDao, FileHelper fileHelper,
			SeqStoreService seqStoreService, ApptJobDispatcher apptJobDispatcher, TsmpDpApptJobDao tsmpDpApptJobDao) {
		super(tsmpDpApptJob, TPILogger.tl, apptJobDispatcher, tsmpDpApptJobDao);
		this.tsmpDpReqOrdermDao = tsmpDpReqOrdermDao;
		this.tsmpDpReqOrderd2Dao = tsmpDpReqOrderd2Dao;
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpApiExtDao = tsmpApiExtDao;
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.tsmpDpApiThemeDao = tsmpDpApiThemeDao;
		this.fileHelper = fileHelper;
		this.seqStoreService = seqStoreService;
	}

	@Override
	public String runApptJob() throws Exception {
		String reqOrderNo = getTsmpDpApptJob().getInParams();
		if (!StringUtils.hasLength(reqOrderNo)) {
			throw new Exception("未輸入單號");
		}
		
		TsmpDpReqOrderm m = getTsmpDpReqOrdermDao().findFirstByReqOrderNo(reqOrderNo);
		if (m == null) {
			throw new Exception("查無工作單: " + reqOrderNo);
		}
		
		Long reqOrdermId = m.getReqOrdermId();
		List<TsmpDpReqOrderd2> d2List = this.getTsmpDpReqOrderd2Dao().findByRefReqOrdermId(reqOrdermId);
		if (d2List == null || d2List.isEmpty()) {
			throw new Exception("申請單明細空白");
		}
		
//		step("準備下架" + d2List.size() + "支API");
		step("PREP_API_OFF");//準備下架API
		return doApiOff(m, d2List);
	}

	public String doApiOff(TsmpDpReqOrderm m, List<TsmpDpReqOrderd2> d2List) {
		int successCnt = 0;

		Long reqOrdermId = m.getReqOrdermId();
		String userName = m.getCreateUser();

		TsmpDpReqOrderd2 d2 = null;
		Long apiExtId = null;
		for(int i = 0; i < d2List.size(); i++) {
			d2 = d2List.get(i);
			
			try {
				// 更新API擴充檔(上下架Flag)
				apiExtId = updateApiExt(d2.getApiUid(), reqOrdermId, userName);
				// 清除API所對應的主題
				getTsmpDpApiThemeDao().deleteByRefApiUid(d2.getApiUid());
				// 清除原有附件
				List<TsmpDpFile> oldDpFiles = this.getTsmpDpFileDao().findByRefFileCateCodeAndRefId( //
						TsmpDpFileType.API_ATTACHMENT.value(), apiExtId);
				if (oldDpFiles != null) {
					oldDpFiles.forEach((apiOff_dpFile) -> {
						try {
							if("Y".equals(apiOff_dpFile.getIsBlob()) && apiOff_dpFile.getBlobData() != null) {
								this.getTsmpDpFileDao().delete(apiOff_dpFile);
							}else {
								this.getFileHelper().remove01(apiOff_dpFile.getFilePath(), apiOff_dpFile.getFileName(), (filename) -> {
									this.getTsmpDpFileDao().delete(apiOff_dpFile);
								});
							}
						} catch (Exception e) {
							logger.debug("" + e);
						}
					});
				}
				
				successCnt++;
			} catch (Exception e) {
				logger.debug("" + e);
			} finally {
				step((i + 1) + "/" + d2List.size());
			}
		}
		
		return successCnt + "/" + d2List.size();
	}

	public Long updateApiExt(String apiUid, Long reqOrdermId, String username) throws Exception {
		TsmpApi api = getApi(apiUid);
		if (api == null) {
			throw new Exception("找不到API: " + apiUid);
		}
		
		TsmpApiExt apiExt = getApiExt(api);
		if (apiExt == null) {
			// 沒有擴增檔就要新增一個
			apiExt = new TsmpApiExt();
			apiExt.setApiKey(api.getApiKey());
			apiExt.setModuleName(api.getModuleName());
			apiExt.setRefOrdermId(reqOrdermId);
			Long apiExtId = this.getSeqStoreService().nextSequence(TsmpDpSeqStoreKey.TSMP_API_EXT, 10001L, 1L);
			apiExt.setApiExtId(apiExtId);
			apiExt.setCreateUser(username);
		} else {
			apiExt.setUpdateDateTime(DateTimeUtil.now());
			apiExt.setUpdateUser(username);
		}
		apiExt.setDpStatus("0");
		apiExt.setDpStuDateTime(DateTimeUtil.now());
		apiExt = this.getTsmpApiExtDao().save(apiExt);
		return apiExt.getApiExtId();
	}

	private TsmpApi getApi(String apiUid) {
		List<TsmpApi> apiOff_apis = this.getTsmpApiDao().findByApiUid(apiUid);
		if (apiOff_apis != null && !apiOff_apis.isEmpty()) {
			return apiOff_apis.get(0);
		}
		return null;
	}

	private TsmpApiExt getApiExt(TsmpApi api) {
		TsmpApiExtId id = new TsmpApiExtId(api.getApiKey(), api.getModuleName());
		Optional<TsmpApiExt> opt_ext = this.getTsmpApiExtDao().findById(id);
		return opt_ext.orElse(null);
	}
	
	protected TsmpDpReqOrdermDao getTsmpDpReqOrdermDao(){
		return tsmpDpReqOrdermDao;
	}
	
	protected TsmpDpReqOrderd2Dao getTsmpDpReqOrderd2Dao(){
		return tsmpDpReqOrderd2Dao;
	}
	
	protected TsmpApiExtDao getTsmpApiExtDao(){
		return tsmpApiExtDao;
	}
	
	protected TsmpDpApiThemeDao getTsmpDpApiThemeDao(){
		return tsmpDpApiThemeDao;
	}
	
	protected TsmpApiDao getTsmpApiDao(){
		return tsmpApiDao;
	}
	
	protected TsmpDpFileDao getTsmpDpFileDao(){
		return tsmpDpFileDao;
	}
	
	protected FileHelper getFileHelper(){
		return fileHelper;
	}
	
	protected SeqStoreService getSeqStoreService(){
		return seqStoreService;
	}
}
