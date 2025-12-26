package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA1202Resp {
	private List<String> labels;
	private List<AA1202DataSetResp> datasets;
	private String xLable;
	private String yLable;
	private String reportName;

	public List<String> getLabels() {
		return labels;
	}

	public void setLabels(List<String> labels) {
		this.labels = labels;
	}

	public List<AA1202DataSetResp> getDatasets() {
		return datasets;
	}

	public void setDatasets(List<AA1202DataSetResp> datasets) {
		this.datasets = datasets;
	}

	public void setxLable(String xLable) {
		this.xLable = xLable;
	}
	
	public String getxLable() {
		return xLable;
	}

	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

	public String getyLable() {
		return yLable;
	}

	public void setyLable(String yLable) {
		this.yLable = yLable;
	}
	
	

}
