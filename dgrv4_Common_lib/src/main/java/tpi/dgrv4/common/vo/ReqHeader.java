package tpi.dgrv4.common.vo;

public class ReqHeader implements IReqHeader{
	
	/** 交易序號: ServerNo(1) + Date(yyMMddHHmmss) + AlphaNumber(6) */
	/** ex. "1180823173301000001" (不可重複) */
	private String txSN;

	/** 交易時間: "YYYYMMDDThhmmssTZD", ex. "20180812T173301+0800" */
	private String txDate;

	/** 交易代碼: ex. "AA0001" */
	private String txID;

	/** 用戶代碼: ex. "YWRtaW5Db25zb2xl" (此值需與JWT中的client_id一致) */
	private String cID;
	
	/** 語言地區: ex. "zh-TW" */
	private String locale;

	public ReqHeader() {}

	public String getTxSN() {
		return txSN;
	}

	public void setTxSN(String txSN) {
		this.txSN = txSN;
	}

	public String getTxDate() {
		return txDate;
	}

	public void setTxDate(String txDate) {
		this.txDate = txDate;
	}

	public String getTxID() {
		return txID;
	}

	public void setTxID(String txID) {
		this.txID = txID;
	}

	public String getcID() {
		return cID;
	}

	public void setcID(String cID) {
		this.cID = cID;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	@Override
	public String toString() {
		return "ReqHeader [txSN=" + txSN + ", txDate=" + txDate + ", txID=" + txID + ", locale=" + locale + ", cID=" + cID + "]";
	}

}
