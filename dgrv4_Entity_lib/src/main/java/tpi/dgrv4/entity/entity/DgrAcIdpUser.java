package tpi.dgrv4.entity.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import tpi.dgrv4.codec.constant.RandomLongTypeEnum;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.component.dgrSeq.DgrSeq;

@JsonIgnoreProperties(ignoreUnknown = true) 
@Entity
@Table(name = "dgr_ac_idp_user")
public class DgrAcIdpUser implements DgrSequenced {
	@Id
	@DgrSeq(strategy = RandomLongTypeEnum.YYYYMMDD)
	@Column(name = "ac_idp_user_id")
	private Long acIdpUserId;

	@Column(name = "user_name")
	private String userName;

	@Column(name = "user_alias")
	private String userAlias;

	@Column(name = "user_status")
	private String userStatus = "1";

	@Column(name = "user_email")
	private String userEmail;

	@Column(name = "org_id")
	private String orgId;

	@Column(name = "idp_type")
	private String idpType;

	@Column(name = "code1")
	private Long code1;

	@Column(name = "code2")
	private Long code2;

	@Column(name = "id_token_jwtstr")
	private String idTokenJwtstr;

	@Column(name = "access_token_jwtstr")
	private String accessTokenJwtstr;

	@Column(name = "refresh_token_jwtstr")
	private String refreshTokenJwtstr;

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
		return "DgrAcIdpUser [acIdpUserId=" + acIdpUserId + ", userName=" + userName + ", userAlias=" + userAlias
				+ ", userStatus=" + userStatus + ", userEmail=" + userEmail + ", orgId=" + orgId + ", idpType="
				+ idpType + ", code1=" + code1 + ", code2=" + code2 + ", idTokenJwtstr=" + idTokenJwtstr
				+ ", accessTokenJwtstr=" + accessTokenJwtstr + ", refreshTokenJwtstr=" + refreshTokenJwtstr
				+ ", createDateTime=" + createDateTime + ", createUser=" + createUser + ", updateDateTime="
				+ updateDateTime + ", updateUser=" + updateUser + ", version=" + version + "]";
	}

	public Long getAcIdpUserId() {
		return acIdpUserId;
	}

	public void setAcIdpUserId(Long acIdpUserId) {
		this.acIdpUserId = acIdpUserId;
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

	public String getUserStatus() {
		return userStatus;
	}

	public void setUserStatus(String userStatus) {
		this.userStatus = userStatus;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getIdpType() {
		return idpType;
	}

	public void setIdpType(String idpType) {
		this.idpType = idpType;
	}

	public Long getCode1() {
		return code1;
	}

	public void setCode1(Long code1) {
		this.code1 = code1;
	}

	public Long getCode2() {
		return code2;
	}

	public void setCode2(Long code2) {
		this.code2 = code2;
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

	@Override
	public Long getPrimaryKey() {
		return acIdpUserId;
	}
}