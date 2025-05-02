package tpi.dgrv4.dpaa.vo;


public class UdpLdapResp {

	private String userName;
	private String codeChallenge;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getCodeChallenge() {
		return codeChallenge;
	}

	public void setCodeChallenge(String codeChallenge) {
		this.codeChallenge = codeChallenge;
	}
}
