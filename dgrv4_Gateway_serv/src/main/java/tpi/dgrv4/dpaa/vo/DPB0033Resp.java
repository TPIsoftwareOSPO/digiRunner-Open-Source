package tpi.dgrv4.dpaa.vo;

public class DPB0033Resp {

	/** ID (流水號) */
	private Long siteId;

	/** 父節點 */
	private Long siteParentId;

	/** 節點名稱 */
	private String siteDesc;

	/** 資料排序 */
	private Integer dataSort;

	/** 網站連結 */
	private String siteUrl;

	public DPB0033Resp() {}

	public Long getSiteId() {
		return siteId;
	}

	public void setSiteId(Long siteId) {
		this.siteId = siteId;
	}
	
	public void setSiteParentId(Long siteParentId) {
		this.siteParentId = siteParentId;
	}
	
	public Long getSiteParentId() {
		return siteParentId;
	}

	public void setSiteDesc(String siteDesc) {
		this.siteDesc = siteDesc;
	}
	
	public String getSiteDesc() {
		return siteDesc;
	}

	public Integer getDataSort() {
		return dataSort;
	}

	public void setDataSort(Integer dataSort) {
		this.dataSort = dataSort;
	}

	public String getSiteUrl() {
		return siteUrl;
	}

	public void setSiteUrl(String siteUrl) {
		this.siteUrl = siteUrl;
	}
	
}
