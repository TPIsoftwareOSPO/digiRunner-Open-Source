package tpi.dgrv4.gateway.component.check;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import tpi.dgrv4.common.utils.ServiceUtil;
import tpi.dgrv4.entity.component.cache.proxy.TsmpRtnCodeCacheProxy;
import tpi.dgrv4.entity.entity.TsmpRtnCode;
import tpi.dgrv4.entity.exceptions.DgrError;
import tpi.dgrv4.entity.exceptions.DgrException;
import tpi.dgrv4.entity.exceptions.DgrModule;
import tpi.dgrv4.entity.exceptions.DgrRtnCode;


@Component
public class DgrRtnMsgBuilder {

	private TsmpRtnCodeCacheProxy tsmpRtnCodeCacheProxy;

	@Autowired
	public DgrRtnMsgBuilder(TsmpRtnCodeCacheProxy tsmpRtnCodeCacheProxy) {
		super();
		this.tsmpRtnCodeCacheProxy = tsmpRtnCodeCacheProxy;
	}

	public DgrException build(DgrException e, String locale) throws RuntimeException {
		return build(e.getError(), e.getParams(), locale, true);
	}

	public DgrException build(DgrException e, String locale, boolean isIgnore_1289) throws RuntimeException {
		return build(e.getError(), e.getParams(), locale, isIgnore_1289);
	}

	public DgrException build(DgrError<?> error, Map<String, String> params, String locale) {
		return build(error, params, locale, true);
	}

	public DgrException build(DgrError<?> error, Map<String, String> params, String locale, boolean isIgnore_1289) {
		// RTN_CODE
		String rtnCode = "";
		if (error == null) {
			error = DgrRtnCode.SYSTEM_ERROR;
		}
		rtnCode = error.getCode();

		// RTN_MSG
		String rtnMsg = "";
		TsmpRtnCode tsmpRtnCode = getTsmpRtnCodeCacheProxy().findById(rtnCode, locale).orElse(null);
		// 判斷代碼是否存在 TSMP_RTN_CODE 中
		if (tsmpRtnCode == null) {
			if (isIgnore_1289) {
				return createEx(error, rtnCode, error.getDefaultMessage());
			}

			// 如果連 1289 都找不到，就直接報錯
			if (DgrRtnCode._1289.getCode().equals(rtnCode)) {
				throw new RuntimeException(String.format("Missing '%s' code in TSMP_RTN_CODE!", rtnCode));
			}

			params = new HashMap<>();
			params.put("0", locale);
			params.put("1", rtnCode);
			return build(new DgrException(DgrRtnCode._1289, params), locale, false);
		} else {
			rtnMsg = tsmpRtnCode.getTsmpRtnMsg();
			// 取代變數
			if (!ObjectUtils.isEmpty(params)) {
				rtnMsg = ServiceUtil.buildContent(rtnMsg, params);
			}
		}

		// 重組訊息格式
		return createEx(error, rtnCode, rtnMsg);
	}

	private DgrException createEx(DgrError<?> oriError, String rtnCode, String rtnMsg) {
		return new DgrException(String.format("%s - %s", rtnCode, rtnMsg),
			new DgrException(new DgrError<DgrException>() {
				@Override
				public DgrModule getModule() {
					return oriError.getModule();
				}

				@Override
				public String getSeq() {
					return oriError.getSeq();
				}

				@Override
				public String getDefaultMessage() {
					return rtnMsg;
				}
			}, null));
	}

	protected TsmpRtnCodeCacheProxy getTsmpRtnCodeCacheProxy() {
		return tsmpRtnCodeCacheProxy;
	}

}
