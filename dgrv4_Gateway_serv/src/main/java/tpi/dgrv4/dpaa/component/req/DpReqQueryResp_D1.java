package tpi.dgrv4.dpaa.component.req;

import java.util.List;

public class DpReqQueryResp_D1 {

	private String clientId;

	private String clientName;

	private String clientAlias;

	private List<DpReqQueryResp_D1d> apiList;

	public DpReqQueryResp_D1() {
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getClientAlias() {
		return clientAlias;
	}

	public void setClientAlias(String clientAlias) {
		this.clientAlias = clientAlias;
	}

	public List<DpReqQueryResp_D1d> getApiList() {
		return apiList;
	}

	public void setApiList(List<DpReqQueryResp_D1d> apiList) {
		this.apiList = apiList;
	}

}
