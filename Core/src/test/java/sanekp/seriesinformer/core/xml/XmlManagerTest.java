package sanekp.seriesinformer.core.xml;

import org.junit.Before;

import javax.xml.bind.JAXBException;

/**
 * Created by sanek_000 on 5/25/2014.
 */
public class XmlManagerTest {
    private DbManager dbManager;

    @Before
    public void setUp() throws JAXBException {
        dbManager = new DbManager();
    }

//    @Test
//    public void testRead() throws JAXBException, MalformedURLException {
//        SeriesList seriesList = xmlManager.load(Thread.currentThread().getContextClassLoader().getResource("db/db.xml"));
//        Assert.assertNotNull(seriesList);
//        List<Series> series = seriesList.getSeries();
//        Assert.assertThat(series, is(notNullValue()));
//        System.out.println(series.size());
//    }
//
//    @Test
//    public void testWrite() throws JAXBException, URISyntaxException {
//        SeriesList seriesList = xmlManager.load(Thread.currentThread().getContextClassLoader().getResource("db/db.xml"));
//        Series series = new Series();
//        series.setName("Test name");
//        seriesList.getSeries().add(series);
//        URL folder = Thread.currentThread().getContextClassLoader().getResource("db");
//        xmlManager.update(seriesList, new File(new URI(folder.toString() + "/test.sanekp.seriesinformer.core.xml")));
//    }
}
