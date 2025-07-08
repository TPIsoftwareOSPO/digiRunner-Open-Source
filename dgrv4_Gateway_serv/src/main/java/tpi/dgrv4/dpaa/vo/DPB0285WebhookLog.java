package tpi.dgrv4.dpaa.vo;

public class DPB0285WebhookLog {
	
	private String webhookNotifyLogId;
	
	private String notifyName;
	
	private String notifyType;
	
	private String clientId;
	
	private String content;
	
	private String remark;
	
	private String createDateTime;
	
	private String result; //Y:success N:fail

	public String getWebhookNotifyLogId() {
		return webhookNotifyLogId;
	}

	public void setWebhookNotifyLogId(String webhookNotifyLogId) {
		this.webhookNotifyLogId = webhookNotifyLogId;
	}

	public String getNotifyName() {
		return notifyName;
	}

	public void setNotifyName(String notifyName) {
		this.notifyName = notifyName;
	}

	public String getNotifyType() {
		return notifyType;
	}

	public void setNotifyType(String notifyType) {
		this.notifyType = notifyType;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(String createDateTime) {
		this.createDateTime = createDateTime;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}
}
