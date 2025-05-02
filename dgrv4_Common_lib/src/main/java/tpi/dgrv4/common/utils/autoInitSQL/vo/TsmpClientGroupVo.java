package tpi.dgrv4.common.utils.autoInitSQL.vo;

public class TsmpClientGroupVo {

	private String clientId;

	private String groupId;

	/* constructors */

	public TsmpClientGroupVo() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpClientGroup [clientId=" + clientId + ", groupId=" + groupId + "]";
	}

	/* getters and setters */

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	
}
