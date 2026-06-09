package tpi.dgrv4.dpaa.service.smartOnFhirProxyService.tool;

import java.util.Optional;

public final class ValueOrDefault<T> {
    private final T value;
    
    ValueOrDefault(T value) {
        this.value = value;
    }
    
    public Optional<T> optional() {
        return Optional.ofNullable(value);
    }
    
    public T get() {
        return value;
    }
    
    @Override
    public String toString() {
        return "ValueOrDefault(" + value + ")";
    }
}
