package tpi.dgrv4.dpaa.constant;

public enum TsmpNoticeSrc {
	JWE加密憑證到期("JWECertExpire"),
	TLS通訊憑證到期("TLSCertExpire")
	;

	private String value;

	private TsmpNoticeSrc(String value) {
		this.value = value;
	}

	public String value() {
		return this.value;
	}

}
