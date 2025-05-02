package tpi.dgrv4.entity.entity.jpql;

import java.io.Serializable;

@SuppressWarnings("serial")
public class TsmpRoleAlertId implements Serializable {

	private String roleId;

	private Long alertId;

	public TsmpRoleAlertId() {}

	public TsmpRoleAlertId(String roleId, Long alertId) {
		super();
		this.roleId = roleId;
		this.alertId = alertId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alertId == null) ? 0 : alertId.hashCode());
		result = prime * result + ((roleId == null) ? 0 : roleId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TsmpRoleAlertId other = (TsmpRoleAlertId) obj;
		if (alertId == null) {
			if (other.alertId != null)
				return false;
		} else if (!alertId.equals(other.alertId))
			return false;
		if (roleId == null) {
			if (other.roleId != null)
				return false;
		} else if (!roleId.equals(other.roleId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TsmpRoleAlertId [roleId=" + roleId + ", alertId=" + alertId + "]";
	}

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
