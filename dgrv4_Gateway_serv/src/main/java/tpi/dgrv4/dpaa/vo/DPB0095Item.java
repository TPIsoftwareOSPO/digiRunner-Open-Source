package tpi.dgrv4.dpaa.vo;

public class DPB0095Item {
	/** 用戶端帳號 */
	private String clientId;

	/** 用戶端代號 */
	private String clientName;

	/** 用戶端名稱 */
	private String clientAlias;

	/** 電子郵件帳號 */
	private String emails;

	/** 用戶端狀態 */
	private String clientStatus;

	@Override
	public String toString() {
		return "DPB0095RespItem [clientId=" + clientId + ", clientName=" + clientName + ", clientAlias=" + clientAlias
				+ ", emails=" + emails + ", clientStatus=" + clientStatus + "]";
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientName() {
		return clientName;
	}
	
	public void setClientStatus(String clientStatus) {
		this.clientStatus = clientStatus;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public void setClientAlias(String clientAlias) {
		this.clientAlias = clientAlias;
	}
	
	public String getClientAlias() {
		return clientAlias;
	}

	public String getEmails() {
		return emails;
	}

	public void setEmails(String emails) {
		this.emails = emails;
	}

	public String getClientStatus() {
		return clientStatus;
	}

}
