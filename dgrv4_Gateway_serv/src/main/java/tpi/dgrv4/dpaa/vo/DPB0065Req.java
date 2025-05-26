package tpi.dgrv4.dpaa.vo;

public class DPB0065Req {

	/** 簽核類別:API_APPLICATION / API_ON_OFF */
	private String reqType;

	/** 簽核子類別:API_ON / API_OFF / API_ON_UPDATE */
	private String reqSubtype;

	/** 申請說明 */
	private String reqDesc;

	/** 生效日期, 日期時間格式:YYYY/MM/DD */
	private String effectiveDate;

	/** 用戶申請API, 若非此類別則不用傳入參數 */
	private DPB0065ApiApplication apiApplicationD;

	/** API上下架及異動, 若非此類別則不用傳入參數 */
	private DPB0065ApiOnOff apiOnOffD;

	/** 用戶註冊, 若非此類別則不用傳入參數 */
	private DPB0065ClientReg clientRegD;
	
	/** Open API Key申請/異動/撤銷, 若非此類別則不用傳入參數 */
	private DPB0065OpenApiKey openApiKeyD;

	public DPB0065Req() {
	}

	public String getReqType() {
		return reqType;
	}

	public void setReqType(String reqType) {
		this.reqType = reqType;
	}

	public String getReqSubtype() {
		return reqSubtype;
	}

	public void setReqSubtype(String reqSubtype) {
		this.reqSubtype = reqSubtype;
	}

	public String getReqDesc() {
		return reqDesc;
	}

	public void setReqDesc(String reqDesc) {
		this.reqDesc = reqDesc;
	}

	public String getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(String effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public DPB0065ApiApplication getApiApplicationD() {
		return apiApplicationD;
	}

	public void setApiApplicationD(DPB0065ApiApplication apiApplicationD) {
		this.apiApplicationD = apiApplicationD;
	}

	public DPB0065ApiOnOff getApiOnOffD() {
		return apiOnOffD;
	}

	public void setApiOnOffD(DPB0065ApiOnOff apiOnOffD) {
		this.apiOnOffD = apiOnOffD;
	}

	public DPB0065ClientReg getClientRegD() {
		return clientRegD;
	}

	public void setClientRegD(DPB0065ClientReg clientRegD) {
		this.clientRegD = clientRegD;
	}

	public DPB0065OpenApiKey getOpenApiKeyD() {
		return openApiKeyD;
	}

	public void setOpenApiKeyD(DPB0065OpenApiKey openApiKeyD) {
		this.openApiKeyD = openApiKeyD;
	}

}
