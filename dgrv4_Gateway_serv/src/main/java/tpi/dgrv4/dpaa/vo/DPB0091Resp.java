package tpi.dgrv4.dpaa.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DPB0091Resp {
	/** Open API Key ID */
	private Long openApiKeyId;

	/** Client ID (用戶端代碼) */
	private String clientId;

	/** 用戶端代號	parser TSMP_CLIENT */
	private String clientName;

	/** 用戶端名稱 parser TSMP_CLIENT */
	private String clientAlias;

	/** Open API Key */
	private String openApiKey;

	/** Secret Key	只顯示前後五個字,中間其餘文字用*號顯示 */
	//checkmarx, Excessive Data Exposure
	@JsonProperty("secretKey")
	private String mimaKey;

	/** Open API Key 別名 */
	private String openApiKeyAlias;

	/** 可使用次數 */
	private Integer timesQuota;

	/** 使用次數上限 */
	private Integer timesThreshold;

	/** Open API Key 建立時間 格式: yyyy/MM/dd */
	private String createDateTime;

	/** Open API Key 效期 格式: yyyy/MM/dd */
	private String expiredAt;

	/** Open API Key 撤銷時間 格式: yyyy/MM/dd */
	private String revokedAt;

	/** Open API Key 狀態 1 / 0 */
	private String openApiKeyStatus;

	/**
	 * Open API Key 狀態中文	
	 * 1. 啟用 / 停用 / 啟用(逾期) / 啟用(逾期)(已展期) / 啟用(已展期) 
	 * 2. 若啟用的Open API Key效期已過,狀態顯示"啟用(逾期)"
	 * 3. 若tsmp_open_apikey.rollover_flag = "Y", 則顯示 "(已展期)"
	 */
	private String openApiKeyStatusName;

	/** API清單 */
	private List<DPB0091ApiItem> dataList;

	public Long getOpenApiKeyId() {
		return openApiKeyId;
	}

	public void setOpenApiKeyId(Long openApiKeyId) {
		this.openApiKeyId = openApiKeyId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
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

	public void setClientAlias(String clientAlias) {
		this.clientAlias = clientAlias;
	}

	public String getOpenApiKey() {
		return openApiKey;
	}

	public void setOpenApiKey(String openApiKey) {
		this.openApiKey = openApiKey;
	}

	public String getMimaKey() {
		return mimaKey;
	}

	public void setMimaKey(String mimaKey) {
		this.mimaKey = mimaKey;
	}

	public String getOpenApiKeyAlias() {
		return openApiKeyAlias;
	}

	public void setOpenApiKeyAlias(String openApiKeyAlias) {
		this.openApiKeyAlias = openApiKeyAlias;
	}

	public Integer getTimesQuota() {
		return timesQuota;
	}

	public void setTimesQuota(Integer timesQuota) {
		this.timesQuota = timesQuota;
	}

	public void setTimesThreshold(Integer timesThreshold) {
		this.timesThreshold = timesThreshold;
	}
	
	public Integer getTimesThreshold() {
		return timesThreshold;
	}

	public String getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(String createDateTime) {
		this.createDateTime = createDateTime;
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

	public List<DPB0091ApiItem> getDataList() {
		return dataList;
	}

	public void setDataList(List<DPB0091ApiItem> dataList) {
		this.dataList = dataList;
	}

}
