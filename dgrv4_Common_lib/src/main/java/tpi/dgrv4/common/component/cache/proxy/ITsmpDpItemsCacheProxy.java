package tpi.dgrv4.common.component.cache.proxy;

import java.util.List;

import tpi.dgrv4.entity.entity.ITsmpDpItems;

public interface ITsmpDpItemsCacheProxy {

	List<ITsmpDpItems> queryBcryptParam(String itemNo, String locale);

	ITsmpDpItems findByItemNoAndSubitemNoAndLocale(String string, String status, String locale);

}
