package tpi.dgrv4.gateway.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DataEncryptionReq {
	@JsonProperty("data")
	private String data;

	@Override
	public String toString() {
		return "DataEncryptionReq [data=" + data + "]";
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
}
