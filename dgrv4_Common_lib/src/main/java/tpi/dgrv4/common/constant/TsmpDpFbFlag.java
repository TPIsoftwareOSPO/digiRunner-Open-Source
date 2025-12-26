package tpi.dgrv4.common.constant;

import java.util.function.Function;
import java.util.function.Predicate;

public enum TsmpDpFbFlag {
	FRONT("FRONT", "前台"),
	BACK("BACK", "後台")	
	;

	private String value;

	private String text;

	private TsmpDpFbFlag(String value, String text) {
		this.value = value;
		this.text = text;
	}

	public String value() {
		return this.value;
	}

	public String text() {
		return this.text;
	}

	public static String value(String text) {
		return get(text, (en) -> {
			return en.text.equals(text);
		}, (en) -> {
			return en.value;
		}) ;
	}

	public static String text(String value) {
		return get(value, (en) -> {
			return en.value.equals(value);
		}, (en) -> {
			return en.text;
		}) ;
	}

	public static final String get(String input, Predicate<TsmpDpFbFlag> pFunc //
			, Function<TsmpDpFbFlag, String> func) {
		for(TsmpDpFbFlag en : TsmpDpFbFlag.values()) {
			if (pFunc.test(en)) {
				return func.apply(en); 
			}
		}
		return input;
	}
}
