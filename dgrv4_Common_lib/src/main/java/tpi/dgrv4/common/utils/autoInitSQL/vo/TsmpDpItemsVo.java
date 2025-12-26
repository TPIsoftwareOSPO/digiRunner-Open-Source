package tpi.dgrv4.common.utils.autoInitSQL.vo;

public class TsmpDpItemsVo {
	
	private Long itemId;

	private String itemNo;

	private String itemName;

	private String subitemNo;

	private String subitemName;

	private Integer sortBy = 0; // 預設0

	private String isDefault;

	private String param1;

	private String param2;

	private String param3;

	private String param4;

	private String param5;
	
	private String locale;

	/* constructors */
	public TsmpDpItemsVo() {}

	/* methods */
	
	@Override
	public String toString() {
		return "TsmpDpItems [itemId=" + itemId + ", itemNo=" + itemNo + ", itemName=" + itemName + ", subitemNo="
				+ subitemNo + ", subitemName=" + subitemName + ", sortBy=" + sortBy + ", isDefault=" + isDefault
				+ ", param1=" + param1 + ", param2=" + param2 + ", param3=" + param3 + ", param4=" + param4
				+ ", param5=" + param5 + ", locale=" + locale + "]\n";
	}

	/* getters and setters */

	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	public String getItemNo() {
		return itemNo;
	}

	public void setItemNo(String itemNo) {
		this.itemNo = itemNo;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getSubitemNo() {
		return subitemNo;
	}

	public void setSubitemNo(String subitemNo) {
		this.subitemNo = subitemNo;
	}

	public String getSubitemName() {
		return subitemName;
	}

	public void setSubitemName(String subitemName) {
		this.subitemName = subitemName;
	}

	public int getSortBy() {
		return sortBy;
	}

	public void setSortBy(Integer sortBy) {
		this.sortBy = sortBy;
	}

	public String getIsDefault() {
		return isDefault;
	}

	public void setIsDefault(String isDefault) {
		this.isDefault = isDefault;
	}

	public String getParam1() {
		return param1;
	}

	public void setParam1(String param1) {
		this.param1 = param1;
	}

	public String getParam2() {
		return param2;
	}

	public void setParam2(String param2) {
		this.param2 = param2;
	}

	public String getParam3() {
		return param3;
	}

	public void setParam3(String param3) {
		this.param3 = param3;
	}

	public String getParam4() {
		return param4;
	}

	public void setParam4(String param4) {
		this.param4 = param4;
	}

	public String getParam5() {
		return param5;
	}

	public void setParam5(String param5) {
		this.param5 = param5;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}
	
}