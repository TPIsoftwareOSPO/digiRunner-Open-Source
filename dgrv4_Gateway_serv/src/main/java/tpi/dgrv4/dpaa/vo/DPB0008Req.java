package tpi.dgrv4.dpaa.vo;

public class DPB0008Req {

	/** 做為分頁使用 */
	private Long appCateId;

	/** 模糊搜尋:每一個字串可以使用"空白鍵" 隔開 */
	private String keyword;

	public DPB0008Req() {}

	public Long getAppCateId() {
		return appCateId;
	}

	public void setAppCateId(Long appCateId) {
		this.appCateId = appCateId;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	
}
