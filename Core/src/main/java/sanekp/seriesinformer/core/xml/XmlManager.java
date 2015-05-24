package sanekp.seriesinformer.core.xml;

import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * Created by sanek_000 on 5/25/2014.
 */
@Component
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

    public SeriesList load(File file) throws JAXBException {
        return (SeriesList) unmarshaller.unmarshal(file);
    }

    public void save(SeriesList seriesList, File file) throws JAXBException {
        marshaller.marshal(seriesList, file);
    }
}
