package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0021Resp {

	private String roleName;
	
	//若ROLE_ALIAS欄位為null，則"unknown:" + ROLE_NAME欄位。
	private String roleAlias;
	

	private List<AA0021RoleInfo> roleMappingInfo;

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

	public List<AA0021RoleInfo> getRoleMappingInfo() {
		return roleMappingInfo;
	}

	public void setRoleMappingInfo(List<AA0021RoleInfo> roleMappingInfo) {
		this.roleMappingInfo = roleMappingInfo;
	}


	
}
