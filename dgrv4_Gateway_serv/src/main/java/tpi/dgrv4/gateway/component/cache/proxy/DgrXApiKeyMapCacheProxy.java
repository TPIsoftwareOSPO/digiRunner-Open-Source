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
import tpi.dgrv4.entity.entity.DgrXApiKeyMap;
import tpi.dgrv4.entity.repository.DgrXApiKeyMapDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class DgrXApiKeyMapCacheProxy extends DaoCacheProxy {

	private DgrXApiKeyMapDao dgrXApiKeyMapDao;

	@Autowired
	public DgrXApiKeyMapCacheProxy(ObjectMapper objectMapper, DaoGenericCache cache,
			DgrXApiKeyMapDao dgrXApiKeyMapDao) {
		super(objectMapper, cache);
		this.dgrXApiKeyMapDao = dgrXApiKeyMapDao;
	}

	public List<DgrXApiKeyMap> findByRefApiKeyId(Long refApiKeyId) {
		Supplier<List<DgrXApiKeyMap>> supplier = () -> {
			List<DgrXApiKeyMap> list = getDgrXApiKeyMapDao().findByRefApiKeyId(refApiKeyId);
			return list;
		};

		return getList("findByRefApiKeyId", supplier, refApiKeyId);
	}

	@Override
	protected Class<?> getDaoClass() {
		return DgrXApiKeyMapDao.class;
	}

	@Override
	protected void kryoRegistration(Kryo kryo) {
		kryo.register(DgrXApiKeyMap.class);
	}

	protected DgrXApiKeyMapDao getDgrXApiKeyMapDao() {
		return dgrXApiKeyMapDao;
	}

	@Override
	protected Consumer<String> getTraceLogger() {
		return (String log) -> {
			TPILogger.tl.trace(log);
		};
	}
}