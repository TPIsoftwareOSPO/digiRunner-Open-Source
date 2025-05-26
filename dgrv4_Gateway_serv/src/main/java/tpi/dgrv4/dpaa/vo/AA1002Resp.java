package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA1002Resp {

	private List<AA1002List> orgList;

	public List<AA1002List> getOrgList() {
		return orgList;
	}

	public void setOrgList(List<AA1002List> orgList) {
		this.orgList = orgList;
	}

	@Override
	public String toString() {
		return "AA1002Resp [orgList=" + orgList + "]";
	}
	
	
}
