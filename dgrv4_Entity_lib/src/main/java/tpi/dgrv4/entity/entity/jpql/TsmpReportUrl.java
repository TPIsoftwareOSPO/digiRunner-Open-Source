package tpi.dgrv4.entity.entity.jpql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "tsmp_report_url")
@IdClass(value = TsmpReportUrlId.class)
public class TsmpReportUrl {
	
	@Id
	@Column(name = "REPORT_ID")
	private String reportId;

	@Id
	@Column(name = "TIME_RANGE")
	private String timeRange;

	@Column(name = "REPORT_URL")
	private String reportUrl;
	
	/* constructors */
	public TsmpReportUrl() {}

	
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
