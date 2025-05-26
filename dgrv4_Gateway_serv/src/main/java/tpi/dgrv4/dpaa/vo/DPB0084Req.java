package tpi.dgrv4.dpaa.vo;

public class DPB0084Req {
	
	/** 用戶代碼 */
	private String clientId;

	/** 憑證類型	使用BcryptParam,ITEM_NO='CERT_TYPE',JWE 使用 TSMP_CLIENT_CERT,TLS 使用 TSMP_CLIENT_CERT2 */
	private String encodeCertType;
	
	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getEncodeCertType() {
		return encodeCertType;
	}

	public void setEncodeCertType(String encodeCertType) {
		this.encodeCertType = encodeCertType;
	}
}
