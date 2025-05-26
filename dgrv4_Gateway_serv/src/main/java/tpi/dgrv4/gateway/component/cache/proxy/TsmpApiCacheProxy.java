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
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class TsmpApiCacheProxy extends DaoCacheProxy {

	private TsmpApiDao tsmpApiDao;

	@Autowired
	public TsmpApiCacheProxy(ObjectMapper objectMapper, DaoGenericCache cache, TsmpApiDao tsmpApiDao) {
		super(objectMapper, cache);
		this.tsmpApiDao = tsmpApiDao;
	}

	public Optional<TsmpApi> findById(TsmpApiId id) {
		Supplier<TsmpApi> supplier = () -> {
			Optional<TsmpApi> opt = getTsmpApiDao().findById(id);
			return opt.orElse(null);
		};
		
		return getOne("findById", supplier, TsmpApi.class, id);
	}
	
	public boolean existsById(TsmpApiId id) {
		Supplier<Boolean> supplier = () -> {
			boolean isExist = getTsmpApiDao().existsById(id);
			return isExist;
		};
		
		return getOne("existsById", supplier, Boolean.class, id).orElse(false).booleanValue();
	}
	
	public List<TsmpApi> queryAll_APIkeyAndModuleNameAndSrcUrl() {
		Supplier<List<TsmpApi>> supplier = () -> {
			List<TsmpApi> opt = getTsmpApiDao().queryAll_APIkeyAndModuleNameAndSrcUrl();
			return opt;
		};
		
		return getList("queryAll_APIkeyAndModuleNameAndSrcUrl", supplier, "queryAll_APIkeyAndModuleNameAndSrcUrl");
	}
	public TsmpApi findByModuleNameAndApiKey(String moduleName, String apiKey) {
		Supplier<TsmpApi> supplier = () -> {
			TsmpApi opt = getTsmpApiDao().findByModuleNameAndApiKey(moduleName,apiKey);
			return opt;
		};
		return getOne("findByModuleNameAndApiKey", supplier,TsmpApi.class ,moduleName, apiKey).orElse(null);
	}

	@Override
	protected Class<?> getDaoClass() {
		return TsmpApiDao.class;
	}

	@Override
	protected void kryoRegistration(Kryo kryo) {
		kryo.register(TsmpApi.class);
	}

	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}
	
	@Override
	protected Consumer<String> getTraceLogger() {
		return (String log) -> {
			TPILogger.tl.trace(log);
		};
	}
}