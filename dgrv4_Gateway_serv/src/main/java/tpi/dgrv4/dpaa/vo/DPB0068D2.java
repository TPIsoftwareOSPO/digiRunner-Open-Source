package tpi.dgrv4.dpaa.vo;

import java.util.Map;

public class DPB0068D2 {

	/**  */
	private Long reqOrderd2Id;

	/** API名稱 */
	private String apiName;

	/** API UUID */
	private String apiUid;

	/** 主題分類 Map<themeId, themeName> */
	private Map<String, String> themeList;

	/** API說明文件 Map<fileName, filePath> */
	private Map<String, String> docFileInfo;

	/** 模組名稱 */
	private String moduleName;

	/** API說明 */
	private String apiDesc;

	/** 組織名稱 */
	private String orgName;

	/** API KEY */
	private String apiKey;

	/** 組織ID */
	private String orgId;

	/** API延伸檔ID */
	private Long apiExtId;

	/** 上下架狀態代碼 */
	private String dpStatus;

	/** 開放狀態代碼	from tsmp_api.public_flag */
	private String publicFlag;

	/** 開放狀態名稱 */
	private String publicFlagName;

	public DPB0068D2() {}

	public Long getReqOrderd2Id() {
		return reqOrderd2Id;
	}

	public void setReqOrderd2Id(Long reqOrderd2Id) {
		this.reqOrderd2Id = reqOrderd2Id;
	}

	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}

	public String getApiUid() {
		return apiUid;
	}

	public void setApiUid(String apiUid) {
		this.apiUid = apiUid;
	}

	public Map<String, String> getThemeList() {
		return themeList;
	}

	public void setThemeList(Map<String, String> themeList) {
		this.themeList = themeList;
	}

	public Map<String, String> getDocFileInfo() {
		return docFileInfo;
	}

	public void setDocFileInfo(Map<String, String> docFileInfo) {
		this.docFileInfo = docFileInfo;
	}

	@Override
	public String toString() {
		return "DPB0068D2 [reqOrderd2Id=" + reqOrderd2Id + ", apiName=" + apiName + ", apiUid=" + apiUid
				+ ", themeList=" + themeList + ", docFileInfo=" + docFileInfo + ", moduleName=" + moduleName
				+ ", apiDesc=" + apiDesc + ", orgName=" + orgName + "]";
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	
	public void setPublicFlagName(String publicFlagName) {
		this.publicFlagName = publicFlagName;
	}

	public String getApiDesc() {
		return apiDesc;
	}

	public void setApiDesc(String apiDesc) {
		this.apiDesc = apiDesc;
	}
	
	public String getPublicFlagName() {
		return publicFlagName;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}
	
	public void setPublicFlag(String publicFlag) {
		this.publicFlag = publicFlag;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	
	public String getPublicFlag() {
		return publicFlag;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public void setApiExtId(Long apiExtId) {
		this.apiExtId = apiExtId;
	}
	
	public Long getApiExtId() {
		return apiExtId;
	}

	public String getDpStatus() {
		return dpStatus;
	}

	public void setDpStatus(String dpStatus) {
		this.dpStatus = dpStatus;
	}
	
}
