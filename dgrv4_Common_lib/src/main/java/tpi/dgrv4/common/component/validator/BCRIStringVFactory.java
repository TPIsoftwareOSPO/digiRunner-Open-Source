package tpi.dgrv4.common.component.validator;

import java.lang.reflect.Field;
import java.util.Optional;

import tpi.dgrv4.common.vo.BeforeControllerRespItem;

/**
 * 'BCRI' is the abbreviation of 'BeforeControllerRespItem'
 * 'V' is abbreviation of 'Validator'
 * @author Kim
 */
public class BCRIStringVFactory extends BCRIVFactory {

	public BCRIStringVFactory(BeforeControllerRespItem item) {
		super(item);
	}
	
	@Override
	protected BCRIValidatorIfs createValidator(BeforeControllerRespItem item, Field field, Object obj)
			throws Exception {
		String fieldValue = (String) field.get(obj);
		return new BCRIStringValidator(item, fieldValue);
	}
	
	@Override
	public Optional<Class<?>> isFieldTypeCorrect(Class<?> fieldType) {
		if (String.class.equals(fieldType)) {
			return Optional.ofNullable(fieldType);
		}
		return Optional.of(String.class);
	}

}