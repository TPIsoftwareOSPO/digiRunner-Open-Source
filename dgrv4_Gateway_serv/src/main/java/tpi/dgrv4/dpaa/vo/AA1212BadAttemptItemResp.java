package tpi.dgrv4.dpaa.vo;

public class AA1212BadAttemptItemResp {
	private String uri;
	private String statusCode;

	public AA1212BadAttemptItemResp() {}
	public AA1212BadAttemptItemResp(String uri, String statusCode) {
		this.uri = uri;
		this.statusCode = statusCode;
	}
	
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}
	
}
