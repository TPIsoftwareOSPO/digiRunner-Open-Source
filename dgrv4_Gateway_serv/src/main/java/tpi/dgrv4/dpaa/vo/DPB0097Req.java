package tpi.dgrv4.dpaa.vo;

public class DPB0097Req {
	
	/** 回覆代碼*/
	private String tsmpRtnCode;
	
	/** 語言地區 */
	private String locale;
	
	/** 關鍵字串 每一個字串可以使用"空白鍵" 隔開 */
	private String keyword;

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

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

}
