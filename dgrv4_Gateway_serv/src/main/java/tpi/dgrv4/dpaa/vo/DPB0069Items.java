package tpi.dgrv4.dpaa.vo;

public class DPB0069Items {
	
	/** PK */
	private Long chkLogId;
	
	/** 異動日期 */
	private String chkCreateDateTime;
	
	/** 異動人員 */
	private String chkCreateUser;
	
	/** 關卡數 */
	private String layer;
	
	/** 關卡名稱 */
	private String chkLayerName;
	
	/** 狀態 */
	private String reviewStatus;
	
	/** 狀態名稱 */
	private String reviewStatusName;
	
	/** 審核意見 */
	private String reqComment;

	public Long getChkLogId() {
		return chkLogId;
	}
	
	@Override
	public String toString() {
		return "DPB0069Items [chkLogId=" + chkLogId + ", chkCreateDateTime=" + chkCreateDateTime + ", chkCreateUser="
				+ chkCreateUser + ", layer=" + layer + ", chkLayerName=" + chkLayerName + ", reviewStatus="
				+ reviewStatus + ", reviewStatusName=" + reviewStatusName + ", reqComment=" + reqComment + "]\n";
	}

	public void setChkLogId(Long chkLogId) {
		this.chkLogId = chkLogId;
	}

	public String getChkCreateDateTime() {
		return chkCreateDateTime;
	}

	public void setChkCreateDateTime(String chkCreateDateTime) {
		this.chkCreateDateTime = chkCreateDateTime;
	}

	public String getChkCreateUser() {
		return chkCreateUser;
	}

	public void setChkCreateUser(String chkCreateUser) {
		this.chkCreateUser = chkCreateUser;
	}

	public String getLayer() {
		return layer;
	}

	public void setLayer(String layer) {
		this.layer = layer;
	}

	public String getChkLayerName() {
		return chkLayerName;
	}

	public void setChkLayerName(String chkLayerName) {
		this.chkLayerName = chkLayerName;
	}

	public String getReviewStatus() {
		return reviewStatus;
	}

	public void setReviewStatus(String reviewStatus) {
		this.reviewStatus = reviewStatus;
	}

	public String getReviewStatusName() {
		return reviewStatusName;
	}

	public void setReviewStatusName(String reviewStatusName) {
		this.reviewStatusName = reviewStatusName;
	}

	public String getReqComment() {
		return reqComment;
	}

	public void setReqComment(String reqComment) {
		this.reqComment = reqComment;
	}
}
