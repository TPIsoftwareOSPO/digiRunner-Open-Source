package tpi.dgrv4.dpaa.vo;

public class AA0319RespItem {

	/** API ID */
	private String apiKey;

	/** 模組名稱 */
	private String moduleName;

	/** 匯入結果 */
	private AA0319Pair result;

	/** 描述 */
	private AA0319Trunc desc;

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

	public AA0319Pair getResult() {
		return result;
	}

	public void setResult(AA0319Pair result) {
		this.result = result;
	}

	public AA0319Trunc getDesc() {
		return desc;
	}

	public void setDesc(AA0319Trunc desc) {
		this.desc = desc;
	}

}