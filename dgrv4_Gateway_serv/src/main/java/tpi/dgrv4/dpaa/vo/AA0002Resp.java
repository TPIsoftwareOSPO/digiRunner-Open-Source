package tpi.dgrv4.dpaa.vo;

public class AA0002Resp {
	private String userID;
	private String userAlias;
	private String idTokenJwtstr;
	private boolean firstTimeLogin;

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getUserAlias() {
		return userAlias;
	}

	public void setUserAlias(String userAlias) {
		this.userAlias = userAlias;
	}

	public String getIdTokenJwtstr() {
		return idTokenJwtstr;
	}

	public void setIdTokenJwtstr(String idTokenJwtstr) {
		this.idTokenJwtstr = idTokenJwtstr;
	}

	public boolean isFirstTimeLogin() {
		return firstTimeLogin;
	}

	public void setFirstTimeLogin(boolean firstTimeLogin) {
		this.firstTimeLogin = firstTimeLogin;
	}
	
	

}
