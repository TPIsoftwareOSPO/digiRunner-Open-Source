package tpi.dgrv4.entity.component.cache.proxy;

import com.esotericsoftware.kryo.Kryo;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import tpi.dgrv4.common.component.cache.core.DaoGenericCache;
import tpi.dgrv4.common.component.cache.proxy.DaoCacheProxy;
import tpi.dgrv4.common.component.cache.proxy.ITsmpDpItemsCacheProxy;
import tpi.dgrv4.common.constant.LocaleType;
import tpi.dgrv4.entity.entity.ITsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.repository.TsmpDpItemsDao;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Component
public class TsmpDpItemsCacheProxy extends DaoCacheProxy implements ITsmpDpItemsCacheProxy{

	private TsmpDpItemsDao tsmpDpItemsDao;

	@Autowired
	public TsmpDpItemsCacheProxy(ObjectMapper objectMapper, DaoGenericCache cache, TsmpDpItemsDao tsmpDpItemsDao) {
		super(objectMapper, cache);
		this.tsmpDpItemsDao = tsmpDpItemsDao;
	}

	public List<ITsmpDpItems> queryBcryptParam(String itemNo, String locale) {
		Supplier<List<ITsmpDpItems>> supplier = () -> {			
			List<ITsmpDpItems> itemsList = getTsmpDpItemsDao().queryBcryptParam(itemNo, locale);
			if((itemsList == null || itemsList.isEmpty()) && !LocaleType.EN_US.equals(locale)) {
				return getTsmpDpItemsDao().queryBcryptParam(itemNo, LocaleType.EN_US);
			}			
			return itemsList;
		};
		return getList("queryBcryptParam", supplier, itemNo, locale);
	}

	public TsmpDpItems findById(TsmpDpItemsId id) {
		Supplier<TsmpDpItems> supplier = () -> {
			TsmpDpItems obj = getTsmpDpItemsDao().findById(id).orElse(null);
			if(obj == null && !LocaleType.EN_US.equals(id.getLocale())) {
				TsmpDpItemsId id2 = new TsmpDpItemsId(id.getItemNo(), id.getSubitemNo(), LocaleType.EN_US);
				obj = getTsmpDpItemsDao().findById(id2).orElse(null);
			}
			return obj;
		};
		
		return getOne("findById", supplier, TsmpDpItems.class, id).orElse(null);
	}
	
	public TsmpDpItems findByItemNoAndParam1AndLocale(String itemNo, String param1, String locale) {
        Supplier<TsmpDpItems> supplier = () -> {
            TsmpDpItems vo = getTsmpDpItemsDao().findByItemNoAndParam1AndLocale(itemNo, param1, locale);
            if(vo == null && !LocaleType.EN_US.equals(locale)) {
                vo = getTsmpDpItemsDao().findByItemNoAndParam1AndLocale(itemNo, param1, LocaleType.EN_US);
            }
            return vo;
        };
         return getOne("findByItemNoAndParam1AndLocale", supplier,TsmpDpItems.class, itemNo, param1, locale).orElse(null);
    }

	public List<TsmpDpItems> queryLike(Long lastId, String[] keyword, String itemNo, 
			String isDefault, int pageSize, String locale) {
		
		Supplier<List<TsmpDpItems>> supplier = () -> {
			List<TsmpDpItems> list = getTsmpDpItemsDao().queryLike(lastId, keyword, itemNo, isDefault, pageSize, locale);
			if(list.isEmpty() && !LocaleType.EN_US.equals(locale)) {
				list = getTsmpDpItemsDao().queryLike(lastId, keyword, itemNo, isDefault, pageSize, LocaleType.EN_US);
			}
			return list;
		};
		
		return getList("queryLike", supplier, lastId,  keyword,  itemNo, isDefault,  pageSize, locale);
	}

	public List<TsmpDpItems> findByLocale(String locale) {
		Supplier<List<TsmpDpItems>> supplier = () -> {
			List<TsmpDpItems> list = getTsmpDpItemsDao().findByLocale(locale);
			if(list.isEmpty() && !LocaleType.EN_US.equals(locale)) {
				list = getTsmpDpItemsDao().findByLocale(LocaleType.EN_US);
			}
			return list;
		};
		return getList("findByLocale", supplier, locale);
	}

	public List<TsmpDpItems> findByLocale(String locale, Sort sort) {
		Supplier<List<TsmpDpItems>> supplier = () -> {
			List<TsmpDpItems> list = getTsmpDpItemsDao().findByLocale(locale, sort);
			if(list.isEmpty() && !LocaleType.EN_US.equals(locale)) {
				list = getTsmpDpItemsDao().findByLocale(LocaleType.EN_US, sort);
			}
			return list;
		};
		
		return getList("findByLocale", supplier, locale, sort);
	}

	public List<TsmpDpItems> findByItemNoAndLocale(String itemNo, String locale) {
		Supplier<List<TsmpDpItems>> supplier = () -> {
			List<TsmpDpItems> list = getTsmpDpItemsDao().findByItemNoAndLocale(itemNo, locale);
			if(list.isEmpty() && !LocaleType.EN_US.equals(locale)) {
				list = getTsmpDpItemsDao().findByItemNoAndLocale(itemNo, LocaleType.EN_US);
			}
			return list;
		};
		
		return getList("findByItemNoAndLocale", supplier, itemNo, locale);
	}

	public List<TsmpDpItems> findByItemNoAndLocaleOrderBySortByAsc(String itemNo, String locale) {
		Supplier<List<TsmpDpItems>> supplier = () -> {
			List<TsmpDpItems> list = getTsmpDpItemsDao().findByItemNoAndLocaleOrderBySortByAsc(itemNo, locale);
			if(list.isEmpty() && !LocaleType.EN_US.equals(locale)) {
				list = getTsmpDpItemsDao().findByItemNoAndLocaleOrderBySortByAsc(itemNo, LocaleType.EN_US);
			}
			return list;
		};
		return getList("findByItemNoAndLocaleOrderBySortByAsc", supplier, itemNo, locale);
		
	}
	
	public TsmpDpItems findByItemNoAndParam2AndLocale(String itemNo,String param2, String locale) {
		Supplier<TsmpDpItems> supplier = () -> {
			TsmpDpItems vo = getTsmpDpItemsDao().findByItemNoAndParam2AndLocale(itemNo, param2, locale);
			if(vo == null && !LocaleType.EN_US.equals(locale)) {
				vo = getTsmpDpItemsDao().findByItemNoAndParam2AndLocale(itemNo, param2, LocaleType.EN_US);
			}
			return vo;
		};
		
		return getOne("findByItemNoAndParam2AndLocale", supplier, TsmpDpItems.class, itemNo, param2, locale).orElse(null);
	}

	public TsmpDpItems findByItemNoAndSubitemNoAndLocale(String itemNo, String subitemNo, String locale) {
		Supplier<TsmpDpItems> supplier = () -> {
			TsmpDpItems vo = getTsmpDpItemsDao().findByItemNoAndSubitemNoAndLocale(itemNo, subitemNo, locale);
			if(vo == null && !LocaleType.EN_US.equals(locale)) {
				vo = getTsmpDpItemsDao().findByItemNoAndSubitemNoAndLocale(itemNo, subitemNo, LocaleType.EN_US);
			}
			return vo;
		};
		return getOne("findByItemNoAndSubitemNoAndLocale", supplier, TsmpDpItems.class, itemNo, subitemNo, locale).orElse(null);
	}

	@Override
	protected Class<?> getDaoClass() {
		return TsmpDpItemsDao.class;
	}

	@Override
	protected void kryoRegistration(Kryo kryo) {
		kryo.register(TsmpDpItems.class);
	}

	protected TsmpDpItemsDao getTsmpDpItemsDao() {
		return this.tsmpDpItemsDao;
	}

	@Override
	protected Consumer<String> getTraceLogger() {
		return null;
	}
	
}