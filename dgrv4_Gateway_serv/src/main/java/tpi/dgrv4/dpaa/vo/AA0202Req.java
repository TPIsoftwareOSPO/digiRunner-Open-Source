package tpi.dgrv4.dpaa.vo;
	
public class AA0202Req {

	// PK
	private String clientId;

	// 模糊搜尋
	private String keyword;

	// 群組
	private String groupID;

	// 狀態，使用BcryptParam
	private String encodeStatus;

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getGroupID() {
		return groupID;
	}

	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}

	public String getEncodeStatus() {
		return encodeStatus;
	}

	public void setEncodeStatus(String encodeStatus) {
		this.encodeStatus = encodeStatus;
	}

}
