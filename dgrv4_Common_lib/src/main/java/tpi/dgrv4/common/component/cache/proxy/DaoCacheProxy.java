package tpi.dgrv4.common.component.cache.proxy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.esotericsoftware.kryo.Kryo;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.el.MethodNotFoundException;
import tpi.dgrv4.common.component.cache.core.CacheValueAdapter;
import tpi.dgrv4.common.component.cache.core.CacheValueKryoAdapter;
import tpi.dgrv4.common.component.cache.core.DaoGenericCache;

@Component
public abstract class DaoCacheProxy {

	private static final String NO_PARAM_KEY = "0";

	protected abstract Class<?> getDaoClass();

	protected abstract Consumer<String> getTraceLogger();

	private final ObjectMapper objectMapper;
	protected final DaoGenericCache cache;

	private final CacheValueAdapter adapter;

	// [EN]Per-key lock map to prevent cache stampede (thundering herd problem):
	//     when multiple threads encounter a cache miss for the same key simultaneously,
	//     only ONE thread queries the DB while the others wait, then all read from cache.
	// [ZH]每個 key 對應的鎖，防止 cache stampede（雷擊群問題）：
	//     當多個執行緒同時遇到相同 key 的 cache miss 時，只有一個執行緒查詢 DB，
	//     其餘執行緒等待後直接從快取讀取，避免重複打穿 DB。
	private final ConcurrentHashMap<String, Object> keyLocks = new ConcurrentHashMap<>();
	
	@Autowired
	protected DaoCacheProxy(ObjectMapper objectMapper, DaoGenericCache cache) {
		super();
		this.adapter = new CacheValueKryoAdapter(getClass().getName(), this::kryoRegistration);
		this.objectMapper = objectMapper;
		this.cache = cache;
	}

	protected <R> Optional<R> getOne(String methodName, Supplier<R> supplier, Class<R> returnType, Object... params) {
		R r = get(methodName, supplier, (obj) -> {
			return returnType.cast(obj);
		}, params);
		return Optional.ofNullable(r);
	}

	@SuppressWarnings("unchecked")
	protected <R> List<R> getList(String methodName, Supplier<List<R>> supplier, Object... params) {
		return get(methodName, supplier, (obj) -> {
			return new ArrayList<>((Collection<R>) obj);
		}, params);
	}

	private <R> R get(String methodName, Supplier<R> supplier, Function<Object, R> caster, Object... params) {
		String cacheKey = genCacheKey(getDaoClass(), methodName, params);

		// [EN]Fast path: return immediately if cache has a valid (non-expired) value, no locking overhead.
		// [ZH]快速路徑：若快取有有效值（未過期）則直接回傳，避免取鎖的開銷（絕大多數請求走此路徑）。
		Optional<Object> fastResult = tryGetFromCache(cacheKey);
		if (fastResult.isPresent()) {
			return caster.apply(fastResult.get());
		}

		// [EN]Slow path: use a per-key lock to prevent cache stampede.
		//     computeIfAbsent is atomic — only one lock object is created per key.
		//     Threads that arrive simultaneously for the same key will queue up on that lock
		//     instead of all hitting the DB.
		// [ZH]慢速路徑：使用 per-key 鎖防止 cache stampede。
		//     computeIfAbsent 是原子操作，每個 key 只會建立一個鎖物件。
		//     同時到達的執行緒會在同一把鎖上排隊，而不是同時打穿 DB。
		Object lock = keyLocks.computeIfAbsent(cacheKey, k -> new Object());
		synchronized (lock) {
			try {
				// [EN]Double-check inside the lock: a thread that was waiting may now find the cache already populated.
				// [ZH]在鎖內雙重確認：前一個執行緒已查 DB 並寫入快取，後續執行緒應直接從快取讀取。
				Optional<Object> rechecked = tryGetFromCache(cacheKey);
				if (rechecked.isPresent()) {
					return caster.apply(rechecked.get());
				}

				// [EN]Confirmed cache miss: only this thread queries the DB and populates the cache.
				// [ZH]確認 cache miss：僅此執行緒向 DB 查詢並寫入快取（同一時間只有一個執行緒執行此操作）。
				R r = supplier.get();
				getCache().put(cacheKey, r, getCacheValueAdapter());
				return r;
			} finally {
				// [EN]Remove the lock entry after the value is populated.
				//     Threads already waiting hold a reference to the lock object, so removal is safe.
				// [ZH]寫入快取後移除鎖物件，防止 keyLocks 無限累積。
				//     等待中的執行緒已持有鎖物件引用，移除 map 中的 entry 不影響其正確性。
				keyLocks.remove(cacheKey);
			}
		}
	}

	// [EN]Attempt to retrieve a value from cache; returns empty if the key is absent or the entry is expired.
	// [ZH]嘗試從快取取值；若 key 不存在或快取項目已過期則回傳 Optional.empty()。
	private Optional<Object> tryGetFromCache(String cacheKey) {
		if (!getCache().containsKey(cacheKey)) {
			return Optional.empty();
		}
		return getCache().get(cacheKey);
	}

	/**
	 * 檢查 clazz 是否存在該 methodName (不檢查參數類型) 若符合則產生 CacheKey
	 * 
	 * @param clazz
	 * @param methodName
	 * @param args
	 * @return
	 */
	private String genCacheKey(Class<?> clazz, String methodName, Object... args) {
		Method method = null;
		for (Method m : clazz.getMethods()) {
			if (methodName.equals(m.getName())) {
				method = m;
				break;
			}
		}
		if (method == null) {
			throw new MethodNotFoundException();
		}

		final char sp = ':';
		StringBuilder strBuilder = new StringBuilder();
		// 類別名稱
		strBuilder.append(clazz.getName());
		strBuilder.append(sp);
		// 方法名稱
		strBuilder.append(method.getName());
		strBuilder.append(sp);
		if (args.length > 0) {
			// 參數值
			String val = "";
			for (Object arg : args) {
				try {
					val = getObjectMapper().writeValueAsString(arg);
				} catch (Exception e) {
					val = arg.toString();
				} finally {
					strBuilder.append(val.hashCode());
				}
			}
		} else {
			strBuilder.append(NO_PARAM_KEY);
		}

		return strBuilder.toString();
	}

	/** 註冊所有需要快取的類別 */
	protected void kryoRegistration(Kryo kryo) {
		// 由子類別自行註冊要序列化的類別
	}

	protected ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}

	protected DaoGenericCache getCache() {
		this.cache.setTraceLogger(getTraceLogger());
		return this.cache;
	}

	protected CacheValueAdapter getCacheValueAdapter() {
		return this.adapter;
	}
}
