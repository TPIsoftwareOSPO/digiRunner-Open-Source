package tpi.dgrv4.dpaa.component.req;

import java.util.List;
import java.util.Map;

public class DpReqServiceResp_D2 extends DpReqServiceResp {

	/** Map<Long, List<reqOrderd2dId>> */
	private Map<Long, List<Long>> reqOrderd2Ids;

	/** Map<d2Id, fileId> */
	private Map<Long, Long> reqOrderd2FileIds;

	public DpReqServiceResp_D2() {
	}

	public Map<Long, List<Long>> getReqOrderd2Ids() {
		return reqOrderd2Ids;
	}

	public void setReqOrderd2Ids(Map<Long, List<Long>> reqOrderd2Ids) {
		this.reqOrderd2Ids = reqOrderd2Ids;
	}

	public Map<Long, Long> getReqOrderd2FileIds() {
		return reqOrderd2FileIds;
	}

	public void setReqOrderd2FileIds(Map<Long, Long> reqOrderd2FileIds) {
		this.reqOrderd2FileIds = reqOrderd2FileIds;
	}

}
