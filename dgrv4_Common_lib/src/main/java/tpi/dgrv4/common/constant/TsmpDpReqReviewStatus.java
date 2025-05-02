package tpi.dgrv4.common.constant;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * REVIEW_STATUS-簽核狀態
 * 搭配 TSMP_DP_ITEMS.SUBITEM_NO, with ITEM_NO = 'REVIEW_STATUS'
 * @author Kim
 */
public enum TsmpDpReqReviewStatus {
	WAIT1("WAIT1", "待審"),
	ACCEPT("ACCEPT", "同意"),
	DENIED("DENIED", "不同意"),
	RETURN("RETURN", "退回"),
	WAIT2("WAIT2", "待審(重送)"),
	END("END", "取消")
	;

	private String value;

	private String text;

	private TsmpDpReqReviewStatus(String value, String text) {
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
		return get(text, (rs) -> {
			return rs.text.equals(text);
		}, (rs) -> {
			return rs.value;
		}) ;
	}

	public static String text(String value) {
		return get(value, (rs) -> {
			return rs.value.equals(value);
		}, (rs) -> {
			return rs.text;
		}) ;
	}

	public static final String get(String input, Predicate<TsmpDpReqReviewStatus> pFunc //
			, Function<TsmpDpReqReviewStatus, String> func) {
		for(TsmpDpReqReviewStatus rs : TsmpDpReqReviewStatus.values()) {
			if (pFunc.test(rs)) {
				return func.apply(rs); 
			}
		}
		return input;
	}

}
