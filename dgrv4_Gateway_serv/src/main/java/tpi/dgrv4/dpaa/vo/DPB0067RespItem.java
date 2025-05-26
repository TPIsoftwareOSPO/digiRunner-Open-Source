package tpi.dgrv4.dpaa.vo;

public class DPB0067RespItem {

	/** PK */
	private Long reqOrdermId;

	/** 申請日期 */
	private String createDateTime;

	/** 案件編號 */
	private String reqOrderNo;

	/**  */
	private String clientId;

	/**  */
	private String reqUserId;

	/** 申請者:有reqUserId 就用它找 UserName, 沒有就用clientId 找 clientName */
	private String applyUserName;

	/** 
	 * 申請類別:
	 * 對應到TSMP_DP_ITEMS的REVIEW_TYPE或子類別
	 * 如果有子類別,就用子類別名稱回傳,否則用父類別名稱回傳
	 */
	private String applyType;

	/** 即將要簽核的關卡代碼,ex: 1, 2, 3 */
	private Integer nextCheckPoint;

	/** 即將要簽核的關卡名稱,ex: 經辦,主管 */
	private String checkPointName;

	/** 即將要簽核的關卡審核狀態名稱 */
	private String chkStatus;

	/** 單位名稱 */
	private String orgName;

	/** [簽核]	送審了且User為審核者 = true */
	private String reviewVisiable;

	/** [歷程]	每個case 都 = true */
	private String trakerVisiable;

	/** [結案]	未有任何簽核且 User為申請者 = true */
	private String closeVisiable;

	/** [重送]	被簽核者退回且 User為申請者 = true */
	private String resendVisiable;

	/** [刪除]	未有任何簽核且 User為申請者 = true
	 * 2020/03/18 deprecated
	private String deleteVisiable;
	*/

	/** [更新]	未有任何簽核且 User為申請者 = true */
	private String updateVisiable;

	/** [送審] 關卡停留在申請者, 且User為申請者時, 回傳"true" */
	private String sendVisible;

	public DPB0067RespItem() {}

	public Long getReqOrdermId() {
		return reqOrdermId;
	}

	public void setReqOrdermId(Long reqOrdermId) {
		this.reqOrdermId = reqOrdermId;
	}

	public String getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(String createDateTime) {
		this.createDateTime = createDateTime;
	}

	public String getReqOrderNo() {
		return reqOrderNo;
	}

	public void setReqOrderNo(String reqOrderNo) {
		this.reqOrderNo = reqOrderNo;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getReqUserId() {
		return reqUserId;
	}

	public void setReqUserId(String reqUserId) {
		this.reqUserId = reqUserId;
	}

	public String getApplyUserName() {
		return applyUserName;
	}

	public void setApplyUserName(String applyUserName) {
		this.applyUserName = applyUserName;
	}

	public String getApplyType() {
		return applyType;
	}

	public void setApplyType(String applyType) {
		this.applyType = applyType;
	}

	public Integer getNextCheckPoint() {
		return nextCheckPoint;
	}

	public void setNextCheckPoint(Integer nextCheckPoint) {
		this.nextCheckPoint = nextCheckPoint;
	}

	public String getCheckPointName() {
		return checkPointName;
	}

	public void setCheckPointName(String checkPointName) {
		this.checkPointName = checkPointName;
	}

	public String getChkStatus() {
		return chkStatus;
	}

	public void setChkStatus(String chkStatus) {
		this.chkStatus = chkStatus;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getReviewVisiable() {
		return reviewVisiable;
	}

	public void setReviewVisiable(String reviewVisiable) {
		this.reviewVisiable = reviewVisiable;
	}

	public String getTrakerVisiable() {
		return trakerVisiable;
	}

	public void setTrakerVisiable(String trakerVisiable) {
		this.trakerVisiable = trakerVisiable;
	}

	public String getCloseVisiable() {
		return closeVisiable;
	}

	public void setCloseVisiable(String closeVisiable) {
		this.closeVisiable = closeVisiable;
	}

	public String getResendVisiable() {
		return resendVisiable;
	}

	public void setResendVisiable(String resendVisiable) {
		this.resendVisiable = resendVisiable;
	}

	/*
	public String getDeleteVisiable() {
		return deleteVisiable;
	}

	public void setDeleteVisiable(String deleteVisiable) {
		this.deleteVisiable = deleteVisiable;
	}
	*/

	public String getUpdateVisiable() {
		return updateVisiable;
	}

	public void setUpdateVisiable(String updateVisiable) {
		this.updateVisiable = updateVisiable;
	}

	public String getSendVisible() {
		return sendVisible;
	}

	public void setSendVisible(String sendVisible) {
		this.sendVisible = sendVisible;
	}
	
	@Override
	public String toString() {
		return "DPB0067RespItem [reqOrdermId=" + reqOrdermId + ", createDateTime=" + createDateTime + ", reqOrderNo="
				+ reqOrderNo + ", clientId=" + clientId + ", reqUserId=" + reqUserId + ", applyUserName="
				+ applyUserName + ", applyType=" + applyType + ", nextCheckPoint=" + nextCheckPoint
				+ ", checkPointName=" + checkPointName + ", chkStatus=" + chkStatus + ", orgName=" + orgName
				+ ", reviewVisiable=" + reviewVisiable + ", trakerVisiable=" + trakerVisiable + ", closeVisiable="
				+ closeVisiable + ", resendVisiable=" + resendVisiable + ", updateVisiable=" + updateVisiable + "]\n";
	}
}
