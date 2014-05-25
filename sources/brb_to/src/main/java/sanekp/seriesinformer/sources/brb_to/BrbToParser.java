package sanekp.seriesinformer.sources.brb_to;

import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Created by sanek_000 on 5/25/2014.
 */
public class BrbToParser {
    public void parse(URL url) throws IOException {
        WebClient webClient = null;
        try {
            webClient = new WebClient();
            webClient.setAjaxController(new NicelyResynchronizingAjaxController());
            HtmlPage htmlPage = webClient.getPage(url);
            HtmlElement folderButton = htmlPage.getFirstByXPath("//div[@id='page-item-file-list']//a");
            folderButton.click();
            List<HtmlElement> folders = (List<HtmlElement>) htmlPage.getByXPath("//div[@class='b-files-folders']//li[@class='folder' and contains(.//b/text(), 'сезон')]");
            System.out.println("Loaded: " + folders.size());
            for (HtmlElement htmlListItem : folders) {
                String date = ((HtmlElement) htmlListItem.getFirstByXPath("span[@class='material-date']")).getTextContent();
                String name = ((HtmlElement) htmlListItem.getFirstByXPath(".//a/b")).getTextContent();
                System.out.println(name + " " + date);
            }
        } finally {
            if (webClient != null) {
                webClient.closeAllWindows();
            }
        }
    }
}
