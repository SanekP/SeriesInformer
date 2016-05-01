package sanekp.seriesinformer.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import sanekp.seriesinformer.ui.tray.TrayManager;
import sanekp.seriesinformer.ui.worker.Task;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;

/**
 * Created by sanek_000 on 8/9/2014.
 */
@Configuration
@ComponentScan(basePackages = "sanekp.seriesinformer")
@PropertySource(value = {"file:prop.properties"}, ignoreResourceNotFound = false)
public class SeriesInformer {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrayManager.class);

    static {
        try {
            LogManager.getLogManager().readConfiguration(SeriesInformer.class.getResourceAsStream("/logging.properties"));  //  TODO get rid of jul
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ScheduledExecutorService scheduledExecutorService;

    @Autowired
    private AnnotationConfigApplicationContext applicationContext;
    @Autowired
    private ObjectFactory<Task> taskFactory;

    public static void main(String[] args) {
        LOGGER.info("main thread has started");
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(SeriesInformer.class);
        LOGGER.debug("Spring context is loaded");
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
        LOGGER.info("Shutting down");
        applicationContext.close();
    }
}
