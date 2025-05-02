package tpi.dgrv4.entity.entity.jpql;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tsmp_dc")
public class TsmpDc {

	@Id
	@Column(name = "dc_id")
	private Long dcId;

	@Column(name = "dc_code")
	private String dcCode;

	@Column(name = "dc_memo")
	private String dcMemo;

	@Column(name = "active")
	private Boolean active;

	@Column(name = "create_user")
	private String createUser;

	@Column(name = "create_time")
	private Date createTime;

	@Column(name = "update_user")
	private String updateUser;

	@Column(name = "update_time")
	private Date updateTime;

	/* constructors */

	public TsmpDc() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpDc [dcId=" + dcId + ", dcCode=" + dcCode + ", dcMemo=" + dcMemo + ", active=" + active
				+ ", createUser=" + createUser + ", createTime=" + createTime + ", updateUser=" + updateUser
				+ ", updateTime=" + updateTime + "]\n";
	}

	/* getters and setters */

	public Long getDcId() {
		return dcId;
	}

	public void setDcId(Long dcId) {
		this.dcId = dcId;
	}

	public String getDcCode() {
		return dcCode;
	}

	public void setDcCode(String dcCode) {
		this.dcCode = dcCode;
	}

	public String getDcMemo() {
		return dcMemo;
	}

	public void setDcMemo(String dcMemo) {
		this.dcMemo = dcMemo;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	
}
