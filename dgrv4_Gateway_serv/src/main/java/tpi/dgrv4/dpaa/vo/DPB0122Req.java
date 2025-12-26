package tpi.dgrv4.dpaa.vo;

public class DPB0122Req {

	/** User Name */
	private String username;

	/** 前端將 UUID 經過 Base64UrlEncode(SHA256(UUID)) 處理過的值 */
	private String codeChallenge;

	@Override
	public String toString() {
		return "DPB0122Req [username=" + username + ", codeChallenge=" + codeChallenge + "]";
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getCodeChallenge() {
		return codeChallenge;
	}

	public void setCodeChallenge(String codeChallenge) {
		this.codeChallenge = codeChallenge;
	}
}
