package tpi.dgrv4.entity.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import tpi.dgrv4.common.utils.DateTimeUtil;

@Entity
@Table(name = "tsmp_group")
public class TsmpGroup {

	@Id
	@Column(name = "group_id", length = 10)
	private String groupId;

	@Column(name = "group_name")
	private String groupName;

	@Column(name = "create_time")
	private Date createTime = DateTimeUtil.now();

	@Column(name = "create_user")
	private String createUser;

	@Column(name = "update_time")
	private Date updateTime;

	@Column(name = "update_user")
	private String updateUser;
	
	@Column(name = "group_alias")
	private String groupAlias;
	
	@Column(name = "group_desc")
	private String groupDesc;

	@Column(name = "group_access")
	private String groupAccess;
	
	@Column(name = "security_level_id")
	private String securityLevelId = "SYSTEM";
	
	@Column(name = "allow_days")
	private Integer allowDays = 0;
	
	@Column(name = "allow_times")
	private Integer allowTimes = 0;
	
	@Column(name = "vgroup_id")
	private String vgroupId;
	
	@Column(name = "vgroup_Name")
	private String vgroupName;
	
	@Column(name = "vgroup_flag")
	private String vgroupFlag = "0";
	
	/* constructors */

	public TsmpGroup() {}

	/* methods */

    /**
     * 2020.04.07; Kim; 改由TSMP平台指定的Sequence取得流水號
     * @param seq
     */
	@Deprecated
	public void ensureId(Long seq) {
		if (seq != null) {
			/* 2020.03.06 入口網從 900000000 開始編碼
			final String id = String.format("DP%08d", seq);
			*/
			seq = (seq + 900000000L) % 10000000000L;
			final String id = String.valueOf(seq);
			setGroupId(id);
		}
	}

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
