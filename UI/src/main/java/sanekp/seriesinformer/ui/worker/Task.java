package sanekp.seriesinformer.ui.worker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import sanekp.seriesinformer.core.xml.Series;
import sanekp.seriesinformer.core.xml.SeriesList;
import sanekp.seriesinformer.core.xml.XmlManager;

import javax.xml.bind.JAXBException;
import java.awt.*;
import java.io.File;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by sanek_000 on 8/16/2014.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Task implements Runnable {
    private static Logger logger = Logger.getLogger(Task.class.getName());
    private Executor executor;
    private SeriesList seriesList;
    private TrayIcon trayIcon;
    @Autowired
    private XmlManager xmlManager;

    public Task(Executor executor, SeriesList seriesList, TrayIcon trayIcon) {
        this.executor = executor;
        this.seriesList = seriesList;
        this.trayIcon = trayIcon;
    }

    @Override
    public void run() {
        System.out.println(xmlManager);
        for (Series series : seriesList.getSeries()) {
            executor.execute(new Checker(series, trayIcon));
        }
        logger.log(Level.INFO, "Storing");
        try {
            xmlManager.save(seriesList, new File("d:/db.xml"));
        } catch (JAXBException e) {
            logger.log(Level.WARNING, "Failed to store", e);
        }
    }
}
