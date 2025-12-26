package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB9907Resp {

	/** 分類編號 */
	private String itemNo;

	/** 資料清單 */
	private List<DPB9907Item> dataList;

	public String getItemNo() {
		return itemNo;
	}

	public void setItemNo(String itemNo) {
		this.itemNo = itemNo;
	}

	public List<DPB9907Item> getDataList() {
		return dataList;
	}

	public void setDataList(List<DPB9907Item> dataList) {
		this.dataList = dataList;
	}

}
