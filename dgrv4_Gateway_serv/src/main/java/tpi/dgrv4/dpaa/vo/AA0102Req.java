package tpi.dgrv4.dpaa.vo;

public class AA0102Req {

	/* 功能代碼 */
	private String funcCode;

	/* 語系 */
	private String locale;

	/* 原功能描述 */
	private String desc;

	/* 功能描述 */
	private String newDesc;

	/* 原功能名稱 */
	private String funcName;

	/* 功能名稱 */
	private String newFuncName;

	/* 原 Report URL */
	private String reportUrl;

	/* 新 Report URL */
	private String newReportUrl;
	
	/* kibana的checkbox */
	private Boolean isKibana;

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

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getNewDesc() {
		return newDesc;
	}

	public void setNewDesc(String newDesc) {
		this.newDesc = newDesc;
	}

	public String getFuncName() {
		return funcName;
	}

	public void setFuncName(String funcName) {
		this.funcName = funcName;
	}

	public String getNewFuncName() {
		return newFuncName;
	}

	public void setNewFuncName(String newFuncName) {
		this.newFuncName = newFuncName;
	}

	public String getReportUrl() {
		return reportUrl;
	}

	public void setReportUrl(String reportUrl) {
		this.reportUrl = reportUrl;
	}

	public String getNewReportUrl() {
		return newReportUrl;
	}

	public void setNewReportUrl(String newReportUrl) {
		this.newReportUrl = newReportUrl;
	}

	@Override
	public String toString() {
		return "AA0102Req [funcCode=" + funcCode + ", locale=" + locale + ", desc=" + desc + ", newDesc=" + newDesc
				+ ", funcName=" + funcName + ", newFuncName=" + newFuncName + ", reportUrl= " + reportUrl
				+ ", newReportUrl=" + newReportUrl + "]";
	}

	public Boolean getIsKibana() {
		return isKibana;
	}

	public void setIsKibana(Boolean isKibana) {
		this.isKibana = isKibana;
	}

}
