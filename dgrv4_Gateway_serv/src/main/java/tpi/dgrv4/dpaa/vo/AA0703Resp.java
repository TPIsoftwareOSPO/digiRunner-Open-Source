package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0703Resp{

	/** 告警編號*/
	private String alertId;
	
	/** 告警說明*/
	private String alertDesc;
	
	/** 狀態*/
	private String alertEnabled;
	
	/** 狀態代碼*/
	private String alertEnabledName;
	
	/** Alert Interval 告警間隔(sec)*/
	private Integer alertInterval;
	
	/** 告警訊息*/
	private String alertMsg;
	
	/** 告警系統*/
	private String alertSys;
	
	/** 告警類型*/
	private String alertType;
	
	/** 客製化告警*/
	private String cFlag;
	
	/** Duration 問題持續時間(sec)*/
	private Integer duration;
	
	/** 告警名稱*/
	private String alertName;
	
	/** 例外日期*/
	private String exDays;
	
	/** 開始時間 + 結束時間*/
	private String exTime;
	
	/** Line 告警*/
	private String imFlag;
	
	/** Line Token*/
	private String imId;

	private String imType;
	
	/** 告警角色清單*/
	private List<AA0703RoleInfo> roleInfoList;
	
	/** 例外類型*/
	private String exType;
	
	/** Keyword與API共用*/
	private String searchMapString;
	
	/** Threshold 門檻 (次數或%)*/
	private Integer threshold;

	public void setAlertId(String alertId) {
		this.alertId = alertId;
	}

	public String getAlertDesc() {
		return alertDesc;
	}

	public void setAlertDesc(String alertDesc) {
		this.alertDesc = alertDesc;
	}

	public String getAlertEnabled() {
		return alertEnabled;
	}
	
	public String getAlertId() {
		return alertId;
	}

	public Integer getAlertInterval() {
		return alertInterval;
	}

	public String getAlertMsg() {
		return alertMsg;
	}

	public void setAlertMsg(String alertMsg) {
		this.alertMsg = alertMsg;
	}
	
	public void setAlertInterval(Integer alertInterval) {
		this.alertInterval = alertInterval;
	}

	public String getAlertName() {
		return alertName;
	}

	public void setAlertName(String alertName) {
		this.alertName = alertName;
	}
	
	public void setAlertEnabled(String alertEnabled) {
		this.alertEnabled = alertEnabled;
	}


	public String getAlertSys() {
		return alertSys;
	}

	public void setAlertSys(String alertSys) {
		this.alertSys = alertSys;
	}

	public void setAlertType(String alertType) {
		this.alertType = alertType;
	}

	public String getcFlag() {
		return cFlag;
	}
	
	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}
	
	public void setcFlag(String cFlag) {
		this.cFlag = cFlag;
	}
	
	public String getAlertType() {
		return alertType;
	}

	public String getExDays() {
		return exDays;
	}

	public void setExDays(String exDays) {
		this.exDays = exDays;
	}

	public String getExTime() {
		return exTime;
	}

	public void setExTime(String exTime) {
		this.exTime = exTime;
	}

	public void setExType(String exType) {
		this.exType = exType;
	}

	public String getImFlag() {
		return imFlag;
	}

	public void setImFlag(String imFlag) {
		this.imFlag = imFlag;
	}

	public String getImId() {
		return imId;
	}

	public void setImId(String imId) {
		this.imId = imId;
	}

	public String getImType() {
		return imType;
	}
	
	public String getExType() {
		return exType;
	}

	public void setImType(String imType) {
		this.imType = imType;
	}

	public List<AA0703RoleInfo> getRoleInfoList() {
		return roleInfoList;
	}

	public void setRoleInfoList(List<AA0703RoleInfo> roleInfoList) {
		this.roleInfoList = roleInfoList;
	}

	public void setSearchMapString(String searchMapString) {
		this.searchMapString = searchMapString;
	}

	public Integer getThreshold() {
		return threshold;
	}

	public void setThreshold(Integer threshold) {
		this.threshold = threshold;
	}
	
	public String getSearchMapString() {
		return searchMapString;
	}

	public String getAlertEnabledName() {
		return alertEnabledName;
	}

	public void setAlertEnabledName(String alertEnabledName) {
		this.alertEnabledName = alertEnabledName;
	}
	

}
