package tpi.dgrv4.gateway.constant;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * AC IdP(MLDAP) 的驗證政策
 */
public enum DgrMLdapPolicy {
	SEQUENTIALLY("S", "Sequentially"), // 依順序
	RANDOM("R", "Random"); // 隨機

	private String value;
	private String text;

	private DgrMLdapPolicy(String value, String text) {
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

	public static final String get(String input, Predicate<DgrMLdapPolicy> pFunc //
			, Function<DgrMLdapPolicy, String> func) {
		for (DgrMLdapPolicy status : DgrMLdapPolicy.values()) {
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
