package tpi.dgrv4.common.constant;

public enum DateTimeFormatEnum {
	西元年月日T時分秒毫秒時區("yyyy-MM-dd'T'HH:mm:ss.SSSZ"),
	西元年月日T時分秒時區("yyyy-MM-dd'T'HH:mm:ssZ"),
	日月西元年時分秒毫秒("dd-MMM-yyyy HH:mm:ss:SSS"),
	西元年月日時分秒毫秒("yyyy-MM-dd HH:mm:ss.SSS"),
	西元年月日時分秒("yyyy-MM-dd HH:mm:ss"),
	西元年月日時分("yyyy-MM-dd HH:mm"),
	西元年月日時("yyyy-MM-dd HH"),
	西元年月日("yyyy-MM-dd"),
	西元年月("yyyy-MM"),
	西元年("yyyy"),

	西元年月日時分秒毫秒_2("yyyy/MM/dd HH:mm:ss.SSS"),
	西元年月日時分秒_2("yyyy/MM/dd HH:mm:ss"),
	西元年月日時分_2("yyyy/MM/dd HH:mm"),
	西元年月日時_2("yyyy/MM/dd HH"),
	西元年月日_2("yyyy/MM/dd"),
	西元年月_2("yyyy/MM"),

	西元年月日時分秒毫秒_3("yyyyMMdd HH:mm:ss.SSS"),
	西元年月日時分秒_3("yyyyMMdd HH:mm:ss"),
	西元年月日時分_3("yyyyMMdd HH:mm"),
	西元年月日時_3("yyyyMMdd HH"),
	西元年月日_3("yyyyMMdd"),

	西元年月日時分秒毫秒_4("yyyyMMddHHmmssSSS"),
	西元年月日時分秒_4("yyyyMMddHHmmss"),
	西元年月日時分_4("yyyyMMddHHmm"),
	西元年月日時_4("yyyyMMddHH"),
	西元年月日_4("yyyyMMdd"),
	

	檔名後綴("yyyyMMddHHmmssSSS"),
	西元年月日時分秒_5("yyMMddHHmmss"),
	時分秒("HH:mm:ss"),
	時分("HH:mm")
	;

	private String value;

	private DateTimeFormatEnum(String value) {
		this.value = value;
	}

	public String value() {
		return this.value;
	}

}