package tpi.dgrv4.common.utils.autoInitSQL.vo;

public class TsmpRtnCodeVo {

	private String tsmpRtnCode;
	
	private String locale;
	
	private String tsmpRtnMsg;
	
	private String tsmpRtnDesc;

	public String getTsmpRtnCode() {
		return tsmpRtnCode;
	}

	public void setTsmpRtnCode(String tsmpRtnCode) {
		this.tsmpRtnCode = tsmpRtnCode;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getTsmpRtnMsg() {
		return tsmpRtnMsg;
	}

	public void setTsmpRtnMsg(String tsmpRtnMsg) {
		this.tsmpRtnMsg = tsmpRtnMsg;
	}

	public String getTsmpRtnDesc() {
		return tsmpRtnDesc;
	}

	public void setTsmpRtnDesc(String tsmpRtnDesc) {
		this.tsmpRtnDesc = tsmpRtnDesc;
	}
	
	@Override
	public String toString() {
		return "TsmpRtnCode [tsmpRtnCode=" + tsmpRtnCode + ", locale=" + locale + ", tsmpRtnMsg=" + tsmpRtnMsg
				+ ", tsmpRtnDesc=" + tsmpRtnDesc + "]";
	}

}
