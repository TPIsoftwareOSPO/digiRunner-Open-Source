package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0022List {

	private String roleId;
	
	private String roleName;
	
	/** 若ROLE_ALIAS欄位為null，則"unknown:" + ROLE_NAME欄位。 */
	private String roleAlias;

	private List<AA0022Detail> roleRoleMapping;
	
	/** 
	 * 截斷後的"可授權角色清單" 
	 * 將AA0022List.roleRoleMapping.roleAlias組成字串，長度超過100個字元截斷字串
	 * */
	private String roleRoleMappingInfo = "";
	/** 
	 * 完整的"可授權角色清單" 
	 * 將AA0022List.roleRoleMapping.roleAlias組成字串
	 * */
	private String oriRoleRoleMappingInfo = "";
	
	/**
	 * 前端用以判斷是否該顯示「顯示更多」的按鈕。
     * true: 內容有被截斷，應顯示「顯示更多」
     * false: 內容未被截斷，不用顯示「顯示更多」
	 * 
	 * */
	private boolean isMsgTruncated;
	

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getRoleAlias() {
		return roleAlias;
	}
	
	public String getRoleName() {
		return roleName;
	}

	public void setRoleAlias(String roleAlias) {
		this.roleAlias = roleAlias;
	}

	public List<AA0022Detail> getRoleRoleMapping() {
		return roleRoleMapping;
	}

	public void setRoleRoleMapping(List<AA0022Detail> roleRoleMapping) {
		this.roleRoleMapping = roleRoleMapping;
	}

	public String getRoleRoleMappingInfo() {
		return roleRoleMappingInfo;
	}

	public void setRoleRoleMappingInfo(String roleRoleMappingInfo) {
		this.roleRoleMappingInfo = roleRoleMappingInfo;
	}

	public String getOriRoleRoleMappingInfo() {
		return oriRoleRoleMappingInfo;
	}

	public void setOriRoleRoleMappingInfo(String oriRoleRoleMappingInfo) {
		this.oriRoleRoleMappingInfo = oriRoleRoleMappingInfo;
	}

	public boolean isMsgTruncated() {
		return isMsgTruncated;
	}

	public void setMsgTruncated(boolean isMsgTruncated) {
		this.isMsgTruncated = isMsgTruncated;
	}

	
}
