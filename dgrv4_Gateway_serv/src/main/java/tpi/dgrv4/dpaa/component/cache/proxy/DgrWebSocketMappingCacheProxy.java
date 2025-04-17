package tpi.dgrv4.dpaa.component.cache.proxy;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.esotericsoftware.kryo.Kryo;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.component.cache.core.DaoGenericCache;
import tpi.dgrv4.common.component.cache.proxy.DaoCacheProxy;
import tpi.dgrv4.entity.entity.DgrWebSocketMapping;
import tpi.dgrv4.entity.repository.DgrWebSocketMappingDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class DgrWebSocketMappingCacheProxy extends DaoCacheProxy {

	private DgrWebSocketMappingDao dgrWebSocketMappingDao;
	
	@Autowired
	public DgrWebSocketMappingCacheProxy(ObjectMapper objectMapper, DaoGenericCache cache,
			DgrWebSocketMappingDao dgrWebSocketMappingDao) {
		super(objectMapper, cache);
		this.dgrWebSocketMappingDao = dgrWebSocketMappingDao;
	}

	public Optional<DgrWebSocketMapping> findFirstBySiteName(String siteName) {
		Supplier<DgrWebSocketMapping> supplier = () -> {
			return getDgrWebSocketMappingDao().findFirstBySiteName(siteName);
		};
		return getOne("findFirstBySiteName", supplier, DgrWebSocketMapping.class, siteName);
	}

	@Override
	protected Class<?> getDaoClass() {
		return DgrWebSocketMappingDao.class;
	}
	
	@Override
	protected void kryoRegistration(Kryo kryo) {
		kryo.register(DgrWebSocketMapping.class);
	}

	protected DgrWebSocketMappingDao getDgrWebSocketMappingDao() {
		return dgrWebSocketMappingDao;
	}

	@Override
	protected Consumer<String> getTraceLogger() {
		return (String log) -> {
			TPILogger.tl.trace(log);
		};
	}
}
