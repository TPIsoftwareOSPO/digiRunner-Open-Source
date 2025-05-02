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

import tpi.dgrv4.common.constant.TsmpAuthCodeStatus2;
import tpi.dgrv4.common.utils.DateTimeUtil;

@Entity
@Table(name = "dgr_ac_idp_auth_code")
@TableGenerator(name = "seq_store", initialValue = 2000000000)
public class DgrAcIdpAuthCode {
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_store")
	@Column(name = "ac_idp_auth_code_id")
	private Long acIdpAuthCodeId;

	@Column(name = "auth_code")
	private String authCode;

	@Column(name = "expire_date_time")
	private Long expireDateTime;

	@Column(name = "status")
	private String status = TsmpAuthCodeStatus2.AVAILABLE.value();

	@Column(name = "idp_type")
	private String idpType;

	@Column(name = "user_name")
	private String userName;
	
	@Column(name = "api_resp")
	private String apiResp;

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
		return "DgrAcIdpAuthCode [acIdpAuthCodeId=" + acIdpAuthCodeId + ", authCode=" + authCode + ", expireDateTime="
				+ expireDateTime + ", status=" + status + ", idpType=" + idpType + ", userName=" + userName
				+ ", apiResp=" + apiResp + ", createDateTime=" + createDateTime + ", createUser=" + createUser
				+ ", updateDateTime=" + updateDateTime + ", updateUser=" + updateUser + ", version=" + version + "]";
	}

	public Long getAcIdpAuthCodeId() {
		return acIdpAuthCodeId;
	}

	public void setAcIdpAuthCodeId(Long acIdpAuthCodeId) {
		this.acIdpAuthCodeId = acIdpAuthCodeId;
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

	public String getIdpType() {
		return idpType;
	}

	public void setIdpType(String idpType) {
		this.idpType = idpType;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getApiResp() {
		return apiResp;
	}

	public void setApiResp(String apiResp) {
		this.apiResp = apiResp;
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
