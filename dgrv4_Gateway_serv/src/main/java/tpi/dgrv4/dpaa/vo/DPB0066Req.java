package tpi.dgrv4.dpaa.vo;

public class DPB0066Req {

	/** PK from TSMP_DP_REQ_ORDERM */
	private Long reqOrdermId;

	/** lockVersion from TSMP_DP_REQ_ORDERM */
	private Long lv;

	/** 操作, S: 送審; U: 更新; R: 重送 */
	private String act;

	/** 申請說明 */
	private String reqDesc;

	/** 生效日期, 日期時間格式:YYYY/MM/DD */
	private String effectiveDate;

	/** 用戶申請, 若非此類別或送審時則不用傳入參數 */
	private DPB0066ApiApplication apiApplicationD;

	/** API上下架及異動, 若非此類別則不用傳入參數 */
	private DPB0066ApiOnOff apiOnOffD;

	/** 用戶註冊, 若非此類別或送審時則不用傳入參數 */
	private DPB0066ClientReg clientRegD;
	
	/** Open API Key申請/異動/撤銷, 若非此類別或送審時則不用傳入參數 */
	private DPB0066OpenApiKey openApiKeyD;

	public DPB0066Req() {
	}

	public Long getReqOrdermId() {
		return reqOrdermId;
	}

	public void setReqOrdermId(Long reqOrdermId) {
		this.reqOrdermId = reqOrdermId;
	}

	public Long getLv() {
		return lv;
	}

	public void setLv(Long lv) {
		this.lv = lv;
	}

	public String getAct() {
		return act;
	}

	public void setAct(String act) {
		this.act = act;
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

	public DPB0066ApiApplication getApiApplicationD() {
		return apiApplicationD;
	}

	public void setApiApplicationD(DPB0066ApiApplication apiApplicationD) {
		this.apiApplicationD = apiApplicationD;
	}

	public DPB0066ApiOnOff getApiOnOffD() {
		return apiOnOffD;
	}

	public void setApiOnOffD(DPB0066ApiOnOff apiOnOffD) {
		this.apiOnOffD = apiOnOffD;
	}

	public DPB0066ClientReg getClientRegD() {
		return clientRegD;
	}

	public void setClientRegD(DPB0066ClientReg clientRegD) {
		this.clientRegD = clientRegD;
	}

	public DPB0066OpenApiKey getOpenApiKeyD() {
		return openApiKeyD;
	}

	public void setOpenApiKeyD(DPB0066OpenApiKey openApiKeyD) {
		this.openApiKeyD = openApiKeyD;
	}

}
