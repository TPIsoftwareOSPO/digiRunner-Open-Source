package tpi.dgrv4.dpaa.vo;

public class DPB0280Req {
	
	private String webhookNotifyId;		
	
	private String enable;
	
	private String keyword;
	
	private String paging;

	public String getWebhookNotifyId() {
		return webhookNotifyId;
	}

	public void setWebhookNotifyId(String webhookNotifyId) {
		this.webhookNotifyId = webhookNotifyId;
	}

	public String getEnable() {
		return enable;
	}

	public void setEnable(String enable) {
		this.enable = enable;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getPaging() {
		return paging;
	}

	public void setPaging(String paging) {
		this.paging = paging;
	}	
}
