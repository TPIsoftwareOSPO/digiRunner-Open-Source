package tpi.dgrv4.gateway.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorResp {

	@JsonProperty("error")
	private String error;

	@JsonProperty("error_description")
	private String errorDescription;

	@Override
	public String toString() {
		return "ErrorResp [error=" + error + ", errorDescription=" + errorDescription + ", getClass()=" + getClass()
				+ ", hashCode()=" + hashCode() + ", toString()=" + super.toString() + "]";
	}

	public ErrorResp(String error, String errorDescription) {
		super();
		this.error = error;
		this.errorDescription = errorDescription;
	}
}
