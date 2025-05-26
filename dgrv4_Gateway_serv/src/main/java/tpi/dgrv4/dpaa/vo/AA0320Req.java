package tpi.dgrv4.dpaa.vo;

public class AA0320Req {

	/** 關鍵字*/
	private String keyword;
	
	/** 群組編號	*/
	private String gId;

	/** 模組名稱	*/
	private String moduleName;
	
	/** API ID*/
	private String apiKey;

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getgId() {
		return gId;
	}

	public void setgId(String gId) {
		this.gId = gId;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	@Override
	public String toString() {
		return "AA0320Resp [keyword=" + keyword + ", gId=" + gId + ", moduleName=" + moduleName + ", apiKey=" + apiKey
				+ "]";
	}

}
