package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0006Resp {

	/** 清單 */
	private List<DPB0006Client> clientList;

	public DPB0006Resp() {}

	public List<DPB0006Client> getClientList() {
		return clientList;
	}

	public void setClientList(List<DPB0006Client> clientList) {
		this.clientList = clientList;
	}

	@Override
	public String toString() {
		return "DPB0006Resp [clientList=" + clientList + "]\n";
	}
}
