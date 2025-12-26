package tpi.dgrv4.common.utils.autoInitSQL.vo;

public class UsersVo {

	private String userName;

	private String password;

	private int userStatus = 1;

	/* constructors */
	public UsersVo() {}

	/* getters and setters */

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getUserStatus() {
		return userStatus;
	}

	public void setUserStatus(int userStatus) {
		this.userStatus = userStatus;
	}

	@Override
	public String toString() {
		return "Users [userName=" + userName + ", password=" + password + ", userStatus=" + userStatus + "]";
	}


}
