package tpi.dgrv4.entity.entity.jpql;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.Version;

import tpi.dgrv4.common.utils.DateTimeUtil;

@Entity
@Table(name = "tsmp_dp_req_orderd4")
@TableGenerator(name = "seq_store", initialValue = 2_000_000_000)
public class TsmpDpReqOrderd4 {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_store")
	@Column(name = "req_orderd4_id")
	private Long reqOrderd4Id;

	@Column(name = "ref_req_orderm_id")
	private Long refReqOrdermId;

	@Column(name = "user_id")
	private String userId;

	@Column(name = "article")
	private String article;

	@Column(name = "is_publish")
	private Integer isPublish;

	@Column(name = "create_date_time")
	private Date createDateTime = DateTimeUtil.now();

	@Column(name = "create_user")
	private String createUser = "SYSTEM";

	@Column(name = "update_date_time")
	private Date updateDateTime;

	@Column(name = "update_user")
	private String updateUser;

	@Version
	@Column(name = "version")
	private Long version = 1L;

	/* constructors */

	public TsmpDpReqOrderd4() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpDpReqOrderd4 [reqOrderd4Id=" + reqOrderd4Id + ", refReqOrdermId=" + refReqOrdermId + ", userId="
				+ userId + ", article=" + article + ", isPublish=" + isPublish + ", createDateTime=" + createDateTime
				+ ", createUser=" + createUser + ", updateDateTime=" + updateDateTime + ", updateUser=" + updateUser
				+ ", version=" + version + "]";
	}

	/* getters and setters */

	public Long getReqOrderd4Id() {
		return reqOrderd4Id;
	}

	public void setReqOrderd4Id(Long reqOrderd4Id) {
		this.reqOrderd4Id = reqOrderd4Id;
	}

	public Long getRefReqOrdermId() {
		return refReqOrdermId;
	}

	public void setRefReqOrdermId(Long refReqOrdermId) {
		this.refReqOrdermId = refReqOrdermId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getArticle() {
		return article;
	}

	public void setArticle(String article) {
		this.article = article;
	}

	public Integer getIsPublish() {
		return isPublish;
	}

	public void setIsPublish(Integer isPublish) {
		this.isPublish = isPublish;
	}

	public Date getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(Date createDateTime) {
		this.createDateTime = createDateTime;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public Date getUpdateDateTime() {
		return updateDateTime;
	}

	public void setUpdateDateTime(Date updateDateTime) {
		this.updateDateTime = updateDateTime;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

}
