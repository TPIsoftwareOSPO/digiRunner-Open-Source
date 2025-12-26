package tpi.dgrv4.gateway.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TWHeader {
	
	/** Client 發送處理序號 */
	@JsonProperty("X-STAN")
	private String xStan;

	/** API 服務提供者銀行代號 */
	@JsonProperty("X-DestinationId")
	private String xDestinationid;

	/** Client 註冊於 Open API Gateway ID */
	@JsonProperty("X-ClientId")
	private String xClientId;

	/** 交易發起日期時間 */
	@JsonProperty("X-TxnInitDateTime")
	private String xTxnInitDateTime;

	@Override
	public String toString() {
		return "TWHeader [xStan=" + xStan + ", xDestinationid=" + xDestinationid + ", xClientId=" + xClientId
				+ ", xTxnInitDateTime=" + xTxnInitDateTime + "]";
	}

	public String getxStan() {
		return xStan;
	}

	public void setxStan(String xStan) {
		this.xStan = xStan;
	}

	public String getxDestinationid() {
		return xDestinationid;
	}

	public void setxDestinationid(String xDestinationid) {
		this.xDestinationid = xDestinationid;
	}

	public String getxClientId() {
		return xClientId;
	}

	public void setxClientId(String xClientId) {
		this.xClientId = xClientId;
	}

	public String getxTxnInitDateTime() {
		return xTxnInitDateTime;
	}

	public void setxTxnInitDateTime(String xTxnInitDateTime) {
		this.xTxnInitDateTime = xTxnInitDateTime;
	}
}
