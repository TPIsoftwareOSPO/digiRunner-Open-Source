package tpi.dgrv4.gateway.component.cache.proxy;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.esotericsoftware.kryo.Kryo;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.component.cache.core.DaoGenericCache;
import tpi.dgrv4.common.component.cache.proxy.DaoCacheProxy;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpClientGroup;
import tpi.dgrv4.entity.entity.jpql.TsmpClientHost;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpClientHostDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class TsmpClientHostCacheProxy extends DaoCacheProxy {

	private TsmpClientHostDao tsmpClientHostDao;

	@Autowired
	public TsmpClientHostCacheProxy(ObjectMapper objectMapper, DaoGenericCache cache,
			TsmpClientHostDao tsmpClientHostDao) {
		super(objectMapper, cache);
		this.tsmpClientHostDao = tsmpClientHostDao;
	}

	public List<TsmpClientHost> findByClientId(String clientId) {
		Supplier<List<TsmpClientHost>> supplier = () -> {
			List<TsmpClientHost> list = getTsmpClientHostDao().findByClientId(clientId);
			return list;
		};
		
		return getList("findByClientId", supplier,  clientId);
	}
	
	@Override
	protected Class<?> getDaoClass() {
		return TsmpClientHostDao.class;
	}

	@Override
	protected void kryoRegistration(Kryo kryo) {
		kryo.register(TsmpClientHost.class);
	}

	protected TsmpClientHostDao getTsmpClientHostDao() {
		return this.tsmpClientHostDao;
	}
	
	@Override
	protected Consumer<String> getTraceLogger() {
		return (String log) -> {
			TPILogger.tl.trace(log);
		};
	}
	
}