package tpi.dgrv4.dpaa.component;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import tpi.dgrv4.common.component.validator.BeforeControllerRespValueRtnCodeBuilder;
import tpi.dgrv4.dpaa.service.TsmpAuthorizationService;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.component.cache.proxy.TsmpRtnCodeCacheProxy;
import tpi.dgrv4.entity.component.cipher.TsmpTAEASKHelper;
import tpi.dgrv4.entity.daoService.OpenApiKeyService;
import tpi.dgrv4.entity.repository.TsmpRtnCodeDao;
import tpi.dgrv4.gateway.service.DPB0115Service;
import tpi.dgrv4.gateway.service.TsmpSettingService;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Component
public class DpaaStaticResourceInitializer {

	private TsmpRtnCodeDao tsmpRtnCodeDao;
	private DPB0115Service dpb0115Service;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private TsmpRtnCodeCacheProxy tsmpRtnCodeCacheProxy;
	private TsmpSettingService gatewayTsmpSettingService;
	private TsmpAuthorizationService tsmpAuthorizationService;
	
	@Autowired
	public DpaaStaticResourceInitializer(TsmpRtnCodeDao tsmpRtnCodeDao, DPB0115Service dpb0115Service,
			TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, TsmpRtnCodeCacheProxy tsmpRtnCodeCacheProxy,
			TsmpSettingService gatewayTsmpSettingService, TsmpAuthorizationService tsmpAuthorizationService) {
		super();
		this.tsmpRtnCodeDao = tsmpRtnCodeDao;
		this.dpb0115Service = dpb0115Service;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.tsmpRtnCodeCacheProxy = tsmpRtnCodeCacheProxy;
		this.gatewayTsmpSettingService = gatewayTsmpSettingService;
		this.tsmpAuthorizationService = tsmpAuthorizationService;
	}

	@PostConstruct
	public void init() {
		BeforeControllerRespValueRtnCodeBuilder.setTsmpRtnCodeDao(getTsmpRtnCodeDao());
		ControllerUtil.setDPB0115Service(getDPB0115Service());
		ServiceUtil.setTsmpDpItemsCacheProxy(getTsmpDpItemsCacheProxy());
		ServiceUtil.setTsmpRtnCodeCacheProxy(getTsmpRtnCodeCacheProxy());
		OpenApiKeyService.setTsmpDpItemsCacheProxy(getTsmpDpItemsCacheProxy());
		TsmpAuthorization.setTsmpSettingService(getGatewayTsmpSettingService());
		TsmpAuthorization.setTsmpAuthorizationService(getTsmpAuthorizationService());
	}

	protected TsmpRtnCodeDao getTsmpRtnCodeDao() {
		return this.tsmpRtnCodeDao;
	}
	
	protected DPB0115Service getDPB0115Service() {
		return this.dpb0115Service;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return this.tsmpDpItemsCacheProxy;
	}

	protected TsmpRtnCodeCacheProxy getTsmpRtnCodeCacheProxy() {
		return this.tsmpRtnCodeCacheProxy;
	}

	protected TsmpSettingService getGatewayTsmpSettingService() {
		return this.gatewayTsmpSettingService;
	}
	
	public TsmpAuthorizationService getTsmpAuthorizationService() {
		return tsmpAuthorizationService;
	}

}
