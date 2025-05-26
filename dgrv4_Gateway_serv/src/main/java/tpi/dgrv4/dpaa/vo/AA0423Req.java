package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0423Req {
	private List<String> labelList;
	private String targetSite;

	public List<String> getLabelList() {
		return labelList;
	}

	public String getTargetSite() {
		return targetSite;
	}

	public void setLabelList(List<String> labelList) {
		this.labelList = labelList;
	}

	public void setTargetSite(String targetSite) {
		this.targetSite = targetSite;
	}
}
