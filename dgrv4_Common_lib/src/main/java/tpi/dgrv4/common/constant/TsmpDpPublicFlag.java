package tpi.dgrv4.common.constant;

import java.util.function.Function;
import java.util.function.Predicate;

public enum TsmpDpPublicFlag {
	ALL("0", " 對內及對外"),
	PUBLIC("1", "對外"),
	PRIVATE("2", "對內"),
	EMPTY("-1", "對內")
	;

	private String value;

	private String text;

	private TsmpDpPublicFlag(String value, String text) {
		this.value = value;
		this.text = text;
	}

	public String text() {
		return this.text;
	}
	
	public String value() {
		return this.value;
	}

	public static String getValue(String text) {
		return get(text, (flag) -> {
			return flag.text.equals(text);
		}, (flag) -> {
			return flag.value;
		}) ;
	}
	

	public static final String get(String input, Predicate<TsmpDpPublicFlag> pFunc //
			, Function<TsmpDpPublicFlag, String> func) {
		for(TsmpDpPublicFlag flag : TsmpDpPublicFlag.values()) {
			if (pFunc.test(flag)) {
				return func.apply(flag);
			}
		}
		return input;
	}
	
	public static String getText(String value) {
		return get(value, (flag) -> {
			return flag.value.equals(value);
		}, (flag) -> {
			return flag.text;
		}) ;
	}

	public static boolean isAll(String value) {
		return ALL.value.equals(value);
	}

	public static boolean isPrivate(String value) {
		return (value == null) || PRIVATE.value.equals(value);
	}
	
	public static boolean isPublic(String value) {
		return PUBLIC.value.equals(value);
	}

}
