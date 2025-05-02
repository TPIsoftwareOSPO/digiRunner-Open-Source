package tpi.dgrv4.gateway.vo;

public class WebsiteTrafficVo {
	
	private long timestampSec;
	private String targetUrl;
	private int frequency = 0;
	
	public long getTimestampSec() {
		return timestampSec;
	}
	public void setTimestampSec(long timestampSec) {
		this.timestampSec = timestampSec;
	}
	public String getTargetUrl() {
		return targetUrl;
	}
	public void setTargetUrl(String targetUrl) {
		this.targetUrl = targetUrl;
	}
	public int getFrequency() {
		return frequency;
	}
	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	
}
