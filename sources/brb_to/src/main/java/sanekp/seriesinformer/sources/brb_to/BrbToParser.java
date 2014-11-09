package sanekp.seriesinformer.sources.brb_to;

import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sanek_000 on 5/25/2014.
 */
public class BrbToParser implements Closeable {
    private static final String STATUS_XPATH = "//div[@class='item-info']/table/tbody/tr[3]/td[2]";
    private static Pattern numberPattern = Pattern.compile("\\d+");
    private static Pattern episodePattern = Pattern.compile("s\\d+e(\\d+)", Pattern.CASE_INSENSITIVE);
    private static Pattern statusPattern = Pattern.compile("(\\d+)\\D+(\\d+)");
    private WebClient webClient;
    private HtmlPage htmlPage;

    public BrbToParser() {
        webClient = new WebClient();
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setCssEnabled(false);
    }

    public void open(URL url) throws IOException {
        htmlPage = webClient.getPage(url);
    }

    /**
     * It parses field Статус.
     *
     * @param season
     * @param episode
     * @return true if there season bigger or episode bigger
     */
    public boolean isNext(int season, int episode) {
        HtmlElement status = htmlPage.getFirstByXPath(STATUS_XPATH);
        Matcher statusMatcher = statusPattern.matcher(status.getTextContent());
        if (!statusMatcher.find()) {
            return false;   //  can not check
        }
        int fetchedSeason = Integer.valueOf(statusMatcher.group(1));
        if (fetchedSeason > season) {
            return true;
        } else if (fetchedSeason == season) {
            int fetchedEpisode = Integer.valueOf(statusMatcher.group(2));
            return fetchedEpisode > episode;
        } else {
            return false;
        }
    }

    /**
     * It searches for next episode's url.
     *
     * @param season
     * @param episode
     * @param quality can be 720 or 1080
     * @return
     * @throws IOException
     */
    public String getNext(int season, int episode, String quality) throws IOException {
        HtmlElement listButton = htmlPage.getFirstByXPath("//div[@id='page-item-file-list']//a");
        listButton.click();
        List<HtmlElement> seasonElements = (List<HtmlElement>) htmlPage.getByXPath("//div[@class='b-files-folders']//li[@class='folder' and contains(.//b/text(), 'сезон')]");
        for (HtmlElement seasonElement : seasonElements) {
            String name = ((HtmlElement) seasonElement.getFirstByXPath(".//a/b")).getTextContent();
            Matcher seasonMatcher = numberPattern.matcher(name);
            if (!seasonMatcher.find()) {
                continue;   //  it's not a season
            }
            if (Integer.parseInt(seasonMatcher.group()) < season) {
                continue;   //  it's less than needed
            }
//            String date = ((HtmlElement) seasonElement.getFirstByXPath("span[@class='material-date']")).getTextContent();
//            System.out.println(name + " " + date);
            HtmlElement seasonButton = seasonElement.getFirstByXPath(".//a");
            seasonButton.click();
            List<HtmlElement> releaseElements = (List<HtmlElement>) seasonElement.getByXPath(".//li[@class='folder' and contains(.//span[@class='material-size']/text(), '(" + quality + ")')]");
//            System.out.println("Releases: " + releaseElements.size());
            for (HtmlElement releaseElement : releaseElements) {
                HtmlElement releaseButton = releaseElement.getFirstByXPath("./div[2]/a[1]");
                releaseButton.click();
                List<HtmlAnchor> links = (List<HtmlAnchor>) releaseElement.getByXPath(".//a[@class='b-file-new__link-material-download']");
//                System.out.println("Links: " + links.size());
                for (HtmlAnchor link : links) {
                    String file = link.getHrefAttribute();
                    Matcher episodeMatcher = episodePattern.matcher(file);
                    if (!episodeMatcher.find()) {
                        continue;   //  it's not an episode
                    }
                    if (Integer.parseInt(episodeMatcher.group(1)) > episode) {
                        return "http://brb.to" + file;
                    }
                }
            }
            episode = 0;    //  if there is no more episodes, let's take first from next season
        }
        return null;    //  specified season was not found
    }

    @Override
    public void close() throws IOException {
        webClient.closeAllWindows();
    }
}
