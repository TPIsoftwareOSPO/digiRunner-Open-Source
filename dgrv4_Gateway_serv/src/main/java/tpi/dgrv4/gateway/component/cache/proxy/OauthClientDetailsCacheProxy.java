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
import tpi.dgrv4.entity.entity.OauthClientDetails;
import tpi.dgrv4.entity.repository.OauthClientDetailsDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class OauthClientDetailsCacheProxy extends DaoCacheProxy {

	private OauthClientDetailsDao oauthClientDetailsDao;

	@Autowired
	public OauthClientDetailsCacheProxy(ObjectMapper objectMapper, DaoGenericCache cache,
			OauthClientDetailsDao oauthClientDetailsDao) {
		super(objectMapper, cache);
		this.oauthClientDetailsDao = oauthClientDetailsDao;
	}

	public Optional<OauthClientDetails> findById(String id) {
		Supplier<OauthClientDetails> supplier = () -> {
			Optional<OauthClientDetails> opt = getOauthClientDetailsDao().findById(id);
			return opt.orElse(null);
		};
		
		return getOne("findById", supplier, OauthClientDetails.class, id);
	}
	
	@Override
	protected Class<?> getDaoClass() {
		return OauthClientDetailsDao.class;
	}

	@Override
	protected void kryoRegistration(Kryo kryo) {
		kryo.register(OauthClientDetails.class);
	}
	
	protected OauthClientDetailsDao getOauthClientDetailsDao() {
		return oauthClientDetailsDao;
	}

	@Override
	protected Consumer<String> getTraceLogger() {
		return (String log) -> {
			TPILogger.tl.trace(log);
		};
	}
}