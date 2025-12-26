package tpi.dgrv4.dpaa.vo;

public class DPB0144RespLostNode {
	private String nodeName;
	private String ip;
	private Integer port;
	private String lostTime;
	
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
	public String getIp() {
		return ip;
	}
	public String getNodeName() {
		return nodeName;
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
	public void setLostTime(String lostTime) {
		this.lostTime = lostTime;
	}
	public String getLostTime() {
		return lostTime;
	}
	
}
