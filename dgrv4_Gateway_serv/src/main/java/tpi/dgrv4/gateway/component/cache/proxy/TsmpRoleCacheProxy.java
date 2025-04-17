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
import tpi.dgrv4.entity.entity.TsmpRole;
import tpi.dgrv4.entity.repository.TsmpRoleDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class TsmpRoleCacheProxy extends DaoCacheProxy {

	private TsmpRoleDao tsmpRoleDao;

	@Autowired
	public TsmpRoleCacheProxy(ObjectMapper objectMapper, DaoGenericCache cache, TsmpRoleDao tsmpRoleDao) {
		super(objectMapper, cache);
		this.tsmpRoleDao = tsmpRoleDao;
	}

	public TsmpRole findById(String roleId) {
		Supplier<TsmpRole> supplier = () -> {
			Optional<TsmpRole> opt = getTsmpRoleDao().findById(roleId);
			return opt.orElse(null);
		};
		
		return getOne("findById", supplier, TsmpRole.class, roleId).orElse(null);
	}
	
	public TsmpRole findFirstByRoleName(String roleName) {
		Supplier<TsmpRole> supplier = () -> {
			TsmpRole vo = getTsmpRoleDao().findFirstByRoleName(roleName);
			return vo;
		};
		
		return getOne("findFirstByRoleName", supplier, TsmpRole.class, roleName).orElse(null);
	}

	@Override
	protected Class<?> getDaoClass() {
		return TsmpRoleDao.class;
	}

	@Override
	protected void kryoRegistration(Kryo kryo) {
		kryo.register(TsmpRole.class);
	}

	protected TsmpRoleDao getTsmpRoleDao() {
		return this.tsmpRoleDao;
	}
	
	@Override
	protected Consumer<String> getTraceLogger() {
		return (String log) -> {
			TPILogger.tl.trace(log);
		};
	}
	
}