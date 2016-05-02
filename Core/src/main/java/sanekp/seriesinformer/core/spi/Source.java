package sanekp.seriesinformer.core.spi;

import sanekp.seriesinformer.core.model.SeriesDto;

/**
 * Created by sanek_000 on 8/25/2014.
 */
public interface Source {
    /**
     * Searches for next episode
     * Implementation should set <strong>next</strong> field
     *
     * @param seriesDto instance of SeriesDto
     */
    void getNext(SeriesDto seriesDto);
}
