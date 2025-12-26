package tpi.dgrv4.gateway.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 簽章前-API 訊息區塊
 * HTTP Request
 * 
 * @author Mini
 */
public class TWRequest {
	
	@JsonProperty("header")
	private TWHeader twHeader;
	
	@JsonProperty("requestBody")
	private String requestBody;

	@Override
	public String toString() {
		return "TWRequest [twHeader=" + twHeader + ", requestBody=" + requestBody + "]";
	}

	public TWHeader getTwHeader() {
		return twHeader;
	}

	public void setTwHeader(TWHeader twHeader) {
		this.twHeader = twHeader;
	}

	public String getRequestBody() {
		return requestBody;
	}

	public void setRequestBody(String requestBody) {
		this.requestBody = requestBody;
	}
}
