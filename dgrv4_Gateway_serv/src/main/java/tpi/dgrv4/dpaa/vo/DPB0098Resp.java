package tpi.dgrv4.dpaa.vo;

public class DPB0098Resp {

	/** 語言地區 */
	private String locale;
	
	/** 顯示的回覆訊息 */
	private String tsmpRtnMsg;
	
	/** 說明  */
	private String tsmpRtnDesc;
	
	/** 回覆代碼*/
	private String tsmpRtnCode;
	
	public String getTsmpRtnCode() {
		return tsmpRtnCode;
	}

	public void setTsmpRtnCode(String tsmpRtnCode) {
		this.tsmpRtnCode = tsmpRtnCode;
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

	public String getLocale() {
		return locale;
	}
	
	public String getTsmpRtnDesc() {
		return tsmpRtnDesc;
	}

	public void setTsmpRtnDesc(String tsmpRtnDesc) {
		this.tsmpRtnDesc = tsmpRtnDesc;
	}


}
