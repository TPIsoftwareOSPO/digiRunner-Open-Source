package tpi.dgrv4.dpaa.vo;


public class UdpEnvItem {
	/* 可登入環境名稱 */
	private String envName;
	
	/* 可登入環境URL */
	private String envUrl;

	@Override
	public String toString() {
		return "UdpEnvItem [envName=" + envName + ", envUrl=" + envUrl + "]\n";
	}

	public String getEnvName() {
		return envName;
	}

	public void setEnvName(String envName) {
		this.envName = envName;
	}

	public String getEnvUrl() {
		return envUrl;
	}

	public void setEnvUrl(String envUrl) {
		this.envUrl = envUrl;
	}
}
