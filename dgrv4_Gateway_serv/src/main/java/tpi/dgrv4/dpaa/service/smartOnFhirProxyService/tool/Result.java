package tpi.dgrv4.dpaa.service.smartOnFhirProxyService.tool;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public sealed interface Result<T> permits Success, Failure {
        
    /** 取得成功的 Optional 值，失敗則拋出例外 */
    Optional<T> get();
    
    /** 判斷是否成功 */
    boolean isSuccess();
    
    /** 判斷是否失敗 */
    boolean isFailure();
    
    /** 判斷是否成功且有值（非 empty） */
    boolean isPresent();
    
    /** 判斷是否成功但無值（empty） */
    boolean isEmpty();
        
    /** 取得值，失敗或空值則回傳預設值 */
    T getOrElse(T defaultValue);
    
    /** 取得值，失敗或空值則用 supplier 產生預設值 */
    T getOrElse(Supplier<T> defaultSupplier);
    
    /** 取得值或預設值，回傳中間物件可繼續呼叫 optional() */
    ValueOrDefault<T> getOrElse();
        
    /** 取得值，失敗則拋出預設例外 */
    T getOrThrow();
    
    /** 取得值，失敗則拋出客製化例外 */
    T getOrThrow(Function<String, ? extends RuntimeException> exceptionMapper);
        
    /** 
     * 條件式拋出例外
     * @param shouldThrow 判斷是否應該拋出例外的條件（接收錯誤訊息）
     * @return 中間物件，可繼續呼叫 getOrElse() 或 getOrThrow()
     */
    ConditionalThrow<T> getOrThrowCondition(Predicate<String> shouldThrow);
    
    /** 
     * 條件式拋出例外
     * @param shouldThrow 判斷是否應該拋出例外的條件（接收 Result 本身）
     * @return 中間物件，可繼續呼叫 getOrElse() 或 getOrThrow()
     */
    ConditionalThrow<T> getOrThrowCondition(Predicate<Result<T>> shouldThrow, boolean useResultAsInput);
        
    /** Map 轉換成功值 */
    <U> Result<U> map(Function<T, U> mapper);
    
    /** FlatMap 用於串接多個可能失敗的操作 */
    <U> Result<U> flatMap(Function<T, Result<U>> mapper);
    
    /** 成功且有值時執行 action */
    Result<T> ifPresent(Consumer<T> action);
    
    /** 失敗時執行 action */
    Result<T> onFailure(Consumer<String> action);
    
    /** 取得錯誤訊息 */
    Optional<String> getErrorMessage();
    
    /** 轉換為 Optional */
    Optional<T> toOptional();
        
    static <T> Result<T> success(T value) {
        return new Success<>(Optional.ofNullable(value));
    }
    
    static <T> Result<T> success(Optional<T> value) {
        return new Success<>(value);
    }
    
    static <T> Result<T> empty() {
        return new Success<>(Optional.empty());
    }
    
    static <T> Result<T> failure(String message) {
        return new Failure<>(message, new RuntimeException(message));
    }
    
    static <T> Result<T> failure(String message, RuntimeException exception) {
        return new Failure<>(message, exception);
    }
    
    static <T> Result<T> of(Supplier<T> supplier) {
        try {
            return success(supplier.get());
        } catch (Exception e) {
            return failure(e.getMessage(), 
                e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e));
        }
    }
}

