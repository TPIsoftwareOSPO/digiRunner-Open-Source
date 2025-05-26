package tpi.dgrv4.dpaa.service.composer;

public class ComposerFindAppsReqItem {

	/** 模組名稱, applications.moduleName */
	private String moduleName;

	/** API名稱, applications.apiName */
	private String apiName;

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}

}