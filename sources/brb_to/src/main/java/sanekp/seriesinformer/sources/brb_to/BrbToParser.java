package sanekp.seriesinformer.sources.brb_to;

import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sanek_000 on 5/25/2014.
 */
public class BrbToParser {
    private Pattern numberPattern = Pattern.compile("\\d+");
    private Pattern episodePattern = Pattern.compile("(?<=e)\\d+", Pattern.CASE_INSENSITIVE);

    public String getNext(URL url, int season, int series, String quality) throws IOException {
        WebClient webClient = null;
        try {
            webClient = new WebClient();
            webClient.setAjaxController(new NicelyResynchronizingAjaxController());
            HtmlPage htmlPage = webClient.getPage(url);
            HtmlElement listButton = htmlPage.getFirstByXPath("//div[@id='page-item-file-list']//a");
            listButton.click();
            List<HtmlElement> seasonElements = (List<HtmlElement>) htmlPage.getByXPath("//div[@class='b-files-folders']//li[@class='folder' and contains(.//b/text(), 'сезон')]");
            System.out.println("Seasons: " + seasonElements.size());
            for (HtmlElement seasonElement : seasonElements) {
                String name = ((HtmlElement) seasonElement.getFirstByXPath(".//a/b")).getTextContent();
                Matcher seasonMatcher = numberPattern.matcher(name);
                if (!seasonMatcher.find()) {
                    System.out.println("Not found number in " + name);
                    continue;
                }
                System.out.println("Season #" + seasonMatcher.group());
                if (Integer.parseInt(seasonMatcher.group()) < season) {
                    System.out.println("It's less than needed " + season);
                    continue;
                }
                String date = ((HtmlElement) seasonElement.getFirstByXPath("span[@class='material-date']")).getTextContent();
                System.out.println(name + " " + date);
                HtmlElement seasonButton = seasonElement.getFirstByXPath(".//a");
                seasonButton.click();
                List<HtmlElement> releaseElements = (List<HtmlElement>) seasonElement.getByXPath(".//li[@class='folder' and contains(.//span[@class='material-size']/text(), '(" + quality + ")')]");
                System.out.println("Releases: " + releaseElements.size());
                for (HtmlElement releaseElement : releaseElements) {
                    HtmlElement releaseButton = releaseElement.getFirstByXPath("./div[2]/a[1]");
                    releaseButton.click();
                    List<HtmlAnchor> links = (List<HtmlAnchor>) releaseElement.getByXPath("//a[@class='b-file-new__link-material-download']");
                    System.out.println("Links: " + links.size());
                    for (HtmlAnchor link : links) {
                        String file = link.getHrefAttribute();
                        Matcher episodeMatcher = episodePattern.matcher(file);
                        if (!episodeMatcher.find()) {
                            System.out.println("Doesn't found episode in " + file);
                            continue;
                        }
                        System.out.println("Episode #" + episodeMatcher.group());
                        if (Integer.parseInt(episodeMatcher.group()) > series) {
                            return "http://brb.to" + file;
                        }
                        System.out.println(file);
                    }
                }
            }
        } finally {
            if (webClient != null) {
                webClient.closeAllWindows();
            }
        }
        return null;
    }
}
