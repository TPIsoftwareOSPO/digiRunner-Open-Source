package tpi.dgrv4.gateway.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 簽章前-API 訊息區塊
 * HTTP Response
 * 
 * @author Mini
 */
public class TWResponse {
	
	@JsonProperty("header")
	private TWHeader twHeader;
	
	@JsonProperty("responseBody")
	private TWResponseBody twResponseBody;

	@Override
	public String toString() {
		return "TWResponse [twHeader=" + twHeader + ", twResponseBody=" + twResponseBody + "]";
	}

	public TWHeader getTwHeader() {
		return twHeader;
	}

	public void setTwHeader(TWHeader twHeader) {
		this.twHeader = twHeader;
	}

	public TWResponseBody getTwResponseBody() {
		return twResponseBody;
	}

	public void setTwResponseBody(TWResponseBody twResponseBody) {
		this.twResponseBody = twResponseBody;
	}
}
