package nivek71.api.utility.functions;

public class WrappedException extends RuntimeException {
    public WrappedException() {
    }

    public WrappedException(String s) {
        super(s);
    }

    public WrappedException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public WrappedException(Throwable throwable) {
        super(throwable);
    }

    private static final long serialVersionUID = -6606135253874916674L;
}
