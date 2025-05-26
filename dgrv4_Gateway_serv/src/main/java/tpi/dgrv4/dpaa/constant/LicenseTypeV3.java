package tpi.dgrv4.dpaa.constant;

public enum LicenseTypeV3 {
	/** 1 */
	edition(10),
	/** 2 */
	expireDate(8),
	/** 3 */
	nearWarnDays(3),
	/** 4 */
	overBufferDays(3),
	/** 5 */
	randomCode(14);

	private int value;

	private LicenseTypeV3(int value) {
		this.value = value;
	}

	public int value() {
		return this.value;
	}
}
