package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0424IpAndSrcUrlList {
	private String ip;
	private List<AA0424SrcUrl> srcUrlList;
	
	public AA0424IpAndSrcUrlList() {
		
	}
	public AA0424IpAndSrcUrlList(AA0424IpAndSrcUrlList o) {
		this.ip = o.getIp();
		this.srcUrlList = o.getSrcUrlList();
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public List<AA0424SrcUrl> getSrcUrlList() {
		return srcUrlList;
	}

	public void setSrcUrlList(List<AA0424SrcUrl> srcUrlList) {
		this.srcUrlList = srcUrlList;
	}
}
