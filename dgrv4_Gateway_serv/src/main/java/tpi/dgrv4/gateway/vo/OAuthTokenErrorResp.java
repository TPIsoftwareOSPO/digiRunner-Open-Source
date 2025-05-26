package tpi.dgrv4.gateway.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OAuthTokenErrorResp {
	@JsonProperty("timestamp")
	private String timestamp;

	@JsonProperty("status")
	private int status;

	@JsonProperty("error")
	private String error;

	@JsonProperty("message")
	@JsonInclude (JsonInclude.Include.NON_NULL)
	private String message;

	@JsonProperty("path")
	private String path;

	@Override
	public String toString() {
		return "OAuthTokenErrorResp [timestamp=" + timestamp + ", status=" + status + ", error=" + error + ", message="
				+ message + ", path=" + path + "]";
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
