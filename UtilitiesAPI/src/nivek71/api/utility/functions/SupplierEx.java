package nivek71.api.utility.functions;

import java.util.function.Supplier;

@FunctionalInterface
public interface SupplierEx<T> extends Supplier<T> {
    static <R> SupplierEx<R> doNothing(R value) {
        return () -> value;
    }

    static <R> SupplierEx<R> doNothing() {
        return doNothing(null);
    }

    @Override
    default T get() {
        try {
            return getEx();
        } catch (Exception ex) {
            throw new WrappedException(ex);
        }
    }

    T getEx() throws Exception;
}
