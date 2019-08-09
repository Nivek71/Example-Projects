package nivek71.api.utility.functions;

@FunctionalInterface
public interface TriFunctionEx<T, U, S, R> {
    static <T, U, S, R> TriFunctionEx<T, U, S, R> doNothing(R value) {
        return (t, u, s) -> value;
    }

    static <T, U, S, R> TriFunctionEx<T, U, S, R> doNothing() {
        return doNothing(null);
    }

    default R apply(T t, U u, S s) {
        try {
            return applyEx(t, u, s);
        } catch (Exception ex) {
            throw new WrappedException(ex);
        }
    }

    R applyEx(T t, U u, S s) throws Exception;
}
