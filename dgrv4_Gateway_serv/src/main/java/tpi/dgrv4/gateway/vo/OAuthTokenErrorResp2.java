package tpi.dgrv4.gateway.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OAuthTokenErrorResp2 {
	
	@JsonProperty("error")
	private String error;

	@JsonProperty("error_description")
	private String errorDescription;

	@Override
	public String toString() {
		return "OAuthTokenErrorResp2 [error=" + error + ", errorDescription=" + errorDescription + "]";
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getErrorDescription() {
		return errorDescription;
	}

	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}
}
