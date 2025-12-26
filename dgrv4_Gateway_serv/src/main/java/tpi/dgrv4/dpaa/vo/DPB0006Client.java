package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0006Client {

	/** Client ID:base64.decode 後為 clientName */
	private String refClientId;

	/** 用戶端帳號 */
	private String clientId;

	/** 用戶端代號 */
	private String clientName;

	/** 用戶端名稱 */
	private String clientAlias;

	/** 電子郵件帳號 */
	private String emails;

	/** 用戶端狀態 */
	private String clientStatus;

	/** 申請會員說明 */
	private String applyPurpose;

	/** 會員資格審核備註 */
	private String reviewRemark;

	/** 審核人員 */
	private String refReviewUser;

	/** 重新送審日期 */
	private String resubmitDateTime;

	/** 狀態 */
	private String regStatus;

	/** 可否操作"申請API"按鈕	用戶註冊狀態必須是"已放行"才可申請API */
	private Boolean applyFlag;

	/** 附件清單 */
	private List<DPB0006File> fileList;

	/** 開放權限代碼	from tsmp_dp_clientext.public_flag */
	private String publicFlag;

	/** 開放權限名稱 */
	private String publicFlagName;
	
	private String status;
	
	private String statusName;
	
	private String chkStatus;
	
	private String chkStatusName;
	
	private String checkPointName;

	public DPB0006Client() {}

	public String getRefClientId() {
		return refClientId;
	}

	public void setRefClientId(String refClientId) {
		this.refClientId = refClientId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getClientAlias() {
		return clientAlias;
	}

	public void setClientAlias(String clientAlias) {
		this.clientAlias = clientAlias;
	}
	
	public String getClientName() {
		return clientName;
	}

	public void setEmails(String emails) {
		this.emails = emails;
	}

	public String getClientStatus() {
		return clientStatus;
	}

	public String getEmails() {
		return emails;
	}
	
	public void setClientStatus(String clientStatus) {
		this.clientStatus = clientStatus;
	}

	public String getApplyPurpose() {
		return applyPurpose;
	}

	public void setApplyPurpose(String applyPurpose) {
		this.applyPurpose = applyPurpose;
	}

	public String getReviewRemark() {
		return reviewRemark;
	}

	public void setReviewRemark(String reviewRemark) {
		this.reviewRemark = reviewRemark;
	}

	public String getRefReviewUser() {
		return refReviewUser;
	}

	public void setRefReviewUser(String refReviewUser) {
		this.refReviewUser = refReviewUser;
	}

	public String getResubmitDateTime() {
		return resubmitDateTime;
	}

	public void setResubmitDateTime(String resubmitDateTime) {
		this.resubmitDateTime = resubmitDateTime;
	}

	public String getRegStatus() {
		return regStatus;
	}

	public void setRegStatus(String regStatus) {
		this.regStatus = regStatus;
	}

	public Boolean getApplyFlag() {
		return applyFlag;
	}

	public void setApplyFlag(Boolean applyFlag) {
		this.applyFlag = applyFlag;
	}

	public List<DPB0006File> getFileList() {
		return fileList;
	}

	public void setFileList(List<DPB0006File> fileList) {
		this.fileList = fileList;
	}

	public String getPublicFlag() {
		return publicFlag;
	}

	public void setPublicFlag(String publicFlag) {
		this.publicFlag = publicFlag;
	}

	public String getPublicFlagName() {
		return publicFlagName;
	}

	public void setPublicFlagName(String publicFlagName) {
		this.publicFlagName = publicFlagName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatusName() {
		return statusName;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
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

	public String getCheckPointName() {
		return checkPointName;
	}

	public void setCheckPointName(String checkPointName) {
		this.checkPointName = checkPointName;
	}

	@Override
	public String toString() {
		return "DPB0006Client [refClientId=" + refClientId + ", clientId=" + clientId + ", clientName=" + clientName
				+ ", clientAlias=" + clientAlias + ", emails=" + emails + ", clientStatus=" + clientStatus
				+ ", applyPurpose=" + applyPurpose + ", reviewRemark=" + reviewRemark + ", refReviewUser="
				+ refReviewUser + ", resubmitDateTime=" + resubmitDateTime + ", regStatus=" + regStatus + ", applyFlag="
				+ applyFlag + ", fileList=" + fileList + ", publicFlag=" + publicFlag + ", publicFlagName="
				+ publicFlagName + ", status=" + status + ", statusName=" + statusName + ", chkStatus=" + chkStatus
				+ ", chkStatusName=" + chkStatusName + ", checkPointName=" + checkPointName + "]\n";
	}
}
