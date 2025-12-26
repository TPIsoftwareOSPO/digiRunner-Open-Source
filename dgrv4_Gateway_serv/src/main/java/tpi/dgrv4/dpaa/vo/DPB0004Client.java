package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0004Client {

	/** Client ID:base64.decode 後為 clientName */
	private String refClientId;

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

	/** 附件清單 */
	private List<DPB0004File> fileList;

	public DPB0004Client() {}

	public String getRefClientId() {
		return refClientId;
	}

	public void setRefClientId(String refClientId) {
		this.refClientId = refClientId;
	}

	public String getApplyPurpose() {
		return applyPurpose;
	}

	public void setApplyPurpose(String applyPurpose) {
		this.applyPurpose = applyPurpose;
	}

	public void setReviewRemark(String reviewRemark) {
		this.reviewRemark = reviewRemark;
	}

	public String getReviewRemark() {
		return reviewRemark;
	}
	
	public String getRefReviewUser() {
		return refReviewUser;
	}

	public void setRefReviewUser(String refReviewUser) {
		this.refReviewUser = refReviewUser;
	}

	public void setResubmitDateTime(String resubmitDateTime) {
		this.resubmitDateTime = resubmitDateTime;
	}
	
	public String getResubmitDateTime() {
		return resubmitDateTime;
	}

	public String getRegStatus() {
		return regStatus;
	}

	public void setRegStatus(String regStatus) {
		this.regStatus = regStatus;
	}

	public List<DPB0004File> getFileList() {
		return fileList;
	}

	public void setFileList(List<DPB0004File> fileList) {
		this.fileList = fileList;
	}
	
}
