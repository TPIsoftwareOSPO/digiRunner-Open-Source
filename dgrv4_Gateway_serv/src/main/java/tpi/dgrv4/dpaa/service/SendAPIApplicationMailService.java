package tpi.dgrv4.dpaa.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import tpi.dgrv4.dpaa.component.TsmpMailEventBuilder;
import tpi.dgrv4.dpaa.component.job.DPB0071MailJob;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpApplyStatus;
import tpi.dgrv4.common.constant.TsmpDpModule;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.jpql.TsmpDpApiAuth2;
import tpi.dgrv4.entity.entity.jpql.TsmpDpMailTplt;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpDpMailTpltDao;
import tpi.dgrv4.escape.MailHelper;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class SendAPIApplicationMailService {

	private TPILogger logger = TPILogger.tl;

	private ApplicationContext ctx;
	private JobHelper jobHelper;
	private ServiceConfig serviceConfig;
	private TsmpDpMailTpltDao tsmpDpMailTpltDao;
	private TsmpApiDao tsmpApiDao;
	private TsmpSettingService tsmpSettingService;

	private String sendTime;
	
	@Autowired
	public SendAPIApplicationMailService(ApplicationContext ctx, JobHelper jobHelper, ServiceConfig serviceConfig,
			TsmpDpMailTpltDao tsmpDpMailTpltDao, TsmpApiDao tsmpApiDao, TsmpSettingService tsmpSettingService) {
		super();
		this.ctx = ctx;
		this.jobHelper = jobHelper;
		this.serviceConfig = serviceConfig;
		this.tsmpDpMailTpltDao = tsmpDpMailTpltDao;
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpSettingService = tsmpSettingService;
	}

	private TsmpMailEvent getTsmpMailEvent(TsmpClient client, List<TsmpDpApiAuth2> authList //
			, TsmpAuthorization authorization, String applyStatus) {
		if (client == null) {
			this.logger.debug("Missing client!");
			return null;
		}
		String clientId = client.getClientId();
		String recipients = client.getEmails();
		if (recipients == null || recipients.isEmpty()) {
			this.logger.debug(String.format("Client %s has empty emails!", clientId));
			return null;
		}

		String subject = null;
		String body = null;
		String templateKey = "";
		if (TsmpDpApplyStatus.PASS.value().equals(applyStatus)) {
			subject = getTemplate("subject.api-pass");
			templateKey = "body.api-pass";
		} else if (TsmpDpApplyStatus.FAIL.value().equals(applyStatus)) {
			subject = getTemplate("subject.api-fail");
			templateKey = "body.api-fail";
		}
		
		body = getTemplate(templateKey);
		if (subject == null || body == null) {
			return null;
		}

		Map<String, String> subjectParams = getSubjectParams(applyStatus);
		if (subjectParams == null || subjectParams.isEmpty()) {
			this.logger.debug(String.format("Client %s has empty subject params!", clientId));
			return null;
		}

		Map<String, String> bodyParams = getBodyParams(client, authList, applyStatus);
		if (bodyParams == null || bodyParams.isEmpty()) {
			this.logger.debug(String.format("Client %s has empty body params!", clientId));
			return null;
		}

		final String title = MailHelper.buildContent(subject, subjectParams);
		final String content = MailHelper.buildContent(body, bodyParams);
		this.logger.debug("Email title = " + title);
		this.logger.debug("Email content = " + content);
		return new TsmpMailEventBuilder() //
		.setSubject(title)
		.setContent(content)
		.setRecipients(recipients)
		.setCreateUser(authorization.getUserName())
		.setRefCode(templateKey)
		.build();
	}
	
	public DPB0071MailJob sendEmail(TsmpClient client, List<TsmpDpApiAuth2> authList, TsmpAuthorization auth, String applyStatus, Long reqOrdermId) {
		// 使用 Job 寫入 APPT_JOB Table & 建立 Mail 檔案, 由排程來寄信

		TsmpMailEvent mailEvent = getTsmpMailEvent(client, authList, auth, applyStatus);

		DPB0071MailJob job = getDPB0071MailJob(auth, Arrays.asList(mailEvent),getSendTime(),reqOrdermId);
		getJobHelper().add(job);
		return job;
	}
	
	protected DPB0071MailJob getDPB0071MailJob(TsmpAuthorization auth, List<TsmpMailEvent> mailEvents, String sendTime,
			Long reqOrdermId) {
		return (DPB0071MailJob) getCtx().getBean("dpb0071MailJob", auth, mailEvents, sendTime, reqOrdermId);
	}
	
	
	
	protected JobHelper getJobHelper() {
		return this.jobHelper;
	}
	
	private String getDataList(List<TsmpDpApiAuth2> authList) {
		StringBuffer dataList = new StringBuffer();

		String rowTemplate = getTemplate("body.api-pass.list");
		Map<String, String> rowParams = null;
		for(TsmpDpApiAuth2 auth : authList) {
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
			fillRowParams(rowParams, auth);
			dataList.append(MailHelper.buildContent(rowTemplate, rowParams));
		}

		return dataList.toString();
	}
	
	protected String getSendTime() {
		this.sendTime = this.getTsmpSettingService().getVal_MAIL_SEND_TIME();// 多久後寄發Email(ms)
		return this.sendTime;
	}

	private String getTemplate(String code) {
		List<TsmpDpMailTplt> list = getTsmpDpMailTpltDao().findByCode(code);
		if (list != null && !list.isEmpty()) {
			return list.get(0).getTemplateTxt();
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
			TsmpApi api = apiList.get(0);
			params.put("apiName", api.getApiName());
			params.put("apiDesc", api.getApiDesc());
			params.put("apiKey", api.getApiKey());

			/*
			TsmpApiModule module = getModule(api.getModuleName());
			if (module != null) {
				params.put("moduleVersion", module.getModuleVersion());
				params.put("moduleName", module.getModuleName());
			}
			*/
			params.put("moduleName", api.getModuleName());
		}
	}

	private TsmpApi getApi(String apiUid) {
		List<TsmpApi> apiList = getTsmpApiDao().findByApiUid(apiUid);
		if (apiList == null || apiList.isEmpty()) {
			return null;
		}
		return apiList.get(0);
	}
	
	protected TsmpDpMailTpltDao getTsmpDpMailTpltDao() {
		return this.tsmpDpMailTpltDao;
	}
	
	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}
	
	
	
	private Map<String, String> getBodyParams(TsmpClient client, List<TsmpDpApiAuth2> authList //
			, String applyStatus) {
		String clientName = client.getClientName();
		if (clientName == null || clientName.isEmpty()) {
			return null;
		}

		String now = "";
		Optional<String> opt = DateTimeUtil.dateTimeToString(DateTimeUtil.now(), DateTimeFormatEnum.西元年月日時分);
		if (opt.isPresent()) {
			now = opt.get();
		}

		String dataList = getDataList(authList);

		Map<String, String> emailParams = new HashMap<>();
		emailParams.put("clientName", clientName);
		emailParams.put("data-list", dataList);
		if (TsmpDpApplyStatus.FAIL.value().equals(applyStatus)) {
			emailParams.put("serviceMail", getTsmpSettingService().getVal_MAIL_BODY_API_FAIL_SERVICE_MAIL());
			emailParams.put("serviceTel", getTsmpSettingService().getVal_MAIL_BODY_API_FAIL_SERVICE_TEL());
		}
		emailParams.put("projectName", TsmpDpModule.DP.getChiDesc());
		emailParams.put("date", now);
		return emailParams;
	}
	
	protected ApplicationContext getCtx() {
		return this.ctx;
	}
	
	private Map<String, String> getSubjectParams(String applyStatus) {
		Map<String, String> emailParams = new HashMap<>();
		emailParams.put("projectName", TsmpDpModule.DP.getChiDesc());
		return emailParams;
	}
	
	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}
	
	protected TsmpSettingService getTsmpSettingService() {
		return this.tsmpSettingService;
	}
}
