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
import tpi.dgrv4.entity.entity.DgrAcIdpUser;
import tpi.dgrv4.entity.repository.DgrAcIdpUserDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class DgrAcIdpUserCacheProxy extends DaoCacheProxy {

	private DgrAcIdpUserDao dgrAcIdpUserDao;

	@Autowired
	public DgrAcIdpUserCacheProxy(ObjectMapper objectMapper, DaoGenericCache cache, DgrAcIdpUserDao dgrAcIdpUserDao) {
		super(objectMapper, cache);
		this.dgrAcIdpUserDao = dgrAcIdpUserDao;
	}

	public List<DgrAcIdpUser> findByUserName(String userName) {
		Supplier<List<DgrAcIdpUser>> supplier = () -> {
			return getDgrAcIdpUserDao().findByUserName(userName);
		};

		return getList("findByUserName", supplier, userName);
	}

	@Override
	protected Class<?> getDaoClass() {
		return DgrAcIdpUserDao.class;
	}

	@Override
	protected void kryoRegistration(Kryo kryo) {
		kryo.register(DgrAcIdpUser.class);
	}

	protected DgrAcIdpUserDao getDgrAcIdpUserDao() {
		return dgrAcIdpUserDao;
	}

	@Override
	protected Consumer<String> getTraceLogger() {
		return (String log) -> {
			TPILogger.tl.trace(log);
		};
	}
}