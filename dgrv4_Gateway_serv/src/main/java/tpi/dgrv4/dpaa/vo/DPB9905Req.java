package tpi.dgrv4.dpaa.vo;

public class DPB9905Req {

	/** 分類順序(分頁用) */
	private Integer p_itemOrder;

	/** 序號(分頁用) */
	private Integer p_sortBy;

	/** 語系 */
	private String locale;

	/** 關鍵字搜尋 */
	private String keyword;

	/** 分類編號 */
	private String itemNo;

	public Integer getP_itemOrder() {
		return p_itemOrder;
	}

	public void setP_itemOrder(Integer p_itemOrder) {
		this.p_itemOrder = p_itemOrder;
	}

	public Integer getP_sortBy() {
		return p_sortBy;
	}

	public void setP_sortBy(Integer p_sortBy) {
		this.p_sortBy = p_sortBy;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getItemNo() {
		return itemNo;
	}

	public void setItemNo(String itemNo) {
		this.itemNo = itemNo;
	}

}