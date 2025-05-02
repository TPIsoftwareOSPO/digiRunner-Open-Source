package tpi.dgrv4.gateway.vo;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JweEncryptionReq {
	@JsonProperty("dataMap")
	private Map<String, String> dataMap;

	@Override
	public String toString() {
		return "JweEncryptionReq [dataMap=" + dataMap + "]";
	}

	public Map<String, String> getDataMap() {
		return dataMap;
	}

	public void setDataMap(Map<String, String> dataMap) {
		this.dataMap = dataMap;
	}
}
