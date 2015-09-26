package sanekp.seriesinformer.ui.worker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sanekp.seriesinformer.core.xml.Series;
import sanekp.seriesinformer.core.xml.SeriesList;
import sanekp.seriesinformer.ui.SeriesInformer;
import sanekp.seriesinformer.ui.tray.TrayManager;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by sanek_000 on 8/16/2014.
 */
@Component
public class Task implements Runnable {
    private static Logger logger = Logger.getLogger(Task.class.getName());
    private ExecutorService executorService;
    private CompletionService<Series> completionService;
    @Autowired
    private SeriesInformer seriesInformer;
    @Autowired
    private TrayManager trayManager;

    @Value("${player}")
    private String player;

    public Task() {
        executorService = Executors.newSingleThreadExecutor();
        completionService = new ExecutorCompletionService<>(executorService);
    }

    @Override
    public void run() {
        try {
            SeriesList seriesList = seriesInformer.loadSeries();
            seriesList.getSeries().forEach(series -> completionService.submit(new Checker(series)));
            for (int i = 0; i < seriesList.getSeries().size(); i++) {
                try {
                    Future<Series> future = completionService.take();
                    Series nextSeries = future.get();
                    if (nextSeries != null) {
                        trayManager.displayInfoMessage(nextSeries.getName(), "Let's go to watch s" + nextSeries.getSeason() + " e" + nextSeries.getEpisode());
                        trayManager.addMenuItem(nextSeries.getName(), () -> {
                            try {
                                Runtime.getRuntime().exec(new String[]{player, nextSeries.getUrl()});
                                logger.log(Level.INFO, "{0} has been viewed", nextSeries.getName());
                                // TODO get rid of lookup same series
                                for (Series series : seriesList.getSeries()) {
                                    if (series.getName().equals(nextSeries.getName())) {
                                        series.setSeason(nextSeries.getSeason());
                                        series.setEpisode(nextSeries.getEpisode());
                                        seriesInformer.saveSeries(seriesList);
                                        break;
                                    }
                                }
                            } catch (IOException e) {
                                logger.log(Level.WARNING, "Runtime.getRuntime().exec failed", e);
                            }
                        });
                    }
                } catch (InterruptedException | ExecutionException e) {
                    logger.log(Level.WARNING, "Failed to get next series", e);
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception encountered during scheduled task execution", e);
        }
    }

    @PreDestroy
    public void close() {
        executorService.shutdown();
    }
}
