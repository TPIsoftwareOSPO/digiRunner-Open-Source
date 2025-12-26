package tpi.dgrv4.entity.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import tpi.dgrv4.common.constant.ReportDateTimeRangeTypeEnum;
	
public class AA1201ReportDateTimeRange {
	private Date now;
	private ReportDateTimeRangeTypeEnum dateTimeRangeType;
	private Optional<LocalDate> opt_startDate;
	private Optional<LocalDate> opt_endDate;
	private Optional<Date> opt_startDateTime;
	private Optional<Date> opt_endDateTime;
	private Map<String, Object> params;
	private StringBuffer sb;

	public Date getNow() {
		return now;
	}

	public void setNow(Date now) {
		this.now = now;
	}

	public ReportDateTimeRangeTypeEnum getDateTimeRangeType() {
		return dateTimeRangeType;
	}

	public void setDateTimeRangeType(ReportDateTimeRangeTypeEnum dateTimeRangeType) {
		this.dateTimeRangeType = dateTimeRangeType;
	}

	public Optional<LocalDate> getOpt_startDate() {
		return opt_startDate;
	}

	public void setOpt_startDate(Optional<LocalDate> opt_startDate) {
		this.opt_startDate = opt_startDate;
	}

	public Optional<LocalDate> getOpt_endDate() {
		return opt_endDate;
	}

	public void setOpt_endDate(Optional<LocalDate> opt_endDate) {
		this.opt_endDate = opt_endDate;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	public StringBuffer getSb() {
		return sb;
	}

	public void setSb(StringBuffer sb) {
		this.sb = sb;
	}

	public Optional<Date> getOpt_startDateTime() {
		return opt_startDateTime;
	}

	public void setOpt_startDateTime(Optional<Date> opt_startDateTime) {
		this.opt_startDateTime = opt_startDateTime;
	}

	public Optional<Date> getOpt_endDateTime() {
		return opt_endDateTime;
	}

	public void setOpt_endDateTime(Optional<Date> opt_endDateTime) {
		this.opt_endDateTime = opt_endDateTime;
	}
	

}
