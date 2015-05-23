package sanekp.seriesinformer.ui;

import sanekp.seriesinformer.core.xml.SeriesList;
import sanekp.seriesinformer.core.xml.XmlManager;
import sanekp.seriesinformer.ui.worker.Task;

import javax.xml.bind.JAXBException;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by sanek_000 on 8/9/2014.
 */
public class SeriesInformer {
    private static Logger logger = Logger.getLogger(SeriesInformer.class.getName());

    public static void main(String[] args) {
        try {
            LogManager.getLogManager().readConfiguration(SeriesInformer.class.getResourceAsStream("/logging.properties"));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to read logger properties", e);
        }
        logger.log(Level.FINE, "Starting");
        URL resource = Thread.currentThread().getContextClassLoader().getResource("images/tray.png");
        Image image = Toolkit.getDefaultToolkit().getImage(resource);
        final SystemTray systemTray = SystemTray.getSystemTray();
        final TrayIcon trayIcon = new TrayIcon(image, "Series Informer");
        trayIcon.setImageAutoSize(true);
        try {
            systemTray.add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }
        final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        final ExecutorService threadPool = Executors.newSingleThreadExecutor();
        trayIcon.addActionListener(e -> {
            systemTray.remove(trayIcon);
            scheduledExecutorService.shutdownNow();
            threadPool.shutdownNow();
        });
        try {
            logger.log(Level.FINE, "Loading series");
            XmlManager xmlManager = new XmlManager();
            SeriesList seriesList = xmlManager.load(Thread.currentThread().getContextClassLoader().getResource("db/db.xml"));
            logger.log(Level.FINE, "{0} series is loaded", seriesList.getSeries().size());
            trayIcon.displayMessage("Series Informer", "Loaded " + seriesList.getSeries().size(), TrayIcon.MessageType.INFO);
            Task task = new Task(threadPool, seriesList, trayIcon);
            scheduledExecutorService.scheduleAtFixedRate(task, 0, 45, TimeUnit.MINUTES);
        } catch (JAXBException e) {
            logger.log(Level.WARNING, "Series loading failed", e);
        }
    }
}
