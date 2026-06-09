package tpi.dgrv4.common.component.cache.core;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import tpi.dgrv4.common.utils.StackTraceUtil;

@Component
public class DaoGenericCache implements IDaoGenericCache<String, Object> {


	protected Long cacheTimeout;
	protected long bufferInterval;

	protected Map<String, CacheValue> cacheMap;

	protected Consumer<String> traceLogger;

	protected interface CacheValue {
		Object getValue();

		long getCreatedAt();

		long getUpdateTime();
		
		void setUpdateTime(long now);
		
	}

	protected CacheValue createCacheValue(Object value) {

		return new CacheValue() {

			long createdTime = System.currentTimeMillis();
			long updateTime = System.currentTimeMillis();

			@Override
			public Object getValue() {
				return value;
			}

			@Override
			public long getCreatedAt() {
				return createdTime;
			}

			@Override
			public long getUpdateTime() {
				return this.updateTime;
			}

			@Override
			public void setUpdateTime(long updateTime) {
				this.updateTime = updateTime;
			}

		};
	}

	private Object daoGenericCacheLock = new Object();

	@Autowired
	public DaoGenericCache(
			@Value("${digi.cache.default-timeout.ms:120000}") Long cacheTimeout,
			@Value("${digi.cache.buffer-interval.ms:6000}") Long bufferInterval) {
		this(cacheTimeout,bufferInterval,null);
	}

	public DaoGenericCache(Long cacheTimeout, Long bufferInterval, Consumer<String> traceLogger) {
		this.cacheTimeout = cacheTimeout;
		this.bufferInterval = bufferInterval;
		if (traceLogger != null) {
			//testcase用
			this.traceLogger = traceLogger;
		}
		this.clear();
		this.startCleanThread();
	}

	// [EN]Starts the background thread that periodically cleans expired cache entries
	// [ZH]啟動背景執行緒，定期清除過期的快取項目
	private void startCleanThread() {
		new Thread() {
			public void run() {
				while (true) {
					try {
						synchronized (daoGenericCacheLock) {
							daoGenericCacheLock.wait(1000);
						}
						clean();
					} catch (InterruptedException e) {
						if (traceLogger != null) {
							traceLogger.accept(StackTraceUtil.logStackTrace(e));
						}
						Thread.currentThread().interrupt();
					}
				}
			}
		}.start();
	}

	@Override
	public void clear() {
		if (this.cacheMap != null) {
			this.cacheMap.clear();
		}
		this.cacheMap = new ConcurrentHashMap<>();
	}

	@Override
	public void put(String key, Object value, CacheValueAdapter adapter) {

		// 將物件複製一份出來，以序列化方式
		byte[] serializeObject = adapter.serialize(value);
		Object clonedObject = adapter.deserialize(serializeObject);
		// 將物件存入cacheMap
		this.cacheMap.put(key, this.createCacheValue(clonedObject));

	}

	public Map<String, CacheValue> getCacheMap() {
		return this.cacheMap;
	}

	@Override
	public boolean containsKey(String key) {
		return this.cacheMap.containsKey(key);
	}

	@Override
	public Optional<Object> get(String key) {

		Optional<CacheValue> opt = Optional.ofNullable(this.cacheMap.get(key));

		if (opt.isPresent()) {

			CacheValue cv = opt.get();

			long expirationTimeMillis = cv.getUpdateTime() + this.bufferInterval;
			boolean isExpired = System.currentTimeMillis() > expirationTimeMillis;

			// 檢查有沒有超過 預設快取緩衝時間，若超過則從快取移除，沒超過就更新快取時間。
			if (isExpired) {
				remove(key);
				return Optional.empty();
			} else {
				// 更新cache時間
				cv.setUpdateTime(System.currentTimeMillis());
			}

			return opt.map(CacheValue::getValue);
		}
		return Optional.empty();
	}

	public void setTraceLogger(Consumer<String> traceLogger) {
		this.traceLogger = traceLogger;
	}

	@Override
	public void clean() {
		for (String key : this.getExpiredKeys()) {
			
			try {
				// 休息1ms，避免CPU飆高
				Thread.sleep(1);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			this.remove(key);
			if (traceLogger != null) {
				this.traceLogger.accept("remove cache key: " + key);
			}
		}

	}

	protected Set<String> getExpiredKeys() {
		return this.cacheMap.keySet().parallelStream().filter(this::isExpired).collect(Collectors.toSet());
	}

	protected boolean isExpired(String key) {
		CacheValue cv = this.cacheMap.get(key);
		if (cv == null) {
			return true; // 找不到就當是到期了吧
		}
		long expirationTimestampMillis = cv.getCreatedAt() + this.cacheTimeout;
		long currentTimestampMillis = System.currentTimeMillis();
		return currentTimestampMillis > expirationTimestampMillis;
	}

	@Override
	public void remove(String key) {
		this.cacheMap.remove(key);
	}

}
