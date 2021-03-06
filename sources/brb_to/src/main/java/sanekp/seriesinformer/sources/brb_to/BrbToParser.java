package sanekp.seriesinformer.sources.brb_to;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(BrbToParser.class);

    private static final String STATUS_XPATH = "//div[@class='item-info']/table/tbody/tr[3]/td[2]";
    private static Pattern statusPattern = Pattern.compile("\\D+(\\d+)\\D+(\\d+)\\D+");
    private static Pattern numberPattern = Pattern.compile("\\d+");
    private static Pattern episodePattern = Pattern.compile("s\\d+e(\\d+)", Pattern.CASE_INSENSITIVE);
    private WebClient webClient;
    private HtmlPage htmlPage;
    private int season;
    private int episode;
    private String url;

    public BrbToParser() {
        webClient = new WebClient(BrowserVersion.BEST_SUPPORTED);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setCssEnabled(false);
//        webClient.setWebConnection(new FalsifyingWebConnection(webClient.getWebConnection()) {
//            @Override
//            public WebResponse getResponse(WebRequest request) throws IOException {
//                String host = request.getUrl().getHost();
//                if (host.contains("brb.to") || host.contains("fs.to") || host.contains("dotua.org")) {
//                    return super.getResponse(request);
//                } else {
//                    return createWebResponse(request, "", "text/plain");
//                }
//            }
//        });
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
        LOGGER.debug("Looking through {} season elements", seasonElements.size());
        for (HtmlElement seasonElement : seasonElements) {
            String name = ((HtmlElement) seasonElement.getFirstByXPath(".//a/b")).getTextContent();
            Matcher seasonMatcher = numberPattern.matcher(name);
            if (!seasonMatcher.find()) {
                LOGGER.debug("{} not a season", name);
                continue;
            }
            int foundSeason = Integer.parseInt(seasonMatcher.group());
            if (foundSeason < season) {
                LOGGER.debug("season {} less than needed {}", new Object[]{foundSeason, season});
                continue;
            }
            this.season = foundSeason;
//            String date = ((HtmlElement) seasonElement.getFirstByXPath("span[@class='material-date']")).getTextContent();
//            System.out.println(name + " " + date);
            final int desiredEpisode = foundSeason == season ? episode : 0; //  if there is no more episodes, let's take first from next season
            HtmlElement seasonButton = seasonElement.getFirstByXPath(".//a");
            seasonButton.click();
            List<HtmlElement> languages = (List<HtmlElement>) seasonElement.getByXPath(".//li[contains(@class, 'folder-language')]");
            languages.stream().filter(language -> {
                LOGGER.debug(((HtmlElement) language.getFirstByXPath("div/a")).getTextContent());
                HtmlElement languageButton = language.getFirstByXPath("./div/a");
                click(languageButton);
                List<HtmlElement> translations = (List<HtmlElement>) language.getByXPath(".//li[contains(@class, 'folder-translation') and contains(div/div/a/text(), '" + quality + "')]");
                LOGGER.debug("translations {}", translations.size());
                return translations.stream().filter(translation -> {
                    LOGGER.debug("\t" + ((HtmlElement) translation.getFirstByXPath("div/a")).getTextContent());
                    HtmlElement translationButton = translation.getFirstByXPath("./div/a[@class='link-subtype title']");
                    click(translationButton);
//                    HtmlElement qualityButton = translation.getFirstByXPath("./div/div/a");
//                    click(qualityButton);
                    List<HtmlElement> lis = (List<HtmlElement>) translation.getByXPath("./ul/li[contains(span/text(), '" + quality + "')]");
                    return lis.stream().filter(li -> {
                        LOGGER.debug("\t\tlooking in {}", li.asText());
                        HtmlAnchor link = li.getFirstByXPath(".//a[contains(@class, 'b-file-new__link-material-download')]");
                        if (link == null) {
                            LOGGER.warn("download link not found for {}", li);
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
                            LOGGER.debug("Found. url={}", url);
                            return true;
                        }
                        return false;
                    }).findFirst().isPresent();
                }).findFirst().isPresent();
            }).findFirst();
            if (url != null) {
                return url;
            }
            LOGGER.debug("Looks like next episode hasn't been found in season {}", foundSeason);
        }
        LOGGER.warn("specified season {} was not found", season);
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
