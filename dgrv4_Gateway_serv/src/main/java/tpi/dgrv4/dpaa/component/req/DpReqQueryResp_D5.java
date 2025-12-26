package tpi.dgrv4.dpaa.component.req;

import java.util.List;

public class DpReqQueryResp_D5 {

	private Long reqOrderd5Id;

	private String clientId;

	private String clientName;

	private String clientAlias;

	private Long openApiKeyId;

	private String openApiKey;

	private String secretKey;

	/** Open API Key 別名 */
	private String openApiKeyAlias;

	/** 效期 */
	private String expiredAt;

	/** 使用次數上限 */
	private Integer timesThreshold;

	/** API清單 */
	private List<DpReqQueryResp_D5d> d5dRespList;

	public Long getReqOrderd5Id() {
		return reqOrderd5Id;
	}

	public void setReqOrderd5Id(Long reqOrderd5Id) {
		this.reqOrderd5Id = reqOrderd5Id;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getClientAlias() {
		return clientAlias;
	}

	public void setClientAlias(String clientAlias) {
		this.clientAlias = clientAlias;
	}

	public Long getOpenApiKeyId() {
		return openApiKeyId;
	}

	public String getOpenApiKey() {
		return openApiKey;
	}

	public void setOpenApiKey(String openApiKey) {
		this.openApiKey = openApiKey;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public void setOpenApiKeyId(Long openApiKeyId) {
		this.openApiKeyId = openApiKeyId;
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

	public void setTimesThreshold(Integer timesThreshold) {
		this.timesThreshold = timesThreshold;
	}

	public List<DpReqQueryResp_D5d> getD5dRespList() {
		return d5dRespList;
	}

	public void setD5dRespList(List<DpReqQueryResp_D5d> d5dRespList) {
		this.d5dRespList = d5dRespList;
	}

}
