package tpi.dgrv4.common.component.validator;

import java.lang.reflect.Field;
import java.util.Optional;

import tpi.dgrv4.common.vo.BeforeControllerRespItem;

/**
 * 'BCRI' is the abbreviation of 'BeforeControllerRespItem'
 * 'V' is abbreviation of 'Validator'
 * @author Kim
 */
public class BCRIIntVFactory extends BCRIVFactory {

	public BCRIIntVFactory(BeforeControllerRespItem item) {
		super(item);
	}

	@Override
	public Optional<Class<?>> isFieldTypeCorrect(Class<?> fieldType) {
		// 相容 Long 型態
		if (Integer.class.equals(fieldType) || Long.class.equals(fieldType)) {
			return Optional.ofNullable(fieldType);
		}
		return Optional.of(Integer.class);
	}
	
	@Override
	protected BCRIValidatorIfs createValidator(BeforeControllerRespItem item, Field field, Object obj)
			throws Exception {
		Class<?> fieldType = field.getType();
		if (Integer.class.equals(fieldType)) {
			Integer fieldValue = (Integer) field.get(obj);
			return new BCRIIntValidator(item, fieldValue);
		} else if (Long.class.equals(fieldType)) {
			Long fieldValue = (Long) field.get(obj);
			return new BCRILongValidator(item, fieldValue);
		}
		throw new Exception("錯誤的欄位型態，應為 Integer 或 Long: " + fieldType.getName());
	}

}