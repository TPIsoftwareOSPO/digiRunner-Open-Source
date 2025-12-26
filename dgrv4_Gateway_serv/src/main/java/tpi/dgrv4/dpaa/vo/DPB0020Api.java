package tpi.dgrv4.dpaa.vo;

public class DPB0020Api {

	/** API代碼 */
	private String apiKey;

	/** 模組名稱 */
	private String moduleName;

	/** API名稱 */
	private String apiName;

	/** API狀態 */
	private String apiStatus;

	/** API來源 */
	private String apiSrc;

	/** 說明 */
	private String apiDesc;

	/** API UUID */
	private String apiUid;

	/** 模組版本 */
	//private String moduleVersion;

	public DPB0020Api() {}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getModuleName() {
		return moduleName;
	}
	
	public void setApiUid(String apiUid) {
		this.apiUid = apiUid;
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

	public String getApiStatus() {
		return apiStatus;
	}
	
	public String getApiSrc() {
		return apiSrc;
	}

	public void setApiStatus(String apiStatus) {
		this.apiStatus = apiStatus;
	}

	public void setApiSrc(String apiSrc) {
		this.apiSrc = apiSrc;
	}

	public String getApiDesc() {
		return apiDesc;
	}

	public String getApiUid() {
		return apiUid;
	}

	public void setApiDesc(String apiDesc) {
		this.apiDesc = apiDesc;
	}

	/*
	public String getModuleVersion() {
		return moduleVersion;
	}

	public void setModuleVersion(String moduleVersion) {
		this.moduleVersion = moduleVersion;
	}
	*/

}
