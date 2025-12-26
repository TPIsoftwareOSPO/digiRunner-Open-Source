package tpi.dgrv4.gateway.vo;

import java.util.List;

public class DPB0115Req {

	/** 欲查詢的角色ID清單 */
	private List<String> roleIdList;

	/** 欲查詢的交易代碼清單 */
	private List<String> txIdList;

	public List<String> getRoleIdList() {
		return roleIdList;
	}

	public void setRoleIdList(List<String> roleIdList) {
		this.roleIdList = roleIdList;
	}

	public List<String> getTxIdList() {
		return txIdList;
	}

	public void setTxIdList(List<String> txIdList) {
		this.txIdList = txIdList;
	}

}
