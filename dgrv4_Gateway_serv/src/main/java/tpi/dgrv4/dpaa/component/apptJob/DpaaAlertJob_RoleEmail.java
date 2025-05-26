package tpi.dgrv4.dpaa.component.apptJob;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.dpaa.component.TsmpMailEventBuilder;
import tpi.dgrv4.dpaa.component.alert.DpaaAlertEvent;
import tpi.dgrv4.dpaa.constant.TsmpDpMailType;
import tpi.dgrv4.dpaa.service.PrepareMailService;
import tpi.dgrv4.dpaa.service.TsmpSettingService;
import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.entity.jpql.TsmpAlert;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.component.job.appt.ApptJob;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;

@SuppressWarnings("serial")
public class DpaaAlertJob_RoleEmail extends ApptJob {

	private TsmpUserDao tsmpUserDao;
	private PrepareMailService prepareMailService;
	private ServiceConfig serviceConfig;
	private ObjectMapper objectMapper;
	private TsmpSettingService tsmpSettingService;

	@Autowired
	public DpaaAlertJob_RoleEmail(TsmpDpApptJob tsmpDpApptJob, TsmpUserDao tsmpUserDao,
			PrepareMailService prepareMailService, ServiceConfig serviceConfig, ObjectMapper objectMapper,
			TsmpSettingService tsmpSettingService, ApptJobDispatcher apptJobDispatcher, TsmpDpApptJobDao tsmpDpApptJobDao) {
		super(tsmpDpApptJob, TPILogger.tl, apptJobDispatcher, tsmpDpApptJobDao);
		this.tsmpUserDao = tsmpUserDao;
		this.prepareMailService = prepareMailService;
		this.serviceConfig = serviceConfig;
		this.objectMapper = objectMapper;
		this.tsmpSettingService = tsmpSettingService;
	}

	@Override
	public String runApptJob() throws Exception {
		String inParams = getTsmpDpApptJob().getInParams();
		DpaaAlertEvent dpaaAlertEvent = parseInParams(inParams);

		checkDpaaAlertEvent(dpaaAlertEvent);
		
		List<TsmpMailEvent> mailEvents = prepareMailEvents(dpaaAlertEvent);
		
		try {
			// 準備好資料, 以寫入排程
			Long alertId = dpaaAlertEvent.getEntity().getAlertId();
			String alertName = dpaaAlertEvent.getEntity().getAlertName();
			createMailSchedule(alertId, alertName, mailEvents);
		} catch (Exception e) {
			throw new Exception("建立寄件排程失敗", e);
		}

		return "SUCCESS";
	}

	protected DpaaAlertEvent parseInParams(String inParams) throws Exception {
		try {
			return getObjectMapper().readValue(inParams, DpaaAlertEvent.class);
		} catch (Exception e) {
			throw new Exception("參數錯誤, 無法轉型為 DpaaAlertEvent 物件");
		}
	}

	protected void checkDpaaAlertEvent(DpaaAlertEvent dpaaAlertEvent) throws Exception {
		TsmpAlert tsmpAlert = dpaaAlertEvent.getEntity();
		throwExceptionWhenNullOrEmpty(tsmpAlert, "TsmpAlert");
		throwExceptionWhenNullOrEmpty(tsmpAlert.getAlertId(), "alertId");
		throwExceptionWhenNullOrEmpty(tsmpAlert.getAlertName(), "alertName");
		throwExceptionWhenNullOrEmpty(tsmpAlert.getAlertMsg(), "alertMsg");
	}

	protected List<TsmpMailEvent> prepareMailEvents(DpaaAlertEvent dpaaAlertEvent) throws Exception {
		// 2021.07.24 一個收件者就要有一個對應的 TsmpMailEvent
		Collection<String> recipients = getRecipients(dpaaAlertEvent);
		
		List<TsmpMailEvent> mailEvents = new ArrayList<>();
		TsmpMailEvent mailEvent = null;
		for (String recipient : recipients) {
			mailEvent = prepareMailEvent(dpaaAlertEvent, recipient);
			mailEvents.add(mailEvent);
		}
		return mailEvents;
	}

	protected Collection<String> getRecipients(DpaaAlertEvent dpaaAlertEvent) throws Exception {
		TsmpAlert tsmpAlert = dpaaAlertEvent.getEntity();
		Long alertId = tsmpAlert.getAlertId();
		List<TsmpUser> users = getTsmpUserDao().queryByRoleAlert(alertId);
		if (CollectionUtils.isEmpty(users)) {
			throw new Exception("沒有可以通知告警的對象");
		}

		// 將所有使用者的 email 用逗號切割成 List<String>
		Collection<String> recipients = usersToRecipients(users);

		if (CollectionUtils.isEmpty(recipients)) {
			throw new Exception("沒有可以通知告警的電子郵件收件地址");
		}

		return recipients;
	}

	protected Collection<String> usersToRecipients(List<TsmpUser> users) {
		return users.stream() //
		.filter((u) -> StringUtils.hasText(u.getUserEmail())) //
		.map((user) -> user.getUserEmail().split(",")) //
		.collect( // 2022.07.21; 相同地址應合併
			() -> new LinkedHashSet<>(), //
			(allEmails, emailAry) -> allEmails.addAll(Arrays.asList(emailAry)), //
			(emails_1, emails_2) -> emails_1.addAll(emails_2)
		);
	}

	protected TsmpMailEvent prepareMailEvent(DpaaAlertEvent dpaaAlertEvent, String recipients) {
		TsmpAlert entity = dpaaAlertEvent.getEntity();
		String title = "";
		String content = "";
		if (entity != null) {
			title = entity.getAlertName();
			content = entity.getAlertMsg();
		}

		return new TsmpMailEventBuilder() //
		.setSubject(title)
		.setContent(content)
		.setRecipients(recipients)
		.setCreateUser("SYS")
		.setRefCode(" ")
		.build();
	}

	protected void createMailSchedule(Long alertId, String alertName, //
			List<TsmpMailEvent> mailEvents) throws Exception {
		String identif = String.format("alertId=%d,　alertName=%s", alertId, alertName);
		String sendTime = getTsmpSettingService().getVal_MAIL_SEND_TIME();	// 多久後寄發Email(ms)

		getPrepareMailService().createMailSchedule(mailEvents, identif
				, TsmpDpMailType.SAME.text(), sendTime);
	}

	private void throwExceptionWhenNullOrEmpty(Object input, String fieldName) throws Exception {
		if (ObjectUtils.isEmpty(input)) {
			throw new Exception("缺少必要參數: " + fieldName);
		}
	}

	protected TsmpSettingService getTsmpSettingService() {
		return this.tsmpSettingService;
	}
	
	protected TsmpUserDao getTsmpUserDao() {
		return this.tsmpUserDao;
	}

	protected PrepareMailService getPrepareMailService() {
		return this.prepareMailService;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}

}