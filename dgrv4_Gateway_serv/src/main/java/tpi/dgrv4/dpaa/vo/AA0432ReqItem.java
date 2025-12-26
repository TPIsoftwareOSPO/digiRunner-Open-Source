package tpi.dgrv4.dpaa.vo;

public class AA0432ReqItem {
	private String apiKey;
	private String moduleName;

	public AA0432ReqItem() {
	}

	public AA0432ReqItem(String apiKey, String moduleName) {
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
