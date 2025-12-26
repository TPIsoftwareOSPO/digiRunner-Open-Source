package tpi.dgrv4.dpaa.service.composer;

import java.util.List;

public class ComposerFindNodesRespItem {

	/** nodes.appId */
	private String appId;

	/** 
	 * nodes 文件<br>
	 * ex: ["{ \"_id\" : { \"$oid\" : \"5c65225acccc70785ce0d552\"} , \"wires\" : [ ]}", "{ \"_id\" : { \"$oid\" : \"5c65225acccc70785ce0d552\"} , \"wires\" : [ ]}"]
	 */
	private List<String> nodes;

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public List<String> getNodes() {
		return nodes;
	}

	public void setNodes(List<String> nodes) {
		this.nodes = nodes;
	}

}