package tpi.dgrv4.common.utils;

public enum LicenseEditionType {
	Alpha
	,Enterprise //
	, Enterprise_Lite//
	, Express//
	;

	public static final int MAX_LENGTH;

	static {
		int maxLength = 0;
		for (LicenseEditionType e : LicenseEditionType.values()) {
			int eLength = e.toString().length();
			if (eLength > maxLength) {
				maxLength = eLength;
			}
		}
		MAX_LENGTH = maxLength;
	}
	
	public static LicenseEditionType resolve(String licenseEditionType) {
		for (LicenseEditionType type : values()) {
			if (type.toString().equals(licenseEditionType)) {
				return type;
			}
		}
		return null;
	}
}
