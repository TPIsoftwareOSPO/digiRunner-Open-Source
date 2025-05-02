package tpi.dgrv4.common.component.validator;

import java.util.Locale;

import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;
import tpi.dgrv4.common.vo.BeforeControllerRespValue;

public class BeforeControllerRespItemStringBuilder extends //
	BeforeControllerRespItemBaseBuilder<BeforeControllerRespItemStringBuilder> {

	// default rtn code for 'maxLength' validation
	public static final String DEFAULT_RTN_CODE_MAX_LENGTH = TsmpDpAaRtnCode._2001.getCode();

	// default rtn code for 'minLength' validation
	public static final String DEFAULT_RTN_CODE_MIN_LENGTH = TsmpDpAaRtnCode._2002.getCode();	
	
	// default rtn code for 'pattern' validation
	public static final String DEFAULT_RTN_CODE_PATTERN = TsmpDpAaRtnCode._2007.getCode();
	
	private BeforeControllerRespValueBuilderIfs<Integer> maxLength;

	private BeforeControllerRespValueBuilderIfs<Integer> minLength;

	private BeforeControllerRespValueBuilderIfs<String> pattern;

	public BeforeControllerRespItemStringBuilder(String locale) {
		super(locale, "string");
	}
	
	/**
	 * Using default rtn code
	 * @param value
	 * @return
	 */
	public BeforeControllerRespItemStringBuilder maxLength(Integer value) {
		this.maxLength = new BeforeControllerRespValueBuilderSelector<Integer>() //
			.buildByRtnCode(getLocale())
			.value(value)
			.rtnCode(DEFAULT_RTN_CODE_MAX_LENGTH)
			.params(new String[0]);
		return this;
	}

	/**
	 * Specify rtn code and params
	 * @param value
	 * @param rtnCode If null, then use {@link BeforeControllerRespItemBaseBuilder#DEFAULT_RTN_CODE_MAX_LENGTH}
	 * @param params
	 * @param locale If null, then use {@link Locale#getDefault()}
	 * @return
	 */
	public BeforeControllerRespItemStringBuilder maxLength(Integer value, String rtnCode, String[] params) {
		this.maxLength = new BeforeControllerRespValueBuilderSelector<Integer>() //
			.buildByRtnCode(getLocale())
			.value(value)
			.rtnCode(StringUtils.isEmpty(rtnCode) ? DEFAULT_RTN_CODE_MAX_LENGTH : rtnCode)
			.params(params);
		return this;
	}
	
	/**
	 * Using default rtn code
	 * @param value
	 * @return
	 */
	public BeforeControllerRespItemStringBuilder minLength(Integer value) {
		this.minLength = new BeforeControllerRespValueBuilderSelector<Integer>() //
			.buildByRtnCode(getLocale())
			.value(value)
			.rtnCode(DEFAULT_RTN_CODE_MIN_LENGTH)
			.params(new String[0]);
		return this;
	}

	/**
	 * Using default rtn code
	 * @param value
	 * @return
	 */
	public BeforeControllerRespItemStringBuilder pattern(String value) {
		this.pattern = new BeforeControllerRespValueBuilderSelector<String>() //
			.buildByRtnCode(getLocale())
			.value(value)
			.rtnCode(DEFAULT_RTN_CODE_PATTERN)
			.params(new String[0]);
		return this;
	}

	/**
	 * Specify rtn code and params
	 * @param value
	 * @param rtnCode If null, then use {@link BeforeControllerRespItemBaseBuilder#DEFAULT_RTN_CODE_MIN_LENGTH}
	 * @param params
	 * @param locale If null, then use {@link Locale#getDefault()}
	 * @return
	 */
	public BeforeControllerRespItemStringBuilder minLength(Integer value, String rtnCode, String[] params) {
		this.minLength = new BeforeControllerRespValueBuilderSelector<Integer>() //
			.buildByRtnCode(getLocale())
			.value(value)
			.rtnCode(StringUtils.isEmpty(rtnCode) ? DEFAULT_RTN_CODE_MIN_LENGTH : rtnCode)
			.params(params);
		return this;
	}
	
	/**
	 * Specify rtn code and params
	 * @param value
	 * @param rtnCode If null, then use {@link BeforeControllerRespItemBaseBuilder#DEFAULT_RTN_CODE_PATTERN}
	 * @param params
	 * @param locale If null, then use {@link Locale#getDefault()}
	 * @return
	 */
	public BeforeControllerRespItemStringBuilder pattern(String value, String rtnCode, String[] params) {
		this.pattern = new BeforeControllerRespValueBuilderSelector<String>() //
			.buildByRtnCode(getLocale())
			.value(value)
			.rtnCode(StringUtils.isEmpty(rtnCode) ? DEFAULT_RTN_CODE_PATTERN : rtnCode)
			.params(params);
		return this;
	}
	
	@Override
	protected BeforeControllerRespItem doBuild() {
		BeforeControllerRespValue<Integer> maxLength = buildValue(this.maxLength);
		BeforeControllerRespValue<Integer> minLength = buildValue(this.minLength);
		BeforeControllerRespValue<String> pattern = buildValue(this.pattern);
		
		BeforeControllerRespItem item = new BeforeControllerRespItem(
			getField(),
			getType(),
			getIsRequired(),
			maxLength,
			minLength,
			pattern,
			null,
			null
		);
		return item;
	}

}