package tpi.dgrv4.dpaa.component.cache.proxy;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.esotericsoftware.kryo.Kryo;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.component.cache.core.DaoGenericCache;
import tpi.dgrv4.common.component.cache.proxy.DaoCacheProxy;
import tpi.dgrv4.entity.entity.DgrWebsiteDetail;
import tpi.dgrv4.entity.repository.DgrWebsiteDetailDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class DgrWebsiteDetailCacheProxy extends DaoCacheProxy {

	private DgrWebsiteDetailDao dgrWebsiteDetailDao;

	@Autowired
	public DgrWebsiteDetailCacheProxy(ObjectMapper objectMapper, DaoGenericCache cache,
			DgrWebsiteDetailDao dgrWebsiteDetailDao) {
		super(objectMapper, cache);
		this.dgrWebsiteDetailDao = dgrWebsiteDetailDao;
	}

	public List<DgrWebsiteDetail> findByDgrWebsiteId(Long dgrWebsiteId) {
		Supplier<List<DgrWebsiteDetail>> supplier = () -> {
			return getDgrWebsiteDetailDao().findByDgrWebsiteId(dgrWebsiteId);
		};
		return getList("findByDgrWebsiteId", supplier, dgrWebsiteId);
	}

	@Override
	protected Class<?> getDaoClass() {
		return DgrWebsiteDetailDao.class;
	}

	@Override
	protected void kryoRegistration(Kryo kryo) {
		kryo.register(DgrWebsiteDetail.class);
	}

	protected DgrWebsiteDetailDao getDgrWebsiteDetailDao() {
		return this.dgrWebsiteDetailDao;
	}
	
	@Override
	protected Consumer<String> getTraceLogger() {
		return (String log) -> {
			TPILogger.tl.trace(log);
		};
	}

}
