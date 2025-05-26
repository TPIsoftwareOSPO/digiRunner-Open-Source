package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0424NewSrcUrlItem {
	private Boolean isSuccess;
	private String ip;
	private List<AA0424SrcUrl> srcUrlList;

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

	public Boolean getIsSuccess() {
		return isSuccess;
	}

	public void setIsSuccess(Boolean isSuccess) {
		this.isSuccess = isSuccess;
	}

}
