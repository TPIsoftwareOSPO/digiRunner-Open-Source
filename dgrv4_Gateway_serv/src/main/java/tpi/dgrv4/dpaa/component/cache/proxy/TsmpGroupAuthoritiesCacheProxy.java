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
import tpi.dgrv4.entity.entity.TsmpGroupAuthorities;
import tpi.dgrv4.entity.repository.TsmpGroupAuthoritiesDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class TsmpGroupAuthoritiesCacheProxy extends DaoCacheProxy {

	private TsmpGroupAuthoritiesDao tsmpGroupAuthoritiesDao;

	@Autowired
	public TsmpGroupAuthoritiesCacheProxy(ObjectMapper objectMapper, DaoGenericCache cache,
			TsmpGroupAuthoritiesDao tsmpGroupAuthoritiesDao) {
		super(objectMapper, cache);
		this.tsmpGroupAuthoritiesDao = tsmpGroupAuthoritiesDao;
	}

	public Optional<TsmpGroupAuthorities> findById(String groupAuthoritieId) {
		Supplier<TsmpGroupAuthorities> supplier = () -> {
			Optional<TsmpGroupAuthorities> opt = getTsmpGroupAuthoritiesDao().findById(groupAuthoritieId);
			return opt.orElse(null);
		};
		return getOne("findById", supplier, TsmpGroupAuthorities.class, groupAuthoritieId);
	}

	@Override
	protected Class<?> getDaoClass() {
		return TsmpGroupAuthoritiesDao.class;
	}

	@Override
	protected void kryoRegistration(Kryo kryo) {
		kryo.register(TsmpGroupAuthorities.class);
	}

	protected TsmpGroupAuthoritiesDao getTsmpGroupAuthoritiesDao() {
		return this.tsmpGroupAuthoritiesDao;
	}
	
	@Override
	protected Consumer<String> getTraceLogger() {
		return (String log) -> {
			TPILogger.tl.trace(log);
		};
	}

}
