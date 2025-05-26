package tpi.dgrv4.dpaa.vo;

public class LdapResp {

	private String codeChallenge;
	private String userName;

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getCodeChallenge() {
		return codeChallenge;
	}

	public void setCodeChallenge(String codeChallenge) {
		this.codeChallenge = codeChallenge;
	}
	
	public String getUserName() {
		return userName;
	}
	
}
