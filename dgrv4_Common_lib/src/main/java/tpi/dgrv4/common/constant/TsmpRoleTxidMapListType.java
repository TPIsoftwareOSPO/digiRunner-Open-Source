package tpi.dgrv4.common.constant;

public enum TsmpRoleTxidMapListType {
	WHITE_LIST("W", "白名單"),
	BLACK_LIST("B", "黑名單")
	;
	
	private String value;

	private String text;

	private TsmpRoleTxidMapListType(String value, String text) {
		this.value = value;
		this.text = text;
	}

	public String value() {
		return this.value;
	}

	public String text() {
		return this.text;
	}

	public static String getValue(String text) {
		for(TsmpRoleTxidMapListType type : values()) {
			if (type.text.equals(text)) {
				return type.value;
			}
		}
		return text;
	}

	public static String getText(String value) {
		for(TsmpRoleTxidMapListType type : values()) {
			if (type.value.equals(value)) {
				return type.text;
			}
		}
		return value;
	}
	
}