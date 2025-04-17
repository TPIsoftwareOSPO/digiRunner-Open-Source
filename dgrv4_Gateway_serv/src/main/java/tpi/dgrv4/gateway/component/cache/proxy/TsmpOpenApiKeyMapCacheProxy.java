package tpi.dgrv4.gateway.component.cache.proxy;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.esotericsoftware.kryo.Kryo;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.component.cache.core.DaoGenericCache;
import tpi.dgrv4.common.component.cache.proxy.DaoCacheProxy;
import tpi.dgrv4.entity.entity.TsmpOpenApiKeyMap;
import tpi.dgrv4.entity.repository.TsmpOpenApiKeyMapDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class TsmpOpenApiKeyMapCacheProxy extends DaoCacheProxy {

	private TsmpOpenApiKeyMapDao tsmpOpenApiKeyMapDao;

	@Autowired
	public TsmpOpenApiKeyMapCacheProxy(ObjectMapper objectMapper, DaoGenericCache cache,
			TsmpOpenApiKeyMapDao tsmpOpenApiKeyMapDao) {
		super(objectMapper, cache);
		this.tsmpOpenApiKeyMapDao = tsmpOpenApiKeyMapDao;
	}

	public List<TsmpOpenApiKeyMap> findByRefOpenApiKeyId(Long openApiKeyId) {
		Supplier<List<TsmpOpenApiKeyMap>> supplier = () -> {
			List<TsmpOpenApiKeyMap> list = getTsmpOpenApiKeyMapDao().findByRefOpenApiKeyId(openApiKeyId);
			return list;
		};
		
		return getList("findByRefOpenApiKeyId", supplier,  openApiKeyId);
	}
	
	@Override
	protected Class<?> getDaoClass() {
		return TsmpOpenApiKeyMapDao.class;
	}

	@Override
	protected void kryoRegistration(Kryo kryo) {
		kryo.register(TsmpOpenApiKeyMap.class);
	}

	protected TsmpOpenApiKeyMapDao getTsmpOpenApiKeyMapDao() {
		return tsmpOpenApiKeyMapDao;
	}

	@Override
	protected Consumer<String> getTraceLogger() {
		return (String log) -> {
			TPILogger.tl.trace(log);
		};
	}
	
}