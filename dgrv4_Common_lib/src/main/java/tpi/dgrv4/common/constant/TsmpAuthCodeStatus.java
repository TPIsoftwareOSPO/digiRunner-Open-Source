package tpi.dgrv4.common.constant;

//Auth code 狀態
public enum TsmpAuthCodeStatus {
	AVAILABLE("0", "可用", "available"),
	USED("1", "已使用", "used"),
	INVALID("2", "失效", "invalid")
	;

	private String value;
	private String text;
	private String engText;

	private TsmpAuthCodeStatus(String value, String text, String engText) {
		this.value = value;
		this.text = text;
		this.engText = engText;
	}

	public String value() {
		return this.value;
	}

	public String text() {
		return this.text;
	}
	
	public String engText() {
		return this.engText;
	}

	public static String getValue(String text) {
		for(TsmpAuthCodeStatus status : values()) {
			if (status.text.equals(text)) {
				return status.value;
			}
		}
		return text;
	}

	public static String getText(String value) {
		for(TsmpAuthCodeStatus status : values()) {
			if (status.value.equals(value)) {
				return status.text;
			}
		}
		return value;
	}

	public static String getEngText(String value) {
		for(TsmpAuthCodeStatus status : values()) {
			if (status.value.equals(value)) {
				return status.engText;
			}
		}
		return value;
	}
}