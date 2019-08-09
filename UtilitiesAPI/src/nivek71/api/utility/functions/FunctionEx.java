package nivek71.api.utility.functions;

import java.util.function.Function;

public interface FunctionEx<T, R> extends Function<T, R> {
    static <T, R> FunctionEx<T, R> doNothing(R value) {
        return (t) -> value;
    }

    static <T, R> FunctionEx<T, R> doNothing() {
        return doNothing(null);
    }

    R applyEx(T t) throws Exception;

    @Override
    default R apply(T t) {
        try {
            return applyEx(t);
        } catch (Exception ex) {
            throw new WrappedException(ex);
        }
    }
}
