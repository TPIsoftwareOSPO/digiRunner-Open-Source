package tpi.dgrv4.dpaa.vo;

public class DPB0142Resp {

	/** 授權碼(JWE String) */
	private String authCode;

	/** Composer Port */
	private Integer targetPort;

	/** Composer Path */
	private String targetPath;
	
	private String apiUid;

	public DPB0142Resp() {
	}

	public String getAuthCode() {
		return authCode;
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

	public String getTargetPath() {
		return targetPath;
	}

	public void setTargetPath(String targetPath) {
		this.targetPath = targetPath;
	}

	public String getApiUid() {
		return apiUid;
	}

	public void setApiUid(String apiUid) {
		this.apiUid = apiUid;
	}
	
}