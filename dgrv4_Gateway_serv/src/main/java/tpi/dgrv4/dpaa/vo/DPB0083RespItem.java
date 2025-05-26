package tpi.dgrv4.dpaa.vo;

public class DPB0083RespItem {

	/** 用來傳給下一個功能 */
	private String clientId;
	
	private String clientName;
	
	private String clientAlias;
	
	private String emails;
	
	private String clientStatus;

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getEmails() {
		return emails;
	}
	
	public String getClientName() {
		return clientName;
	}

	public void setClientAlias(String clientAlias) {
		this.clientAlias = clientAlias;
	}
	
	public String getClientAlias() {
		return clientAlias;
	}

	public void setEmails(String emails) {
		this.emails = emails;
	}

	public String getClientStatus() {
		return clientStatus;
	}
	
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public void setClientStatus(String clientStatus) {
		this.clientStatus = clientStatus;
	}
		
}
