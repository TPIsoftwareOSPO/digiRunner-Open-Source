package tpi.dgrv4.common.component.validator;

import java.util.Optional;

public interface BCRIVFactoryIfs {

	BCRIValidatorIfs getValidator(Object obj);

	/**
	 * 檢查fieldType是否正確<br>
	 * 正確則回傳fieldType, 否則回傳正確的型別
	 * @param fieldType
	 * @return
	 */
	Optional<Class<?>> isFieldTypeCorrect(Class<?> fieldType);

}