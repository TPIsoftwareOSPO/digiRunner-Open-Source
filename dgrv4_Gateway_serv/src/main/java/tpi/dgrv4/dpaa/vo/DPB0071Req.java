package tpi.dgrv4.dpaa.vo;

public class DPB0071Req {

	/** PK */
	private Long reqOrdermId;

	/** BcryptParam, 編碼過的 subItemNo, 加密內容:ACCEPT,DENIED,RETURN,END */
	private String encodeSubItemNo;

	/** 最近關卡審核狀態代碼, update 的 where 條件 */
	private String chkStatus;

	/** 審核意見 */
	private String reqComment;

	public DPB0071Req() {}

	public Long getReqOrdermId() {
		return reqOrdermId;
	}

	public void setReqOrdermId(Long reqOrdermId) {
		this.reqOrdermId = reqOrdermId;
	}

	public String getEncodeSubItemNo() {
		return encodeSubItemNo;
	}

	public void setEncodeSubItemNo(String encodeSubItemNo) {
		this.encodeSubItemNo = encodeSubItemNo;
	}

	public String getChkStatus() {
		return chkStatus;
	}

	public void setChkStatus(String chkStatus) {
		this.chkStatus = chkStatus;
	}

	public String getReqComment() {
		return reqComment;
	}

	public void setReqComment(String reqComment) {
		this.reqComment = reqComment;
	}

}
