package sanekp.seriesinformer.core.converter;

import org.springframework.core.convert.converter.Converter;
import sanekp.seriesinformer.core.model.SeriesDto;
import sanekp.seriesinformer.core.model.Viewed;
import sanekp.seriesinformer.core.xml.Series;

/**
 * Created by Sanek on 5/1/2016.
 */
public class SeriesToSeriesDtoConverter implements Converter<Series, SeriesDto> {
    @Override
    public SeriesDto convert(Series series) {
        SeriesDto seriesDto = new SeriesDto();
        seriesDto.setName(series.getName());
        seriesDto.setUrl(series.getUrl());
        seriesDto.setQuality(series.getQuality());

        Viewed viewed = new Viewed();
        viewed.setSeason(series.getSeason());
        viewed.setEpisode(series.getEpisode());
        seriesDto.setViewed(viewed);
        return seriesDto;
    }
}
