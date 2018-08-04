package uk.callumr.eventstore.core.internal;

import org.junit.Test;

import java.util.Iterator;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class ReadableReducingStreamTest {

    private ReadableReducingStream<Integer, String> readableReducingStream = new ReadableReducingStream<>(
            IntStream.range(0, 3).boxed(),
            "",
            (string, i) -> string + i);

    @Test
    public void should_allow_reading_of_the_stream() {
        canReadTheStream();
    }

    @Test
    public void get_the_reduction() {
        canGetTheReduction();
    }

    @Test
    public void return_the_stream_and_get_the_reduction() {
        canReadTheStream();
        canGetTheReduction();
    }

    @Test
    public void get_the_reduction_then_return_the_stream() {
        canGetTheReduction();
        canReadTheStream();
    }

    @Test
    public void can_read_some_of_the_stream_get_the_reduction_then_read_the_rest_of_the_stream() {
        Iterator<Integer> iterator = readableReducingStream.stream().iterator();
        assertThat(iterator.next()).isEqualTo(0);

        canGetTheReduction();

        assertThat(iterator).containsExactly(1, 2)
;    }

    private void canReadTheStream() {
        assertThat(readableReducingStream.stream()).containsExactly(0, 1, 2);
    }

    private void canGetTheReduction() {
        assertThat(readableReducingStream.reduction()).isEqualTo("012");
    }
}