package tpi.dgrv4.gateway.constant;

public enum DgrCusAcIdpLogin {

	DGR_STATE("dgrState"), //
	CUS_STATE("cusState"), //
	CUS_CODE("cusCode"), //

	CUS_USER_ID("cusUserId"), //
	CUS_USER_ALIAS("cusUserAlias"), //
	CUS_USER_EMAIL("cusUserEmail"), //
	CUS_USER_PICTURE("cusUserPicture"),//

	;

	private String value;

	private DgrCusAcIdpLogin(String value) {
		this.value = value;
	}

	public String code() {
		return this.name();
	}

	public String value() {
		return this.value;
	}

}
