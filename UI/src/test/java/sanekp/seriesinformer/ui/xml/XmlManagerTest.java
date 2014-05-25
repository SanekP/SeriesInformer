package sanekp.seriesinformer.ui.xml;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

/**
 * Created by sanek_000 on 5/25/2014.
 */
public class XmlManagerTest {
    private XmlManager xmlManager;

    @Before
    public void setUp() {
        try {
            xmlManager = new XmlManager();
        } catch (JAXBException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testRead() {
        try {
            SeriesList load = xmlManager.load(new File("C:\\Users\\sanek_000\\Documents\\SeriesInformer\\db.xml"));
            Assert.assertNotNull(load);
            List<Series> seriesList = load.getSeries();
            Assert.assertThat(seriesList, is(notNullValue()));
            System.out.println(seriesList.size());
        } catch (JAXBException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testWrite() {
        SeriesList seriesList = new SeriesList();
        Series series = new Series();
        series.setName("Test name");
        seriesList.getSeries().add(series);
        try {
            xmlManager.save(seriesList, new File("C:\\Users\\sanek_000\\Documents\\SeriesInformer\\test.xml"));
        } catch (JAXBException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
