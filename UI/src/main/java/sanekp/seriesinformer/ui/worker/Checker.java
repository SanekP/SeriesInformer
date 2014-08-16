package sanekp.seriesinformer.ui.worker;

import sanekp.seriesinformer.sources.brb_to.BrbToParser;
import sanekp.seriesinformer.ui.xml.Series;

import java.awt.*;
import java.io.IOException;
import java.net.URL;

/**
 * Created by sanek_000 on 8/9/2014.
 */
public class Checker implements Runnable {
    private Series series;
    private TrayIcon trayIcon;

    public Checker(Series series, TrayIcon trayIcon) {
        this.series = series;
        this.trayIcon = trayIcon;
    }

    @Override
    public void run() {
        try (BrbToParser parser = new BrbToParser()) {
            URL url = new URL(series.getUrl());
            parser.open(url);
            boolean next = parser.isNext(series.getSeason(), series.getEpisode());
            if (next) {
                trayIcon.displayMessage(series.getName(), "let's go to watch", TrayIcon.MessageType.INFO);
                System.out.println(parser.getNext(series.getSeason(), series.getEpisode(), "1080"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
