package tpi.dgrv4.entity.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "tsmp_role_func")
@IdClass(value = TsmpRoleFuncId.class)
public class TsmpRoleFunc {

	@Id
	@Column(name = "ROLE_ID")
	private String roleId;

	@Id
	@Column(name = "FUNC_CODE")
	private String funcCode;

	/* constructors */

	public TsmpRoleFunc() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpRoleFunc [roleId=" + roleId + ", funcCode=" + funcCode + "]\n";
	}
	
	/* getters and setters */
	
	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public String getFuncCode() {
		return funcCode;
	}

	public void setFuncCode(String funcCode) {
		this.funcCode = funcCode;
	}

}
