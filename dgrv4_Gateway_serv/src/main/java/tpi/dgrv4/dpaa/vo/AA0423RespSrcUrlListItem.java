package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0423RespSrcUrlListItem {
	private String ip;
	private List<AA0423RespSrcUrlAndPercentageItem> srcUrlAndPercentageList;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public List<AA0423RespSrcUrlAndPercentageItem> getSrcUrlAndPercentageList() {
		return srcUrlAndPercentageList;
	}

	public void setSrcUrlAndPercentageList(List<AA0423RespSrcUrlAndPercentageItem> srcUrlAndPercentageList) {
		this.srcUrlAndPercentageList = srcUrlAndPercentageList;
	}

}
