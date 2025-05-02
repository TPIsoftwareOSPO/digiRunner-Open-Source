package tpi.dgrv4.gateway.vo;

public class ComposerInfoData implements java.io.Serializable {

	private String remoteIP;
	private Integer remotePort;
	private String composerID;
	private String startupTime;
	private String keeperAPI;
	private String webServerPort;
	private String version;
	private Long ts;
	private String tsToString;
	private String upTime;
	private String webLocalIP;
	private String cpuUsage;
	private String memoryUsage; 
	
	

	@Override
	public String toString() {
		return "ComposerInfoData [remoteIP=" + remoteIP + ", remotePort=" + remotePort + ", composerID=" + composerID
				+ ", startupTime=" + startupTime + ", keeperAPI=" + keeperAPI + ", webServerPort=" + webServerPort
				+ ", version=" + version + ", ts=" + ts + ", tsToString=" + tsToString + ", upTime=" + upTime
				+ ", webLocalIP=" + webLocalIP + ", cpuUsage=" + cpuUsage + ", memoryUsage=" + memoryUsage + "]";
	}

	public String getRemoteIP() {
		return remoteIP;
	}

	public void setRemoteIP(String remoteIP) {
		this.remoteIP = remoteIP;
	}

	public String getComposerID() {
		return composerID;
	}

	public void setComposerID(String composerID) {
		this.composerID = composerID;
	}

	public String getStartupTime() {
		return startupTime;
	}

	public void setStartupTime(String startupTime) {
		this.startupTime = startupTime;
	}

	public String getWebServerPort() {
		return webServerPort;
	}

	public void setWebServerPort(String webServerPort) {
		this.webServerPort = webServerPort;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Long getTs() {
		return ts;
	}

	public void setTs(Long ts) {
		this.ts = ts;
	}

	public String getUpTime() {
		return upTime;
	}

	public void setUpTime(String upTime) {
		this.upTime = upTime;
	}

	public String getTsToString() {
		return tsToString;
	}

	public void setTsToString(String tsToString) {
		this.tsToString = tsToString;
	}

	public Integer getRemotePort() {
		return remotePort;
	}

	public void setRemotePort(Integer remotePort) {
		this.remotePort = remotePort;
	}

	public String getKeeperAPI() {
		return keeperAPI;
	}

	public void setKeeperAPI(String keeperAPI) {
		this.keeperAPI = keeperAPI;
	}

	public String getWebLocalIP() {
		return webLocalIP;
	}

	public void setWebLocalIP(String webLocalIP) {
		this.webLocalIP = webLocalIP;
	}

	public String getCpuUsage() {
		return cpuUsage;
	}

	public void setCpuUsage(String cpuUsage) {
		this.cpuUsage = cpuUsage;
	}

	public String getMemoryUsage() {
		return memoryUsage;
	}

	public void setMemoryUsage(String memoryUsage) {
		this.memoryUsage = memoryUsage;
	}
	

}
