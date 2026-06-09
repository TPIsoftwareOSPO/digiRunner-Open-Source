package tpi.dgrv4.dpaa.service.smartOnFhirProxyService.tool;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class Failure<T> implements Result<T> {
    private final String message;
    private final RuntimeException exception;

    Failure(String message, RuntimeException exception) {
        this.message = message;
        this.exception = exception;
    }

    Failure(String message, Throwable cause) {
        this.message = message;
        this.exception = new RuntimeException(message, cause);
    }

    @Override
    public Optional<T> get() {
        throw exception;
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public boolean isFailure() {
        return true;
    }

    @Override
    public boolean isPresent() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public T getOrElse(T defaultValue) {
        return defaultValue;
    }

    @Override
    public T getOrElse(Supplier<T> defaultSupplier) {
        return defaultSupplier.get();
    }

    @Override
    public ValueOrDefault<T> getOrElse() {
        return new ValueOrDefault<>(null);
    }

    @Override
    public T getOrThrow() {
        throw exception;
    }

    @Override
    public T getOrThrow(Function<String, ? extends RuntimeException> exceptionMapper) {
        throw exceptionMapper.apply(message);
    }

    @Override
    public ConditionalThrow<T> getOrThrowCondition(Predicate<String> shouldThrow) {
        boolean condition = shouldThrow.test(message);
        return new ConditionalThrow<>(this, condition, message);
    }

    @Override
    public ConditionalThrow<T> getOrThrowCondition(Predicate<Result<T>> shouldThrow, boolean useResultAsInput) {
        boolean condition = shouldThrow.test(this);
        return new ConditionalThrow<>(this, condition, message);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <U> Result<U> map(Function<T, U> mapper) {
        return (Result<U>) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
        return (Result<U>) this;
    }

    @Override
    public Result<T> ifPresent(Consumer<T> action) {
        return this;
    }

    @Override
    public Result<T> onFailure(Consumer<String> action) {
        action.accept(message);
        return this;
    }

    @Override
    public Optional<String> getErrorMessage() {
        return Optional.of(message);
    }

    @Override
    public Optional<T> toOptional() {
        return Optional.empty();
    }

    public RuntimeException getException() {
        return exception;
    }

    @Override
    public String toString() {
        return "Failure(" + message + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Failure<?> other)) return false;
        return message != null ? message.equals(other.message) : other.message == null;
    }

    @Override
    public int hashCode() {
        return message != null ? message.hashCode() : 0;
    }
}
