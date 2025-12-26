package tpi.dgrv4.dpaa.vo;

public class DPB0097Item {
	
	/** 回覆代碼 */
	private String tsmpRtnCode;
	
	/** 語言地區 */
	private String locale;
	
	/** 截斷後的"顯示的回覆訊息" */
	private String tsmpRtnMsg;
	
	/** 完整的"顯示的回覆訊息 */
	private String oriTsmpRtnMsg;
	
	/** 顯示的回覆訊息是否被截斷 */
	private Boolean isMsgTruncated;
	
	/** 截斷後的"說明" */
	private String tsmpRtnDesc;

	/** 完整的"說明" */
	private String oriTsmpRtnDesc;

	/** 說明是否被截斷 */
	private Boolean isDescTruncated;


	public String getTsmpRtnCode() {
		return tsmpRtnCode;
	}

	public void setTsmpRtnCode(String tsmpRtnCode) {
		this.tsmpRtnCode = tsmpRtnCode;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}
	
	public String getLocale() {
		return locale;
	}

	public String getTsmpRtnMsg() {
		return tsmpRtnMsg;
	}

	public void setTsmpRtnMsg(String tsmpRtnMsg) {
		this.tsmpRtnMsg = tsmpRtnMsg;
	}

	public String getOriTsmpRtnMsg() {
		return oriTsmpRtnMsg;
	}

	public void setOriTsmpRtnMsg(String oriTsmpRtnMsg) {
		this.oriTsmpRtnMsg = oriTsmpRtnMsg;
	}

	public Boolean getIsMsgTruncated() {
		return isMsgTruncated;
	}

	public void setIsMsgTruncated(Boolean isMsgTruncated) {
		this.isMsgTruncated = isMsgTruncated;
	}

	public String getTsmpRtnDesc() {
		return tsmpRtnDesc;
	}

	public void setTsmpRtnDesc(String tsmpRtnDesc) {
		this.tsmpRtnDesc = tsmpRtnDesc;
	}

	public String getOriTsmpRtnDesc() {
		return oriTsmpRtnDesc;
	}

	public void setOriTsmpRtnDesc(String oriTsmpRtnDesc) {
		this.oriTsmpRtnDesc = oriTsmpRtnDesc;
	}

	public Boolean getIsDescTruncated() {
		return isDescTruncated;
	}

	public void setIsDescTruncated(Boolean isDescTruncated) {
		this.isDescTruncated = isDescTruncated;
	}


	@Override
	public String toString() {
		return "DPB0097Item [tsmpRtnCode=" + tsmpRtnCode + ", locale=" + locale + ", tsmpRtnMsg=" + tsmpRtnMsg
				+ ", oriTsmpRtnMsg=" + oriTsmpRtnMsg + ", isMsgTruncated=" + isMsgTruncated + ", tsmpRtnDesc="
				+ tsmpRtnDesc + ", oriTsmpRtnDesc=" + oriTsmpRtnDesc + ", isDescTruncated=" + isDescTruncated + "]";
	}

}
