package tpi.dgrv4.dpaa.vo;

public class DPB0207GroupItem {
	private String groupId;

	private String groupName;

	private String groupAlias;

	private String groupDesc;

	@Override
	public String toString() {
		return "DPB0207GroupItem [groupId=" + groupId + ", groupName=" + groupName + ", groupAlias=" + groupAlias
				+ ", groupDesc=" + groupDesc + "]";
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

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
}
