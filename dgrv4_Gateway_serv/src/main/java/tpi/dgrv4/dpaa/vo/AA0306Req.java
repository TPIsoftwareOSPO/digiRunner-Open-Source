package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0306Req {

	private List<AA0306ItemReq> apiList;
	private String revokeFlag;

	public List<AA0306ItemReq> getApiList() {
		return apiList;
	}

	public void setApiList(List<AA0306ItemReq> apiList) {
		this.apiList = apiList;
	}

	public String getRevokeFlag() {
		return revokeFlag;
	}

	public void setRevokeFlag(String revokeFlag) {
		this.revokeFlag = revokeFlag;
	}

}
