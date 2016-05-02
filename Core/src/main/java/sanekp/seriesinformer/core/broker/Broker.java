package sanekp.seriesinformer.core.broker;

import org.springframework.stereotype.Component;
import sanekp.seriesinformer.core.model.SeriesDto;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by Sanek on 5/2/2016.
 */
@Component
public class Broker {
    private BlockingQueue<SeriesDto> seriesDtos = new LinkedBlockingQueue<>();

    public boolean add(SeriesDto seriesDto) {
        return seriesDtos.add(seriesDto);
    }

    public SeriesDto poll(long timeout, TimeUnit unit) throws InterruptedException {
        return seriesDtos.poll(timeout, unit);
    }
}
