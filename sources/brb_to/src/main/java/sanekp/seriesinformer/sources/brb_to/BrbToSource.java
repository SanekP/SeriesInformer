package sanekp.seriesinformer.sources.brb_to;

import sanekp.seriesinformer.core.spi.Source;
import sanekp.seriesinformer.core.xml.Series;

import java.io.IOException;
import java.net.URL;

/**
 * Created by sanek_000 on 8/25/2014.
 */
public class BrbToSource implements Source {
    @Override
    public Series getNext(Series series) {
        try (BrbToParser parser = new BrbToParser()) {
            URL url = new URL(series.getUrl());
            parser.open(url);
            boolean next = parser.isNext(series.getSeason(), series.getEpisode());
            if (next) {
                Series newSeries = new Series();
                newSeries.setName(series.getName());
                newSeries.setUrl(parser.getNext(series.getSeason(), series.getEpisode(), "1080"));
                return newSeries;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
