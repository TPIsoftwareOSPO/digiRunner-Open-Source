package tpi.dgrv4.common.utils.autoInitSQL.vo;

public class TsmpReportUrlVo {
	
	private String reportId;

	private String timeRange;

	private String reportUrl;


	/* constructors */
	public TsmpReportUrlVo() {}

	
	/* methods */
	@Override
	public String toString() {
		return "TsmpReportUrl [reportId=" + reportId + ", timeRange=" + timeRange + ", reportUrl=" + reportUrl + "]";
	}


	/* getters and setters */
	public String getReportId() {
		return reportId;
	}
	
	public void setReportId(String reportId) {
		this.reportId = reportId;
	}
	
	public String getTimeRange() {
		return timeRange;
	}
	
	public void setTimeRange(String timeRange) {
		this.timeRange = timeRange;
	}
	
	public String getReportUrl() {
		return reportUrl;
	}
	
	public void setReportUrl(String reportUrl) {
		this.reportUrl = reportUrl;
	}
	
}
