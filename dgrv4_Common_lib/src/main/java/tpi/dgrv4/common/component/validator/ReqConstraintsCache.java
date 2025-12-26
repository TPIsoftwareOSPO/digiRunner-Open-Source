package tpi.dgrv4.common.component.validator;

import static tpi.dgrv4.common.utils.ServiceUtil.nvl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import tpi.dgrv4.common.keeper.ITPILogger;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

/**
 * 以Class的hashCode作為Cache的Key值, 存放provideConstraints的結果
 * @author Kim
 */
public class ReqConstraintsCache {

	private ITPILogger logger;

	private static Map<String, List<BeforeControllerRespItem>> constraintsCache;

	static {
		constraintsCache = new ConcurrentHashMap<>();
	}

	public ReqConstraintsCache(ITPILogger logger) {
		this.logger = logger;
	}
	
	/**
	 * 清除Cache
	 */
	public void clear() {
		synchronized (constraintsCache) {
			constraintsCache.clear();
			constraintsCache.notifyAll();
		}
	}
	
	/**
	 * 產生Cache Key
	 * @param clazz
	 * @return
	 */
	public static String genCacheKey(Class<?> clazz, String locale) {
		Integer hashCode = clazz.hashCode();
		locale = nvl(locale);
		return String.valueOf(hashCode).concat(locale);
	}

	public void setConstraints(Class<?> reqClass, List<BeforeControllerRespItem> constraints, String locale) {
		this.logger.debug(String.format("cache constraints [%s]: %d", reqClass.getCanonicalName(), constraints.size()));
		set(reqClass, constraintsCache, constraints, locale);
	}

	private <T> List<T> get(Class<?> reqClass, Map<String, List<T>> cache, String locale) {
		return syncExec(reqClass, cache, (cacheKey) -> {
			return cache.get(cacheKey);
		}, locale);
	}
	
	/**
	 * 取出某個Request所設定的限制式
	 * @param reqClass
	 * @return
	 */
	public List<BeforeControllerRespItem> getConstraints(Class<?> reqClass, String locale) {
		return get(reqClass, constraintsCache, locale);
	}

	private <T> void set(Class<?> reqClass, Map<String, List<T>> cache, List<T> tList, String locale) {
		syncExec(reqClass, cache, (cacheKey) -> {
			cache.put(cacheKey, tList);
		}, locale);
	}
	
	private String findCacheKey(Class<?> reqClass, Map<String, ?> cache, String locale) {
		if (reqClass == null || cache == null) return null;
		String targetKey = genCacheKey(reqClass, locale);
		for (String key : cache.keySet()) {
			if (key.equals(targetKey)) {
				return key;
			}
		}
		// 如果找不到就新增
		// cache.put(targetKey, null);
		return targetKey;
		
//		synchronized (cache) {
//			try {
//			} finally {
//				cache.notifyAll();
//			}
//		}
	}

	private <R> R syncExec(Class<?> reqClass, Map<String, R> cache, Consumer<String> consumer, String locale) {
		return syncExec(reqClass, cache, (cacheKey) -> {
			consumer.accept(cacheKey);
			return null;
		}, locale);
	}

	private <R> R syncExec(Class<?> reqClass, Map<String, R> cache, Function<String, R> func, String locale) {
		// lock key of map, if key does not exist, then lock the entire cache
		String cacheKey = findCacheKey(reqClass, cache, locale);
		return func.apply(cacheKey);
		
//		synchronized (cacheKey) {
//			try {
//			} finally {
//				cacheKey.notifyAll();
//			}
//		}
	}
	


}