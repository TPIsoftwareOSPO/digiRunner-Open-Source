package tpi.dgrv4.common.utils.autoInitSQL.vo;

public class TsmpRoleFuncVo {

	private String roleId;

	private String funcCode;

	/* constructors */

	public TsmpRoleFuncVo() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpRoleFunc [roleId=" + roleId + ", funcCode=" + funcCode + "]\n";
	}
	
	/* getters and setters */
	
	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public String getFuncCode() {
		return funcCode;
	}

	public void setFuncCode(String funcCode) {
		this.funcCode = funcCode;
	}

}
