package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0425RespIpAndSrcUrlList {
	private String ip;
	private List<AA0425RespPercentageAndSrcUrl> srcUrlList;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public List<AA0425RespPercentageAndSrcUrl> getSrcUrlList() {
		return srcUrlList;
	}

	public void setSrcUrlList(List<AA0425RespPercentageAndSrcUrl> srcUrlList) {
		this.srcUrlList = srcUrlList;
	}

}
