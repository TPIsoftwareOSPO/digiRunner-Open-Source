package tpi.dgrv4.dpaa.constant;

//會員資格狀態
public enum TsmpDpQuyType {
	REQ("REQ", "申請單"),	//申請單
	EXA("EXA", "待審單"),	//待審單
	REV("REV", "已審單"),	//已審單
	;

	private String value;
	private String text;

	private TsmpDpQuyType(String value, String text) {
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
		for(TsmpDpQuyType status : values()) {
			if (status.text.equals(text)) {
				return status.value;
			}
		}
		return text;
	}

	public static String getText(String value) {
		for(TsmpDpQuyType status : values()) {
			if (status.value.equals(value)) {
				return status.text;
			}
		}
		return value;
	}

}
