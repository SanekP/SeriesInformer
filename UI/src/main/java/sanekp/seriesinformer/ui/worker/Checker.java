package sanekp.seriesinformer.ui.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sanekp.seriesinformer.core.spi.Source;
import sanekp.seriesinformer.core.spi.SourceManager;
import sanekp.seriesinformer.core.xml.Series;
import sanekp.seriesinformer.ui.tray.TrayManager;

import java.util.ServiceLoader;
import java.util.concurrent.Callable;

/**
 * Created by sanek_000 on 8/9/2014.
 */
public class Checker implements Callable<Series> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrayManager.class);

    private Series series;
    private ServiceLoader<Source> sources = SourceManager.getSources();

    public Checker(Series series) {
        this.series = series;
    }

    @Override
    public Series call() throws Exception {
        for (Source source : sources) {
            LOGGER.debug("Source {0}", source);
            LOGGER.debug("Start looking for next episode for {0} s{1} e{2}", series.getName(), series.getSeason(), series.getEpisode());
            Series next = source.getNext(series);
            if (next != null) {
                LOGGER.debug("Next episode found for {0}", series.getName());
                return next;
            } else {
                LOGGER.debug("Next episode hasn''t been found for {0}", series.getName());
            }
        }
        LOGGER.debug("Next series of {0} wasn''t found", series.getName());
        return null;
    }
}
