package tpi.dgrv4.entity.entity.jpql;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "TSMP_SECURITY_LEVEL")
public class TsmpSecurityLevel implements Serializable {
	@Id
	@Column(name = "SECURITY_LEVEL_ID")
	private String securityLevelId;

	@Column(name = "SECURITY_LEVEL_NAME")
	private String securityLevelName;

	@Column(name = "SECURITY_LEVEL_DESC")
	private String securityLevelDesc;

	@Override
	public String toString() {
		return "TsmpSecurityLevel [securityLevelId=" + securityLevelId + ", securityLevelName=" + securityLevelName
				+ ", securityLevelDesc=" + securityLevelDesc + "]";
	}

	public String getSecurityLevelId() {
		return securityLevelId;
	}

	public void setSecurityLevelId(String securityLevelId) {
		this.securityLevelId = securityLevelId;
	}

	public String getSecurityLevelName() {
		return securityLevelName;
	}

	public void setSecurityLevelName(String securityLevelName) {
		this.securityLevelName = securityLevelName;
	}

	public String getSecurityLevelDesc() {
		return securityLevelDesc;
	}

	public void setSecurityLevelDesc(String securityLevelDesc) {
		this.securityLevelDesc = securityLevelDesc;
	}

}
