package tpi.dgrv4.gateway.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TWResponseBody {
	
	@JsonProperty("appRepBody")
	private String appRepBody;
	
	@JsonProperty("appRepExtension")
	private String appRepExtension;

	@Override
	public String toString() {
		return "TWResponseBody [appRepBody=" + appRepBody + ", appRepExtension=" + appRepExtension + "]";
	}

	public String getAppRepBody() {
		return appRepBody;
	}

	public void setAppRepBody(String appRepBody) {
		this.appRepBody = appRepBody;
	}

	public String getAppRepExtension() {
		return appRepExtension;
	}

	public void setAppRepExtension(String appRepExtension) {
		this.appRepExtension = appRepExtension;
	}
}
