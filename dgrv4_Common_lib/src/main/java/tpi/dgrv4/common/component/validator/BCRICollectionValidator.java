package tpi.dgrv4.common.component.validator;

import java.util.Collection;

import org.springframework.util.CollectionUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

/**
 * 'BCRI' is the abbreviation of 'BeforeControllerRespItem'
 * @author Kim
 */
public class BCRICollectionValidator extends BCRIValidator<Collection<?>> {

	public BCRICollectionValidator(BeforeControllerRespItem item, Collection<?> fieldValue) {
		super(item, fieldValue);
	}

	@Override
	protected void doValidate(BeforeControllerRespItem item, Collection<?> fieldValue) {
		// 是否必填
		checkIsRequired(item, fieldValue);
		
		// 數量範圍
		checkMin(item, fieldValue);
		checkMax(item, fieldValue);
	}

	private void checkIsRequired(BeforeControllerRespItem item, Collection<?> fieldValue) {
		Boolean isRequired = getRespValue(item.getIsRequired());
		if (isRequired != null && isRequired && CollectionUtils.isEmpty(fieldValue)) {
			throw TsmpDpAaRtnCode._1350.throwing(getWrappedFieldName());
		}
	}

	private void checkMin(BeforeControllerRespItem item, Collection<?> fieldValue) {
		Long min = getRespValue(item.getMin());
		if (min != null) {
			int size = (fieldValue == null ? 0 : fieldValue.size());
			// The int will be implicitly converted to a long.
			if (size < min) {
				throw TsmpDpAaRtnCode._1406.throwing(getWrappedFieldName(), String.valueOf(min), String.valueOf(size));
			}
		}
	}

	private void checkMax(BeforeControllerRespItem item, Collection<?> fieldValue) {
		Long max = getRespValue(item.getMax());
		if (max != null) {
			int size = (fieldValue == null ? 0 : fieldValue.size());
			// The int will be implicitly converted to a long.
			if (size > max) {
				throw TsmpDpAaRtnCode._1407.throwing(getWrappedFieldName(), String.valueOf(max), String.valueOf(size));
			}
		}
	}

}