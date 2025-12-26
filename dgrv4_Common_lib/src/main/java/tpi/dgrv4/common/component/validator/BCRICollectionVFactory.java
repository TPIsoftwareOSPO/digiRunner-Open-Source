package tpi.dgrv4.common.component.validator;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Optional;

import tpi.dgrv4.common.vo.BeforeControllerRespItem;

/**
 * 'BCRI' is the abbreviation of 'BeforeControllerRespItem'
 * 'V' is abbreviation of 'Validator'
 * @author Kim
 */
public class BCRICollectionVFactory extends BCRIVFactory {

	public BCRICollectionVFactory(BeforeControllerRespItem item) {
		super(item);
	}

	@Override
	public Optional<Class<?>> isFieldTypeCorrect(Class<?> fieldType) {
		if (Collection.class.isAssignableFrom(fieldType)) {
			return Optional.ofNullable(fieldType);
		}
		return Optional.of(Collection.class);
	}
	
	@Override
	protected BCRIValidatorIfs createValidator(BeforeControllerRespItem item, Field field, Object obj)
			throws Exception {
		Collection<?> fieldValue = (Collection<?>) field.get(obj);
		return new BCRICollectionValidator(item, fieldValue);
	}

}