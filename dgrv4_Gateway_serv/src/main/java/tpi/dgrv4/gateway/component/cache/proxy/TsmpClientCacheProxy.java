package tpi.dgrv4.gateway.component.cache.proxy;

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
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class TsmpClientCacheProxy extends DaoCacheProxy {

	private TsmpClientDao tsmpClientDao;

	@Autowired
	public TsmpClientCacheProxy(ObjectMapper objectMapper, DaoGenericCache cache, TsmpClientDao tsmpClientDao) {
		super(objectMapper, cache);
		this.tsmpClientDao = tsmpClientDao;
	}

	public Optional<TsmpClient> findById(String clientId) {
		Supplier<TsmpClient> supplier = () -> {
			Optional<TsmpClient> opt = getTsmpClientDao().findById(clientId);
			return opt.orElse(null);
		};
		
		return getOne("findById", supplier, TsmpClient.class, clientId);
	}
	
	public TsmpClient findFirstByClientId(String clientId) {
		Supplier<TsmpClient> supplier = () -> {
			TsmpClient vo = getTsmpClientDao().findFirstByClientId(clientId);
			return vo;
		};
		
		return getOne("findFirstByClientId", supplier, TsmpClient.class, clientId).orElse(null);
	}
	
	
	
	@Override
	protected Class<?> getDaoClass() {
		return TsmpClientDao.class;
	}

	@Override
	protected void kryoRegistration(Kryo kryo) {
		kryo.register(TsmpClient.class);
	}

	protected TsmpClientDao getTsmpClientDao() {
		return this.tsmpClientDao;
	}
	
	@Override
	protected Consumer<String> getTraceLogger() {
		return (String log) -> {
			TPILogger.tl.trace(log);
		};
	}
	
}