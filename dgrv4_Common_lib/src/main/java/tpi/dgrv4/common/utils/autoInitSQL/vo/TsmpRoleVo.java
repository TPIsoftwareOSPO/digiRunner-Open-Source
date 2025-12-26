package tpi.dgrv4.common.utils.autoInitSQL.vo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import tpi.dgrv4.common.utils.DateTimeUtil;

public class TsmpRoleVo  {
	
	private static String DEFAULT_USER = "SYSTEM";

	private String roleId;

	private String roleName;

	private String roleAlias;

	private String createUser = DEFAULT_USER;

	private Date createTime = DateTimeUtil.now();

	/* constructors */
	public TsmpRoleVo() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpRole [roleId=" + roleId + ", roleName=" + roleName + ", roleAlias=" + roleAlias + ", createUser="
				+ createUser + ", createTime=" + createTime + "]";
	}

	// 為了 DPB0111Service 從 Map 比對
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((createTime == null) ? 0 : createTime.hashCode());
		result = prime * result + ((createUser == null) ? 0 : createUser.hashCode());
		result = prime * result + ((roleAlias == null) ? 0 : roleAlias.hashCode());
		result = prime * result + ((roleId == null) ? 0 : roleId.hashCode());
		result = prime * result + ((roleName == null) ? 0 : roleName.hashCode());
		return result;
	}

	// 為了 DPB0111Service 從 Map 比對
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TsmpRoleVo other = (TsmpRoleVo) obj;
		if (roleId == null) {
			if (other.roleId != null)
				return false;
		} else if (!roleId.equals(other.roleId))
			return false;
		return true;
	}

	/* getters and setters */

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getRoleAlias() {
		return roleAlias;
	}

	public void setRoleAlias(String roleAlias) {
		this.roleAlias = roleAlias;
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

}
