package tpi.dgrv4.dpaa.service;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpRegStatus;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.dpaa.component.TsmpMailEventBuilder;
import tpi.dgrv4.dpaa.component.job.DPB0071MailJob;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpModule;
import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpDpClientext;
import tpi.dgrv4.entity.entity.jpql.TsmpDpMailTplt;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpDpMailTpltDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.escape.MailHelper;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class SendClientRegMailService {

	private TPILogger logger = TPILogger.tl;

	private JobHelper jobHelper;
	private ApplicationContext ctx;
	private TsmpUserDao tsmpUserDao;
	private TsmpClientDao tsmpClientDao;
	private TsmpDpMailTpltDao tsmpDpMailTpltDao;
	private TsmpSettingService tsmpSettingService;
	
	private String sendTime;

	@Autowired
	public SendClientRegMailService(JobHelper jobHelper, ApplicationContext ctx, TsmpUserDao tsmpUserDao,
			TsmpClientDao tsmpClientDao, TsmpDpMailTpltDao tsmpDpMailTpltDao, TsmpSettingService tsmpSettingService) {
		super();
		this.jobHelper = jobHelper;
		this.ctx = ctx;
		this.tsmpUserDao = tsmpUserDao;
		this.tsmpClientDao = tsmpClientDao;
		this.tsmpDpMailTpltDao = tsmpDpMailTpltDao;
		this.tsmpSettingService = tsmpSettingService;
	}

	@PostConstruct
	public void init() {
	}

	public DPB0071MailJob sendEmail(TsmpDpClientext ext, String regStatus, TsmpAuthorization auth, Long reqOrdermId) {
		// 使用 Job 寫入 APPT_JOB Table & 建立 Mail 檔案, 由排程來寄信

		TsmpMailEvent mailEvent = getTsmpMailEvent(ext, auth, regStatus);

		DPB0071MailJob job = getDPB0071MailJob(auth, Arrays.asList(mailEvent),getSendTime(), reqOrdermId);
		getJobHelper().add(job);
		return job;
	}

	protected DPB0071MailJob getDPB0071MailJob(TsmpAuthorization auth, List<TsmpMailEvent> mailEvents, String sendTime,
			Long reqOrdermId) {
		return (DPB0071MailJob) getCtx().getBean("dpb0071MailJob", auth, mailEvents, sendTime, reqOrdermId);
	}

	protected ApplicationContext getCtx() {
		return this.ctx;
	}

	protected JobHelper getJobHelper() {
		return this.jobHelper;
	}

	protected TsmpUserDao getTsmpUserDao() {
		return this.tsmpUserDao;
	}

	protected String getSendTime() {
		this.sendTime = this.getTsmpSettingService().getVal_MAIL_SEND_TIME();// 多久後寄發Email(ms)
		return this.sendTime;
	}

	public TsmpMailEvent getTsmpMailEvent(TsmpDpClientext ext, TsmpAuthorization authorization //
			, String regStatus) {
		TsmpClient client = getClient(ext.getClientId());
		if (client == null) {
			this.logger.debug(String.format("TsmpDpClientext: clientId=%s missing client!", ext.getClientId()));
			return null;
		}

		String recipients = client.getEmails();
		if (recipients == null || recipients.isEmpty()) {
			this.logger.debug(String.format("TsmpDpClientext: clientId=%s empty emails!", ext.getClientId()));
			return null;
		}

		Map<String, String> subjectParams = getSubjectParams();
		if (subjectParams == null || subjectParams.isEmpty()) {
			this.logger.debug(String.format("TsmpDpClientext: clientId=%s empty subject params!", ext.getClientId()));
			return null;
		}

		Map<String, String> bodyParams = getBodyParams(ext, client, regStatus);
		if (bodyParams == null || bodyParams.isEmpty()) {
			this.logger.debug(String.format("TsmpDpClientext: clientId=%s empty body params!", ext.getClientId()));
			return null;
		}

		String templateKey = "subject.member-pass";
		String subject = getTemplate(templateKey);
		if (StringUtils.isEmpty(subject == null)) {
			this.logger.error(String.format("Missing template \"%s\", didn't send email.", templateKey));
			return null;
		}

		if (TsmpDpRegStatus.PASS.value().equals(regStatus)) {
			templateKey = "body.member-pass";
		} else if (TsmpDpRegStatus.RETURN.value().equals(regStatus)) {
			templateKey = "body.member-fail";
		}
		String template = getTemplate(templateKey);
		if (StringUtils.isEmpty(template)) {
			this.logger.error(String.format("Missing template \"%s\", didn't send email.", templateKey));
			return null;
		}

		final String title = MailHelper.buildContent(subject, subjectParams);
		final String content = MailHelper.buildContent(template, bodyParams);
		this.logger.debug("Email title = " + title);
		this.logger.debug("Email content = " + content);
		return new TsmpMailEventBuilder() //
				.setSubject(title).setContent(content)
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

	protected TsmpClientDao getTsmpClientDao() {
		return this.tsmpClientDao;
	}

	private Map<String, String> getSubjectParams() {
		Map<String, String> params = new HashMap<>();
		params.put("projectName", TsmpDpModule.DP.getChiDesc());
		return params;
	}

	private Map<String, String> getBodyParams(TsmpDpClientext ext, TsmpClient client //
			, String regStatus) {
		Map<String, String> params = new HashMap<>();

		// 通過
		if (TsmpDpRegStatus.PASS.value().equals(regStatus)) {
			String clientName = client.getClientName();
			if (clientName == null || clientName.isEmpty()) {
				return null;
			}
			params.put("projectName", TsmpDpModule.DP.getChiDesc());
			params.put("clientId", client.getClientId());
			params.put("clientName", clientName);
			params.put("clientSd", convertObjectToDateString(client.getStartDate(),client.getTimeZone()));
			params.put("clientEd", convertObjectToDateString(client.getEndDate(),client.getTimeZone()));
			params.put("svcSt", convertObjectToTimeString(client.getStartTimePerDay(),client.getTimeZone()));
			params.put("svcEt", convertObjectToTimeString(client.getEndTimePerDay(),client.getTimeZone()));
			params.put("timeZone", nvl(client.getTimeZone()));
			params.put("apiQuota", nvl(client.getApiQuota()));
			params.put("tps", nvl(client.getTps()));
			params.put("cPriority", nvl(client.getcPriority()));
			// 退回
		} else if (TsmpDpRegStatus.RETURN.value().equals(regStatus)) {
			params.put("reviewRemark", ext.getReviewRemark());
			params.put("refReviewUser", ext.getRefReviewUser());
			params.put("updateDateTime", DateTimeUtil.dateTimeToString(ext.getUpdateDateTime(), DateTimeFormatEnum.西元年月日時分_2).orElse(""));
			params.put("projectName", TsmpDpModule.DP.getChiDesc());
		}

		return params;
	}

	private String nvl(Object obj, Function<Object, String> func) {
		if (obj == null) {
			return new String();
		}
		if (func == null) {
			return String.valueOf(obj);
		}
		return func.apply(obj);
	}

	private String convertObjectToDateString(Long datetime, String timezone) {
		if (datetime == null || !StringUtils.hasText(timezone)) {
			return "";
		}
		Date date = new Date(datetime);
		return DateTimeUtil.dateTimeToString(date, DateTimeFormatEnum.西元年月日_2, timezone).orElse("");
	}
	
	private String convertObjectToTimeString(Long datetime, String timezone) {
		if (datetime == null || !StringUtils.hasText(timezone)) {
			return "";
		}
		Date date = new Date(datetime);
		return DateTimeUtil.dateTimeToString(date, DateTimeFormatEnum.時分秒, timezone).orElse("");
	}

	private String nvl(Object obj) {
		return nvl(obj, null);
	}

	private String getTemplate(String code) {
		if (!StringUtils.isEmpty(code)) {
			List<TsmpDpMailTplt> list = getTsmpDpMailTpltDao().findByCode(code);
			if (list != null && !list.isEmpty()) {
				return list.get(0).getTemplateTxt();
			}
		}
		return null;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return this.tsmpSettingService;
	}
	
	protected TsmpDpMailTpltDao getTsmpDpMailTpltDao() {
		return this.tsmpDpMailTpltDao;
	}
}
