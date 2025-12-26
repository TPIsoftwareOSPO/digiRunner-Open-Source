package tpi.dgrv4.dpaa.vo;

public class DPB0084CertItem {

	//ID (流水號)
	private long clientCertId;
	//ID (流水號)
	private long clientCert2Id;
	//Client代碼
	private String clientId;
	//檔名	
	private String clientFileName;
	//檔案內容轉為 base64
	private String clientFileContent;
	//公鑰	
	private String pubKey;
	//憑證版本	
	private String certVersion;
	//憑證序號
	private String certSerialNum;
	//簽章演算法
	private String sAlgorithmID;
	//公鑰演算法
	private String alogorithmID;
	//CA數位指紋
	private String certThumbprint;
	//發行方ID	
	private String iuId;
	//發行方名稱
	private String issuerName;
	//持有憑證者ID	Subject Unique ID (這個ID是憑證內容所發布的Client正式ID, 與digiRunner上註冊的Client ID是不同的請注意)
	private String sUid;
	//憑證創建日
	private String createAt;
	//憑證到期日
	private String expiredAt;
	//2020/03/05 13:50
	private String createDateTime;
	private String createUser;
	//Version
	private Long lv	;
	//Key size
	private Integer keySize;
	
	public long getClientCertId() {
		return clientCertId;
	}
	public void setClientCertId(long clientCertId) {
		this.clientCertId = clientCertId;
	}
	public long getClientCert2Id() {
		return clientCert2Id;
	}
	public void setClientCert2Id(long clientCert2Id) {
		this.clientCert2Id = clientCert2Id;
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getClientFileName() {
		return clientFileName;
	}
	public void setClientFileName(String clientFileName) {
		this.clientFileName = clientFileName;
	}
 
	public String getClientFileContent() {
		return clientFileContent;
	}
	public void setClientFileContent(String clientFileContent) {
		this.clientFileContent = clientFileContent;
	}
	
	public String getPubKey() {
		return pubKey;
	}
	public void setPubKey(String pubKey) {
		this.pubKey = pubKey;
	}
	public void setCertVersion(String certVersion) {
		this.certVersion = certVersion;
	}
	public String getCertVersion() {
		return certVersion;
	}
	public String getCertSerialNum() {
		return certSerialNum;
	}
	public void setCertSerialNum(String certSerialNum) {
		this.certSerialNum = certSerialNum;
	}
	public void setsAlgorithmID(String sAlgorithmID) {
		this.sAlgorithmID = sAlgorithmID;
	}
	public String getsAlgorithmID() {
		return sAlgorithmID;
	}
	public String getAlogorithmID() {
		return alogorithmID;
	}
	public void setAlogorithmID(String alogorithmID) {
		this.alogorithmID = alogorithmID;
	}
	public String getCertThumbprint() {
		return certThumbprint;
	}
	public void setCertThumbprint(String certThumbprint) {
		this.certThumbprint = certThumbprint;
	}
	public void setIuId(String iuId) {
		this.iuId = iuId;
	}
	public String getIuId() {
		return iuId;
	}
	public String getIssuerName() {
		return issuerName;
	}
	public void setIssuerName(String issuerName) {
		this.issuerName = issuerName;
	}
	public void setsUid(String sUid) {
		this.sUid = sUid;
	}
	public String getsUid() {
		return sUid;
	}
	public String getCreateAt() {
		return createAt;
	}
	public void setCreateAt(String createAt) {
		this.createAt = createAt;
	}
	public void setExpiredAt(String expiredAt) {
		this.expiredAt = expiredAt;
	}
	public String getExpiredAt() {
		return expiredAt;
	}
	public String getCreateDateTime() {
		return createDateTime;
	}
	public void setCreateDateTime(String createDateTime) {
		this.createDateTime = createDateTime;
	}
	public String getCreateUser() {
		return createUser;
	}
	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}
	public Long getLv() {
		return lv;
	}
	public void setLv(Long lv) {
		this.lv = lv;
	}
	public Integer getKeySize() {
		return keySize;
	}
	public void setKeySize(Integer keySize) {
		this.keySize = keySize;
	}
		
}
