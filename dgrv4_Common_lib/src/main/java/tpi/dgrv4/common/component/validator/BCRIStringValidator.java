package tpi.dgrv4.common.component.validator;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;


/**
 * 'BCRI' is the abbreviation of 'BeforeControllerRespItem'
 * @author Kim
 */
public class BCRIStringValidator extends BCRIValidator<String> {

	public BCRIStringValidator(BeforeControllerRespItem item, String fieldValue) {
		super(item, fieldValue);
	}

	@Override
	protected void doValidate(BeforeControllerRespItem item, String fieldValue) {
		// 是否必填
		checkIsRequired(item, fieldValue);
		
		// 最大長度
		checkMaxLength(item, fieldValue);

		// 最小長度
		checkMinLength(item, fieldValue);
		
		// 格式
		checkPattern(item, fieldValue);
	}

	private void checkIsRequired(BeforeControllerRespItem item, String fieldValue) {
		Boolean isRequired = getRespValue(item.getIsRequired());
		if (!Objects.isNull(isRequired)) {
			if (isRequired && StringUtils.isEmpty(fieldValue)) {
				throw TsmpDpAaRtnCode._1350.throwing(getWrappedFieldName());
			}
		}
	}

	private void checkMaxLength(BeforeControllerRespItem item, String fieldValue) {
		Integer maxLength = getRespValue(item.getMaxLength());
		if (maxLength != null) {
			if (fieldValue != null && fieldValue.length() > maxLength) {
				throw TsmpDpAaRtnCode._1351.throwing(getWrappedFieldName(), //
					String.valueOf(maxLength), String.valueOf(fieldValue.length()));
			}
		}
	}

	private void checkMinLength(BeforeControllerRespItem item, String fieldValue) {
		Integer minLength = getRespValue(item.getMinLength());
		if (minLength != null) {
			String fieldLength = "0";
			if (fieldValue != null) {
				fieldLength = fieldValue.length() + "";
			}
			
			if (StringUtils.isEmpty(fieldValue) || (!StringUtils.isEmpty(fieldValue)) && fieldValue.length() < minLength) {
				throw TsmpDpAaRtnCode._1384.throwing(getWrappedFieldName(), String.valueOf(minLength), fieldLength);
			}
		}
	}

	private void checkPattern(BeforeControllerRespItem item, String fieldValue) {
		String regex = getRespValue(item.getPattern());
		// 欄位有值時才檢查pattern, 否則與isRequired相抵觸
		if (!StringUtils.isEmpty(regex) && !StringUtils.isEmpty(fieldValue)) {
			Pattern p = null;
			try {
				p = Pattern.compile(regex);
			} catch (Exception e) {
				this.logger.debug("轉換正則表達式失敗: " + regex);
				throw TsmpDpAaRtnCode._1297.throwing();
			}
			
			Matcher m = p.matcher(fieldValue);
			if (!m.matches()) {
				throw TsmpDpAaRtnCode._1352.throwing(getWrappedFieldName());
			}
		}
	}

}