package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0806Resp {
	private List<AA0806HostInfo> hostInfoList;

	public List<AA0806HostInfo> getHostInfoList() {
		return hostInfoList;
	}

	public void setHostInfoList(List<AA0806HostInfo> hostInfoList) {
		this.hostInfoList = hostInfoList;
	}

	@Override
	public String toString() {
		return "AA0806Resp [hostInfoList=" + hostInfoList + "]";
	}
	
}
