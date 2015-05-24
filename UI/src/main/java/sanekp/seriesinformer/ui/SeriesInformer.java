package sanekp.seriesinformer.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import sanekp.seriesinformer.core.xml.SeriesList;
import sanekp.seriesinformer.core.xml.XmlManager;
import sanekp.seriesinformer.ui.tray.TrayManager;
import sanekp.seriesinformer.ui.worker.Task;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by sanek_000 on 8/9/2014.
 */
@Configuration
@ComponentScan(basePackages = "sanekp.seriesinformer")
public class SeriesInformer {
    private static Logger logger;

    static {
        try {
            LogManager.getLogManager().readConfiguration(SeriesInformer.class.getResourceAsStream("/logging.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger = Logger.getLogger(SeriesInformer.class.getName());
    }

    @Autowired
    private XmlManager xmlManager;
    @Autowired
    private TrayManager trayManager;
    @Autowired
    private File dbPath;

    public static void main(String[] args) {
        logger.log(Level.FINE, "Starting");
        logger.log(Level.FINE, "Loading Spring context");
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(SeriesInformer.class);
        logger.log(Level.FINE, "Spring context is loaded");
        final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        Task task = applicationContext.getBean(Task.class);
        scheduledExecutorService.scheduleAtFixedRate(task, 0, 45, TimeUnit.MINUTES);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.log(Level.INFO, "Shutting down");
            scheduledExecutorService.shutdown();
            applicationContext.close();
            logger.log(Level.INFO, "Exiting");
        }));
    }

    @Bean
    public File getDbPath() {
        return new File("d:/db.xml");
    }

    public SeriesList loadSeries() {
        try {
            logger.log(Level.FINE, "Loading series");
            SeriesList seriesList = xmlManager.load(dbPath);
            logger.log(Level.FINE, "{0} series are loaded", seriesList.getSeries().size());
            trayManager.displayInfoMessage("Loaded " + seriesList.getSeries().size());
            return seriesList;
        } catch (JAXBException e) {
            logger.log(Level.WARNING, "Series loading failed", e);
        }
        return null;
    }

    public void saveSeries(SeriesList seriesList) {
        try {
            logger.log(Level.INFO, "Storing series");
            xmlManager.save(seriesList, dbPath);
            logger.log(Level.INFO, "Series are stored");
        } catch (JAXBException e) {
            logger.log(Level.WARNING, "Failed to store", e);
        }
    }
}
