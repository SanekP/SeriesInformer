package sanekp.seriesinformer.core.spi;

import sanekp.seriesinformer.core.xml.Series;

/**
 * Created by sanek_000 on 8/25/2014.
 */
public interface Source {
    /**
     * Searches for next episode
     *
     * @param series contain catalog-url in url property
     * @return next series where
     * url property is file-url
     * season and episode contain next values
     * otherwise it returns null
     */
    Series getNext(Series series);
}
