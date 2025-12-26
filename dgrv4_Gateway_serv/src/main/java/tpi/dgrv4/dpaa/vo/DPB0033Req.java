package tpi.dgrv4.dpaa.vo;

public class DPB0033Req {

	/** 父節點 */
	private Long siteParentId;	

	/** 節點名稱 */
	private String siteDesc;

	/** 網站連結 */
	private String siteUrl;

	public DPB0033Req() {}

	public Long getSiteParentId() {
		return siteParentId;
	}

	public void setSiteParentId(Long siteParentId) {
		this.siteParentId = siteParentId;
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
