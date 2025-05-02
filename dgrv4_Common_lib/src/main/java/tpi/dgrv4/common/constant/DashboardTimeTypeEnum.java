package tpi.dgrv4.common.constant;

public enum DashboardTimeTypeEnum {
	/** 1 */
	MINUTE(1),
	/** 2 */
	DAY(2),
	/** 3 */
	MONTH(3),
	/** 4 */
	YEAR(4);

	private int value;

	private DashboardTimeTypeEnum(int value) {
		this.value = value;
	}

	public int value() {
		return this.value;
	}

	public static boolean checkInput(int value) {
		for (DashboardTimeTypeEnum e : DashboardTimeTypeEnum.values()) {
			if (e.value == value) {
				return true;
			}
		}
		return false;
	}
}
