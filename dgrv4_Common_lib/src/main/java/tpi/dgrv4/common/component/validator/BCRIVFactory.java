package tpi.dgrv4.common.component.validator;

import java.lang.reflect.Field;
import java.util.Optional;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.keeper.ITPILogger;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

/**
 * 'BCRIV' is the abbreviation of 'BeforeControllerRespItemValidator'
 * @author Kim
 */
public abstract class BCRIVFactory implements BCRIVFactoryIfs {

	private static ITPILogger logger;

	private final BeforeControllerRespItem item;

	public BCRIVFactory(BeforeControllerRespItem item) {
		this.item = item;
	}

	@Override
	public BCRIValidatorIfs getValidator(Object obj) {
		try {
			String fieldName = this.item.getField();
			Field field = obj.getClass().getDeclaredField(fieldName);
			
			// 驗證欄位資料型態與Item所指定的type是否相符
			checkFieldType(field.getType());
			
			field.setAccessible(true);
			return createValidator(this.item, field, obj);
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.debug("" + e);
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}

	private void checkFieldType(Class<?> actualFieldType) throws Exception {
		Optional<Class<?>> opt = isFieldTypeCorrect(actualFieldType);
		if (!opt.isPresent()) {
			throw new Exception("未正確執行資料型別判定");
		}
		Class<?> expectedFieldType = opt.get();
		if (!actualFieldType.equals(expectedFieldType)) {
			String expectedType = expectedFieldType.getSimpleName();
			String actualType = actualFieldType.getSimpleName();
			throw new Exception("限制式型別(" + expectedType + ")與實際資料型別(" + actualType + ")不符");
		}
	}

	protected abstract BCRIValidatorIfs createValidator(BeforeControllerRespItem item, Field field, Object obj) //
		throws Exception;
	
	public static void setLogger(ITPILogger logger) {
		BCRIVFactory.logger = logger;
	}

}