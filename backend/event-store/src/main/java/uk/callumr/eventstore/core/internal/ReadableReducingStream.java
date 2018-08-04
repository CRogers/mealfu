package uk.callumr.eventstore.core.internal;

import java.util.function.BiFunction;
import java.util.stream.Stream;

public class ReadableReducingStream<T, R> {
    private final Stream<T> stream;
    private final R initialState;
    private final BiFunction<R, T, R> reducer;

    public ReadableReducingStream(Stream<T> stream, R initialState, BiFunction<R, T, R> reducer) {
        this.stream = stream;
        this.initialState = initialState;
        this.reducer = reducer;
    }

    public Stream<T> stream() {
        return stream;
    }

    public R reduction() {
        return stream.reduce(initialState, reducer, (a, b) -> {
            throw new UnsupportedOperationException();
        });
    }
}
