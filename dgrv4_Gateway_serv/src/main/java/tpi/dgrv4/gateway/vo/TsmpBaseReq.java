package tpi.dgrv4.gateway.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import tpi.dgrv4.common.vo.ReqHeader;

public class TsmpBaseReq<BodyType> {

	public static final String KEY_OF_HEADER = "ReqHeader";
	
	@JsonProperty("ReqHeader")
	private ReqHeader reqHeader;
	
	@JsonProperty("ReqBody")
	private BodyType body;

	public BodyType getBody() {
		return body;
	}

	public void setBody(BodyType body) {
		this.body = body;
	}

	public ReqHeader getReqHeader() {
		return reqHeader;
	}

	public void setReqHeader(ReqHeader reqHeader) {
		this.reqHeader = reqHeader;
	}

	@Override
	public String toString() {
		return "TsmpBaseReq [body=" + body + ", reqHeader=" + reqHeader + "]";
	}
	
}
