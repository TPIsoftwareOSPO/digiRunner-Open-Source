package tpi.dgrv4.dpaa.vo;

public class ResHeaderSignBlockReset {

	/** 回覆代碼 */
	private String rtnCode;

	/** 回覆訊息 */
	private String rtnMsg;

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

	@Override
	public String toString() {
		return "ResHeaderSignBlockReset [rtnCode=" + rtnCode + ", rtnMsg=" + rtnMsg + "]";
	}
}
