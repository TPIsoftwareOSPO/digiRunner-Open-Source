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
import tpi.dgrv4.entity.entity.jpql.TsmpSecurityLevel;
import tpi.dgrv4.entity.repository.TsmpSecurityLevelDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class TsmpSecurityLevelCacheProxy extends DaoCacheProxy {

	private TsmpSecurityLevelDao tsmpSecurityLevelDao;

	@Autowired
	public TsmpSecurityLevelCacheProxy(ObjectMapper objectMapper, DaoGenericCache cache,
			TsmpSecurityLevelDao tsmpSecurityLevelDao) {
		super(objectMapper, cache);
		this.tsmpSecurityLevelDao = tsmpSecurityLevelDao;
	}

	public Optional<TsmpSecurityLevel> findById(String securityLevelId) {
		Supplier<TsmpSecurityLevel> supplier = () -> {
			Optional<TsmpSecurityLevel> opt = getTsmpSecurityLevelDao().findById(securityLevelId);
			return opt.orElse(null);
		};
		return getOne("findById", supplier, TsmpSecurityLevel.class, securityLevelId);
	}

	@Override
	protected Class<?> getDaoClass() {
		return TsmpSecurityLevelDao.class;
	}

	@Override
	protected void kryoRegistration(Kryo kryo) {
		kryo.register(TsmpSecurityLevel.class);
	}

	protected TsmpSecurityLevelDao getTsmpSecurityLevelDao() {
		return this.tsmpSecurityLevelDao;
	}

	@Override
	protected Consumer<String> getTraceLogger() {
		return (String log) -> {
			TPILogger.tl.trace(log);
		};
	}
}
