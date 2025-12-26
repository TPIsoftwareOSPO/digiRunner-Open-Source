package tpi.dgrv4.dpaa.vo;

public class AA0302Req {

	/** 模組名稱	*/
	private String moduleName;
	
	/** API ID*/
	private String apiKey;

	public void setModuleName(String moduleName) {
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

	@Override
	public String toString() {
		return "AA0302Req [moduleName=" + moduleName + ", apiKey=" + apiKey + "]";
	}

}
