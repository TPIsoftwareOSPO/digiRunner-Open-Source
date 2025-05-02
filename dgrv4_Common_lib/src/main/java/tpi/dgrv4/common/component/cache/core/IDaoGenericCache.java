package tpi.dgrv4.common.component.cache.core;

import java.util.Optional;

public interface IDaoGenericCache<K, V> {

	/**
	 * 清空 Cache
	 */
	void clear();

	/**
	 * 清除過期的值({@link DaoGenericCache#cacheTimeout})
	 */
	void clean();

	/**
	 * 移除 Cache 中的某個 Key-Value
	 * @param key
	 */
	void remove(K key);

	/**
	 * Cache 中是否包含某個 Key
	 * @param key
	 * @return
	 */
	boolean containsKey(K key);

	
	/**
	 * 設值
	 * @param key
	 * @param value
	 */
	default void put(K key, V value) {
		put(key, value, null);
	}

	/**
	 * 取值
	 * @param key
	 * @return
	 */
	Optional<V> get(K key);

	/**
	 * 設值
	 * @param key
	 * @param value
	 */
	void put(K key, V value, CacheValueAdapter adapter);

}