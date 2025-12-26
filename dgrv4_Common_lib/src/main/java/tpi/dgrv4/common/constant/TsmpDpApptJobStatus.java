package tpi.dgrv4.common.constant;

import java.util.function.Function;
import java.util.function.Predicate;

/** 排程狀態 (tsmp_dp_items.item_no = 'JOB_STATUS') */
public enum TsmpDpApptJobStatus {
	WAIT("W", "等待"),
	RUNNING("R", "執行中"),
	ERROR("E", "失敗"),
	DONE("D", "完成"),
	CANCEL("C", "取消"),
	ALL("A", "全部")
	;
	
	private String value;

	private String text;

	private TsmpDpApptJobStatus(String value, String text) {
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
		}) ;
	}

	public static String getText(String value) {
		return get(value, (status) -> {
			return status.value.equals(value);
		}, (flag) -> {
			return flag.text;
		}) ;
	}

	public static final String get(String input, Predicate<TsmpDpApptJobStatus> pFunc //
			, Function<TsmpDpApptJobStatus, String> func) {
		for(TsmpDpApptJobStatus status : TsmpDpApptJobStatus.values()) {
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
