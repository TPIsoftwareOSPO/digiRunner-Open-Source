package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0003Resp {
	/** 使用者編號*/
	private String userID;
	
	/** 使用者帳號*/
	private String userName;
	
	/** 使用者名稱*/
	private String userAlias;
	
	/** 組織名稱*/
	private String orgName;
	
	/** 角色ID清單*/
	private List<String> roleID;
	
	/** 角色清單*/
	private List<String> roleAlias;
	
	/** 使用者E-mail*/
	private String userMail;
	
	/** 登錄日期*/
	private String logonDate;
	
	/** 建立日期 */
	private String createDate;
	
	/** 狀態*/
	private String status;
	
	/** 狀態名稱*/
	private String statusName;
	
	/** 重置密碼錯誤次數*/
	private Integer pwdFailTimes;
	
	/** 組織id*/
	private String orgId;
	
	/** IdP Type */
	private String idPType;

	/** ID Token 的 JWT 資料 */
	private String idTokenJwtstr;
	
	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getUserName() {
		return userName;
	}

	public String getUserAlias() {
		return userAlias;
	}

	public void setUserAlias(String userAlias) {
		this.userAlias = userAlias;
	}

	public String getOrgName() {
		return orgName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public List<String> getRoleID() {
		return roleID;
	}
	
	public List<String> getRoleAlias() {
		return roleAlias;
	}

	public void setRoleAlias(List<String> roleAlias) {
		this.roleAlias = roleAlias;
	}

	public String getUserMail() {
		return userMail;
	}
	
	public void setRoleID(List<String> roleID) {
		this.roleID = roleID;
	}

	public void setUserMail(String userMail) {
		this.userMail = userMail;
	}

	public String getLogonDate() {
		return logonDate;
	}

	public void setLogonDate(String logonDate) {
		this.logonDate = logonDate;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}
	public String getStatusName() {
		return statusName;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
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

	public String getIdPType() {
		return idPType;
	}

	public void setIdPType(String idPType) {
		this.idPType = idPType;
	}
	
	public String getIdTokenJwtstr() {
		return idTokenJwtstr;
	}

	public void setIdTokenJwtstr(String idTokenJwtstr) {
		this.idTokenJwtstr = idTokenJwtstr;
	}

	@Override
	public String toString() {
		return "AA0003Resp [userID=" + userID + ", userName=" + userName + ", userAlias=" + userAlias + ", orgName="
				+ orgName + ", roleID=" + roleID + ", roleAlias=" + roleAlias + ", userMail=" + userMail
				+ ", logonDate=" + logonDate + ", createDate=" + createDate + ", status=" + status + ", statusName="
				+ statusName + ", pwdFailTimes=" + pwdFailTimes + ", orgId=" + orgId + ", idPType=" + idPType
				+ ", idTokenJwtstr=" + idTokenJwtstr + "]";
	}
}
