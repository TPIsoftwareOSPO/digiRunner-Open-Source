package tpi.dgrv4.dpaa.component.alert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.entity.jpql.TsmpAlert;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;

public abstract class DpaaAlertNotifierAbstract implements DpaaAlertNotifier {

	private TPILogger logger = TPILogger.tl;

	private ApptJobDispatcher apptJobDispatcher;
	private ObjectMapper objectMapper;

	@Autowired
	public DpaaAlertNotifierAbstract(ApptJobDispatcher apptJobDispatcher, ObjectMapper objectMapper) {
		super();
		this.apptJobDispatcher = apptJobDispatcher;
		this.objectMapper = objectMapper;
	}

	/**
	 * 為了讓告警留下紀錄, 故利用 ApptJob 執行告警通知
	 */
	@Override
	public void notice(DpaaAlertEvent dpaaAlertEvent) {
		String inParams = getInParams(dpaaAlertEvent);
		if (!StringUtils.hasLength(inParams)) {
			return;
		}
		
		TsmpDpApptJob job = new TsmpDpApptJob();
		String refItemNo = getRefItemNo();
		job.setRefItemNo(refItemNo);
		String refSubitemNo = getRefSubitemNo();
		job.setRefSubitemNo(refSubitemNo);
		job.setInParams(inParams);
		job.setStartDateTime(DateTimeUtil.now());	// 生效時間為現在, 表示立即生效
		String identifData = getIdentifData(dpaaAlertEvent);
		job.setIdentifData(identifData);
		job.setCreateUser("SYS");
		job = getApptJobDispatcher().addAndRefresh(job);
		this.logger.debug("告警排程已建立: " + job.getApptJobId());
	}

	protected String getInParams(DpaaAlertEvent dpaaAlertEvent) {
		try {
			return getObjectMapper().writeValueAsString(dpaaAlertEvent);
		} catch (Exception e) {
			this.logger.error("DpaaAlertEvent 轉型 JSON 失敗: alertId=" + dpaaAlertEvent.getEntity().getAlertId());
			return new String();
		}
	}

	protected String getIdentifData(DpaaAlertEvent dpaaAlertEvent) {
		TsmpAlert entity = dpaaAlertEvent.getEntity();
		Long alertId = entity.getAlertId();
		String alertType = dpaaAlertEvent.getAlertType();
		String alertName = entity.getAlertName();
		return String.format("alertId=%s, alertType=%s, alertName=%s", alertId, alertType, alertName);
	}

	protected ApptJobDispatcher getApptJobDispatcher() {
		return this.apptJobDispatcher;
	}

	protected ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}

	protected abstract String getRefItemNo();

	protected abstract String getRefSubitemNo();

}