package tpi.dgrv4.dpaa.service.composer;

import java.util.List;

public class ComposerFindNodesReq {

	/** 篩選條件 */
	private List<String> criterias;

	public List<String> getCriterias() {
		return criterias;
	}

	public void setCriterias(List<String> criterias) {
		this.criterias = criterias;
	}
	
}