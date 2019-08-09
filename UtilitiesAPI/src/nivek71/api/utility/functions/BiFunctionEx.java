package nivek71.api.utility.functions;

import java.util.function.BiFunction;

@FunctionalInterface
public interface BiFunctionEx<T, U, R> extends BiFunction<T, U, R> {
    static <T, U, R> BiFunctionEx<T, U, R> doNothing(R value) {
        return (t, u) -> value;
    }

    static <T, U, R> BiFunctionEx<T, U, R> doNothing() {
        return doNothing(null);
    }

    @Override
    default R apply(T t, U u) {
        try {
            return applyEx(t, u);
        } catch (Exception ex) {
            throw new WrappedException(ex);
        }
    }

    R applyEx(T t, U u) throws Exception;
}
