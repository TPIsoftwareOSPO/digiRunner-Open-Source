package tpi.dgrv4.dpaa.vo;

import java.util.Map;

public class DPB0068D1 {

	/** from TSMP_DP_REQ_ORDERD1.req_orderd1_id */
	private Long reqOrderd1Id;

	/** API UUID from TSMP_DP_REQ_ORDERD1.api_uid */
	private String apiUid;

	/** API名稱 from TSMP_API.api_name */
	private String apiName;

	/** 模組名稱 from TSMP_API.module_name */
	private String moduleName;

	/** 組織名稱 parse from TSMP_API.org_id */
	private String orgName;

	/** 主題分類, Map<themId:String, themeName:String> */
	private Map<String, String> themeList;		

	/** API說明 */
	private String apiDesc;	

	/** API說明文件, Map<fileName:String, filePath:String>, ex: fileName="aa.doc", filePath="xxx/1/aa.doc" */
	private Map<String, String> docFileInfo;

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

	public DPB0068D1() {
	}

	public Long getReqOrderd1Id() {
		return reqOrderd1Id;
	}

	public void setReqOrderd1Id(Long reqOrderd1Id) {
		this.reqOrderd1Id = reqOrderd1Id;
	}

	public void setApiUid(String apiUid) {
		this.apiUid = apiUid;
	}
	
	public String getApiUid() {
		return apiUid;
	}

	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}
	
	public String getOrgName() {
		return orgName;
	}

	public Map<String, String> getThemeList() {
		return themeList;
	}

	public void setThemeList(Map<String, String> themeList) {
		this.themeList = themeList;
	}

	public String getApiDesc() {
		return apiDesc;
	}

	public void setApiDesc(String apiDesc) {
		this.apiDesc = apiDesc;
	}

	public Map<String, String> getDocFileInfo() {
		return docFileInfo;
	}

	public void setDocFileInfo(Map<String, String> docFileInfo) {
		this.docFileInfo = docFileInfo;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public Long getApiExtId() {
		return apiExtId;
	}

	public void setApiExtId(Long apiExtId) {
		this.apiExtId = apiExtId;
	}

	public String getDpStatus() {
		return dpStatus;
	}

	public void setDpStatus(String dpStatus) {
		this.dpStatus = dpStatus;
	}

	public String getPublicFlag() {
		return publicFlag;
	}

	public void setPublicFlag(String publicFlag) {
		this.publicFlag = publicFlag;
	}

	public String getPublicFlagName() {
		return publicFlagName;
	}

	public void setPublicFlagName(String publicFlagName) {
		this.publicFlagName = publicFlagName;
	}

}
