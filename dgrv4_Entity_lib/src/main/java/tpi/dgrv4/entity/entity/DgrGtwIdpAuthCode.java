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
@Table(name = "dgr_gtw_idp_auth_code")
@TableGenerator(name = "seq_store", initialValue = 2000000000)
public class DgrGtwIdpAuthCode {
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_store")
	@Column(name = "gtw_idp_auth_code_id")
	private Long gtwIdpAuthCodeId;

	@Column(name = "auth_code")
	private String authCode;

	@Column(name = "phase")
	private String phase;

	@Column(name = "status")
	private String status = TsmpAuthCodeStatus2.AVAILABLE.value();

	@Column(name = "expire_date_time")
	private Long expireDateTime;

	@Column(name = "idp_type")
	private String idpType;

	@Column(name = "client_id")
	private String clientId;

	@Column(name = "user_name")
	private String userName;

	@Column(name = "user_alias")
	private String userAlias;

	@Column(name = "user_email")
	private String userEmail;

	@Column(name = "user_picture")
	private String userPicture;

	@Column(name = "id_token_jwtstr")
	private String idTokenJwtstr;

	@Column(name = "access_token_jwtstr")
	private String accessTokenJwtstr;

	@Column(name = "refresh_token_jwtstr")
	private String refreshTokenJwtstr;

	@Column(name = "api_resp")
	private String apiResp;

	@Column(name = "state")
	private String state;

	@Column(name = "user_light_id")
	private String userLightId;

	@Column(name = "user_role_name")
	private String userRoleName;


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
		return "DgrGtwIdpAuthCode [gtwIdpAuthCodeId=" + gtwIdpAuthCodeId + ", authCode=" + authCode + ", phase=" + phase
				+ ", status=" + status + ", expireDateTime=" + expireDateTime + ", idpType=" + idpType + ", clientId="
				+ clientId + ", userName=" + userName + ", userAlias=" + userAlias + ", userEmail=" + userEmail
				+ ", userPicture=" + userPicture + ", idTokenJwtstr=" + idTokenJwtstr + ", accessTokenJwtstr="
				+ accessTokenJwtstr + ", refreshTokenJwtstr=" + refreshTokenJwtstr + ", apiResp=" + apiResp + ", state="
				+ state + ", createDateTime=" + createDateTime + ", createUser=" + createUser + ", updateDateTime="
				+ updateDateTime + ", updateUser=" + updateUser + ", version=" + version + "]";
	}

	public Long getGtwIdpAuthCodeId() {
		return gtwIdpAuthCodeId;
	}

	public void setGtwIdpAuthCodeId(Long gtwIdpAuthCodeId) {
		this.gtwIdpAuthCodeId = gtwIdpAuthCodeId;
	}

	public String getAuthCode() {
		return authCode;
	}

	public void setAuthCode(String authCode) {
		this.authCode = authCode;
	}

	public String getPhase() {
		return phase;
	}

	public void setPhase(String phase) {
		this.phase = phase;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getExpireDateTime() {
		return expireDateTime;
	}

	public void setExpireDateTime(Long expireDateTime) {
		this.expireDateTime = expireDateTime;
	}

	public String getIdpType() {
		return idpType;
	}

	public void setIdpType(String idpType) {
		this.idpType = idpType;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserAlias() {
		return userAlias;
	}

	public void setUserAlias(String userAlias) {
		this.userAlias = userAlias;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getUserPicture() {
		return userPicture;
	}

	public void setUserPicture(String userPicture) {
		this.userPicture = userPicture;
	}

	public String getIdTokenJwtstr() {
		return idTokenJwtstr;
	}

	public void setIdTokenJwtstr(String idTokenJwtstr) {
		this.idTokenJwtstr = idTokenJwtstr;
	}

	public String getAccessTokenJwtstr() {
		return accessTokenJwtstr;
	}

	public void setAccessTokenJwtstr(String accessTokenJwtstr) {
		this.accessTokenJwtstr = accessTokenJwtstr;
	}

	public String getRefreshTokenJwtstr() {
		return refreshTokenJwtstr;
	}

	public void setRefreshTokenJwtstr(String refreshTokenJwtstr) {
		this.refreshTokenJwtstr = refreshTokenJwtstr;
	}

	public String getApiResp() {
		return apiResp;
	}

	public void setApiResp(String apiResp) {
		this.apiResp = apiResp;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getUserLightId() {
		return userLightId;
	}

	public void setUserLightId(String userLightId) {
		this.userLightId = userLightId;
	}

	public String getUserRoleName() {
		return userRoleName;
	}

	public void setUserRoleName(String userRoleName) {
		this.userRoleName = userRoleName;
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