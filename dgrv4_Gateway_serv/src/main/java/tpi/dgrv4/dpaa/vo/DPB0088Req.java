package tpi.dgrv4.dpaa.vo;

public class DPB0088Req {

	// 分頁用
	private Long clientCertId;
	
	// 分頁用
	private Long clientCert2Id;

	// 建立日期(起), ref tsmp_client_cert.create_date_time, yyyy/MM/dd
	private String startDate;

	// 建立日期(迄), ref tsmp_client_cert.create_date_time, yyyy/MM/dd
	private String endDate;
	
	/** 憑證類型	使用BcryptParam,ITEM_NO='CERT_TYPE',JWE 使用 TSMP_CLIENT_CERT,TLS 使用 TSMP_CLIENT_CERT2 */
	private String encodeCertType;

	public DPB0088Req() {
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

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getEncodeCertType() {
		return encodeCertType;
	}

	public void setEncodeCertType(String encodeCertType) {
		this.encodeCertType = encodeCertType;
	}
}
