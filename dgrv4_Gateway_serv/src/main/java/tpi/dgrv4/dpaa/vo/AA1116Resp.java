package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA1116Resp {

	/** 資料清單 ex: "http://", "https://" */
	private List<AA1116Item> dataList;

	@Override
	public String toString() {
		return "AA1116Resp []";
	}

	public List<AA1116Item> getDataList() {
		return dataList;
	}

	public void setDataList(List<AA1116Item> dataList) {
		this.dataList = dataList;
	}

}
