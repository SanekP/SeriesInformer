package sanekp.seriesinformer.ui.worker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sanekp.seriesinformer.core.xml.Series;
import sanekp.seriesinformer.core.xml.SeriesList;
import sanekp.seriesinformer.ui.SeriesInformer;
import sanekp.seriesinformer.ui.tray.TrayManager;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by sanek_000 on 8/16/2014.
 */
@Component
public class Task implements Runnable {
    private static Logger logger = Logger.getLogger(Task.class.getName());
    private ExecutorService executorService;
    @Autowired
    private SeriesInformer seriesInformer;
    @Autowired
    private TrayManager trayManager;

    public Task() {
        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void run() {
        try {
            SeriesList seriesList = seriesInformer.loadSeries();
            HashSet<Future<Series>> futures = new HashSet<>();
            for (Series series : seriesList.getSeries()) {
                Future<Series> future = executorService.submit(new Checker(series));
                futures.add(future);
            }
            while (!futures.isEmpty()) {
                Iterator<Future<Series>> iterator = futures.iterator();
                while (iterator.hasNext()) {
                    Future<Series> future = iterator.next();
                    if (future.isDone()) {
                        try {
                            Series nextSeries = future.get();
                            if (nextSeries != null) {
                                trayManager.displayInfoMessage(nextSeries.getName(), "Let's go to watch s" + nextSeries.getSeason() + " e" + nextSeries.getEpisode());
                                trayManager.setActionListener(() -> {
                                    try {
                                        Runtime.getRuntime().exec(new String[]{"C:\\Program Files (x86)\\DAUM\\PotPlayer\\PotPlayerMini.exe", nextSeries.getUrl()});
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
                                        logger.log(Level.WARNING, "Runtime.getRuntime().exec failed");
                                    }
                                });
                            }
                        } catch (InterruptedException | ExecutionException e) {
                            logger.log(Level.WARNING, "Failed to get next series", e);
                        }
                        iterator.remove();
                    }
                }
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    logger.log(Level.WARNING, "Failed to sleep", e);
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
