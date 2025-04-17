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
		addRefreshCacheJob(cacheKey, supplier);  
		
		// 如果有存入過 cache
		boolean hasCacheEntry = getCache().containsKey(cacheKey);
		if (hasCacheEntry) {
			Optional<Object> opt = getCache().get(cacheKey, getCacheValueAdapter());
			// 且 cache 中有值
			if (opt.isPresent()) {
				// 就直接從 cache 拿值
				return caster.apply(opt.get());
			}
		}
		
		// 某一種情況是：從 cache 取出的值為空，可能是過期被清掉了，應該要重查並放回 cache
		// 2025.3.11, 新機制下, 不需要 DummyJob 
		// addDummyJob(cacheKey);	// 不是從 cache 取值時才需要加入此工作。利用 job 的 replace 機制，抑制首次 RefreshCacheJob 的執行
		
		R r = supplier.get();
		getCache().put(cacheKey, r, getCacheValueAdapter());
		return r;
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