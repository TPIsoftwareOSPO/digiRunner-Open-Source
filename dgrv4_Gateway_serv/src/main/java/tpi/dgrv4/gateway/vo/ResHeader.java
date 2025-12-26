package tpi.dgrv4.gateway.vo;

public class ResHeader {

	/** 交易序號:(同Request) */
	private String txSN;

	/** 交易時間:(同Request) */
	private String txDate;

	/** 交易代碼:(同Request) */
	private String txID;

	/** 回覆代碼 */
	private String rtnCode;

	/** 回覆訊息 */
	private String rtnMsg;

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

	public String getRtnCode() {
		return rtnCode;
	}

	public void setRtnCode(String rtnCode) {
		this.rtnCode = rtnCode;
	}

	public String getRtnMsg() {
		return rtnMsg;
	}

	public void setRtnMsg(String rtnMsg) {
		this.rtnMsg = rtnMsg;
	}

}
