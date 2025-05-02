package tpi.dgrv4.common.constant;

public enum DashboardReportTypeEnum {
	/**1*/
	DATATIME(1),
	/**2*/
	REQUEST(2),
	/**3*/
	SUCCESS(3),
	/**4*/
	FAIL(4),
	/**5*/
	BAD_ATTEMPT(5),
	/**6*/
	AVG(6),
	/**7*/
	MEDIAN(7),
	/**8*/
	POPULAR(8),
	/**9*/
	UNPOPULAR(9),
	/**10*/
	API_TRAFFIC_DISTRIBUTION(10),
	/**11*/
	CLIENT_USAGE_PERCENTAGE(11),
	/**12*/
	CLIENT_USAGE_METRICS(12)
	;
	
	
	private int value;

	private DashboardReportTypeEnum(int value) {
		this.value = value;
	}

	public int value() {
		return this.value;
	}
}
