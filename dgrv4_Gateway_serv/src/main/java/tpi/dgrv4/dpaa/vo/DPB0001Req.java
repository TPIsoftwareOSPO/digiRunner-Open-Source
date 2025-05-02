package tpi.dgrv4.dpaa.vo;

public class DPB0001Req {

	/** PK:做為分頁使用 */
	private Long apiAuthId;

	/** 模糊搜尋:每一個字串可以使用"空白鍵"隔開 */
	private String keyword;

	public DPB0001Req() {}

	public Long getApiAuthId() {
		return apiAuthId;
	}

	public void setApiAuthId(Long apiAuthId) {
		this.apiAuthId = apiAuthId;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	
}
