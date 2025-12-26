package tpi.dgrv4.gateway.constant;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * GTW / AC IdP(API) 調用 API 的 Request Body 類型
 * 
 * @author Mini
 */
public enum DgrIdPReqBodyType {
	NONE("N", "none"), // none
	FORM_DATA("F", "form-data"), // form-data
	X_WWW_FORM_URLENCODED("X", "x-www-form-urlencoded"), // x-www-form-urlencoded
	RAW("R", "raw");// raw

	private String value;

	private String text;

	private DgrIdPReqBodyType(String value, String text) {
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
		return get(text, (status) -> {
			return status.text.equals(text);
		}, (flag) -> {
			return flag.value;
		});
	}

	public static String getText(String value) {
		return get(value, (status) -> {
			return status.value.equals(value);
		}, (flag) -> {
			return flag.text;
		});
	}

	public static final String get(String input, Predicate<DgrIdPReqBodyType> pFunc //
			, Function<DgrIdPReqBodyType, String> func) {
		for (DgrIdPReqBodyType status : DgrIdPReqBodyType.values()) {
			if (pFunc.test(status)) {
				return func.apply(status);
			}
		}
		return input;
	}

	public boolean isValueEquals(String input) {
		return value().equals(input);
	}
}
