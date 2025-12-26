package tpi.dgrv4.dpaa.component.apptJob;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.entity.jpql.TsmpAlert;

import java.util.Date;
import java.util.Optional;

public class DpaaAlertDetectorJobParams {

	/* 監測開始時間, yyyy-MM-dd HH:mm:ss.SSS */
	private String startDt;

	private TsmpAlert tsmpAlert;

	public String getStartDt() {
		return startDt;
	}

	public void setStartDt(String startDt) {
		this.startDt = startDt;
	}

	@JsonIgnore
	public Optional<Date> getStartDate() {
		return DateTimeUtil.stringToDateTime(this.startDt, DateTimeFormatEnum.西元年月日時分秒毫秒);
	}

	@JsonIgnore
	public String saveStartDt(Date startDt) {
		this.startDt = DateTimeUtil.dateTimeToString(startDt, DateTimeFormatEnum.西元年月日時分秒毫秒).orElse(String.valueOf(TsmpDpAaRtnCode._1295));
		return this.startDt;
	}

	public TsmpAlert getTsmpAlert() {
		return tsmpAlert;
	}

	public void setTsmpAlert(TsmpAlert tsmpAlert) {
		this.tsmpAlert = tsmpAlert;
	}

}
