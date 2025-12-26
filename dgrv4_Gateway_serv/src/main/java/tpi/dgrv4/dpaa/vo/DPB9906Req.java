package tpi.dgrv4.dpaa.vo;

public class DPB9906Req {

	/** 語系 */
	private String locale;

	/** 分類編號 */
	private String itemNo;

	/** 子分類編號 */
	private String subitemNo;

	/** 是否取得所有語系的子分類名稱清單 */
	private String getSubitemNameList;

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getItemNo() {
		return itemNo;
	}

	public void setItemNo(String itemNo) {
		this.itemNo = itemNo;
	}

	public String getSubitemNo() {
		return subitemNo;
	}

	public void setSubitemNo(String subitemNo) {
		this.subitemNo = subitemNo;
	}

	public String getGetSubitemNameList() {
		return getSubitemNameList;
	}

	public void setGetSubitemNameList(String getSubitemNameList) {
		this.getSubitemNameList = getSubitemNameList;
	}

}