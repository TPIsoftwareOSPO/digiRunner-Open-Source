package tpi.dgrv4.common.utils.autoInitSQL.vo;

public class AuthoritiesVo{
	
	private String username;

	private String authority;

	/* constructors */

	public AuthoritiesVo() {}

	/* methods */

	@Override
	public String toString() {
		return "Authorities [username=" + username + ", authority=" + authority + "]\n";
	}

	/* getters and setters */

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getAuthority() {
		return authority;
	}

	public void setAuthority(String authority) {
		this.authority = authority;
	}
	
}
