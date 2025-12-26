package tpi.dgrv4.dpaa.vo;

import java.util.Map;

public class DPB0091ApiItem {
	
	/** PK */
	private String apiKey;

	/** PK */
	private String moduleName;
	
	/** 主題名稱 ex: 1:資訊, 2:社福 */
	private Map<Long, String> themeDatas;

	/** API 名稱 */
	private String apiName;

	/** 組織單位ID */
	private String orgId;

	/** 組織名稱 */
	private String orgName;
	
	/**
	 * API UUID "apiUid":"4CD1C35A-FAD7-4557-A268-28C98E6D7134" 資料from: TSMP_API"
	 */
	private String apiUid;

	/** API 說明 */
	private String apiDesc;

	/** ID (流水號) */
	private Long apiExtId;

	/** 檔案名 ex: xx.doc ; from TSMP_DP_FILE */
	private String fileName;

	/** 路徑 moduleName之後的 path, ex: DOC/1/xx.doc ; from TSMP_DP_FILE */
	private String filePath;

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
	
	public String getApiKey() {
		return apiKey;
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

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}
	
	public String getFileName() {
		return fileName;
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

	public void setApiDesc(String apiDesc) {
		this.apiDesc = apiDesc;
	}

	public Long getApiExtId() {
		return apiExtId;
	}
	
	public String getOrgId() {
		return orgId;
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

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

}
