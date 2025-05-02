package tpi.dgrv4.gateway.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TsmpBaseResp<BodyType> {

	public static final String KEY_OF_HEADER = "ResHeader";

	@JsonProperty("ResHeader")
	private ResHeader resHeader;
	
	@JsonProperty("RespBody")
	private BodyType body;

	public BodyType getBody() {
		return body;
	}

	public void setBody(BodyType body) {
		this.body = body;
	}

	public ResHeader getResHeader() {
		return resHeader;
	}

	public void setResHeader(ResHeader resHeader) {
		this.resHeader = resHeader;
	}

	@Override
	public String toString() {
		return "TsmpBaseResp [body=" + body + ", resHeader=" + resHeader + "]";
	}
	
}
