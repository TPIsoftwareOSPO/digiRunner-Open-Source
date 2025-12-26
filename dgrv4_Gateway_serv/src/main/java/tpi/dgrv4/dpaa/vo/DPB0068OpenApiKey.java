package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0068OpenApiKey {

	/** PK */
	private Long reqOrderd5Id;
	
	/** 用戶ID	欲申請使用Open API Key的用戶 */
	private String clientId;
	
	/** 用戶端代號 */
	private String clientName;
	
	/** Open API Key ID	Open API Key 異動 / Open API Key 撤銷, 若非此子類別則不用傳入參數 */
	private Long openApiKeyId;

	/** Open API Key Open API Key 異動 / Open API Key 撤銷, 若非此子類別則不用傳入參數 */
	private String openApiKey;

	/** Secret Key Open API Key 異動 / Open API Key 撤銷, 若非此子類別則不用傳入參數 */
	private String secretKey;

	/** Open API Key 別名 */
	private String openApiKeyAlias;

	/** 效期 */
	private String expiredAt;
	
	/** 用戶端名稱 parser TSMP_CLIENT */
	private String clientAlias;

	/** 使用次數上限 */
	private Integer timesThreshold;

	/** API清單 */
	private List<DPB0068D5> apiDatas;

	public Long getReqOrderd5Id() {
		return reqOrderd5Id;
	}

	public void setReqOrderd5Id(Long reqOrderd5Id) {
		this.reqOrderd5Id = reqOrderd5Id;
	}

	public String getClientId() {
		return clientId;
	}
	
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getClientName() {
		return clientName;
	}
	
	public String getClientAlias() {
		return clientAlias;
	}
	
	public Long getOpenApiKeyId() {
		return openApiKeyId;
	}

	public void setClientAlias(String clientAlias) {
		this.clientAlias = clientAlias;
	}

	public void setOpenApiKeyId(Long openApiKeyId) {
		this.openApiKeyId = openApiKeyId;
	}
	
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getOpenApiKey() {
		return openApiKey;
	}

	public void setOpenApiKey(String openApiKey) {
		this.openApiKey = openApiKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getOpenApiKeyAlias() {
		return openApiKeyAlias;
	}

	public void setOpenApiKeyAlias(String openApiKeyAlias) {
		this.openApiKeyAlias = openApiKeyAlias;
	}

	public String getExpiredAt() {
		return expiredAt;
	}

	public void setExpiredAt(String expiredAt) {
		this.expiredAt = expiredAt;
	}

	public Integer getTimesThreshold() {
		return timesThreshold;
	}
	
	public String getSecretKey() {
		return secretKey;
	}

	public void setTimesThreshold(Integer timesThreshold) {
		this.timesThreshold = timesThreshold;
	}

	public List<DPB0068D5> getApiDatas() {
		return apiDatas;
	}

	public void setApiDatas(List<DPB0068D5> apiDatas) {
		this.apiDatas = apiDatas;
	}

}
