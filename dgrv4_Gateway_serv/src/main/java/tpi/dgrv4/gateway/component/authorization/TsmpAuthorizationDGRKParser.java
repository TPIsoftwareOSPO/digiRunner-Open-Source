package tpi.dgrv4.gateway.component.authorization;

import tpi.dgrv4.entity.entity.TsmpOpenApiKey;
import tpi.dgrv4.gateway.component.StaticResourceInitializer;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpOpenApiKeyCacheProxy;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

public class TsmpAuthorizationDGRKParser extends TsmpAuthorizationParserImpl {

	/**
	 * Injected by {@link StaticResourceInitializer}
	 */
	private static TsmpOpenApiKeyCacheProxy tsmpOpenApiKeyCacheProxy;

	private static TPILogger logger;

	public TsmpAuthorizationDGRKParser(String authorization) {
		super(authorization);
	}

	@Override
	public TsmpAuthorization parse() throws Exception {
		TsmpAuthorization auth = new TsmpAuthorization();
		if (this.infos.length == 2) {
			TsmpOpenApiKey entity = tsmpOpenApiKeyCacheProxy.findFirstByOpenApiKey(this.infos[0]);
			if (entity != null) {
				auth.setClientId(entity.getClientId());
				auth.setUserName("DGRK");
			} else {
				this.logger.debug("Client not found by api key: " + this.infos[0]);
			}
		}
		return auth;
	}

	public static void setTsmpOpenApiKeyCacheProxy(TsmpOpenApiKeyCacheProxy tsmpOpenApiKeyCacheProxy) {
		TsmpAuthorizationDGRKParser.tsmpOpenApiKeyCacheProxy = tsmpOpenApiKeyCacheProxy;
	}

	public static void setLogger(TPILogger logger) {
		TsmpAuthorizationDGRKParser.logger = logger;
	}

}