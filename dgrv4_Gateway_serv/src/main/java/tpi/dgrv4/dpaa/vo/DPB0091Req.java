package tpi.dgrv4.dpaa.vo;

public class DPB0091Req {

	/** API PK 做為分頁使用, 必需是 List 回傳的最後一筆 */
	private String apiKey;

	/** API PK 做為分頁使用, 必需是 List 回傳的最後一筆 */
	private String moduleName;

	/** Open API PK */
	private Long openApiKeyId;

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

	public Long getOpenApiKeyId() {
		return openApiKeyId;
	}

	public void setOpenApiKeyId(Long openApiKeyId) {
		this.openApiKeyId = openApiKeyId;
	}
 
}
