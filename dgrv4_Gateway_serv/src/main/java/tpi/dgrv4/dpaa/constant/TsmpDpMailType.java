package tpi.dgrv4.dpaa.constant;

/**
 * Mail的內文類型
 */
public enum TsmpDpMailType {
	SAME("內文相同"),
	DIFFERENT("內文不同"),
	;

	private String text;

	private TsmpDpMailType(String text) {
		this.text = text;
	}

	public String value() {
		return this.name();
	}

	public String text() {
		return this.text;
	}

	public static String getValue(String text) {
		for(TsmpDpMailType status : values()) {
			if (status.text().equals(text)) {
				return status.value();
			}
		}
		return text;
	}

	public static String getText(String value) {
		for(TsmpDpMailType status : values()) {
			if (status.value().equals(value)) {
				return status.text();
			}
		}
		return value;
	}

}
