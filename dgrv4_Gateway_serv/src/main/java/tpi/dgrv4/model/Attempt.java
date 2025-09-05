package tpi.dgrv4.model;

import java.util.Optional;

public class Attempt<T> {
    final T result;
    final Throwable error;

    public static <T> Attempt<T> success(T result) {
        return new Attempt<>(result, null);
    }

    public static <T> Attempt<T> failure(Throwable error) {
        return new Attempt<>(null, error);
    }

    private Attempt(T result, Throwable error) {
        this.result = result;
        this.error = error;
    }

    public Optional<T> success() {
        return Optional.ofNullable(result);
    }

    public Optional<Throwable> failure() {
        return Optional.ofNullable(error);
    }

    @Override
    public String toString() {
        var template = this.getClass().getSimpleName()+"(%s)";

        if (this.success().isPresent()) {
            return template.formatted("Success: "+result);
        } else if (this.failure().isPresent()) {
            return template.formatted("Error: " + error.getClass().getSimpleName() + " - " + error.getMessage());
        } else {
            return template.formatted("Neither success nor error - invalid state");
        }
    }
}
