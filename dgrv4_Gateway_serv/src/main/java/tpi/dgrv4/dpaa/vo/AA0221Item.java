package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0221Item {
	
	/** API Key 清單 */
	private List<String> apiKeyList;
	
	/** 模組名稱 */
	private String moduleName;

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public List<String> getApiKeyList() {
		return apiKeyList;
	}

	public void setApiKeyList(List<String> apiKeyList) {
		this.apiKeyList = apiKeyList;
	}
	
	public String getModuleName() {
		return moduleName;
	}

	@Override
	public String toString() {
		return "AA0221Item [moduleName=" + moduleName + ", apiKeyList=" + apiKeyList + "]";
	}
	
	
	
}
