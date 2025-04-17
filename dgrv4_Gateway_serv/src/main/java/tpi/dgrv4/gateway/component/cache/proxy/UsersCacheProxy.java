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
import tpi.dgrv4.entity.entity.Users;
import tpi.dgrv4.entity.repository.UsersDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class UsersCacheProxy extends DaoCacheProxy {

	private UsersDao usersDao;

	@Autowired
	public UsersCacheProxy(ObjectMapper objectMapper, DaoGenericCache cache, UsersDao usersDao) {
		super(objectMapper, cache);
		this.usersDao = usersDao;
	}

	public Optional<Users> findById(String id) {
		Supplier<Users> supplier = () -> {
			Optional<Users> opt = getUsersDao().findById(id);
			return opt.orElse(null);
		};
		
		return getOne("findById", supplier, Users.class, id);
	}
	
	@Override
	protected Class<?> getDaoClass() {
		return UsersDao.class;
	}

	@Override
	protected void kryoRegistration(Kryo kryo) {
		kryo.register(Users.class);
	}

	protected UsersDao getUsersDao() {
		return usersDao;
	}

	@Override
	protected Consumer<String> getTraceLogger() {
		return (String log) -> {
			TPILogger.tl.trace(log);
		};
	}
}