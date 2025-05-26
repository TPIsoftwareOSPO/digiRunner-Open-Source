package tpi.dgrv4.entity.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "tsmp_func")
@IdClass(value = TsmpFuncId.class)
public class TsmpFunc {
	
	@Id
	@Column(name = "func_code")
	private String funcCode;
	
	@Column(name = "func_name")
	private String funcName;
	
	@Column(name = "func_name_en")
	private String funcNameEn;
	
	@Column(name = "func_desc")
	private String funcDesc;
	
	@Id
	@Column(name = "locale")
	private String locale;
	
	@Column(name = "update_user")
	private String updateUser;
	
	@Column(name = "update_time")
	private Date updateTime;
	
	@Column(name = "func_url")
	private String funcUrl;
	
	@Column(name = "func_type")
	private String funcType = "1";
	
	/* constructors */
	public TsmpFunc() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpFuncCode [funcCode=" + funcCode + ", funcName=" + funcName + ", funcNameEn=" + funcNameEn
				+ ", funcDesc=" + funcDesc + ", locale=" + locale + ", updateUser=" + updateUser + ", updateTime="
				+ updateTime + ", funcUrl=" + funcUrl + ",funcType=" + funcType +"]";
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