package tpi.dgrv4.gateway.vo;

import java.util.List;

public class Whitelist {
	private List<String> ips;
	private List<String> apis;
	public List<String> getIps() {
		return ips;
	}
	public void setIps(List<String> ips) {
		this.ips = ips;
	}
	public List<String> getApis() {
		return apis;
	}
	public void setApis(List<String> apis) {
		this.apis = apis;
	}
}
