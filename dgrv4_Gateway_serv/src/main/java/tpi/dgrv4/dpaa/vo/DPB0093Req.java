package tpi.dgrv4.dpaa.vo;

public class DPB0093Req {

	/** API PK 做為分頁使用, 必需是 List 回傳的最後一筆 */
	private String apiKey;

	/** API PK 做為分頁使用, 必需是 List 回傳的最後一筆 */
	private String moduleName;

	/** 模糊搜尋 每一個字串可以使用"空白鍵" 隔開, */
	private String keyword;

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
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

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

}
