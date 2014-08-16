package sanekp.seriesinformer.ui.xml;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.net.URL;

/**
 * Created by sanek_000 on 5/25/2014.
 */
public class XmlManager {

    private final JAXBContext jaxbContext;
    private Unmarshaller unmarshaller;
    private Marshaller marshaller;

    public XmlManager() throws JAXBException {
        jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "http://seriesinformer.sanekp dbschema.xsd");
        unmarshaller = jaxbContext.createUnmarshaller();
    }

    public SeriesList load(URL file) throws JAXBException {
        return (SeriesList) unmarshaller.unmarshal(file);
    }

    public void save(SeriesList seriesList, File file) throws JAXBException {
        marshaller.marshal(seriesList, file);
    }
}
