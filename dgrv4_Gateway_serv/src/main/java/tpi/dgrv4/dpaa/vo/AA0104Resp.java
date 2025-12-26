package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0104Resp {

	private List<AA0104List> roleInfoList;

	public List<AA0104List> getRoleInfoList() {
		return roleInfoList;
	}

	public void setRoleInfoList(List<AA0104List> roleInfoList) {
		this.roleInfoList = roleInfoList;
	}

	@Override
	public String toString() {
		return "AA0104Resp [roleInfoList=" + roleInfoList + "]";
	}

}
