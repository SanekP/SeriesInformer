package sanekp.seriesinformer.sources.brb_to;

import org.hamcrest.CoreMatchers;
import org.junit.*;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by sanek_000 on 5/25/2014.
 */
public class BrbToParserTest {
    private BrbToParser parser;

    @BeforeClass
    public static void setUpClass() {
        Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
    }

    @Before
    public void setUp() throws IOException {
        parser = new BrbToParser();
        URL url = new URL("http://brb.to/video/serials/i3TEloA2B4eGCdx0A9Xelj2-teoriya-bolshogo-vzryva.html");
        parser.open(url);
    }

    @Test
    public void testIsNext() {
        Assert.assertThat(parser.isNext(1, 1), CoreMatchers.is(true));
        Assert.assertThat(parser.isNext(100, 1), CoreMatchers.is(false));
    }

    @Test
    public void testGetNext() throws IOException {
        String file = parser.getNext(7, 18, "1080");
        System.out.println(file);
    }

    @After
    public void tearDown() throws IOException {
        parser.close();
    }
}
