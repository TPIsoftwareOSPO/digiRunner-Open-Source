package tpi.dgrv4.dpaa.vo;

public class AA0203VgroupInfo {

	/**安全等級ID*/
	private String securityLevelID;
	
	/**安全等級名稱*/
	private String securityLevelName;
	
	/**分類名稱*/
	private String vgroupAlias;
	
	/**分類描述*/
	private String vgroupDesc;
	
	/**分類代碼*/
	private String vgroupID;
	
	/**分類代號*/
	private String vgroupName;

	public String getSecurityLevelID() {
		return securityLevelID;
	}

	public void setSecurityLevelID(String securityLevelID) {
		this.securityLevelID = securityLevelID;
	}

	public String getSecurityLevelName() {
		return securityLevelName;
	}

	public void setSecurityLevelName(String securityLevelName) {
		this.securityLevelName = securityLevelName;
	}

	public void setVgroupAlias(String vgroupAlias) {
		this.vgroupAlias = vgroupAlias;
	}
	
	public String getVgroupAlias() {
		return vgroupAlias;
	}

	public String getVgroupDesc() {
		return vgroupDesc;
	}

	public void setVgroupDesc(String vgroupDesc) {
		this.vgroupDesc = vgroupDesc;
	}

	public void setVgroupID(String vgroupID) {
		this.vgroupID = vgroupID;
	}
	
	public String getVgroupID() {
		return vgroupID;
	}

	public String getVgroupName() {
		return vgroupName;
	}

	public void setVgroupName(String vgroupName) {
		this.vgroupName = vgroupName;
	}
	
}
