package tpi.dgrv4.dpaa.vo;

public class DPB0056Resp {

	/** PK */
	private Long themeId;

	/** API主題名稱 */
	private String themeName;

	private String dataStatusName;

	/** 資料排序 */
	private Integer dataSort;

	/** 組織單位ID */
	private String orgId;

	/** 組織名稱 */
	private String orgName;

	/**  */
	private Long fileId;

	/**  */
	private String fileName;
	
	/** 資料狀態 */
	private String dataStatus;

	private String filePath;

	/** tsmp_dp_theme_category.version */
	private Long lv;

	public DPB0056Resp() {}

	public Long getThemeId() {
		return themeId;
	}

	public void setThemeId(Long themeId) {
		this.themeId = themeId;
	}

	public String getThemeName() {
		return themeName;
	}

	

	public String getDataStatus() {
		return dataStatus;
	}

	public void setDataStatus(String dataStatus) {
		this.dataStatus = dataStatus;
	}

	public String getDataStatusName() {
		return dataStatusName;
	}

	public void setDataStatusName(String dataStatusName) {
		this.dataStatusName = dataStatusName;
	}

	public Integer getDataSort() {
		return dataSort;
	}

	public void setDataSort(Integer dataSort) {
		this.dataSort = dataSort;
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

	public Long getFileId() {
		return fileId;
	}
	
	public void setLv(Long lv) {
		this.lv = lv;
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

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Long getLv() {
		return lv;
	}

	
	
	public void setThemeName(String themeName) {
		this.themeName = themeName;
	}
}
