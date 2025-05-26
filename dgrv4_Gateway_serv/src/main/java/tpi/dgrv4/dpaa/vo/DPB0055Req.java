package tpi.dgrv4.dpaa.vo;

public class DPB0055Req {

	/** 做為分頁使用, 必需是 List 回傳的最後一筆 */
	private Long themeId;

	/** 模糊搜尋, 每一個字串可以使用"空白鍵" 隔開 */
	private String keyword;

	public DPB0055Req() {}

	public Long getThemeId() {
		return themeId;
	}

	public void setThemeId(Long themeId) {
		this.themeId = themeId;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	
}
