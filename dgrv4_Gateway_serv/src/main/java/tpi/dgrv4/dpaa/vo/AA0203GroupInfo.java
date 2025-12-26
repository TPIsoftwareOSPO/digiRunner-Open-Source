package tpi.dgrv4.dpaa.vo;

public class AA0203GroupInfo {

	/**群組名稱*/
	private String groupAlias;
	
	/**群組描述*/
	private String groupDesc;
	
	/**群組代碼*/
	private String groupID;
	
	/**群組代號*/
	private String groupName;
	
	/**安全等級ID*/
	private String securityLevelID;
	
	/**安全等級名稱*/
	private String securityLevelName;

	public String getGroupAlias() {
		return groupAlias;
	}

	public void setGroupAlias(String groupAlias) {
		this.groupAlias = groupAlias;
	}

	public String getGroupDesc() {
		return groupDesc;
	}

	public void setGroupDesc(String groupDesc) {
		this.groupDesc = groupDesc;
	}
	
	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}

	public String getGroupID() {
		return groupID;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

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
	
	
}
