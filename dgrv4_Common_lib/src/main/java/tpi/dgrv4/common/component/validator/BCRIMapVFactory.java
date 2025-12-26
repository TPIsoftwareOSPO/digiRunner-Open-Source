package tpi.dgrv4.common.component.validator;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;

import tpi.dgrv4.common.vo.BeforeControllerRespItem;

/**
 * 'BCRI' is the abbreviation of 'BeforeControllerRespItem'
 * 'V' is abbreviation of 'Validator'
 * @author Kim
 */
public class BCRIMapVFactory extends BCRIVFactory {

	public BCRIMapVFactory(BeforeControllerRespItem item) {
		super(item);
	}

	@Override
	public Optional<Class<?>> isFieldTypeCorrect(Class<?> fieldType) {
		if (Map.class.isAssignableFrom(fieldType)) {
			return Optional.ofNullable(fieldType);
		}
		return Optional.of(Map.class);
	}
	
	@Override
	protected BCRIValidatorIfs createValidator(BeforeControllerRespItem item, Field field, Object obj)
			throws Exception {
		Map<?, ?> fieldValue = (Map<?, ?>) field.get(obj);
		return new BCRIMapValidator(item, fieldValue);
	}

}