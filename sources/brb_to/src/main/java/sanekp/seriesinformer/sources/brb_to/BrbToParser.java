package sanekp.seriesinformer.sources.brb_to;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sanek_000 on 5/25/2014.
 */
public class BrbToParser implements Closeable {
    private static final String STATUS_XPATH = "//div[@class='item-info']/table/tbody/tr[3]/td[2]";
    static Pattern statusPattern = Pattern.compile("\\D+(\\d+)\\D+(\\d+)\\D+");
    private static Logger logger = Logger.getLogger(BrbToParser.class.getName());
    private static Pattern numberPattern = Pattern.compile("\\d+");
    private static Pattern episodePattern = Pattern.compile("s\\d+e(\\d+)", Pattern.CASE_INSENSITIVE);
    private WebClient webClient;
    private HtmlPage htmlPage;
    private int season;
    private int episode;
    private String url;

    public BrbToParser() {
        webClient = new WebClient(BrowserVersion.INTERNET_EXPLORER_11);
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
        if (true)
            return true;
        HtmlElement status = htmlPage.getFirstByXPath(STATUS_XPATH);
        Matcher statusMatcher = statusPattern.matcher(status.getTextContent());
        if (!statusMatcher.matches()) {
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
    public String getNext(final int season, final int episode, String quality) throws IOException {
        HtmlElement listButton = htmlPage.getFirstByXPath("//div[@id='page-item-file-list']//a[@class='b-files-folders-link']");
        listButton.click();
        List<HtmlElement> seasonElements = (List<HtmlElement>) htmlPage.getByXPath("//div[@class='b-files-folders']//li[@class='folder']");
        logger.log(Level.FINE, "Looking through {0} season elements", seasonElements.size());
        for (HtmlElement seasonElement : seasonElements) {
            String name = ((HtmlElement) seasonElement.getFirstByXPath(".//a/b")).getTextContent();
            Matcher seasonMatcher = numberPattern.matcher(name);
            if (!seasonMatcher.find()) {
                logger.log(Level.FINE, "{0} not a season", name);
                continue;
            }
            int foundSeason = Integer.parseInt(seasonMatcher.group());
            if (foundSeason < season) {
                logger.log(Level.FINE, "season {0} less than needed {1}", new Object[]{foundSeason, season});
                continue;
            }
            this.season = foundSeason;
//            String date = ((HtmlElement) seasonElement.getFirstByXPath("span[@class='material-date']")).getTextContent();
//            System.out.println(name + " " + date);
            final int desiredEpisode = foundSeason == season ? episode : 0; //  if there is no more episodes, let's take first from next season
            HtmlElement seasonButton = seasonElement.getFirstByXPath(".//a");
            seasonButton.click();
            List<HtmlElement> languages = (List<HtmlElement>) seasonElement.getByXPath(".//li[@class='folder folder-language ']");
            languages.stream().filter(language -> {
                logger.log(Level.FINE, ((HtmlElement) language.getFirstByXPath("div/a")).getTextContent());
                HtmlElement languageButton = language.getFirstByXPath("./div/a");
                click(languageButton);
                List<HtmlElement> translations = (List<HtmlElement>) language.getByXPath(".//li[contains(@class, 'folder-translation') and contains(div/div/a/text(), '" + quality + "')]");
                logger.log(Level.FINE, "translations {0}", translations.size());
                return translations.stream().filter(translation -> {
                    logger.log(Level.FINE, "\t" + ((HtmlElement) translation.getFirstByXPath("div/a")).getTextContent());
                    HtmlElement translationButton = translation.getFirstByXPath("./div/a[@class='link-subtype title']");
                    click(translationButton);
//                    HtmlElement qualityButton = translation.getFirstByXPath("./div/div/a");
//                    click(qualityButton);
                    List<HtmlElement> lis = (List<HtmlElement>) translation.getByXPath("./ul/li[contains(span/text(), '" + quality + "')]");
                    return lis.stream().filter(li -> {
                        logger.log(Level.FINE, "\t\tlooking in {0}", li.asText());
                        HtmlAnchor link = li.getFirstByXPath(".//a[contains(@class, 'b-file-new__link-material-download')]");
                        if (link == null) {
                            logger.log(Level.WARNING, "download link not found for {0}", li);
                            return false;
                        }
                        String file = link.getHrefAttribute();
                        Matcher episodeMatcher = episodePattern.matcher(file);
                        if (!episodeMatcher.find()) {
                            return false;   //  it isn't an episode
                        }
                        int foundEpisode = Integer.parseInt(episodeMatcher.group(1));
                        if (foundEpisode > desiredEpisode) {
                            this.episode = foundEpisode;
                            url = "http://brb.to" + file;
                            logger.log(Level.FINE, "Found. url={0}", url);
                            return true;
                        }
                        return false;
                    }).findFirst().isPresent();
                }).findFirst().isPresent();
            }).findFirst();
            if (url != null) {
                return url;
            }
            logger.log(Level.FINE, "Looks like next episode hasn''t been found in season {0}", foundSeason);
        }
        logger.log(Level.WARNING, "specified season {0} was not found", season);
        return null;    //  specified season was not found
    }

    private void click(HtmlElement htmlElement) {
        try {
            htmlElement.click();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getSeason() {
        return season;
    }

    public int getEpisode() {
        return episode;
    }

    @Override
    public void close() throws IOException {
        webClient.close();
    }
}
