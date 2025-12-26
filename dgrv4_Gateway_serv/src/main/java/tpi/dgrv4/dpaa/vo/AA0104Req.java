package tpi.dgrv4.dpaa.vo;
	
public class AA0104Req {
	
	/* PK*/
	private String roleId;

	/* 功能代碼*/
	private String funcCode;

	/* 模糊搜尋*/
	private String keyword;

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

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	@Override
	public String toString() {
		return "AA0104Req [roleId=" + roleId + ", funcCode=" + funcCode + ", keyword=" + keyword + "]";
	}
	
	
}
