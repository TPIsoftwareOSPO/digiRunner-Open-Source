package tpi.dgrv4.dpaa.vo;

public class DPB0123CgRespBody extends CgRespBody {

	private String login;

	@Override
	public String toString() {
		return "DPB0123CgRespBody [login=" + login + "]";
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}
}
