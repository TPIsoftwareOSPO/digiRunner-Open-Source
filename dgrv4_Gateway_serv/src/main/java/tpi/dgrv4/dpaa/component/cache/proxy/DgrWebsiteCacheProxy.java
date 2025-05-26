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
import tpi.dgrv4.entity.entity.DgrWebsite;
import tpi.dgrv4.entity.repository.DgrWebsiteDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class DgrWebsiteCacheProxy extends DaoCacheProxy {

	private DgrWebsiteDao dgrWebsiteDao;

	@Autowired
	public DgrWebsiteCacheProxy(ObjectMapper objectMapper, DaoGenericCache cache, DgrWebsiteDao dgrWebsiteDao) {
		super(objectMapper, cache);
		this.dgrWebsiteDao = dgrWebsiteDao;
	}

	public List<DgrWebsite> findByWebsiteName(String websiteName) {
		Supplier<List<DgrWebsite>> supplier = () -> {
			return getDgrWebsiteDao().findByWebsiteName(websiteName);
		};
		return getList("findByWebsiteName", supplier, websiteName);
	}

	public List<DgrWebsite> findByWebsiteNameAndWebsiteStatus(String websiteName, String websiteStatus) {
		Supplier<List<DgrWebsite>> supplier = () -> {
			return getDgrWebsiteDao().findByWebsiteNameAndWebsiteStatus(websiteName, websiteStatus);
		};
		return getList("findByWebsiteNameAndWebsiteStatus", supplier, websiteName, websiteStatus);
	}
	
	public DgrWebsite findFirstByWebsiteName(String websiteName) {
		Supplier<DgrWebsite> supplier = () -> {
			return getDgrWebsiteDao().findFirstByWebsiteName(websiteName);
		};
		return getOne("findFirstByWebsiteName", supplier, DgrWebsite.class, websiteName).orElse(null);
	}

	@Override
	protected Class<?> getDaoClass() {
		return DgrWebsiteDao.class;
	}
	
	@Override
	protected void kryoRegistration(Kryo kryo) {
		kryo.register(DgrWebsite.class);
	}

	protected DgrWebsiteDao getDgrWebsiteDao() {
		return this.dgrWebsiteDao;
	}
	
	@Override
	protected Consumer<String> getTraceLogger() {
		return (String log) -> {
			TPILogger.tl.trace(log);
		};
	}

}
