package sanekp.seriesinformer.core.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sanek_000 on 5/25/2014.
 */
@Component
public class DbManager {
    private static Logger logger = LoggerFactory.getLogger(DbManager.class);
    private Unmarshaller unmarshaller;
    private Marshaller marshaller;
    private SimpleDateFormat simpleDateFormat;

    @Value("${db.path}")
    private String dbPath;
    @Value("commit.log")
    private String commitLog;

    public DbManager() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "http://seriesinformer.sanekp dbschema.xsd");
        unmarshaller = jaxbContext.createUnmarshaller();

        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    public SeriesList load() {
        try {
            SeriesList seriesList = (SeriesList) unmarshaller.unmarshal(new File(dbPath));
            logger.debug("Loaded {}", seriesList);
            return seriesList;
        } catch (JAXBException e) {
            logger.warn("Failed to load series", e);
        }
        return null;
    }

    public void update(Series seriesToUpdate) {
        try (FileWriter fileWriter = new FileWriter(commitLog, true)) {
            fileWriter.write(simpleDateFormat.format(new Date()) + "," + seriesToUpdate.getName() + "," + seriesToUpdate.getSeason() + "," + seriesToUpdate.getEpisode() + "\n");
            SeriesList seriesList = load();
            for (Series series : seriesList.getSeries()) {
                if (series.getName().equals(seriesToUpdate.getName())) {
                    series.setSeason(seriesToUpdate.getSeason());
                    series.setEpisode(seriesToUpdate.getEpisode());
                    marshaller.marshal(seriesList, new File(dbPath));
                    break;
                }
            }
            logger.debug("Updated {}", seriesToUpdate.getName());
        } catch (JAXBException | IOException e) {
            logger.warn("Failed to store series", e);
        }
    }
}
