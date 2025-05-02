package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA1201Resp {
	private List<String> labels;
	private List<AA1201DataSetResp> datasets;
	private String xLable;
	private String reportName;

	public List<String> getLabels() {
		return labels;
	}

	public void setLabels(List<String> labels) {
		this.labels = labels;
	}

	public List<AA1201DataSetResp> getDatasets() {
		return datasets;
	}

	public void setDatasets(List<AA1201DataSetResp> datasets) {
		this.datasets = datasets;
	}

	public String getxLable() {
		return xLable;
	}

	public void setxLable(String xLable) {
		this.xLable = xLable;
	}

	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}
	
	

}
