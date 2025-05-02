package tpi.dgrv4.dpaa.vo;

public class AA0706AlertSetting {

	/** 狀態*/
	private String alertEnabled;
	
	/** 告警編號*/
	private String alertID;
	
	/** 告警名稱*/
	private String alertName;
	
	/** 告警系統*/
	private String alertSys;
	
	/** 告警類型*/
	private String alertType;

	public String getAlertEnabled() {
		return alertEnabled;
	}

	public void setAlertEnabled(String alertEnabled) {
		this.alertEnabled = alertEnabled;
	}

	public String getAlertID() {
		return alertID;
	}

	public void setAlertID(String alertID) {
		this.alertID = alertID;
	}

	public String getAlertName() {
		return alertName;
	}

	public void setAlertName(String alertName) {
		this.alertName = alertName;
	}

	public String getAlertSys() {
		return alertSys;
	}

	public void setAlertSys(String alertSys) {
		this.alertSys = alertSys;
	}

	public String getAlertType() {
		return alertType;
	}

	public void setAlertType(String alertType) {
		this.alertType = alertType;
	}
	
}
