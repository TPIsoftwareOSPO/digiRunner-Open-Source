package tpi.dgrv4.dpaa.vo;

public class AA0014Req {

	/** 角色代碼 */
	private String roleId;

	/** 角色代號 */
	private String roleName;

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

	@Override
	public String toString() {
		return "AA0014Req [roleId=" + roleId + ", roleName=" + roleName + "]";
	}

	
	
	
}
