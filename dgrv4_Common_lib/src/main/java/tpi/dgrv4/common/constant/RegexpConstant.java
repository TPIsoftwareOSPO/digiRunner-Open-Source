package tpi.dgrv4.common.constant;

public class RegexpConstant {

	/**英文和_和-*/
	public static final String ENGLISH_NUMBER = "^[\\w|\\-]+$"; 
	/**EMAIL*/
	public static final String EMAIL = "^([^,](\\w+[-\\w+|\\.\\w+]*\\@[A-Za-z0-9]+([\\.|-][A-Za-z0-9]+)*\\.[A-Za-z]+,?)*[^,])?$";
	/**網址*/
	public static final String URI = "^(?:http(s)?:\\/\\/)?[\\w.-]+(?:\\.[\\w\\.-]+)+[\\w\\-\\._~:/?#\\[\\]@!\\$&'\\(\\)\\*\\+,;=.]+$";
	/**例外類型NWMD*/
	public static final String EX_TYPE = "[N|W|M|D]";
	/** https, http, 或是空值 */
	public static final String HTTP_SCHEME = "^(https|http|)$";
	/** 'Y' or 'N' */
	public static final String Y_OR_N = "^[Y|N]$";
	/** 主機路徑,注意只有/是不合法
	 *  1./
	 *  2./var/log/security
	 *  3./var/log/security/
	 *  4.var/log/security
	 *  2、3合法(目錄都是security)
	 *  1、4不合法 */
	public static final String HOST_PATH = "(/[\\w-]+)+/?$";
	
	/** IP */
	public static final String IP = "^((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])$";
	/** IP6_1 */
	public static final String IP6_1 = "^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$";
	/** IP6_2 */
	public static final String IP6_2 = "^((?:[0-9A-Fa-f]{1,4}(:[0-9A-Fa-f]{1,4})*)?)::((?:([0-9A-Fa-f]{1,4}:)*[0-9A-Fa-f]{1,4})?)$";
	/** IP6_3 */
	public static final String IP6_3 = "^(::(?:[0-9A-Fa-f]{1,4})(?::[0-9A-Fa-f]{1,4}){5})|((?:[0-9A-Fa-f]{1,4})(?::[0-9A-Fa-f]{1,4}){5}::)$";
	/** 1或0 */
	public static final String ONE_ZERO = "[1|0]";

	/**
	 * IPv4 , IPv6(含縮寫ex ::0 ) IPv4 CIDR, IPv6 CIDR , localhost  , FQDN ,用逗號串接也可以單獨使用
	 * 10.20.30.1   IPv4
	 * 0.0.0.0/0  IPv4 CIDR
	 * 2001:db8::/32   IPv6 CIDR
	 * ::/0   IPv6 縮寫 CIDR
	 * ::1   IPv6 縮寫
	 * 2001:0db9:ffff:ffff:ffff:ffff:ffff:ffff  IPv6
	 * example.com  FQDN
	 * localhost
	 * 10.20.30.4,10.20.40.0/24,www.google.com,::1,::/0,localhost,127.0.0.1
 	 */
	public static final String IP_CIDR_FQDN = "((((\\d{1,3}\\.){3}\\d{1,3}(\\/\\d{1,2})?|([0-9a-fA-F]{1,4}:){1,7}:?([0-9a-fA-F]{1,4})?(\\/\\d{1,3})?|::\\/\\d{1,3}|([0-9a-fA-F]{0,4}:){2,7}[0-9a-fA-F]{0,4}|[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})|((\\d{1,3}\\.){3}\\d{1,3}(\\/\\d{1,2})?|([0-9a-fA-F]{1,4}:){1,7}:?([0-9a-fA-F]{1,4})?(\\/\\d{1,3})?|::\\/\\d{1,3}|([0-9a-fA-F]{0,4}:){2,7}[0-9a-fA-F]{0,4}|[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})|localhost),*)+";

}

