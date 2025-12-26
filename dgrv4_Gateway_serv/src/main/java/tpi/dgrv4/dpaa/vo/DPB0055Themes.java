package tpi.dgrv4.dpaa.vo;

public class DPB0055Themes {

	/** PK */
	private Long themeId;

	/** 資料狀態-code */
	private String dataStatus;

	/** 資料狀態-中文 */
	private String dataStatusName;

	/** 資料排序 */
	private Integer dataSort;
	
	/** API主題名稱 */
	private String themeName;

	/** 組織名稱 */
	private String orgName;

	/**  */
	private Long fileId;

	/**  */
	private String fileName;

	/**  */
	private String filePath;
	
	/** 組織單位ID */
	private String orgId;

	public DPB0055Themes() {}

	public void setThemeId(Long themeId) {
		this.themeId = themeId;
	}

	public String getThemeName() {
		return themeName;
	}

	public void setThemeName(String themeName) {
		this.themeName = themeName;
	}

	public String getDataStatus() {
		return dataStatus;
	}

	public void setDataStatus(String dataStatus) {
		this.dataStatus = dataStatus;
	}
	
	public Long getThemeId() {
		return themeId;
	}

	public String getDataStatusName() {
		return dataStatusName;
	}

	public void setDataStatusName(String dataStatusName) {
		this.dataStatusName = dataStatusName;
	}
	
	public Long getFileId() {
		return fileId;
	}

	public Integer getDataSort() {
		return dataSort;
	}

	public String getOrgId() {
		return orgId;
	}
	
	public void setDataSort(Integer dataSort) {
		this.dataSort = dataSort;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getOrgName() {
		return orgName;
	}
	
	public String getFilePath() {
		return filePath;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public void setFileId(Long fileId) {
		this.fileId = fileId;
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
	
}
