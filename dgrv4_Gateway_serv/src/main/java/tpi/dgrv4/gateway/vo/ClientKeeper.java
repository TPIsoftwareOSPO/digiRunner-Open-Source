package tpi.dgrv4.gateway.vo;

import java.io.Serializable;

public class ClientKeeper implements Serializable {
	private String ip;
	private Integer port;
	private String username;
	public String main;
	public String deferrable;
	public String refresh;
	public String version;
	public String updateTime;
	public String upTime;
	private String startTime;
	private String serverPort;
	private String serverServletContextPath;
	private String serverSslEnalbed;
	private String springProfilesActive;
	private String keeperServerIp;
	private String keeperServerPort;
	private String rcdCacheSize;
	private String daoCacheSize;
	private String fixedCacheSize;
	private String webLocalIP;
	private String fqdn;
	private String esQueue;
	private String rdbQueue;

	private String cpu;
	private String mem;
	private String metaSpace;
	private String h_used;
	private String h_free;
	private String h_total;

	private String api_ReqThroughputSize;
	private String api_RespThroughputSize;

	private String lastUpdateTimeClient;
	private String lastUpdateTimeAPI;
	private String lastUpdateTimeSetting;
	private String lastUpdateTimeToken;

	private String dbConnect;
	private Object cusInfo;

	
	//0:dg產品 1:客製包
	public String projectType;
	public String keeperServerApi;
	public String livenessUrlPath;
	
	public String getRcdCacheSize() {
		return rcdCacheSize;
	}

	public void setRcdCacheSize(String rcdCacheSize) {
		this.rcdCacheSize = rcdCacheSize;
	}

	public void setFixedCacheSize(String fixedCacheSize) {
		this.fixedCacheSize = fixedCacheSize;
	}

	public String getFixedCacheSize() {
		return fixedCacheSize;
	}

	public String getKeeperServerIp() {
		return keeperServerIp;
	}

	public void setKeeperServerIp(String keeperServerIp) {
		this.keeperServerIp = keeperServerIp;
	}

	public void setKeeperServerPort(String keeperServerPort) {
		this.keeperServerPort = keeperServerPort;
	}

	public String getKeeperServerPort() {
		return keeperServerPort;
	}

	public String getServerPort() {
		return serverPort;
	}

	public void setServerPort(String serverPort) {
		this.serverPort = serverPort;
	}

	public void setServerServletContextPath(String serverServletContextPath) {
		this.serverServletContextPath = serverServletContextPath;
	}

	public String getServerServletContextPath() {
		return serverServletContextPath;
	}

	public String getServerSslEnalbed() {
		return serverSslEnalbed;
	}

	public void setServerSslEnalbed(String serverSslEnalbed) {
		this.serverSslEnalbed = serverSslEnalbed;
	}

	public String getSpringProfilesActive() {
		return springProfilesActive;
	}

	public void setSpringProfilesActive(String springProfilesActive) {
		this.springProfilesActive = springProfilesActive;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getMain() {
		return main;
	}

	public void setMain(String main) {
		this.main = main;
	}

	public String getDeferrable() {
		return deferrable;
	}

	public void setDeferrable(String deferrable) {
		this.deferrable = deferrable;
	}

	public String getRefresh() {
		return refresh;
	}

	public void setRefresh(String refresh) {
		this.refresh = refresh;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public String getUpTime() {
		return upTime;
	}

	public void setUpTime(String upTime) {
		this.upTime = upTime;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
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

	public void setEsQueue(String esQueue) {
		this.esQueue = esQueue;
	}

	public String getEsQueue() {
		return esQueue;
	}

	public void setRdbQueue(String rdbQueue) {
		this.rdbQueue = rdbQueue;
	}

	public String getRdbQueue() {
		return rdbQueue;
	}

	public String getCpu() {
		try {
			float c = Float.parseFloat(this.cpu);
			c = c * 100;
			return String.format("%.2f%%", c);
		} catch (Exception e) {
			return cpu;
		}
	}

	public void setCpu(String cpu) {
		this.cpu = cpu;
	}

	public String getMem() {
		return mem;
	}

	public String getMetaSpace() {
		return metaSpace;
	}

	public void setMem(String mem) {
		this.mem = mem;
	}

	public String getH_used() {
		return h_used;
	}

	public void setH_used(String h_used) {
		this.h_used = h_used;
	}

	public String getH_free() {
		return h_free;
	}

	public void setH_free(String h_free) {
		this.h_free = h_free;
	}

	public String getH_total() {
		return h_total;
	}

	public void setH_total(String h_total) {
		this.h_total = h_total;
	}

	public String getApi_ReqThroughputSize() {
		return api_ReqThroughputSize;
	}

	public void setApi_ReqThroughputSize(String api_ReqThroughputSize) {
		this.api_ReqThroughputSize = api_ReqThroughputSize;
	}

	public String getApi_RespThroughputSize() {
		return api_RespThroughputSize;
	}

	public void setApi_RespThroughputSize(String api_RespThroughputSize) {
		this.api_RespThroughputSize = api_RespThroughputSize;
	}

	public String getDaoCacheSize() {
		return daoCacheSize;
	}

	public void setDaoCacheSize(String daoCacheSize) {
		this.daoCacheSize = daoCacheSize;
	}

	public String getLastUpdateTimeClient() {
		return lastUpdateTimeClient;
	}

	public void setLastUpdateTimeClient(String lastUpdateTimeClient) {
		this.lastUpdateTimeClient = lastUpdateTimeClient;
	}

	public String getLastUpdateTimeAPI() {
		return lastUpdateTimeAPI;
	}

	public void setLastUpdateTimeAPI(String lastUpdateTimeAPI) {
		this.lastUpdateTimeAPI = lastUpdateTimeAPI;
	}

	public String getLastUpdateTimeSetting() {
		return lastUpdateTimeSetting;
	}

	public void setLastUpdateTimeSetting(String lastUpdateTimeSetting) {
		this.lastUpdateTimeSetting = lastUpdateTimeSetting;
	}

	public String getLastUpdateTimeToken() {
		return lastUpdateTimeToken;
	}

	public void setLastUpdateTimeToken(String lastUpdateTimeToken) {
		this.lastUpdateTimeToken = lastUpdateTimeToken;
		
	}
	public String getProjectType() {
		return projectType;
	}

	public void setProjectType(String projectType) {
		this.projectType = projectType;
	}

	public String getKeeperServerApi() {
		return keeperServerApi;
	}

	public void setKeeperServerApi(String keeperServerApi) {
		this.keeperServerApi = keeperServerApi;
	}

	public String getLivenessUrlPath() {
		return livenessUrlPath;
	}

	public void setLivenessUrlPath(String livenessUrlPath) {
		this.livenessUrlPath = livenessUrlPath;
	}

	@Override
	public String toString() {
		return "ClientKeeper [ip=" + ip + ", port=" + port + ", username=" + username + ", main=" + main
				+ ", deferrable=" + deferrable + ", refresh=" + refresh + ", version=" + version + ", updateTime="
				+ updateTime + ", upTime=" + upTime + ", startTime=" + startTime + ", serverPort=" + serverPort
				+ ", serverServletContextPath=" + serverServletContextPath + ", serverSslEnalbed=" + serverSslEnalbed
				+ ", springProfilesActive=" + springProfilesActive + ", keeperServerIp=" + keeperServerIp
				+ ", keeperServerPort=" + keeperServerPort + ", rcdCacheSize=" + rcdCacheSize + ", daoCacheSize="
				+ daoCacheSize + ", fixedCacheSize=" + fixedCacheSize + ", webLocalIP=" + webLocalIP + ", fqdn=" + fqdn
				+ ", esQueue=" + esQueue + ", rdbQueue=" + rdbQueue + ", cpu=" + cpu + ", mem=" + mem + ", h_used="
				+ h_used + ", h_free=" + h_free + ", h_total=" + h_total + ", api_ReqThroughputSize="
				+ api_ReqThroughputSize + ", api_RespThroughputSize=" + api_RespThroughputSize + ", lastUpdateTimeAPI="
				+ lastUpdateTimeAPI + ", lastUpdateTimeClient=" + lastUpdateTimeClient + ", lastUpdateTimeSetting="
				+ lastUpdateTimeSetting + ", lastUpdateTimeToken=" + lastUpdateTimeToken + "]";
	}

	/**
	 * @return the cusInfo
	 */
	public Object getCusInfo() {
		return cusInfo;
	}

	/**
	 * @param cusInfo the cusInfo to set
	 */
	public void setCusInfo(Object cusInfo) {
		this.cusInfo = cusInfo;
	}

	/**
	 * @return the dbConnect
	 */
	public String getDbConnect() {
		return dbConnect;
	}

	/**
	 * @param dbConnect the dbConnect to set
	 */
	public void setDbConnect(String dbConnect) {
		this.dbConnect = dbConnect;
	}

	public void setMetaSpace(String metaSpace) {
		this.metaSpace = metaSpace;
	}



}
