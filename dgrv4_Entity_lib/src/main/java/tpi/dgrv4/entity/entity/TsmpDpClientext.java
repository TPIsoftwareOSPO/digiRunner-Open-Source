package tpi.dgrv4.entity.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

import tpi.dgrv4.entity.component.fuzzy.Fuzzy;
import tpi.dgrv4.entity.component.fuzzy.FuzzyEntityListener;

@Entity
@Table(name = "tsmp_dp_clientext")
@EntityListeners(FuzzyEntityListener.class)
public class TsmpDpClientext extends BasicFields {

	@Id
	@Column(name = "client_id")
	private String clientId;

	/** 存入前請自行呼叫 SeqStoreService 產生編號 */
	private Long clientSeqId;

	@Fuzzy
	@Column(name = "content_txt")
	private String contentTxt;

	/** 會員資格狀態=0：儲存，1：送審，2：放行，3：退回，4：重新送審 */
	@Column(name = "reg_status")
	private String regStatus = "0";

	/** 密碼狀態=1：可使用，2：重置等待確認 */
	@Column(name = "pwd_status")
	private String pwdStatus = "1";

	@Column(name = "pwd_reset_key")
	private String pwdResetKey;

	@Fuzzy
	@Column(name = "review_remark")
	private String reviewRemark;

	@Column(name = "ref_review_user")
	private String refReviewUser;

	@Column(name = "resubmit_date_time")
	@Temporal(TemporalType.DATE)
	private Date resubmitDateTime;

	@Column(name = "public_flag")
	private String publicFlag;

	@Transient
	private String resubmitDateTimeForLongTime;
	/* constructors */

	public TsmpDpClientext() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpDpClientext [clientId=" + clientId + ", clientSeqId=" + clientSeqId + ", contentTxt=" + contentTxt + ", regStatus=" + regStatus
				+ ", pwdStatus=" + pwdStatus + ", pwdResetKey=" + pwdResetKey + ", reviewRemark=" + reviewRemark
				+ ", refReviewUser=" + refReviewUser + ", resubmitDateTime=" + resubmitDateTime
				+ ", publicFlag=" + publicFlag
				+ ", getCreateDateTime()=" + getCreateDateTime() + ", getCreateUser()=" + getCreateUser()
				+ ", getUpdateDateTime()=" + getUpdateDateTime() + ", getUpdateUser()=" + getUpdateUser()
				+ ", getVersion()=" + getVersion() + ", getKeywordSearch()=" + getKeywordSearch() + "]";
	}

	/* getters and setters */

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public Long getClientSeqId() {
		return clientSeqId;
	}

	public void setClientSeqId(Long clientSeqId) {
		this.clientSeqId = clientSeqId;
	}

	public String getContentTxt() {
		return contentTxt;
	}

	public void setContentTxt(String contentTxt) {
		this.contentTxt = contentTxt;
	}

	public String getRegStatus() {
		return regStatus;
	}

	public void setRegStatus(String regStatus) {
		this.regStatus = regStatus;
	}

	public String getPwdStatus() {
		return pwdStatus;
	}

	public void setPwdStatus(String pwdStatus) {
		this.pwdStatus = pwdStatus;
	}

	public String getPwdResetKey() {
		return pwdResetKey;
	}

	public void setPwdResetKey(String pwdResetKey) {
		this.pwdResetKey = pwdResetKey;
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

	public Date getResubmitDateTime() {
		return resubmitDateTime;
	}

	public void setResubmitDateTime(Date resubmitDateTime) {
		this.resubmitDateTime = resubmitDateTime;
	}

	public String getPublicFlag() {
		return publicFlag;
	}

	public void setPublicFlag(String publicFlag) {
		this.publicFlag = publicFlag;
	}

	@Transient
	public String getResubmitDateTimeForLongTime() {
		return resubmitDateTimeForLongTime;
	}

	public void setResubmitDateTimeForLongTime(String resubmitDateTimeForLongTime) {
		this.resubmitDateTimeForLongTime = resubmitDateTimeForLongTime;
	}
	
	
	
}
