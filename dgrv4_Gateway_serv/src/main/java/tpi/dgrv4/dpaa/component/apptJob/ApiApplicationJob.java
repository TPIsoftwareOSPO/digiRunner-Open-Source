package tpi.dgrv4.dpaa.component.apptJob;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpApplyStatus;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.constant.TsmpSequenceName;
import tpi.dgrv4.entity.daoService.SeqStoreService;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpClientGroup;
import tpi.dgrv4.entity.entity.TsmpClientGroupId;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.entity.TsmpGroup;
import tpi.dgrv4.entity.entity.TsmpGroupApi;
import tpi.dgrv4.entity.entity.TsmpGroupApiId;
import tpi.dgrv4.entity.entity.jpql.TsmpDpApiAuth2;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderd1;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderm;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpClientGroupDao;
import tpi.dgrv4.entity.repository.TsmpDpApiAuth2Dao;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrderd1Dao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdermDao;
import tpi.dgrv4.entity.repository.TsmpGroupApiDao;
import tpi.dgrv4.entity.repository.TsmpGroupDao;
import tpi.dgrv4.gateway.component.job.appt.ApptJob;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * <b>排程工作</b><br>
 * 簽核類型: 用戶申請API<br>
 * 工作說明: 最後一關審核同意時, 要發送apptJob更新tsmp_dp_api_auth2.applyStatus = 'PASS'
 * @author Kim
 *
 */
@SuppressWarnings("serial")
public class ApiApplicationJob extends ApptJob {

	private TPILogger logger = TPILogger.tl;

	private TsmpDpReqOrdermDao tsmpDpReqOrdermDao;
	private TsmpDpReqOrderd1Dao tsmpDpReqOrderd1Dao;
	private TsmpDpApiAuth2Dao tsmpDpApiAuth2Dao;
	private TsmpClientDao tsmpClientDao;
	private TsmpGroupApiDao tsmpGroupApiDao;
	private TsmpApiDao tsmpApiDao;
	private TsmpClientGroupDao tsmpClientGroupDao;
	private TsmpGroupDao tsmpGroupDao;
	private SeqStoreService seqStoreService;
	
	@Autowired
	public ApiApplicationJob(TsmpDpApptJob tsmpDpApptJob, TsmpDpReqOrdermDao tsmpDpReqOrdermDao,
			TsmpDpReqOrderd1Dao tsmpDpReqOrderd1Dao, TsmpDpApiAuth2Dao tsmpDpApiAuth2Dao, TsmpClientDao tsmpClientDao,
			TsmpGroupApiDao tsmpGroupApiDao, TsmpApiDao tsmpApiDao, TsmpClientGroupDao tsmpClientGroupDao,
			TsmpGroupDao tsmpGroupDao, SeqStoreService seqStoreService, ApptJobDispatcher apptJobDispatcher, TsmpDpApptJobDao tsmpDpApptJobDao) {
		super(tsmpDpApptJob, TPILogger.tl, apptJobDispatcher, tsmpDpApptJobDao);
		this.tsmpDpReqOrdermDao = tsmpDpReqOrdermDao;
		this.tsmpDpReqOrderd1Dao = tsmpDpReqOrderd1Dao;
		this.tsmpDpApiAuth2Dao = tsmpDpApiAuth2Dao;
		this.tsmpClientDao = tsmpClientDao;
		this.tsmpGroupApiDao = tsmpGroupApiDao;
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpClientGroupDao = tsmpClientGroupDao;
		this.tsmpGroupDao = tsmpGroupDao;
		this.seqStoreService = seqStoreService;
	}

	@Override
	public String runApptJob() throws Exception {
		String apiApp_reqOrderNo = getTsmpDpApptJob().getInParams();
		if (StringUtils.isEmpty(apiApp_reqOrderNo)) {
			throw new Exception("未輸入單號");
		}
		
		TsmpDpReqOrderm m = getTsmpDpReqOrdermDao().findFirstByReqOrderNo(apiApp_reqOrderNo);
		if (m == null) {
			throw new Exception("查無工作單: " + apiApp_reqOrderNo);
		}
		
		Long apiApp_reqOrdermId = m.getReqOrdermId();
		List<TsmpDpReqOrderd1> d1List = getTsmpDpReqOrderd1Dao().findByRefReqOrdermId(apiApp_reqOrdermId);
		if (d1List == null || d1List.isEmpty()) {
			throw new Exception("申請單明細空白");
		}
		
//		step("準備更新" + d1List.size() + "筆API授權");
		step("PREP_API_APPLIC");
		
		int success = 0;
		TsmpDpReqOrderd1 d1 = null;
		List<TsmpDpApiAuth2> auth2List = null;
		for(int i = 0; i < d1List.size(); i++) {
			d1 = d1List.get(i);
			//step(d1.getClientId() + ":" + d1.getApiUid());
			step(d1.getApiUid());
			
			try {
				auth2List = getTsmpDpApiAuth2Dao().findByRefClientIdAndRefApiUid(d1.getClientId(), d1.getApiUid());
				if (auth2List == null || auth2List.isEmpty()) {
					continue;
				}
				for(TsmpDpApiAuth2 auth2 : auth2List) {
					// 同clientId同apiUid在tsmp_dp_api_auth2中可能重複, 但是狀態一定是"審核中"才會通過審核
					if (TsmpDpApplyStatus.REVIEW.value().equals(auth2.getApplyStatus())) {
						auth2.setApplyStatus(TsmpDpApplyStatus.PASS.value());
						auth2.setUpdateDateTime(DateTimeUtil.now());
						auth2.setUpdateUser("SYS");
						auth2 = getTsmpDpApiAuth2Dao().save(auth2);
						
						Optional<TsmpClient> opt_c = getTsmpClientDao().findById(d1.getClientId());
						if(!opt_c.isPresent()) {
							throw TsmpDpAaRtnCode.FAIL_AUTHORIZE_API.throwing();
						}
						// 將所申請的API加入使用者群組，以使生效
						TsmpClient client = opt_c.get();
						insertGroupApi(client, auth2.getRefApiUid(), auth2.getUpdateUser());
					}
				}
				success++;
			} catch (Exception e) {
				logger.debug("" + e);
			}
		}
		// 最後押上"進度"
		step(success + "/" + d1List.size());
		return success + "/" + d1List.size();
	}

	private void insertGroupApi(TsmpClient client, String apiUid, String userName) {
		if (client == null) {
			throw TsmpDpAaRtnCode.FAIL_AUTHORIZE_API.throwing();
		}
		TsmpApi api = getApi(apiUid);
		if (api == null) {
			throw TsmpDpAaRtnCode.FAIL_AUTHORIZE_API.throwing();
		}

		// 檢查是否有與用戶同名的群組, 沒有就新增, 並與該用戶建立關聯
		final String apiApp_clientName = client.getClientName();
		final String clientId = client.getClientId();
		TsmpGroup group = getOrCreateGroup(apiApp_clientName, userName);
		TsmpClientGroup apiApp_cg = getOrCreateClientGroup(clientId, group.getGroupId());

		// 刪除舊資料
		TsmpGroupApiId id = new TsmpGroupApiId(apiApp_cg.getGroupId(), api.getApiKey(), api.getModuleName());
		if (getTsmpGroupApiDao().existsById(id)) {
			getTsmpGroupApiDao().deleteById(id);
		}

		// 將所申請的API加入該群組內
		TsmpGroupApi apiApp_groupApi = new TsmpGroupApi();
		apiApp_groupApi.setGroupId(id.getGroupId());
		apiApp_groupApi.setApiKey(id.getApiKey());
		apiApp_groupApi.setModuleName(id.getModuleName());
		apiApp_groupApi.setModuleVer(null);
		apiApp_groupApi.setCreateTime(DateTimeUtil.now());
		apiApp_groupApi = getTsmpGroupApiDao().save(apiApp_groupApi);
		 
	}

	private TsmpGroup getOrCreateGroup(String clientName, String userName) {
		TsmpGroup apiApp_group = getTsmpGroupDao().findFirstByGroupName(clientName);
		if (apiApp_group != null) {
			return apiApp_group;
		}
		
		apiApp_group = new TsmpGroup();
		// 取得流水號
		/* 20200331; Kim; 改取用TSMP內部的序號
		final Long seq = getSeqStoreService().nextSequence(TsmpDpSeqStoreKey.TSMP_GROUP);
		 */
		final Long seq = getSeqStoreService().nextTsmpSequence(TsmpSequenceName.SEQ_TSMP_GROUP_PK);
		if (seq != null) {
			//group.ensureId(seq);
			apiApp_group.setGroupId(seq.toString());
		}
		apiApp_group.setGroupName(clientName);
		apiApp_group.setCreateUser(userName);
		apiApp_group.setCreateTime(DateTimeUtil.now());
		apiApp_group = getTsmpGroupDao().save(apiApp_group);
		return apiApp_group;
	}
	
	private TsmpClientGroup getOrCreateClientGroup(String clientId, String groupId) {
		TsmpClientGroupId id = new TsmpClientGroupId();
		id.setClientId(clientId);
		id.setGroupId(groupId);
		Optional<TsmpClientGroup> apiApp_opt_cg = getTsmpClientGroupDao().findById(id);
		if (apiApp_opt_cg.isPresent()) {
			return apiApp_opt_cg.get();
		}
		TsmpClientGroup cg = new TsmpClientGroup();
		cg.setClientId(clientId);
		cg.setGroupId(groupId);
		cg = getTsmpClientGroupDao().save(cg);
		return cg;
	}
	
	private TsmpApi getApi(String apiUid) {
		List<TsmpApi> apiList = getTsmpApiDao().findByApiUid(apiUid);
		if (apiList == null || apiList.isEmpty()) {
			return null;
		}
		return apiList.get(0);
	}
	
	protected TsmpDpReqOrdermDao getTsmpDpReqOrdermDao() {
		return this.tsmpDpReqOrdermDao;
	}

	protected TsmpDpReqOrderd1Dao getTsmpDpReqOrderd1Dao() {
		return this.tsmpDpReqOrderd1Dao;
	}

	protected TsmpDpApiAuth2Dao getTsmpDpApiAuth2Dao() {
		return this.tsmpDpApiAuth2Dao;
	}
	
	protected TsmpClientDao getTsmpClientDao() {
		return this.tsmpClientDao;
	}
	
	protected TsmpGroupApiDao getTsmpGroupApiDao() {
		return this.tsmpGroupApiDao;
	}
	
	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}
	
	protected TsmpClientGroupDao getTsmpClientGroupDao() {
		return this.tsmpClientGroupDao;
	}
	
	protected TsmpGroupDao getTsmpGroupDao() {
		return this.tsmpGroupDao;
	}

	protected SeqStoreService getSeqStoreService() {
		return this.seqStoreService;
	}
}
