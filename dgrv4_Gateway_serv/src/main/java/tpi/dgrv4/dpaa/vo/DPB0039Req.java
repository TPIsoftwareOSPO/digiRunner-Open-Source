package tpi.dgrv4.dpaa.vo;

public class DPB0039Req {

	/** PK	分頁使用 */
	private String userId;

	/** 模糊搜尋	每一個字串可以使用"空白鍵" 隔開 */
	private String keyword;

	public DPB0039Req() {}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	
}
