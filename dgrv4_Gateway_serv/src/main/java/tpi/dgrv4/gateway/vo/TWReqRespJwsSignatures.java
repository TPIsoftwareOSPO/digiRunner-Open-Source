package tpi.dgrv4.gateway.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TWReqRespJwsSignatures {
	
	/** 描述訊息簽章演算法資訊 */
	@JsonProperty("protected")
	private String protectedData;

	/** 描述簽章金鑰資訊  本階段不使用，此欄位值放空字串("") */
	@JsonProperty("header")
	private String header = "";

	/** 訊息簽章  須採 BASE64URL 編碼 */
	@JsonProperty("signature")
	private String signature;

	@Override
	public String toString() {
		return "TWReqRespJwsSignatures [protectedData=" + protectedData + ", header=" + header + ", signature="
				+ signature + "]";
	}

	public String getProtectedData() {
		return protectedData;
	}

	public void setProtectedData(String protectedData) {
		this.protectedData = protectedData;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}
}
