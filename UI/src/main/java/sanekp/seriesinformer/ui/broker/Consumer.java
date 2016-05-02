package sanekp.seriesinformer.ui.broker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sanekp.seriesinformer.core.broker.Broker;
import sanekp.seriesinformer.core.model.SeriesDto;
import sanekp.seriesinformer.ui.tray.TrayManager;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Sanek on 5/2/2016.
 */
@Component
public class Consumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Consumer.class);

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean stopped;

    @Autowired
    private Broker broker;
    @Autowired
    private TrayManager trayManager;

    @PostConstruct
    public void init() {
        executorService.execute(() -> {
            while (!stopped) {
                try {
                    SeriesDto seriesDto = broker.poll(1, TimeUnit.SECONDS);
                    if (seriesDto != null) {
                        trayManager.update(seriesDto);
                    }
                } catch (InterruptedException e) {
                    LOGGER.warn("", e);
                }
            }
        });
    }

    @PreDestroy
    public void close() {
        stopped = true;
        executorService.shutdown();
    }
}
