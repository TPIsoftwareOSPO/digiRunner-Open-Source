package tpi.dgrv4.common.utils.autoInitSQL.vo;

import java.util.Date;


public class TsmpUserVo {


	private String userId;


	private String userName;

	private String userStatus = "1";

	private String userAlias;

	private String userEmail;

	private Date logonDate;

	private Date logoffDate;

	private String updateUser;

	private Date updateTime;

	private String createUser;

	private Date createTime;

	private Integer pwdFailTimes = 0;

	private String orgId;

	/* constructors */
	public TsmpUserVo() {}
	
	public TsmpUserVo(String userName, Date logonDate, String userStatus, Date createTime) {
		this.userName = userName;
		this.logonDate = logonDate;
		this.userStatus = userStatus;
		this.createTime = createTime;
	}
	
	public TsmpUserVo(String userName, String userAlias, Date logonDate, String userStatus) {
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
