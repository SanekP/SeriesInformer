package sanekp.seriesinformer.core.spi;

import java.util.ServiceLoader;

/**
 * Created by sanek_000 on 8/25/2014.
 */
public class SourceManager {
    public static ServiceLoader<Source> getSources() {
        return ServiceLoader.load(Source.class);
    }
}
