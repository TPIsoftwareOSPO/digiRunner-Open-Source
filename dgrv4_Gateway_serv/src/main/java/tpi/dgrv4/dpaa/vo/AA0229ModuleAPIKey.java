package tpi.dgrv4.dpaa.vo;

public class AA0229ModuleAPIKey {
	
	private String moduleName;
	
	private String apiNameApiKey;
	
	private String orgApiNameApiKey;
	
	private boolean isTruncated;

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public void setApiNameApiKey(String apiNameApiKey) {
		this.apiNameApiKey = apiNameApiKey;
	}
	
	public String getApiNameApiKey() {
		return apiNameApiKey;
	}

	public String getOrgApiNameApiKey() {
		return orgApiNameApiKey;
	}

	public void setOrgApiNameApiKey(String orgApiNameApiKey) {
		this.orgApiNameApiKey = orgApiNameApiKey;
	}

	public boolean isTruncated() {
		return isTruncated;
	}

	public void setTruncated(boolean isTruncated) {
		this.isTruncated = isTruncated;
	}
	
	
}
