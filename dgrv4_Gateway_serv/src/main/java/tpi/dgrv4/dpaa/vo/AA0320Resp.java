package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0320Resp {

	/** 模組名稱	*/
	private String moduleName;
	
	/** API ID*/
	private String apiKey;

	/** 資料清單	*/
	private List<AA0320Item> dataList;

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public List<AA0320Item> getDataList() {
		return dataList;
	}

	public void setDataList(List<AA0320Item> dataList) {
		this.dataList = dataList;
	}

	@Override
	public String toString() {
		return "AA0320Resp [moduleName=" + moduleName + ", apiKey=" + apiKey + ", dataList=" + dataList + "]";
	}
	
}
