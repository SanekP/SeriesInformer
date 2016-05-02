package sanekp.seriesinformer.sources.brb_to;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sanekp.seriesinformer.core.model.Next;
import sanekp.seriesinformer.core.model.SeriesDto;
import sanekp.seriesinformer.core.model.Viewed;
import sanekp.seriesinformer.core.spi.Source;
import sanekp.seriesinformer.core.xml.Series;

import java.io.IOException;
import java.net.URL;

/**
 * Created by sanek_000 on 8/25/2014.
 */
public class BrbToSource implements Source {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrbToSource.class);

    @Override
    public void getNext(SeriesDto seriesDto) {
        try (BrbToParser brbToParser = new BrbToParser()) {
            brbToParser.open(new URL(seriesDto.getUrl()));
            Viewed viewed = seriesDto.getViewed();
            boolean available = brbToParser.isNext(viewed.getSeason(), viewed.getEpisode());
            if (available) {
                Series newSeries = new Series();
                newSeries.setName(seriesDto.getName());
                String url = null;
                if (seriesDto.getQuality().isEmpty()) {
                    url = brbToParser.getNext(viewed.getSeason(), viewed.getEpisode(), "1080");
                } else {
                    for (String quality : seriesDto.getQuality()) {
                        url = brbToParser.getNext(viewed.getSeason(), viewed.getEpisode(), quality);
                        if (url != null) {
                            break;
                        }
                    }
                }
                if (url == null) {
                    return;
                }
                Next next = new Next();
                next.setUrl(url);
                next.setSeason(brbToParser.getSeason());
                next.setEpisode(brbToParser.getEpisode());
                seriesDto.setNext(next);
            }
        } catch (IOException e) {
            LOGGER.error("", e);
        }
    }
}
