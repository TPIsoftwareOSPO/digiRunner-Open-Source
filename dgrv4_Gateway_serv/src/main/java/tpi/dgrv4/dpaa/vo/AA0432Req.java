package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0432Req {
	private List<AA0432ReqItem> apiList;
	private String failDiscoveryPolicy;
	private String failHandlePolicy;

	public List<AA0432ReqItem> getApiList() {
		return apiList;
	}

	public void setApiList(List<AA0432ReqItem> apiList) {
		this.apiList = apiList;
	}

	public String getFailDiscoveryPolicy() {
		return failDiscoveryPolicy;
	}

	public void setFailDiscoveryPolicy(String failDiscoveryPolicy) {
		this.failDiscoveryPolicy = failDiscoveryPolicy;
	}

	public String getFailHandlePolicy() {
		return failHandlePolicy;
	}

	public void setFailHandlePolicy(String failHandlePolicy) {
		this.failHandlePolicy = failHandlePolicy;
	}

}
