package tpi.dgrv4.common.constant;

/**
 * 對應 TSMP_DP_API_AUTH2.APPLY_STATUS 申請狀態
 */
public enum TsmpDpApplyStatus {
	REVIEW("審核中"),
	PASS("通過"),
	FAIL("不通過")
	;

	private String text;

	private TsmpDpApplyStatus(String text) {
		this.text = text;
	}

	public String value() {
		return this.name();
	}

	public String text() {
		return this.text;
	}

	public static String getValue(String text) {
		for(TsmpDpApplyStatus status : values()) {
			if (status.text().equals(text)) {
				return status.value();
			}
		}
		return text;
	}

	public static String getText(String value) {
		for(TsmpDpApplyStatus status : values()) {
			if (status.value().equals(value)) {
				return status.text();
			}
		}
		return value;
	}

}
