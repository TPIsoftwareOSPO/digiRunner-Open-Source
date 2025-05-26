package tpi.dgrv4.dpaa.vo;

public class DPB0100Resp {
	
	/** 回覆代碼*/
	private String tsmpRtnCode;
	
	/** 顯示的回覆訊息 */
	private String tsmpRtnMsg;
	
	/** 說明  */
	private String tsmpRtnDesc;
	
	/** 語言地區 */
	private String locale;

	public void setTsmpRtnCode(String tsmpRtnCode) {
		this.tsmpRtnCode = tsmpRtnCode;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getTsmpRtnMsg() {
		return tsmpRtnMsg;
	}
	
	public String getLocale() {
		return locale;
	}

	public void setTsmpRtnMsg(String tsmpRtnMsg) {
		this.tsmpRtnMsg = tsmpRtnMsg;
	}
	
	public String getTsmpRtnCode() {
		return tsmpRtnCode;
	}

	public String getTsmpRtnDesc() {
		return tsmpRtnDesc;
	}

	public void setTsmpRtnDesc(String tsmpRtnDesc) {
		this.tsmpRtnDesc = tsmpRtnDesc;
	}

}
