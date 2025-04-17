package tpi.dgrv4.entity.component.cache.proxy;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.esotericsoftware.kryo.Kryo;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.component.cache.core.DaoGenericCache;
import tpi.dgrv4.common.component.cache.proxy.DaoCacheProxy;
import tpi.dgrv4.common.constant.LocaleType;
import tpi.dgrv4.entity.entity.TsmpRtnCode;
import tpi.dgrv4.entity.entity.TsmpRtnCodeId;
import tpi.dgrv4.entity.repository.TsmpRtnCodeDao;

@Component
public class TsmpRtnCodeCacheProxy extends DaoCacheProxy {

	private TsmpRtnCodeDao tsmpRtnCodeDao;

	@Autowired
	public TsmpRtnCodeCacheProxy(ObjectMapper objectMapper, DaoGenericCache cache, TsmpRtnCodeDao tsmpRtnCodeDao) {
		super(objectMapper, cache);
		this.tsmpRtnCodeDao = tsmpRtnCodeDao;
	}

	public Optional<TsmpRtnCode> findById(String rtnCode, String locale) {
		Supplier<TsmpRtnCode> supplier = () -> {
			TsmpRtnCodeId id = new TsmpRtnCodeId(rtnCode, locale);
			Optional<TsmpRtnCode> opt = getTsmpRtnCodeDao().findById(id);
			if(opt.isEmpty() && !LocaleType.EN_US.equalsIgnoreCase(locale)) {
				id = new TsmpRtnCodeId(rtnCode, LocaleType.EN_US);
				opt = getTsmpRtnCodeDao().findById(id);
			}
			
			return opt.orElse(null);
		};
		return getOne("findById", supplier, TsmpRtnCode.class, rtnCode, locale);
	}

	@Override
	protected Class<?> getDaoClass() {
		return TsmpRtnCodeDao.class;
	}

	@Override
	protected void kryoRegistration(Kryo kryo) {
		kryo.register(TsmpRtnCode.class);
	}

	protected TsmpRtnCodeDao getTsmpRtnCodeDao() {
		return this.tsmpRtnCodeDao;
	}

	@Override
	protected Consumer<String> getTraceLogger() {
		// TODO Auto-generated method stub
		return null;
	}
	
}