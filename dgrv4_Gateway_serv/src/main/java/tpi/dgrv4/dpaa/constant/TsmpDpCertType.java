package tpi.dgrv4.dpaa.constant;

import java.util.function.Function;
import java.util.function.Predicate;

public enum TsmpDpCertType {
	JWE("JWE", "JWE加密憑證"),
	TLS("TLS", "TLS通訊憑證"),
	;

	private String value;

	private String text;

	private TsmpDpCertType(String value, String text) {
		this.value = value;
		this.text = text;
	}

	public String value() {
		return this.value;
	}

	public String text() {
		return this.text;
	}

	public static String getValue(String text) {
		return get(text, (flag) -> {
			return flag.text.equals(text);
		}, (flag) -> {
			return flag.value;
		}) ;
	}

	public static String getText(String value) {
		return get(value, (flag) -> {
			return flag.value.equals(value);
		}, (flag) -> {
			return flag.text;
		}) ;
	}

	public static final String get(String input, Predicate<TsmpDpCertType> pFunc //
			, Function<TsmpDpCertType, String> func) {
		for(TsmpDpCertType flag : TsmpDpCertType.values()) {
			if (pFunc.test(flag)) {
				return func.apply(flag);
			}
		}
		return input;
	}
}
