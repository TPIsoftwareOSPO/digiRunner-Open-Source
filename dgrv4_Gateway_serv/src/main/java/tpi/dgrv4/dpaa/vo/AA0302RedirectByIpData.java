package tpi.dgrv4.dpaa.vo;

public class AA0302RedirectByIpData {
	private String ipForRedirect;
	private AA0302Trunc ipSrcUrl;

	public AA0302Trunc getIpSrcUrl() {
		return ipSrcUrl;
	}

	public void setIpSrcUrl(AA0302Trunc ipSrcUrl) {
		this.ipSrcUrl = ipSrcUrl;
	}

	public String getIpForRedirect() {
		return ipForRedirect;
	}

	public void setIpForRedirect(String ipForRedirect) {
		this.ipForRedirect = ipForRedirect;
	}
}
