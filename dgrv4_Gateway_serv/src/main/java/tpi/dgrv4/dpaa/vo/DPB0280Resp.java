package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0280Resp {
			
	private List<DPB0280WebhookNotify> webhookNotifyList;

	public List<DPB0280WebhookNotify> getWebhookNotifyList() {
		return webhookNotifyList;
	}

	public void setWebhookNotifyList(List<DPB0280WebhookNotify> webhookNotifyList) {
		this.webhookNotifyList = webhookNotifyList;
	}
}
