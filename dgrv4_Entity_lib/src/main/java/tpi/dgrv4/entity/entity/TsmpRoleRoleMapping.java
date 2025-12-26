package tpi.dgrv4.entity.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

@Entity
@Table(name = "tsmp_role_role_mapping")
@TableGenerator(name = "seq_store", initialValue = 2_000_000_000)
public class TsmpRoleRoleMapping {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_store")
	@Column(name = "ROLE_ROLE_ID")
	private Long roleRoleId;
	
	@Column(name = "ROLE_NAME")
	private String roleName;

	@Column(name = "ROLE_NAME_MAPPING")
	private String roleNameMapping;

	/* constructors */

	public TsmpRoleRoleMapping() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpRoleRoleMapping [roleRoleId=" + roleRoleId + ", roleName=" + roleName + ", roleNameMapping="
				+ roleNameMapping + "]";
	}
	
	/* getters and setters */

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	
	public String getRoleName() {
		return roleName;
	}

	public String getRoleNameMapping() {
		return roleNameMapping;
	}

	public void setRoleNameMapping(String roleNameMapping) {
		this.roleNameMapping = roleNameMapping;
	}

	public void setRoleRoleId(Long roleRoleId) {
		this.roleRoleId = roleRoleId;
	}
	
	public Long getRoleRoleId() {
		return roleRoleId;
	}
	
}