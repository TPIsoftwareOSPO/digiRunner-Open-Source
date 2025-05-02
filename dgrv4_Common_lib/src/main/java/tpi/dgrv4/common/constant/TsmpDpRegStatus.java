package tpi.dgrv4.common.constant;

//會員資格狀態
public enum TsmpDpRegStatus {
	SAVED("0", "儲存"),
	REVIEWING("1", "送審"),
	PASS("2", "放行"),
	RETURN("3", "退回"),
	RESUBMIT("4", "重新送審")
	;

	private String value;
	private String text;

	private TsmpDpRegStatus(String value, String text) {
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
		for(TsmpDpRegStatus status : values()) {
			if (status.text.equals(text)) {
				return status.value;
			}
		}
		return text;
	}

	public static String getText(String value) {
		for(TsmpDpRegStatus status : values()) {
			if (status.value.equals(value)) {
				return status.text;
			}
		}
		return value;
	}

}
