package tpi.dgrv4.dpaa.component.req;

public class DpReqServiceSignReq {

	private Long reqOrdermId;

	/** bcryptParams, item_no = 'REVIEW_STATUS' */
	private String encNextReviewStatus;

	/** for 雙欄位更新 */
	private String currentReviewStatus;

	private String signUserName;

	private String orgId;
	
	private String idPType;

	/** tsmp_dp_req_orders.req_comment */
	private String reqComment;

	public DpReqServiceSignReq() {
	}

	public Long getReqOrdermId() {
		return reqOrdermId;
	}

	public void setReqOrdermId(Long reqOrdermId) {
		this.reqOrdermId = reqOrdermId;
	}

	public String getEncNextReviewStatus() {
		return encNextReviewStatus;
	}

	public void setEncNextReviewStatus(String encNextReviewStatus) {
		this.encNextReviewStatus = encNextReviewStatus;
	}

	public String getCurrentReviewStatus() {
		return currentReviewStatus;
	}

	public void setCurrentReviewStatus(String currentReviewStatus) {
		this.currentReviewStatus = currentReviewStatus;
	}

	public String getSignUserName() {
		return signUserName;
	}

	public void setSignUserName(String signUserName) {
		this.signUserName = signUserName;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getReqComment() {
		return reqComment;
	}

	public void setReqComment(String reqComment) {
		this.reqComment = reqComment;
	}

	public String getIdPType() {
		return idPType;
	}

	public void setIdPType(String idPType) {
		this.idPType = idPType;
	}
}
