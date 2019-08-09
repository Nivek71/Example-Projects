package nivek71.api.utility.containers;

import java.util.Map;

public class ImmutablePair<A, B> {
    public static final ImmutablePair EMPTY_IMMUTABLE_PAIR = new ImmutablePair<Object, Object>(null, null) {
        @Override
        public String toString() {
            return "EMPTY_IMMUTABLE_PAIR";
        }
    };
    private final A first;
    private final B second;

    public ImmutablePair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }

    @Override
    public String toString() {
        return "ImmutablePair{" + first + ',' + second + '}';
    }

    public static <A, B> ImmutablePair<A, B> create(Map.Entry<A, B> entry) {
        return new ImmutablePair<>(entry.getKey(), entry.getValue());
    }

    public static <A, B> ImmutablePair<A, B> emptyPair() {
        //noinspection unchecked
        return EMPTY_IMMUTABLE_PAIR;
    }
}
