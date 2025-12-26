package tpi.dgrv4.dpaa.vo;

public class DPB0034Req {

	/** ID (流水號) */
	private Long siteId;	

	/** 節點名稱 */
	private String siteDesc;

	/** 網站連結 */
	private String siteUrl;

	public DPB0034Req() {}

	public Long getSiteId() {
		return siteId;
	}

	public void setSiteId(Long siteId) {
		this.siteId = siteId;
	}

	public String getSiteDesc() {
		return siteDesc;
	}

	public void setSiteDesc(String siteDesc) {
		this.siteDesc = siteDesc;
	}

	public String getSiteUrl() {
		return siteUrl;
	}

	public void setSiteUrl(String siteUrl) {
		this.siteUrl = siteUrl;
	}
	
}
