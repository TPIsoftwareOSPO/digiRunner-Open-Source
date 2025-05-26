package tpi.dgrv4.entity.entity.jpql;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tsmp_reg_host")
public class TsmpRegHost {

	@Id
	@Column(name = "reghost_id")
	private String reghostId;

	@Column(name = "reghost")
	private String reghost;

	@Column(name = "reghost_status")
	private String reghostStatus = "S";

	@Column(name = "enabled")
	private String enabled = "N";

	@Column(name = "clientid")
	private String clientid;
	
	@Column(name = "heartbeat")
	private Date heartbeat;
	
	@Column(name = "memo")
	private String memo;
	
	@Column(name = "create_user")
	private String createUser;
	
	@Column(name = "create_time")
	private Date createTime;
	
	@Column(name = "update_user")
	private String updateUser;
	
	@Column(name = "update_time")
	private Date updateTime;

	/* constructors */
	public TsmpRegHost() {}

	/* methods */
	@Override
	public String toString() {
		return "TsmpRegHost [reghostId=" + reghostId + ", reghost=" + reghost + ", reghostStatus=" + reghostStatus
				+ ", enabled=" + enabled + ", clientid=" + clientid + ", heartbeat=" + heartbeat + ", memo=" + memo
				+ ", createUser=" + createUser + ", createTime=" + createTime + ", updateUser=" + updateUser
				+ ", updateTime=" + updateTime + "]";
	}

	/* getters and setters */
	public String getReghostId() {
		return reghostId;
	}

	public void setReghostId(String reghostId) {
		this.reghostId = reghostId;
	}

	public String getReghost() {
		return reghost;
	}

	public void setReghost(String reghost) {
		this.reghost = reghost;
	}

	public String getReghostStatus() {
		return reghostStatus;
	}

	public void setReghostStatus(String reghostStatus) {
		this.reghostStatus = reghostStatus;
	}

	public String getEnabled() {
		return enabled;
	}

	public void setEnabled(String enabled) {
		this.enabled = enabled;
	}

	public String getClientid() {
		return clientid;
	}

	public void setClientid(String clientid) {
		this.clientid = clientid;
	}

	public Date getHeartbeat() {
		return heartbeat;
	}

	public void setHeartbeat(Date heartbeat) {
		this.heartbeat = heartbeat;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
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
