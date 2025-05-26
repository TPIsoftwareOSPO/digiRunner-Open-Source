package tpi.dgrv4.dpaa.vo;

public class AA1212RankedResp {
	private String uri;
	private Integer statusCode;
	private String elapsedTime;
	
	public AA1212RankedResp() {}
	public AA1212RankedResp(String uri, Integer statusCode, String elapsedTime) {
		this.uri = uri;
		this.statusCode = statusCode;
		this.elapsedTime = elapsedTime;
	}
	
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public Integer getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(Integer statusCode) {
		this.statusCode = statusCode;
	}
	public String getElapsedTime() {
		return elapsedTime;
	}
	public void setElapsedTime(String elapsedTime) {
		this.elapsedTime = elapsedTime;
	}
	
	
	
	
	
}
