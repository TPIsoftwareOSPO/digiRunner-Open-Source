package tpi.dgrv4.gateway.component.cache.proxy;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.esotericsoftware.kryo.Kryo;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.component.cache.core.DaoGenericCache;
import tpi.dgrv4.common.component.cache.proxy.DaoCacheProxy;
import tpi.dgrv4.entity.entity.DgrXApiKey;
import tpi.dgrv4.entity.repository.DgrXApiKeyDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class DgrXApiKeyCacheProxy extends DaoCacheProxy {

	private DgrXApiKeyDao dgrXApiKeyDao;

	@Autowired
	public DgrXApiKeyCacheProxy(ObjectMapper objectMapper, DaoGenericCache cache, DgrXApiKeyDao dgrXApiKeyDao) {
		super(objectMapper, cache);
		this.dgrXApiKeyDao = dgrXApiKeyDao;
	}

	public DgrXApiKey findFirstByApiKeyEn(String xApikeyEn) {
		Supplier<DgrXApiKey> supplier = () -> {
			DgrXApiKey vo = getDgrXApiKeyDao().findFirstByApiKeyEn(xApikeyEn);
			return vo;
		};

		return getOne("findFirstByApiKeyEn", supplier, DgrXApiKey.class, xApikeyEn).orElse(null);
	}

	@Override
	protected Class<?> getDaoClass() {
		return DgrXApiKeyDao.class;
	}

	@Override
	protected void kryoRegistration(Kryo kryo) {
		kryo.register(DgrXApiKey.class);
	}

	protected DgrXApiKeyDao getDgrXApiKeyDao() {
		return dgrXApiKeyDao;
	}

	@Override
	protected Consumer<String> getTraceLogger() {
		return (String log) -> {
			TPILogger.tl.trace(log);
		};
	}
}
