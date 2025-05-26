package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0020List {

	//角色代碼
	private String roleID;
	
	//角色名稱
	private String roleName;
	
	//角色代號
	private String roleAlias;
	
	//角色與功能關係
	private List<String> funcCodeList;
	
	//被設定為可授權角色
	private boolean mappingFlag;

	public String getRoleID() {
		return roleID;
	}

	public void setRoleID(String roleID) {
		this.roleID = roleID;
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

	public List<String> getFuncCodeList() {
		return funcCodeList;
	}

	public void setFuncCodeList(List<String> funcCodeList) {
		this.funcCodeList = funcCodeList;
	}

	public boolean isMappingFlag() {
		return mappingFlag;
	}

	public void setMappingFlag(boolean mappingFlag) {
		this.mappingFlag = mappingFlag;
	}
	
	

}
