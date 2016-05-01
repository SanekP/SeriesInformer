package sanekp.seriesinformer.ui.tray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sanekp.seriesinformer.ui.SeriesInformer;

import javax.annotation.PreDestroy;
import java.awt.*;
import java.net.URL;
import java.util.Arrays;

/**
 * Created by sanek_000 on 5/24/2015.
 */
@Component
public class TrayManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrayManager.class);

    private SystemTray systemTray;
    private TrayIcon trayIcon;
    private PopupMenu popupMenu;

    @Autowired
    private SeriesInformer seriesInformer;

    public TrayManager() {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("images/tray.png");
        Image image = Toolkit.getDefaultToolkit().getImage(resource);
        systemTray = SystemTray.getSystemTray();
        trayIcon = new TrayIcon(image, "Series Informer");
        trayIcon.setImageAutoSize(true);
        popupMenu = new PopupMenu();
        MenuItem close = new MenuItem("Close");
        close.addActionListener(e -> seriesInformer.exit());
        popupMenu.add(close);
        trayIcon.setPopupMenu(popupMenu);
        try {
            systemTray.add(trayIcon);
        } catch (AWTException e) {
            LOGGER.warn("Failed to add icon into tray", e);
        }
    }

    public void displayInfoMessage(String message) {
        displayInfoMessage("Series Informer", message);
    }

    public void displayInfoMessage(String title, String message) {
        LOGGER.info("{}:{}", title, message);
        trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
    }

    public void setActionListener(Runnable runnable) {
        Arrays.stream(trayIcon.getActionListeners()).forEach(trayIcon::removeActionListener);
        trayIcon.addActionListener(e -> runnable.run());
    }

    public void addMenuItem(String name, Runnable runnable) {
        MenuItem close = new MenuItem(name);
        close.addActionListener(e -> {
            runnable.run();
            popupMenu.remove(close);
        });
        popupMenu.add(close);
    }

    @PreDestroy
    public void close() {
        // don't do this in shutdown hook
        systemTray.remove(trayIcon);
    }
}
