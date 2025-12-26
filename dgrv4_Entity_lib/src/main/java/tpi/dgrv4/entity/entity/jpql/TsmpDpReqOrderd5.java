package tpi.dgrv4.entity.entity.jpql;

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

@Entity
@Table(name = "tsmp_dp_req_orderd5")
@TableGenerator(name = "seq_store", initialValue = 2_000_000_000)
public class TsmpDpReqOrderd5 {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_store")
	@Column(name = "req_orderd5_id")
	private Long reqOrderd5Id;

	@Column(name = "client_id")
	private String clientId;

	@Column(name = "ref_req_orderm_id")
	private Long refReqOrdermId;

	@Column(name = "ref_open_apikey_id")
	private Long refOpenApiKeyId;

	@Column(name = "open_apikey")
	private String openApiKey;

	@Column(name = "secret_key")
	private String secretKey;

	@Column(name = "open_apikey_alias")
	private String openApiKeyAlias;

	@Column(name = "times_threshold")
	private Integer timesThreshold = 0;

	@Column(name = "expired_at")
	private Long expiredAt;

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
		return "TsmpDpReqOrderd5 [reqOrderd5Id=" + reqOrderd5Id + ", clientId=" + clientId + ", refReqOrdermId="
				+ refReqOrdermId + ", refOpenApiKeyId=" + refOpenApiKeyId + ", openApiKey=" + openApiKey
				+ ", secretKey=" + secretKey + ", openApiKeyAlias=" + openApiKeyAlias + ", timesThreshold="
				+ timesThreshold + ", expiredAt=" + expiredAt + ", createDateTime=" + createDateTime + ", createUser="
				+ createUser + ", updateDateTime=" + updateDateTime + ", updateUser=" + updateUser + ", version="
				+ version + "]";
	}

	public Long getReqOrderd5Id() {
		return reqOrderd5Id;
	}

	public void setReqOrderd5Id(Long reqOrderd5Id) {
		this.reqOrderd5Id = reqOrderd5Id;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public Long getRefReqOrdermId() {
		return refReqOrdermId;
	}

	public void setRefReqOrdermId(Long refReqOrdermId) {
		this.refReqOrdermId = refReqOrdermId;
	}

	public Long getRefOpenApiKeyId() {
		return refOpenApiKeyId;
	}

	public void setRefOpenApiKeyId(Long refOpenApiKeyId) {
		this.refOpenApiKeyId = refOpenApiKeyId;
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
