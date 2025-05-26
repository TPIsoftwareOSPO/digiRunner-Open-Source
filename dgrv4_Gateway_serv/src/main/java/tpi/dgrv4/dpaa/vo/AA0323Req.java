package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0323Req {
	private String composerID;

	private List<AA0323Flow> flows;

	public String getComposerID() {
		return composerID;
	}

	public void setComposerID(String composerID) {
		this.composerID = composerID;
	}

	public List<AA0323Flow> getFlows() {
		return flows;
	}

	public void setFlows(List<AA0323Flow> flows) {
		this.flows = flows;
	}

}
