package tpi.dgrv4.entity.entity.jpql;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;

import tpi.dgrv4.common.utils.DateTimeUtil;

@MappedSuperclass
public class TsmpClientCertBasic{
	
	@Column(name = "client_id")
	protected String clientId;

	@Column(name = "cert_file_name")
	protected String certFileName;

	@Column(name = "file_content")
	private byte[] fileContent;

	@Column(name = "pub_Key")
	protected String pubKey;

	@Column(name = "cert_version")
	private String certVersion;

	@Column(name = "cert_serial_num")
	private String certSerialNum;

	@Column(name = "s_algorithm_id")
	private String sAlgorithmId;

	@Column(name = "algorithm_id")
	private String algorithmId;

	@Column(name = "cert_thumbprint")
	private String certThumbprint;

	@Column(name = "iuid")
	private String iuid;

	@Column(name = "issuer_name")
	private String issuerName;

	@Column(name = "suid")
	private String suid;

	@Column(name = "create_at")
	private Long createAt;

	@Column(name = "expired_at")
	private Long expiredAt;
	
	@Column(name = "key_size")
	private Integer keySize = 0;
	
	@Column(name = "create_date_time")
	private Date createDateTime = DateTimeUtil.now();

	@Column(name = "create_user")
	private String createUser = "SYSTEM";

	@Column(name = "update_date_time")
	private Date updateDateTime;

	@Column(name = "update_user")
	private String updateUser;

	@Version
	@Column(name = "version")
	private Long version = 1L;

	/* constructors */
	
	public TsmpClientCertBasic() {
	}

	/* getters and setters */

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getCertFileName() {
		return certFileName;
	}

	public void setCertFileName(String certFileName) {
		this.certFileName = certFileName;
	}

	public byte[] getFileContent() {
		return fileContent;
	}

	public void setFileContent(byte[] fileContent) {
		this.fileContent = fileContent;
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

	public void setCreateAt(Long cerateAt) {
		this.createAt = cerateAt;
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

	public Date getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(Date createDateTime) {
		this.createDateTime = createDateTime;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public Date getUpdateDateTime() {
		return updateDateTime;
	}

	public void setUpdateDateTime(Date updateDateTime) {
		this.updateDateTime = updateDateTime;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}
	 
}
