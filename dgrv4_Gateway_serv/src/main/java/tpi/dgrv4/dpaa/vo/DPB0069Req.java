package tpi.dgrv4.dpaa.vo;

public class DPB0069Req {
	
	/** 簽核單Id	做查詢條件 */
	private Long reqOrdermId;
	
	/** PK	做為分頁使用, 必需是 List 回傳的最後一筆 */
	private Long chkLogId;
	
	/** httpToken 這2個欄位用來檢查是否有權限查看, 必需user所屬組織以下, 及有審核權才行*/
	private String orgId;
	
	/** httpToken 這2個欄位用來檢查是否有權限查看, 必需user所屬組織以下, 及有審核權才行*/
	private String userName;

	public Long getReqOrdermId() {
		return reqOrdermId;
	}

	public void setReqOrdermId(Long reqOrdermId) {
		this.reqOrdermId = reqOrdermId;
	}

	public Long getChkLogId() {
		return chkLogId;
	}

	public void setChkLogId(Long chkLogId) {
		this.chkLogId = chkLogId;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
}
