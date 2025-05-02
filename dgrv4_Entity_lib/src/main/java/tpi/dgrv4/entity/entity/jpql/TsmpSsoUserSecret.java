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
@Table(name = "tsmp_sso_user_secret")
@TableGenerator(name = "seq_store", initialValue = 2_000_000_000)
public class TsmpSsoUserSecret {
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_store")
	@Column(name = "user_secret_id")
	private Long userSecretId;

	@Column(name = "user_name")
	private String userName;

	@Column(name = "secret")
	private String secret;

	@Column(name = "create_date_time")
	private Date createDateTime = DateTimeUtil.now();

	@Column(name = "create_user")
	private String createUser;

	@Column(name = "update_date_time")
	private Date updateDateTime;

	@Column(name = "update_user")
	private String updateUser;

	@Version
	@Column(name = "version")
	private Long version = 1L;

	public Long getUserSecretId() {
		return userSecretId;
	}

	@Override
	public String toString() {
		return "TsmpSsoUserSecret [userSecretId=" + userSecretId + ", userName=" + userName + ", secret=" + secret
				+ ", createDateTime=" + createDateTime + ", createUser=" + createUser + ", updateDateTime="
				+ updateDateTime + ", updateUser=" + updateUser + ", version=" + version + "]";
	}

	public void setUserSecretId(Long userSecretId) {
		this.userSecretId = userSecretId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
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