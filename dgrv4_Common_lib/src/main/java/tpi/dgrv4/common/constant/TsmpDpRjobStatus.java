package tpi.dgrv4.common.constant;

import java.util.function.Function;
import java.util.function.Predicate;

public enum TsmpDpRjobStatus {
	DISABLED("0", "作廢"),
	ACTIVE("1", "啟動"),
	PAUSE("2", "暫停"),
	IN_PROGRESS("3", "執行中")
	;

	private String value;

	private String text;

	private TsmpDpRjobStatus(String value, String text) {
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
		return get(text, (rjobStatus) -> {
			return rjobStatus.text.equals(text);
		}, (rjobStatus) -> {
			return rjobStatus.value;
		}) ;
	}

	public static String text(String value) {
		return get(value, (rjobStatus) -> {
			return rjobStatus.value.equals(value);
		}, (rjobStatus) -> {
			return rjobStatus.text;
		}) ;
	}

	public static final String get(String input, Predicate<TsmpDpRjobStatus> pFunc //
			, Function<TsmpDpRjobStatus, String> func) {
		for(TsmpDpRjobStatus rjobStatus : TsmpDpRjobStatus.values()) {
			if (pFunc.test(rjobStatus)) {
				return func.apply(rjobStatus); 
			}
		}
		return input;
	}

}
