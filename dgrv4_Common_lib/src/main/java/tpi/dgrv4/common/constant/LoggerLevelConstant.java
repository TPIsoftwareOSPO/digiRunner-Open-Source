package tpi.dgrv4.common.constant;

import java.util.function.Function;
import java.util.function.Predicate;

public enum LoggerLevelConstant {
	ERROR("ERROR", 200),
	WARN("WARN", 300),
	INFO("INFO", 400),
	APILOG("LOGUUID", 450),
	DEBUG("DEBUG", 500),
	TRACE("TRACE", 600),
	ALL("ALL", Integer.MAX_VALUE)
	;
	
	private Integer value;

	private String text;

	private LoggerLevelConstant(String text, Integer value) {
		this.value = value;
		this.text = text;
	}
	
	public static Integer getValue(String text) {
		return get(text, (status) -> {
			return status.text.equals(text);
		}, (flag) -> {
			return flag.value;
		}) ;
	}
	
	public static final Integer get(String input, Predicate<LoggerLevelConstant> pFunc //
			, Function<LoggerLevelConstant, Integer> func) {
		for(LoggerLevelConstant status : LoggerLevelConstant.values()) {
			if (pFunc.test(status)) {
				return func.apply(status);
			}
		}
		return 0;
	}
	
	public static boolean isLevelValid(Integer logLevel) {
	    for (LoggerLevelConstant status : LoggerLevelConstant.values()) {
	        if (status.value().equals(logLevel)) {
	            return true;
	        }
	    }
	    return false;
	}

	public Integer value() {
		return this.value;
	}

	public String text() {
		return this.text;
	}

	
}
