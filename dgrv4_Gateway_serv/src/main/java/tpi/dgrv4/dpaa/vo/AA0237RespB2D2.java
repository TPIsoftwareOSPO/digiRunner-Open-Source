package tpi.dgrv4.dpaa.vo;

public class AA0237RespB2D2 {
	
	/** TSMP_GROUP_API.api_key*/
	private String apiKey;
	
	/** TSMP_API.api_name*/
	private String apiName;

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}

	@Override
	public String toString() {
		return "AA0237RespB2D2 [apiKey=" + apiKey + ", apiName=" + apiName + "]";
	}

	
}
