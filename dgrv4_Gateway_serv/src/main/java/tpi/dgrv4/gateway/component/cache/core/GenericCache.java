package tpi.dgrv4.gateway.component.cache.core;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import tpi.dgrv4.gateway.TCP.Packet.ClearCacheProxyPacket;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class GenericCache implements IGenericCache<String, Object> {

	private static final Long DEFAULT_CACHE_TIMEOUT = 120000L;	// 120sec

	private TPILogger logger = TPILogger.tl;

	protected Map<String, CacheValue> cacheMap;

	protected Long cacheTimeout;

	protected interface CacheValue {
		byte[] getValue();
		LocalDateTime getCreatedAt();
	}

	protected CacheValue createCacheValue(byte[] value) {
		LocalDateTime now = LocalDateTime.now();
		return new CacheValue() {
			@Override
			public byte[] getValue() {
				return value;
			}
			@Override
			public LocalDateTime getCreatedAt() {
				return now;
			}
		};
	}

	@Autowired
	public GenericCache() {
		this(DEFAULT_CACHE_TIMEOUT);
	}

	public GenericCache(Long cacheTimeout) {
		this.cacheTimeout = cacheTimeout;
		this.clear();
	}

	@Override
	public void clear() {
		this.cacheMap = new ConcurrentHashMap<>();
	}

	public void clearAndNotify() {
		if (this.cacheMap != null) {
			this.cacheMap.clear();
			clear();
			if (TPILogger.lc == null) {
				return;
			}
			TPILogger.tl.debug("Client [" + TPILogger.lc.userName + "] IGenericCache is totally cleared.");
			// Notify
			synchronized (TPILogger.lc) {
				try {
					TPILogger.lc.send(new ClearCacheProxyPacket());
				} catch (Exception e) {
					this.logger.warn(String.format("Failed to notify node to clear cache proxy: %s", e.getMessage()));
				}
			}
		}
	}

	@Override
	public void clean() {
		//StringBuffer msg = new StringBuffer(); // 不要在 loop 中 logger, 使用 StringBuffer 收集完成後再 logger out.
		for (String key : this.getExpiredKeys()) {
			this.remove(key);
			this.logger.info("remove cache key: " + key );
			//msg.append("remove cache key: " + key + "\n");
		}
	}

	protected Set<String> getExpiredKeys() {
		return this.cacheMap.keySet().parallelStream().filter(this::isExpired).collect(Collectors.toSet());
	}

	protected boolean isExpired(String key) {
		CacheValue cv = this.cacheMap.get(key);
		if (cv == null) {
			return true; //找不到就當是到期了吧
		}
		LocalDateTime expirationDateTime = cv.getCreatedAt().plus(//
			this.cacheTimeout, ChronoUnit.MILLIS);
		//System.err.println("isExpired():: key=" + key + ", 到期時間::" + expirationDateTime);
		return LocalDateTime.now().isAfter(expirationDateTime);
	}

	@Override
	public void remove(String key) {
		this.cacheMap.remove(key);
	}

	@Override
	public boolean containsKey(String key) {
		return this.cacheMap.containsKey(key);
	}

	@Override
	public Optional<Object> get(String key, CacheValueAdapter adapter) {
		
		// 不要每次都執行 , 以排程來做, 目前排程在 'RefreshCacheJob.java' 執行
		// this.clean();//只清除到期資料 
		
		// 取出反序列化後再回傳
		Optional<byte[]> opt = Optional.ofNullable(this.cacheMap.get(key)).map(CacheValue::getValue);
		if (opt.isPresent()) {
			adapter = adapter == null ? new CacheValueKryoAdapter() : adapter;
			Object obj = null;
			try {
				obj = adapter.deserialize(opt.get());
			} catch (Exception e) {
				this.logger.error("An error occurred when deserializing at " + adapter.getIdentifier() 
					+ ", are all cachable classes registered in 'kryoRegistration' method?");
				throw e;
			}
			return Optional.ofNullable(obj);
		}
		return Optional.empty();
	}

	@Override
	public void put(String key, Object value, CacheValueAdapter adapter) {
		// 將物件序列化後再存入cacheMap
		adapter = adapter == null ? new CacheValueKryoAdapter() : adapter;
		byte[] data = null;
		try {
			data = adapter.serialize(value);
		} catch (Exception e) {
			this.logger.error("An error occurred when serializing at " + adapter.getIdentifier() 
			+ ", are all cachable classes registered in 'kryoRegistration' method?");
			throw e;
		}
		this.cacheMap.put(key, this.createCacheValue(data));
		this.logger.trace("cache size: " + this.cacheMap.size());
	}
	
	public Map<String, CacheValue> getCacheMap() {
		return this.cacheMap;
	}

}