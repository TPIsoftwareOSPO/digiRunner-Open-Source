package tpi.dgrv4.dpaa.vo;

public class DPB0098Req {

	/** 回覆代碼*/
	private String tsmpRtnCode;
	
	/** 語言地區 */
	private String locale;
	

	public void setTsmpRtnCode(String tsmpRtnCode) {
		this.tsmpRtnCode = tsmpRtnCode;
	}

	public String getLocale() {
		return locale;
	}
	
	public String getTsmpRtnCode() {
		return tsmpRtnCode;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

}
