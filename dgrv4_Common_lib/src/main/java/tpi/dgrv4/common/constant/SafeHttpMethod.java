package tpi.dgrv4.common.constant;

import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;

import java.util.HashMap;
import java.util.Map;

// Science spring6.0 the resolve method has been removed from HttpMethod class, this enum is used to check http method.
public enum SafeHttpMethod {
    GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE;

    private static final Map<String, HttpMethod> mappings = new HashMap<>(16);

    static {
        for (SafeHttpMethod method : values()) {
            mappings.put(method.name(), HttpMethod.valueOf(method.name()));
        }
    }

    @Nullable
    public static SafeHttpMethod resolve(@Nullable String method) {
        if (method == null) {
            return null;
        }
        try {
            return SafeHttpMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static SafeHttpMethod safeValueOf(String method, boolean caseSensitive) throws TsmpDpAaException {
        String methodName = caseSensitive ? method : method.toUpperCase();
        try {
            return SafeHttpMethod.valueOf(methodName);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown HTTP method: " + method, e);
        }
    }
}