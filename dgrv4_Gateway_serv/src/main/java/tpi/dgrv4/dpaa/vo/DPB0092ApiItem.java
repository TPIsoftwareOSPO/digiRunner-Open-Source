package tpi.dgrv4.dpaa.vo;

import java.util.Map;

public class DPB0092ApiItem {

	/** PK */
	private String apiKey;

	/** PK */
	private String moduleName;
	
	/** 路徑 moduleName之後的 path, ex: DOC/1/xx.doc ; from TSMP_DP_FILE */
	private String filePath;

	/** 主題名稱 ex: 1:資訊, 2:社福 */
	private Map<Long, String> themeDatas;

	/** 組織單位ID */
	private String orgId;

	/** 組織名稱 */
	private String orgName;

	/** API 說明 */
	private String apiDesc;

	/** ID (流水號) */
	private Long apiExtId;
	
	/** API 名稱 */
	private String apiName;

	/** API UUID "apiUid":"4CD1C35A-FAD7-4557-A268-28C98E6D7134" */
	private String apiUid;

	/** 檔案名 ex: xx.doc ; from TSMP_DP_FILE */
	private String fileName;

	

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	
	public String getApiDesc() {
		return apiDesc;
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

	public Map<Long, String> getThemeDatas() {
		return themeDatas;
	}

	public void setThemeDatas(Map<Long, String> themeDatas) {
		this.themeDatas = themeDatas;
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

	public void setApiDesc(String apiDesc) {
		this.apiDesc = apiDesc;
	}

	public Long getApiExtId() {
		return apiExtId;
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

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public String getFileName() {
		return fileName;
	}

	public String getFilePath() {
		return filePath;
	}
	
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

}
