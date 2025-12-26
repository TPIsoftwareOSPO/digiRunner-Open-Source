package tpi.dgrv4.common.constant;

public enum ReportTypeEnum {
	/**1*/
	API使用次數統計(1),
	/**2*/
	API次數_時間分析(2),
	/**3*/
	API平均時間計算分析(3),
	/**4*/
	API流量分析(4),
	/**5*/
	BadAttempt連線報告(5)
	;

	private int value;

	private ReportTypeEnum(int value) {
		this.value = value;
	}

	public int value() {
		return this.value;
	}

}
