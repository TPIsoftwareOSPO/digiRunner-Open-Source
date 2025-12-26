package tpi.dgrv4.dpaa.vo;

public class ConvertTimeUnitRst {

	/** 時間 */
	private Integer time;

	/** 時間代碼(單位) */
	private String timeUnit;

	/** 時間名稱(單位) */
	private String timeUnitName;
	
	/** 粗略時間 */
	private String approximateTimeUnit;

	public Integer getTime() {
		return time;
	}

	public void setTime(Integer time) {
		this.time = time;
	}

	public String getTimeUnit() {
		return timeUnit;
	}

	public void setTimeUnit(String timeUnit) {
		this.timeUnit = timeUnit;
	}

	public String getTimeUnitName() {
		return timeUnitName;
	}

	public void setTimeUnitName(String timeUnitName) {
		this.timeUnitName = timeUnitName;
	}

	public String getApproximateTimeUnit() {
		return approximateTimeUnit;
	}

	public void setApproximateTimeUnit(String approximateTimeUnit) {
		this.approximateTimeUnit = approximateTimeUnit;
	}

	@Override
	public String toString() {
		return "ConvertTimeUnitRst [time=" + time + ", timeUnit=" + timeUnit + ", timeUnitName=" + timeUnitName
				+ ", approximateTimeUnit=" + approximateTimeUnit + "]";
	}

}
