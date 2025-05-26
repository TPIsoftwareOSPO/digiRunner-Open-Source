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

import tpi.dgrv4.common.utils.DateTimeUtil;


@Entity
@Table(name = "ldap_auth_result")
@TableGenerator(name = "seq_store", initialValue = 2_000_000_000)
public class LdapAuthResult {
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_store")
	@Column(name = "ldap_id")
	private Long ldapId;

	@Column(name = "user_name")
	private String userName;

	@Column(name = "code_challenge")
	private String codeChallenge;

	@Column(name = "user_ip")
	private String userIp;
	
	@Column(name = "use_date_time")
	private Date useDateTime;

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
		return "LdapAuthResult [ldapId=" + ldapId + ", userName=" + userName + ", codeChallenge=" + codeChallenge
				+ ", userIp=" + userIp + ", useDateTime=" + useDateTime + ", createDateTime=" + createDateTime
				+ ", createUser=" + createUser + ", updateDateTime=" + updateDateTime + ", updateUser=" + updateUser
				+ ", version=" + version + "]\n";
	}

	public Long getLdapId() {
		return ldapId;
	}

	public void setLdapId(Long ldapId) {
		this.ldapId = ldapId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getCodeChallenge() {
		return codeChallenge;
	}

	public void setCodeChallenge(String codeChallenge) {
		this.codeChallenge = codeChallenge;
	}

	public String getUserIp() {
		return userIp;
	}

	public void setUserIp(String userIp) {
		this.userIp = userIp;
	}

	public Date getUseDateTime() {
		return useDateTime;
	}

	public void setUseDateTime(Date useDateTime) {
		this.useDateTime = useDateTime;
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
