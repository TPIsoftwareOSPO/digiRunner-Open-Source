package tpi.dgrv4.dpaa.vo;

public class AA0236API {

	// 群組代碼
	private String groupId;

	// API Key
	private String apiKey;

	// 模組名稱
	private String moduleName;

	// API Name
	private String apiName;

	public void setGroupId(String groupId) {
		this.groupId = groupId;
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
	
	public String getGroupId() {
		return groupId;
	}
	
	public String getApiKey() {
		return apiKey;
	}

	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}

}
