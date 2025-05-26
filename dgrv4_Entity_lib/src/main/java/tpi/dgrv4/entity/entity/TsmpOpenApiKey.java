package tpi.dgrv4.entity.entity;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.Version;

import tpi.dgrv4.common.utils.DateTimeUtil;

@SuppressWarnings("serial")
@Entity
@Table(name = "tsmp_open_apikey")
@TableGenerator(name = "seq_store", initialValue = 2_000_000_000)
public class TsmpOpenApiKey implements Serializable {
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_store")
	@Column(name = "open_apikey_id")
	private Long openApiKeyId;
	
	@Column(name = "client_id")
	private String clientId;
	
	@Column(name = "open_apikey")
	private String openApiKey;
	
	@Column(name = "secret_key")
	private String secretKey;
	
	@Column(name = "open_apikey_alias")
	private String openApiKeyAlias;
	
	@Column(name = "times_quota")
	private Integer timesQuota = -1;
	
	@Column(name = "times_threshold")
	private Integer timesThreshold = -1;
	
	@Column(name = "expired_at")
	private Long expiredAt;
	
	@Column(name = "revoked_at")
	private Long revokedAt;
	
	@Column(name = "open_apikey_status")
	private String openApiKeyStatus = "1";
	
	@Column(name = "rollover_flag")
	private String rolloverFlag = "N";
	
	//
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

	@Override
	public String toString() {
		return "TsmpOpenApiKey [openApiKeyId=" + openApiKeyId + ", clientId=" + clientId + ", openApiKey=" + openApiKey
				+ ", secretKey=" + secretKey + ", openApiKeyAlias=" + openApiKeyAlias + ", timesQuota=" + timesQuota
				+ ", timesThreshold=" + timesThreshold + ", expiredAt=" + expiredAt + ", revokedAt=" + revokedAt
				+ ", openApiKeyStatus=" + openApiKeyStatus + ", rolloverFlag=" + rolloverFlag + ", createDateTime="
				+ createDateTime + ", createUser=" + createUser + ", updateDateTime=" + updateDateTime + ", updateUser="
				+ updateUser + ", version=" + version + "]\n";
	}

	public Long getOpenApiKeyId() {
		return openApiKeyId;
	}

	public void setOpenApiKeyId(Long openApiKeyId) {
		this.openApiKeyId = openApiKeyId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getOpenApiKey() {
		return openApiKey;
	}

	public void setOpenApiKey(String openApiKey) {
		this.openApiKey = openApiKey;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getOpenApiKeyAlias() {
		return openApiKeyAlias;
	}

	public void setOpenApiKeyAlias(String openApiKeyAlias) {
		this.openApiKeyAlias = openApiKeyAlias;
	}

	public Integer getTimesQuota() {
		return timesQuota;
	}

	public void setTimesQuota(Integer timesQuota) {
		this.timesQuota = timesQuota;
	}

	public Integer getTimesThreshold() {
		return timesThreshold;
	}

	public void setTimesThreshold(Integer timesThreshold) {
		this.timesThreshold = timesThreshold;
	}

	public Long getExpiredAt() {
		return expiredAt;
	}

	public void setExpiredAt(Long expiredAt) {
		this.expiredAt = expiredAt;
	}

	public Long getRevokedAt() {
		return revokedAt;
	}

	public void setRevokedAt(Long revokedAt) {
		this.revokedAt = revokedAt;
	}

	public String getOpenApiKeyStatus() {
		return openApiKeyStatus;
	}

	public void setOpenApiKeyStatus(String openApiKeyStatus) {
		this.openApiKeyStatus = openApiKeyStatus;
	}

	public String getRolloverFlag() {
		return rolloverFlag;
	}

	public void setRolloverFlag(String rolloverFlag) {
		this.rolloverFlag = rolloverFlag;
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
