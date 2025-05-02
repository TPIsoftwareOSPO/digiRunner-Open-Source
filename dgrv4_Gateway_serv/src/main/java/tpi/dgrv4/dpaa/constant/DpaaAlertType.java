package tpi.dgrv4.dpaa.constant;

public class DpaaAlertType {

	public final static String CPU = "CPU";

	public final static String Heap = "Heap";

	public final static String Disk = "Disk";
	
	public final static String[] SYSTEM_BASIC_TYPE = new String[] {CPU, Heap, Disk};

	public final static String KEYWORD = "keyword";

	public final static String RESPONSE_TIME = "responseTime";

	public final static String[] KEYWORD_TYPE = new String[] {KEYWORD, RESPONSE_TIME};

	public static boolean isSystemBasicType(String alertType) {
		for (String sbType : SYSTEM_BASIC_TYPE) {
			if (sbType.equals(alertType)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isKeywordType(String alertType) {
		for (String sbType : KEYWORD_TYPE) {
			if (sbType.equals(alertType)) {
				return true;
			}
		}
		return false;
	}

}
