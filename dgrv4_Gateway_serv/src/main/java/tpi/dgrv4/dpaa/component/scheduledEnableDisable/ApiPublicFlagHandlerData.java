package tpi.dgrv4.dpaa.component.scheduledEnableDisable;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.dpaa.constant.TsmpDpItem;
import tpi.dgrv4.dpaa.util.TimestampConverterUtil;

public class ApiPublicFlagHandlerData {

	private String apiStatus;
	private long apiEnableScheduledDate;
	private long apiDisableScheduledDate;

	private String status;
	private long enableScheduledDate;
	private long disableScheduledDate;

	public String getApiStatus() {
		return apiStatus;
	}

	public void setApiStatus(String apiStatus) {
		checkPublicFlag(apiStatus);
		this.apiStatus = apiStatus;
	}

	public long getApiEnableScheduledDate() {
		return apiEnableScheduledDate;
	}

	public void setApiEnableScheduledDate(long apiEnableScheduledDate) {
		this.apiEnableScheduledDate = apiEnableScheduledDate;
	}

	public long getApiDisableScheduledDate() {
		return apiDisableScheduledDate;
	}

	public void setApiDisableScheduledDate(long apiDisableScheduledDate) {
		this.apiDisableScheduledDate = apiDisableScheduledDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		checkPublicFlag(status);
		this.status = status;
	}

	public long getEnableScheduledDate() {
		return enableScheduledDate;
	}

	public void setEnableScheduledDate(long enableScheduledDate) {
		if (enableScheduledDate != 0) {
			isAfterToday(enableScheduledDate);
		}
		this.enableScheduledDate = enableScheduledDate;
	}

	public long getDisableScheduledDate() {
		return disableScheduledDate;
	}

	public void setDisableScheduledDate(long disableScheduledDate) {

		if (disableScheduledDate != 0) {
			isAfterToday(disableScheduledDate);
		}
		this.disableScheduledDate = disableScheduledDate;
	}

	private void checkPublicFlag(String s) {

		if (!TsmpDpItem.existsParam1InItemNo(TsmpDpItem.ENABLE_FLAG, s)) {
//			throw new IllegalArgumentException("It can only be set to ENABLE_FLAG_ENABLE or ENABLE_FLAG_DEACTIVATE.");
		}
	}

	/**
	 * 檢查給定的時間戳是否代表的日期在當前日期之後。 如果給定的時間戳代表的日期不在今天之後，則拋出一個自定義異常。
	 *
	 * @param time 需要檢查的時間戳
	 */
	private void isAfterToday(long time) {

		// 將傳入的時間戳轉換為只包含日期的時間戳
		time = TimestampConverterUtil.getDateOnlyTimestamp(time);
		// 獲取當前系統時間的只包含日期的時間戳
		long currentTime = TimestampConverterUtil.getDateOnlyTimestamp(System.currentTimeMillis());

		if (time <= currentTime) {
			throw TsmpDpAaRtnCode._1549.throwing();
		}
	}

}
