package sanekp.seriesinformer.ui.worker;

import sanekp.seriesinformer.core.spi.Source;
import sanekp.seriesinformer.core.spi.SourceManager;
import sanekp.seriesinformer.core.xml.Series;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by sanek_000 on 8/9/2014.
 */
public class Checker implements Runnable {
    private static Logger logger = Logger.getLogger(Checker.class.getName());
    private Series series;
    private TrayIcon trayIcon;
    private ServiceLoader<Source> sources = SourceManager.getSources();

    public Checker(Series series, TrayIcon trayIcon) {
        this.series = series;
        this.trayIcon = trayIcon;
    }

    @Override
    public void run() {
        sources.forEach(source -> {
            logger.log(Level.FINE, "Start looking for next episode for {0}", series.getName());
            Series next = source.getNext(series);
            if (next != null) {
                logger.log(Level.FINE, "Next episode found for {0}", series.getName());
                trayIcon.displayMessage(series.getName(), "let's go to watch s" + next.getSeason() + " e" + next.getEpisode(), TrayIcon.MessageType.INFO);
                System.out.println(next.getUrl());
                Arrays.stream(trayIcon.getActionListeners()).forEach(trayIcon::removeActionListener);
                trayIcon.addActionListener(getActionListeners -> {
                    try {
                        Runtime.getRuntime().exec(new String[]{"C:/Program Files (x86)/DAUM/PotPlayer/PotPlayerMini.exe", next.getUrl()});
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                logger.log(Level.FINE, "Next episode hasn''t been found for {0}", series.getName());
            }
        });
    }
}
