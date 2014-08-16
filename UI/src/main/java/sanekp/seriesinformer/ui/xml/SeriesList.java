package sanekp.seriesinformer.ui.xml;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sanek_000 on 5/25/2014.
 */
@XmlRootElement(name = "list")
public class SeriesList {
    private List<Series> series;

    public List<Series> getSeries() {
        if (series == null) {
            series = new ArrayList<>();
        }
        return series;
    }

    public void setSeries(List<Series> series) {
        this.series = series;
    }
}
