package sanekp.seriesinformer.sources.brb_to;

import sanekp.seriesinformer.core.spi.Source;
import sanekp.seriesinformer.core.xml.Series;

import java.io.IOException;
import java.net.URL;

/**
 * Created by sanek_000 on 8/25/2014.
 */
public class BrbToSource implements Source {

    private final BrbToParser brbToParser = new BrbToParser();

    @Override
    public Series getNext(Series series) {
        try {
            brbToParser.open(new URL(series.getUrl()));
            boolean next = brbToParser.isNext(series.getSeason(), series.getEpisode());
            if (next) {
                Series newSeries = new Series();
                newSeries.setName(series.getName());
                String url = null;
                if (series.getQuality().isEmpty()) {
                    url = brbToParser.getNext(series.getSeason(), series.getEpisode(), "1080");
                } else {
                    for (String quality : series.getQuality()) {
                        url = brbToParser.getNext(series.getSeason(), series.getEpisode(), quality);
                        if (url != null) {
                            break;
                        }
                    }
                }
                if (url == null) {
                    return null;
                }
                newSeries.setUrl(url);
                newSeries.setSeason(brbToParser.getSeason());
                newSeries.setEpisode(brbToParser.getEpisode());
                return newSeries;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
