package sanekp.seriesinformer.core.broker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sanekp.seriesinformer.core.model.SeriesDto;
import sanekp.seriesinformer.core.model.Viewed;
import sanekp.seriesinformer.core.spi.Source;
import sanekp.seriesinformer.core.spi.SourceManager;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Sanek on 5/2/2016.
 */
@Component
public class Producer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Producer.class);

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private ServiceLoader<Source> sources = SourceManager.getSources();

    @Autowired
    private Broker broker;

    public void lookFor(List<SeriesDto> seriesDtos) {
        seriesDtos.forEach(seriesDto -> {
            if (seriesDto.getNext() == null) {
                lookFor(seriesDto);
            }
        });
    }

    public void lookFor(SeriesDto seriesDto) {
        executorService.execute(() -> {
            for (Source source : sources) {
                Viewed viewed = seriesDto.getViewed();
                LOGGER.info("Looking for next episode for {} s{}e{} with {}", seriesDto.getName(), viewed.getSeason(), viewed.getEpisode(), source);

                source.getNext(seriesDto);
                if (seriesDto.getNext() != null) {
                    LOGGER.info("Next episode found for {}", seriesDto.getName());
                    broker.add(seriesDto);
                    return;
                } else {
                    LOGGER.info("Next episode hasn't been found for {} in {}", seriesDto.getName(), source);
                }
            }
        });
    }

    @PreDestroy
    public void close() {
        executorService.shutdown();
    }
}
