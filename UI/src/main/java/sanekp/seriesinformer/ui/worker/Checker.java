package sanekp.seriesinformer.ui.worker;

import sanekp.seriesinformer.core.spi.Source;
import sanekp.seriesinformer.core.spi.SourceManager;
import sanekp.seriesinformer.core.xml.Series;

import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by sanek_000 on 8/9/2014.
 */
public class Checker implements Callable<Series> {
    private static Logger logger = Logger.getLogger(Checker.class.getName());
    private Series series;
    private ServiceLoader<Source> sources = SourceManager.getSources();

    public Checker(Series series) {
        this.series = series;
    }

    @Override
    public Series call() throws Exception {
        for (Source source : sources) {
            logger.log(Level.FINE, "Start looking for next episode for {0}", series.getName());
            Series next = source.getNext(series);
            if (next != null) {
                logger.log(Level.FINE, "Next episode found for {0}", series.getName());
                return next;
            } else {
                logger.log(Level.FINE, "Next episode hasn''t been found for {0}", series.getName());
            }
        }
        return null;
    }
}
