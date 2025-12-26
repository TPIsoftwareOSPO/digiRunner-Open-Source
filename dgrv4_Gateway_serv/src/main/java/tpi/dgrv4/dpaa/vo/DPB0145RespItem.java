package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0145RespItem {

	private String id;
	private String longId;
	private String userName;
	private String userNameOrig;
	private String userAlias;
	private String status;
	private String icon;
	private String idpType;
	private String statusName;
	private String orgId;
	private String orgName;
	private List<String> roleId;
	private List<String> roleAlias;

	@Override
	public String toString() {
		return "DPB0145RespItem [id=" + id + ", longId=" + longId + ", userName=" + userName + ", userAlias="
				+ userAlias + ", status=" + status + ", icon=" + icon + ", idpType=" + idpType + ", statusName="
				+ statusName + ", orgId=" + orgId + ", orgName=" + orgName + "]";
	}

	public String getUserNameOrig() {
		return userNameOrig;
	}

	public void setUserNameOrig(String userNameOrig) {
		this.userNameOrig = userNameOrig;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLongId() {
		return longId;
	}

	public void setLongId(String longId) {
		this.longId = longId;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getIdpType() {
		return idpType;
	}

	public void setIdpType(String idpType) {
		this.idpType = idpType;
	}

	public String getStatusName() {
		return statusName;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public List<String> getRoleId() {
		return roleId;
	}

	public void setRoleId(List<String> roleId) {
		this.roleId = roleId;
	}

	public List<String> getRoleAlias() {
		return roleAlias;
	}

	public void setRoleAlias(List<String> roleAlias) {
		this.roleAlias = roleAlias;
	}
}
