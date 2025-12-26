package tpi.dgrv4.dpaa.vo;

import java.util.Map;

public class TsmpApiItem {
	
	/** PK */
	private String apiKey;
	
	/** PK */
	private String moduleName;
	
	/** API名稱 */
	private String apiName;
	
	/** 組織單位ID */
	private String orgId;
	
	/** 主題分類	ex: 1:資訊, 2:社福 */
	private Map<Long, String> themeDatas;
	
	/** 業務單位 */
	private String orgName;
	
	/** API說明 */
	private String apiDesc;
	
	/** 上下架	0：下架，1：上架 */
	private String dpStatus;
	
	/** ID (流水號) */
	private Long apiExtId;
    
    /** "apiUid":"4CD1C35A-FAD7-4557-A268-28C98E6D7134" 資料from: TSMP_API */
    private String apiUid;
    
    /** 路徑    moduleName之後的 path, ex: DOC/1/ ; from TSMP_DP_FILE */
    private String filePath;

    /** 檔案名    ex: xx.doc ; from TSMP_DP_FILE */
    private String fileName;
    
    /** 開放狀態代碼	from tsmp_api.public_flag */
    private String publicFlag;

    /** 開放狀態名稱 */
    private String publicFlagName;

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

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
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

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}
	
	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getApiDesc() {
		return apiDesc;
	}

	public void setApiDesc(String apiDesc) {
		this.apiDesc = apiDesc;
	}

	public String getDpStatus() {
		return dpStatus;
	}

	public void setDpStatus(String dpStatus) {
		this.dpStatus = dpStatus;
	}

	public Long getApiExtId() {
		return apiExtId;
	}

	public String getApiUid() {
		return apiUid;
	}

	public void setApiUid(String apiUid) {
		this.apiUid = apiUid;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public void setApiExtId(Long apiExtId) {
		this.apiExtId = apiExtId;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	public String getFilePath() {
		return filePath;
	}

	public String getPublicFlag() {
		return publicFlag;
	}

	public void setPublicFlag(String publicFlag) {
		this.publicFlag = publicFlag;
	}

	public void setPublicFlagName(String publicFlagName) {
		this.publicFlagName = publicFlagName;
	}
	
	public String getPublicFlagName() {
		return publicFlagName;
	}
	
}
