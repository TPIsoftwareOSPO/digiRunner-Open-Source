package tpi.dgrv4.dpaa.vo;

import java.util.Map;

public class DPB0068D5 {
	
	/** 組織單位ID */
	private String orgId;
	
	/** PK */
	private Long reqOrderd5dId;

	/** PK */
	private String apiKey;

	/** PK */
	private String moduleName;
	
	/** API UUID "apiUid":"4CD1C35A-FAD7-4557-A268-28C98E6D7134" */
	private String apiUid;
	
	/** API 名稱 */
	private String apiName;

	/** 主題名稱 ex: 1:資訊, 2:社福 */
	private Map<Long, String> themeList;

	/** 組織名稱 */
	private String orgName;

	/** API 說明 */
	private String apiDesc;

	/** ID (流水號) */
	private Long apiExtId;

	/** API說明文件 ex: fileName="aa.doc", filePath="xxx/1/aa.doc" */
	private Map<String, String> docFileInfo;

	public Long getReqOrderd5dId() {
		return reqOrderd5dId;
	}

	public void setReqOrderd5dId(Long reqOrderd5dId) {
		this.reqOrderd5dId = reqOrderd5dId;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getModuleName() {
		return moduleName;
	}

	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}

	public Map<Long, String> getThemeList() {
		return themeList;
	}

	
	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}
	
	public void setThemeList(Map<Long, String> themeList) {
		this.themeList = themeList;
	}


	public String getApiDesc() {
		return apiDesc;
	}

	public void setApiDesc(String apiDesc) {
		this.apiDesc = apiDesc;
	}

	public Long getApiExtId() {
		return apiExtId;
	}

	public String getApiUid() {
		return apiUid;
	}
	
	public void setApiExtId(Long apiExtId) {
		this.apiExtId = apiExtId;
	}

	public void setApiUid(String apiUid) {
		this.apiUid = apiUid;
	}

	public Map<String, String> getDocFileInfo() {
		return docFileInfo;
	}

	public void setDocFileInfo(Map<String, String> docFileInfo) {
		this.docFileInfo = docFileInfo;
	}

}
