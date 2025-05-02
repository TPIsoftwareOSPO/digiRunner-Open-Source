package tpi.dgrv4.dpaa.vo;

public class AA0104List {
	
	/* 角色代碼*/
	private String roleId;
	
	/* 角色名稱*/
	private String roleName;
	
	/* 角色代號*/
	private String roleAlias;

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	
	public String getRoleName() {
		return roleName;
	}

	public String getRoleAlias() {
		return roleAlias;
	}

	public void setRoleAlias(String roleAlias) {
		this.roleAlias = roleAlias;
	}

	@Override
	public String toString() {
		return "AA0104List [roleId=" + roleId + ", roleName=" + roleName + ", roleAlias=" + roleAlias + "]";
	}
	

}
