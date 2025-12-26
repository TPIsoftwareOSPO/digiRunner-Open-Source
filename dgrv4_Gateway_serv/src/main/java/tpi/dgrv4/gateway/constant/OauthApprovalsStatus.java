package tpi.dgrv4.gateway.constant;

public enum OauthApprovalsStatus {
	APPROVED("APPROVED"),
	DENIED("DENIED"),
	;
	private String value;

	private OauthApprovalsStatus(String value) {
		this.value = value;
	}

	public String code() {
		return this.name();
	}

	public String value() {
		return this.value;
	}
}
