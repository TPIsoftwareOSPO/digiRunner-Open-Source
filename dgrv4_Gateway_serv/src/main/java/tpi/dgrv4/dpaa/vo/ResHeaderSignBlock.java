package tpi.dgrv4.dpaa.vo;

public class ResHeaderSignBlock{

	/** 回覆訊息 */
	private String rtnMsg;
	
	/** 回覆代碼 */
	private String rtnCode;

	public String getRtnCode() {
		return rtnCode;
	}

	public void setRtnCode(String rtnCode) {
		this.rtnCode = rtnCode;
	}

	public void setRtnMsg(String rtnMsg) {
		this.rtnMsg = rtnMsg;
	}
	
	public String getRtnMsg() {
		return rtnMsg;
	}

	@Override
	public String toString() {
		return "ResHeaderSignBlock [rtnCode=" + rtnCode + ", rtnMsg=" + rtnMsg + "]";
	}
}
