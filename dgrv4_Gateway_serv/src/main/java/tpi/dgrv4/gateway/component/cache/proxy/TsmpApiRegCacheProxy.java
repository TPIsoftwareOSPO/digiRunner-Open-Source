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
import tpi.dgrv4.entity.entity.TsmpApiReg;
import tpi.dgrv4.entity.entity.TsmpApiRegId;
import tpi.dgrv4.entity.repository.TsmpApiRegDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class TsmpApiRegCacheProxy extends DaoCacheProxy{
	
	private TsmpApiRegDao tsmpApiRegDao;

	@Autowired
	public TsmpApiRegCacheProxy(ObjectMapper objectMapper, DaoGenericCache cache, TsmpApiRegDao tsmpApiRegDao) {
		super(objectMapper, cache);
		this.tsmpApiRegDao = tsmpApiRegDao;
	}

	public Optional<TsmpApiReg> findById(TsmpApiRegId id) {
		Supplier<TsmpApiReg> supplier = () -> {
			Optional<TsmpApiReg> opt = getTsmpApiRegDao().findById(id);
			return opt.orElse(null);
		};
		
		return getOne("findById", supplier, TsmpApiReg.class, id);
	}
	

	@Override
	protected Class<?> getDaoClass() {
		return TsmpApiRegDao.class;
	}

	@Override
	protected void kryoRegistration(Kryo kryo) {
		kryo.register(TsmpApiReg.class);
	}

	protected TsmpApiRegDao getTsmpApiRegDao() {
		return this.tsmpApiRegDao;
	}
	
	@Override
	protected Consumer<String> getTraceLogger() {
		return (String log) -> {
			TPILogger.tl.trace(log);
		};
	}
}
