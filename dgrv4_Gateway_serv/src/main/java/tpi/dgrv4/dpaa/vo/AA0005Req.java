package tpi.dgrv4.dpaa.vo;

public class AA0005Req {

	/** 使用者編號 */
	private String userID;

	/** 使用者帳號 */
	private String userName;

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Override
	public String toString() {
		return "AA0005Req [userID=" + userID + ", userName=" + userName + "]";
	}
	
	
	

}
