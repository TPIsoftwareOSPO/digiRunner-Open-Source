package tpi.dgrv4.dpaa.vo;

public class AA0237RespB1D {
	
	/** TSMP_GROUP_API.group_id*/
	private String groupId;
	
	/** TSMP_GROUP_API.module_name	*/
	private String moduleName;
	
	/** TSMP_GROUP_API.api_key*/
	private String apiKey;
	
	/** TSMP_API.api_name*/
	private String apiName;

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getApiKey() {
		return apiKey;
	}

	public String getApiName() {
		return apiName;
	}
	
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}

	@Override
	public String toString() {
		return "AA0237RespB1D [groupId=" + groupId + ", moduleName=" + moduleName + ", apiKey=" + apiKey + ", apiName="
				+ apiName + "]";
	}
	
	
}
