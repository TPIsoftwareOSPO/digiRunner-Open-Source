package tpi.dgrv4.dpaa.vo;

public class DPB0280WebhookNotify {
	
	private String webhookNotifyId;		
	
	private String notifyName;
	
	private String notifyType;
	
	private String enable;
	
	private String enableName;
	
	private String createDateTime;
	
	private String createUser;

	public String getWebhookNotifyId() {
		return webhookNotifyId;
	}

	public void setWebhookNotifyId(String webhookNotifyId) {
		this.webhookNotifyId = webhookNotifyId;
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

	public String getEnable() {
		return enable;
	}

	public void setEnable(String enable) {
		this.enable = enable;
	}

	public String getEnableName() {
		return enableName;
	}

	public void setEnableName(String enableName) {
		this.enableName = enableName;
	}

	public String getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(String createDateTime) {
		this.createDateTime = createDateTime;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}
}
