package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0213Resp {

	// 允許使用時間
	private Integer allowAccessDays;
	
	/** 允許使用時間代碼(單位)*/
	private String timeUnit;
	
	/** 允許使用時間名稱(單位)*/
	private String timeUnitName;
	
	/** 粗略時間*/
	private String approximateTimeUnit;

	// 授權次數上限
	private Integer allowAccessUseTimes;

	// 建立日期
	private String createDate;

	// 建立人員
	private String createUser;

	// 允許存取方式
	private List<String> groupAccessList;

	// 群組名稱
	private String groupAlias;

	// 授權核身種類
	private List<AA0213GroupAuthorities> groupAuthorities;

	// 群組描述
	private String groupDesc;

	// 群組編號
	private String groupID;

	// 群組代碼
	private String groupName;

	// 安全等級
	private AA0213SecurityLevel securityLevel;

	// 更新日期
	private String updateDate;

	// 更新人員
	private String updateUser;

	public Integer getAllowAccessDays() {
		return allowAccessDays;
	}

	public void setAllowAccessDays(Integer allowAccessDays) {
		this.allowAccessDays = allowAccessDays;
	}

	public Integer getAllowAccessUseTimes() {
		return allowAccessUseTimes;
	}

	public void setAllowAccessUseTimes(Integer allowAccessUseTimes) {
		this.allowAccessUseTimes = allowAccessUseTimes;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public List<String> getGroupAccessList() {
		return groupAccessList;
	}

	public void setGroupAccessList(List<String> groupAccessList) {
		this.groupAccessList = groupAccessList;
	}

	public String getGroupAlias() {
		return groupAlias;
	}

	public void setGroupAlias(String groupAlias) {
		this.groupAlias = groupAlias;
	}

	public List<AA0213GroupAuthorities> getGroupAuthorities() {
		return groupAuthorities;
	}

	public void setGroupAuthorities(List<AA0213GroupAuthorities> groupAuthorities) {
		this.groupAuthorities = groupAuthorities;
	}

	public String getGroupDesc() {
		return groupDesc;
	}

	public void setGroupDesc(String groupDesc) {
		this.groupDesc = groupDesc;
	}

	public String getGroupID() {
		return groupID;
	}

	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public AA0213SecurityLevel getSecurityLevel() {
		return securityLevel;
	}

	public void setSecurityLevel(AA0213SecurityLevel securityLevel) {
		this.securityLevel = securityLevel;
	}

	public String getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public String getTimeUnit() {
		return timeUnit;
	}

	public void setTimeUnit(String timeUnit) {
		this.timeUnit = timeUnit;
	}

	public void setTimeUnitName(String timeUnitName) {
		this.timeUnitName = timeUnitName;
	}
	
	public String getTimeUnitName() {
		return timeUnitName;
	}

	public String getApproximateTimeUnit() {
		return approximateTimeUnit;
	}

	public void setApproximateTimeUnit(String approximateTimeUnit) {
		this.approximateTimeUnit = approximateTimeUnit;
	}

	
}
