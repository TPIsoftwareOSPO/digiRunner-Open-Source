package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0417Resp {

	/** 節點名稱清單 from TSMP_NODE.node */
	private List<String> nodeList;

	@Override
	public String toString() {
		return "AA0417Resp [nodeList=" + nodeList + "]";
	}

	public List<String> getNodeList() {
		return nodeList;
	}

	public void setNodeList(List<String> nodeList) {
		this.nodeList = nodeList;
	}

}
