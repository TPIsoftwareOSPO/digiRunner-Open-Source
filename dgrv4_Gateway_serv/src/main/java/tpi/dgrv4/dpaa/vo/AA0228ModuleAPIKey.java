package tpi.dgrv4.dpaa.vo;

public class AA0228ModuleAPIKey {
	
	private String moduleName;
	
	private String apiNameApiKey;
	
	private String orgApiNameApiKey;
	
	private Boolean isTruncated;

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public void setApiNameApiKey(String apiNameApiKey) {
		this.apiNameApiKey = apiNameApiKey;
	}
	
	public void setOrgApiNameApiKey(String orgApiNameApiKey) {
		this.orgApiNameApiKey = orgApiNameApiKey;
	}
	
	public String getApiNameApiKey() {
		return apiNameApiKey;
	}

	public String getOrgApiNameApiKey() {
		return orgApiNameApiKey;
	}

	public Boolean getIsTruncated() {
		return isTruncated;
	}

	public void setIsTruncated(Boolean isTruncated) {
		this.isTruncated = isTruncated;
	}

	@Override
	public String toString() {
		return "AA0228ModuleAPIKey [moduleName=" + moduleName + ", apiNameApiKey=" + apiNameApiKey
				+ ", orgApiNameApiKey=" + orgApiNameApiKey + ", isTruncated=" + isTruncated + "]";
	}

	
}
