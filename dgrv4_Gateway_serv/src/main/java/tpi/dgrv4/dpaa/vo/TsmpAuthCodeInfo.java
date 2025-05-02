package tpi.dgrv4.dpaa.vo;

import java.util.Date;

public class TsmpAuthCodeInfo {

	private String type;
	
	private String proxy_target;
	
	private String proxy_login_path;
	
	private String proxy_redirect_path;
	
	private String user;
	
	private String client;
	
	private Date expiredTime;

	public TsmpAuthCodeInfo() {
	}

	@Override
	public String toString() {
		return "TsmpAuthCodeInfo [type=" + type + ", proxy_target=" + proxy_target + ", proxy_login_path="
				+ proxy_login_path + ", proxy_redirect_path=" + proxy_redirect_path + ", user=" + user + ", client="
				+ client + ", expiredTime=" + expiredTime + "]";
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getProxy_target() {
		return proxy_target;
	}

	public void setProxy_target(String proxy_target) {
		this.proxy_target = proxy_target;
	}

	public String getProxy_login_path() {
		return proxy_login_path;
	}

	public void setProxy_login_path(String proxy_login_path) {
		this.proxy_login_path = proxy_login_path;
	}

	public String getProxy_redirect_path() {
		return proxy_redirect_path;
	}

	public void setProxy_redirect_path(String proxy_redirect_path) {
		this.proxy_redirect_path = proxy_redirect_path;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public Date getExpiredTime() {
		return expiredTime;
	}

	public void setExpiredTime(Date expiredTime) {
		this.expiredTime = expiredTime;
	}
}