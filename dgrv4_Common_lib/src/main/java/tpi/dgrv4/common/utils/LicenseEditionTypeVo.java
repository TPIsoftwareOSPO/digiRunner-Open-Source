package tpi.dgrv4.common.utils;


//需根package tpi.dgrv4.common.utils.LicenseEditionType同步
public enum LicenseEditionTypeVo {
	Alpha
	,Enterprise //
	, Enterprise_Lite//
	, Express//
	;

	public static final int MAX_LENGTH;

	static {
		int maxLength = 0;
		for (LicenseEditionTypeVo e : LicenseEditionTypeVo.values()) {
			int eLength = e.toString().length();
			if (eLength > maxLength) {
				maxLength = eLength;
			}
		}
		MAX_LENGTH = maxLength;
	}
	
	public static LicenseEditionTypeVo resolve(String licenseEditionType) {
		for (LicenseEditionTypeVo type : values()) {
			if (type.toString().equals(licenseEditionType)) {
				return type;
			}
		}
		return null;
	}
}
