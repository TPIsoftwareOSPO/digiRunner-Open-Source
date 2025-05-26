package tpi.dgrv4.dpaa.vo;

public class DPB0075Req {
	
	/** PK	做為分頁使用, 必需是 List 回傳的最後一筆 */
	private String apiKey;
	
	/** PK	做為分頁使用, 必需是 List 回傳的最後一筆 */
	private String moduleName;
	
	/** 模糊搜尋	每一個字串可以使用"空白鍵" 隔開 */
	private String keyword;
	
	/** 上架或下架	0：下架，1：上架 */
	private String dpStatus;
 
	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getDpStatus() {
		return dpStatus;
	}
	
	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getKeyword() {
		return keyword;
	}
	
	public void setDpStatus(String dpStatus) {
		this.dpStatus = dpStatus;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

}
