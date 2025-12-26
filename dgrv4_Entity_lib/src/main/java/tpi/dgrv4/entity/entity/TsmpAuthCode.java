package tpi.dgrv4.entity.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.Version;

import tpi.dgrv4.common.constant.TsmpAuthCodeStatus;
import tpi.dgrv4.common.utils.DateTimeUtil;

@Entity
@Table(name = "tsmp_auth_code")
@TableGenerator(name = "seq_store", initialValue = 2_000_000_000)
public class TsmpAuthCode {
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_store")
	@Column(name = "auth_code_id")
	private Long authCodeId;

	@Column(name = "auth_code")
	private String authCode;

	@Column(name = "expire_date_time")
	private Long expireDateTime;

	@Column(name = "status")
	private String status = TsmpAuthCodeStatus.AVAILABLE.value();

	@Column(name = "auth_type")
	private String authType;

	@Column(name = "client_name")
	private String clientName;

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

	public TsmpAuthCode() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpAuthCode [authCodeId=" + authCodeId + ", authCode=" + authCode + ", expireDateTime="
				+ expireDateTime + ", status=" + status + ", authType=" + authType + ", clientName=" + clientName
				+ ", createDateTime=" + createDateTime + ", createUser=" + createUser + ", updateDateTime="
				+ updateDateTime + ", updateUser=" + updateUser + ", version=" + version + "]";
	}

	/* getters and setters */

	public Long getAuthCodeId() {
		return authCodeId;
	}

	public void setAuthCodeId(Long authCodeId) {
		this.authCodeId = authCodeId;
	}

	public String getAuthCode() {
		return authCode;
	}

	public void setAuthCode(String authCode) {
		this.authCode = authCode;
	}

	public Long getExpireDateTime() {
		return expireDateTime;
	}

	public void setExpireDateTime(Long expireDateTime) {
		this.expireDateTime = expireDateTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAuthType() {
		return authType;
	}

	public void setAuthType(String authType) {
		this.authType = authType;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
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
