package tpi.dgrv4.common.constant;

public enum ReportDateTimeRangeTypeEnum {
	/**1*/
	MINUTE(1),
	/**2*/
	HOUR(2),
	/**3*/
	DAY(3),
	/**4*/
	MONTH(4),
	/**5*/
	YEAR(5)
	;

	private int value;

	private ReportDateTimeRangeTypeEnum(int value) {
		this.value = value;
	}

	public int value() {
		return this.value;
	}

}
