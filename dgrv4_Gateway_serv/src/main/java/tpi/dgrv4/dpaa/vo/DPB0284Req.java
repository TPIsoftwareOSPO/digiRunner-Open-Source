package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class DPB0284Req extends ReqValidator {
			
	private String webhookNotifyId;
	
	public String getWebhookNotifyId() {
		return webhookNotifyId;
	}

	public void setWebhookNotifyId(String webhookNotifyId) {
		this.webhookNotifyId = webhookNotifyId;
	}

	@Override
    protected List<BeforeControllerRespItem> provideConstraints(String locale) {
        return Arrays.asList(new BeforeControllerRespItem[] {
        		new BeforeControllerRespItemBuilderSelector()
		                .buildString(locale)
		                .field("webhookNotifyId")
		                .isRequired()
		                .build(),
        });
	}
}
