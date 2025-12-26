package tpi.dgrv4.dpaa.component.req;

import java.util.Map;

public class DpReqQueryResp_D5d {
	/** PK */
	private Long reqOrderd5dId;

	/** PK */
	private String apiKey;

	/** PK */
	private String moduleName;
	
	/** ID (流水號) */
	private Long apiExtId;

	/** API 名稱 */
	private String apiName;

	/** 主題名稱 ex: 1:資訊, 2:社福 */
	private Map<Long, String> themeList;

	/** 組織單位ID */
	private String orgId;

	/** 組織名稱 */
	private String orgName;

	/** API 說明 */
	private String apiDesc;

	/** API UUID "apiUid":"4CD1C35A-FAD7-4557-A268-28C98E6D7134" */
	private String apiUid;

	/** API說明文件 ex: fileName="aa.doc", filePath="xxx/1/aa.doc" */
	private Map<String, String> docFileInfo;

	public Long getReqOrderd5dId() {
		return reqOrderd5dId;
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

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getApiName() {
		return apiName;
	}
	
	public void setReqOrderd5dId(Long reqOrderd5dId) {
		this.reqOrderd5dId = reqOrderd5dId;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}

	public Map<Long, String> getThemeList() {
		return themeList;
	}

	public void setThemeList(Map<Long, String> themeList) {
		this.themeList = themeList;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}
	
	public void setApiDesc(String apiDesc) {
		this.apiDesc = apiDesc;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getApiDesc() {
		return apiDesc;
	}

	public Long getApiExtId() {
		return apiExtId;
	}
	
	public Map<String, String> getDocFileInfo() {
		return docFileInfo;
	}

	public void setApiExtId(Long apiExtId) {
		this.apiExtId = apiExtId;
	}

	public String getApiUid() {
		return apiUid;
	}

	public void setApiUid(String apiUid) {
		this.apiUid = apiUid;
	}

	public void setDocFileInfo(Map<String, String> docFileInfo) {
		this.docFileInfo = docFileInfo;
	}

}
