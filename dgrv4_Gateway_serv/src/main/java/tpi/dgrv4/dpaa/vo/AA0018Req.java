package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0018Req {

	//登入角色代號
	private String roleName;
	
	//可授權角色清單
	private List<String> roleNameMapping;

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public List<String> getRoleNameMapping() {
		return roleNameMapping;
	}

	public void setRoleNameMapping(List<String> roleNameMapping) {
		this.roleNameMapping = roleNameMapping;
	}
	
}
