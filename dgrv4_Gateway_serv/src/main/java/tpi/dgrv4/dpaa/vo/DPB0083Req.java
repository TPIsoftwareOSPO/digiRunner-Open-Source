package tpi.dgrv4.dpaa.vo;

public class DPB0083Req {
	
	/** PK	做為分頁使用, 必需是 List 回傳的最後一筆 */
	private String clientId;
	
	/** 模糊搜尋	每一個字串可以使用"空白鍵" 隔開 */
	private String keyword;

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
 
}
