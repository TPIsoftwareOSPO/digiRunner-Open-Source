package tpi.dgrv4.entity.entity.jpql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import tpi.dgrv4.entity.component.fuzzy.Fuzzy;
import tpi.dgrv4.entity.component.fuzzy.FuzzyEntityListener;
import tpi.dgrv4.entity.entity.BasicFields;

@Entity
@Table(name = "tsmp_dp_api_auth2")
@EntityListeners(FuzzyEntityListener.class)
@TableGenerator(name = "seq_store", initialValue = 2_000_000_000)
public class TsmpDpApiAuth2 extends BasicFields {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_store")
	@Column(name = "api_auth_id")
	private Long apiAuthId;

	@Column(name = "ref_client_id")
	private String refClientId;

	@Column(name = "ref_api_uid")
	private String refApiUid;

	@Column(name = "apply_status")
	private String applyStatus;

	@Fuzzy
	@Column(name = "apply_purpose")
	private String applyPurpose;

	@Column(name = "ref_review_user")
	private String refReviewUser;

	@Fuzzy
	@Column(name = "review_remark")
	private String reviewRemark;

	/* constructors */

	public TsmpDpApiAuth2() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpDpApiAuth2 [apiAuthId=" + apiAuthId + ", refClientId=" + refClientId + ", refApiUid=" + refApiUid
				+ ", applyStatus=" + applyStatus + ", applyPurpose=" + applyPurpose + ", refReviewUser=" + refReviewUser
				+ ", reviewRemark=" + reviewRemark + ", getCreateDateTime()=" + getCreateDateTime()
				+ ", getCreateUser()=" + getCreateUser() + ", getUpdateDateTime()=" + getUpdateDateTime()
				+ ", getUpdateUser()=" + getUpdateUser() + ", getVersion()=" + getVersion() + ", getKeywordSearch()="
				+ getKeywordSearch() + "]";
	}

	/* getters and setters */

	public Long getApiAuthId() {
		return apiAuthId;
	}

	public void setApiAuthId(Long apiAuthId) {
		this.apiAuthId = apiAuthId;
	}

	public String getRefClientId() {
		return refClientId;
	}

	public void setRefClientId(String refClientId) {
		this.refClientId = refClientId;
	}

	public String getRefApiUid() {
		return refApiUid;
	}

	public void setRefApiUid(String refApiUid) {
		this.refApiUid = refApiUid;
	}

	public String getApplyStatus() {
		return applyStatus;
	}

	public void setApplyStatus(String applyStatus) {
		this.applyStatus = applyStatus;
	}

	public String getApplyPurpose() {
		return applyPurpose;
	}

	public void setApplyPurpose(String applyPurpose) {
		this.applyPurpose = applyPurpose;
	}

	public String getRefReviewUser() {
		return refReviewUser;
	}

	public void setRefReviewUser(String refReviewUser) {
		this.refReviewUser = refReviewUser;
	}

	public String getReviewRemark() {
		return reviewRemark;
	}

	public void setReviewRemark(String reviewRemark) {
		this.reviewRemark = reviewRemark;
	}

}
