package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0019List {
	
	/** 使用者編號 */
	private String userID;
	
	/** 使用者帳號 */
	private String userName;
	
	/** 使用者名稱 */
	private String userAlias;

	/** 組織ID */
	private String orgName;
	
	/** 組織名稱 */
	private String orgId;
	
	/** 角色名稱 */
	private List<String> roleAlias;
	
	/** 狀態	 */
	private String status;
	
	/** 狀態名稱	 */
	private String statusName;
	
	
	public String getUserID() {
		return userID;
	}
	
	public void setUserID(String userID) {
		this.userID = userID;
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
	
	public String getOrgName() {
		return orgName;
	}
	
	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}
	
	public String getOrgId() {
		return orgId;
	}
	
	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}
	
	public List<String> getRoleAlias() {
		return roleAlias;
	}
	
	public void setRoleAlias(List<String> roleAlias) {
		this.roleAlias = roleAlias;
	}
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getStatusName() {
		return statusName;
	}
	
	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}

	@Override
	public String toString() {
		return "AA0019List [userID=" + userID + ", userName=" + userName + ", userAlias=" + userAlias + ", orgName="
				+ orgName + ", orgId=" + orgId + ", roleAlias=" + roleAlias + ", status=" + status + ", statusName="
				+ statusName + "]";
	}
	
	
	
}
