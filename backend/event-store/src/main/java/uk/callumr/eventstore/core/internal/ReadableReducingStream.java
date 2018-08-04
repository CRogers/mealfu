package uk.callumr.eventstore.core.internal;

import com.google.common.collect.Iterators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ReadableReducingStream<T, R> {
    private final Logger log = LoggerFactory.getLogger(ReadableReducingStream.class);

    private final Iterator<T> iterator;
    private final SwitchingIterator<T> switchingIterator;
    private final BiFunction<R, T, R> reducer;
    private R state;

    public ReadableReducingStream(Stream<T> stream, R initialState, BiFunction<R, T, R> reducer) {
        iterator = stream
                .peek(item -> state = reducer.apply(state, item))
                .iterator();
        this.switchingIterator = new SwitchingIterator<>(iterator);
        this.state = initialState;
        this.reducer = reducer;
    }

    public Stream<T> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(switchingIterator, 0), false);
    }

    public R reduction() {
        if (iterator.hasNext()) {
            log.debug("Reduction has been performed before consuming the stream! This reads the whole stream into memory!");
            List<T> savedItems = new ArrayList<>();
            Iterators.addAll(savedItems, switchingIterator);
            switchingIterator.setBackingIterator(savedItems.iterator());
        }

        return state;
    }
}
