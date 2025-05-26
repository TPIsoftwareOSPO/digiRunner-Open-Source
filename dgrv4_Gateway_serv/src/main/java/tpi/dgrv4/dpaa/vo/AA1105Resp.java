package tpi.dgrv4.dpaa.vo;

public class AA1105Resp {

	/**安全等級名稱*/
	private String securityLevelName;
	
	/**安全等級描述*/
	private String securityLevelDesc;
	
	/**安全等級ID*/
	private String securityLevelId;

	public String getSecurityLevelId() {
		return securityLevelId;
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
	
	public void setSecurityLevelId(String securityLevelId) {
		this.securityLevelId = securityLevelId;
	}

	public void setSecurityLevelDesc(String securityLevelDesc) {
		this.securityLevelDesc = securityLevelDesc;
	}

	
}
