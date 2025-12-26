package tpi.dgrv4.dpaa.component.req;

import java.util.List;

public class DpReqServiceSaveDraftReq_D1 extends DpReqServiceSaveDraftReq {

	/** 欲申請使用API的用戶ID */
	private String _clientId;

	/** 申請的API */
	private List<String> apiUids;

	public DpReqServiceSaveDraftReq_D1() {
	}

	public String get_clientId() {
		return _clientId;
	}

	public void set_clientId(String _clientId) {
		this._clientId = _clientId;
	}

	public List<String> getApiUids() {
		return apiUids;
	}

	public void setApiUids(List<String> apiUids) {
		this.apiUids = apiUids;
	}

}
