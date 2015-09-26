package sanekp.seriesinformer.ui;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import sanekp.seriesinformer.ui.worker.Task;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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
@PropertySource(value = {"file:prop.properties"}, ignoreResourceNotFound = false)
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

    private ScheduledExecutorService scheduledExecutorService;

    @Autowired
    private AnnotationConfigApplicationContext applicationContext;
    @Autowired
    private ObjectFactory<Task> taskFactory;

    public static void main(String[] args) {
        logger.log(Level.FINE, "Loading Spring context");
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(SeriesInformer.class);
        logger.log(Level.FINE, "Spring context is loaded");
        applicationContext.registerShutdownHook();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @PostConstruct
    public void init() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(taskFactory.getObject(), 0, 45, TimeUnit.MINUTES);
    }

    @PreDestroy
    public void close() {
        scheduledExecutorService.shutdown();
    }

    public void exit() {
        logger.log(Level.INFO, "Shutting down");
        applicationContext.close();
    }
}
