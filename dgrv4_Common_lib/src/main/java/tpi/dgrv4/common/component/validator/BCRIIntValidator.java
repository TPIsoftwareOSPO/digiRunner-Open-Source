package tpi.dgrv4.common.component.validator;

import java.util.Objects;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

/**
 * 'BCRI' is the abbreviation of 'BeforeControllerRespItem'
 * @author Kim
 */
public class BCRIIntValidator extends BCRIValidator<Integer> {

	public BCRIIntValidator(BeforeControllerRespItem item, Integer fieldValue) {
		super(item, fieldValue);
	}

	@Override
	protected void doValidate(BeforeControllerRespItem item, Integer fieldValue) {
		// 是否必填
		checkIsRequired(item, fieldValue);
		
		// 數值範圍
		checkMin(item, fieldValue);
		checkMax(item, fieldValue);
	}

	private void checkIsRequired(BeforeControllerRespItem item, Integer fieldValue) {
		Boolean isRequired = getRespValue(item.getIsRequired());
		if (!Objects.isNull(isRequired) && isRequired && Objects.isNull(fieldValue)) {
			throw TsmpDpAaRtnCode._1350.throwing(getWrappedFieldName());
		}
	}

	private void checkMin(BeforeControllerRespItem item, Integer fieldValue) {
		Long min = getRespValue(item.getMin());
		if (min != null) {
			// The int will be implicitly converted to a long.
			if (fieldValue == null || (fieldValue != null && fieldValue < min)) {
				throw TsmpDpAaRtnCode._1355.throwing(getWrappedFieldName(), String.valueOf(min), String.valueOf(fieldValue));
			}
		}
	}

	private void checkMax(BeforeControllerRespItem item, Integer fieldValue) {
		Long max = getRespValue(item.getMax());
		if (max != null) {
			// The int will be implicitly converted to a long.
			if (fieldValue != null && fieldValue > max) {
				throw TsmpDpAaRtnCode._1356.throwing(getWrappedFieldName(), String.valueOf(max), String.valueOf(fieldValue));
			}
		}
	}

}