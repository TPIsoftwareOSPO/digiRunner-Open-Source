package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0325Req {

	private String composerID;
	private Long startupTime;
	private String keeperAPI;
	private String webServerPort;
	private String version;
	private Long ts;
	private String webLocalIP;
	private List<AA0325Log> httpRequestLog;
	private List<AA0325SysLog> sysLog;
	private String cpuUsage;
	private String memoryUsage;

	
	@Override
	public String toString() {
		return "AA0325Req [composerID=" + composerID + ", startupTime=" + startupTime + ", keeperAPI=" + keeperAPI
				+ ", webServerPort=" + webServerPort + ", version=" + version + ", ts=" + ts + ", webLocalIP="
				+ webLocalIP + ", httpRequestLog=" + httpRequestLog + ", sysLog=" + sysLog + ", cpuUsage=" + cpuUsage
				+ ", memoryUsage=" + memoryUsage + "]";
	}

	public String getComposerID() {
		return composerID;
	}

	public void setComposerID(String composerID) {
		this.composerID = composerID;
	}

	public Long getStartupTime() {
		return startupTime;
	}

	public void setStartupTime(Long startupTime) {
		this.startupTime = startupTime;
	}

	public void setWebServerPort(String webServerPort) {
		this.webServerPort = webServerPort;
	}

	public String getWebServerPort() {
		return webServerPort;
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

	public List<AA0325Log> getHttpRequestLog() {
		return httpRequestLog;
	}

	public void setHttpRequestLog(List<AA0325Log> httpRequestLog) {
		this.httpRequestLog = httpRequestLog;
	}

	public List<AA0325SysLog> getSysLog() {
		return sysLog;
	}

	public void setSysLog(List<AA0325SysLog> sysLog) {
		this.sysLog = sysLog;
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
