package tpi.dgrv4.dpaa.service.composer;

import java.util.List;

public class ComposerFindNodesResp {

	/** 回傳資料 */
	private List<ComposerFindNodesRespItem> data;

	public List<ComposerFindNodesRespItem> getData() {
		return data;
	}

	public void setData(List<ComposerFindNodesRespItem> data) {
		this.data = data;
	}

}