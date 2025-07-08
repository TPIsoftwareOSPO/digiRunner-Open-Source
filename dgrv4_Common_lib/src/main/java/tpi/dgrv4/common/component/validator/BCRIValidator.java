package tpi.dgrv4.common.component.validator;

import tpi.dgrv4.common.keeper.ITPILogger;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;
import tpi.dgrv4.common.vo.BeforeControllerRespValue;

/**
 * 'BCRI' is the abbreviation of 'BeforeControllerRespItem'
 * @author Kim
 */
public abstract class BCRIValidator<V> implements BCRIValidatorIfs {

	private final BeforeControllerRespItem item;

	private final V fieldValue;
	
	protected static ITPILogger logger;

	protected BCRIValidator(BeforeControllerRespItem item, V fieldValue) {
		this.item = item;
		this.fieldValue = fieldValue;
	}
	
	@Override
	public void validate() {
		doValidate(this.item, this.fieldValue);
	}

	protected final <T> T getRespValue(BeforeControllerRespValue<T> respValue) {
		if (respValue != null) {
			return respValue.getValue();
		}
		return null;
	}	
	
	protected final String getWrappedFieldName() {
		return "{{" + this.item.getField() + "}}";
	}

	protected abstract void doValidate(BeforeControllerRespItem item, V fieldValue);
	
	public static void setLogger(ITPILogger logger) {
		BCRIValidator.logger = logger;
	}

}