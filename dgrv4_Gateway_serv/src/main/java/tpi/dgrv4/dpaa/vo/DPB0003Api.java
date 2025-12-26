package tpi.dgrv4.dpaa.vo;

public class DPB0003Api {

	/** ID:(流水號) */
	private Long apiAuthId;

	/** Client ID:base64.decode 後為 clientName */
	private String refClientId;

	/** 用戶端名稱 */
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

	/** 模組版本 */
	//private String moduleVersion;

	/** 申請狀態 */
	private String applyStatus;

	/** 審核人員 */
	private String refReviewUser;

	/** 審核備註 */
	private String reviewRemark;

	public DPB0003Api() {}

	public Long getApiAuthId() {
		return apiAuthId;
	}

	public void setApiAuthId(Long apiAuthId) {
		this.apiAuthId = apiAuthId;
	}

	public String getRefClientId() {
		return refClientId;
	}

	public void setRefClientId(String refClientId) {
		this.refClientId = refClientId;
	}

	public String getRefClientName() {
		return refClientName;
	}

	public void setRefClientName(String refClientName) {
		this.refClientName = refClientName;
	}

	public String getRefApiUid() {
		return refApiUid;
	}

	public void setRefApiUid(String refApiUid) {
		this.refApiUid = refApiUid;
	}

	public String getApplyPurpose() {
		return applyPurpose;
	}

	public void setApplyPurpose(String applyPurpose) {
		this.applyPurpose = applyPurpose;
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
	
	public void setApiDesc(String apiDesc) {
		this.apiDesc = apiDesc;
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

	public String getApiDesc() {
		return apiDesc;
	}

	/*
	public String getModuleVersion() {
		return moduleVersion;
	}

	public void setModuleVersion(String moduleVersion) {
		this.moduleVersion = moduleVersion;
	}
	*/

	public String getApplyStatus() {
		return applyStatus;
	}

	public void setApplyStatus(String applyStatus) {
		this.applyStatus = applyStatus;
	}

	public String getRefReviewUser() {
		return refReviewUser;
	}

	public void setRefReviewUser(String refReviewUser) {
		this.refReviewUser = refReviewUser;
	}

	public String getReviewRemark() {
		return reviewRemark;
	}

	public void setReviewRemark(String reviewRemark) {
		this.reviewRemark = reviewRemark;
	}
	
}
