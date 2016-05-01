package sanekp.seriesinformer.ui.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sanekp.seriesinformer.core.xml.DbManager;
import sanekp.seriesinformer.core.xml.Series;
import sanekp.seriesinformer.core.xml.SeriesList;
import sanekp.seriesinformer.ui.tray.TrayManager;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.*;

/**
 * Created by sanek_000 on 8/16/2014.
 */
@Component
public class Task implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrayManager.class);

    private ExecutorService executorService;
    private CompletionService<Series> completionService;

    @Autowired
    private DbManager dbManager;
    @Autowired
    private TrayManager trayManager;

    @Value("${player}")
    private String player;
    @Value("${showFound}")
    private boolean showFound;

    public Task() {
        executorService = Executors.newSingleThreadExecutor();
        completionService = new ExecutorCompletionService<>(executorService);
    }

    @Override
    public void run() {
        try {
            SeriesList seriesList = dbManager.load();
            trayManager.displayInfoMessage("Loaded " + seriesList.getSeries().size() + " series");
            seriesList.getSeries().forEach(series -> completionService.submit(new Checker(series)));
            for (int i = 0; i < seriesList.getSeries().size(); i++) {
                try {
                    Future<Series> future = completionService.take();
                    Series nextSeries = future.get();
                    if (nextSeries != null) {
                        if (showFound) {
                            trayManager.displayInfoMessage(nextSeries.getName(), "Let's go to watch s" + nextSeries.getSeason() + " e" + nextSeries.getEpisode());
                        }
                        trayManager.addMenuItem(nextSeries.getName() + " s" + nextSeries.getSeason() + "e" + nextSeries.getEpisode(), () -> {
                            try {
                                Runtime.getRuntime().exec(new String[]{player, nextSeries.getUrl()});
                                LOGGER.info("opening {}", nextSeries.getUrl());
                                dbManager.update(nextSeries);
                            } catch (IOException e) {
                                LOGGER.warn("Runtime.getRuntime().exec failed", e);
                            }
                        });
                    }
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.warn("Failed to get next series", e);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Exception encountered during scheduled task execution", e);
        }
        trayManager.displayInfoMessage("The search is over");
    }

    @PreDestroy
    public void close() {
        executorService.shutdown();
    }
}
