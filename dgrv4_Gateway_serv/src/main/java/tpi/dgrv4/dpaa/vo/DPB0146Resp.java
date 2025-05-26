package tpi.dgrv4.dpaa.vo;

import java.util.List;

import tpi.dgrv4.common.constant.DgrIdPType;
import tpi.dgrv4.dpaa.util.ServiceUtil;

public class DPB0146Resp {

	private String longId;

	/** 組織名稱 */
	private String orgName = "";

	/** 角色ID清單 */
	private List<String> roleId;

	/** 角色清單 */
	private List<String> roleAlias;

	/** 組織id */
	private String orgId;

	private String userName;

	private String userNameOrig;

	private String userAlias;

	private String status;

	private String statusName;

	private String userEmail;

	private String idpType;

	public String getUserNameOrig() {
		return userNameOrig;
	}

	public void setUserNameOrig(String userNameOrig) {
		this.userNameOrig = userNameOrig;
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

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
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

	public String getStatusName() {
		return statusName;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getIdpType() {
		return idpType;
	}

	public void setIdpType(String idpType) {
		this.idpType = idpType;
	}

}
