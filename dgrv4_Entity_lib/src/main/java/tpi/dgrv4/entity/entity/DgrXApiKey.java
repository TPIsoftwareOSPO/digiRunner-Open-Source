package tpi.dgrv4.entity.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import tpi.dgrv4.codec.constant.RandomLongTypeEnum;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.component.dgrSeq.DgrSeq;

@Entity
@Table(name = "DGR_X_API_KEY")
public class DgrXApiKey implements DgrSequenced {
	@Id
	@DgrSeq(strategy = RandomLongTypeEnum.YYYYMMDD)
	@Column(name = "api_key_id")
	private Long apiKeyId;

	@Column(name = "client_id")
	private String clientId;

	@Column(name = "api_key_alias")
	private String apiKeyAlias;

	@Column(name = "effective_at")
	private Long effectiveAt;

	@Column(name = "expired_at")
	private Long expiredAt;

	@Column(name = "api_key")
	private String apiKey;

	@Column(name = "api_key_mask")
	private String apiKeyMask;

	@Column(name = "api_key_en")
	private String apiKeyEn;

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
	public Long getPrimaryKey() {
		return apiKeyId;
	}

	@Override
	public String toString() {
		return "DgrXApiKey [apiKeyId=" + apiKeyId + ", clientId=" + clientId + ", apiKeyAlias=" + apiKeyAlias
				+ ", effectiveAt=" + effectiveAt + ", expiredAt=" + expiredAt + ", apiKey=" + apiKey + ", apiKeyMask="
				+ apiKeyMask + ", apiKeyEn=" + apiKeyEn + ", createDateTime=" + createDateTime + ", createUser="
				+ createUser + ", updateDateTime=" + updateDateTime + ", updateUser=" + updateUser + ", version="
				+ version + "]";
	}

	public Long getApiKeyId() {
		return apiKeyId;
	}

	public void setApiKeyId(Long apiKeyId) {
		this.apiKeyId = apiKeyId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getApiKeyAlias() {
		return apiKeyAlias;
	}

	public void setApiKeyAlias(String apiKeyAlias) {
		this.apiKeyAlias = apiKeyAlias;
	}

	public Long getEffectiveAt() {
		return effectiveAt;
	}

	public void setEffectiveAt(Long effectiveAt) {
		this.effectiveAt = effectiveAt;
	}

	public Long getExpiredAt() {
		return expiredAt;
	}

	public void setExpiredAt(Long expiredAt) {
		this.expiredAt = expiredAt;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getApiKeyMask() {
		return apiKeyMask;
	}

	public void setApiKeyMask(String apiKeyMask) {
		this.apiKeyMask = apiKeyMask;
	}

	public String getApiKeyEn() {
		return apiKeyEn;
	}

	public void setApiKeyEn(String apiKeyEn) {
		this.apiKeyEn = apiKeyEn;
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
