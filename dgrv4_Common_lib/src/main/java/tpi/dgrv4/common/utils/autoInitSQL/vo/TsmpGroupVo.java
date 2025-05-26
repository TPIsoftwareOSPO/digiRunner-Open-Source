package tpi.dgrv4.common.utils.autoInitSQL.vo;

import java.util.Date;

public class TsmpGroupVo {

	private String groupId;

	private String groupName;

	private Date createTime;

	private String createUser;

	private Date updateTime;

	private String updateUser;
	
	private String groupAlias;
	
	private String groupDesc;

	private String groupAccess;
	
	private String securityLevelId = "SYSTEM";
	
	private Integer allowDays = 0;
	
	private Integer allowTimes = 0;
	
	private String vgroupId;
	
	private String vgroupName;
	
	private String vgroupFlag = "0";
	
	/* constructors */

	public TsmpGroupVo() {}



	@Override
	public String toString() {
		return "TsmpGroup [groupId=" + groupId + ", groupName=" + groupName + ", createTime=" + createTime
				+ ", createUser=" + createUser + ", updateTime=" + updateTime + ", updateUser=" + updateUser
				+ ", groupAlias=" + groupAlias + ", groupDesc=" + groupDesc + ", groupAccess=" + groupAccess
				+ ", securityLevelId=" + securityLevelId + ", allowDays=" + allowDays + ", allowTimes=" + allowTimes
				+ ", vgroupId=" + vgroupId + ", vgroupName=" + vgroupName + ", vgroupFlag=" + vgroupFlag + "]";
	}
	
	/* getters and setters */

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
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

	public String getGroupAlias() {
		return groupAlias;
	}

	public void setGroupAlias(String groupAlias) {
		this.groupAlias = groupAlias;
	}

	public String getGroupDesc() {
		return groupDesc;
	}

	public void setGroupDesc(String groupDesc) {
		this.groupDesc = groupDesc;
	}

	public String getGroupAccess() {
		return groupAccess;
	}

	public void setGroupAccess(String groupAccess) {
		this.groupAccess = groupAccess;
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

	public String getVgroupFlag() {
		return vgroupFlag;
	}

	public void setVgroupFlag(String vgroupFlag) {
		this.vgroupFlag = vgroupFlag;
	}
	
}
