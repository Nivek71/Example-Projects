package nivek71.api.utility.functions;

@FunctionalInterface
public interface RunnableEx extends Runnable {
    static RunnableEx doNothing() {
        return () -> {
        };
    }

    @Override
    default void run() {
        try {
            runEx();
        } catch (Exception ex) {
            throw new WrappedException(ex);
        }
    }

    void runEx() throws Exception;
}
