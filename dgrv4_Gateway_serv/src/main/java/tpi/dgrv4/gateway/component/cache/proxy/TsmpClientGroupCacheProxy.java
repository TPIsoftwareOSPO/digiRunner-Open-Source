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
import tpi.dgrv4.entity.entity.TsmpClientGroup;
import tpi.dgrv4.entity.repository.TsmpClientGroupDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class TsmpClientGroupCacheProxy extends DaoCacheProxy {

	private TsmpClientGroupDao tsmpClientGroupDao;

	@Autowired
	public TsmpClientGroupCacheProxy(ObjectMapper objectMapper, DaoGenericCache cache,
			TsmpClientGroupDao tsmpClientGroupDao) {
		super(objectMapper, cache);
		this.tsmpClientGroupDao = tsmpClientGroupDao;
	}

	public List<TsmpClientGroup> findByClientId(String clientId) {
		Supplier<List<TsmpClientGroup>> supplier = () -> {
			List<TsmpClientGroup> list = getTsmpClientGroupDao().findByClientId(clientId);
			return list;
		};
		
		return getList("findByClientId", supplier,  clientId);
	}
	
	@Override
	protected Class<?> getDaoClass() {
		return TsmpClientGroupDao.class;
	}

	@Override
	protected void kryoRegistration(Kryo kryo) {
		kryo.register(TsmpClientGroup.class);
	}

	protected TsmpClientGroupDao getTsmpClientGroupDao() {
		return tsmpClientGroupDao;
	}

	@Override
	protected Consumer<String> getTraceLogger() {
		return (String log) -> {
			TPILogger.tl.trace(log);
		};
	}
	
}