package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0228GroupInfo {
	
	/** 安全等級 */
	private String securityLevelName;
	
	/** 群組代號 */
	private String groupName;
	
	/** 群組流水號 */
	private String groupID;
	
	/** 群組描述 */
	private String groupDesc;
	
	/** 群組名稱 */
	private String groupAlias;
	
	/** 建立時間 */
	private String createTime;
	
	/** AA0228ModuleAPIKey */
	private List<AA0228ModuleAPIKey> moduleAPIKeyList;

	public String getSecurityLevelName() {
		return securityLevelName;
	}

	public void setSecurityLevelName(String securityLevelName) {
		this.securityLevelName = securityLevelName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	
	public String getGroupName() {
		return groupName;
	}

	public String getGroupID() {
		return groupID;
	}

	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}

	public void setGroupDesc(String groupDesc) {
		this.groupDesc = groupDesc;
	}
	
	public String getGroupDesc() {
		return groupDesc;
	}

	public String getGroupAlias() {
		return groupAlias;
	}

	public void setGroupAlias(String groupAlias) {
		this.groupAlias = groupAlias;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	
	public String getCreateTime() {
		return createTime;
	}

	public List<AA0228ModuleAPIKey> getModuleAPIKeyList() {
		return moduleAPIKeyList;
	}

	public void setModuleAPIKeyList(List<AA0228ModuleAPIKey> moduleAPIKeyList) {
		this.moduleAPIKeyList = moduleAPIKeyList;
	}

	@Override
	public String toString() {
		return "AA0228GroupInfo [securityLevelName=" + securityLevelName + ", groupName=" + groupName + ", groupID="
				+ groupID + ", groupDesc=" + groupDesc + ", groupAlias=" + groupAlias + ", createTime=" + createTime
				+ ", moduleAPIKeyList=" + moduleAPIKeyList + "]";
	}
	
	
}
