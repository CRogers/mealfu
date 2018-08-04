package uk.callumr.eventstore.core.internal;

import org.junit.Test;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class ReadableReducingStreamTest {

    private ReadableReducingStream<Integer, String> readableReducingStream = new ReadableReducingStream<>(
            IntStream.range(0, 3).boxed(),
            "",
            (string, i) -> string + i);

    @Test
    public void should_allow_reading_of_the_stream() {
        assertThat(readableReducingStream.stream()).containsExactly(0, 1, 2);
    }

    @Test
    public void get_the_reduction() {
        assertThat(readableReducingStream.reduction()).isEqualTo("012");
    }
}