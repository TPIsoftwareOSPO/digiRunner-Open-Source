package tpi.dgrv4.entity.entity.autoInitSQL;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tsmp_role_role_mapping")
public class AutoInitSQLTsmpRoleRoleMapping {

	@Id
	@Column(name = "ROLE_ROLE_ID")
	private Long roleRoleId;
	
	@Column(name = "ROLE_NAME")
	private String roleName;

	@Column(name = "ROLE_NAME_MAPPING")
	private String roleNameMapping;

	/* constructors */

	public AutoInitSQLTsmpRoleRoleMapping() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpRoleRoleMapping [roleRoleId=" + roleRoleId + ", roleName=" + roleName + ", roleNameMapping="
				+ roleNameMapping + "]";
	}
	
	/* getters and setters */
	
	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getRoleNameMapping() {
		return roleNameMapping;
	}

	public void setRoleNameMapping(String roleNameMapping) {
		this.roleNameMapping = roleNameMapping;
	}

	public Long getRoleRoleId() {
		return roleRoleId;
	}

	public void setRoleRoleId(Long roleRoleId) {
		this.roleRoleId = roleRoleId;
	}
	
}
