package tpi.dgrv4.dpaa.vo;

public class AA0003Req {

	/** 使用者帳號 */
	private String userName;
	
	/** 使用者編號 */
	private String userID;

	public String getUserID() {
		return userID;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}
	
	@Override
	public String toString() {
		return "AA0003Req [userID=" + userID + ", userName=" + userName + "]";
	}

}
