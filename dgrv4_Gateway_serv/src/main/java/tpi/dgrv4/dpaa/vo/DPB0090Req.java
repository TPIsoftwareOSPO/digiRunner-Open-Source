package tpi.dgrv4.dpaa.vo;

public class DPB0090Req {

	/** Open API Key PK 做為分頁使用, 必需是 List 回傳的最後一筆 */
	private Long openApiKeyId;

	/** Client ID (用戶代碼) */
	private String clientId;

	/** 前後台分類 */
	private String fbTypeEncode;
	
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

	public String getFbTypeEncode() {
		return fbTypeEncode;
	}

	public void setFbTypeEncode(String fbTypeEncode) {
		this.fbTypeEncode = fbTypeEncode;
	}

}
