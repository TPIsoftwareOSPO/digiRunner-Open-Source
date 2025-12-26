package tpi.dgrv4.gateway.util;

import java.lang.reflect.Method;
import java.util.function.Supplier;

import jakarta.el.MethodNotFoundException;

import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.component.job.RefreshCacheJob;

public class CacheUtil {

	private static final String NO_PARAM_KEY = "0";

	/**
	 * 檢查 clazz 是否存在該 methodName (不檢查參數類型)
	 * 若符合則產生 CacheKey
	 * @param clazz
	 * @param methodName
	 * @param args
	 * @return
	 */
	public static final String genCacheKey(Class<?> clazz, String methodName, Object ... args) {
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
		strBuilder.append(clazz.getSimpleName());
		strBuilder.append(sp);
		// 方法名稱
		strBuilder.append(method.getName());
		strBuilder.append(sp);
		if (args.length > 0) {
			// 參數值
			ObjectMapper om = new ObjectMapper();
			String val = "";
			for (Object arg : args) {
				try {
					val = om.writeValueAsString(arg);
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

	public <ReturnType> RefreshCacheJob addRefreshCacheJob(ApplicationContext ctx, JobHelper jobHelper //
			, String cacheName, String key, Supplier<ReturnType> supplier) {
		if (ctx != null && jobHelper != null && 
			cacheName != null && !cacheName.isEmpty() &&
			key != null && !key.isEmpty()) {

			try {
				RefreshCacheJob job = (RefreshCacheJob) ctx.getBean("refreshCacheJob", cacheName, key, supplier);
				if (job != null) {
					jobHelper.add(job);
					return job;
				}
			} catch (Exception e) {
				// do nothing...
			}
		}
		return null;
	}

}
