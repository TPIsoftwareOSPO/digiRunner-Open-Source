package tpi.dgrv4.gateway.component.cache.proxy;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.esotericsoftware.kryo.Kryo;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.component.cache.core.DaoGenericCache;
import tpi.dgrv4.common.component.cache.proxy.DaoCacheProxy;
import tpi.dgrv4.entity.entity.TsmpGroup;
import tpi.dgrv4.entity.repository.TsmpGroupDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class TsmpGroupCacheProxy extends DaoCacheProxy {

	private TsmpGroupDao tsmpGroupDao;

	@Autowired
	public TsmpGroupCacheProxy(ObjectMapper objectMapper, DaoGenericCache cache, TsmpGroupDao tsmpGroupDao) {
		super(objectMapper, cache);
		this.tsmpGroupDao = tsmpGroupDao;
	}

	public TsmpGroup findFirstByGroupIdAndVgroupFlag(String groupId, String vgroupFlag) {
		Supplier<TsmpGroup> supplier = () -> {
			return getTsmpGroupDao().findFirstByGroupIdAndVgroupFlag(groupId, vgroupFlag);
		};
		return getOne("findFirstByGroupIdAndVgroupFlag", supplier, TsmpGroup.class, groupId, vgroupFlag).orElse(null);
	}

	@Override
	protected Class<?> getDaoClass() {
		return TsmpGroupDao.class;
	}

	@Override
	protected void kryoRegistration(Kryo kryo) {
		kryo.register(TsmpGroup.class);
	}

	protected TsmpGroupDao getTsmpGroupDao() {
		return tsmpGroupDao;
	}

	@Override
	protected Consumer<String> getTraceLogger() {
		return (String log) -> {
			TPILogger.tl.trace(log);
		};
	}

}