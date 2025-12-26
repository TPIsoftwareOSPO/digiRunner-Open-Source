package tpi.dgrv4.dpaa.vo;

public class DPB0068Resp {

	/**  */
	private Long reqOrdermId;

	/** 主類別 */
	private String reqType;

	/** 標題 (主類別名稱) */
	private String title;

	/** 案件編號 */
	private String reqOrderNo;

	/** 申請日期 (yyyy-MM-dd) */
	private String createDateTime;

	/** 申請人員 (有reqUserId -> userName就用, 否則用clientId -> clientName) */
	private String applyUserName;

	/** 申請單位 */
	private String orgName;
	
	/** 申請項目子分類代碼 */
	private String reqSubtype;

	/** 申請項目子分類 (申請子類別名稱) */
	private String subTitle;

	/** 最近關卡審核狀態代碼 */
	private String chkStatus;

	/** 最近關卡審核狀態名稱 */
	private String chkStatusName;

	/** 最近關卡 (關卡代碼) */
	private String nextChkPoint;

	/** 最近關卡名稱 */
	private String chkPointName;

	/** version	from TSMP_DP_REQ_ORDERM (主表) */
	private Long lv;

	/** 申請說明, from TSMP_DP_REQ_ORDERM (主表) */
	private String reqDesc;

	/** 生效日期, YYYY/MM/DD */
	private String effectiveDate;

	/** 表身: 用戶申請 */
	private DPB0068ApiUserApply apiUserApply;
	
	/** 表身: API上下架 */
	private DPB0068ApiOnOff apiOnOff;

	/** 表身: 用戶註冊 */
	private DPB0068ClientReg clientReg;
	
	/** 表身: Open API Key */
	private DPB0068OpenApiKey openApiKey;

	public DPB0068Resp() {}

	public Long getReqOrdermId() {
		return reqOrdermId;
	}

	public void setReqOrdermId(Long reqOrdermId) {
		this.reqOrdermId = reqOrdermId;
	}

	public String getReqType() {
		return reqType;
	}

	public void setReqType(String reqType) {
		this.reqType = reqType;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getReqOrderNo() {
		return reqOrderNo;
	}

	public void setReqOrderNo(String reqOrderNo) {
		this.reqOrderNo = reqOrderNo;
	}

	public String getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(String createDateTime) {
		this.createDateTime = createDateTime;
	}

	public String getApplyUserName() {
		return applyUserName;
	}

	public void setApplyUserName(String applyUserName) {
		this.applyUserName = applyUserName;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}
	
	public String getReqSubtype() {
		return reqSubtype;
	}

	public void setReqSubtype(String reqSubtype) {
		this.reqSubtype = reqSubtype;
	}

	public String getSubTitle() {
		return subTitle;
	}

	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
	}

	public String getChkStatus() {
		return chkStatus;
	}

	public void setChkStatus(String chkStatus) {
		this.chkStatus = chkStatus;
	}

	public String getChkStatusName() {
		return chkStatusName;
	}

	public void setChkStatusName(String chkStatusName) {
		this.chkStatusName = chkStatusName;
	}

	public String getNextChkPoint() {
		return nextChkPoint;
	}

	public void setNextChkPoint(String nextChkPoint) {
		this.nextChkPoint = nextChkPoint;
	}

	public String getChkPointName() {
		return chkPointName;
	}

	public void setChkPointName(String chkPointName) {
		this.chkPointName = chkPointName;
	}

	public Long getLv() {
		return lv;
	}

	public void setLv(Long lv) {
		this.lv = lv;
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

	public DPB0068ApiUserApply getApiUserApply() {
		return apiUserApply;
	}

	public void setApiUserApply(DPB0068ApiUserApply apiUserApply) {
		this.apiUserApply = apiUserApply;
	}

	public DPB0068ApiOnOff getApiOnOff() {
		return apiOnOff;
	}

	public void setApiOnOff(DPB0068ApiOnOff apiOnOff) {
		this.apiOnOff = apiOnOff;
	}

	public DPB0068ClientReg getClientReg() {
		return clientReg;
	}

	public void setClientReg(DPB0068ClientReg clientReg) {
		this.clientReg = clientReg;
	}

	public DPB0068OpenApiKey getOpenApiKey() {
		return openApiKey;
	}

	public void setOpenApiKey(DPB0068OpenApiKey openApiKey) {
		this.openApiKey = openApiKey;
	}
	
}
