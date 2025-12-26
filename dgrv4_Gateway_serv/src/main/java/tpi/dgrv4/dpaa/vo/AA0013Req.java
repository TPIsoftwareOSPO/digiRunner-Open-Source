package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0013Req {

	/** 角色代碼 */
	private String roleId;

	/** 角色代號 */
	private String roleName;

	/** 新角色名稱*/
	private String newRoleAlias;

	/** 新功能清單 */
	private List<String> newFuncCodeList;

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

	public String getNewRoleAlias() {
		return newRoleAlias;
	}

	public void setNewRoleAlias(String newRoleAlias) {
		this.newRoleAlias = newRoleAlias;
	}

	public List<String> getNewFuncCodeList() {
		return newFuncCodeList;
	}

	public void setNewFuncCodeList(List<String> newFuncCodeList) {
		this.newFuncCodeList = newFuncCodeList;
	}

	@Override
	public String toString() {
		return "AA0013Req [roleId=" + roleId + ", roleName=" + roleName + ", newRoleAlias=" + newRoleAlias
				+ ", newFuncCodeList=" + newFuncCodeList + "]";
	}

	
	
}
