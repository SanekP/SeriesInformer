package sanekp.seriesinformer.ui.worker;

import sanekp.seriesinformer.core.xml.Series;
import sanekp.seriesinformer.core.xml.SeriesList;

import java.awt.*;
import java.util.concurrent.Executor;

/**
 * Created by sanek_000 on 8/16/2014.
 */
public class Task implements Runnable {
    private Executor executor;
    private SeriesList seriesList;
    private TrayIcon trayIcon;

    public Task(Executor executor, SeriesList seriesList, TrayIcon trayIcon) {
        this.executor = executor;
        this.seriesList = seriesList;
        this.trayIcon = trayIcon;
    }

    @Override
    public void run() {
        for (Series series : seriesList.getSeries()) {
            executor.execute(new Checker(series, trayIcon));
        }
    }
}
