package tpi.dgrv4.dpaa.vo;

public class AA0806HostInfo {
	
	/** 用戶端帳號*/
	private String clientID;
	
	/** 啟用心跳*/
	private String enabled;
	
	/** 備註*/
	private String memo;
	
	/** 主機名稱*/
	private String regHost;
	
	/** 註冊主機序號*/
	private String regHostID;
	
	/** 主機狀態*/
	private String regHostStatus;
	
	/** 心跳時間*/
	private String heartbeatTime;
	
	/** 啟用心代碼*/
	private String enabledName;

	/** 主機狀態代碼*/
	private String regHostStatusName;
	
	/** 燈號 */
	private String bulb;

	public String getClientID() {
		return clientID;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public String getEnabled() {
		return enabled;
	}

	public void setEnabled(String enabled) {
		this.enabled = enabled;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public String getRegHost() {
		return regHost;
	}

	public void setRegHost(String regHost) {
		this.regHost = regHost;
	}

	public String getRegHostID() {
		return regHostID;
	}

	public void setRegHostID(String regHostID) {
		this.regHostID = regHostID;
	}

	public String getRegHostStatus() {
		return regHostStatus;
	}

	public void setRegHostStatus(String regHostStatus) {
		this.regHostStatus = regHostStatus;
	}

	public String getHeartbeatTime() {
		return heartbeatTime;
	}

	public void setHeartbeatTime(String heartbeatTime) {
		this.heartbeatTime = heartbeatTime;
	}

	public String getEnabledName() {
		return enabledName;
	}

	public void setEnabledName(String enabledName) {
		this.enabledName = enabledName;
	}

	public String getRegHostStatusName() {
		return regHostStatusName;
	}

	public void setRegHostStatusName(String regHostStatusName) {
		this.regHostStatusName = regHostStatusName;
	}

	public String getBulb() {
		return bulb;
	}

	public void setBulb(String bulb) {
		this.bulb = bulb;
	}

	@Override
	public String toString() {
		return "AA0806HostInfo [clientID=" + clientID + ", enabled=" + enabled + ", memo=" + memo + ", regHost="
				+ regHost + ", regHostID=" + regHostID + ", regHostStatus=" + regHostStatus + ", heartbeatTime="
				+ heartbeatTime + ", enabledName=" + enabledName + ", regHostStatusName=" + regHostStatusName
				+ ", bulb=" + bulb + "]";
	}
	
}
