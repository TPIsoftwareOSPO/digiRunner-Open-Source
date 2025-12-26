package tpi.dgrv4.gateway.vo;

public class DPB0115Item {

	/** 交易代碼 */
	private String txId;

	/** 是否可用 */
	private Boolean available;

	public String getTxId() {
		return txId;
	}

	public void setTxId(String txId) {
		this.txId = txId;
	}

	public Boolean getAvailable() {
		return available;
	}

	public void setAvailable(Boolean available) {
		this.available = available;
	}

}
