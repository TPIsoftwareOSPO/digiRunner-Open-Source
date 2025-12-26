package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0228Resp {
	
	private List<AA0228GroupInfo> groupInfoList;

	public List<AA0228GroupInfo> getGroupInfoList() {
		return groupInfoList;
	}

	public void setGroupInfoList(List<AA0228GroupInfo> groupInfoList) {
		this.groupInfoList = groupInfoList;
	}

	@Override
	public String toString() {
		return "AA0228Resp [groupInfoList=" + groupInfoList + "]";
	}
	
	
}
