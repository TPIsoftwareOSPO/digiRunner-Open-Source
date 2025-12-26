package tpi.dgrv4.dpaa.constant;

import java.util.function.Function;
import java.util.function.Predicate;

public enum TsmpDpNodeHealth {
	SUCCESS("success", "良好"),
	WARNING("warning", "警示"),
	DANGER("danger", "危險"),
	;

	private String value;

	private String text;

	private TsmpDpNodeHealth(String value, String text) {
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

	public static final String get(String input, Predicate<TsmpDpNodeHealth> pFunc //
			, Function<TsmpDpNodeHealth, String> func) {
		for(TsmpDpNodeHealth en : TsmpDpNodeHealth.values()) {
			if (pFunc.test(en)) {
				return func.apply(en); 
			}
		}
		return input;
	}
}
