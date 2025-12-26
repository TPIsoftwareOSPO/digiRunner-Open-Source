package tpi.dgrv4.dpaa.constant;

public enum TsmpDpTaskSrc {
	JAVA("Java"),
	NET(".NET"),
	;

	private String value;

	private TsmpDpTaskSrc(String value) {
		this.value = value;
	}

	public String code() {
		return this.name();
	}

	public String value() {
		return this.value;
	}

}
