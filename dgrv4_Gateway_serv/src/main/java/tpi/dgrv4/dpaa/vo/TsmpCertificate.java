package tpi.dgrv4.dpaa.vo;

public class TsmpCertificate {

	private String pubKey;

	private String certVersion;

	private String certSerialNum;

	private String sAlgorithmId;

	private String algorithmId;

	private String certThumbprint;

	private String iuid;

	private String issuerName;

	private String suid;

	private Long createAt;

	private Long expiredAt;
	
	private Integer keySize;

	public String getPubKey() {
		return pubKey;
	}

	public void setPubKey(String pubKey) {
		this.pubKey = pubKey;
	}

	public String getCertVersion() {
		return certVersion;
	}

	public void setCertVersion(String certVersion) {
		this.certVersion = certVersion;
	}

	public String getCertSerialNum() {
		return certSerialNum;
	}

	public void setCertSerialNum(String certSerialNum) {
		this.certSerialNum = certSerialNum;
	}

	public String getsAlgorithmId() {
		return sAlgorithmId;
	}

	public void setsAlgorithmId(String sAlgorithmId) {
		this.sAlgorithmId = sAlgorithmId;
	}

	public String getAlgorithmId() {
		return algorithmId;
	}

	public void setAlgorithmId(String algorithmId) {
		this.algorithmId = algorithmId;
	}

	public String getCertThumbprint() {
		return certThumbprint;
	}

	public void setCertThumbprint(String certThumbprint) {
		this.certThumbprint = certThumbprint;
	}

	public String getIuid() {
		return iuid;
	}

	public void setIuid(String iuid) {
		this.iuid = iuid;
	}

	public String getIssuerName() {
		return issuerName;
	}

	public void setIssuerName(String issuerName) {
		this.issuerName = issuerName;
	}

	public String getSuid() {
		return suid;
	}

	public void setSuid(String suid) {
		this.suid = suid;
	}

	public Long getCreateAt() {
		return createAt;
	}

	public void setCreateAt(Long createAt) {
		this.createAt = createAt;
	}

	public Long getExpiredAt() {
		return expiredAt;
	}

	public void setExpiredAt(Long expiredAt) {
		this.expiredAt = expiredAt;
	}

	public Integer getKeySize() {
		return keySize;
	}

	public void setKeySize(Integer keySize) {
		this.keySize = keySize;
	}

	@Override
	public String toString() {
		return "TsmpCertificate [pubKey=" + pubKey + ", certVersion=" + certVersion + ", certSerialNum=" + certSerialNum
				+ ", sAlgorithmId=" + sAlgorithmId + ", algorithmId=" + algorithmId + ", certThumbprint="
				+ certThumbprint + ", iuid=" + iuid + ", issuerName=" + issuerName + ", suid=" + suid + ", createAt="
				+ createAt + ", expiredAt=" + expiredAt + ", keySize=" + keySize + "]";
	}
	
}
