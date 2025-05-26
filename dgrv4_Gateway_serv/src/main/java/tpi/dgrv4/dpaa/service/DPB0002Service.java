package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpApplyStatus;
import tpi.dgrv4.common.constant.TsmpDpModule;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.TsmpMailEventBuilder;
import tpi.dgrv4.dpaa.component.job.DPB0002Job;
import tpi.dgrv4.dpaa.component.job.DeleteExpiredMailJob;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0002Req;
import tpi.dgrv4.dpaa.vo.DPB0002Resp;
import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.entity.constant.TsmpSequenceName;
import tpi.dgrv4.entity.daoService.SeqStoreService;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpClientGroup;
import tpi.dgrv4.entity.entity.TsmpClientGroupId;
import tpi.dgrv4.entity.entity.TsmpGroup;
import tpi.dgrv4.entity.entity.TsmpGroupApi;
import tpi.dgrv4.entity.entity.TsmpGroupApiId;
import tpi.dgrv4.entity.entity.jpql.TsmpApiModule;
import tpi.dgrv4.entity.entity.jpql.TsmpDpApiAuth2;
import tpi.dgrv4.entity.entity.jpql.TsmpDpMailTplt;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpApiModuleDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpClientGroupDao;
import tpi.dgrv4.entity.repository.TsmpDpApiAuth2Dao;
import tpi.dgrv4.entity.repository.TsmpDpMailTpltDao;
import tpi.dgrv4.entity.repository.TsmpGroupApiDao;
import tpi.dgrv4.entity.repository.TsmpGroupDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.escape.MailHelper;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0002Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpDpApiAuth2Dao tsmpDpApiAuth2Dao;
	private TsmpClientDao tsmpClientDao;
	private TsmpApiDao tsmpApiDao;
	private TsmpApiModuleDao tsmpApiModuleDao;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private TsmpGroupApiDao tsmpGroupApiDao;
	private TsmpClientGroupDao tsmpClientGroupDao;
	private TsmpDpMailTpltDao tsmpDpMailTpltDao;
	private TsmpGroupDao tsmpGroupDao;
	private SeqStoreService seqStoreService;
	private JobHelper jobHelper;
	private ApplicationContext ctx;
	private ServiceConfig serviceConfig;
	private TsmpSettingService tsmpSettingService;
	
	private String sendTime;
	
	@Autowired
	public DPB0002Service(TsmpDpApiAuth2Dao tsmpDpApiAuth2Dao, TsmpClientDao tsmpClientDao, TsmpApiDao tsmpApiDao,
			TsmpApiModuleDao tsmpApiModuleDao, TsmpOrganizationDao tsmpOrganizationDao, TsmpGroupApiDao tsmpGroupApiDao,
			TsmpClientGroupDao tsmpClientGroupDao, TsmpDpMailTpltDao tsmpDpMailTpltDao, TsmpGroupDao tsmpGroupDao,
			SeqStoreService seqStoreService, JobHelper jobHelper, ApplicationContext ctx, ServiceConfig serviceConfig,
			TsmpSettingService tsmpSettingService) {
		super();
		this.tsmpDpApiAuth2Dao = tsmpDpApiAuth2Dao;
		this.tsmpClientDao = tsmpClientDao;
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpApiModuleDao = tsmpApiModuleDao;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.tsmpGroupApiDao = tsmpGroupApiDao;
		this.tsmpClientGroupDao = tsmpClientGroupDao;
		this.tsmpDpMailTpltDao = tsmpDpMailTpltDao;
		this.tsmpGroupDao = tsmpGroupDao;
		this.seqStoreService = seqStoreService;
		this.jobHelper = jobHelper;
		this.ctx = ctx;
		this.serviceConfig = serviceConfig;
		this.tsmpSettingService = tsmpSettingService;
	}

	@Transactional
	public DPB0002Resp updateApiStatus(TsmpAuthorization authorization, DPB0002Req req) {
		List<TsmpDpApiAuth2> apiAuthList = null;
		String applyStatus = null;
		String reviewRemark = null;
		boolean isValidReviewRemark = false;
		List<String> orgDescList = null;
		String mailFlag = null;
		try {
			apiAuthList = getApiAuthList(req.getLv());

			String statusParam = req.getStatusParam();
			applyStatus = getApplyStatus(statusParam);

			reviewRemark = req.getReviewRemark();
			isValidReviewRemark = checkReviewRemark(applyStatus, reviewRemark);

			String orgId = authorization.getOrgId();
			orgDescList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(orgId, Integer.MAX_VALUE);

			mailFlag = getMailFlag(req.getMailFlag());
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode.FAIL_AUTHORIZE_API.throwing();
		}

		if (
			apiAuthList == null || apiAuthList.isEmpty() ||		// 傳入Id有誤
			applyStatus == null || applyStatus.isEmpty() ||		// 傳入錯誤的狀態碼(通過/不通過 :P/F)
			!isValidReviewRemark ||								// 審核備註不合規定
			orgDescList == null || orgDescList.isEmpty() ||		// 未登入取不到組織ID
			mailFlag == null									// 錯誤的MailFlag
		) {
			throw TsmpDpAaRtnCode.FAIL_AUTHORIZE_API.throwing();
		}

		// 依照clientId分組
		Map<String, List<TsmpDpApiAuth2>> successData = new HashMap<>();
		String clientId;
		// 先把 TsmpClient 資料查出來準備
		TsmpClient client = null;
		Map<String, TsmpClient> clientMapping = new HashMap<>();
		// 批次審核, 若有一筆失敗則全部取消更新
		for(TsmpDpApiAuth2 auth : apiAuthList) {
			auth = findExistingAuth(auth);
			clientId = auth.getRefClientId();

			if (!clientMapping.containsKey(clientId)) {
				client = getClient(clientId);
				clientMapping.put(clientId, client);
			}
			
			auth = doUpdate(auth, applyStatus, reviewRemark, authorization, orgDescList //
					, clientMapping.get(clientId));
			
			List<TsmpDpApiAuth2> list = successData.get(clientId) == null ? new ArrayList<>() : successData.get(clientId);
			list.add(auth);
			successData.put(clientId, list);
		}

		// 更新成功後, 確認MailFlag是否發送Email
		this.logger.debug("Mail Flag = " + mailFlag);
		if ("0".equals(mailFlag)) {
			sendEmail(successData, authorization, applyStatus, clientMapping);
		}
		
		// 刪除過期的 Mail log
		deleteExpiredMail();
		
		return new DPB0002Resp();
	}

	private List<TsmpDpApiAuth2> getApiAuthList(List<Map<String, Long>> lv) {
		List<TsmpDpApiAuth2> apiAuthList = null;

		if (lv == null || lv.isEmpty()) {
			return null;
		}

		apiAuthList = new ArrayList<>();
		for(Map<String, Long> data : lv) {
			for(Map.Entry<String, Long> d : data.entrySet()) {
				TsmpDpApiAuth2 auth = new TsmpDpApiAuth2();
				auth.setApiAuthId(Long.parseLong(d.getKey().trim()));
				auth.setVersion(d.getValue());
				apiAuthList.add(auth);
			}
		}
		
		return apiAuthList;
	}

	private String getApplyStatus(String statusParam) {
		if ("P".equals(statusParam)) {
			return TsmpDpApplyStatus.PASS.value();
		} else if ("F".equals(statusParam)) {
			return TsmpDpApplyStatus.FAIL.value();
		}
		return null;
	}

	private boolean checkReviewRemark(String applyStatus, String reviewRemark) {
		if (TsmpDpApplyStatus.FAIL.value().equals(applyStatus)) {
			if (reviewRemark == null || reviewRemark.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	private String getMailFlag(String mailFlag) {
		if (mailFlag != null && ("0".equals(mailFlag) || "1".equals(mailFlag))) {
			return mailFlag;
		}
		return null;
	}

	private TsmpDpApiAuth2 findExistingAuth(TsmpDpApiAuth2 auth) {
		final Long apiAuthId = auth.getApiAuthId();
		final Long version = auth.getVersion();
		Optional<TsmpDpApiAuth2> opt = getTsmpDpApiAuth2Dao().findById(apiAuthId);
		if (!opt.isPresent()) {
			throw TsmpDpAaRtnCode.FAIL_AUTHORIZE_API.throwing();
		}

		// 深層拷貝
		TsmpDpApiAuth2 orig = opt.get();
		auth = ServiceUtil.deepCopy(orig, TsmpDpApiAuth2.class);
		auth.setVersion(version);
		return auth;
	}

	private TsmpDpApiAuth2 doUpdate(TsmpDpApiAuth2 auth, String applyStatus, //
			String reviewRemark, TsmpAuthorization authorization, List<String> orgDescList //
			, TsmpClient client) {

		// 依登入者所屬組織判斷是否有權放行此API
		if (!isInOrgDescList(auth.getRefApiUid(), orgDescList)) {
			throw TsmpDpAaRtnCode.FAIL_AUTHORIZE_API.throwing();
		}

		auth.setApplyStatus(applyStatus);
		auth.setRefReviewUser(authorization.getUserName());
		auth.setReviewRemark(reviewRemark);
		auth.setUpdateDateTime(DateTimeUtil.now());
		auth.setUpdateUser(authorization.getUserName());
		try {
			auth = getTsmpDpApiAuth2Dao().save(auth);

			// 若為放行，則將所申請的API加入使用者群組，以使生效
			if (TsmpDpApplyStatus.PASS.value().equals(applyStatus)) {
				insertGroupApi(client, auth.getRefApiUid(), auth.getUpdateUser());
			}
		} catch (ObjectOptimisticLockingFailureException e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode.ERROR_DATA_EDITED.throwing();
		}
		return auth;
	}

	private boolean isInOrgDescList(String apiUid, List<String> orgDescList) {
		TsmpApi api = getApi(apiUid);
		if (api == null) {
			return false;
		}
		
		for(String orgId : orgDescList) {
			if (orgId.equals(api.getOrgId())) {
				return true;
			}
		}
		return false;
	}

	private void insertGroupApi(TsmpClient client, String apiUid, String userName) {
		if (client == null) {
			throw TsmpDpAaRtnCode.FAIL_AUTHORIZE_API.throwing();
		}
		TsmpApi api = getApi(apiUid);
		if (api == null) {
			this.logger.error(String.format("Unable to find tsmp_api by api_uid '%s'", apiUid));
			throw TsmpDpAaRtnCode.FAIL_AUTHORIZE_API.throwing();
		}

		// 檢查是否有與用戶同名的群組, 沒有就新增, 並與該用戶建立關聯
		final String clientName = client.getClientName();
		final String clientId = client.getClientId();
		TsmpGroup group = getOrCreateGroup(clientName, userName);
		TsmpClientGroup cg = getOrCreateClientGroup(clientId, group.getGroupId());

		// 刪除舊資料
		TsmpGroupApiId id = new TsmpGroupApiId(cg.getGroupId(), api.getApiKey(), api.getModuleName());
		if (getTsmpGroupApiDao().existsById(id)) {
			getTsmpGroupApiDao().deleteById(id);
		}

		// 將所申請的API加入該群組內
		TsmpGroupApi groupApi = new TsmpGroupApi();
		groupApi.setGroupId(id.getGroupId());
		groupApi.setApiKey(id.getApiKey());
		groupApi.setModuleName(id.getModuleName());
		groupApi.setModuleVer(null);
		groupApi.setCreateTime(DateTimeUtil.now());
		groupApi = getTsmpGroupApiDao().save(groupApi);
		
		/* 2020/02/21 Kim; API申請審核通過的流程有異動
		List<TsmpClientGroup> groups = getTsmpClientGroupDao().findByClientId(clientId);
		if (groups != null && groups.size() > 0) {
			// Client所屬的每個Group都要加入此API(入口網申請的Client只會屬於單一Group)
			for(TsmpClientGroup group : groups) {
				TsmpApi api = getApi(apiUid);
				if (api == null) {
					this.logger.error("Unable to find tsmp_api by api_uid '{}'", apiUid);
					throw TsmpDpAaRtnCode.FAIL_AUTHORIZE_API.throwing();
				}

				// 刪除舊資料
				TsmpGroupApiId id = new TsmpGroupApiId(group.getGroupId(), api.getApiKey(), api.getModuleName());
				if (getTsmpGroupApiDao().existsById(id)) {
					getTsmpGroupApiDao().deleteById(id);
				}

				TsmpGroupApi groupApi = new TsmpGroupApi();
				groupApi.setGroupId(id.getGroupId());
				groupApi.setApiKey(id.getApiKey());
				groupApi.setModuleName(id.getModuleName());
				groupApi.setModuleVer(null);
				//TsmpApiModule module = getModule(id.getModuleName());
				//if (module != null) {
				//	groupApi.setModuleVer(module.getModuleVersion());
				//}
				groupApi.setCreateTime(DateTimeUtil.now());
				groupApi = getTsmpGroupApiDao().save(groupApi);
			}
		}
		*/
	}

	private TsmpGroup getOrCreateGroup(String clientName, String userName) {
		TsmpGroup group = getTsmpGroupDao().findFirstByGroupName(clientName);
		if (group != null) {
			return group;
		}
		
		group = new TsmpGroup();
		// 取得流水號
		/* 20200331; Kim; 改取用TSMP內部的序號
		final Long seq = getSeqStoreService().nextSequence(TsmpDpSeqStoreKey.TSMP_GROUP);
		 */
		final Long seq = getSeqStoreService().nextTsmpSequence(TsmpSequenceName.SEQ_TSMP_GROUP_PK);
		if (seq != null) {
			//group.ensureId(seq);
			group.setGroupId(seq.toString());
		}
		group.setGroupName(clientName);
		group.setCreateUser(userName);
		group.setCreateTime(DateTimeUtil.now());
		group = getTsmpGroupDao().save(group);
		return group;
	}

	private TsmpClientGroup getOrCreateClientGroup(String clientId, String groupId) {
		TsmpClientGroupId id = new TsmpClientGroupId();
		id.setClientId(clientId);
		id.setGroupId(groupId);
		Optional<TsmpClientGroup> opt_cg = getTsmpClientGroupDao().findById(id);
		if (opt_cg.isPresent()) {
			return opt_cg.get();
		}
		TsmpClientGroup cg = new TsmpClientGroup();
		cg.setClientId(clientId);
		cg.setGroupId(groupId);
		cg = getTsmpClientGroupDao().save(cg);
		return cg;
	}

	public DPB0002Job sendEmail(Map<String, List<TsmpDpApiAuth2>> successData //
			, TsmpAuthorization authorization, String applyStatus, Map<String, TsmpClient> clientMapping) {
		List<TsmpMailEvent> mailEvents = new ArrayList<>();
		// 一個Client寄一次信就好
		successData.forEach((clientId, list) -> {
			TsmpClient client = clientMapping.get(clientId);
			TsmpMailEvent mailEvent = getTsmpMailEvent(client, list, authorization, applyStatus);
			if (mailEvent != null) {
				mailEvents.add(mailEvent);
			}
		});
		
		//使用 Job 寫入 APPT_JOB Table & 建立 Mail 檔案, 由排程來寄信
		DPB0002Job job = getDPB0002Job(authorization, mailEvents, getSendTime());
		
		return job;
	}

	protected DPB0002Job getDPB0002Job(TsmpAuthorization authorization, List<TsmpMailEvent> mailEvents,
			String sendTime) {
		DPB0002Job job = (DPB0002Job) getCtx().getBean("dpb0002Job", authorization, mailEvents, sendTime);
		getJobHelper().add(job);
		return job;
	}
	
	public DeleteExpiredMailJob deleteExpiredMail() {
		DeleteExpiredMailJob job = (DeleteExpiredMailJob) getCtx().getBean("deleteExpiredMailJob");
		getJobHelper().add(job);
		return job;
	}

	private TsmpMailEvent getTsmpMailEvent(TsmpClient client, List<TsmpDpApiAuth2> authList //
			, TsmpAuthorization authorization, String applyStatus) {
		if (client == null) {
			this.logger.debug("Missing client!");
			return null;
		}
		String dpb0002_clientId = client.getClientId();
		String recipients = client.getEmails();
		if (recipients == null || recipients.isEmpty()) {
			this.logger.debug(String.format("Client %s has empty emails!", dpb0002_clientId));
			return null;
		}

		String dpb0002_subject = null;
		String body = null;
		String templateKey = "";
		if (TsmpDpApplyStatus.PASS.value().equals(applyStatus)) {
			dpb0002_subject = getTemplate("subject.api-pass");
			templateKey = "body.api-pass";
		} else if (TsmpDpApplyStatus.FAIL.value().equals(applyStatus)) {
			dpb0002_subject = getTemplate("subject.api-fail");
			templateKey = "body.api-fail";
		}
		
		body = getTemplate(templateKey);
		if (dpb0002_subject == null || body == null) {
			return null;
		}

		Map<String, String> dpb0002_subjectParams = getSubjectParams(applyStatus);
		if (dpb0002_subjectParams == null || dpb0002_subjectParams.isEmpty()) {
			this.logger.debug(String.format("Client %s has empty subject params!", dpb0002_clientId));
			return null;
		}

		Map<String, String> bodyParams = getBodyParams(client, authList, applyStatus);
		if (bodyParams == null || bodyParams.isEmpty()) {
			this.logger.debug(String.format("Client %s has empty body params!", dpb0002_clientId));
			return null;
		}

		final String title = MailHelper.buildContent(dpb0002_subject, dpb0002_subjectParams);
		final String dpb0002_content = MailHelper.buildContent(body, bodyParams);
		this.logger.debug("Email title = " + title);
		this.logger.debug("Email content = " + dpb0002_content);
		return new TsmpMailEventBuilder() //
		.setSubject(title)
		.setContent(dpb0002_content)
		.setRecipients(recipients)
		.setCreateUser(authorization.getUserName())
		.setRefCode(templateKey)
		.build();
	}

	private TsmpClient getClient(String clientId) {
		Optional<TsmpClient> opt = getTsmpClientDao().findById(clientId);
		if (opt.isPresent()) {
			return opt.get();
		}
		return null;
	}

	private Map<String, String> getSubjectParams(String applyStatus) {
		Map<String, String> emailParams = new HashMap<>();
		emailParams.put("projectName", TsmpDpModule.DP.getChiDesc());
		return emailParams;
	}

	private Map<String, String> getBodyParams(TsmpClient client, List<TsmpDpApiAuth2> authList //
			, String applyStatus) {
		String dpb0002_clientName = client.getClientName();
		if (dpb0002_clientName == null || dpb0002_clientName.isEmpty()) {
			return null;
		}

		String now = "";
		Optional<String> opt = DateTimeUtil.dateTimeToString(DateTimeUtil.now(), DateTimeFormatEnum.西元年月日時分);
		if (opt.isPresent()) {
			now = opt.get();
		}

		String dataList = getDataList(authList);

		Map<String, String> dpb0002_emailParams = new HashMap<>();
		dpb0002_emailParams.put("clientName", dpb0002_clientName);
		dpb0002_emailParams.put("data-list", dataList);
		if (TsmpDpApplyStatus.FAIL.value().equals(applyStatus)) {
			dpb0002_emailParams.put("serviceMail", getTsmpSettingService().getVal_MAIL_BODY_API_FAIL_SERVICE_MAIL());
			dpb0002_emailParams.put("serviceTel", getTsmpSettingService().getVal_MAIL_BODY_API_FAIL_SERVICE_TEL());
		}
		dpb0002_emailParams.put("projectName", TsmpDpModule.DP.getChiDesc());
		dpb0002_emailParams.put("date", now);
		return dpb0002_emailParams;
	}

	private String getDataList(List<TsmpDpApiAuth2> authList) {
		StringBuffer dpb0002_dataList = new StringBuffer();

		String rowTemplate = getTemplate("body.api-pass.list");
		Map<String, String> rowParams = null;
		for(TsmpDpApiAuth2 dpb0002_auth : authList) {
			// Initialize
			rowParams = new HashMap<>();
			rowParams.put("apiName", "");
			rowParams.put("apiDesc", "");
//			rowParams.put("moduleVersion", "");
//			rowParams.put("applyStatus", "");
//			rowParams.put("refReviewUser", "");
			rowParams.put("reviewRemark", "");
			rowParams.put("apiKey", "");
			rowParams.put("moduleName", "");
			fillRowParams(rowParams, dpb0002_auth);
			dpb0002_dataList.append(MailHelper.buildContent(rowTemplate, rowParams));
		}

		return dpb0002_dataList.toString();
	}

	private String getTemplate(String code) {
		List<TsmpDpMailTplt> dpb0002_list = getTsmpDpMailTpltDao().findByCode(code);
		if (dpb0002_list != null && !dpb0002_list.isEmpty()) {
			return dpb0002_list.get(0).getTemplateTxt();
		}
		return null;
	}

	private void fillRowParams(Map<String, String> params, TsmpDpApiAuth2 auth) {
//		params.put("applyStatus", TsmpDpApplyStatus.getText(auth.getApplyStatus()));
//		params.put("refReviewUser", auth.getRefReviewUser());
		params.put("reviewRemark", auth.getReviewRemark());
		
		String apiUid = auth.getRefApiUid();
		List<TsmpApi> apiList = getTsmpApiDao().findByApiUid(apiUid);
		if (apiList != null && !apiList.isEmpty()) {
			TsmpApi dpb0002_api = apiList.get(0);
			params.put("apiName", dpb0002_api.getApiName());
			params.put("apiDesc", dpb0002_api.getApiDesc());
			params.put("apiKey", dpb0002_api.getApiKey());

			/*
			TsmpApiModule module = getModule(api.getModuleName());
			if (module != null) {
				params.put("moduleVersion", module.getModuleVersion());
				params.put("moduleName", module.getModuleName());
			}
			*/
			params.put("moduleName", dpb0002_api.getModuleName());
		}
	}

	private TsmpApi getApi(String apiUid) {
		List<TsmpApi> dpb0002_apiList = getTsmpApiDao().findByApiUid(apiUid);
		if (dpb0002_apiList == null || dpb0002_apiList.isEmpty()) {
			return null;
		}
		return dpb0002_apiList.get(0);
	}

	private TsmpApiModule getModule(String moduleName) {
		List<TsmpApiModule> mList = getTsmpApiModuleDao().findByModuleNameAndActive(moduleName, true);
		if (mList != null && !mList.isEmpty()) {
			return mList.get(0);
		}
		return null;
	}

	protected TsmpDpApiAuth2Dao getTsmpDpApiAuth2Dao() {
		return this.tsmpDpApiAuth2Dao;
	}

	protected TsmpClientDao getTsmpClientDao() {
		return this.tsmpClientDao;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}

	protected TsmpApiModuleDao getTsmpApiModuleDao() {
		return this.tsmpApiModuleDao;
	}

	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}

	protected TsmpGroupApiDao getTsmpGroupApiDao() {
		return this.tsmpGroupApiDao;
	}

	protected TsmpClientGroupDao getTsmpClientGroupDao() {
		return this.tsmpClientGroupDao;
	}

	protected TsmpDpMailTpltDao getTsmpDpMailTpltDao() {
		return this.tsmpDpMailTpltDao;
	}

	protected TsmpGroupDao getTsmpGroupDao() {
		return this.tsmpGroupDao;
	}

	protected SeqStoreService getSeqStoreService() {
		return this.seqStoreService;
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
	
	protected String getSendTime() {
		this.sendTime = this.getTsmpSettingService().getVal_MAIL_SEND_TIME();//多久後寄發Email(ms)
		return this.sendTime;
	}
	
	protected TsmpSettingService getTsmpSettingService() {
		return this.tsmpSettingService;
	}

}
