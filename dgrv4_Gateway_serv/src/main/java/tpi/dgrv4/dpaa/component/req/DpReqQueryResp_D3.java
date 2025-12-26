package tpi.dgrv4.dpaa.component.req;

public class DpReqQueryResp_D3 {

	private Long reqOrderd3Id;

	private String clientId;

	private String clientName;

	private String emails;

	private String publicFlag;

	private String publicFlagName;

	public DpReqQueryResp_D3() {
	}

	public Long getReqOrderd3Id() {
		return reqOrderd3Id;
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
	
	public void setReqOrderd3Id(Long reqOrderd3Id) {
		this.reqOrderd3Id = reqOrderd3Id;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getEmails() {
		return emails;
	}

	public String getPublicFlag() {
		return publicFlag;
	}

	public void setPublicFlag(String publicFlag) {
		this.publicFlag = publicFlag;
	}

	public String getPublicFlagName() {
		return publicFlagName;
	}

	public void setEmails(String emails) {
		this.emails = emails;
	}
	
	public void setPublicFlagName(String publicFlagName) {
		this.publicFlagName = publicFlagName;
	}

}
