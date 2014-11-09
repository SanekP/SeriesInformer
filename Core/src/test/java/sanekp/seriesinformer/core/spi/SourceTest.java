package sanekp.seriesinformer.core.spi;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

/**
 * Created by sanek_000 on 8/25/2014.
 */
public class SourceTest {
    @Test
    public void test() {
        ServiceLoader<Source> sourceServiceLoader = ServiceLoader.load(Source.class);
        sourceServiceLoader.forEach(System.out::println);
        long count = StreamSupport.stream(sourceServiceLoader.spliterator(), false).count();
        MatcherAssert.assertThat(count, CoreMatchers.not(0L));
    }
}
