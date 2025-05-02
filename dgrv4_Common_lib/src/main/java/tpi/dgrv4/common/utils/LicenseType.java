package tpi.dgrv4.common.utils;

public enum LicenseType {
	/** 1 */
	edition(15),
	/** 2 */
	expireDate(8),
	/** 3 */
	nearWarnDays(3),
	/** 4 */
	overBufferDays(3),
	/** 5 */
	randomCode(14),
	
	account(20), 
	
	env(5)
	;

	private int value;

	private LicenseType(int value) {
		this.value = value;
	}

	public int value() {
		return this.value;
	}
}
