package sanekp.seriesinformer.sources.brb_to_v2;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import sanekp.seriesinformer.core.spi.Source;
import sanekp.seriesinformer.core.xml.Series;

import java.io.IOException;
import java.util.List;

/**
 * Created by Sanek on 8/29/2015.
 */
public class BrbToSource implements Source {
    @Override
    public Series getNext(Series series) {
        Series nextSeries = new Series();
        nextSeries.setUrl(series.getUrl());
        nextSeries.setSeason(series.getSeason());
        nextSeries.setEpisode(series.getEpisode() + 1);
        Series nextEpisode = tryNext(nextSeries);
        if (nextEpisode != null) {
            return nextEpisode;
        } else {
            System.out.println("Let's try next season");
            nextSeries.setSeason(series.getSeason() + 1);
            nextSeries.setEpisode(1);
            return tryNext(nextSeries);
        }
    }

    public Series tryNext(Series series) {
        try (WebClient webClient = new WebClient()) {
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.getOptions().setCssEnabled(false);
            System.out.println(series.getUrl() + "?ajax&folder=0");
            HtmlPage page = webClient.getPage(series.getUrl() + "?ajax&folder=0");
            HtmlAnchor seasonAnchor = page.getFirstByXPath("//a[contains(b/text(),'" + series.getSeason() + "')]");
            if (seasonAnchor == null) {
                System.out.println("Season " + series.getSeason() + " unavailable");
                return null;
            }
            String season = seasonAnchor.getAttribute("name");
            String seasonUrl = series.getUrl() + "?ajax&folder=" + season.substring(2, season.length());
            System.out.println(seasonUrl);
            page = webClient.getPage(seasonUrl);

            HtmlAnchor translationAnchor = page.getFirstByXPath("//a");
            String translation = translationAnchor.getAttribute("name");
            String translationUrl = series.getUrl() + "?ajax&folder=" + translation.substring(2, translation.length());
            System.out.println(translationUrl);
            page = webClient.getPage(translationUrl);

            for (HtmlAnchor releaseAnchor : (List<HtmlAnchor>) page.getByXPath("//a[@class='link-subtype title']")) {
                String release = releaseAnchor.getAttribute("name");
                String releaseUrl = series.getUrl() + "?ajax&folder=" + release.substring(2, release.length());
                System.out.println(releaseUrl);
                page = webClient.getPage(releaseUrl);

                HtmlAnchor fileAnchor = page.getFirstByXPath("//a[contains(@id, '1080') and contains(@href, '" + String.format("S%02dE%02d", series.getSeason(), series.getEpisode()) + "')]");
                if (fileAnchor != null) {
                    String url = fileAnchor.getAttribute("href");
                    series.setUrl("http://brb.to" + url);
                    System.out.println(series.getUrl());
                    return series;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
