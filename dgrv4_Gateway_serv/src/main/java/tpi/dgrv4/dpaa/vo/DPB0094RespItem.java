package tpi.dgrv4.dpaa.vo;

public class DPB0094RespItem {

	/** Open API Key ID */
	private Long openApiKeyId;

	/** Client ID (用戶代碼) */
	private String clientId;

	/** 用戶端代號 parser TSMP_CLIENT */
	private String clientName;

	/** 用戶端名稱 parser TSMP_CLIENT */
	private String clientAlias;

	/** Open API Key */
	private String openApiKey;

	/** Open API Key 別名 */
	private String openApiKeyAlias;

	/** Open API Key 建立時間 格式:yyyy/MM/dd */
	private String createDateTime;

	/** Open API Key 效期 格式:yyyy/MM/dd */
	private String expiredAt;

	/** Open API Key 撤銷時間 格式:yyyy/MM/dd */
	private String revokedAt;
	
	/** Open API Key 狀態 1 / 0 */
	private String openApiKeyStatus;

	/**
	 * 1. 啟用 / 停用 / 啟用(逾期) / 啟用(逾期)(已展期) / 啟用(已展期) 
	 * 2. 若啟用的Open API Key效期已過,狀態顯示"啟用(逾期)"
	 * 3. 若tsmp_open_apikey.rollover_flag = "Y", 則顯示 "(已展期)"
	 */
	private String openApiKeyStatusName;

	public Long getOpenApiKeyId() {
		return openApiKeyId;
	}

	public void setOpenApiKeyId(Long openApiKeyId) {
		this.openApiKeyId = openApiKeyId;
	}

	public String getClientId() {
		return clientId;
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

	public String getOpenApiKey() {
		return openApiKey;
	}
	
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public void setOpenApiKey(String openApiKey) {
		this.openApiKey = openApiKey;
	}

	public String getOpenApiKeyAlias() {
		return openApiKeyAlias;
	}

	public void setOpenApiKeyAlias(String openApiKeyAlias) {
		this.openApiKeyAlias = openApiKeyAlias;
	}

	public String getCreateDateTime() {
		return createDateTime;
	}

	public String getExpiredAt() {
		return expiredAt;
	}

	public void setExpiredAt(String expiredAt) {
		this.expiredAt = expiredAt;
	}

	public String getRevokedAt() {
		return revokedAt;
	}

	public void setRevokedAt(String revokedAt) {
		this.revokedAt = revokedAt;
	}
	
	public void setCreateDateTime(String createDateTime) {
		this.createDateTime = createDateTime;
	}

	public String getOpenApiKeyStatus() {
		return openApiKeyStatus;
	}

	public void setOpenApiKeyStatus(String openApiKeyStatus) {
		this.openApiKeyStatus = openApiKeyStatus;
	}

	public String getOpenApiKeyStatusName() {
		return openApiKeyStatusName;
	}

	public void setOpenApiKeyStatusName(String openApiKeyStatusName) {
		this.openApiKeyStatusName = openApiKeyStatusName;
	}

}
