package tpi.dgrv4.dpaa.vo;

public class AA0506Report {

	private String reportID;
	
	private String reportUrl;

	public String getReportID() {
		return reportID;
	}

	public void setReportID(String reportID) {
		this.reportID = reportID;
	}

	public void setReportUrl(String reportUrl) {
		this.reportUrl = reportUrl;
	}
	
	public String getReportUrl() {
		return reportUrl;
	}

	@Override
	public String toString() {
		return "AA0506Report [reportID=" + reportID + ", reportUrl=" + reportUrl + "]";
	}

	
}
