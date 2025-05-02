package tpi.dgrv4.dpaa.vo;

public class DPB0013Req {

	/** PK=做為分頁使用 */
	private Long appId;

	/** 模糊搜尋=每一個字串可以使用"空白鍵"隔開 */
	private String keyword;

	/** 狀態=1：啟用，0：停用(預設啟用)，""：全部 */
	private String dataStatus;

	public DPB0013Req() {}

	public Long getAppId() {
		return appId;
	}

	public void setAppId(Long appId) {
		this.appId = appId;
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
