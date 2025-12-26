package tpi.dgrv4.entity.entity.jpql;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import tpi.dgrv4.common.constant.TsmpDpDataStatus;
import tpi.dgrv4.common.utils.DateTimeUtil;

@Entity
@Table(name = "tsmp_dp_chk_layer")
@IdClass(TsmpDpChkLayerId.class)
public class TsmpDpChkLayer {

	@Column(name = "chk_layer_id")
	private Long chkLayerId;

	@Id
	@Column(name = "review_type")
	private String reviewType;

	@Id
	@Column(name = "layer")
	private Integer layer;

	@Id
	@Column(name = "role_id")
	private String roleId;

	@Column(name = "status")
	private String status = TsmpDpDataStatus.ON.value();

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

	public TsmpDpChkLayer() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpDpChkLayer [chkLayerId=" + chkLayerId + ", reviewType=" + reviewType + ", layer=" + layer
				+ ", roleId=" + roleId + ", status=" + status + ", createDateTime=" + createDateTime + ", createUser="
				+ createUser + ", updateDateTime=" + updateDateTime + ", updateUser=" + updateUser + ", version="
				+ version + "]\n";
	}

	/* getters and setters */

	public Long getChkLayerId() {
		return chkLayerId;
	}

	public void setChkLayerId(Long chkLayerId) {
		this.chkLayerId = chkLayerId;
	}

	public String getReviewType() {
		return reviewType;
	}

	public void setReviewType(String reviewType) {
		this.reviewType = reviewType;
	}

	public Integer getLayer() {
		return layer;
	}

	public void setLayer(Integer layer) {
		this.layer = layer;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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
