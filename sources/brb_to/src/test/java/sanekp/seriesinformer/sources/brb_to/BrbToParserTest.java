package sanekp.seriesinformer.sources.brb_to;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by sanek_000 on 5/25/2014.
 */
public class BrbToParserTest {
    @BeforeClass
    public static void setUpClass() {
        Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
    }

    @Test
    public void testParse() {
        BrbToParser parser = new BrbToParser();
        try {
            URL url = new URL("http://brb.to/video/serials/i3TEloA2B4eGCdx0A9Xelj2-teoriya-bolshogo-vzryva.html");
            String file = parser.getNext(url, 7, 18, "1080");
            System.out.println(file);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
