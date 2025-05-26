package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.dpaa.component.TsmpMailEventBuilder;
import tpi.dgrv4.dpaa.component.cache.proxy.TsmpDpMailTpltCacheProxy;
import tpi.dgrv4.dpaa.component.job.DeleteExpiredMailJob;
import tpi.dgrv4.dpaa.component.job.SendOpenApiKeyMailJob;
import tpi.dgrv4.dpaa.component.req.DpReqQueryFactory;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpModule;
import tpi.dgrv4.common.constant.TsmpDpReqReviewType;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpOpenApiKey;
import tpi.dgrv4.entity.entity.TsmpOpenApiKeyMap;
import tpi.dgrv4.entity.entity.jpql.TsmpDpChkLog;
import tpi.dgrv4.entity.entity.jpql.TsmpDpMailTplt;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpDpChkLogDao;
import tpi.dgrv4.entity.repository.TsmpOpenApiKeyDao;
import tpi.dgrv4.entity.repository.TsmpOpenApiKeyMapDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.escape.MailHelper;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class SendOpenApiKeyMailService {

	private TPILogger logger = TPILogger.tl;

	private TsmpDpMailTpltCacheProxy tsmpDpMailTpltCacheProxy;
	private MailHelper mailHelper;
	private TsmpDpChkLogDao tsmpDpChkLogDao;
	private TsmpUserDao tsmpUserDao;
	private ApplicationContext ctx;
	private JobHelper jobHelper;
	private DpReqQueryFactory dpReqQueryFactory;
	private TsmpOpenApiKeyDao tsmpOpenApiKeyDao;
	private TsmpOpenApiKeyMapDao tsmpOpenApiKeyMapDao;
	private TsmpClientDao tsmpClientDao;
	private TsmpApiDao tsmpApiDao;
	private TsmpSettingService tsmpSettingService;
	
	private String sendTime;
 
	@Autowired
	public SendOpenApiKeyMailService(TsmpDpMailTpltCacheProxy tsmpDpMailTpltCacheProxy, MailHelper mailHelper,
			TsmpDpChkLogDao tsmpDpChkLogDao, TsmpUserDao tsmpUserDao, ApplicationContext ctx, JobHelper jobHelper,
			DpReqQueryFactory dpReqQueryFactory, TsmpOpenApiKeyDao tsmpOpenApiKeyDao,
			TsmpOpenApiKeyMapDao tsmpOpenApiKeyMapDao, TsmpClientDao tsmpClientDao, TsmpApiDao tsmpApiDao,
			TsmpSettingService tsmpSettingService) {
		super();
		this.tsmpDpMailTpltCacheProxy = tsmpDpMailTpltCacheProxy;
		this.mailHelper = mailHelper;
		this.tsmpDpChkLogDao = tsmpDpChkLogDao;
		this.tsmpUserDao = tsmpUserDao;
		this.ctx = ctx;
		this.jobHelper = jobHelper;
		this.dpReqQueryFactory = dpReqQueryFactory;
		this.tsmpOpenApiKeyDao = tsmpOpenApiKeyDao;
		this.tsmpOpenApiKeyMapDao = tsmpOpenApiKeyMapDao;
		this.tsmpClientDao = tsmpClientDao;
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpSettingService = tsmpSettingService;
	}

	@PostConstruct
	public void init() {
	}
	
	/**
	 * Open API Key申請/異動/撤銷成功,
	 * 以 email 發給 client, Open API Key & Secret Key及可使用的 API
	 * 
	 * @param reqOrdermId
	 * @param openApiKeyId
	 * @param openApiKeyType
	 * @return
	 */
	public SendOpenApiKeyMailJob sendEmail(Long reqOrdermId, Long openApiKeyId, String openApiKeyType, String reqOrderNo) {
		List<TsmpDpChkLog> logList = getTsmpDpChkLogDao().queryHistoryByPk(null, reqOrdermId, Integer.MAX_VALUE);
		String userName = "";
		if(logList != null && !logList.isEmpty()) {
			TsmpDpChkLog log = logList.get(logList.size() - 1);
			userName = log.getCreateUser();//最後的簽核者
		}
		
		TsmpAuthorization auth = new TsmpAuthorization();
		auth.setUserName(userName);
		
		//使用 Job 寫入 APPT_JOB Table & 建立 Mail 檔案, 由排程來寄信
		SendOpenApiKeyMailJob job = getSendOpenApiKeyMailJob(auth, getSendTime(), openApiKeyId, openApiKeyType, reqOrderNo);
		getJobHelper().add(job);
		
		// 刪除過期的 Mail log
		deleteExpiredMail();
		
		return job;
	}
	
	protected DeleteExpiredMailJob deleteExpiredMail() {
		DeleteExpiredMailJob job = (DeleteExpiredMailJob) getCtx().getBean("deleteExpiredMailJob");
		getJobHelper().add(job);
		return job;
	}
	
	public List<TsmpMailEvent> getTsmpMailEvents(TsmpAuthorization auth, Long openApiKeyId, String openApiKeyType) throws Exception {
		Optional<TsmpOpenApiKey> opt_oak = getTsmpOpenApiKeyDao().findById(openApiKeyId);
		if (!opt_oak.isPresent()) {
			throw new Exception("查無Open Api Key, 無法寄出通知信");
		}
		TsmpOpenApiKey oak = opt_oak.get();
		String clientId = oak.getClientId();
		Optional<TsmpClient> opt_client = getTsmpClientDao().findById(clientId);
		if (!opt_client.isPresent()) {
			throw new Exception("查無收件者 " + clientId + ", 無法寄出通知信");
		}
		//收件者
		TsmpClient client = opt_client.get();
		String recipients = opt_client.get().getEmails();
		if (StringUtils.isEmpty(recipients)) {
			this.logger.debug(String.format("Client %s has empty emails!", clientId));
			return null;
		}
		
		List<TsmpMailEvent> mailEvents = new ArrayList<>();
		TsmpMailEvent mailEvent = getTsmpMailEvent(client, recipients, auth, oak, openApiKeyType);
		if (mailEvent != null) {
			mailEvents.add(mailEvent);
		}
 
		return mailEvents;
	}
	
	protected TsmpMailEvent getTsmpMailEvent(TsmpClient client, String recipients, TsmpAuthorization auth, 
			TsmpOpenApiKey oak, String openApiKeyType) {
		String subjTpltCode = "subject.oak-pass";
		String bodyTpltCode = "body.oak-pass";
		
		String subject = getTemplate(subjTpltCode);
		String body = getTemplate(bodyTpltCode);
		if (StringUtils.isEmpty(subject) || StringUtils.isEmpty(body)) {
			this.logger.debug(String.format("Cannot find email templates: %s, %s}", subjTpltCode, bodyTpltCode));
			return null;
		}
		
		//主旨
		Map<String, Object> subjectParams = new HashMap<>();
		subjectParams.put("projectName", TsmpDpModule.DP.getChiDesc());
		subject = getMailHelper().buildNestedContent(subjTpltCode, subjectParams);
		
		//內文
		List<Map<String, Object>> apiMappings = getApiMappings(client, oak.getOpenApiKeyId());
		Map<String, Object> bodyParams = new HashMap<>();
		
		String oakTypeName = "";
		String revokedAt = "";
		if(TsmpDpReqReviewType.OPEN_API_KEY.OPEN_API_KEY_APPLICA.isValueEquals(openApiKeyType)) {
			oakTypeName = TsmpDpReqReviewType.OPEN_API_KEY.OPEN_API_KEY_APPLICA.text();
			
		}else if(TsmpDpReqReviewType.OPEN_API_KEY.OPEN_API_KEY_UPDATE.isValueEquals(openApiKeyType)) {
			oakTypeName = TsmpDpReqReviewType.OPEN_API_KEY.OPEN_API_KEY_UPDATE.text();
			
		}else if(TsmpDpReqReviewType.OPEN_API_KEY.OPEN_API_KEY_REVOKE.isValueEquals(openApiKeyType)) {
			oakTypeName = TsmpDpReqReviewType.OPEN_API_KEY.OPEN_API_KEY_REVOKE.text();
			revokedAt = "撤銷日期 : " + getDateTime(oak.getRevokedAt());
		}
		
		oakTypeName = oakTypeName.replace("Open ", "");
		oakTypeName = oakTypeName.replace("申請", "Application");
		oakTypeName = oakTypeName.replace("異動", "Update");
		oakTypeName = oakTypeName.replace("撤銷", "Revoke");
		
		bodyParams.put("openApiKeyType", oakTypeName);
		bodyParams.put("openApiKey", oak.getOpenApiKey());
		bodyParams.put("secretKey", oak.getSecretKey());
		bodyParams.put("openApiKeyAlias", oak.getOpenApiKeyAlias());
		bodyParams.put("timesThreshold", oak.getTimesThreshold());
		bodyParams.put("createDateTime", DateTimeUtil.dateTimeToString(oak.getCreateDateTime(), DateTimeFormatEnum.西元年月日).orElse(new String()));
		bodyParams.put("expiredAt", getDateTime(oak.getExpiredAt()));
		bodyParams.put("revokedAt", revokedAt);
		bodyParams.put("apiMappings", apiMappings);
		
		String content = getMailHelper().buildNestedContent(bodyTpltCode, bodyParams);
		
		return new TsmpMailEventBuilder() //
		.setSubject(subject)
		.setContent(content)
		.setRecipients(recipients)
		.setCreateUser(auth.getUserName())
		.setRefCode(bodyTpltCode)
		.build();
	}
	
	private List<Map<String, Object>> getApiMappings(TsmpClient client, Long openApiKeyId){
		List<Map<String, Object>> apiMappings = new ArrayList<>();
		List<TsmpOpenApiKeyMap> oakMapList = getTsmpOpenApiKeyMapDao().findByRefOpenApiKeyId(openApiKeyId);
		
		Map<String, Object> apiMapping = new HashMap<>();
		apiMapping.put("clientId", client.getClientId());
		apiMapping.put("clientName", client.getClientName());
		apiMapping.put("clientAlias", ServiceUtil.nvl(client.getClientAlias()));
		
		List<String> apiNameList = new ArrayList<>();
		for (TsmpOpenApiKeyMap oakMap : oakMapList) {
			String apiUid = oakMap.getRefApiUid();
			List<TsmpApi> apiList = getTsmpApiDao().findByApiUid(apiUid);
			if (apiList != null && !apiList.isEmpty()) {
				TsmpApi api = apiList.get(0);
				apiNameList.add(api.getApiName());
			}
		}
		apiMapping.put("apiNames", apiNameList);
		apiMappings.add(apiMapping);
		
		return apiMappings;
	}
	
	private String getTemplate(String code) {
		List<TsmpDpMailTplt> list = getTsmpDpMailTpltCacheProxy().findByCode(code);
		if (list != null && !list.isEmpty()) {
			return list.get(0).getTemplateTxt();
		}
		return null;
	}
	
	private String getDateTime(Long timeInMillis) {
		if(timeInMillis == null) {
			return "";
		}
		Date dt = new Date(timeInMillis);
		String dtStr = DateTimeUtil.dateTimeToString(dt, DateTimeFormatEnum.西元年月日).orElseThrow(TsmpDpAaRtnCode._1295::throwing);  // yyyy-MM-dd
		return dtStr;
	}
	
	protected SendOpenApiKeyMailJob getSendOpenApiKeyMailJob(TsmpAuthorization auth, String sendTime, Long openApiKeyId, 
			String openApiKeyType, String reqOrderNo) {
		return (SendOpenApiKeyMailJob) getCtx().getBean("sendOpenApiKeyMailJob", auth, getSendTime(), openApiKeyId,
				openApiKeyType, reqOrderNo);
	}
	
	protected TsmpDpMailTpltCacheProxy getTsmpDpMailTpltCacheProxy() {
		return this.tsmpDpMailTpltCacheProxy;
	}
	
	protected MailHelper getMailHelper() {
		return this.mailHelper;
	}
	
	protected TsmpDpChkLogDao getTsmpDpChkLogDao() {
		return this.tsmpDpChkLogDao;
	}
	
	protected TsmpUserDao getTsmpUserDao() {
		return this.tsmpUserDao;
	}

	protected String getSendTime() {
		this.sendTime = this.getTsmpSettingService().getVal_MAIL_SEND_TIME();//多久後寄發Email(ms)
		return this.sendTime;
	}

	protected ApplicationContext getCtx() {
		return this.ctx;
	}

	protected JobHelper getJobHelper() {
		return this.jobHelper;
	}

	protected DpReqQueryFactory getDpReqQueryFactory() {
		return this.dpReqQueryFactory;
	}

	protected TsmpOpenApiKeyDao getTsmpOpenApiKeyDao() {
		return this.tsmpOpenApiKeyDao;
	}
	
	protected TsmpOpenApiKeyMapDao getTsmpOpenApiKeyMapDao() {
		return this.tsmpOpenApiKeyMapDao;
	}
	
	protected TsmpClientDao getTsmpClientDao() {
		return this.tsmpClientDao;
	}
	
	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return this.tsmpSettingService;
	}
}
