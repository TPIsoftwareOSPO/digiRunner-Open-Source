package tpi.dgrv4.gateway.component.cache.proxy;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.esotericsoftware.kryo.Kryo;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.component.cache.core.DaoGenericCache;
import tpi.dgrv4.common.component.cache.proxy.DaoCacheProxy;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class TsmpUserCacheProxy extends DaoCacheProxy {

	private TsmpUserDao tsmpUserDao;

	@Autowired
	public TsmpUserCacheProxy(ObjectMapper objectMapper, DaoGenericCache cache, TsmpUserDao tsmpUserDao) {
		super(objectMapper, cache);
		this.tsmpUserDao = tsmpUserDao;
	}

	public TsmpUser findFirstByUserName(String userName) {
		Supplier<TsmpUser> supplier = () -> {
			TsmpUser vo = getTsmpUserDao().findFirstByUserName(userName);
			return vo;
		};
		
		return getOne("findFirstByUserName", supplier, TsmpUser.class, userName).orElse(null);
	}
	
	@Override
	protected Class<?> getDaoClass() {
		return TsmpUserDao.class;
	}

	@Override
	protected void kryoRegistration(Kryo kryo) {
		kryo.register(TsmpUser.class);
	}

	protected TsmpUserDao getTsmpUserDao() {
		return tsmpUserDao;
	}

	@Override
	protected Consumer<String> getTraceLogger() {
		return (String log) -> {
			TPILogger.tl.trace(log);
		};
	}
}