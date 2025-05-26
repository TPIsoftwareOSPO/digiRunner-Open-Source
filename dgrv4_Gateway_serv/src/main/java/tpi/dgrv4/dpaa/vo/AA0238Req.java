package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0238Req{

	/** PK*/
	private String groupId;
	
	/** 模糊搜尋*/
	private String keyword;
	
	/** 安全等級*/
	private String securityLevelID;
	
	/** 授權核身種類*/
	private List<String> groupAuthoritiesID;

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getSecurityLevelID() {
		return securityLevelID;
	}

	public void setSecurityLevelID(String securityLevelID) {
		this.securityLevelID = securityLevelID;
	}

	public List<String> getGroupAuthoritiesID() {
		return groupAuthoritiesID;
	}

	public void setGroupAuthoritiesID(List<String> groupAuthoritiesID) {
		this.groupAuthoritiesID = groupAuthoritiesID;
	}

}
