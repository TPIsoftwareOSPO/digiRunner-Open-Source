package tpi.dgrv4.dpaa.vo;

public class DPB0020Req {

	/** PK=做為分頁使用 */
	private Long apiThemeId;

	/** 模糊搜尋=每一個字串可以使用"空白鍵"隔開 */
	private String keyword;

	/** 1：啟用，0：停用 （預設啟用），""：全部 */
	private String dataStatus;

	public DPB0020Req() {}

	public Long getApiThemeId() {
		return apiThemeId;
	}

	public void setApiThemeId(Long apiThemeId) {
		this.apiThemeId = apiThemeId;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getDataStatus() {
		return dataStatus;
	}

	public void setDataStatus(String dataStatus) {
		this.dataStatus = dataStatus;
	}
	
}
