package tpi.dgrv4.dpaa.vo;
	
public class AA0103Req {
	
	/* PK*/
	private String funcCode;

	/* PK(語系)*/
	private String locale;

	/* 模糊搜尋*/
	private String keyword;
	
	/* 功能類別 */
	private String funcType;
	
	public String getFuncCode() {
		return funcCode;
	}

	public void setFuncCode(String funcCode) {
		this.funcCode = funcCode;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getFuncType() {
		return funcType;
	}

	public void setFuncType(String funcType) {
		this.funcType = funcType;
	}

	@Override
	public String toString() {
		return "AA0103Req [funcCode=" + funcCode + ", locale=" + locale + ", keyword=" + keyword + ", funcType=" + funcType+ "]";
	}

	

}
