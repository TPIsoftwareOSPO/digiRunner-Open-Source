package tpi.dgrv4.common.component.validator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.LocaleType;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.ControllerUtil;
import tpi.dgrv4.common.vo.BeforeControllerRespValue;
import tpi.dgrv4.entity.entity.ITsmpRtnCode;
import tpi.dgrv4.entity.repository.ITsmpRtnCodeDao;

/**
 * 設定 RtnCode 以組出 Msg
 * @author Kim
 *
 * @param <V>
 */
public class BeforeControllerRespValueRtnCodeBuilder<V> extends //
	BeforeControllerRespValueBaseBuilder<V, BeforeControllerRespValueRtnCodeBuilder<V>> {

	/**
	 * Injected by {@link DpaaStaticResourceInitializer}
	 */
	private static ITsmpRtnCodeDao dao;

	private String locale;

	private String rtnCode;

	private String[] params;

	public static final void setTsmpRtnCodeDao(ITsmpRtnCodeDao tsmpRtnCodeDao) {
		dao = tsmpRtnCodeDao;
	}
	
	public BeforeControllerRespValueRtnCodeBuilder(String locale) {
		this.locale = ControllerUtil.getLocale(locale);
	}

	public BeforeControllerRespValueRtnCodeBuilder<V> rtnCode(String rtnCode) {
		this.rtnCode = rtnCode;
		return this;
	}

	public BeforeControllerRespValueRtnCodeBuilder<V> params(String... params) {
		this.params = params;
		return this;
	}

	private String composeMsg() {
		ITsmpRtnCode tsmpRtnCode = getTsmpRtnCodeById(this.rtnCode, this.locale);
		if (tsmpRtnCode == null) {
			TsmpDpAaRtnCode._1289.throwing(this.locale, this.rtnCode);
			return "";
		}
		
		// 如果沒有設定 params 則預設第一個變數為 value
		Map<String, String> templateParams = new HashMap<>();
		if (this.params != null && this.params.length > 0) {
			for (int i = 0; i < this.params.length; i++) {
				templateParams.put(String.valueOf(i), this.params[i]);
			}
		} else {
			templateParams.put("0", String.valueOf(getValue()));
		}
		
		String msgTemplate = tsmpRtnCode.getTsmpRtnMsg();	
		return ControllerUtil.buildContent(msgTemplate, templateParams);
	}
	
	@Override
	protected BeforeControllerRespValue<V> doBuild() {
		if (StringUtils.isEmpty(this.rtnCode)) {
			throw new IllegalArgumentException("rtnCode is empty");
		}
		if (StringUtils.isEmpty(this.locale)) {
			throw new IllegalArgumentException("locale is empty");
		}
		final String msg = composeMsg();
		return new BeforeControllerRespValue<>(getValue(), msg);
	}

	private ITsmpRtnCode getTsmpRtnCodeById(String rtnCode, String locale) {
		Optional<ITsmpRtnCode> opt = dao.findByTsmpRtnCodeAndLocale(rtnCode, locale);
		if(!opt.isPresent() && !LocaleType.EN_US.equals(locale)) {
			opt = dao.findByTsmpRtnCodeAndLocale(rtnCode, LocaleType.EN_US);
		}
		
		return opt.orElse(null);
		
	}

}