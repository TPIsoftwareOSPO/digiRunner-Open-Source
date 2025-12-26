package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0016Req {

	//可授權角色清單
	private List<String> roleNameMapping;
		
	//登入角色
	private String roleName;

	public String getRoleName() {
		return roleName;
	}

	public List<String> getRoleNameMapping() {
		return roleNameMapping;
	}

	public void setRoleNameMapping(List<String> roleNameMapping) {
		this.roleNameMapping = roleNameMapping;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
}
