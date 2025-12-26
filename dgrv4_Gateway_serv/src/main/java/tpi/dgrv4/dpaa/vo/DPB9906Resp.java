package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB9906Resp {

	/** 語系 */
	private String locale;

	/** 流水號 */
	private Integer itemId;

	/** 序號 */
	private Integer sortBy;

	/** 是否為預設 */
	private String isDefault;

	/** 分類編號 */
	private String itemNo;

	/** 分類名稱 */
	private String itemName;

	/** 子分類編號 */
	private String subitemNo;

	/** 子分類名稱 */
	private String subitemName;

	/** 參數清單 */
	private List<String> params;

	/** 子分類名稱清單 */
	private List<DPB9906Item> subitemNameList;
	
	/** 參數最大數量 */
	private Integer paramSize = 5;

	public Integer getParamSize() {
		return paramSize;
	}

	public void setParamSize(Integer paramSize) {
		this.paramSize = paramSize;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public Integer getItemId() {
		return itemId;
	}

	public void setItemId(Integer itemId) {
		this.itemId = itemId;
	}

	public Integer getSortBy() {
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

	public List<String> getParams() {
		return params;
	}

	public void setParams(List<String> params) {
		this.params = params;
	}

	public List<DPB9906Item> getSubitemNameList() {
		return subitemNameList;
	}

	public void setSubitemNameList(List<DPB9906Item> subitemNameList) {
		this.subitemNameList = subitemNameList;
	}

}