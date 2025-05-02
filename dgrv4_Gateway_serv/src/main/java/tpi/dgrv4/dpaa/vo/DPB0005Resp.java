package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0005Resp {

	/** 多組Client ID->處理成功 size=0, 失敗時回傳失敗的clientId, 並pop於畫面 */
	private List<String> clientIds;

	public DPB0005Resp() {}

	public List<String> getClientIds() {
		return clientIds;
	}

	public void setClientIds(List<String> clientIds) {
		this.clientIds = clientIds;
	}
	
}
