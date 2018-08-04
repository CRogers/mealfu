package uk.callumr.eventstore.core.internal;

import org.junit.Test;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class ReadableReducingStreamTest {
    @Test
    public void should_allow_reading_of_the_stream() {
        ReadableReducingStream<Integer, String> readableReducingStream = new ReadableReducingStream<>(
                IntStream.range(0, 4).boxed(),
                "",
                (string, i) -> string + i);

        assertThat(readableReducingStream.stream()).containsExactly(0, 1, 2, 3);
    }

}