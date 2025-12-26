package tpi.dgrv4.dpaa.constant;

public class RegexpConstant {

	/**英文和_和-*/
	public static final String ENGLISH_NUMBER = "^[\\w\\-]+$";
	/**英文和_和-和=*/
	public static final String ENGLISH_NUMBER_EQUALSIGN = "^[\\w\\-\\=]+$";
	/**EMAIL*/
	public static final String EMAIL = "^([^,](\\w+[-\\w+|\\.\\w+]*\\@[A-Za-z0-9]+([\\.|-][A-Za-z0-9]+)*\\.[A-Za-z]+,?)*[^,])?$";
	/**網址*/
	public static final String URI = "^(?:http(s)?:\\/\\/)?[\\w.-]+(?:\\.[\\w\\.-]+)+[\\w\\-\\._~:/?#\\[\\]@!\\$&'\\(\\)\\*\\+,;=.]+$";
	/**例外類型NWMD*/
	public static final String EX_TYPE = "[NWMD]";
	/** https, http, 或是空值 */
	public static final String HTTP_SCHEME = "^(https|http|)$";
	/** 'Y' or 'N' */
	public static final String Y_OR_N = "^[YN]$";
	/**中文英文和_和-*/
	public static final String CHINESE_ENGLISH_NUMBER = "^[\\u4E00-\\u9fa5\\w\\-]+$";
	/**英文和_和-和.和@*/
	public static final String ENGLISH_NUMBER_AT = "^[\\w\\-\\.\\@]+$";
	
	/**英文和數字和_和-和空白*/
	public static final String ENGLISH_NUMBER_WHITESPACE = "^[\\w\\-\\s]+$";
	
}
