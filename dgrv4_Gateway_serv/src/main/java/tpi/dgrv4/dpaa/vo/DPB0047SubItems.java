package tpi.dgrv4.dpaa.vo;

public class DPB0047SubItems {
	
	/** ID (流水號) */
	public Long itemId;
	
	/** 分類編號 */
	public String itemNo;
	
	/** 分類名稱 */
	public String itemName;
	
	/** 參數1 */
	public String param1;
	
	/** 子分類編號 */
	public String subitemNo;
	
	/** 子分類名稱 */
	public String subitemName;
	
	public Integer sortBy;
	
	/** 是否為選單中的default select */
	public String isDefault;
	
	/** 參數2 */
	public String param2;
	
	/** 參數3 */
	public String param3;

	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	public void setItemNo(String itemNo) {
		this.itemNo = itemNo;
	}
	
	public String getItemNo() {
		return itemNo;
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

	public Integer getSortBy() {
		return sortBy;
	}
	
	public String getItemName() {
		return itemName;
	}

	public void setSortBy(Integer sortBy) {
		this.sortBy = sortBy;
	}

	public String getIsDefault() {
		return isDefault;
	}

	public String getParam1() {
		return param1;
	}

	public String getParam2() {
		return param2;
	}
	
	public void setParam1(String param1) {
		this.param1 = param1;
	}

	public void setParam2(String param2) {
		this.param2 = param2;
	}

	public String getParam3() {
		return param3;
	}
	
	public void setIsDefault(String isDefault) {
		this.isDefault = isDefault;
	}

	public void setParam3(String param3) {
		this.param3 = param3;
	}

}
