package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0011Req {

	/** 角色代號 */
	private String roleName;

	/** 角色名稱*/
	private String roleAlias;

	/** 功能清單 */
	private List<String> funcCodeList;

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public void setRoleAlias(String roleAlias) {
		this.roleAlias = roleAlias;
	}
	
	public String getRoleAlias() {
		return roleAlias;
	}

	public List<String> getFuncCodeList() {
		return funcCodeList;
	}

	public void setFuncCodeList(List<String> funcCodeList) {
		this.funcCodeList = funcCodeList;
	}

	@Override
	public String toString() {
		return "AA0011Req [roleName=" + roleName + ", roleAlias=" + roleAlias + ", funcCodeList=" + funcCodeList + "]";
	}
	
}
