package tpi.dgrv4.dpaa.vo;

public class DPB9905Subitem {

	/** 子分類順序 */
	private String subitemOrder;

	/** 序號 */
	private Integer sortBy;

	/** 子分類編號 */
	private String subitemNo;

	/** 子分類名稱 */
	private String subitemName;

	/** 最後異動時間 */
	private String updateDateTime;

	/** 最後異動人員 */
	private String updateUser;

	/** 是否為預設 */
	private String isDefault;

	public String getSubitemOrder() {
		return subitemOrder;
	}

	public void setSubitemOrder(String subitemOrder) {
		this.subitemOrder = subitemOrder;
	}

	public Integer getSortBy() {
		return sortBy;
	}

	public void setSortBy(Integer sortBy) {
		this.sortBy = sortBy;
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

	public String getUpdateDateTime() {
		return updateDateTime;
	}

	public void setUpdateDateTime(String updateDateTime) {
		this.updateDateTime = updateDateTime;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public String getIsDefault() {
		return isDefault;
	}

	public void setIsDefault(String isDefault) {
		this.isDefault = isDefault;
	}

	@Override
	public String toString() {
		return "DPB9905Subitem [subitemOrder=" + subitemOrder + ", sortBy=" + sortBy + ", subitemNo=" + subitemNo
				+ ", subitemName=" + subitemName + ", updateDateTime=" + updateDateTime + ", updateUser=" + updateUser
				+ ", isDefault=" + isDefault + "]";
	}

}