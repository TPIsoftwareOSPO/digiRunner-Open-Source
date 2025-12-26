package tpi.dgrv4.dpaa.component.req;

import java.util.List;

public class DpReqServiceResp_D1 extends DpReqServiceResp {

	/** from tsmp_dp_req_orderd1.req_orderd1_id */
	private List<Long> reqOrderd1Ids;

	private List<Long> apiAuthIds;

	public DpReqServiceResp_D1() {
	}

	public List<Long> getReqOrderd1Ids() {
		return reqOrderd1Ids;
	}

	public void setReqOrderd1Ids(List<Long> reqOrderd1Ids) {
		this.reqOrderd1Ids = reqOrderd1Ids;
	}

	public List<Long> getApiAuthIds() {
		return apiAuthIds;
	}

	public void setApiAuthIds(List<Long> apiAuthIds) {
		this.apiAuthIds = apiAuthIds;
	}
	
}
