package tpi.dgrv4.dpaa.service.composer;

import java.util.List;

public class ComposerFindAppsReq {

	/** 篩選條件 */
	private List<ComposerFindAppsReqItem> criterias;

	public List<ComposerFindAppsReqItem> getCriterias() {
		return criterias;
	}

	public void setCriterias(List<ComposerFindAppsReqItem> criterias) {
		this.criterias = criterias;
	}

}