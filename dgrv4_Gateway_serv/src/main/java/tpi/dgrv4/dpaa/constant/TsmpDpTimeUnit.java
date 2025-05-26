package tpi.dgrv4.dpaa.constant;

//時間單位
public enum TsmpDpTimeUnit {
	SECOND("s", "秒"),	
	MINUTE("m", "分鐘"),
	HOUR("H", "小時"),
	DAY("d", "天"),
	;

	private String value;
	private String text;

	private TsmpDpTimeUnit(String value, String text) {
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
		for(TsmpDpTimeUnit status : values()) {
			if (status.text.equals(text)) {
				return status.value;
			}
		}
		return text;
	}

	public static String getText(String value) {
		for(TsmpDpTimeUnit status : values()) {
			if (status.value.equals(value)) {
				return status.text;
			}
		}
		return value;
	}

}
