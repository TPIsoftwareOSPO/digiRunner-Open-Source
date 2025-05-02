package tpi.dgrv4.dpaa.vo;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;

public class DPB9905Item {

	/** 分類順序 */
	private Integer itemOrder;

	/** 分類編號 */
	private String itemNo;

	/** 分類名稱 */
	private String itemName;

	/** 最後異動時間 */
	private String updateDateTime;

	/** 最後異動人員 */
	private String updateUser;

	/** 子分類數量 */
	private String subitemCount;

	/** 子分類清單 */
	private List<DPB9905Subitem> subitemList;

	public Integer getItemOrder() {
		return itemOrder;
	}

	public void setItemOrder(Integer itemOrder) {
		this.itemOrder = itemOrder;
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

	public String getSubitemCount() {
		return subitemCount;
	}

	public void setSubitemCount(String subitemCount) {
		this.subitemCount = subitemCount;
	}

	public List<DPB9905Subitem> getSubitemList() {
		return subitemList;
	}

	public void setSubitemList(List<DPB9905Subitem> subitemList) {
		this.subitemList = subitemList;
	}

	@Override
	public String toString() {
		String subitemListStr = "";
		if (!CollectionUtils.isEmpty(subitemList)) {
			subitemListStr = String.format("\n\t%s\n", //
				subitemList.stream().map(DPB9905Subitem::toString).collect(Collectors.joining("\n\t")));
		}
		return "DPB9905Item [itemOrder=" + itemOrder + ", itemNo=" + itemNo + ", itemName=" + itemName
				+ ", updateDateTime=" + updateDateTime + ", updateUser=" + updateUser + ", subitemCount=" + subitemCount
				+ ", subitemList=[" + subitemListStr + "]]";
	}

}