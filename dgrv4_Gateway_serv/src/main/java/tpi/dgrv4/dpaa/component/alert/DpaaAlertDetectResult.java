package tpi.dgrv4.dpaa.component.alert;

import java.util.HashMap;
import java.util.Map;

import tpi.dgrv4.entity.entity.jpql.TsmpAlert;

public class DpaaAlertDetectResult {

	private boolean isAlert = false;

	private TsmpAlert entity;

	private Map<String, Object> payload;

	/**
	 * 為了 JSON Deserialization 用, 不應直接呼叫此建構子
	 */
	public DpaaAlertDetectResult() {
	}

	public DpaaAlertDetectResult(TsmpAlert entity) {
		setEntity(entity);
		setPayload(new HashMap<>());
	}

	public boolean isAlert() {
		return isAlert;
	}

	public void setAlert(boolean isAlert) {
		this.isAlert = isAlert;
	}

	public TsmpAlert getEntity() {
		return entity;
	}

	public void setEntity(TsmpAlert entity) {
		this.entity = entity;
	}

	public Map<String, Object> getPayload() {
		return payload;
	}

	public void setPayload(Map<String, Object> payload) {
		this.payload = payload;
	}

	@Override
	public String toString() {
		return "DpaaAlertDetectResult [isAlert=" + isAlert + ", entity=" + entity + ", payload=" + payload + "]";
	}

}