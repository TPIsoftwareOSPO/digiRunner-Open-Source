package tpi.dgrv4.gateway.component.check;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.entity.TsmpRtnCode;
import tpi.dgrv4.entity.exceptions.DgrRtnCode;
import tpi.dgrv4.entity.exceptions.ICheck;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpApiCacheProxy;
import tpi.dgrv4.gateway.service.CommForwardProcService;
import tpi.dgrv4.gateway.service.TsmpRtnCodeService;
import tpi.dgrv4.gateway.service.TsmpSettingService;

@Component
public class ApiStatusCheck implements ICheck{

	private TsmpSettingService tsmpSettingService;
	private TsmpApiCacheProxy tsmpApiCacheProxy;
	private TsmpRtnCodeService tsmpRtnCodeService;
	private CommForwardProcService commForwardProcService;
		
	@Autowired
	public ApiStatusCheck(TsmpSettingService tsmpSettingService, TsmpApiCacheProxy tsmpApiCacheProxy,
			TsmpRtnCodeService tsmpRtnCodeService, CommForwardProcService commForwardProcService) {
		super();
		this.tsmpSettingService = tsmpSettingService;
		this.tsmpApiCacheProxy = tsmpApiCacheProxy;
		this.tsmpRtnCodeService = tsmpRtnCodeService;
		this.commForwardProcService = commForwardProcService;
	}

	public boolean check(String apiKey, String moduleName) {
		boolean isEnabled = getTsmpSettingService().getVal_CHECK_API_STATUS_ENABLE();
		if(isEnabled) {
			TsmpApiId id = new TsmpApiId(apiKey, moduleName);
			Optional<TsmpApi> vo = getTsmpApiCacheProxy().findById(id);
			if(vo .isPresent() && "1".equals(vo.get().getApiStatus())) {
				return true;
			}
		}
		return false;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected TsmpApiCacheProxy getTsmpApiCacheProxy() {
		return tsmpApiCacheProxy;
	}
	
	protected TsmpRtnCodeService getTsmpRtnCodeService() {
		return this.tsmpRtnCodeService;
	}
	
	@Override
	public DgrRtnCode getRtnCode() {
		return DgrRtnCode._9912;
	}

	@Override
	public String getMessage(String locale) {
		TsmpRtnCode apiStatus_tsmpRtnCode = getTsmpRtnCodeService().findById(getRtnCode().getCode(), locale);
		if (apiStatus_tsmpRtnCode!=null) {
			return apiStatus_tsmpRtnCode.getTsmpRtnMsg();	
		}else {
			return getRtnCode().getDefaultMessage();
		}
	}
	
	protected CommForwardProcService getCommForwardProcService() {
		return commForwardProcService;
	}
}
