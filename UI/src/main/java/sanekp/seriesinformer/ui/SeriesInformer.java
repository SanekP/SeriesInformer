package sanekp.seriesinformer.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import sanekp.seriesinformer.core.broker.Producer;
import sanekp.seriesinformer.core.manager.DbManager;
import sanekp.seriesinformer.core.model.SeriesDto;
import sanekp.seriesinformer.ui.tray.TrayManager;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by sanek_000 on 8/9/2014.
 */
@Configuration
@ComponentScan(basePackages = "sanekp.seriesinformer")
@PropertySource(value = {"file:prop.properties"}, ignoreResourceNotFound = false)
@ImportResource("classpath:spring.xml")
public class SeriesInformer {
    private static final Logger LOGGER = LoggerFactory.getLogger(SeriesInformer.class);

    private ScheduledExecutorService scheduledExecutorService;

    @Autowired
    private AnnotationConfigApplicationContext applicationContext;
    @Autowired
    private DbManager dbManager;
    @Autowired
    private TrayManager trayManager;
    @Autowired
    private Producer producer;

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
        scheduledExecutorService.scheduleAtFixedRate(this::checkNew, 0, 20, TimeUnit.MINUTES);
        List<SeriesDto> seriesDtos = dbManager.getSeriesDtos();
        trayManager.setSeriesDtos(seriesDtos);
    }

    public void checkNew() {
        List<SeriesDto> seriesDtos = dbManager.getSeriesDtos();
        producer.lookFor(seriesDtos);
    }

    public void exit() {
        LOGGER.info("Shutting down");
        applicationContext.close();
    }

    @PreDestroy
    public void close() {
        scheduledExecutorService.shutdown();
    }
}
