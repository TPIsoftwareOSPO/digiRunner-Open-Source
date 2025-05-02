package tpi.dgrv4.dpaa.constant;

public enum TsmpApiSrc {
	NET_MODULE("N"),
	JAVA_MODULE("M"),
	REGISTERED("R"),
	COMPOSED("C"),
	;

	private String value;

	private TsmpApiSrc(String value) {
		this.value = value;
	}

	public String code() {
		return this.name();
	}

	public String value() {
		return this.value;
	}

}
