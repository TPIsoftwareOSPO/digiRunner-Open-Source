package tpi.dgrv4.dpaa.component;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.http.HttpMethod;

public interface ApiHelper {

	public String call(String reqUrl, Map<String, Object> params, HttpMethod method) throws Exception;

	public default String getString(String key, Map<String, Object> params, boolean isAllowNull) {
		return getParam(key, params, isAllowNull //
				, () -> {return "";} //
				, (value) -> {return String.valueOf(value);});
	};

	public default <R> R getParam(String key, Map<String, Object> params, boolean isAllowNull //
			, Supplier<R> emptySupplier, Function<Object, R> function) {
		if (params == null) {
			if (isAllowNull) {
				return null;
			}
			return emptySupplier.get();
		}
		
		Object value = params.get(key);
		if (value == null) {
			if (isAllowNull) {
				return null;
			} else {
				return emptySupplier.get();
			}
		}

		return function.apply(value);
	}

}
