package nivek71.api.utility.functions;

import java.util.function.Consumer;

@FunctionalInterface
public interface ConsumerEx<T> extends Consumer<T> {
    static <T> ConsumerEx<T> doNothing() {
        return t -> {
        };
    }

    void acceptEx(T t) throws Exception;

    @Override
    default void accept(T t) {
        try {
            acceptEx(t);
        } catch (Exception ex) {
            throw new WrappedException(ex);
        }
    }
}
