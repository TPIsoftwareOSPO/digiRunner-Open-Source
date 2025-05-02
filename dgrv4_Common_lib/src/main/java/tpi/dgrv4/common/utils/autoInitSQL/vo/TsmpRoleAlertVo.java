package tpi.dgrv4.common.utils.autoInitSQL.vo;

public class TsmpRoleAlertVo {

	private String roleId;

	private Long alertId;

	@Override
	public String toString() {
		return "TsmpRoleAlert [roleId=" + roleId + ", alertId=" + alertId + "]";
	}

	/* getters and setters */
	
	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public Long getAlertId() {
		return alertId;
	}

	public void setAlertId(Long alertId) {
		this.alertId = alertId;
	}

}
