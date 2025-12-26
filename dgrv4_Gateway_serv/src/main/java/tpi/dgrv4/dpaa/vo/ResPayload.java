package tpi.dgrv4.dpaa.vo;

import tpi.dgrv4.gateway.vo.ResHeader;

public class ResPayload<T> {

	public static final String KEY_OF_HEADER = "ResHeader";

	private ResHeader header;

	private T body;

	public ResHeader getHeader() {
		return this.header;
	}

	public void setHeader(ResHeader header) {
		this.header = header;
	}

	public T getBody() {
		return this.body;
	}

	public void setBody(T body) {
		this.body = body;
	}
}
