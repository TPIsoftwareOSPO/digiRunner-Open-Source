package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class DPB0281Req extends ReqValidator {		
	
	private String notifyName;
	private String notifyType;
	private String enable;
	private String message;
	private String payloadFlag;
	private List<DPB0281Field> fieldList;
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
                        .field("notifyName")
                        .maxLength(100)
                        .isRequired()
                        .build(),
                new BeforeControllerRespItemBuilderSelector()
        				.buildString(locale)
        				.field("notifyType")
        				.maxLength(100)
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
