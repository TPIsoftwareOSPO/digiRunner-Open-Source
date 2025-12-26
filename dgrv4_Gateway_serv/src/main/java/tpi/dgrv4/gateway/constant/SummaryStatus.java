package tpi.dgrv4.gateway.constant;

public enum SummaryStatus {
	
	ENABLED("1"), 
	DISABLED("2"), 
	LOCKED("3"), 
	REGISTER("R"), 
	COMPOSER("C"), 
	ON("1"), 
	OFF("2"),;

	private String value;

	private SummaryStatus(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}
}
