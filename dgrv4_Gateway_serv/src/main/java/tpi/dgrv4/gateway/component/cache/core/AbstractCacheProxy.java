package tpi.dgrv4.gateway.component.cache.core;

import com.esotericsoftware.kryo.Kryo;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.el.MethodNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import tpi.dgrv4.gateway.component.job.DummyJob;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.component.job.RefreshCacheJob;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/** API 自適用快取使用此 proxy */
public abstract class AbstractCacheProxy {

	private static final String NO_PARAM_KEY = "0";

	protected GenericCache cache;
	private ObjectMapper objectMapper;
	private ApplicationContext ctx;
	private JobHelper jobHelper;

	private final CacheValueAdapter adapter;

	// [EN]Per-key lock map to prevent cache stampede (thundering herd problem):
	//     when multiple threads encounter a cache miss for the same key simultaneously,
	//     only ONE thread queries the DB while the others wait, then all read from cache.
	// [ZH]每個 key 對應的鎖，防止 cache stampede（雷擊群問題）：
	//     當多個執行緒同時遇到相同 key 的 cache miss 時，只有一個執行緒查詢 DB，
	//     其餘執行緒等待後直接從快取讀取，避免重複打穿 DB。
	private final ConcurrentHashMap<String, Object> keyLocks = new ConcurrentHashMap<>();

	@Autowired
	protected AbstractCacheProxy(GenericCache cache, ObjectMapper objectMapper, ApplicationContext ctx) {
		this.adapter = new CacheValueKryoAdapter(getClass().getName(), this::kryoRegistration);
		this.cache = cache;
		this.objectMapper = objectMapper;
		this.ctx = ctx;
	}
	
	/*
	 * Because using constructor injection will cause a circular dependency, use method injection instead
	 */
	@Autowired
	public void setJobHelper(JobHelper jobHelper) {
		this.jobHelper = jobHelper;
	}

	protected <R> Optional<R> getOne(String methodName, Supplier<R> supplier, Class<R> returnType, Object...params) {
		// API '自適應' cache 會啟用 addRefreshCacheJob() & addDummyJob()
		R r = get(methodName, supplier, (obj) -> {
			return returnType.cast(obj);
		}, params);
		return Optional.ofNullable(r);
	}

	@SuppressWarnings("unchecked")
	protected <R> List<R> getList(String methodName, Supplier<List<R>> supplier, Object...params) {
		return get(methodName, supplier, (obj) -> {
			return new ArrayList<>((Collection<R>) obj);
		}, params);
	}

	protected abstract Class<?> getDaoClass();

	/** 註冊所有需要快取的類別 */
	protected void kryoRegistration(Kryo kryo) {
		// 由子類別自行註冊要序列化的類別
	}

	private <R> R get(String methodName, Supplier<R> supplier, Function<Object, R> caster, Object...params) {
		String cacheKey = genCacheKey(getDaoClass(), methodName, params);

		// [EN]Register a background refresh job for this key (schedules periodic DB re-fetch to keep cache warm).
		// [ZH]為此 key 登錄背景更新 job（定期重新查詢 DB 以保持快取資料新鮮）。
		addRefreshCacheJob(cacheKey, supplier);

		// [EN]Fast path: return immediately if cache has a valid value, no locking overhead.
		// [ZH]快速路徑：若快取有有效值則直接回傳，避免取鎖的開銷（絕大多數請求走此路徑）。
		Optional<Object> fastResult = tryGetFromCache(cacheKey);
		if (fastResult.isPresent()) {
			return caster.apply(fastResult.get());
		}
		
		// 某一種情況是：從 cache 取出的值為空，可能是過期被清掉了，應該要重查並放回 cache
		// 2025.3.11, 新機制下, 不需要 DummyJob 
		// addDummyJob(cacheKey);	// 不是從 cache 取值時才需要加入此工作。利用 job 的 replace 機制，抑制首次 RefreshCacheJob 的執行
				
//		R r = supplier.get();
//		getCache().put(cacheKey, r, getCacheValueAdapter());
//		return r;

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
				// 2025.3.11, 新機制下, 不需要 DummyJob
				// addDummyJob(cacheKey);
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

	// [EN]Attempt to retrieve a value from cache; returns empty if the key is absent or the entry is not present.
	// [ZH]嘗試從快取取值；若 key 不存在或快取項目為空則回傳 Optional.empty()。
	private Optional<Object> tryGetFromCache(String cacheKey) {
		if (!getCache().containsKey(cacheKey)) {
			return Optional.empty();
		}
		return getCache().get(cacheKey, getCacheValueAdapter());
	}

	/**
	 * 檢查 clazz 是否存在該 methodName (不檢查參數類型)
	 * 若符合則產生 CacheKey
	 * @param clazz
	 * @param methodName
	 * @param args
	 * @return
	 */
	private String genCacheKey(Class<?> clazz, String methodName, Object ... args) {
		Method method = null;
		for(Method m : clazz.getMethods()) {
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

	protected RefreshCacheJob addRefreshCacheJob(String key, Supplier<?> supplier) {
		if (StringUtils.hasText(key)) {
			try {
				RefreshCacheJob job = getRefreshCacheJob(key, supplier);
				if (job != null) {
					getJobHelper().add(job);
					return job;
				}
			} catch (Exception e) {
				// do nothing...
			}
		}
		return null;
	}

	private void addDummyJob(String cacheKey) {
		String groupId = RefreshCacheJob.GROUP_ID.concat("-").concat(cacheKey);
		DummyJob job = new DummyJob(groupId, 0);
		getJobHelper().add(job);
	}

	protected GenericCache getCache() {
		return this.cache;
	}

	protected ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}

	protected ApplicationContext getCtx() {
		return this.ctx;
	}

	protected JobHelper getJobHelper() {
		return this.jobHelper;
	}

	protected RefreshCacheJob getRefreshCacheJob(String key, Supplier<?> supplier) {
		return (RefreshCacheJob) getCtx().getBean("refreshCacheJob", key, supplier, this.adapter);
	}

	protected CacheValueAdapter getCacheValueAdapter() {
		return this.adapter;
	}
	
}