package tpi.dgrv4.dpaa.vo;

public class DPB0072RespItem {

	/** PK */
	private String apiKey;

	/** PK */
	private String moduleName;

	/** API UID */
	private String apiUid;

	/** API名稱 */
	private String apiName;

	/** API說明 */
	private String apiDesc;

	/** 主題名稱	ex: 名稱1, 名稱2, .... etc */
	private String themeName;

	/** 組織名稱 */
	private String orgName;

	/** 上架日期 yyyy-MM-dd */
	private String dpStuDateTime;

	/** ID (流水號) */
	private Long apiExtId;

	/** 檔案名	ex: xx.doc ; from TSMP_DP_FILE */
	private String fileName;

	/** 路徑	moduleName之後的 path, ex: DOC/1/ ; from TSMP_DP_FILE */
	private String filePath;

	/** 對外開放權限	直接取TSMP_API.PUBLIC_LAG的值 */
	private String publicFlag;

	/** 對外開放權限Name	"由publicFlag 去解析回傳 TSMP_DP_ITEMS.SUBITEM_NAME。NULL 表示'對內'。 */
	private String publicFlagName;

	public DPB0072RespItem() {}

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

	public String getApiUid() {
		return apiUid;
	}

	public void setApiUid(String apiUid) {
		this.apiUid = apiUid;
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

	public void setApiDesc(String apiDesc) {
		this.apiDesc = apiDesc;
	}

	public String getThemeName() {
		return themeName;
	}

	public void setThemeName(String themeName) {
		this.themeName = themeName;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getDpStuDateTime() {
		return dpStuDateTime;
	}

	public void setDpStuDateTime(String dpStuDateTime) {
		this.dpStuDateTime = dpStuDateTime;
	}

	public Long getApiExtId() {
		return apiExtId;
	}

	public void setApiExtId(Long apiExtId) {
		this.apiExtId = apiExtId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getPublicFlag() {
		return publicFlag;
	}
	
	public String getFilePath() {
		return filePath;
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
