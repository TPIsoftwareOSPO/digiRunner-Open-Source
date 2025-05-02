package tpi.dgrv4.dpaa.component.apptJob;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.utils.DateTimeUtil;

public class DpaaAlertDetectorJobKeywordParams extends DpaaAlertDetectorJobParams {

	/* 上次觸警時間, 無值表示從未觸警, yyyy-MM-dd HH:mm:ss.SSS */
	private String lastAlertDt;

	private Map<String, Object> alertPayload;

	public String getLastAlertDt() {
		return lastAlertDt;
	}

	public void setLastAlertDt(String lastAlertDt) {
		this.lastAlertDt = lastAlertDt;
	}

	public Map<String, Object> getAlertPayload() {
		return alertPayload;
	}

	public void setAlertPayload(Map<String, Object> alertPayload) {
		this.alertPayload = alertPayload;
	}

	@JsonIgnore
	public String saveLastAlertDt(Date lastAlertDt) {
		this.lastAlertDt = DateTimeUtil.dateTimeToString(lastAlertDt, DateTimeFormatEnum.西元年月日時分秒毫秒).orElse(null);
		return this.lastAlertDt;
	}

	@JsonIgnore
	public void addToPayload(String key, Object val) {
		if (this.alertPayload == null) {
			this.alertPayload = new HashMap<>();
		}
		this.alertPayload.put(key, val);
	}

	@JsonIgnore
	public void clearAlertRecord() {
		this.lastAlertDt = null;
		if (this.alertPayload != null) {
			this.alertPayload.clear();
		}
		this.alertPayload = null;
	}

}
