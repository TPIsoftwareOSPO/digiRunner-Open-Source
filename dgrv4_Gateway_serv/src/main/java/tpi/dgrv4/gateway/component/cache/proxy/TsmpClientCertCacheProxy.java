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
import tpi.dgrv4.entity.entity.jpql.TsmpClientCert;
import tpi.dgrv4.entity.repository.TsmpClientCertDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class TsmpClientCertCacheProxy extends DaoCacheProxy {
	private TsmpClientCertDao tsmpClientCertDao;

	@Autowired
	public TsmpClientCertCacheProxy(ObjectMapper objectMapper, DaoGenericCache cache,
			TsmpClientCertDao tsmpClientCertDao) {
		super(objectMapper, cache);
		this.tsmpClientCertDao = tsmpClientCertDao;
	}

	public List<TsmpClientCert> findByClientIdAndExpiredAtAfterOrderByCreateDateTime(String clientId, long nowLong) {
		Supplier<List<TsmpClientCert>> supplier = () -> {
			return getTsmpClientCertDao().findByClientIdAndExpiredAtAfterOrderByCreateDateTime(clientId, nowLong);
		};
		return getList("findByClientIdAndExpiredAtAfterOrderByCreateDateTime", supplier, clientId, nowLong);
	}

	@Override
	protected Class<?> getDaoClass() {
		return TsmpClientCertDao.class;
	}

	@Override
	protected void kryoRegistration(Kryo kryo) {
		kryo.register(TsmpClientCert.class);
	}

	protected TsmpClientCertDao getTsmpClientCertDao() {
		return this.tsmpClientCertDao;
	}

	@Override
	protected Consumer<String> getTraceLogger() {
		return (String log) -> {
			TPILogger.tl.trace(log);
		};
	}
}
