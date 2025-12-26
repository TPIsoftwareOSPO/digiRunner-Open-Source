package tpi.dgrv4.dpaa.constant;

/**
 * 用戶端匯入的資料異動狀態
 */
public enum AnalyzeClientRelatedDataStatus {
	A("add"),
	R("repeat"),
	C("cover"),
	CA("cannot be add"),
	CR("cannot be repeat")
	;

	private String text;

	private AnalyzeClientRelatedDataStatus(String text) {
		this.text = text;
	}

	public String value() {
		return this.name();
	}

	public String text() {
		return this.text;
	}

	public static String getValue(String text) {
		for(AnalyzeClientRelatedDataStatus status : values()) {
			if (status.text().equals(text)) {
				return status.value();
			}
		}
		return text;
	}

	public static String getText(String value) {
		for(AnalyzeClientRelatedDataStatus status : values()) {
			if (status.value().equals(value)) {
				return status.text();
			}
		}
		return value;
	}

}
