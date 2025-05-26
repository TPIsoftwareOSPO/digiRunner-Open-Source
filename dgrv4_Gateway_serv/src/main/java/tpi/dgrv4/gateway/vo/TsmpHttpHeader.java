package tpi.dgrv4.gateway.vo;

public class TsmpHttpHeader {

	/** "Bearer <JWT>" */
	private TsmpAuthorization authorization;

	/** Base64 of SHA256 of <signBlock + Http body> */
	private String signCode;

	/** 一次性交易用的代幣 */
	private String txToken;

	public TsmpHttpHeader() {}
	
	public TsmpAuthorization getAuthorization() {
		return authorization;
	}

	public void setAuthorization(TsmpAuthorization authorization) {
		this.authorization = authorization;
	}

	public String getSignCode() {
		return signCode;
	}

	public void setSignCode(String signCode) {
		this.signCode = signCode;
	}

	public String getTxToken() {
		return txToken;
	}

	public void setTxToken(String txToken) {
		this.txToken = txToken;
	}

}
