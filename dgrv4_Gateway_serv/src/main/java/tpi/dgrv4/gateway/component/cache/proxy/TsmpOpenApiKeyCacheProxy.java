package tpi.dgrv4.gateway.component.cache.proxy;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.esotericsoftware.kryo.Kryo;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.component.cache.core.DaoGenericCache;
import tpi.dgrv4.common.component.cache.proxy.DaoCacheProxy;
import tpi.dgrv4.entity.entity.TsmpOpenApiKey;
import tpi.dgrv4.entity.repository.TsmpOpenApiKeyDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class TsmpOpenApiKeyCacheProxy extends DaoCacheProxy {

	private TsmpOpenApiKeyDao tsmpOpenApiKeyDao;

	@Autowired
	public TsmpOpenApiKeyCacheProxy(ObjectMapper objectMapper, DaoGenericCache cache,
			TsmpOpenApiKeyDao tsmpOpenApiKeyDao) {
		super(objectMapper, cache);
		this.tsmpOpenApiKeyDao = tsmpOpenApiKeyDao;
	}

	public TsmpOpenApiKey findFirstByOpenApiKey(String openApiKey) {
		Supplier<TsmpOpenApiKey> supplier = () -> {
			return getTsmpOpenApiKeyDao().findFirstByOpenApiKey(openApiKey);
		};
		return getOne("findFirstByOpenApiKey", supplier, TsmpOpenApiKey.class, openApiKey).orElse(null);
	}

	@Override
	protected Class<?> getDaoClass() {
		return TsmpOpenApiKeyDao.class;
	}

	@Override
	protected void kryoRegistration(Kryo kryo) {
		kryo.register(TsmpOpenApiKey.class);
	}

	protected TsmpOpenApiKeyDao getTsmpOpenApiKeyDao() {
		return this.tsmpOpenApiKeyDao;
	}

	@Override
	protected Consumer<String> getTraceLogger() {
		return (String log) -> {
			TPILogger.tl.trace(log);
		};
	}
}
