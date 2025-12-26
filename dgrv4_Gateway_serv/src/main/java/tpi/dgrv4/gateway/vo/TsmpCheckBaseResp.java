package tpi.dgrv4.gateway.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TsmpCheckBaseResp {

	@JsonProperty("ResHeader")
	private ResHeader resHeader;

	@JsonProperty("errorDescription")
	private String errorDescription;

	@JsonProperty("code")
	private String code;

	@JsonProperty("message")
	private String message;

	public ResHeader getResHeader() {
		return resHeader;
	}

	public void setResHeader(ResHeader resHeader) {
		this.resHeader = resHeader;
	}

	public String getErrorDescription() {
		return errorDescription;
	}

	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
