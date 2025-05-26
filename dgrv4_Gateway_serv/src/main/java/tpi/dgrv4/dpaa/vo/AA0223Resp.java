package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0223Resp {
	
	/** 虛擬群組ID*/
	private String vgroupId;
	
	/** 虛擬群組代碼*/
	private String vgroupName;
	
	/** 虛擬群組名稱*/
	private String vgroupAlias;
	
	/** 允許使用時間*/
	private Integer allowDays;
	
	/** 允許使用時間代碼(單位)*/
	private String timeUnit;
	
	/** 允許使用時間名稱(單位)*/
	private String timeUnitName;
	
	/** 粗略時間*/
	private String approximateTimeUnit;
	
	/** 授權次數上限*/
	private Integer allowTimes;
	
	/** 授權核身種類*/
	private List<AA0223Auth> vgroupAuthorities;
	
	/** 安全等級代碼*/
	private String securityLevelId;
	
	/** 安全等級名稱*/
	private String securityLevelName;
	
	/** 虛擬群組描述*/
	private String vgroupDesc;
	
	/** 建立日期*/
	private String createDate;
	
	/** 建立人員*/
	private String createUser;
	
	/** 更新日期*/
	private String updateDate;
	
	/** 更新人員*/
	private String updateUser;

	public String getVgroupId() {
		return vgroupId;
	}

	public void setVgroupId(String vgroupId) {
		this.vgroupId = vgroupId;
	}

	public String getVgroupName() {
		return vgroupName;
	}

	public void setVgroupName(String vgroupName) {
		this.vgroupName = vgroupName;
	}

	public String getVgroupAlias() {
		return vgroupAlias;
	}

	public void setVgroupAlias(String vgroupAlias) {
		this.vgroupAlias = vgroupAlias;
	}

	public Integer getAllowDays() {
		return allowDays;
	}

	public void setAllowDays(Integer allowDays) {
		this.allowDays = allowDays;
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

	public Integer getAllowTimes() {
		return allowTimes;
	}

	public void setAllowTimes(Integer allowTimes) {
		this.allowTimes = allowTimes;
	}

	public List<AA0223Auth> getVgroupAuthorities() {
		return vgroupAuthorities;
	}

	public void setVgroupAuthorities(List<AA0223Auth> vgroupAuthorities) {
		this.vgroupAuthorities = vgroupAuthorities;
	}

	public String getSecurityLevelId() {
		return securityLevelId;
	}

	public void setSecurityLevelId(String securityLevelId) {
		this.securityLevelId = securityLevelId;
	}

	public String getSecurityLevelName() {
		return securityLevelName;
	}

	public void setSecurityLevelName(String securityLevelName) {
		this.securityLevelName = securityLevelName;
	}

	public String getVgroupDesc() {
		return vgroupDesc;
	}

	public void setVgroupDesc(String vgroupDesc) {
		this.vgroupDesc = vgroupDesc;
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

	@Override
	public String toString() {
		return "AA0223Resp [vgroupId=" + vgroupId + ", vgroupName=" + vgroupName + ", vgroupAlias=" + vgroupAlias
				+ ", allowDays=" + allowDays + ", timeUnit=" + timeUnit + ", timeUnitName=" + timeUnitName
				+ ", approximateTimeUnit=" + approximateTimeUnit + ", allowTimes=" + allowTimes + ", vgroupAuthorities="
				+ vgroupAuthorities + ", securityLevelId=" + securityLevelId + ", securityLevelName="
				+ securityLevelName + ", vgroupDesc=" + vgroupDesc + ", createDate=" + createDate + ", createUser="
				+ createUser + ", updateDate=" + updateDate + ", updateUser=" + updateUser + "]";
	}

}
