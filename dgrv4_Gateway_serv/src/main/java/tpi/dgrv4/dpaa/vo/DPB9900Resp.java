package tpi.dgrv4.dpaa.vo;

import java.util.List;
import java.util.Map;

public class DPB9900Resp {

	/** 資料清單 */
	private List<DPB9900Item> dataList;

	public List<DPB9900Item> getDataList() {
		return dataList;
	}

	public void setDataList(List<DPB9900Item> dataList) {
		this.dataList = dataList;
	}

	public Map<String, String> getAllProperties() {
		return allProperties;
	}

	public void setAllProperties(Map<String, String> allProperties) {
		this.allProperties = allProperties;
	}

	private Map<String, String> allProperties;// 所有屬性清單

}
