package tpi.dgrv4.dpaa.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TsmpRespSignBlockReset<BodyType> {

	@JsonProperty("ResHeader")
	private ResHeaderSignBlockReset resHeader;
	
	@JsonProperty("Res_resetSignBlock")
	private BodyType body;

	public BodyType getBody() {
		return body;
	}

	public void setBody(BodyType body) {
		this.body = body;
	}

	public ResHeaderSignBlockReset getResHeader() {
		return resHeader;
	}

	public void setResHeader(ResHeaderSignBlockReset resHeader) {
		this.resHeader = resHeader;
	}

	@Override
	public String toString() {
		return "TsmpRespSignBlockReset [body=" + body + ", resHeader=" + resHeader + "]";
	}
	
}
