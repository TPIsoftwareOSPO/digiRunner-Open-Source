package tpi.dgrv4.dpaa.vo;

import java.util.List;
import java.util.Map;

public class AA0301Resp {
	/** 排序設定	*/
	private Map<String, String> sortBy;
	
	/** 資料清單	*/
	private List<AA0301Item> dataList;

	public Map<String, String> getSortBy() {
		return sortBy;
	}

	public void setSortBy(Map<String, String> sortBy) {
		this.sortBy = sortBy;
	}

	public List<AA0301Item> getDataList() {
		return dataList;
	}

	public void setDataList(List<AA0301Item> dataList) {
		this.dataList = dataList;
	}

	@Override
	public String toString() {
		return "AA0301Resp [sortBy=" + sortBy + ", dataList=" + dataList + "]";
	}

}
