package sanekp.seriesinformer.core.xml;

import javax.xml.bind.annotation.XmlRegistry;

/**
 * Created by sanek_000 on 5/25/2014.
 */
@XmlRegistry
public class ObjectFactory {
    public SeriesList createSeriesList() {
        return new SeriesList();
    }

    public Series createSeries() {
        return new Series();
    }
}
