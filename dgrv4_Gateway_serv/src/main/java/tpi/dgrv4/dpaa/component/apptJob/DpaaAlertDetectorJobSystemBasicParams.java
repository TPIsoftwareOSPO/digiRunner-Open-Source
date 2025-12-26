package tpi.dgrv4.dpaa.component.apptJob;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.DateTimeUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DpaaAlertDetectorJobSystemBasicParams extends DpaaAlertDetectorJobParams {

	// 最近偵測的結果(有異動才會記錄)
	private Boolean recentAlert;

	/* 最近偵測到不同告警結果的時間, yyyy-MM-dd HH:mm:ss.SSS */
	private String recentDetectDt;

	private Map<String, Object> alertPayload;

	public Boolean isRecentAlert() {
		return recentAlert;
	}

	public void setRecentAlert(Boolean recentAlert) {
		this.recentAlert = recentAlert;
	}

	public String getRecentDetectDt() {
		return recentDetectDt;
	}

	public void setRecentDetectDt(String recentDetectDt) {
		this.recentDetectDt = recentDetectDt;
	}

	@JsonIgnore
	public Optional<Date> getRecentDetectDate() {
		return DateTimeUtil.stringToDateTime(this.recentDetectDt, DateTimeFormatEnum.西元年月日時分秒毫秒);
	}

	public Map<String, Object> getAlertPayload() {
		return alertPayload;
	}

	public void setAlertPayload(Map<String, Object> alertPayload) {
		this.alertPayload = alertPayload;
	}

	@JsonIgnore
	public void setDetectResult(boolean isAlert, Date detectDt) {
		this.recentAlert = isAlert;
		this.recentDetectDt = DateTimeUtil.dateTimeToString(detectDt, DateTimeFormatEnum.西元年月日時分秒毫秒).orElse(String.valueOf(TsmpDpAaRtnCode._1295));
	}

	@JsonIgnore
	public void addToPayload(String key, Object val) {
		if (this.alertPayload == null) {
			this.alertPayload = new HashMap<>();
		}
		this.alertPayload.put(key, val);
	}

}
