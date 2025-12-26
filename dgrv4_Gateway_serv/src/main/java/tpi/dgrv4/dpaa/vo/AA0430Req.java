package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0430Req {
	private List<AA0430ReqItem> apiList;
	private Boolean noOauth;

	public List<AA0430ReqItem> getApiList() {
		return apiList;
	}

	public void setApiList(List<AA0430ReqItem> apiList) {
		this.apiList = apiList;
	}

	public Boolean getNoOauth() {
		return noOauth;
	}

	public void setNoOauth(Boolean noOauth) {
		this.noOauth = noOauth;
	}

}
