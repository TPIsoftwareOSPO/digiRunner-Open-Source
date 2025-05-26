package tpi.dgrv4.entity.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import tpi.dgrv4.common.utils.DateTimeUtil;

@Entity
@Table(name = "tsmp_vgroup")
public class TsmpVgroup {

	@Id
	@Column(name = "vgroup_id", length = 10)
	private String vgroupId;

	@Column(name = "vgroup_name")
	private String vgroupName;

	@Column(name = "create_time")
	private Date createTime = DateTimeUtil.now();

	@Column(name = "create_user")
	private String createUser;

	@Column(name = "update_time")
	private Date updateTime;

	@Column(name = "update_user")
	private String updateUser;
	
	@Column(name = "vgroup_alias")
	private String vgroupAlias;
	
	@Column(name = "vgroup_desc")
	private String vgroupDesc;

	@Column(name = "vgroup_access")
	private String vgroupAccess;
	
	@Column(name = "security_level_id")
	private String securityLevelId = "SYSTEM";
	
	@Column(name = "allow_days")
	private Integer allowDays = 0;
	
	@Column(name = "allow_times")
	private Integer allowTimes = 0;
	
	
	/* constructors */

	public TsmpVgroup() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpVgroup [vgroupId=" + vgroupId + ", vgroupName=" + vgroupName + ", createTime=" + createTime
				+ ", createUser=" + createUser + ", updateTime=" + updateTime + ", updateUser=" + updateUser
				+ ", vgroupAlias=" + vgroupAlias + ", vgroupDesc=" + vgroupDesc + ", vgroupAccess=" + vgroupAccess
				+ ", securityLevelId=" + securityLevelId + ", allowDays=" + allowDays + ", allowTimes=" + allowTimes
				+ "]";
	}

	/* getters and setters */
	
	public String getVgroupId() {
		return vgroupId;
	}

	public void setVgroupId(String vgroupId) {
		this.vgroupId = vgroupId;
	}

	public String getVgroupName() {
		return vgroupName;
	}

	public void setVgroupName(String vgroupName) {
		this.vgroupName = vgroupName;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public String getVgroupAlias() {
		return vgroupAlias;
	}

	public void setVgroupAlias(String vgroupAlias) {
		this.vgroupAlias = vgroupAlias;
	}

	public String getVgroupDesc() {
		return vgroupDesc;
	}

	public void setVgroupDesc(String vgroupDesc) {
		this.vgroupDesc = vgroupDesc;
	}

	public String getVgroupAccess() {
		return vgroupAccess;
	}

	public void setVgroupAccess(String vgroupAccess) {
		this.vgroupAccess = vgroupAccess;
	}

	public String getSecurityLevelId() {
		return securityLevelId;
	}

	public void setSecurityLevelId(String securityLevelId) {
		this.securityLevelId = securityLevelId;
	}

	public Integer getAllowDays() {
		return allowDays;
	}

	public void setAllowDays(Integer allowDays) {
		this.allowDays = allowDays;
	}

	public Integer getAllowTimes() {
		return allowTimes;
	}

	public void setAllowTimes(Integer allowTimes) {
		this.allowTimes = allowTimes;
	}

	
	
}
