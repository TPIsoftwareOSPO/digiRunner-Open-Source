package tpi.dgrv4.gateway.constant;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * SSO AC IdP 使用者的狀態
 */
public enum DgrAcIdpUserStatus {
	REQUEST("1", "Request"), 
	ALLOW("2", "Allow"), 
	DENY("3", "Deny");

	private String value;

	private String text;

	private DgrAcIdpUserStatus(String value, String text) {
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

	public static final String get(String input, Predicate<DgrAcIdpUserStatus> pFunc //
			, Function<DgrAcIdpUserStatus, String> func) {
		for (DgrAcIdpUserStatus status : DgrAcIdpUserStatus.values()) {
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
