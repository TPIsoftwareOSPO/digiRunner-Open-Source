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
import tpi.dgrv4.entity.entity.TsmpGroupApi;
import tpi.dgrv4.entity.repository.TsmpGroupApiDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class TsmpGroupApiCacheProxy extends DaoCacheProxy {

	private TsmpGroupApiDao tsmpGroupApiDao;

	@Autowired
	public TsmpGroupApiCacheProxy(ObjectMapper objectMapper, DaoGenericCache cache, TsmpGroupApiDao tsmpGroupApiDao) {
		super(objectMapper, cache);
		this.tsmpGroupApiDao = tsmpGroupApiDao;
	}

	public List<TsmpGroupApi> findByApiKeyAndModuleName(String apiId, String moduleName) {
		Supplier<List<TsmpGroupApi>> supplier = () -> {
			List<TsmpGroupApi> list = getTsmpGroupApiDao().findByApiKeyAndModuleName(apiId, moduleName);
			return list;
		};
		return getList("findByApiKeyAndModuleName", supplier, apiId, moduleName);
	}

	@Override
	protected Class<?> getDaoClass() {
		return TsmpGroupApiDao.class;
	}

	@Override
	protected void kryoRegistration(Kryo kryo) {
		kryo.register(TsmpGroupApi.class);
	}

	protected TsmpGroupApiDao getTsmpGroupApiDao() {
		return tsmpGroupApiDao;
	}

	@Override
	protected Consumer<String> getTraceLogger() {
		return (String log) -> {
			TPILogger.tl.trace(log);
		};
	}
	
}