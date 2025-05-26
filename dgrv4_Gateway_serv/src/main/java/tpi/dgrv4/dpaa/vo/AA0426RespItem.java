package tpi.dgrv4.dpaa.vo;

public class AA0426RespItem {
	private String apiKey;
	private String moduleName;

	public AA0426RespItem(String apiKey, String moduleName) {
		this.apiKey = apiKey;
		this.moduleName = moduleName;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
}
