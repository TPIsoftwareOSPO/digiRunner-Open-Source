package tpi.dgrv4.common.component.validator;

import java.util.Locale;

import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;
import tpi.dgrv4.common.vo.BeforeControllerRespValue;

public abstract class BeforeControllerRespItemBaseBuilder<T extends BeforeControllerRespItemBaseBuilder<T>> //
	implements BeforeControllerRespItemBuilderIfs {

	private String locale;
	
	// default rtn code for 'isRequired' validation
	public static final String DEFAULT_RTN_CODE_IS_REQUIRED = TsmpDpAaRtnCode._2000.getCode();
	
	// common fields

	private String field;

	private String type;

	private BeforeControllerRespValueBuilderIfs<Boolean> isRequired;
	
	protected BeforeControllerRespItemBaseBuilder(String locale, String type) {
		this.locale = locale;
		this.type = type;
	}
	
	@SuppressWarnings("unchecked")
	public T field(String field) {
		this.field = field;
		return (T) this;
	}

	/**
	 * Using default rtn code
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T isRequired() {
		this.isRequired = new BeforeControllerRespValueBuilderSelector<Boolean>() //
			.buildByRtnCode(this.locale)
			.value(Boolean.TRUE)
			.rtnCode(DEFAULT_RTN_CODE_IS_REQUIRED)
			.params(new String[0]);
		return (T) this;
	}

	/**
	 * Specify rtn code and params
	 * @param rtnCode If null, then use {@link BeforeControllerRespItemBaseBuilder#DEFAULT_RTN_CODE_IS_REQUIRED}
	 * @param params
	 * @param locale If null, then use {@link Locale#getDefault()}
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T isRequired(String rtnCode, String[] params) {
		this.isRequired = new BeforeControllerRespValueBuilderSelector<Boolean>() //
			.buildByRtnCode(this.locale)
			.value(Boolean.TRUE)
			.rtnCode(StringUtils.isEmpty(rtnCode) ? DEFAULT_RTN_CODE_IS_REQUIRED : rtnCode)
			.params(params);
		return (T) this;
	}

	protected String getLocale() {
		return this.locale;
	}
	
	protected String getField() {
		return field;
	}
	
	protected String getType() {
		return type;
	}	

	protected BeforeControllerRespValue<Boolean> getIsRequired() {
		return buildValue(this.isRequired);
	}

	protected <V> BeforeControllerRespValue<V> buildValue(BeforeControllerRespValueBuilderIfs<V> builderIfs) {
		if (builderIfs != null) {
			return builderIfs.build();	// Validation is applied while building
		}
		return null;
	}
	
	@Override
	public BeforeControllerRespItem build() throws IllegalArgumentException {
		if (StringUtils.isEmpty(this.field)) {
			throw new IllegalArgumentException("field is empty");
		}
		if (StringUtils.isEmpty(this.type)) {
			throw new IllegalArgumentException("type is empty");
		}

		return doBuild();
	}
	
	protected abstract BeforeControllerRespItem doBuild();

}