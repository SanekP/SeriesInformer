package sanekp.seriesinformer.core.model;

import java.util.List;

/**
 * Created by Sanek on 5/1/2016.
 */
public class SeriesDto {
    private String name;
    private String url;
    private Viewed viewed;
    private Next next;
    private List<String> quality;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Viewed getViewed() {
        return viewed;
    }

    public void setViewed(Viewed viewed) {
        this.viewed = viewed;
    }

    public Next getNext() {
        return next;
    }

    public void setNext(Next next) {
        this.next = next;
    }

    public List<String> getQuality() {
        return quality;
    }

    public void setQuality(List<String> quality) {
        this.quality = quality;
    }
}
