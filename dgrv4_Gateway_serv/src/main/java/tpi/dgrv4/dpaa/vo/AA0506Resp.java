package tpi.dgrv4.dpaa.vo;

public class AA0506Resp {

	/** Kibana主機的Port*/
	private Integer rpport;
	
	/** 報表ID*/
	private String reportID;

	/** 報表Kibana URL*/
	private String reportUrl;
	/** 報表Kibana 路由 */
	private String rpContentPath;
		
	private String reportType;
	
	public Integer getRpport() {
		return rpport;
	}

	public void setRpport(Integer rpport) {
		this.rpport = rpport;
	}

	public String getReportID() {
		return reportID;
	}

	public void setReportID(String reportID) {
		this.reportID = reportID;
	}

	public String getReportUrl() {
		return reportUrl;
	}

	public void setReportUrl(String reportUrl) {
		this.reportUrl = reportUrl;
	}

	/**
	 * @return the rpContentPath
	 */
	public String getRpContentPath() {
		return rpContentPath;
	}

	/**
	 * @param rpContentPath the rpContentPath to set
	 */
	public void setRpContentPath(String rpContentPath) {
		this.rpContentPath = rpContentPath;
	}

	public String getReportType() {
		return reportType;
	}

	public void setReportType(String reportType) {
		this.reportType = reportType;
	}

	@Override
	public String toString() {
		return "AA0506Resp [rpport=" + rpport + ", reportID=" + reportID + ", reportUrl=" + reportUrl + ",rpContentPath=" + rpContentPath + ",reportType=" + reportType + "]";
	}
	

}
