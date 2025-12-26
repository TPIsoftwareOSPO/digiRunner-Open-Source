package tpi.dgrv4.dpaa.vo;

public class DPB0018Req {

	/** PK=做為分頁使用 */
	private String apiKey;

	/** PK=做為分頁使用 */
	private String moduleName;

	/** 模糊搜尋=每一個字串可以使用"空白鍵"隔開 */
	private String keyword;

	public DPB0018Req() {}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	
	public void setKeyword(String keyword) {
		this.keyword = keyword;
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

}
