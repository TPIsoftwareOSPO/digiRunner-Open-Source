package tpi.dgrv4.dpaa.vo;

public class AA1102SecurityLevel {

	// 安全等級ID
	private String securityLevelId;
	
	// 安全等級描述
	private String securityLevelDesc;

	// 安全等級名稱
	private String securityLevelName;

	public String getSecurityLevelId() {
		return securityLevelId;
	}

	public void setSecurityLevelId(String securityLevelId) {
		this.securityLevelId = securityLevelId;
	}

	public String getSecurityLevelName() {
		return securityLevelName;
	}

	public void setSecurityLevelDesc(String securityLevelDesc) {
		this.securityLevelDesc = securityLevelDesc;
	}
	
	public String getSecurityLevelDesc() {
		return securityLevelDesc;
	}
	
	public void setSecurityLevelName(String securityLevelName) {
		this.securityLevelName = securityLevelName;
	}

}
