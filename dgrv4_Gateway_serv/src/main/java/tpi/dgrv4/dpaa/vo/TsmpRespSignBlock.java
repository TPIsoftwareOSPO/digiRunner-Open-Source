package tpi.dgrv4.dpaa.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TsmpRespSignBlock<BodyType> {

	@JsonProperty("ResHeader")
	private ResHeaderSignBlock resHeader;
	
	@JsonProperty("Res_getSignBlock")
	private BodyType body;

	public BodyType getBody() {
		return body;
	}

	public void setBody(BodyType body) {
		this.body = body;
	}

	public ResHeaderSignBlock getResHeader() {
		return resHeader;
	}

	public void setResHeader(ResHeaderSignBlock resHeader) {
		this.resHeader = resHeader;
	}

	@Override
	public String toString() {
		return "TsmpSignBlockResp [body=" + body + ", resHeader=" + resHeader + "]";
	}
	
}
