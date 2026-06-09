package tpi.dgrv4.dpaa.service.smartOnFhirProxyService.tool;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class Success<T> implements Result<T> {
    private final Optional<T> value;

    Success(Optional<T> value) {
        this.value = java.util.Objects.requireNonNull(value, "Optional value must not be null");
    }

    @Override
    public Optional<T> get() {
        return value;
    }

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public boolean isFailure() {
        return false;
    }

    @Override
    public boolean isPresent() {
        return value.isPresent();
    }

    @Override
    public boolean isEmpty() {
        return value.isEmpty();
    }

    @Override
    public T getOrElse(T defaultValue) {
        return value.orElse(defaultValue);
    }

    @Override
    public T getOrElse(Supplier<T> defaultSupplier) {
        return value.orElseGet(defaultSupplier);
    }

    @Override
    public ValueOrDefault<T> getOrElse() {
        return new ValueOrDefault<>(value.orElse(null));
    }

    @Override
    public T getOrThrow() {
        return value.orElseThrow(() -> 
            new IllegalStateException("Success but value is empty")
        );
    }

    @Override
    public T getOrThrow(Function<String, ? extends RuntimeException> exceptionMapper) {
        return value.orElseThrow(() -> 
            exceptionMapper.apply("Value is empty")
        );
    }

    @Override
    public ConditionalThrow<T> getOrThrowCondition(Predicate<String> shouldThrow) {
        return new ConditionalThrow<>(this, false, null);
    }

    @Override
    public ConditionalThrow<T> getOrThrowCondition(Predicate<Result<T>> shouldThrow, boolean useResultAsInput) {
        boolean condition = shouldThrow.test(this);
        return new ConditionalThrow<>(this, condition, "Condition matched");
    }

    @Override
    public <U> Result<U> map(Function<T, U> mapper) {
        try {
            return new Success<>(value.map(mapper));
        } catch (Exception e) {
            return new Failure<>(e.getMessage(), e);
        }
    }

    @Override
    public <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
        try {
            return value.map(mapper).orElseGet(Result::empty);
        } catch (Exception e) {
            return new Failure<>(e.getMessage(), e);
        }
    }

    @Override
    public Result<T> ifPresent(Consumer<T> action) {
        value.ifPresent(action);
        return this;
    }

    @Override
    public Result<T> onFailure(Consumer<String> action) {
        return this;
    }

    @Override
    public Optional<String> getErrorMessage() {
        return Optional.empty();
    }

    @Override
    public Optional<T> toOptional() {
        return value;
    }

    @Override
    public String toString() {
        return "Success(" + value + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Success<?> other)) return false;
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
