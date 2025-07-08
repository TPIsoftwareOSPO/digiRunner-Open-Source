package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class DPB0286Req extends ReqValidator {
			
	private String webhookNotifyLogId;

	public String getWebhookNotifyLogId() {
		return webhookNotifyLogId;
	}
	
	public void setWebhookNotifyLogId(String webhookNotifyLogId) {
		this.webhookNotifyLogId = webhookNotifyLogId;
	}

	@Override
    protected List<BeforeControllerRespItem> provideConstraints(String locale) {
        return Arrays.asList(new BeforeControllerRespItem[] {
        		new BeforeControllerRespItemBuilderSelector()
		                .buildString(locale)
		                .field("webhookNotifyLogId")
		                .isRequired()
		                .build(),
        });
	}
}
