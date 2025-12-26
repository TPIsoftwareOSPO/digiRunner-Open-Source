package tpi.dgrv4.dpaa.vo;

public class AA0108FuncInfo {
	
	/* 選單代碼*/
	private String funcCode;
	
	/* 選單名稱*/
	private String funcName;

	public String getFuncCode() {
		return funcCode;
	}

	public void setFuncCode(String funcCode) {
		this.funcCode = funcCode;
	}

	public String getFuncName() {
		return funcName;
	}

	public void setFuncName(String funcName) {
		this.funcName = funcName;
	}

	@Override
	public String toString() {
		return "AA0108FuncInfo [funcCode=" + funcCode + ", funcName=" + funcName + "]";
	}
}
