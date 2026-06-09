package tpi.dgrv4.dpaa.service.smartOnFhirProxyService.tool;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 中間狀態物件：表示條件式拋出例外的邏輯
 * 如果條件符合則拋出例外，否則可以取得預設值
 */
public final class ConditionalThrow<T> {
    private final Result<T> result;
    private final boolean shouldThrow;
    private final String errorMessage;
    
    ConditionalThrow(Result<T> result, boolean shouldThrow, String errorMessage) {
        this.result = result;
        this.shouldThrow = shouldThrow;
        this.errorMessage = errorMessage;
    }
    
    /**
     * 如果條件符合則拋出例外，否則回傳預設值
     */
    public T getOrElse(T defaultValue) {
        if (shouldThrow) {
            throw new RuntimeException(errorMessage);
        }
        return result.getOrElse(defaultValue);
    }
    
    /**
     * 如果條件符合則拋出例外，否則用 supplier 產生預設值
     */
    public T getOrElse(Supplier<T> defaultSupplier) {
        if (shouldThrow) {
            throw new RuntimeException(errorMessage);
        }
        return result.getOrElse(defaultSupplier);
    }
    
    /**
     * 如果條件符合則拋出例外，否則取得值或預設值（可繼續呼叫 optional()）
     */
    public ValueOrDefault<T> getOrElse() {
        if (shouldThrow) {
            throw new RuntimeException(errorMessage);
        }
        return result.getOrElse();
    }
    
    /**
     * 如果條件符合則拋出客製化例外，否則取得值或預設值
     */
    public T getOrThrow(Function<String, ? extends RuntimeException> exceptionMapper) {
        if (shouldThrow) {
            throw exceptionMapper.apply(errorMessage);
        }
        return result.getOrElse((T) null);
    }
    
    /**
     * 取得底層的 Result
     */
    public Result<T> getResult() {
        return result;
    }
}
