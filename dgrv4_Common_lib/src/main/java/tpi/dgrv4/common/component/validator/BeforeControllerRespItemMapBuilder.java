package tpi.dgrv4.common.component.validator;

import java.util.Locale;

import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;
import tpi.dgrv4.common.vo.BeforeControllerRespValue;

public class BeforeControllerRespItemMapBuilder extends //
	BeforeControllerRespItemBaseBuilder<BeforeControllerRespItemMapBuilder> {

	// default rtn code for 'min' validation
	public static final String DEFAULT_RTN_CODE_MIN = TsmpDpAaRtnCode._2009.getCode();

	// default rtn code for 'max' validation
	public static final String DEFAULT_RTN_CODE_MAX = TsmpDpAaRtnCode._2010.getCode();

	private BeforeControllerRespValueBuilderIfs<Long> min;

	private BeforeControllerRespValueBuilderIfs<Long> max;
	
	public BeforeControllerRespItemMapBuilder(String locale) {
		super(locale, "map");
	}

	/**
	 * Using default rtn code
	 * @param value
	 * @return
	 */
	public BeforeControllerRespItemMapBuilder min(Integer value) {
		this.min = new BeforeControllerRespValueBuilderSelector<Long>() //
			.buildByRtnCode(getLocale())
			.value(value.longValue())
			.rtnCode(DEFAULT_RTN_CODE_MIN)
			.params(new String[0]);
		return this;
	}
	
	@Override
	protected BeforeControllerRespItem doBuild() {
		BeforeControllerRespValue<Long> min = buildValue(this.min);
		BeforeControllerRespValue<Long> max = buildValue(this.max);
		
		return new BeforeControllerRespItem(
			getField(),
			getType(),
			getIsRequired(),
			null,
			null,
			null,
			min,
			max
		);
	}

	/**
	 * Using default rtn code
	 * @param value
	 * @return
	 */
	public BeforeControllerRespItemMapBuilder max(Integer value) {
		this.max = new BeforeControllerRespValueBuilderSelector<Long>() //
			.buildByRtnCode(getLocale())
			.value(value.longValue())
			.rtnCode(DEFAULT_RTN_CODE_MAX)
			.params(new String[0]);
		return this;
	}
	
	/**
	 * Specify rtn code and params
	 * @param value
	 * @param rtnCode If null, then use {@link BeforeControllerRespItemBaseBuilder#DEFAULT_RTN_CODE_MIN}
	 * @param params
	 * @param locale If null, then use {@link Locale#getDefault()}
	 * @return
	 */
	public BeforeControllerRespItemMapBuilder min(Integer value, String rtnCode, String[] params) {
		this.min = new BeforeControllerRespValueBuilderSelector<Long>() //
			.buildByRtnCode(getLocale())
			.value(value.longValue())
			.rtnCode(StringUtils.isEmpty(rtnCode) ? DEFAULT_RTN_CODE_MIN : rtnCode)
			.params(params);
		return this;
	}

	/**
	 * Specify rtn code and params
	 * @param value
	 * @param rtnCode If null, then use {@link BeforeControllerRespItemBaseBuilder#DEFAULT_RTN_CODE_MAX}
	 * @param params
	 * @param locale If null, then use {@link Locale#getDefault()}
	 * @return
	 */
	public BeforeControllerRespItemMapBuilder max(Integer value, String rtnCode, String[] params) {
		this.max = new BeforeControllerRespValueBuilderSelector<Long>() //
			.buildByRtnCode(getLocale())
			.value(value.longValue())
			.rtnCode(StringUtils.isEmpty(rtnCode) ? DEFAULT_RTN_CODE_MAX : rtnCode)
			.params(params);
		return this;
	}

	

}