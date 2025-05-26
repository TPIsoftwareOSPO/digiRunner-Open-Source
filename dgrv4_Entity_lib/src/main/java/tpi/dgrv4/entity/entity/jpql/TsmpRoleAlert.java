package tpi.dgrv4.entity.entity.jpql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "tsmp_role_alert")
@IdClass(value = TsmpRoleAlertId.class)
public class TsmpRoleAlert {

	@Id
	@Column(name = "role_id")
	private String roleId;

	@Id
	@Column(name = "alert_id")
	private Long alertId;

	/* constructors */

	public TsmpRoleAlert() {}

	/* methods */

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
