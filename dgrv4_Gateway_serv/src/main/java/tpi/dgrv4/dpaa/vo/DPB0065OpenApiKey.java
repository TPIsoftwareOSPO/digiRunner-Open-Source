package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0065OpenApiKey {

	/** Open API Key ID Open API Key 異動 / Open API Key 撤銷, 若非此子類別則不用傳入參數 */
	private Long openApiKeyId;

	/** Open API Key Open API Key 異動 / Open API Key 撤銷, 若非此子類別則不用傳入參數 */
	private String openApiKey;

	/** Secret Key Open API Key 異動 / Open API Key 撤銷, 若非此子類別則不用傳入參數 */
	private String secretKey;

	/** Open API Key 別名 */
	private String openApiKeyAlias;
	
	/** 用戶ID 欲申請使用Open API Key的用戶 */
	private String clientId;

	/** 使用次數上限 */
	private Integer timesThreshold;

	/** Open API Key 效期 1.日期時間格式: YYYY/MM/DD */
	private String expiredAt;

	/** 申請的 API UUID */
	private List<String> apiUids;

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public Long getOpenApiKeyId() {
		return openApiKeyId;
	}

	public void setOpenApiKeyId(Long openApiKeyId) {
		this.openApiKeyId = openApiKeyId;
	}

	public String getOpenApiKey() {
		return openApiKey;
	}

	public void setOpenApiKey(String openApiKey) {
		this.openApiKey = openApiKey;
	}
	
	public String getClientId() {
		return clientId;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public String getOpenApiKeyAlias() {
		return openApiKeyAlias;
	}

	public void setOpenApiKeyAlias(String openApiKeyAlias) {
		this.openApiKeyAlias = openApiKeyAlias;
	}
	
	public List<String> getApiUids() {
		return apiUids;
	}

	public Integer getTimesThreshold() {
		return timesThreshold;
	}

	public void setTimesThreshold(Integer timesThreshold) {
		this.timesThreshold = timesThreshold;
	}

	public String getExpiredAt() {
		return expiredAt;
	}

	public void setExpiredAt(String expiredAt) {
		this.expiredAt = expiredAt;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public void setApiUids(List<String> apiUids) {
		this.apiUids = apiUids;
	}

}
