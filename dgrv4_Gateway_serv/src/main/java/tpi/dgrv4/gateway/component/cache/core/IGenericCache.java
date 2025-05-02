package tpi.dgrv4.gateway.component.cache.core;

import java.util.Optional;

public interface IGenericCache<K, V> {

	/**
	 * 清空 Cache
	 */
	void clear();

	/**
	 * 清除過期的值({@link GenericCache#cacheTimeout})
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
	 * 取值
	 * @param key
	 * @return
	 */
	default Optional<V> get(K key) {
		return get(key, null);
	}

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
	Optional<V> get(K key, CacheValueAdapter adapter);

	/**
	 * 設值
	 * @param key
	 * @param value
	 */
	void put(K key, V value, CacheValueAdapter adapter);

}