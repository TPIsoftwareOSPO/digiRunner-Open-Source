package tpi.dgrv4.common.component.validator;

import java.util.Map;

import org.springframework.util.CollectionUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

/**
 * 'BCRI' is the abbreviation of 'BeforeControllerRespItem'
 * @author Kim
 */
// REVIEW TODO BCRIMapVFactory, BCRICollectionVFactory應該也要支援這種形式
// REVIEW TODO ex: field("list[0]"), field("list[0].name"), field("map.key")
public class BCRIMapValidator extends BCRIValidator<Map<?, ?>> {

	public BCRIMapValidator(BeforeControllerRespItem item, Map<?, ?> fieldValue) {
		super(item, fieldValue);
	}

	@Override
	protected void doValidate(BeforeControllerRespItem item, Map<?, ?> fieldValue) {
		// 是否必填
		checkIsRequired(item, fieldValue);
		
		// 數量範圍
		checkMin(item, fieldValue);
		checkMax(item, fieldValue);
	}

	private void checkIsRequired(BeforeControllerRespItem item, Map<?, ?> fieldValue) {
		Boolean isRequired = getRespValue(item.getIsRequired());
		if (isRequired != null && isRequired && CollectionUtils.isEmpty(fieldValue)) {
			throw TsmpDpAaRtnCode._1350.throwing(getWrappedFieldName());
		}
	}

	private void checkMin(BeforeControllerRespItem item, Map<?, ?> fieldValue) {
		Long min = getRespValue(item.getMin());
		if (min != null) {
			int size = (fieldValue == null ? 0 : fieldValue.size());
			// The int will be implicitly converted to a long.
			if (size < min) {
				throw TsmpDpAaRtnCode._1406.throwing(getWrappedFieldName(), String.valueOf(min), String.valueOf(size));
			}
		}
	}

	private void checkMax(BeforeControllerRespItem item, Map<?, ?> fieldValue) {
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