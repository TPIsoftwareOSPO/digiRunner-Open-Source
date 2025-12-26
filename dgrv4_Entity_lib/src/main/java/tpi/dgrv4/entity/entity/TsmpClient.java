package tpi.dgrv4.entity.entity;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tsmp_client")
public class TsmpClient implements Serializable {

	@Id
	@Column(name = "client_id")
	private String clientId;

	@Column(name = "client_name")
	private String clientName;

	@Column(name = "client_alias")
	private String clientAlias;

	@Column(name = "client_status")
	private String clientStatus = "1";

	@Column(name = "tps")
	private Integer tps = 10;

	@Column(name = "emails")
	private String emails;

	@Column(name = "api_quota")
	private Integer apiQuota;

	@Column(name = "api_used")
	private Integer apiUsed;

	@Column(name = "c_priority")
	private Integer cPriority = 5;

	@Column(name = "create_time")
	private Date createTime;

	@Column(name = "update_time")
	private Date updateTime;

	@Column(name = "owner")
	private String owner;

	@Column(name = "remark")
	private String remark;

	@Column(name = "create_user")
	private String createUser;

	@Column(name = "update_user")
	private String updateUser;

	@Column(name = "security_level_id")
	private String securityLevelId;

	@Column(name = "signup_num")
	private String signupNum;

	@Column(name = "pwd_fail_times")
	private Integer pwdFailTimes = 0;

	@Column(name = "fail_treshhold")
	private Integer failTreshhold = 3;

	@Column(name = "ACCESS_TOKEN_QUOTA")
	private Integer accessTokenQuota;

	@Column(name = "REFRESH_TOKEN_QUOTA")
	private Integer refreshTokenQuota;

	@Column(name = "client_secret")
	private String clientSecret;

	@Column(name = "start_date")
	private Long startDate;

	@Column(name = "end_date")
	private Long endDate;

	@Column(name = "start_time_per_day")
	private Long startTimePerDay;

	@Column(name = "end_time_per_day")
	private Long endTimePerDay;

	@Column(name = "time_zone")
	private String timeZone;

	@Column(name = "dp_client_entry")
	private String dpClientEntry;

	/* constructors */
	public TsmpClient() {
	}

	@Override
	public String toString() {
		return "TsmpClient [clientId=" + clientId + ", clientName=" + clientName + ", clientAlias=" + clientAlias
				+ ", clientStatus=" + clientStatus + ", tps=" + tps + ", emails=" + emails + ", apiQuota=" + apiQuota
				+ ", apiUsed=" + apiUsed + ", cPriority=" + cPriority + ", createTime=" + createTime + ", updateTime="
				+ updateTime + ", owner=" + owner + ", remark=" + remark + ", createUser=" + createUser
				+ ", updateUser=" + updateUser + ", securityLevelId=" + securityLevelId + ", signupNum=" + signupNum
				+ ", pwdFailTimes=" + pwdFailTimes + ", failTreshhold=" + failTreshhold + ", accessTokenQuota="
				+ accessTokenQuota + ", refreshTokenQuota=" + refreshTokenQuota + ", clientSecret=" + clientSecret
				+ ", startDate=" + startDate + ", endDate=" + endDate + ", startTimePerDay=" + startTimePerDay
				+ ", endTimePerDay=" + endTimePerDay + ", timeZone=" + timeZone + ", clientStatus=" + clientStatus
				+ ", dpClientEntry=" + dpClientEntry + "]\n";
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getClientAlias() {
		return clientAlias;
	}

	public void setClientAlias(String clientAlias) {
		this.clientAlias = clientAlias;
	}

	public String getClientStatus() {
		return clientStatus;
	}

	public void setClientStatus(String clientStatus) {
		this.clientStatus = clientStatus;
	}

	public Integer getTps() {
		return tps;
	}

	public void setTps(Integer tps) {
		this.tps = tps;
	}

	public String getEmails() {
		return emails;
	}

	public void setEmails(String emails) {
		this.emails = emails;
	}

	public Integer getApiQuota() {
		return apiQuota;
	}

	public void setApiQuota(Integer apiQuota) {
		this.apiQuota = apiQuota;
	}

	public Integer getApiUsed() {
		return apiUsed;
	}

	public void setApiUsed(Integer apiUsed) {
		this.apiUsed = apiUsed;
	}

	public Integer getcPriority() {
		return cPriority;
	}

	public void setcPriority(Integer cPriority) {
		this.cPriority = cPriority;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public String getSecurityLevelId() {
		return securityLevelId;
	}

	public void setSecurityLevelId(String securityLevelId) {
		this.securityLevelId = securityLevelId;
	}

	public String getSignupNum() {
		return signupNum;
	}

	public void setSignupNum(String signupNum) {
		this.signupNum = signupNum;
	}

	public Integer getPwdFailTimes() {
		return pwdFailTimes;
	}

	public void setPwdFailTimes(Integer pwdFailTimes) {
		this.pwdFailTimes = pwdFailTimes;
	}

	public Integer getFailTreshhold() {
		return failTreshhold;
	}

	public void setFailTreshhold(Integer failTreshhold) {
		this.failTreshhold = failTreshhold;
	}

	public Integer getAccessTokenQuota() {
		return accessTokenQuota;
	}

	public void setAccessTokenQuota(Integer accessTokenQuota) {
		this.accessTokenQuota = accessTokenQuota;
	}

	public Integer getRefreshTokenQuota() {
		return refreshTokenQuota;
	}

	public void setRefreshTokenQuota(Integer refreshTokenQuota) {
		this.refreshTokenQuota = refreshTokenQuota;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public Long getStartDate() {
		return startDate;
	}

	public void setStartDate(Long startDate) {
		this.startDate = startDate;
	}

	public Long getEndDate() {
		return endDate;
	}

	public void setEndDate(Long endDate) {
		this.endDate = endDate;
	}

	public Long getStartTimePerDay() {
		return startTimePerDay;
	}

	public void setStartTimePerDay(Long startTimePerDay) {
		this.startTimePerDay = startTimePerDay;
	}

	public Long getEndTimePerDay() {
		return endTimePerDay;
	}

	public void setEndTimePerDay(Long endTimePerDay) {
		this.endTimePerDay = endTimePerDay;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public String getDpClientEntry() {
		return dpClientEntry;
	}

	public void setDpClientEntry(String dpClientEntry) {
		this.dpClientEntry = dpClientEntry;
	}

}