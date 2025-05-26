package tpi.dgrv4.gateway.component.cache.proxy;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.esotericsoftware.kryo.Kryo;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.component.cache.core.DaoGenericCache;
import tpi.dgrv4.common.component.cache.proxy.DaoCacheProxy;
import tpi.dgrv4.entity.entity.TsmpSetting;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.gateway.component.cache.core.AbstractCacheProxy;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class TsmpSettingCacheProxy extends DaoCacheProxy {

	private TsmpSettingDao tsmpSettingDao;

	@Autowired
	public TsmpSettingCacheProxy(ObjectMapper objectMapper, DaoGenericCache cache, TsmpSettingDao tsmpSettingDao) {
		super(objectMapper, cache);
		this.tsmpSettingDao = tsmpSettingDao;
	}

	public Optional<TsmpSetting> findById(String id) {
		Supplier<TsmpSetting> supplier = () -> {
			Optional<TsmpSetting> opt = getTsmpSettingDao().findById(id);
			return opt.orElse(null);
		};
		return getOne("findById", supplier, TsmpSetting.class, id);
	}
	
	public List<TsmpSetting> findAllById(List<String> ids) {
		Supplier<List<TsmpSetting>> supplier = () -> {
			return getTsmpSettingDao().findAllById(ids);
		};
		return getList("findAllById", supplier, ids);
	}

	@Override
	protected Class<?> getDaoClass() {
		return TsmpSettingDao.class;
	}

	@Override
	protected void kryoRegistration(Kryo kryo) {
		kryo.register(TsmpSetting.class);
	}

	protected TsmpSettingDao getTsmpSettingDao() {
		return this.tsmpSettingDao;
	}
	
	@Override
	protected Consumer<String> getTraceLogger() {
		return (String log) -> {
			TPILogger.tl.trace(log);
		};
	}
	
}