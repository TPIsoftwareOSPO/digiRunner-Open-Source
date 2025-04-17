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
import tpi.dgrv4.entity.entity.TsmpOrganization;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class TsmpOrganizationCacheProxy extends DaoCacheProxy {

	private TsmpOrganizationDao tsmpOrganizationDao;

	@Autowired
	public TsmpOrganizationCacheProxy(ObjectMapper objectMapper, DaoGenericCache cache,
			TsmpOrganizationDao tsmpOrganizationDao) {
		super(objectMapper, cache);
		this.tsmpOrganizationDao = tsmpOrganizationDao;
	}

	public TsmpOrganization findById(String id) {
		Supplier<TsmpOrganization> supplier = () -> {
			Optional<TsmpOrganization> opt = getTsmpOrganizationDao().findById(id);
			return opt.orElse(null);
		};
		return getOne("findById", supplier, TsmpOrganization.class, id).orElse(null);
	}

	@Override
	protected Class<?> getDaoClass() {
		return TsmpOrganizationDao.class;
	}

	@Override
	protected void kryoRegistration(Kryo kryo) {
		kryo.register(TsmpOrganization.class);
	}

	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}
	
	@Override
	protected Consumer<String> getTraceLogger() {
		return (String log) -> {
			TPILogger.tl.trace(log);
		};
	}
	
}