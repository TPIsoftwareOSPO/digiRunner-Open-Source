package tpi.dgrv4.dpaa.vo;

public class DPB0144RespLiveNode {
	private String nodeName;
	private String ip;
	private Integer port;
	private String startupTime;
	private String version;
	private String rcdCacheSize;
	private String daoCacheSize;
	private String fixedCacheSize;
	private String keeperServerIp;
	private String keeperServerPort;
	private String serverPort;
	private String serverServletContextPath;
	private String serverSslEnabled;
	private String springProfilesActive;
	private String webLocalIP;	
	private String fqdn;
	
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getRcdCacheSize() {
		return rcdCacheSize;
	}
	public void setRcdCacheSize(String rcdCacheSize) {
		this.rcdCacheSize = rcdCacheSize;
	}
	public String getFixedCacheSize() {
		return fixedCacheSize;
	}
	public void setFixedCacheSize(String fixedCacheSize) {
		this.fixedCacheSize = fixedCacheSize;
	}
	public String getKeeperServerIp() {
		return keeperServerIp;
	}
	public void setKeeperServerIp(String keeperServerIp) {
		this.keeperServerIp = keeperServerIp;
	}
	public String getKeeperServerPort() {
		return keeperServerPort;
	}
	public void setKeeperServerPort(String keeperServerPort) {
		this.keeperServerPort = keeperServerPort;
	}
	public String getServerPort() {
		return serverPort;
	}
	public void setServerPort(String serverPort) {
		this.serverPort = serverPort;
	}
	public String getServerServletContextPath() {
		return serverServletContextPath;
	}
	public void setServerServletContextPath(String serverServletContextPath) {
		this.serverServletContextPath = serverServletContextPath;
	}
	public String getServerSslEnabled() {
		return serverSslEnabled;
	}
	public void setServerSslEnabled(String serverSslEnabled) {
		this.serverSslEnabled = serverSslEnabled;
	}
	public String getSpringProfilesActive() {
		return springProfilesActive;
	}
	public void setSpringProfilesActive(String springProfilesActive) {
		this.springProfilesActive = springProfilesActive;
	}
	public String getNodeName() {
		return nodeName;
	}
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getIp() {
		return ip;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public String getStartupTime() {
		return startupTime;
	}
	public void setStartupTime(String startupTime) {
		this.startupTime = startupTime;
	}
	public String getWebLocalIP() {
		return webLocalIP;
	}
	public void setWebLocalIP(String webLocalIP) {
		this.webLocalIP = webLocalIP;
	}
	public String getFqdn() {
		return fqdn;
	}
	public void setFqdn(String fqdn) {
		this.fqdn = fqdn;
	}
	public String getDaoCacheSize() {
		return daoCacheSize;
	}
	public void setDaoCacheSize(String daoCacheSize) {
		this.daoCacheSize = daoCacheSize;
	}
	
	
}
