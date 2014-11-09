package sanekp.seriesinformer.ui;

import sanekp.seriesinformer.core.xml.SeriesList;
import sanekp.seriesinformer.core.xml.XmlManager;
import sanekp.seriesinformer.ui.worker.Task;

import javax.xml.bind.JAXBException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by sanek_000 on 8/9/2014.
 */
public class SeriesInformer {
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("com.gargoylesoftware");
        logger.setLevel(Level.OFF);
        Logger.getLogger("org.apache.http.client").setLevel(Level.OFF);
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
        final ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        trayIcon.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                systemTray.remove(trayIcon);
                scheduledExecutorService.shutdownNow();
                cachedThreadPool.shutdownNow();
            }
        });
        try {
            XmlManager xmlManager = new XmlManager();
            SeriesList seriesList = xmlManager.load(Thread.currentThread().getContextClassLoader().getResource("db/db.xml"));
            trayIcon.displayMessage("Series Informer", "Loaded " + seriesList.getSeries().size(), TrayIcon.MessageType.INFO);
            Task task = new Task(cachedThreadPool, seriesList, trayIcon);
            scheduledExecutorService.scheduleAtFixedRate(task, 0, 30, TimeUnit.MINUTES);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}
