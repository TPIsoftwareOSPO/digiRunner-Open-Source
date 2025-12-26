package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0066OpenApiKey {
	
	/** Open API Key 別名 */
	private String openApiKeyAlias;

	/** 用戶ID 欲申請使用Open API Key的用戶 */
	private String clientId;

	/** Open API Key ID Open API Key 異動 / Open API Key 撤銷, 若非此子類別則不用傳入參數 */
	private Long openApiKeyId;

	/** Open API Key Open API Key 異動 / Open API Key 撤銷, 若非此子類別則不用傳入參數 */
	private String openApiKey;

	/** Secret Key Open API Key 異動 / Open API Key 撤銷, 若非此子類別則不用傳入參數 */
	private String secretKey;

	/** 使用次數上限 */
	private Integer timesThreshold;

	/** Open API Key 效期 1.日期時間格式: YYYY/MM/DD */
	private String expiredAt;

	/** 新的API UUID */
	private List<String> apiUids;

	public String getClientId() {
		return clientId;
	}

	public Long getOpenApiKeyId() {
		return openApiKeyId;
	}

	public String getOpenApiKey() {
		return openApiKey;
	}
	
	public void setOpenApiKeyId(Long openApiKeyId) {
		this.openApiKeyId = openApiKeyId;
	}

	public void setOpenApiKey(String openApiKey) {
		this.openApiKey = openApiKey;
	}

	public String getSecretKey() {
		return secretKey;
	}
	
	public String getExpiredAt() {
		return expiredAt;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getOpenApiKeyAlias() {
		return openApiKeyAlias;
	}

	public Integer getTimesThreshold() {
		return timesThreshold;
	}
	
	public void setOpenApiKeyAlias(String openApiKeyAlias) {
		this.openApiKeyAlias = openApiKeyAlias;
	}

	public void setTimesThreshold(Integer timesThreshold) {
		this.timesThreshold = timesThreshold;
	}

	public void setExpiredAt(String expiredAt) {
		this.expiredAt = expiredAt;
	}

	public List<String> getApiUids() {
		return apiUids;
	}
	
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public void setApiUids(List<String> apiUids) {
		this.apiUids = apiUids;
	}

}
