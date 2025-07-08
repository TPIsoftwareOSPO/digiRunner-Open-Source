package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0282Resp {		
	
	private String webhookNotifyId;
	private String notifyName;
	private String notifyType;
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
}
