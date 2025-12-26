package tpi.dgrv4.dpaa.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DPB0092Resp {

	/** Client ID (用戶代碼) */
	private String clientId;

	/** 用戶端代號	parser TSMP_CLIENT */
	private String clientName;

	/** 用戶端名稱 parser TSMP_CLIENT */
	private String clientAlias;

	/** 申請日期 當日, 格式: yyyy/MM/dd */
	private String reqDate;

	/** 申請人員名稱 */
	private String userName;

	/** 申請人員:user身份的組織代碼 */
	private String orgId;

	/** 申請單位:user身份的組織名稱 */
	private String orgName;

	/** 生效日期 格式: yyyy/MM/dd */
	private String effectiveDate;

	/** 簽核子類別 OPEN_API_KEY_APPLICA / OPEN_API_KEY_UPDATE/ OPEN_API_KEY_REVOKE */
	private String reqSubtype;

	/** 申請項目 Open API Key 申請 / Open API Key 異動 / Open API Key 撤銷 */
	private String reqSubtypeName;
	
	/** Open API Key ID		Open API Key 異動 / Open API Key 撤銷, 若非此子類別則不用傳入參數 */
	private Long openApiKeyId;

	/** Open API Key Open 	API Key 異動 / Open API Key 撤銷, 若非此子類別則不用傳入參數 */
	private String openApiKey;

	/** Secret Key 	Open API Key 異動 / Open API Key 撤銷, 若非此子類別則不用傳入參數 */
	//checkmarx, Excessive Data Exposure
	@JsonProperty("secretKey")
	private String mimaKey;

	/** Open API Key 別名 Open API Key 異動 / Open API Key 撤銷, 若非此子類別則不用傳入參數 */
	private String openApiKeyAlias;

	/** 效期 Open API Key 異動 / Open API Key 撤銷, 若非此子類別則不用傳入參數 */
	private String expiredAt;

	/** 使用次數上限 Open API Key 異動 / Open API Key 撤銷, 若非此子類別則不用傳入參數 */
	private String timesThreshold;

	/** API清單 Open API Key 異動 / Open API Key 撤銷, 若非此子類別則不用傳入參數 */
	private List<DPB0092ApiItem> dataList;

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientName() {
		return clientName;
	}

	public String getClientId() {
		return clientId;
	}
	
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public void setClientAlias(String clientAlias) {
		this.clientAlias = clientAlias;
	}
	
	public String getClientAlias() {
		return clientAlias;
	}

	public String getReqDate() {
		return reqDate;
	}

	public void setReqDate(String reqDate) {
		this.reqDate = reqDate;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(String effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public String getReqSubtype() {
		return reqSubtype;
	}

	public void setReqSubtype(String reqSubtype) {
		this.reqSubtype = reqSubtype;
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
	
	public void setReqSubtypeName(String reqSubtypeName) {
		this.reqSubtypeName = reqSubtypeName;
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
	
	public String getReqSubtypeName() {
		return reqSubtypeName;
	}

	public void setOpenApiKeyAlias(String openApiKeyAlias) {
		this.openApiKeyAlias = openApiKeyAlias;
	}
	
	public String getTimesThreshold() {
		return timesThreshold;
	}

	public String getExpiredAt() {
		return expiredAt;
	}

	public void setExpiredAt(String expiredAt) {
		this.expiredAt = expiredAt;
	}

	public void setTimesThreshold(String timesThreshold) {
		this.timesThreshold = timesThreshold;
	}

	public List<DPB0092ApiItem> getDataList() {
		return dataList;
	}

	public void setDataList(List<DPB0092ApiItem> dataList) {
		this.dataList = dataList;
	}

}
