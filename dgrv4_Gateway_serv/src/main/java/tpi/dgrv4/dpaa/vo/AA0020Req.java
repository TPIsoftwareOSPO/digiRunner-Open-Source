package tpi.dgrv4.dpaa.vo;
	
public class AA0020Req {

	// PK
	private String roleId;

	// 模糊搜尋
	private String keyword;
	
	//是否需要TSMP_ROLE_FUNC
	private boolean funcFlag;
	
	//是否查詢"登入角色"擁有的"可授權角色清單"(AA0018會使用到)
	private boolean authorityFlag;
	
	//登入角色(AA0018會使用到)
	private String roleName;

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public boolean isFuncFlag() {
		return funcFlag;
	}

	public void setFuncFlag(boolean funcFlag) {
		this.funcFlag = funcFlag;
	}

	public boolean isAuthorityFlag() {
		return authorityFlag;
	}

	public void setAuthorityFlag(boolean authorityFlag) {
		this.authorityFlag = authorityFlag;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

}
