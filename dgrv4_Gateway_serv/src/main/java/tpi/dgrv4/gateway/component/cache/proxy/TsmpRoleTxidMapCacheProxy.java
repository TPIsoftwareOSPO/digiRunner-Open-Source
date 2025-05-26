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
import tpi.dgrv4.entity.entity.TsmpRoleTxidMap;
import tpi.dgrv4.entity.repository.TsmpRoleTxidMapDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class TsmpRoleTxidMapCacheProxy extends DaoCacheProxy {

	private TsmpRoleTxidMapDao tsmpRoleTxidMapDao;

	@Autowired
	public TsmpRoleTxidMapCacheProxy(ObjectMapper objectMapper, DaoGenericCache cache,
			TsmpRoleTxidMapDao tsmpRoleTxidMapDao) {
		super(objectMapper, cache);
		this.tsmpRoleTxidMapDao = tsmpRoleTxidMapDao;
	}

	public List<TsmpRoleTxidMap> findByRoleIdIn(List<String> roleIdList) {
		Supplier<List<TsmpRoleTxidMap>> supplier = () -> {
			return getTsmpRoleTxidMapDao().findByRoleIdIn(roleIdList);
		};
		return getList("findByRoleIdIn", supplier, roleIdList);
	}

	@Override
	protected Class<?> getDaoClass() {
		return TsmpRoleTxidMapDao.class;
	}

	@Override
	protected void kryoRegistration(Kryo kryo) {
		kryo.register(TsmpRoleTxidMap.class);
	}

	protected TsmpRoleTxidMapDao getTsmpRoleTxidMapDao() {
		return this.tsmpRoleTxidMapDao;
	}
	
	@Override
	protected Consumer<String> getTraceLogger() {
		return (String log) -> {
			TPILogger.tl.trace(log);
		};
	}
}