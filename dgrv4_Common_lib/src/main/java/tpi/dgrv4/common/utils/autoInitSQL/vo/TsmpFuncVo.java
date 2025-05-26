package tpi.dgrv4.common.utils.autoInitSQL.vo;

import java.util.Date;

public class TsmpFuncVo {
	
	private String funcCode;
	
	private String funcName;
	
	private String funcNameEn;
	
	private String funcDesc;
	
	private String locale;
	
	private String updateUser;
	
	private Date updateTime;
	
	private String funcUrl;
	
	private String funcType;
	/* constructors */

	public TsmpFuncVo() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpFuncCode [funcCode=" + funcCode + ", funcName=" + funcName + ", funcNameEn=" + funcNameEn
				+ ", funcDesc=" + funcDesc + ", locale=" + locale + ", updateUser=" + updateUser + ", updateTime="
				+ updateTime + ", funcUrl=" + funcUrl + "]";
	}

	
	/* getters and setters */
	
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

	public String getFuncNameEn() {
		return funcNameEn;
	}

	public void setFuncNameEn(String funcNameEn) {
		this.funcNameEn = funcNameEn;
	}

	public String getFuncDesc() {
		return funcDesc;
	}

	public void setFuncDesc(String funcDesc) {
		this.funcDesc = funcDesc;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getFuncUrl() {
		return funcUrl;
	}

	public void setFuncUrl(String funcUrl) {
		this.funcUrl = funcUrl;
	}

	public String getFuncType() {
		return funcType;
	}

	public void setFuncType(String funcType) {
		this.funcType = funcType;
	}

}