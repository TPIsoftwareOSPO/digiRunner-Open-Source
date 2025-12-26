package tpi.dgrv4.entity.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tsmp_user")
public class TsmpUser {

	@Id
	@Column(name = "user_id")
	private String userId;

	@Column(name = "user_name")
	private String userName;

	@Column(name = "user_status")
	private String userStatus = "1";

	@Column(name = "user_alias")
	private String userAlias;

	@Column(name = "user_email")
	private String userEmail;

	@Column(name = "logon_date")
	private Date logonDate;

	@Column(name = "logoff_date")
	private Date logoffDate;

	@Column(name = "update_user")
	private String updateUser;

	@Column(name = "update_time")
	private Date updateTime;

	@Column(name = "create_user")
	private String createUser;

	@Column(name = "create_time")
	private Date createTime;

	@Column(name = "pwd_fail_times")
	private Integer pwdFailTimes = 0;

	@Column(name = "org_id")
	private String orgId;

	/* constructors */
	public TsmpUser() {}
	
	public TsmpUser(String userName, Date logonDate, String userStatus, Date createTime) {
		this.userName = userName;
		this.logonDate = logonDate;
		this.userStatus = userStatus;
		this.createTime = createTime;
	}
	
	public TsmpUser(String userName, String userAlias, Date logonDate, String userStatus) {
		this.userName = userName;
		this.userAlias = userAlias;
		this.logonDate = logonDate;
		this.userStatus = userStatus;
	}

	/* methods */
	@Override
	public String toString() {
		return "TsmpUser [userId=" + userId + ", userName=" + userName + ", userStatus=" + userStatus + ", userAlias="
				+ userAlias + ", userEmail=" + userEmail + ", logonDate=" + logonDate + ", logoffDate=" + logoffDate
				+ ", updateUser=" + updateUser + ", updateTime=" + updateTime + ", createUser=" + createUser
				+ ", createTime=" + createTime + ", pwdFailTimes=" + pwdFailTimes + ", orgId=" + orgId + "]";
	}
	

	/* getters and setters */

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserStatus() {
		return userStatus;
	}

	public void setUserStatus(String userStatus) {
		this.userStatus = userStatus;
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

	public Date getLogonDate() {
		return logonDate;
	}

	public void setLogonDate(Date logonDate) {
		this.logonDate = logonDate;
	}

	public Date getLogoffDate() {
		return logoffDate;
	}

	public void setLogoffDate(Date logoffDate) {
		this.logoffDate = logoffDate;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Integer getPwdFailTimes() {
		return pwdFailTimes;
	}

	public void setPwdFailTimes(Integer pwdFailTimes) {
		this.pwdFailTimes = pwdFailTimes;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

}
