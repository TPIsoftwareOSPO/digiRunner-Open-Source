package tpi.dgrv4.dpaa.component.cache.proxy;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.component.cache.core.DaoGenericCache;
import tpi.dgrv4.common.component.cache.proxy.DaoCacheProxy;
import tpi.dgrv4.entity.entity.jpql.DgrDashboardLastData;
import tpi.dgrv4.entity.repository.DgrDashboardLastDataDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class DgrDashboardLastDataCacheProxy extends DaoCacheProxy {
	private DgrDashboardLastDataDao dgrDashboardLastDataDao;

	@Autowired
	public DgrDashboardLastDataCacheProxy(@Lazy DgrDashboardLastDataDao dgrDashboardLastDataDao,
			ObjectMapper objectMapper, DaoGenericCache cache) {
		super(objectMapper, cache);
		this.dgrDashboardLastDataDao = dgrDashboardLastDataDao;
	}

	public List<DgrDashboardLastData> findAll() {
		Supplier<List<DgrDashboardLastData>> supplier = () -> {
			return getDgrDashboardLastDataDao().findAll();

		};
		return getList("findAll", supplier, DgrDashboardLastData.class, null);
	}

	public List<DgrDashboardLastData> findByTimeType(int timeType){
		Supplier<List<DgrDashboardLastData>> supplier = () -> {
			return getDgrDashboardLastDataDao().findByTimeType(timeType);

		};
		return getList("findByTimeType", supplier, DgrDashboardLastData.class, timeType);
		
	}
	
	@Override
	protected Class<?> getDaoClass() {
		return DgrDashboardLastDataDao.class;
	}

	protected DgrDashboardLastDataDao getDgrDashboardLastDataDao() {
		return this.dgrDashboardLastDataDao;
	}

	@Override
	protected Consumer<String> getTraceLogger() {
		return (String log) -> {
			TPILogger.tl.trace(log);
		};
	}

}
