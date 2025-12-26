package tpi.dgrv4.dpaa.vo;

public class AA0022Detail {

	private String roleName;
	
	private String roleId;
	
	/** 若ROLE_ALIAS欄位為null，則"unknown:" + ROLE_NAME欄位。 */
	private String roleAlias;

	public String getRoleId() {
		return roleId;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	
	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public String getRoleAlias() {
		return roleAlias;
	}

	public void setRoleAlias(String roleAlias) {
		this.roleAlias = roleAlias;
	}
	
	
}
