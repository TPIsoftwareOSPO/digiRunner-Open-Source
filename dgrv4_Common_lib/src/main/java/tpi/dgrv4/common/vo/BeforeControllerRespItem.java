package tpi.dgrv4.common.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class BeforeControllerRespItem {

	// common fields

	private final String field;
	
	private final String type;

	@JsonInclude(value = Include.NON_NULL)
	private final BeforeControllerRespValue<Boolean> isRequired;

	// string type

	@JsonInclude(value = Include.NON_NULL)
	private final BeforeControllerRespValue<Integer> maxLength;

	@JsonInclude(value = Include.NON_NULL)
	private final BeforeControllerRespValue<Integer> minLength;

	@JsonInclude(value = Include.NON_NULL)
	private final BeforeControllerRespValue<String> pattern;

	// int type

	@JsonInclude(value = Include.NON_NULL)
	private final BeforeControllerRespValue<Long> min;

	@JsonInclude(value = Include.NON_NULL)
	private final BeforeControllerRespValue<Long> max;
	
	public BeforeControllerRespItem(String field, String type, BeforeControllerRespValue<Boolean> isRequired,
			BeforeControllerRespValue<Integer> maxLength, BeforeControllerRespValue<Integer> minLength,
			BeforeControllerRespValue<String> pattern, BeforeControllerRespValue<Long> min,
			BeforeControllerRespValue<Long> max) {
		super();
		this.field = field;
		this.type = type;
		this.isRequired = isRequired;
		this.minLength = minLength;
		this.maxLength = maxLength;
		this.pattern = pattern;
		this.min = min;
		this.max = max;
	}
	
	public String getField() {
		return field;
	}
	
	public String getType() {
		return type;
	}

	public BeforeControllerRespValue<Boolean> getIsRequired() {
		return isRequired;
	}

	public BeforeControllerRespValue<Integer> getMaxLength() {
		return maxLength;
	}

	public BeforeControllerRespValue<Integer> getMinLength() {
		return minLength;
	}

	public BeforeControllerRespValue<String> getPattern() {
		return pattern;
	}
	
	public BeforeControllerRespValue<Long> getMin() {
		return min;
	}

	public BeforeControllerRespValue<Long> getMax() {
		return max;
	}

}