package tpi.dgrv4.dpaa.vo;

public class DPB0054Resp {

	/**  */
	private Long themeId;

	/**  */
	private Long fileId;

	/** from TSMP_DP_THEME_CATEGORY.VERSION */
	private Long lv;

	public DPB0054Resp() {}

	public Long getThemeId() {
		return themeId;
	}

	public void setThemeId(Long themeId) {
		this.themeId = themeId;
	}

	public Long getFileId() {
		return fileId;
	}

	public void setFileId(Long fileId) {
		this.fileId = fileId;
	}

	public Long getLv() {
		return lv;
	}

	public void setLv(Long lv) {
		this.lv = lv;
	}
	
}
