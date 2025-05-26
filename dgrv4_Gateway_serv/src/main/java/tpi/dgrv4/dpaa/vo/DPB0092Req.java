package tpi.dgrv4.dpaa.vo;

public class DPB0092Req {
	
	/** Client ID (用戶代碼) */
	private String clientId;

	/** user身份的組織代碼 資料來自 token, 不用 javascript 填入 */
	private String orgId;

	/** "申請項目 :編碼過的 reqType	ex: OPEN_API_KEY_APPLICA / OPEN_API_KEY_UPDATE / OPEN_API_KEY_REVOKE , 使用BcryptParam設計, itemNo="OPEN_API_KEY" */
	private String encodeReqSubtype;

	/** Open API Key ID Open API Key 異動 / Open API Key 撤銷, 若非此子類別則不用傳入參數 */
	private Long openApiKeyId;

	/** 前後台分類 */
	private String fbTypeEncode;

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getEncodeReqSubtype() {
		return encodeReqSubtype;
	}

	public void setEncodeReqSubtype(String encodeReqSubtype) {
		this.encodeReqSubtype = encodeReqSubtype;
	}

	public Long getOpenApiKeyId() {
		return openApiKeyId;
	}

	public void setOpenApiKeyId(Long openApiKeyId) {
		this.openApiKeyId = openApiKeyId;
	}

	public String getFbTypeEncode() {
		return fbTypeEncode;
	}

	public void setFbTypeEncode(String fbTypeEncode) {
		this.fbTypeEncode = fbTypeEncode;
	}

}
