package nivek71.api.utility.containers;

import java.util.Map;

public class Pair<A, B> {
    private A first;
    private B second;

    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public Pair() {
    }

    public A getFirst() {
        return first;
    }

    public void setFirst(A first) {
        this.first = first;
    }

    public B getSecond() {
        return second;
    }

    public void setSecond(B second) {
        this.second = second;
    }

    @Override
    public String toString() {
        return "Pair{" + first + ',' + second + '}';
    }

    public static <A, B> Pair<A, B> create(Map.Entry<A, B> entry) {
        return new Pair<>(entry.getKey(), entry.getValue());
    }
}
