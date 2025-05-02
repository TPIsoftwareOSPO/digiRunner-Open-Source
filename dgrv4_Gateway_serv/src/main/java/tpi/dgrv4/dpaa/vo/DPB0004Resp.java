package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0004Resp {

	/** 清單 */
	private List<DPB0004Client> clientList;

	public DPB0004Resp() {}

	public List<DPB0004Client> getClientList() {
		return clientList;
	}

	public void setClientList(List<DPB0004Client> clientList) {
		this.clientList = clientList;
	}
	
}
