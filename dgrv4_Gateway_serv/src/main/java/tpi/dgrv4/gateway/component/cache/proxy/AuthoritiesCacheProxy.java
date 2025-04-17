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
import tpi.dgrv4.entity.entity.Authorities;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.repository.AuthoritiesDao;
import tpi.dgrv4.entity.repository.UsersDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class AuthoritiesCacheProxy extends DaoCacheProxy {

	private AuthoritiesDao authoritiesDao;
	private UsersDao usersDao;

	@Autowired
	public AuthoritiesCacheProxy(ObjectMapper objectMapper, DaoGenericCache cache, AuthoritiesDao authoritiesDao,
			UsersDao usersDao) {
		super(objectMapper, cache);
		this.authoritiesDao = authoritiesDao;
		this.usersDao = usersDao;
	}

	public List<Authorities> findByUsername(String userName) {
		Supplier<List<Authorities>> supplier = () -> {
			return getAuthoritiesDao().findByUsername(userName);
		};
		return getList("findByUsername", supplier, userName);
	}
	
	public Authorities findFirstByUserName(String userName) {
		Supplier<Authorities> supplier = () -> {
			Authorities vo = getAuthoritiesDao().findFirstByUsername(userName);
			return vo;
		};
		
		return getOne("findFirstByUsername", supplier, Authorities.class, userName).orElse(null);
	}

	@Override
	protected Class<?> getDaoClass() {
		return AuthoritiesDao.class;
	}

	@Override
	protected void kryoRegistration(Kryo kryo) {
		kryo.register(Authorities.class);
	}

	protected AuthoritiesDao getAuthoritiesDao() {
		return this.authoritiesDao;
	}
	
	protected UsersDao getUsersDao() {
		return this.usersDao;
	}
	
	@Override
	protected Consumer<String> getTraceLogger() {
		return (String log) -> {
			TPILogger.tl.trace(log);
		};
	}
	
}