package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class DPB0283Req extends ReqValidator {		
	
	private String webhookNotifyId;
	private String enable;
	private String message;
	private String payloadFlag;
	private List<DPB0281Field> fieldList;
		
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
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getPayloadFlag() {
		return payloadFlag;
	}
	public void setPayloadFlag(String payloadFlag) {
		this.payloadFlag = payloadFlag;
	}
	public List<DPB0281Field> getFieldList() {
		return fieldList;
	}
	public void setFieldList(List<DPB0281Field> fieldList) {
		this.fieldList = fieldList;
	}
	
	@Override
    protected List<BeforeControllerRespItem> provideConstraints(String locale) {
        return Arrays.asList(new BeforeControllerRespItem[] {
        		new BeforeControllerRespItemBuilderSelector()
		                .buildString(locale)
		                .field("webhookNotifyId")
		                .isRequired()
		                .build(),                                
        		new BeforeControllerRespItemBuilderSelector()
        				.buildString(locale)
        				.field("enable")
        				.isRequired()
        				.build(),        				
        		new BeforeControllerRespItemBuilderSelector()
        				.buildString(locale)
        				.field("payloadFlag")
        				.isRequired()
        				.pattern("^[012]$")
        				.build(),
        });
	}
}
