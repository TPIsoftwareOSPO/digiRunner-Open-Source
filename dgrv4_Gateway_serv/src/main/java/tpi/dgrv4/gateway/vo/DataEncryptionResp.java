package tpi.dgrv4.gateway.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DataEncryptionResp {
	//checkmarx, Excessive Data Exposure
	@JsonProperty("ciphertext")
	private String text;

	@Override
	public String toString() {
		return "DataEncryptionResp [ciphertext=" + text + "]";
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	

	
}
