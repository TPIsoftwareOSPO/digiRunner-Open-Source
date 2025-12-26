package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0431Req {
	private List<AA0431ReqItem> apiList;
	private List<String> labelList;

	public List<AA0431ReqItem> getApiList() {
		return apiList;
	}

	public void setApiList(List<AA0431ReqItem> apiList) {
		this.apiList = apiList;
	}

	public List<String> getLabelList() {
		return labelList;
	}

	public void setLabelList(List<String> labelList) {
		this.labelList = labelList;
	}
}
