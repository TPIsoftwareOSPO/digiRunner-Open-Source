package tpi.dgrv4.dpaa.vo;

public class AA0511Resp {

	/** 授權碼 */
	private String authCode;
	
	/** Composer Path */
	private String targetPath;
	
	/** Composer Port */
	private Integer targetPort;


	public AA0511Resp() {
	}

	public void setAuthCode(String authCode) {
		this.authCode = authCode;
	}

	public Integer getTargetPort() {
		return targetPort;
	}

	public void setTargetPort(Integer targetPort) {
		this.targetPort = targetPort;
	}
	
	public String getAuthCode() {
		return authCode;
	}

	public String getTargetPath() {
		return targetPath;
	}

	public void setTargetPath(String targetPath) {
		this.targetPath = targetPath;
	}
	
}