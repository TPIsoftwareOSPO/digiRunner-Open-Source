package tpi.dgrv4.dpaa.vo;

import java.util.LinkedList;
import java.util.List;

import tpi.dgrv4.gateway.vo.ComposerInfoData;

public class DPB0144Resp {

	// monitor audit
	private DPB0144RespAudit audit;
	// monitor summary
	private DPB0144RespSummary summary;

	private List<DPB0144RespLiveNode> liveNodeList;

	LinkedList<ComposerInfoData> liveComposerList;

	private List<DPB0144RespLostNode> lostNodeList;

	private String period;

	public DPB0144RespAudit getAudit() {
		return audit;
	}

	public void setAudit(DPB0144RespAudit audit) {
		this.audit = audit;
	}

	public DPB0144RespSummary getSummary() {
		return summary;
	}

	public void setSummary(DPB0144RespSummary summary) {
		this.summary = summary;
	}

	public List<DPB0144RespLiveNode> getLiveNodeList() {
		return liveNodeList;
	}

	public void setLiveNodeList(List<DPB0144RespLiveNode> liveNodeList) {
		this.liveNodeList = liveNodeList;
	}

	public List<DPB0144RespLostNode> getLostNodeList() {
		return lostNodeList;
	}

	public void setLostNodeList(List<DPB0144RespLostNode> lostNodeList) {
		this.lostNodeList = lostNodeList;
	}

	public LinkedList<ComposerInfoData> getLiveComposerList() {
		return liveComposerList;
	}

	public void setLiveComposerList(LinkedList<ComposerInfoData> liveComposerList) {
		this.liveComposerList = liveComposerList;
	}

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

}
