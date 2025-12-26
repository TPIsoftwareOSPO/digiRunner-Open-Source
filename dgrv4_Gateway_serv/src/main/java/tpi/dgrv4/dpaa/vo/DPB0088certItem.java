package tpi.dgrv4.dpaa.vo;

public class DPB0088certItem {

	/** ID (流水號) */
	private Long clientCertId;
	
	/** ID (流水號) */
	private Long clientCert2Id;

	/** Client代碼 */
	private String clientId;

	/** 公鑰 */
	private String pubKey;

	/** 憑證版本 */
	private String certVersion;

	/** 憑證序號 */
	private String certSerialNum;

	/** 簽章演算法 */
	private String sAlgorithmID;

	/** 公鑰演算法 */
	private String algorithmID;

	/** CA數位指紋 */
	private String certThumbprint;

	/** 發行方ID */
	private String iuId;

	/** 發行方名稱 */
	private String issuerName;

	/**
	 * 持有憑證者ID<br>
	 * Subject Unique ID (這個ID是憑證內容所發布的Client正式ID, 與digiRunner上註冊的Client ID是不同的請注意)
	 */
	private String sUid;

	/** 憑證創建日, yyyy/MM/dd */
	private String createAt;

	/** 憑證到期日, yyyy/MM/dd */
	private String expiredAt;

	/** parser TSMP_CLIENT */
	private String clientName;

	/** parser TSMP_CLIENT */
	private String clientAlias;

	/**  */
	private String certFileName;

	/** ref tsmp_client_cert.create_date_time, ex: 2020/03/05 13:50 */
	private String createDateTime;

	/**  */
	private String createUser;

	/** ref tsmp_client_cert.update_date_time, ex: 2020/03/05 13:50 */
	private String updateDateTime;

	public DPB0088certItem() {
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

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getPubKey() {
		return pubKey;
	}

	public void setPubKey(String pubKey) {
		this.pubKey = pubKey;
	}

	public String getCertVersion() {
		return certVersion;
	}
	
	public String getCertSerialNum() {
		return certSerialNum;
	}

	public void setCertVersion(String certVersion) {
		this.certVersion = certVersion;
	}

	public void setCertSerialNum(String certSerialNum) {
		this.certSerialNum = certSerialNum;
	}

	public String getsAlgorithmID() {
		return sAlgorithmID;
	}

	public void setsAlgorithmID(String sAlgorithmID) {
		this.sAlgorithmID = sAlgorithmID;
	}

	public String getAlgorithmID() {
		return algorithmID;
	}

	public void setAlgorithmID(String algorithmID) {
		this.algorithmID = algorithmID;
	}

	public String getCertThumbprint() {
		return certThumbprint;
	}

	public void setCertThumbprint(String certThumbprint) {
		this.certThumbprint = certThumbprint;
	}

	public String getIuId() {
		return iuId;
	}

	public void setIuId(String iuId) {
		this.iuId = iuId;
	}

	public String getIssuerName() {
		return issuerName;
	}

	public void setIssuerName(String issuerName) {
		this.issuerName = issuerName;
	}

	public String getsUid() {
		return sUid;
	}

	public void setsUid(String sUid) {
		this.sUid = sUid;
	}

	public String getCreateAt() {
		return createAt;
	}

	public void setCreateAt(String createAt) {
		this.createAt = createAt;
	}

	public String getExpiredAt() {
		return expiredAt;
	}

	public void setExpiredAt(String expiredAt) {
		this.expiredAt = expiredAt;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getClientAlias() {
		return clientAlias;
	}

	public void setClientAlias(String clientAlias) {
		this.clientAlias = clientAlias;
	}

	public String getCertFileName() {
		return certFileName;
	}

	public void setCertFileName(String certFileName) {
		this.certFileName = certFileName;
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

	public String getUpdateDateTime() {
		return updateDateTime;
	}

	public void setUpdateDateTime(String updateDateTime) {
		this.updateDateTime = updateDateTime;
	}

}
