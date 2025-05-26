package tpi.dgrv4.gateway.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class OCInReq {
	
	private OCInMetadata metadata;
	
	@JsonInclude(value = Include.NON_NULL)
	private OCInMetrics metrics;
	
	@JsonInclude(value = Include.NON_NULL)
	private List<OCInLogs> logs;
	
	private String webLocalIP;
	
	private String serverPort;
	private String dbConnect;
	private String cusInfo;
	public OCInMetadata getMetadata() {
		return metadata;
	}
	public void setMetadata(OCInMetadata metadata) {
		this.metadata = metadata;
	}
	public OCInMetrics getMetrics() {
		return metrics;
	}
	public void setMetrics(OCInMetrics metrics) {
		this.metrics = metrics;
	}
	public List<OCInLogs> getLogs() {
		return logs;
	}
	public void setLogs(List<OCInLogs> logs) {
		this.logs = logs;
	}
	public String getWebLocalIP() {
		return webLocalIP;
	}
	public void setWebLocalIP(String webLocalIP) {
		this.webLocalIP = webLocalIP;
	}
	public String getServerPort() {
		return serverPort;
	}
	public void setServerPort(String serverPort) {
		this.serverPort = serverPort;
	}
	public String getDbConnect() {
		return dbConnect;
	}
	public void setDbConnect(String dbConnect) {
		this.dbConnect = dbConnect;
	}
	public String getCusInfo() {
		return cusInfo;
	}
	public void setCusInfo(String cusInfo) {
		this.cusInfo = cusInfo;
	}

}
