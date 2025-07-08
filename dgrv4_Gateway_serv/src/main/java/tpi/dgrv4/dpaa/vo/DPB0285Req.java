package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class DPB0285Req extends ReqValidator {
			
	private String webhookNotifyLogId;
	
	private String startDate;
	
	private String endDate;
	
	private String keyword;
	
	private String paging;

	public String getWebhookNotifyLogId() {
		return webhookNotifyLogId;
	}

	public void setWebhookNotifyLogId(String webhookNotifyLogId) {
		this.webhookNotifyLogId = webhookNotifyLogId;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
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
	
	@Override
    protected List<BeforeControllerRespItem> provideConstraints(String locale) {
        return Arrays.asList(new BeforeControllerRespItem[] {
                new BeforeControllerRespItemBuilderSelector()
                        .buildString(locale)
                        .field("startDate")
                        .isRequired()
                        .build(),
                new BeforeControllerRespItemBuilderSelector()
        				.buildString(locale)
        				.field("endDate")
        				.isRequired()
        				.build(),        		     	
        });
	}
}
