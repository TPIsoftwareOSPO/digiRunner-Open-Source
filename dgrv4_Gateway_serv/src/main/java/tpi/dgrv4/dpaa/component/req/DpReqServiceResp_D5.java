package tpi.dgrv4.dpaa.component.req;

import java.util.List;

public class DpReqServiceResp_D5 extends DpReqServiceResp {

	/** from tsmp_dp_req_orderd5.req_orderd5_id */
	private Long reqOrderd5Id;

	/** List<Long : reqOrderd5dId> */
	private List<Long> reqOrderd5dIds; 

	public Long getReqOrderd5Id() {
		return reqOrderd5Id;
	}

	public void setReqOrderd5Id(Long reqOrderd5Id) {
		this.reqOrderd5Id = reqOrderd5Id;
	}

	public List<Long> getReqOrderd5dIds() {
		return reqOrderd5dIds;
	}

	public void setReqOrderd5dIds(List<Long> reqOrderd5dIds) {
		this.reqOrderd5dIds = reqOrderd5dIds;
	}

}
