package tpi.dgrv4.common.utils.autoInitSQL.vo;

public class AutoInitSQLTsmpRoleRoleMappingVo {

	private Long roleRoleId;
	
	private String roleName;

	private String roleNameMapping;

	/* constructors */

	public AutoInitSQLTsmpRoleRoleMappingVo() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpRoleRoleMapping [roleRoleId=" + roleRoleId + ", roleName=" + roleName + ", roleNameMapping="
				+ roleNameMapping + "]";
	}
	
	/* getters and setters */
	
	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getRoleNameMapping() {
		return roleNameMapping;
	}

	public void setRoleNameMapping(String roleNameMapping) {
		this.roleNameMapping = roleNameMapping;
	}

	public Long getRoleRoleId() {
		return roleRoleId;
	}

	public void setRoleRoleId(Long roleRoleId) {
		this.roleRoleId = roleRoleId;
	}
	
}
