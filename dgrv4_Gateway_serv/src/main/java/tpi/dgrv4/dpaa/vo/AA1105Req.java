package tpi.dgrv4.dpaa.vo;

public class AA1105Req{

	/** 安全等級名稱 */
	private String securityLevelName;
	
	/** 安全等級ID */
	private String securityLevelId;

	public void setSecurityLevelId(String securityLevelId) {
		this.securityLevelId = securityLevelId;
	}

	public String getSecurityLevelName() {
		return securityLevelName;
	}

	public void setSecurityLevelName(String securityLevelName) {
		this.securityLevelName = securityLevelName;
	}
	
	public String getSecurityLevelId() {
		return securityLevelId;
	}
	

}
