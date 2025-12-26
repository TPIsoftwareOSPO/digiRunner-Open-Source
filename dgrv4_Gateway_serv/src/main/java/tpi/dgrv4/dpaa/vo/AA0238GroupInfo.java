package tpi.dgrv4.dpaa.vo;

public class AA0238GroupInfo {

	/** 安全等級*/
	private String securityLevelName;
	
	/** 群組代號*/
	private String groupName;
	
	/** 群組流水號*/
	private String groupID;
	
	/** 群組描述*/
	private String groupDesc;
	
	/** 群組名稱*/
	private String groupAlias;
	
	/** 建立時間*/
	private String createTime;
	
	/** 授權核身種類*/
	private AA0238GroupAuthoritiesInfo groupAuthorities;

	public String getSecurityLevelName() {
		return securityLevelName;
	}

	public void setSecurityLevelName(String securityLevelName) {
		this.securityLevelName = securityLevelName;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getGroupID() {
		return groupID;
	}

	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}

	public String getGroupDesc() {
		return groupDesc;
	}

	public void setGroupDesc(String groupDesc) {
		this.groupDesc = groupDesc;
	}

	public String getGroupAlias() {
		return groupAlias;
	}

	public void setGroupAlias(String groupAlias) {
		this.groupAlias = groupAlias;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public AA0238GroupAuthoritiesInfo getGroupAuthorities() {
		return groupAuthorities;
	}

	public void setGroupAuthorities(AA0238GroupAuthoritiesInfo groupAuthorities) {
		this.groupAuthorities = groupAuthorities;
	}
	
	
}
