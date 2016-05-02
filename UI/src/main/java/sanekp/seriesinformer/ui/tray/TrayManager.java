package sanekp.seriesinformer.ui.tray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sanekp.seriesinformer.core.broker.Producer;
import sanekp.seriesinformer.core.manager.DbManager;
import sanekp.seriesinformer.core.model.Next;
import sanekp.seriesinformer.core.model.SeriesDto;
import sanekp.seriesinformer.ui.SeriesInformer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sanek_000 on 5/24/2015.
 */
@Component
public class TrayManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrayManager.class);

    private SystemTray systemTray;
    private TrayIcon trayIcon;
    private PopupMenu popupMenu;
    private Map<String, MenuItem> menuItems;

    @Autowired
    private SeriesInformer seriesInformer;
    @Autowired
    private DbManager dbManager;
    @Autowired
    private Producer producer;

    @Value("${player}")
    private String player;

    @PostConstruct
    public void init() {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("images/tray.png");
        Image image = Toolkit.getDefaultToolkit().getImage(resource);
        systemTray = SystemTray.getSystemTray();
        trayIcon = new TrayIcon(image, "Series Informer");
        trayIcon.setImageAutoSize(true);
        popupMenu = new PopupMenu();

        MenuItem close = new MenuItem("Close");
        close.addActionListener(e -> seriesInformer.exit());
        popupMenu.add(close);

        MenuItem checkNew = new MenuItem("Check for new");
        checkNew.addActionListener(e -> seriesInformer.checkNew());
        popupMenu.add(checkNew);

        popupMenu.addSeparator();
        trayIcon.setPopupMenu(popupMenu);
        try {
            systemTray.add(trayIcon);
        } catch (AWTException e) {
            LOGGER.warn("Failed to add icon into tray", e);
        }

        menuItems = new HashMap<>();
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

    public void setSeriesDtos(List<SeriesDto> seriesDtos) {
//        menuItems.forEach(popupMenu::remove);
//        menuItems = new ArrayList<>(seriesDtos.size());
        seriesDtos.forEach(this::update);
    }

    public void update(SeriesDto seriesDto) {
        MenuItem menuItem = menuItems.get(seriesDto.getName());
        if (menuItem == null) {
            menuItem = new MenuItem();
            popupMenu.add(menuItem);
            menuItems.put(seriesDto.getName(), menuItem);
        }
        Next next = seriesDto.getNext();
        if (next == null) {
            menuItem.setLabel(seriesDto.getName() + " s" + seriesDto.getViewed().getSeason() + "e" + seriesDto.getViewed().getEpisode());
            menuItem.setEnabled(false);
        } else {
            menuItem.setLabel(seriesDto.getName() + " s" + next.getSeason() + "e" + next.getEpisode());
            menuItem.setEnabled(true);
            MenuItem finalMenuItem = menuItem;
            Arrays.stream(menuItem.getActionListeners()).forEach(menuItem::removeActionListener);
            menuItem.addActionListener(actionEvent -> {
                finalMenuItem.setEnabled(false);
                LOGGER.info("opening {}", seriesDto.getUrl());
                LOGGER.info("opening {}", next.getUrl());
                try {
                    Process process = Runtime.getRuntime().exec(new String[]{player, next.getUrl()});
                    seriesDto.getViewed().setSeason(next.getSeason());
                    seriesDto.getViewed().setEpisode(next.getEpisode());
                    seriesDto.setNext(null);
                    dbManager.update(seriesDto);
                    producer.lookFor(seriesDto);
                } catch (IOException e) {
                    LOGGER.warn("Runtime.getRuntime().exec failed", e);
                }
            });
        }

    }

    @PreDestroy
    public void close() {
        // don't do this in shutdown hook
        systemTray.remove(trayIcon);
    }
}
