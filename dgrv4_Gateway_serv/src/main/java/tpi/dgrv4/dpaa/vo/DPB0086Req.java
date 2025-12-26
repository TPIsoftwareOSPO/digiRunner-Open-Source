package tpi.dgrv4.dpaa.vo;

public class DPB0086Req {
	
	private String clientId;
	
	private Long clientCertId;
	
	private Long clientCert2Id;
	
	/** 憑證類型	使用BcryptParam,ITEM_NO='CERT_TYPE',JWE 使用 TSMP_CLIENT_CERT,TLS 使用 TSMP_CLIENT_CERT2 */
	private String encodeCertType;
	
	public String getClientId() {
		return clientId;
	}

	public Long getClientCertId() {
		return clientCertId;
	}

	public void setClientCertId(Long clientCertId) {
		this.clientCertId = clientCertId;
	}

	public Long getClientCert2Id() {
		return clientCert2Id;
	}

	public void setClientCert2Id(Long clientCert2Id) {
		this.clientCert2Id = clientCert2Id;
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
