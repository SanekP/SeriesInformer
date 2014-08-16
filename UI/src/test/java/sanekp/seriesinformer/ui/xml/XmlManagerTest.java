package sanekp.seriesinformer.ui.xml;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

/**
 * Created by sanek_000 on 5/25/2014.
 */
public class XmlManagerTest {
    private XmlManager xmlManager;

    @Before
    public void setUp() throws JAXBException {
        xmlManager = new XmlManager();
    }

    @Test
    public void testRead() throws JAXBException, MalformedURLException {
        SeriesList seriesList = xmlManager.load(Thread.currentThread().getContextClassLoader().getResource("db/db.xml"));
        Assert.assertNotNull(seriesList);
        List<Series> series = seriesList.getSeries();
        Assert.assertThat(series, is(notNullValue()));
        System.out.println(series.size());
    }

    @Test
    public void testWrite() throws JAXBException, URISyntaxException {
        SeriesList seriesList = xmlManager.load(Thread.currentThread().getContextClassLoader().getResource("db/db.xml"));
        Series series = new Series();
        series.setName("Test name");
        seriesList.getSeries().add(series);
        URL folder = Thread.currentThread().getContextClassLoader().getResource("db");
        xmlManager.save(seriesList, new File(new URI(folder.toString() + "/test.xml")));
    }
}
