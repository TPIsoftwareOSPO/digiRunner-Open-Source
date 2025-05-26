package tpi.dgrv4.dpaa.vo;

public class AA0103List {

	/* 功能代碼*/
	private String funcCode;
	
	/* 語言地區*/
	private String locale;
	
	/* 功能名稱*/
	private String funcName;
	
	/* 功能名稱(英文)*/
	private String funcNameEn;
	
	/* 功能描述*/
	private String funcDesc;
	
	/* 更新人員*/
	private String updateUser;
	
	/* 更新時間*/
	private String updateTime;
	
	/* 報表 url*/
	private String reportUrl;
	
	/* 功能類別*/
	private String funcType;
	
	/* 主選單*/
	private String masterFuncName;
	
	/* 語言地區名稱*/
	private String localeName;
	
	/* SYS_RPT:kibana報表
		IFRAME:嵌入頁面
		LINK:另開頁面*/
	private String reportType;
	
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

	public String getFuncName() {
		return funcName;
	}

	public void setFuncName(String funcName) {
		this.funcName = funcName;
	}

	public void setFuncNameEn(String funcNameEn) {
		this.funcNameEn = funcNameEn;
	}
	
	public String getFuncNameEn() {
		return funcNameEn;
	}

	public String getFuncDesc() {
		return funcDesc;
	}

	public void setFuncDesc(String funcDesc) {
		this.funcDesc = funcDesc;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public String getReportUrl() {
		return reportUrl;
	}

	public void setReportUrl(String reportUrl) {
		this.reportUrl = reportUrl;
	}

	@Override
	public String toString() {
		return "AA0103List [funcCode=" + funcCode + ", locale=" + locale + ", funcName=" + funcName + ", funcNameEn="
				+ funcNameEn + ", funcDesc=" + funcDesc + ", updateUser=" + updateUser + ", updateTime=" + updateTime
				+ ", reportUrl=" + reportUrl + ", funcType=" + funcType + ", masterFuncName=" + masterFuncName 
				+ ", localeName=" + localeName + "]";
	}

	public String getFuncType() {
		return funcType;
	}

	public void setFuncType(String funcType) {
		this.funcType = funcType;
	}

	public String getMasterFuncName() {
		return masterFuncName;
	}

	public void setMasterFuncName(String masterFuncName) {
		this.masterFuncName = masterFuncName;
	}

	public String getLocaleName() {
		return localeName;
	}

	public void setLocaleName(String localeName) {
		this.localeName = localeName;
	}

	public String getReportType() {
		return reportType;
	}

	public void setReportType(String reportType) {
		this.reportType = reportType;
	};
}
