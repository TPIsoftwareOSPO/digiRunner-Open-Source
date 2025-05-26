package tpi.dgrv4.dpaa.vo;

public class DPB0001Api {

	/** ID:(流水號) */
	private Long apiAuthId;

	/** Client ID:base64.decode 後為 clientName */
	private String refClientId;

	private String refClientName;

	/** API UID */
	private String refApiUid;

	/** 申請用途說明 */
	private String applyPurpose;

	/**  */
	private String apiKey;

	/** 模組名稱 */
	private String moduleName;

	/** api名稱 */
	private String apiName;

	/** api功能說明 */
	private String apiDesc;

	/** 模組版本:TSMP_API_MODULE.findByModuleNameAndActive() */
	//private String moduleVersion;

	/** version(處理雙欄位更新) */
	private Long lv;

	public DPB0001Api() {}

	public Long getApiAuthId() {
		return apiAuthId;
	}

	public void setApiAuthId(Long apiAuthId) {
		this.apiAuthId = apiAuthId;
	}

	public void setRefClientId(String refClientId) {
		this.refClientId = refClientId;
	}
	
	public String getRefClientId() {
		return refClientId;
	}

	public String getRefClientName() {
		return refClientName;
	}

	public void setRefClientName(String refClientName) {
		this.refClientName = refClientName;
	}

	public void setRefApiUid(String refApiUid) {
		this.refApiUid = refApiUid;
	}
	
	public String getRefApiUid() {
		return refApiUid;
	}

	public String getApplyPurpose() {
		return applyPurpose;
	}

	public void setApplyPurpose(String applyPurpose) {
		this.applyPurpose = applyPurpose;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	
	public String getApiKey() {
		return apiKey;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}
	
	public String getApiName() {
		return apiName;
	}

	public String getApiDesc() {
		return apiDesc;
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

	public Long getLv() {
		return lv;
	}

	public void setLv(Long lv) {
		this.lv = lv;
	}

}
