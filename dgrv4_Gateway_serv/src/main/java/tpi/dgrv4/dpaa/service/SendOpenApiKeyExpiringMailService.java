package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpModule;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.LicenseUtilBase;
import tpi.dgrv4.dpaa.component.TsmpMailEventBuilder;
import tpi.dgrv4.dpaa.component.UrlCodecHelper;
import tpi.dgrv4.dpaa.component.cache.proxy.TsmpDpMailTpltCacheProxy;
import tpi.dgrv4.dpaa.component.job.DeleteExpiredMailJob;
import tpi.dgrv4.dpaa.component.job.SendOpenApiKeyExpiringMailJob;
import tpi.dgrv4.dpaa.util.OAuthUtil;
import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpOpenApiKey;
import tpi.dgrv4.entity.entity.TsmpOpenApiKeyMap;
import tpi.dgrv4.entity.entity.jpql.TsmpDpMailTplt;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpOpenApiKeyDao;
import tpi.dgrv4.entity.repository.TsmpOpenApiKeyMapDao;
import tpi.dgrv4.escape.MailHelper;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
@Scope("prototype")
public class SendOpenApiKeyExpiringMailService {
	
	private LicenseUtilBase licenseUtil;
	private TsmpOpenApiKeyDao tsmpOpenApiKeyDao;
	private TsmpOpenApiKeyMapDao tsmpOpenApiKeyMapDao;
	private MailHelper mailHelper;
	private ApplicationContext ctx;
	private JobHelper jobHelper;
	private TsmpApiDao tsmpApiDao;
	private TsmpClientDao tsmpClientDao;
	private TsmpSettingService tsmpSettingService;
	private TsmpDpMailTpltCacheProxy tsmpDpMailTpltCacheProxy;
	
	private String sendTime;
	
	@Autowired
	public SendOpenApiKeyExpiringMailService(LicenseUtilBase licenseUtil, TsmpOpenApiKeyDao tsmpOpenApiKeyDao,
			TsmpOpenApiKeyMapDao tsmpOpenApiKeyMapDao, MailHelper mailHelper, ApplicationContext ctx,
			JobHelper jobHelper, TsmpApiDao tsmpApiDao, TsmpClientDao tsmpClientDao,
			TsmpSettingService tsmpSettingService, TsmpDpMailTpltCacheProxy tsmpDpMailTpltCacheProxy) {
		super();
		this.licenseUtil = licenseUtil;
		this.tsmpOpenApiKeyDao = tsmpOpenApiKeyDao;
		this.tsmpOpenApiKeyMapDao = tsmpOpenApiKeyMapDao;
		this.mailHelper = mailHelper;
		this.ctx = ctx;
		this.jobHelper = jobHelper;
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpClientDao = tsmpClientDao;
		this.tsmpSettingService = tsmpSettingService;
		this.tsmpDpMailTpltCacheProxy = tsmpDpMailTpltCacheProxy;
	}

	/**
	 * 以 email 發給 client, 效期快到的 Open API Key & Secret Key及可使用的 API
	 * 
	 * @param openApiKeyId
	 * @param openApiKeyType
	 * @return
	 */
	public SendOpenApiKeyExpiringMailJob sendEmail(Long openApiKeyId) {
		TsmpAuthorization auth = new TsmpAuthorization();
		auth.setUserName("SYSTEM");
		
		//使用 Job 寫入 APPT_JOB Table & 建立 Mail 檔案, 由排程來寄信
		SendOpenApiKeyExpiringMailJob job = getSendOpenApiKeyExpiringMailJob(auth, getSendTime(), openApiKeyId);
		getJobHelper().add(job);
		
		// 刪除過期的 Mail log
		deleteExpiredMail();
		
		return job;
	}
	
	protected SendOpenApiKeyExpiringMailJob getSendOpenApiKeyExpiringMailJob(TsmpAuthorization auth, String sendTime, Long openApiKeyId) {
		return (SendOpenApiKeyExpiringMailJob) getCtx().getBean("sendOpenApiKeyExpiringMailJob", auth, sendTime, openApiKeyId);
	}
	
	public List<TsmpMailEvent> getTsmpMailEvents(TsmpAuthorization auth, Long openApiKeyId) throws Exception {
		Optional<TsmpOpenApiKey> opt_oak = getTsmpOpenApiKeyDao().findById(openApiKeyId);
		if (!opt_oak.isPresent()) {
			throw new Exception("查無Open Api Key, 無法寄出通知信");
		}
		TsmpOpenApiKey oak = opt_oak.get();
		String soakem_clientId = oak.getClientId();
		Optional<TsmpClient> opt_client = getTsmpClientDao().findById(soakem_clientId);
		if (!opt_client.isPresent()) {
			throw new Exception("查無收件者 " + soakem_clientId + ", 無法寄出通知信");
		}
		//收件者
		TsmpClient client = opt_client.get();
		String recipients = opt_client.get().getEmails();
		if (StringUtils.isEmpty(recipients)) {
			TPILogger.tl.error(String.format("Client %s has empty emails!", soakem_clientId));
			return null;
		}
		
		List<TsmpMailEvent> mailEvents = new ArrayList<>();
		TsmpMailEvent mailEvent = getTsmpMailEvent(client, recipients, auth, oak);
		if (mailEvent != null) {
			mailEvents.add(mailEvent);
		}
 
		return mailEvents;
	}
	
	protected TsmpMailEvent getTsmpMailEvent(TsmpClient client, String recipients, TsmpAuthorization auth, 
			TsmpOpenApiKey oak) throws Exception {
		
		Long openApiKeyId = oak.getOpenApiKeyId();
		String openApiKey = oak.getOpenApiKey();
		String encodeOpenApiKey = UrlCodecHelper.getEncode(openApiKey);//用URL 編碼
		String secretKey = oak.getSecretKey();
		String subjectCode = "subject.oak-expi";
		String subject = getTemplate(subjectCode);
		String tsmpEdition = getTsmpEdition();

		getLicenseUtil().initLicenseUtil(tsmpEdition, null);
		String oakExpiUrl = getTsmpSettingService().getVal_OAK_EXPI_URL();
		
		String edition = getLicenseUtil().getEdition(tsmpEdition);
		TPILogger.tl.debug("edition:" + edition);
		
		String bodyCode = "";
		if("Enterprise".equalsIgnoreCase(edition) || "Alpha".equalsIgnoreCase(edition)) {
			bodyCode = "body.oak-expi";//for Enterprise
			
		}else if("Express".equalsIgnoreCase(edition)) {
			bodyCode = "body.oak-expi2";//for Express
			
		}else {
			bodyCode = "body.oak-expi2";//找不到版本時,預設為for Express
			TPILogger.tl.debug("Cannot find email edition: " + edition);
		}
		TPILogger.tl.debug("bodyCode:" + bodyCode);
		
		String body = getTemplate(bodyCode);
		if (StringUtils.isEmpty(subject) || StringUtils.isEmpty(body)) {
			TPILogger.tl.debug(String.format("Cannot find email templates: %s, %s", subjectCode, bodyCode));
			return null;
		}
		
		//主旨
		Map<String, Object> subjectParams = new HashMap<>();
		subjectParams.put("projectName", TsmpDpModule.DP.getChiDesc());
		subject = getMailHelper().buildNestedContent(subjectCode, subjectParams);
 
		//內文
		List<Map<String, Object>> apiMappings = getApiMappings(client, openApiKeyId);
		Map<String, Object> bodyParams = new HashMap<>();
		
		String par1 = OAuthUtil.getPar1(openApiKeyId, openApiKey, secretKey);
		String par2 = encodeOpenApiKey;//Open API Key(用URL 編碼)
		
		bodyParams.put("oakExpiUrl",oakExpiUrl);
		bodyParams.put("par1", par1);
		bodyParams.put("par2", par2);
		bodyParams.put("openApiKey", openApiKey);
		bodyParams.put("secretKey", secretKey);
		bodyParams.put("openApiKeyAlias", oak.getOpenApiKeyAlias());
		bodyParams.put("timesThreshold", oak.getTimesThreshold());
		bodyParams.put("createDateTime", DateTimeUtil.dateTimeToString(oak.getCreateDateTime(), DateTimeFormatEnum.西元年月日).orElse(new String()));
		bodyParams.put("expiredAt", getDateTime(oak.getExpiredAt()));
		bodyParams.put("apiMappings", apiMappings);
		
		String content = getMailHelper().buildNestedContent(bodyCode, bodyParams);
		
		return new TsmpMailEventBuilder() //
		.setSubject(subject)
		.setContent(content)
		.setRecipients(recipients)
		.setCreateUser(auth.getUserName())
		.setRefCode(bodyCode)
		.build();
	}
	
	private List<Map<String, Object>> getApiMappings(TsmpClient client, Long openApiKeyId){
		List<Map<String, Object>> apiMappings = new ArrayList<>();
		List<TsmpOpenApiKeyMap> oakMapList = getTsmpOpenApiKeyMapDao().findByRefOpenApiKeyId(openApiKeyId);
		
		Map<String, Object> apiMapping = new HashMap<>();
		apiMapping.put("clientId", client.getClientId());
		apiMapping.put("clientName", client.getClientName());
		apiMapping.put("clientAlias", client.getClientAlias());
		
		List<String> soakem_apiNameList = new ArrayList<>();
		for (TsmpOpenApiKeyMap oakMap : oakMapList) {
			String apiUid = oakMap.getRefApiUid();
			List<TsmpApi> apiList = getTsmpApiDao().findByApiUid(apiUid);
			if (apiList != null && !apiList.isEmpty()) {
				TsmpApi api = apiList.get(0);
				soakem_apiNameList.add(api.getApiName());
			}
		}
		apiMapping.put("apiNames", soakem_apiNameList);
		apiMappings.add(apiMapping);
		
		return apiMappings;
	}
	
	private String getTemplate(String code) {
		List<TsmpDpMailTplt> soakem_list = getTsmpDpMailTpltCacheProxy().findByCode(code);
		if (soakem_list != null && !soakem_list.isEmpty()) {
			return soakem_list.get(0).getTemplateTxt();
		}
		return null;
	}
	
	protected DeleteExpiredMailJob deleteExpiredMail() {
		DeleteExpiredMailJob job = (DeleteExpiredMailJob) getCtx().getBean("deleteExpiredMailJob");
		getJobHelper().add(job);
		return job;
	}
	
	private String getDateTime(Long timeInMillis) {
		if(timeInMillis == null) {
			return "";
		}
		Date dt = new Date(timeInMillis);
		String dtStr = DateTimeUtil.dateTimeToString(dt, DateTimeFormatEnum.西元年月日).orElseThrow(TsmpDpAaRtnCode._1295::throwing);//yyyy-MM-dd
		return dtStr;
	}

	protected String getTsmpEdition() {
		String tsmpEdition = getTsmpSettingService().getVal_TSMP_LICENSE_KEY();
		return tsmpEdition;
	}

	protected LicenseUtilBase getLicenseUtil(){
		return this.licenseUtil;
	}
	
	protected String getSendTime() {
		return this.sendTime;
	}

	protected TsmpOpenApiKeyDao getTsmpOpenApiKeyDao() {
		return this.tsmpOpenApiKeyDao;
	}
	
	protected TsmpOpenApiKeyMapDao getTsmpOpenApiKeyMapDao() {
		return this.tsmpOpenApiKeyMapDao;
	}
	
	protected MailHelper getMailHelper() {
		return this.mailHelper;
	}

	protected ApplicationContext getCtx() {
		return this.ctx;
	}
	
	protected JobHelper getJobHelper() {
		return this.jobHelper;
	}
	
	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}
	
	protected TsmpClientDao getTsmpClientDao() {
		return this.tsmpClientDao;
	}
 
	protected TsmpSettingService getTsmpSettingService() {
		return this.tsmpSettingService;
	}

	protected TsmpDpMailTpltCacheProxy getTsmpDpMailTpltCacheProxy() {
		return this.tsmpDpMailTpltCacheProxy;
	}

}
