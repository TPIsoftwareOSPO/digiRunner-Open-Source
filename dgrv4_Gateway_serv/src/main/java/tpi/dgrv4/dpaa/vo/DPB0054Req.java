package tpi.dgrv4.dpaa.vo;

public class DPB0054Req {

	/** 主題id */
	private Long themeId;

	/** 主題名稱 */
	private String themeName;

	/** 暫存檔名:沒有上傳請傳入null,表示檔案不用異動 */
	private String fileName;

	/** version, for 雙欄位更新 */
	private Long lv;

	public DPB0054Req() {}

	public Long getThemeId() {
		return themeId;
	}

	public void setThemeId(Long themeId) {
		this.themeId = themeId;
	}

	public String getThemeName() {
		return themeName;
	}

	public void setThemeName(String themeName) {
		this.themeName = themeName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Long getLv() {
		return lv;
	}

	public void setLv(Long lv) {
		this.lv = lv;
	}
	
}
